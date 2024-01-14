package com.gbeldilmi.lead;

import java.io.Serializable;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;

public class ElectionActor extends AbstractActor {
  private ElectionCandidate candidate;
  private ActorRef nextActorRef;
  private int uid;

  private ElectionActor(ElectionCandidate candidate, ActorRef nextActorRef) {
    setCandidate(candidate);
    this.nextActorRef = nextActorRef;
    if (this.nextActorRef == null) {
      getContext().become(createReceiveWaiting());
    }
    resetUid();
  }
  public static Props props(ElectionCandidate candidate, ActorRef nextActorRef) {
    return Props.create(ElectionActor.class, candidate, nextActorRef);
  }
  public void setCandidate(ElectionCandidate candidate) {
    this.candidate = candidate;
  }
  private void resetUid() {
    this.uid = (int) (Math.random() * 1000000);
  }

  // Events
  private void elected() {
    // this.candidate.elected(); // doesn't work
    System.out.println("Elected ---- UID : " + uid);
  }
  private void unelected() {
    // this.candidate.unelected(); // doesn't work
    System.out.println("Unelected -- UID : " + uid);
  }

  // States : (>>> : state // + : message // --> : actions)
  // >>> Waiting
  //   + CandidateMessage        --> set candidate and become candidate if nextActorRef setted
  //   + ActorRefMessage         --> set next ref and become candidate if candidate setted
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
  //   + (nothing)
  @Override
  public Receive createReceive() {
    return createReceiveCandidate();
  }
  public Receive createReceiveWaiting() {
    return receiveBuilder()
      .match(ElectionActor.CandidateMessage.class, message -> waitingCandidate(message))
      .match(ElectionActor.ActorRefMessage.class, message -> waitingRef(message))
      .build();
  }
  private void waitingCandidate(ElectionActor.CandidateMessage message) { // --> set candidate and become candidate if nextActorRef setted
    this.candidate = message.candidate;
    if (this.nextActorRef != null) {
      getContext().become(createReceiveCandidate());
    }
  }
  private void waitingRef(ElectionActor.ActorRefMessage message) { // --> set next ref and become candidate if candidate setted
    this.nextActorRef = message.actorRef;
    if (this.candidate != null) {
      getContext().become(createReceiveCandidate());
    }
  }
  public Receive createReceiveCandidate() {
    return receiveBuilder()
      .match(ElectionActor.ElectionMessage.class, message -> candidate(message))
      .match(ElectionActor.ElectedMessage.class, message -> candidate(message))
      .match(ElectionActor.ErrorMessage.class, message -> candidate(message))
      .build();
  }
  private void candidate(ElectionActor.ElectionMessage message) {
    if (message.uid > this.uid) { // if less --> forward message to next and become non candidate
      this.nextActorRef.forward(message, getContext());
      getContext().become(createReceiveNotCandidate());
    } else if (message.uid == this.uid) {
      if (getSender().equals(getSelf())) { // if same && sender --> send elected to next, become elected and action event
        this.nextActorRef.tell(new ElectionActor.ElectedMessage(this.uid), getSelf());
        getContext().become(createReceiveElectionEnded());
        this.elected();
      } else { // if same && !sender --> reset uid and send error to next
        resetUid();
        this.nextActorRef.tell(new ElectionActor.ErrorMessage(), getSelf());
      }
    } else { // if higher --> send my uid to next
      this.nextActorRef.tell(new ElectionActor.ElectionMessage(this.uid), getSelf());
    }
  }
  private void candidate(ElectionActor.ElectedMessage message) { // --> pass as unelected and forward
    this.unelected();
    this.nextActorRef.forward(message, getContext());
    getContext().become(createReceiveElectionEnded());
  }
  private void candidate(ElectionActor.ErrorMessage message) { // if sender --> send my uid to next
    if (getSender().equals(getSelf())) {
      this.nextActorRef.tell(new ElectionActor.ElectionMessage(this.uid), getSelf());
    } else { // else --> reset uid and forward
      resetUid();
      this.nextActorRef.forward(message, getContext());
    }
  }
  public Receive createReceiveNotCandidate() {
    return receiveBuilder()
      .match(ElectionActor.ElectionMessage.class, message -> notCandidate(message))
      .match(ElectionActor.ElectedMessage.class, message -> notCandidate(message))
      .match(ElectionActor.ErrorMessage.class, message -> notCandidate(message))
      .build();
  }
  private void notCandidate(ElectionActor.ElectionMessage message) { // --> forward
    this.nextActorRef.forward(message, getContext());
  }
  private void notCandidate(ElectionActor.ElectedMessage message) { // --> pass as unelected, forward and action event
    this.unelected();
    this.nextActorRef.forward(message, getContext());
    getContext().become(createReceiveElectionEnded());
  }
  private void notCandidate(ElectionActor.ErrorMessage message) { // --> reset and forward
    resetUid();
    this.nextActorRef.forward(message, getContext());
  }
  public Receive createReceiveElectionEnded() {
    return receiveBuilder().build(); // (nothing)
  }

  public interface Message extends Serializable  {}
  public static class CandidateMessage implements Message {
    public final ElectionCandidate candidate;
    public CandidateMessage(ElectionCandidate candidate) {
      this.candidate = candidate;
    }
  }
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
