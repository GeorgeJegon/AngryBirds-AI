package ab.utils;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ab.ai.Building;
import ab.ai.agents.Agent;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;

public class ABUtil {

  public static int                gap = 5;

  private static TrajectoryPlanner tp  = new TrajectoryPlanner();
  
  private static final int birdsBlocksDamage[][] = {
    //wood, ice, stone 
    { 600, 800, 300 }, // RED_BIRD
    { 900, 900, 700 }, // YELLOW_BIRD
    { 300, 700, 150 }, // BLUE_BIRD
    { 900, 900, 900 }, // BLACK_BIRD
    { 700, 950, 500 }  // WHITE_BIRD
  };
  
  private static void addObjectTrajectoryToHashMap(HashMap<ABObject, List<Point>> map,
      ABObject object, Point releasePoint) {
    List<Point> releasePoints = new ArrayList<Point>();

    if (map.containsKey(object)) {
      releasePoints = map.get(object);
    }

    releasePoints.add(releasePoint);
    map.put(object, releasePoints);
  }

  private static boolean trajectoryReachableHandler(
      HashMap<ABObject, List<Point>> map, Point releasePoint, Vision vision,
      ABObject target) {
    Point targetCenter = target.getCenter();

    if (ABUtil.isReachableFast(vision, targetCenter, releasePoint)) {
      ABUtil.addObjectTrajectoryToHashMap(map, target, releasePoint);
      return true;
    }
    return false;
  }

  public static HashMap<ABObject, List<Point>> findReachableObjectsTrajectory(
      Agent agent, List<ABObject> objects, Rectangle sling, Vision vision) {
    HashMap<ABObject, List<Point>> reachableObjects = new HashMap<ABObject, List<Point>>();
    List<Point> releasePoints = new ArrayList<Point>();
    ABObject target = null;
    Point targetPoint = null;
    Point releasePoint = null;

    for (ABObject object : objects) {
      target = object;
      targetPoint = target.getRandomMainPoint();
      releasePoints = agent.getReleasePoints(sling, targetPoint);
      if (releasePoints.size() > 0) {
        for (Point currentReleasePoint : releasePoints) {
          ABUtil.trajectoryReachableHandler(reachableObjects, currentReleasePoint, vision,
              target); 
        }
      } else {
        releasePoint = agent.getDefaultReleasePoint(sling);
        ABUtil.trajectoryReachableHandler(reachableObjects, releasePoint, vision,
            target);
      }
    }
    return reachableObjects;
  }

  public static List<ABObject> filterObjects(List<ABObject> objects, ABType type) {
    List<ABObject> listObjects = new ArrayList<ABObject>();

    for (ABObject currentObject : objects) {
      if (!(currentObject.type.equals(type))) {
        listObjects.add(currentObject);
      }
    }

    return listObjects;
  }

  public static ABObject nearestPig(List<ABObject> listPigs) {
    ABObject nearestPig = listPigs.get(0);
    for (ABObject pig : listPigs) {
      if (pig.getX() < nearestPig.getX()) {
        nearestPig = pig;
      }
    }
    return nearestPig;
  }

  public static ABObject highestPig(List<ABObject> listPigs) {
    ABObject highestPig = listPigs.get(0);
    for (ABObject pig : listPigs) {
      if (pig.getY() < highestPig.getY()) {
        highestPig = pig;
      }
    }
    return highestPig;
  }

  public static List<Building> findBuildingsWithPigs(List<ABObject> objects,
      List<ABObject> pigs) {
    List<Building> buildings = ABUtil.findBuildings(objects);
    List<Building> buildingsWithPigs = new ArrayList<Building>();

    for (Building building : buildings) {
      for (ABObject pig : pigs) {
        if (building.hasPig(pig)) {
          buildingsWithPigs.add(building);
          break;
        }
      }
    }

    return buildingsWithPigs;
  }

  public static List<Building> findBuildings(List<ABObject> objects) {
    List<ABObject> queue = new ArrayList<ABObject>(objects);
    List<Building> buildings = new ArrayList<Building>();

    while (!queue.isEmpty()) {
      buildings.add(ABUtil.findBuilding(queue));
    }

    return buildings;
  }

