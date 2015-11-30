package ab.ai.Heuristics;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ab.ai.agents.Agent;
import ab.demo.other.Shot;
import ab.utils.ABUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionUtils;

public class BaseSupportersHeuristic extends Heuristic {

  @Override
  public GameState solve(Agent agent, GameState state, Vision vision,
      Rectangle sling, BufferedImage screenshot, ABType birdOnSling) {
    // TODO Auto-generated method stub
    Shot shot = null;
    ABObject target = null;
    Point targetCenter = null;
    Point releasePoint = null;
    List<ABObject> pigs = new ArrayList<ABObject>();
    List<ABObject> supporters = new ArrayList<ABObject>();

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
      }

      targetCenter = target.getCenter();
      releasePoint = agent.getReleasePoint(sling, targetCenter);
      shot = agent.createShot(target, sling, targetCenter, releasePoint);

      if (shot != null) {
        shot.setBirdOnSling(birdOnSling.toString());
        shot.setTargetType(target.type.toString());

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
