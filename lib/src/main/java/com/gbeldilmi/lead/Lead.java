package com.gbeldilmi.lead;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;

public class Lead { 
  private ActorSystem actorSystem;
  private ActorRef[] electionActorRefs;
  public Lead(ElectionCandidate... candidates) throws RuntimeException {
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
      lastActorRef = this.electionActorRefs[i] = actorSystem.actorOf(ElectionActor.props(candidates[i], lastActorRef));
    }
    this.electionActorRefs[0].tell(new ElectionActor.ActorRefMessage(lastActorRef), ActorRef.noSender());
  }
  public void elect() {
    this.electionActorRefs[0].tell(new ElectionActor.ElectionMessage(-1), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}
