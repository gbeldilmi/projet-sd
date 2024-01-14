package com.gbeldilmi.lead_example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.gbeldilmi.lead.Client;

public class App {
  static final int NB_CANDIDATES = 10;
  static Client clt;
  static int id;
  public static void main(String[] args) {
    Foo[] foos = new Foo[NB_CANDIDATES];
    id = askId();
    for (int i = 0; i < foos.length; i++) {
      foos[i] = new Foo(i);
    }
    try {
      clt = new Client(id, foos);
      clt.elect();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      clt.terminate();
      System.out.println("Client terminated.");
  }));
  }
  private static int askId() {
    int i = -1;
    while (i < 0) {
      try {
        System.out.print("Enter this client ID:");
        i = Integer.parseInt(scan());
      } catch (Exception e) {
        e.printStackTrace();
        i = -1;
      }
    }
    return i;
  }
  private static String scan() {
    String input;
    input = "";
    try {
      input = new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return input;
  }
}
