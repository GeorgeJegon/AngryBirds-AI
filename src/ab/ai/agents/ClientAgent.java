package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.ai.Heuristics.Heuristic;
import ab.ai.Heuristics.HeuristicHandler;
import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public abstract class ClientAgent {
  private final String            DATA_PATH     = "src/ab/data/";
  private int                     id            = 28888;

  protected ClientActionRobotJava aRobot;
  protected Random                randomGenerator;
  protected TrajectoryPlanner     tp;
  protected boolean               firstShot;
  protected Point                 prevTarget;
  protected List<Shot>            listShots;
  protected Heuristic             currentHeuristic;
  protected HeuristicHandler      currentHeuristicHandler;

  public int                      sleepTime     = 3000;
  public byte                     currentLevel  = -1;
  public int                      failedCounter = 0;
  public int[]                    solved;

  public ClientAgent() {
    this("127.0.0.1");
  }

  public ClientAgent(String ip) {
    this(ip, 28888);
  }

  public ClientAgent(int id) {
    this("127.0.0.1", id);
  }

  public ClientAgent(String ip, int id) {
    this.aRobot = new ClientActionRobotJava(ip);
    this.tp = new TrajectoryPlanner();
    this.randomGenerator = new Random();
    this.prevTarget = null;
    this.firstShot = true;
    this.id = id;
    this.listShots = new ArrayList<Shot>();
    this.currentHeuristicHandler = new HeuristicHandler();
  }

  private int getNextLevel() {
    int level = 0;
    boolean unsolved = false;
    // all the level have been solved, then get the first unsolved level
    for (int i = 0; i < solved.length; i++) {
      if (solved[i] == 0) {
        unsolved = true;
        level = i + 1;
        if (level <= currentLevel && currentLevel < solved.length)
          continue;
        else
          return level;
      }
    }
    if (unsolved)
      return level;
    level = (currentLevel + 1) % solved.length;
    if (level == 0)
      level = solved.length;
    return level;
  }

  private void checkMyScore() {

    int[] scores = aRobot.checkMyScore();
    System.out.println(" My score: ");
    int level = 1;
    for (int i : scores) {
      System.out.println(" level " + level + "  " + i);
      if (i > 0)
        solved[level - 1] = 1;
      level++;
    }
  }

  public void run() {
    GameState state = GameState.PLAYING;
    byte[] info = aRobot.configure(ClientActionRobot.intToByteArray(id));
    this.solved = new int[info[2]];
    checkMyScore();

    this.currentLevel = (byte) getNextLevel();
    this.aRobot.loadLevel(currentLevel);

    while (true) {

      state = solve();

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

  protected abstract boolean beforeRestartLevel();

  protected abstract void afterRestartLevel();

  protected abstract boolean beforeLoadNextLevel();

  protected abstract void afterLoadNextLevel();

  protected abstract void onShotFinish(Shot currentShot);

  protected double distance(Point p1, Point p2) {
    return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y)));
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
    switch (this.aRobot.getBirdTypeOnSling()) {
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
    return this.tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
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

  protected GameState executeShot(Rectangle sling, Shot shot, GameState state,
      Point releasePoint) {
    // check whether the slingshot is changed. the change of the slingshot
    // indicates a change in the scale.
    this.aRobot.fullyZoomOut();
    BufferedImage screenshot = this.aRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle _sling = vision.findSlingshotMBR();
    if (_sling != null) {
      double scale_diff = Math.pow((sling.width - _sling.width), 2)
          + Math.pow((sling.height - _sling.height), 2);
      if (scale_diff < 25) {
        if (shot.getDx() < 0) {
          long timer = System.currentTimeMillis();
          this.aRobot.shoot(shot.getX(), shot.getY(), shot.getDx(),
              shot.getDy(), 0, shot.getT_tap(), false);
          System.out.println("It takes " + (System.currentTimeMillis() - timer)
              + " ms to take a shot");
          // this.onShotFinish(shot);
          state = this.aRobot.checkState();

          if (state == GameState.PLAYING) {
            screenshot = this.aRobot.doScreenShot();
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

  protected Boolean waitForSling(Vision vision, Rectangle sling,
      BufferedImage screenshot) {
    while (sling == null && this.aRobot.checkState() == GameState.PLAYING) {
      System.out
          .println("No slingshot detected. Please remove pop up or zoom out");
      this.aRobot.fullyZoomOut();
      screenshot = this.aRobot.doScreenShot();
      vision = new Vision(screenshot);
      sling = vision.findSlingshotMBR();
    }

    return sling != null;
  }

  private void resetLevelInformation() {
    // this.saveHeuristicHandler();

    this.firstShot = true;
    this.listShots.clear();
    this.prevTarget = null;
    this.tp = new TrajectoryPlanner();
    this.currentHeuristicHandler = new HeuristicHandler();
    this.currentHeuristic = null;
  }

  protected void onLostState() {
    if (++this.failedCounter > 3) {

      this.failedCounter = 0;
      currentLevel = (byte) getNextLevel();
      aRobot.loadLevel(currentLevel);

    } else {

      this.beforeRestartLevel();

      System.out.println("restart");

      this.resetLevelInformation();
      aRobot.restartLevel();
      this.afterRestartLevel();

    }
  }

  private void onLevelSelectionState() {
    System.out
        .println("unexpected level selection page, go to the last current level : "
            + currentLevel);
    this.aRobot.loadLevel(currentLevel);
  }

  private void onMainMenuState() {
    System.out.println("unexpected main menu page, reload the level : "
        + currentLevel);
    this.aRobot.loadLevel(currentLevel);
  }

  private void onEpisodeMenuState() {
    System.out.println("unexpected episode menu page, reload the level: "
        + currentLevel);
    this.aRobot.loadLevel(currentLevel);
  }

  private void onWonState() {
    this.beforeLoadNextLevel();

    this.checkMyScore();

    this.currentLevel = (byte) getNextLevel();
    this.aRobot.loadLevel(currentLevel);

    this.resetLevelInformation();
    this.afterLoadNextLevel();

    int[] scores = this.aRobot.checkScore();

    System.out.println("Global best score: ");
    for (int i = 0; i < scores.length; i++) {
      System.out.print(" level " + (i + 1) + ": " + scores[i]);
    }
    System.out.println();

  }

}
