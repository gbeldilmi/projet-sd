package com.gbeldilmi.lead;

import java.rmi.Remote;

public interface ElectionCandidate extends Remote  {
  public void elected();
  public void unelected();
}