  public static Building findBuilding(List<ABObject> objects) {
    return findBuilding(objects.remove(0), objects);
  }

  public static Building findBuilding(ABObject initialObject,
      List<ABObject> objects) {
    List<ABObject> listObjects = objects;
    List<ABObject> connectedObjects = new ArrayList<ABObject>();
    Queue<ABObject> queue = new ArrayDeque<ABObject>();
    ABObject currentObject = null;

    queue.add(initialObject);

    while ((currentObject = queue.poll()) != null) {
      connectedObjects.add(currentObject);

      for (int i = 0; i < listObjects.size(); ++i) {
        if (currentObject.touches(listObjects.get(i))) {
          queue.add(listObjects.remove(i--));
        }
      }
    }

    return new Building(connectedObjects);
  }

  /**
   * Adds 1 px to polygon realshape for the sake of touch method
   */
  public static Area resize(Polygon p) {
    int shift = 2;

    Polygon p1 = new Polygon(p.xpoints, p.ypoints, p.npoints);
    Polygon p2 = new Polygon(p.xpoints, p.ypoints, p.npoints);
    Polygon p3 = new Polygon(p.xpoints, p.ypoints, p.npoints);
    Polygon p4 = new Polygon(p.xpoints, p.ypoints, p.npoints);

    p1.translate(shift, 0);
    p2.translate(-shift, 0);
    p3.translate(0, shift);
    p4.translate(0, -shift);

    Area ret = new Area(p1);
    ret.add(new Area(p2));
    ret.add(new Area(p3));
    ret.add(new Area(p4));

    return ret;
  }

  // If o1 supports o2, return true
  public static boolean isSupport(ABObject o2, ABObject o1) {
    if (o2.x == o1.x && o2.y == o1.y && o2.width == o1.width
        && o2.height == o1.height)
      return false;

    int ex_o1 = o1.x + o1.width;
    int ex_o2 = o2.x + o2.width;

    int ey_o2 = o2.y + o2.height;
    if ((Math.abs(ey_o2 - o1.y) < gap)
        && !(o2.x - ex_o1 > gap || o1.x - ex_o2 > gap))
      return true;

    return false;
  }

  // Return a link list of ABObjects that support o1 (test by isSupport function
  // ).
  // objs refers to a list of potential supporters.
  // Empty list will be returned if no such supporters.
  public static List<ABObject> getSupporters(ABObject o2, List<ABObject> objs) {
    List<ABObject> result = new LinkedList<ABObject>();
    // Loop through the potential supporters
    for (ABObject o1 : objs) {
      if (isSupport(o2, o1))
        result.add(o1);
    }
    return result;
  }

  // Return true if the target can be hit by releasing the bird at the specified
  // release point
  public static boolean isReachable(Vision vision, Point target, Shot shot) {
    // test whether the trajectory can pass the target without considering
    // obstructions
    Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY()
        + shot.getDy());
    int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint,
        target.x);
    if (Math.abs(traY - target.y) > 100) {
      return false;
    }
    boolean result = true;
    List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(),
        releasePoint);
    for (Point point : points) {
      if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
        for (ABObject ab : vision.findBlocksMBR()) {
          if (((ab.contains(point) && !ab.contains(target)) || Math.abs(vision
              .getMBRVision()._scene[point.y][point.x] - 72) < 10)
              && point.x < target.x)
            return false;
        }

    }
    return result;
  }
  
  public static boolean isReachableFast(Vision vision, Point target, Point releasePoint) {
    Rectangle sling = vision.findSlingshotMBR();
    List<ABObject> blocks = vision.findBlocksMBR();
    List<Point> points = new ArrayList<Point>();
    int traY = tp.getYCoordinate(sling, releasePoint, target.x);
   
    if (Math.abs(traY - target.y) > 100) {
      return false;
    }
  
    for (Point point : points) {
      if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
        for (ABObject ab : blocks) {
          if (((ab.contains(point) && !ab.contains(target)) || Math.abs(vision
              .getMBRVision()._scene[point.y][point.x] - 72) < 10)
              && point.x < target.x)
            return false;
        }
    }
    return true;
  }
}
