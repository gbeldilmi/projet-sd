package com.gbeldilmi.lead;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;

public class Lead { 
  private ActorSystem actorSystem;
  private ActorRef[] electionActorRefs;
  public Lead(ElectionCandidate... candidates) {
    // Create actors :
    // --> first created with next actor ref null
    // --> next created with previous actor ref
    // --> etc...
    // --> set first ref to last created via message
    ActorRef lastActorRef = null;
    actorSystem = ActorSystem.create();
    int i = 0;
    if (candidates.length == 0) {
      throw new RuntimeException("No candidates");
    }
    this.electionActorRefs = new ActorRef[candidates.length];
    for (i = 0; i < candidates.length; i++) {
      lastActorRef = this.electionActorRefs[i] = actorSystem.actorOf(LeadActor.props(candidates[i], lastActorRef));
    }
    this.electionActorRefs[0].tell(new LeadActor.ActorRefMessage(lastActorRef), ActorRef.noSender());
  }
  public void elect() {
    this.electionActorRefs[0].tell(new LeadActor.ElectionMessage(-1), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}

/*
 * ActorSystem actorSystem = ActorSystem.create("Client", ConfigFactory.load("client.conf"));
        ActorSelection selection = actorSystem.actorSelection("akka://myActorSystem@127.0.0.1:8000/user/helloActor");
        selection.tell(new HelloWorldMessage.SayHello("Akka Remote"), ActorRef.noSender());
        // En attente de Ctrl-C        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            actorSystem.terminate();
            System.out.println("System terminated.");
        }));
 * ActorSystem actorSystem = ActorSystem.create("myActorSystem");
		ActorRef helloActor = actorSystem.actorOf(HelloWorldActor.props(), "helloActor");		
        // En attente de Ctrl-C        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            actorSystem.terminate();
            System.out.println("System terminated.");
        }));
 */
