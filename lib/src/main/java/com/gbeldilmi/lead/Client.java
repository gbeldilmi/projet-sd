package com.gbeldilmi.lead;

import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class Client { 
  private ActorSystem actorSystem;
  private ActorSelection selection;
  public Client(int id) throws RuntimeException {
    if (id < 0) {
      throw new RuntimeException("Id must be positive.");
    }
    actorSystem = ActorSystem.create("client", ConfigFactory.load("client.conf"));
    selection = actorSystem.actorSelection("akka://server@127.0.0.1:8000/user/ea_" + id);
  }
  public void elect() {
    this.selection.tell(new ElectionActor.ElectionMessage(-1), ActorRef.noSender());
  }
  public void terminate() {
    actorSystem.terminate();
  }
}
