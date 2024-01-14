package com.gbeldilmi.lead;

import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;

public class Server { 
  private ActorSystem actorSystem;
  private ActorRef[] electionActorRefs;
  public Server(int nb_clients) throws RuntimeException {
    ActorRef lastActorRef = null;
    int i;
    if (nb_clients == 0) {
      throw new RuntimeException("Not enough clients.");
    }
    actorSystem = ActorSystem.create("server", ConfigFactory.load("server.conf"));
    // Create actors :
    // --> first created with next actor ref null
    // --> next created with previous actor ref
    // --> etc...
    // --> set first ref to last created via message
    this.electionActorRefs = new ActorRef[nb_clients];
    for (i = 0; i < nb_clients; i++) {
      lastActorRef = this.electionActorRefs[i] = actorSystem.actorOf(ElectionActor.props(i, lastActorRef), "ea_" + i);
    }
    this.electionActorRefs[0].tell(new ElectionActor.ActorRefMessage(lastActorRef), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}
