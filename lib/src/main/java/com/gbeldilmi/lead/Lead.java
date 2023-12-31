package com.gbeldilmi.lead;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.HashMap;
import java.util.Map;

public class Lead { 
  private ActorSystem as, nextAs;
  private final int basePort = 8878;
  public Lead(int nbAs, int noNode, String nextNodeHostname, LeadCandidate... candidates) throws RuntimeException {
    // This constructor is divided to simplify the code reading and understandability
    checkErrors(nbAs, noNode, candidates);
    createActorSystems(nbAs, noNode, nextNodeHostname);
    linkActorSystems(nbAs, noNode, nextNodeHostname, createLeadActors(noNode, candidates));
  }

  private void checkErrors(int nbAs, int noNode, LeadCandidate... candidates) throws RuntimeException {
    // Check possible errors before creating actor systems
    if (nbAs < 1) {
      throw new RuntimeException("Invalid number of actor systems");
    }
    if (noNode < 0 || noNode > nbAs - 1) {
      throw new RuntimeException("Invalid node number");
    }
    if (candidates.length == 0) {
      throw new RuntimeException("No actors");
    }
  }

  private void createActorSystems(int nbAs, int noNode, String nextNodeHostname) {
    as = ActorSystem.create("AS");
    nextAs = ActorSystem.create("nextAS", createConfig(nextNodeHostname, basePort + (noNode + 1 < nbAs ? noNode + 1 : 0)));
  }

  private Config createConfig(String hostname, int port) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("akka.actor.provider", "remote");
    map.put("akka.actor.allow-java-serialization", "true");
    map.put("akka.actor.warn-about-java-serializer-usage", "false");
    map.put("akka.remote.artery.transport", "tcp");
    map.put("akka.remote.artery.canonical.hostname", hostname);
    map.put("akka.remote.artery.canonical.port", port);
    return ConfigFactory.parseMap(map);
  }

  private ActorRef createLeadActors(int noNode, LeadCandidate... candidates) {
    // Create lead actors :
    // --> last created with next actor ref as the first actor ref of the next actor system
    // --> previous created with the actor ref of the last created actor
    // --> etc...
    // --> first created with the actor ref of the last created actor
    // --> return the actor ref of the last created actor for linking purpose
    ActorRef ars[] = new ActorRef[candidates.length], lastAr = null;
    for (int i = 0; i < candidates.length; i++) {
      lastAr = ars[i] = as.actorOf(LeadActor.props(lastAr, candidates[i]), "leadActor" + i + "AS" + noNode);
    }
    return lastAr;
  }

  private void linkActorSystems(int nbAs, int noNode, String nextNodeHostname, ActorRef lastAr) {
    ActorSelection s = nextAs.actorSelection("akka://AS@" + nextNodeHostname + ":" 
        + (basePort + (noNode + 1 < nbAs ? noNode + 1 : 0)) + "/" + System.getProperty("user.name")
        + "/leadActor0");
    s.tell(new LeadActor.ActorRefMessage(lastAr), ActorRef.noSender());
  }

  /*public void elect() {
    this.electionActorRefs[0].tell(new LeadActor.ElectionMessage(-1), ActorRef.noSender());
  }*/
  public void terminate() {
    as.terminate();
  }
}

/*
 * ActorSystem actorSystem = ActorSystem.create("Client", ConfigFactory.load("client.conf"));
        ActorSelection selection = actorSystem.actorSelection("akka://myActorSystem@127.0.0.1:8000/user/helloActor");
        selection.tell(new HelloWorldMessage.SayHello("Akka Remote"), ActorRef.noSender());
 * ActorSystem actorSystem = ActorSystem.create("myActorSystem");
		ActorRef helloActor = actorSystem.actorOf(HelloWorldActor.props(), "helloActor");
 */
