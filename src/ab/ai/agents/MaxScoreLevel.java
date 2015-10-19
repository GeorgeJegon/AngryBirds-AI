package ab.ai.agents;

import java.awt.image.BufferedImage;

import ab.ai.Level;
import ab.ai.ListLevel;
import ab.ai.Util;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class MaxScoreLevel extends Agent implements Runnable {
  ListLevel                   listLevel       = loadListLevel();
  private static final String MAX_SCORE_LEVEL = "src/ab/data/level_max_score.xml";

  private ListLevel loadListLevel() {
    ListLevel loaded = (ListLevel) Util.loadXML(ListLevel.class,
        MAX_SCORE_LEVEL);
    return (loaded != null) ? loaded : new ListLevel();
  }

  public MaxScoreLevel() {
    super();
    this.sleepTime = 0;
    ActionRobot.GoFromMainMenuToLevelSelection();
  }

  public static void main(String[] args) {
    MaxScoreLevel agent = new MaxScoreLevel();
    agent.run();
  }

  @Override
  protected GameState solve() {
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Level level = new Level();

    if (this.currentLevel > this.listLevel.size()) {
      level.setId(this.currentLevel);
      level.setNumber_of_birds(vision.findBirdsRealShape().size());
      level.setNumber_of_blocks(vision.findBlocksRealShape().size());
      level.setNumber_of_pigs(vision.findPigsRealShape().size());
      level.setNumber_of_tnts(vision.findTNTs().size());
      level.defineMaxScoreAvaliable();

      this.listLevel.addLevel(level);
    } else {
      System.out.println("Já temos informações sobre este level");
    }

    return GameState.WON;
  }

  @Override
  protected void beforeRestartLevel() {
  }

  @Override
  protected void afterRestartLevel() {
  }

  @Override
  protected void beforeLoadNextLevel() {
    Util.saveXML(this.listLevel, MAX_SCORE_LEVEL);
  }

  @Override
  protected void afterLoadNextLevel() {
  }

  @Override
  protected void onShotFinish(Shot currentShot) {
  }
}
