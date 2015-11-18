package ab.ai.Heuristics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import ab.vision.ABObject;
import ab.vision.Vision;

@XmlRootElement
@XmlType(propOrder = { "name", "frequency"})
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

  @XmlAttribute
  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  @XmlTransient
  public int getBadDecreaseRate() {
    return badDecreaseRate;
  }

  public void setBadDecreaseRate(int badDecreaseRate) {
    this.badDecreaseRate = badDecreaseRate;
  }
  
  @XmlAttribute
  public String getName() {
    return this.getClass().getSimpleName();
  }

  public abstract int getHeuristicID();
}
