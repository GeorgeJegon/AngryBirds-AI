package ab.ai.Heuristics;

import ab.vision.ABObject;
import ab.vision.Vision;

public abstract class Heuristic {
  private int             frequency;
  private int             badDecreaseRate;

  public final static int HEURISTIC_VALUE = 100;

  public Heuristic() {
    this.frequency = HEURISTIC_VALUE;
    this.badDecreaseRate = 1;
  }

  public abstract ABObject solve(Vision vision);

  public void bad() {
    bad(this.badDecreaseRate);
  }

  public void bad(int number_to_decrease) {
    this.frequency -= number_to_decrease;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public int getBadDecreaseRate() {
    return badDecreaseRate;
  }

  public void setBadDecreaseRate(int badDecreaseRate) {
    this.badDecreaseRate = badDecreaseRate;
  }

  public abstract int getHeuristicID();
}
