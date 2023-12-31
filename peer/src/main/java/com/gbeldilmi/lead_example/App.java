package com.gbeldilmi.lead_example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.gbeldilmi.lead.Lead;

public class App {
  static final int NB_ACTORSYSTEMS = 3;
  static final int NB_ACTORS_PER_SYSTEM = 3;
  static Lead lead;
  static String nextNodeHostname;
  static int noNode;
  public static void main(String[] args) {
    askConfig();
    try {
      lead = new Lead(NB_ACTORSYSTEMS, noNode, nextNodeHostname, createFoos(NB_ACTORS_PER_SYSTEM));
      // lead.elect();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      lead.terminate();
      System.out.println("System terminated.");
  }));
  }
  private static void askConfig() {
    System.out.print("Enter next node hostname or ip (default : 127.0.0.1) : ");
    nextNodeHostname = inpuString();
    if (nextNodeHostname.equals("")) nextNodeHostname = "127.0.0.1";
    System.out.print("Enter this node number (between " + 0 + " and " + (NB_ACTORSYSTEMS - 1) + ") : "); 
    noNode = inputInt(0, NB_ACTORSYSTEMS - 1);
  }
  private static Foo[] createFoos(int nb) {
    Foo[] foos = new Foo[nb];
    for (int i = 0; i < foos.length; i++) {
      foos[i] = new Foo(i);
    }
    return foos;
  }
  // Utilities
  private static String inpuString() {
    String input;
    input = "";
    try {
      input = new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (Exception error) {
      System.out.println(error);
    }
    return input;
  }
  private static int inputInt(int min, int max) {
    int input;
    input = min - 1;
    while (input < min || input > max) {
      try {
        input = Integer.parseInt(inpuString());
      } catch (Exception error) {
        System.out.println(error);
      }
      if (input < min || input > max) {
        System.out.print("Invalid input. Enter a number between " + min + " and " + max + " : ");
      }
    }
    return input;
  }
}
