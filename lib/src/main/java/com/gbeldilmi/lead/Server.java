package com.gbeldilmi.lead;

import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;

public class Server { 
  private ActorSystem actorSystem;
  private ActorRef[] electionActorRefs;
  public Server(int nb_clients, int nb_candidates_per_client) throws RuntimeException {
    ActorRef lastActorRef = null;
    int i, j;
    if (nb_clients == 0 || nb_candidates_per_client == 0) {
      throw new RuntimeException("Not enough candidates/clients.");
    }
    actorSystem = ActorSystem.create("server", ConfigFactory.load("server.conf"));
    // Create actors :
    // --> first created with next actor ref null
    // --> next created with previous actor ref
    // --> etc...
    // --> set first ref to last created via message
    this.electionActorRefs = new ActorRef[nb_clients * nb_candidates_per_client];
    for (i = 0; i < nb_clients; i++) {
      for (j = 0; j < nb_candidates_per_client; j++) {
        lastActorRef = this.electionActorRefs[i * nb_candidates_per_client + j] = actorSystem.actorOf(ElectionActor.props(null, lastActorRef), "ea_" + i + "_" + j);
      }
    }
    this.electionActorRefs[0].tell(new ElectionActor.ActorRefMessage(lastActorRef), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}
