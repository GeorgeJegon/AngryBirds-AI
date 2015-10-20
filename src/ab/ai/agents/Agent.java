package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.ai.Util;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public abstract class Agent {
  private final String        SHOTS_FILE   = "src/ab/data/shots.xml";
  private final String        MATCHES_FILE = "src/ab/data/matches.xml";
  private ImageSegFrame       frame        = null;

  protected ActionRobot       aRobot;
  protected Random            randomGenerator;

  protected TrajectoryPlanner tp;
  protected boolean           firstShot;
  protected Point             prevTarget;

  public int                  currentLevel = 1;
  public static int           time_limit   = 12;
  public int                  sleepTime    = 3000;

  public Agent() {
    this.aRobot = new ActionRobot();
    this.tp = new TrajectoryPlanner();
    this.prevTarget = null;
    this.firstShot = true;
    this.randomGenerator = new Random();
  }

  protected void showTrajectory(BufferedImage screenshot, Rectangle sling,
      Point releasePoint) {
    this.tp.plotTrajectory(screenshot, sling, releasePoint);

    if (this.frame == null) {
      this.frame = new ImageSegFrame("trajectory", screenshot);
    } else {
      this.frame.refresh(screenshot);
    }
  }

  protected void showTrajectory(Rectangle sling, Point releasePoint) {
    BufferedImage screenshot = ActionRobot.doScreenShot();
    showTrajectory(screenshot, sling, releasePoint);
  }

  public void run() {
    aRobot.loadLevel(currentLevel);

    while (true) {
      System.out.println("Execute level " + currentLevel);

      GameState state = solve();

      switch (state) {
      case EPISODE_MENU:
        this.onEpisodeMenuState();
        break;
      case LEVEL_SELECTION:
        this.onLevelSelectionState();
        break;
      case LOADING:
        break;
      case LOST:
        this.onLostState();
        break;
      case MAIN_MENU:
        this.onMainMenuState();
        break;
      case PLAYING:
        break;
      case UNKNOWN:
        break;
      case WON:
        this.onWonState();
        break;
      }
    }
  }

  protected abstract GameState solve();

  protected abstract void beforeRestartLevel();

  protected abstract void afterRestartLevel();

  protected abstract void beforeLoadNextLevel();

  protected abstract void afterLoadNextLevel();

  protected abstract void onShotFinish(Shot currentShot);

  protected void saveKnowledge(Object object, String filePathName) {
    Util.saveXML(object, filePathName);
  }

  @SuppressWarnings("rawtypes")
  protected Object loadKnowledge(Class objectClass, String filePathName) {
    Object loaded = objectClass.cast(Util.loadXML(objectClass, filePathName));
    Object loadedInstance = null;
    try {
      loadedInstance = objectClass.newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return (loaded != null) ? loaded : loadedInstance;
  }

  protected Point getReleasePoint(Rectangle sling, Point _tpt) {
    Point releasePoint = null;
    // estimate the trajectory
    ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

    // do a high shot when entering a level to find an accurate velocity
    if (firstShot && pts.size() > 1) {
      releasePoint = pts.get(1);
    } else if (pts.size() == 1) {
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
        releasePoint = tp.findReleasePoint(sling, Math.PI / 4);
      }
    }
    return releasePoint;
  }

  protected int getTapTime(Rectangle sling, Point releasePoint, Point _tpt) {
    int tapInterval = 0;
    switch (aRobot.getBirdTypeOnSling()) {
    case RedBird:
      tapInterval = 0;
      break; // start of trajectory
    case YellowBird:
      tapInterval = 65 + randomGenerator.nextInt(25);
      break; // 65-90% of the way
    case WhiteBird:
      tapInterval = 70 + randomGenerator.nextInt(20);
      break; // 70-90% of the way
    case BlackBird:
      tapInterval = 70 + randomGenerator.nextInt(20);
      break; // 70-90% of the way
    case BlueBird:
      tapInterval = 65 + randomGenerator.nextInt(20);
      break; // 65-85% of the way
    default:
      tapInterval = 60;
    }
    return tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
  }

  protected GameState executeShot(Rectangle sling, Shot shot, GameState state,
      Point releasePoint) {
    // check whether the slingshot is changed. the change of the slingshot
    // indicates a change in the scale.
    ActionRobot.fullyZoomOut();
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle _sling = vision.findSlingshotMBR();
    if (_sling != null) {
      double scale_diff = Math.pow((sling.width - _sling.width), 2)
          + Math.pow((sling.height - _sling.height), 2);
      if (scale_diff < 25) {
        if (shot.getDx() < 0) {
          aRobot.cshoot(shot);
          this.onShotFinish(shot);
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
        System.out
            .println("Scale is changed, can not execute the shot, will re-segement the image");
      }
    } else {
      System.out
          .println("no sling detected, can not execute the shot, will re-segement the image");
    }
    return state;
  }

  protected Shot createShot(ABObject abObject, Rectangle sling, Point _tpt,
      Point releasePoint) {
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

    // Calculate the tapping time according the bird type
    if (releasePoint != null) {
      int tapTime = getTapTime(sling, releasePoint, _tpt);
      int dx = (int) releasePoint.getX() - refPoint.x;
      int dy = (int) releasePoint.getY() - refPoint.y;

      shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime, releasePoint);
    }

    return shot;
  }

  protected double distance(Point p1, Point p2) {
    return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y)));
  }

  protected void onLostState() {
    System.out.println("Restart");
    aRobot.restartLevel();
  }

  private void onEpisodeMenuState() {
    System.out.println("Unexpected episode menu page: " + currentLevel);
    ActionRobot.GoFromMainMenuToLevelSelection();
    this.aRobot.loadLevel(currentLevel);
  }

  private void onMainMenuState() {
    System.out.println("Unexpected main menu page: " + currentLevel);
    ActionRobot.GoFromMainMenuToLevelSelection();
    aRobot.loadLevel(currentLevel);
  }

  private void onLevelSelectionState() {
    System.out.println("Unexpected level selection page: " + currentLevel);
    aRobot.loadLevel(currentLevel);
  }

  private void onWonState() {
    this.beforeLoadNextLevel();

    try {
      Thread.sleep(this.sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    aRobot.loadLevel(++currentLevel);
    tp = new TrajectoryPlanner();
    firstShot = true;

    this.afterLoadNextLevel();
  }
}
