package com.gbeldilmi.lead;

import javax.sound.midi.Receiver;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class LeadActor extends AbstractActor {
  private ActorRef nextActorRef;
  private int uid;
  private boolean isParticipant;

  private LeadActor(ActorRef nextActorRef) {
    this(nextActorRef, (int) (Math.random() * 1000000));
  }

  private LeadActor(ActorRef nextActorRef, int uid) {
    this.nextActorRef = nextActorRef;
    this.uid = uid;
    this.isParticipant = true;
    if (this.nextActorRef == null) {
      getContext().become(createReceiveFirst());
    }
  }

  @Override
  public Receive createReceive() {
    return createReceiveParticipant();
  }

  public Receive createReceiveFirst() {
    return receiveBuilder()
      .match(LeadActor.ActorRefMessage.class, message -> {
        this.nextActorRef = message.actorRef;
        getContext().become(createReceiveParticipant());
      })
      .build();
  }

  public Receive createReceiveParticipant() {
    return receiveBuilder()
      .match(LeadActor.ElectionMessage.class, message -> election(message))
      .match(LeadActor.ErrorMessage.class, message -> error(message))
      .build();
  }

  public Receive createReceiveNotParticipant() {
    return receiveBuilder()
      .match(LeadActor.ElectionMessage.class, message -> nextActorRef.forward(message, getContext()))
      .match(LeadActor.ErrorMessage.class, message -> error(message))
      .build();
  }

  public static Props props() {
    return Props.create(LeadActor.class);
  }
  
  public static Props props(int uid) {
    return Props.create(LeadActor.class, uid);
  }

  private void election(LeadActor.ElectionMessage message) {
    System.out.println("Election message received by " + this.uid);
    if (message.uid > this.uid) {
      System.out.println("Election message forwarded by " + this.uid);
      message.actorRef.tell(new LeadActor.ElectionMessage(message.uid, message.actorRef), this.getSelf());
    } else if (message.uid == this.uid) {
      System.out.println("Elected message sent by " + this.uid);
      message.actorRef.tell(new LeadActor.ElectedMessage(message.uid, message.actorRef), this.getSelf());
    } else {
      System.out.println("Error message sent by " + this.uid);
      message.actorRef.tell(new LeadActor.ErrorMessage(message.uid, message.actorRef), this.getSelf());
    }
  }

  private void error(LeadActor.ErrorMessage message) {
    System.out.println("Error message received by " + this.uid);
    if (message.uid == this.uid) {
      System.out.println("Election message sent by " + this.uid);
      message.actorRef.tell(new LeadActor.ElectionMessage(message.uid, message.actorRef), this.getSelf());
    } else {
      System.out.println("Error message forwarded by " + this.uid);
      message.actorRef.tell(new LeadActor.ErrorMessage(message.uid, message.actorRef), this.getSelf());
    }
  }

  private void resetElection() {
    this.isParticipant = true;
    getContext().become(createReceiveFirst());
  }

  public interface Message {}  
  
  public static class ElectionMessage implements Message {
    public final int uid;

    public ElectionMessage(int uid) {
      this.uid = uid;
    }
  }

  public static class ErrorMessage extends Message {
    public final int uid;

    public ErrorMessage(int uid) {
      this.uid = uid;
    }
  }

  public static class ActorRefMessage extends Message {
    public final ActorRef actorRef;

    public ActorRefMessage(ActorRef actorRef) {
      this.actorRef = actorRef;
    }
  }
}
