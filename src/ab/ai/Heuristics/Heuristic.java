package ab.ai.Heuristics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import ab.ai.agents.Agent;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class Heuristic {
  private int             frequency;
  private int             badDecreaseRate;
  private String          name;
  protected Random        randomGenerator;

  public final static int HEURISTIC_VALUE = 100;

  public Heuristic() {
    this(HEURISTIC_VALUE);
    this.badDecreaseRate = 1;
    this.randomGenerator = new Random();
    this.name = this.getClass().getSimpleName();
  }

  public Heuristic(int frequency) {
    this.frequency = frequency;
  }

  public GameState solve(Agent agent, GameState state, Vision vision,
      Rectangle sling, BufferedImage screenshot, ABType birdOnSling) {
    return GameState.PLAYING;
  }

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
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getHeuristicID() {
    return -1;
  }
}
