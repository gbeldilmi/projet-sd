package com.gbeldilmi.lead_example;

import com.gbeldilmi.lead.LeadCandidate;

public class Foo implements LeadCandidate {
  private int id;
  public Foo(int id) {
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
