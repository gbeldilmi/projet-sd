package com.gbeldilmi.lead_example;

import com.gbeldilmi.lead.ElectionCandidate;

public class Foo implements ElectionCandidate {
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
}
