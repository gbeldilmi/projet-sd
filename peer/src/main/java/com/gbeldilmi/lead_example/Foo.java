package com.gbeldilmi.lead_example;

import akka.actor.ActorRef;
import com.gbeldilmi.lead.LeadActor;

public class Foo extends LeadActor {
  private int id;
  public Foo(ActorRef nextActorRef, int id) {
    super(nextActorRef);
    this.id = id;
  }
  public void elected() {
    System.out.println("Foo " + this.id + " elected");
  }
  public void unelected() {
    System.out.println("Foo " + this.id + " not elected");
  }
  // Don't override createReceiveElectionEnded() because it's an example
}
