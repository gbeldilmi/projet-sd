package com.gbeldilmi.lead;

import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class Client { 
  private ActorSystem actorSystem;
  private ActorSelection[] selections;
  private int id;
  public Client(int id, ElectionCandidate... candidates) throws RuntimeException {
    int i;
    if (id < 0) {
      throw new RuntimeException("Id must be positive.");
    }
    if (candidates.length == 0) {
      throw new RuntimeException("No candidates");
    }
    this.id = id;
    actorSystem = ActorSystem.create("client", ConfigFactory.load("client.conf"));
    selections = new ActorSelection[candidates.length];
    for (i = 0; i < selections.length; i++) {
      selections[i] = actorSystem.actorSelection("akka://server@127.0.0.1:8000/" + System.getProperty("user.name") + "/ea_" + id + "_" + i);
      selections[i].tell(new ElectionActor.CandidateMessage(candidates[i]), ActorRef.noSender());
    }
  }
  public void elect() {
    this.selections[0].tell(new ElectionActor.ElectionMessage(-1), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}
