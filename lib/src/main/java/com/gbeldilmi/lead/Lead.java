package com.gbeldilmi.lead;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.FileWriter;
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
    as = ActorSystem.create("AS", createConfig("127.0.0.1", basePort + noNode));
    nextAs = ActorSystem.create("nextAS", createConfig(nextNodeHostname, basePort + (noNode + 1 < nbAs ? noNode + 1 : 0)));
  }

  private Config createConfig(String hostname, int port) {
    String configFilename = "as_lead_h_" + hostname + "_p_" + port + ".conf";
    File configFile = new File(configFilename);
    try {
      if (!configFile.exists()) {
        System.out.println("Creating config for " + hostname + ":" + port);
        String content = "akka {\nactor {\n# provider=remote is possible, but prefer cluster\n"
          + "provider = remote\nallow-java-serialization = true\nwarn-about-java-serializer-us"
          + "age = false\n}\nremote {\nartery {\ntransport = tcp # See Selecting a transport b"
          + "elow\ncanonical.hostname = \"" + hostname + "\"\ncanonical.port = " + port + "\n}"
          + "}\n}\n";
        FileWriter fw = new FileWriter(configFile);
        fw.write(content);
        fw.close();
      }
      System.out.println("Loading config for " + hostname + ":" + port);
      return ConfigFactory.load("conf");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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
    try {
      Thread.sleep(5000);
    } catch (Exception e) {}
    System.out.println("Linking actor systems\nakka://AS@" + nextNodeHostname + ":" 
        + (basePort + (noNode + 1 < nbAs ? noNode + 1 : 0)) + "/" + System.getProperty("user.name")
        + "/leadActor0");
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
