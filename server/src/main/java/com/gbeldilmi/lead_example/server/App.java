package com.gbeldilmi.lead_example.server;

import com.gbeldilmi.lead.Server;

public class App {
  static final int NB_CLIENTS = 1,
                   NB_CANDIDATES_PER_CLIENT = 10;
  static Server srv;
  public static void main(String[] args) {
    try {
      srv = new Server(NB_CLIENTS, NB_CANDIDATES_PER_CLIENT);
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      srv.terminate();
      System.out.println("Server terminated.");
  }));
  }
}
