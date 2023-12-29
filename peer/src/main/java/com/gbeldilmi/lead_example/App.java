package com.gbeldilmi.lead_example;

import com.gbeldilmi.lead.Lead;

public class App {
  static final int NB_ACTORSYSTEMS = 3;
  static final int NB_ACTORS_PER_SYSTEM = 3;
  private static Lead lead;
  public static void main(String[] args) {

    Foo[] foos = new Foo[NB_CANDIDATES];
    for (int i = 0; i < foos.length; i++) {
      foos[i] = new Foo(i);
    }
    try {
      lead = new Lead(foos);
      lead.elect();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      lead.terminate();
      System.out.println("System terminated.");
  }));
  }
}
