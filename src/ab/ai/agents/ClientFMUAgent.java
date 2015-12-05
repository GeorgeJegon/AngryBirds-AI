package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.Vision;
import ab.vision.GameStateExtractor.GameState;

public class ClientFMUAgent extends ClientAgent implements Runnable {

  public ClientFMUAgent() {
    super(1234);
  }

  public static void main(String args[]) {
    ClientFMUAgent na;
    na = new ClientFMUAgent();
    na.run();
  }

  protected GameState solve() {
    BufferedImage screenshot = this.aRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle sling = vision.findSlingshotMBR();
    GameState state = this.aRobot.checkState();

    if (this.waitForSling(vision, sling, screenshot)) {
      List<ABObject> pigs = vision.findPigsMBR();

      if (sling != null) {

        // If there are pigs, we pick up a pig randomly and shoot it.
        if (!pigs.isEmpty()) {
          Point releasePoint = null;
          // random pick up a pig
          ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));

          Point _tpt = pig.getCenter();
          Shot shot = null;

          releasePoint = this.getReleasePoint(sling, _tpt);
          shot = createShot(pig, sling, _tpt, releasePoint);

          if (shot == null) {
            System.err.println("No Release Point Found");
            return state;
          }

          state = executeShot(sling, shot, state, releasePoint);
        }
      }
    }

    return state;
  }

  @Override
  protected boolean beforeRestartLevel() {
    // TODO Auto-generated method stub
    System.out.println("Vou restartar um level");
    return true;
  }

  @Override
  protected void afterRestartLevel() {
    // TODO Auto-generated method stub
    System.out.println("Ja restartei um level");
  }

  @Override
  protected boolean beforeLoadNextLevel() {
    // TODO Auto-generated method stub
    System.out.println("Vou carregar um level");
    return true;
  }

  @Override
  protected void afterLoadNextLevel() {
    // TODO Auto-generated method stub
    System.out.println("Ja carreguei um level");
  }

  @Override
  protected void onShotFinish(Shot currentShot) {
    // TODO Auto-generated method stub
    System.out.println("Acabei de fazer um tiro");
  }

}
