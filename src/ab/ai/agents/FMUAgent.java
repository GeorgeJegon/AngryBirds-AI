package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.abTrajectory;
import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionMBR;

public class FMUAgent extends Agent implements Runnable {
  public ImageSegFrame frame = null;

  public FMUAgent() {
    super();
    ActionRobot.GoFromMainMenuToLevelSelection();
  }

  public static void main(String[] args) {
    FMUAgent agent = new FMUAgent();
    agent.run();
  }

  @Override
  protected GameState solve() {
    GameState state = GameState.PLAYING;

    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle sling = vision.findSlingshotMBR();
    List<ABObject> listPigs = vision.findPigsMBR();

    ABObject pig = listPigs.get(0);
    Point _tpt = pig.getCenter();
    Point releasePoint = getReleasePoint(sling, _tpt);

    this.showTrajectory(sling, releasePoint);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }

    return state;
  }

  @Override
  protected void beforeRestartLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void afterRestartLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void beforeLoadNextLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void afterLoadNextLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onShotFinish(Shot currentShot) {

  }
}
