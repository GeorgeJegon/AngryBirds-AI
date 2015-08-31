package ab.ai;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class RandomAgent implements Runnable {
  private ActionRobot aRobot;
  private Random randomGenerator;
  public int currentLevel = 1;
  public static int time_limit = 12;
  private TrajectoryPlanner tp;
  private boolean firstShot;
  private Point prevTarget;
  private int shotNumber = 0;
  private File knowledge;

  private List<BestShot> bestShots = new ArrayList<BestShot>();
  private List<Shot> listObjects = null;

  public RandomAgent() {
    aRobot = new ActionRobot();
    tp = new TrajectoryPlanner();
    prevTarget = null;
    firstShot = true;
    randomGenerator = new Random();
    bestShots = loadKnowledge();

    ActionRobot.GoFromMainMenuToLevelSelection();
  }

  public void saveKnowledge() {
    try{
      knowledge = new File("knowledge.txt");
      FileOutputStream saveFile = new FileOutputStream(knowledge, false);
      ObjectOutputStream save = new ObjectOutputStream(saveFile);
      save.writeObject(bestShots);
      save.close();
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  public List<BestShot> loadKnowledge() {
    List<BestShot> bestShots = null;
    try{
      // Look for a file named knowledge.txt.
      knowledge = new File("knowledge.txt");
      if (knowledge.isFile() && knowledge.canRead()){
        FileInputStream saveFile = new FileInputStream(knowledge);
        ObjectInputStream restore = new ObjectInputStream(saveFile);
        // If have a file restore the list of BestShots.
        bestShots = (List<BestShot>) restore.readObject();
        restore.close();
      }
    } catch(Exception exc){
      exc.printStackTrace();
    }

    // If don't have the file so create a new list of BestShots.
    if (bestShots == null) {
      bestShots = new ArrayList<BestShot>();
    }

    return bestShots;
  }

  // run the client
  public void run() {
    listObjects = new ArrayList<Shot>();
    aRobot.loadLevel(currentLevel);

    while (true) {
      System.out.println("Execute level " + currentLevel);

      GameState state = solve();
      if (state == GameState.WON) {
        int score = StateUtil.getScore(ActionRobot.proxy);
        // Create a new bestshot.
        BestShot bs = new BestShot(currentLevel, score, listObjects);

        // If is a new level, add to bestShots list.
        if (bestShots.size() == currentLevel - 1) {
          bestShots.add(bs);
          saveKnowledge();
        } else { // Update with the new score.
          if (score > bestShots.get(currentLevel - 1).getScore()) {
            bestShots.set(currentLevel - 1, bs);
            saveKnowledge();
          }
        }

        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        aRobot.loadLevel(++currentLevel);
        tp = new TrajectoryPlanner();
        firstShot = true;

        listObjects = new ArrayList<Shot>();
        shotNumber = 0;
      } else if (state == GameState.LOST) {
        listObjects = new ArrayList<Shot>();
        shotNumber = 0;
        System.out.println("Restart");
        aRobot.restartLevel();
      } else if (state == GameState.LEVEL_SELECTION) {
        System.out
          .println("Unexpected level selection page: "
            + currentLevel);
        aRobot.loadLevel(currentLevel);
      } else if (state == GameState.MAIN_MENU) {
        System.out
          .println("Unexpected main menu page: "
            + currentLevel);
        ActionRobot.GoFromMainMenuToLevelSelection();
        aRobot.loadLevel(currentLevel);
      } else if (state == GameState.EPISODE_MENU) {
        System.out
          .println("Unexpected episode menu page: "
            + currentLevel);
        ActionRobot.GoFromMainMenuToLevelSelection();
        aRobot.loadLevel(currentLevel);
      }
    }
  }

  public GameState solve() {
    boolean hasBestShot = bestShots.size() > currentLevel - 1
      && bestShots.get(currentLevel - 1) != null
      && bestShots.get(currentLevel - 1).getShots().size() > shotNumber;

    ABObject abObject = null;
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle sling = vision.findSlingshotMBR();

    while (sling == null && aRobot.getState() == GameState.PLAYING) {
      System.out.println("No slingshot detected. Please remove pop up or zoom out");
      ActionRobot.fullyZoomOut();
      screenshot = ActionRobot.doScreenShot();
      vision = new Vision(screenshot);
      sling = vision.findSlingshotMBR();
    }

    List<ABObject> objects = new ArrayList<ABObject>();
    List<Shot> shots = new ArrayList<Shot>();

    if (hasBestShot) {
      // Use the shots that learn to pass the level.
      shots = bestShots.get(currentLevel - 1).getShots();
    } else {
      // Get all screem itens.
      // objects = makeActionChoices(vision);
    }

    GameState state = aRobot.getState();

    // If there is a sling, then play, otherwise just skip.
    if (sling != null) {
      Shot shot = null;

      if (hasBestShot) {
        shot = shots.get(shotNumber);
        // Execute the knowledge best shot.
        state = executeShot(sling, shot, state, shot.getReleasePoint());
      } else {
        // Random pick up an object.
        abObject = objects.get(randomGenerator.nextInt(objects.size()));
        Point _tpt = abObject.getCenter();
        Point releasePoint = getReleasePoint(sling, _tpt);
        shot = createShot(abObject, sling, _tpt, releasePoint);

        if (shot == null) {
          System.err.println("No Release Point Found");
          return state;
        }

        state = executeShot(sling, shot, state, releasePoint);
      }
    }

    return state;
  }

  public Point getReleasePoint(Rectangle sling, Point _tpt) {
    Point releasePoint = null;
    // estimate the trajectory
    ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

    // do a high shot when entering a level to find an accurate velocity
    if (firstShot && pts.size() > 1) {
      releasePoint = pts.get(1);
    }
    else if (pts.size() == 1) {
      releasePoint = pts.get(0);
    } else if (pts.size() == 2) {
      // randomly choose between the trajectories, with a 1 in
      // 6 chance of choosing the high one
      if (randomGenerator.nextInt(6) == 0)
        releasePoint = pts.get(1);
      else
        releasePoint = pts.get(0);
    } else {
      if (pts.isEmpty()) {
        System.out.println("No release point found for the target");
        System.out.println("Try a shot with 45 degree");
        releasePoint = tp.findReleasePoint(sling, Math.PI/4);
      }
    }
    return releasePoint;
  }

  public int getTapTime(Rectangle sling, Point releasePoint, Point _tpt) {
    int tapInterval = 0;
    switch (aRobot.getBirdTypeOnSling()) {
      case RedBird:
        tapInterval = 0; break;                         // start of trajectory
      case YellowBird:
        tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
      case WhiteBird:
        tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
      case BlackBird:
        tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
      case BlueBird:
        tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
      default:
        tapInterval =  60;
    }
    return tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
  }

  private double distance(Point p1, Point p2) {
    return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
  }

  public Shot createShot(ABObject abObject, Rectangle sling, Point _tpt, Point releasePoint) {
    Shot shot = null;

    // point near it
    if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
      double _angle = randomGenerator.nextDouble() * Math.PI * 2;
      _tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
      _tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
      System.out.println("Randomly changing to " + _tpt);
    }

    prevTarget = new Point(_tpt.x, _tpt.y);

    // Get the reference point
    Point refPoint = tp.getReferencePoint(sling);

    //Calculate the tapping time according the bird type
    if (releasePoint != null) {
      int tapTime = getTapTime(sling, releasePoint, _tpt);
      int dx = (int)releasePoint.getX() - refPoint.x;
      int dy = (int)releasePoint.getY() - refPoint.y;

      shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime, releasePoint);
    }

    return shot;
  }

  public GameState executeShot(Rectangle sling, Shot shot, GameState state, Point releasePoint) {
    // check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
    ActionRobot.fullyZoomOut();
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle _sling = vision.findSlingshotMBR();
    if(_sling != null)
    {
      double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
      if(scale_diff < 25) {
        if(shot.getDx() < 0) {
          aRobot.cshoot(shot);
          listObjects.add(shot);
          shotNumber++;
          state = aRobot.getState();

          if (state == GameState.PLAYING) {
            screenshot = ActionRobot.doScreenShot();
            vision = new Vision(screenshot);
            List<Point> traj = vision.findTrajPoints();
            tp.adjustTrajectory(traj, sling, releasePoint);
            firstShot = false;
          }
        }
      } else {
        System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
      }
    } else {
      System.out.println("no sling detected, can not execute the shot, will re-segement the image");
    }
    return state;
  }

  public static void main(String args[]) {
    RandomAgent na = new RandomAgent();
    if (args.length > 0) {
      na.currentLevel = Integer.parseInt(args[0]);
    }
    na.run();
  }
}