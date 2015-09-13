package ab.ai;

import java.io.Serializable;
import java.util.List;


import ab.demo.other.Shot;

public class BestShot implements Serializable {
  private static final long serialVersionUID = 5003839097166308999L;

  private int level;
  private int score;
  private List<Shot> shots;

  public BestShot(final int level, final int score, final List<Shot> shots) {
    this.level = level;
    this.score = score;
    this.shots = shots;
  }

  public Shot get(int index) {
    return this.shots.get(index);
  }

  public List<Shot> getShots() {
    return this.shots;
  }

  public int getScore() {
    return this.score;
  }

  @Override
  public String toString() {
    return "BestShot [level=" + this.level + ", score=" + this.score + ", shots=" + this.shots + "]";
  }
}