package ab.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ab.demo.other.Shot;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class Match {
  private UUID       id;
  private int        level;
  private int        score;
  private String     type;
  private List<Shot> shots;
  private String     heuristic;

  public Match() {
    id = UUID.randomUUID();
    shots = new ArrayList<Shot>();
  }

  @XmlAttribute
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @XmlAttribute
  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  @XmlAttribute
  public String getHeuristic() {
    return heuristic;
  }

  public void setHeuristic(String heuristic) {
    this.heuristic = heuristic;
  }

  @XmlAttribute
  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  @XmlElement(name = "shot")
  public List<Shot> getShots() {
    return shots;
  }

  public void setShots(List<Shot> shots) {
    this.shots = shots;
  }

  public void addShot(Shot shot) {
    this.shots.add(shot);
  }
}
