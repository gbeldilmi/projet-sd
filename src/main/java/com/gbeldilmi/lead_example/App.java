package com.gbeldilmi.lead_example;

import com.gbeldilmi.lead.Lead;

public class App {
  static final int NB_CANDIDATES = 10;
  public static void main(String[] args) {
    Foo[] foos = new Foo[NB_CANDIDATES];
    for (int i = 0; i < foos.length; i++) {
      foos[i] = new Foo(i);
    }
    Lead lead = new Lead(foos);
    try {
      lead.elect();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
  }
}
