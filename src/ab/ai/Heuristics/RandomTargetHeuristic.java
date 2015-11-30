package ab.ai.Heuristics;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.ai.agents.Agent;
import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.GameStateExtractor.GameState;

public class RandomTargetHeuristic extends Heuristic {

  public RandomTargetHeuristic() {
    super();
  }

  @Override
  public GameState solve(Agent agent, GameState state, Vision vision,
      Rectangle sling, BufferedImage screenshot, ABType birdOnSling) {
    List<ABObject> listObjects = new ArrayList<ABObject>();
    ABObject target = null;
    Point targetCenter = null;
    Point releasePoint = null;
    Shot shot = null;

    listObjects.addAll(vision.findBlocksRealShape());
    listObjects.addAll(vision.findPigsRealShape());

    if (listObjects.size() > 0) {
      target = listObjects
          .get(this.randomGenerator.nextInt(listObjects.size()));
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
    return 0;
  }

}
