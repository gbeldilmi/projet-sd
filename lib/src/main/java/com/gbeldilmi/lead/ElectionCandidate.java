package com.gbeldilmi.lead;

import java.io.Serializable;

public interface ElectionCandidate extends Serializable  {
  public void elected();
  public void unelected();
}

