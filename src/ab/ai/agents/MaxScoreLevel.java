package ab.ai.agents;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ab.ai.Level;
import ab.ai.ListLevel;
import ab.ai.SceneState;
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
    SceneState scene = new SceneState(screenshot);
    Level level = new Level();

    level.setId(this.currentLevel);
    level.setNumber_of_birds(scene.getBirds().size());
    level.setNumber_of_blocks(scene.getBlocks().size());
    level.setNumber_of_pigs(scene.getPigs().size());
    level.setNumber_of_tnts(scene.getTnts().size());
    level.defineMaxScoreAvaliable();
    this.listLevel.addLevel(level);
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
