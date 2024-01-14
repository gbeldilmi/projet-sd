package com.gbeldilmi.lead_example.server;

import com.gbeldilmi.lead.Server;

public class App {
  static final int NB_CLIENTS = 3;
  static Server srv;
  public static void main(String[] args) {
    try {
      srv = new Server(NB_CLIENTS);
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      srv.terminate();
      System.out.println("Server terminated.");
  }));
  }
}
