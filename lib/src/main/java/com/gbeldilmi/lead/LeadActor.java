package com.gbeldilmi.lead;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;

public class LeadActor extends AbstractActor {
  private ActorRef nextActorRef;
  private int uid;

  protected LeadActor(ActorRef nextActorRef) {
    this.nextActorRef = nextActorRef;
    if (this.nextActorRef == null) {
      getContext().become(createReceiveWaitingRef());
    }
    resetUid();
  }
  public static Props props(ActorRef nextActorRef) {
    return Props.create(LeadActor.class, nextActorRef);
  }
  private void resetUid() {
    this.uid = (int) (Math.random() * 1000000);
  }

  // States : (>>> : state // + : message // --> : actions)
  // >>> WaitingRef
  //   + ActorRefMessage         --> set next ref and become candidate
  // >>> Candidate
  //   + ElectionMessage         --> compare
  //         (id = -1 to begin)     if less             --> forward message to next and become non candidate
  //                                if same && sender   --> send elected to next, become elected and action event
  //                                if same && !sender  --> reset uid and send error to next
  //                                if higher           --> send my uid to next
  //   + ElectedMessage          --> pass as unelected and forward
  //   + ErrorMessage            --> if sender          --> send my uid to next
  //                                else                --> reset uid and forward
  // >>> NotCandidate
  //   + ElectionMessage         --> forward
  //   + ElectedMessage          --> pass as unelected, forward and action event
  //   + ErrorMessage            --> reset and forward
  // >>> ElectionEnded  (= elected | unelected)
  //   + (nothing)               --> must be overrided to do something
  @Override
  public Receive createReceive() {
    return createReceiveCandidate();
  }
  public Receive createReceiveWaitingRef() {
    return receiveBuilder()
      .match(LeadActor.ActorRefMessage.class, message -> waitingRef(message))
      .build();
  }
  private void waitingRef(LeadActor.ActorRefMessage message) { // --> set next ref and become candidate
    this.nextActorRef = message.actorRef;
    getContext().become(createReceiveCandidate());
  }
  public Receive createReceiveCandidate() {
    return receiveBuilder()
      .match(LeadActor.ElectionMessage.class, message -> candidate(message))
      .match(LeadActor.ElectedMessage.class, message -> candidate(message))
      .match(LeadActor.ErrorMessage.class, message -> candidate(message))
      .build();
  }
  private void candidate(LeadActor.ElectionMessage message) {
    if (message.uid > this.uid) { // if less --> forward message to next and become non candidate
      this.nextActorRef.forward(message, getContext());
      getContext().become(createReceiveNotCandidate());
    } else if (message.uid == this.uid) {
      if (getSender().equals(getSelf())) { // if same && sender --> send elected to next, become elected and action event
        this.nextActorRef.tell(new LeadActor.ElectedMessage(this.uid), getSelf());
        getContext().become(createReceiveElectionEnded());
        this.elected();
      } else { // if same && !sender --> reset uid and send error to next
        resetUid();
        this.nextActorRef.tell(new LeadActor.ErrorMessage(), getSelf());
      }
    } else { // if higher --> send my uid to next
      this.nextActorRef.tell(new LeadActor.ElectionMessage(this.uid), getSelf());
    }
  }
  private void candidate(LeadActor.ElectedMessage message) { // --> pass as unelected and forward
    this.unelected();
    this.nextActorRef.forward(message, getContext());
    getContext().become(createReceiveElectionEnded());
  }
  private void candidate(LeadActor.ErrorMessage message) { // if sender --> send my uid to next
    if (getSender().equals(getSelf())) {
      this.nextActorRef.tell(new LeadActor.ElectionMessage(this.uid), getSelf());
    } else { // else --> reset uid and forward
      resetUid();
      this.nextActorRef.forward(message, getContext());
    }
  }
  public Receive createReceiveNotCandidate() {
    return receiveBuilder()
      .match(LeadActor.ElectionMessage.class, message -> notCandidate(message))
      .match(LeadActor.ElectedMessage.class, message -> notCandidate(message))
      .match(LeadActor.ErrorMessage.class, message -> notCandidate(message))
      .build();
  }
  private void notCandidate(LeadActor.ElectionMessage message) { // --> forward
    this.nextActorRef.forward(message, getContext());
  }
  private void notCandidate(LeadActor.ElectedMessage message) { // --> pass as unelected, forward and action event
    this.unelected();
    this.nextActorRef.forward(message, getContext());
    getContext().become(createReceiveElectionEnded());
  }
  private void notCandidate(LeadActor.ErrorMessage message) { // --> reset and forward
    resetUid();
    this.nextActorRef.forward(message, getContext());
  }
  public Receive createReceiveElectionEnded() {
    return receiveBuilder().build(); // nothing, override to do something after election
  }

  // Events : (must be overrided)
  public void elected() {}
  public void unelected() {}

  public interface Message {}  
  public static class ActorRefMessage implements Message {
    public final ActorRef actorRef;
    public ActorRefMessage(ActorRef actorRef) {
      this.actorRef = actorRef;
    }
  }
  public static class ElectionMessage implements Message {
    public final int uid;
    public ElectionMessage(int uid) {
      this.uid = uid;
    }
  }
  public static class ElectedMessage implements Message {
    public final int uid;
    public ElectedMessage(int uid) {
      this.uid = uid;
    }
  }
  public static class ErrorMessage implements Message {
    public ErrorMessage() {}
  }
}
