package ab.ai.agents;

import java.awt.Rectangle;
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
  private static final String MAX_SCORE_LEVEL = "src/ab/data/level_info.xml";

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
    this.aRobot.click();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Level level = new Level();
    
    if (this.currentLevel > this.listLevel.size()) {
      level.setId(this.currentLevel);
      level.setNumber_of_birds(vision.findBirdsMBR().size());
      level.setNumber_of_blocks(vision.findBlocksMBR().size());
      level.setNumber_of_pigs(vision.findPigsMBR().size());
      level.setNumber_of_tnts(vision.findTNTs().size());
      level.defineMaxScoreAvaliable();

      this.listLevel.addLevel(level);
    } else {
      System.out.println("J� temos informa��es sobre este level");
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
