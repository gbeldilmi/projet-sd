package com.gbeldilmi.lead_example.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.gbeldilmi.lead.ElectionCandidate;

public class Foo extends UnicastRemoteObject implements ElectionCandidate {
  private int id;
  public Foo(int id) throws RemoteException {
    this.id = id;
  }
  public void elected() {
    System.out.println("Foo " + this.id + " elected");
  }
  public void unelected() {
    System.out.println("Foo " + this.id + " not elected");
  }
}
