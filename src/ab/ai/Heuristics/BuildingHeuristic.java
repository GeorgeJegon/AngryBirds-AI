package ab.ai.Heuristics;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ab.ai.Building;
import ab.ai.Util;
import ab.ai.agents.Agent;
import ab.demo.other.Shot;
import ab.utils.ABUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionUtils;

public class BuildingHeuristic extends Heuristic {

  @Override
  public GameState solve(Agent agent, GameState state, Vision vision,
      Rectangle sling, BufferedImage screenshot, ABType birdOnSling) {
    // TODO Auto-generated method stub
    List<ABObject> objects = new ArrayList<ABObject>();
    List<ABObject> pigs = new ArrayList<ABObject>();
    List<Building> buildings = new ArrayList<Building>();
    List<ABObject> supporters = new ArrayList<ABObject>();
    HashMap<ABObject, List<Point>> trajectoryMap = new HashMap<ABObject, List<Point>>();
    String screenShotFileName = agent.getScreenshotPath() + "level-"+ agent.currentLevel + "/tiro-" + agent.getListShots().size() + ".png";
    BufferedImage screenshotGrey = VisionUtils.convert2grey(screenshot);
    ABObject target = null;
    Point targetCenter = null;
    Point releasePoint = null;
    Shot shot = null;

    objects = vision.findBlocksRealShape();
    pigs = vision.findPigsRealShape();
    
    if (!objects.isEmpty() && !pigs.isEmpty()) {
        buildings = ABUtil.findBuildingsWithPigs(objects, pigs);
        
        if (!buildings.isEmpty()) {
          Building building = Util.getRandom(buildings);
          target = Util.getRandom(building.getComponents());
          trajectoryMap = ABUtil.findReachableObjectsTrajectory(agent, building.getComponents(), sling, vision);
        }
        
        if (target == null) {
          if (this.randomGenerator.nextFloat() >= 0.5) {
            target = Util.getRandom(pigs);
            System.out.println("Escolhendo um porco aleatorio.");
          } else {
            target = ABUtil.nearestPig(pigs);
            System.out.println("Procurando o porco mais proximo.");
          }
          
          supporters = target.getBaseSupporters(objects);
          
          if (!supporters.isEmpty()) {
            target = Util.getRandom(supporters);
          }
        }
       
      if (!trajectoryMap.isEmpty()) {
        List<ABObject> trajectoryMapKeys = new ArrayList<ABObject>(trajectoryMap.keySet());
        target = Util.getRandom(trajectoryMapKeys);
        releasePoint = Util.getRandom(trajectoryMap.get(target));
      }
      
      if (target != null) {
        targetCenter = target.getCenter();
        
        if (releasePoint == null) {
           releasePoint = agent.getReleasePoint(sling, targetCenter);
        }
        
        shot = agent.createShot(target, sling, targetCenter, releasePoint);
        
        
        if (shot != null) {
          shot.setBirdOnSling(birdOnSling.toString());
          shot.setTargetType(target.type.toString());
        
          VisionUtils.drawBoundingBox(screenshotGrey, target, Util.getRandomColor());
          agent.showTrajectory(screenshotGrey, sling, releasePoint, screenShotFileName);
  
          state = agent.executeShot(sling, shot, state, releasePoint);
        } else {
          System.err.println("No Release Point Found");
        }
      }
    }
    
//    Util.saveImage(screenshotGrey, screenShotFileName);

    return state;
  }

  @Override
  public int getHeuristicID() {
    // TODO Auto-generated method stub
    return 0;
  }

}
