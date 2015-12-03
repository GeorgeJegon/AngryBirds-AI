package ab.ai.Heuristics;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ab.ai.Util;
import ab.ai.agents.Agent;
import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.VisionUtils;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

@XmlRootElement
@XmlType(propOrder = { "name", "frequency" })
public class BaseSupportersHeuristic extends Heuristic {

  @Override
  public GameState solve(Agent agent, GameState state, Vision vision,
      Rectangle sling, BufferedImage screenshot, ABType birdOnSling) {
    // TODO Auto-generated method stub
    Shot shot = null;
    ABObject target = null;
    Point targetPoint = null;
    Point releasePoint = null;
    String targetPosition = new String();
    List<ABObject> pigs = new ArrayList<ABObject>();
    List<ABObject> supporters = new ArrayList<ABObject>();
    BufferedImage screenshotGrey = VisionUtils.convert2grey(screenshot);
    String screenShotFileName = agent.getScreenshotPath() + "level-"+ agent.currentLevel + "/tiro-" + agent.listShots.size() + ".png";
    List<Rectangle> listSupportDraw = new ArrayList<Rectangle>();

    pigs = vision.findPigsMBR();

    if (!pigs.isEmpty()) {
      
      if (pigs.size() > 1) {
        target = pigs.get(this.randomGenerator.nextInt(pigs.size()));
      } else {
        target = pigs.get(0);
      }
      
      supporters = target.getBaseSupporters(vision.findBlocksMBR());

      if (!supporters.isEmpty()) {
        target = supporters.get(0);
        listSupportDraw.addAll(supporters);
      }

      targetPoint = target.getRandomMainPoint();
      targetPosition = target.getRandomMainPointPosition();
      releasePoint = agent.getReleasePoint(sling, targetPoint);
      shot = agent.createShot(target, sling, targetPoint, releasePoint);

      if (shot != null) {
        shot.setBirdOnSling(birdOnSling.toString());
        shot.setTargetType(target.type.toString());
        shot.setTargetPointType(targetPosition);
        
        VisionUtils.drawBoundingBoxes(screenshotGrey, listSupportDraw, Util.getRandomColor());
        agent.showTrajectory(screenshotGrey, sling, releasePoint, screenShotFileName);

        state = agent.executeShot(sling, shot, state, releasePoint);
      } else {
        System.err.println("No Release Point Found");
      }
    }
    return state;
  }

  @Override
  public int getHeuristicID() {
    // TODO Auto-generated method stub
    return 2;
  }
}
