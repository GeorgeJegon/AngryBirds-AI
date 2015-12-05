package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.ai.ListLevel;
import ab.ai.ListMatches;
import ab.ai.Match;
import ab.ai.Util;
import ab.ai.Heuristics.Heuristic;
import ab.ai.Heuristics.HeuristicHandler;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public abstract class Agent {
  private ImageSegFrame       frame;
  private final String        DATA_PATH    = "src/ab/data/";

  protected ActionRobot       aRobot;
  protected Random            randomGenerator;
  protected TrajectoryPlanner tp;
  protected boolean           firstShot;
  protected int               failedAttempts;
  protected Point             prevTarget;
  public List<Shot>           listShots;
  protected Heuristic         currentHeuristic;
  protected HeuristicHandler  currentHeuristicHandler;
  protected List<Integer>     levelsCleared;
  protected List<Integer>     levelsListController;
  protected ListLevel         levelInfo;
  protected ListMatches       listBestShotsMatches;

  public static int           time_limit   = 12;
  public int                  currentLevel = 1;
  public int                  totalLevels  = 21;
  public int                  sleepTime    = 3000;
  public boolean              trainning;

  public Agent() {
    this.aRobot = new ActionRobot();
    this.tp = new TrajectoryPlanner();
    this.prevTarget = null;
    this.frame = null;
    this.firstShot = true;
    this.listShots = new ArrayList<Shot>();
    this.levelsCleared = new ArrayList<Integer>();
    this.levelsListController = new ArrayList<Integer>();
    this.levelInfo = (ListLevel) this.loadKnowledge(ListLevel.class, DATA_PATH
        + "/level_info.xml");
    this.listBestShotsMatches = (ListMatches) this.loadKnowledge(
        ListMatches.class, this.getDataPath() + "/best-shots.xml");
    this.failedAttempts = 0;
    this.randomGenerator = new Random();
    this.currentHeuristicHandler = new HeuristicHandler();
    this.trainning = false;
    this.initializeLevelsListController(this.totalLevels);
  }

  public void run() {
    aRobot.loadLevel(currentLevel);

    while (true) {
      System.out.println("Execute level " + currentLevel);

      if (this.listShots.isEmpty()) {
        this.onStartLevel();
      }

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

  protected abstract boolean beforeRestartLevel();

  protected abstract void afterRestartLevel();

  protected abstract boolean beforeLoadNextLevel();

  protected abstract void afterLoadNextLevel();

  protected abstract void onShotFinish(Shot currentShot);

  public void showTrajectory(BufferedImage screenshot, Rectangle sling,
      Point releasePoint, String fileNamePath) {
    this.tp.plotTrajectory(screenshot, sling, releasePoint);
    Util.saveImage(screenshot, fileNamePath);
  }

  public void showTrajectory(BufferedImage screenshot, Rectangle sling,
      Point releasePoint) {
    this.tp.plotTrajectory(screenshot, sling, releasePoint);

    if (this.frame == null) {
      this.frame = new ImageSegFrame("trajectory", screenshot);
    } else {
      this.frame.refresh(screenshot);
    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void showTrajectory(Rectangle sling, Point releasePoint) {
    BufferedImage screenshot = ActionRobot.doScreenShot();
    this.showTrajectory(screenshot, sling, releasePoint);
  }

  protected Boolean waitForSling(Vision vision, Rectangle sling,
      BufferedImage screenshot, ABType birdOnSling) {
    while (sling == null && birdOnSling == ABType.Unknown
        && this.aRobot.getState() == GameState.PLAYING) {
      System.out
          .println("No slingshot detected. Please remove pop up or zoom out");
      ActionRobot.fullyZoomOut();
      screenshot = ActionRobot.doScreenShot();
      vision = new Vision(screenshot);
      sling = vision.findSlingshotMBR();
      birdOnSling = vision.getBirdTypeOnSling();
    }

    return sling != null;
  }

  protected void saveKnowledge(Object object, String filePathName) {
    Util.saveXML(object, filePathName);
  }

  protected void onStartLevel() {
    this.failedAttempts = 0;
  }

  @SuppressWarnings("rawtypes")
  protected Object loadKnowledge(Class objectClass, String filePathName) {
    Object loaded = objectClass.cast(Util.loadXML(objectClass, filePathName));
    Object loadedInstance = null;
    try {
      loadedInstance = objectClass.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return (loaded != null) ? loaded : loadedInstance;
  }

  public List<Point> getReleasePoints(Rectangle sling, Point _tpt) {
    return tp.estimateLaunchPoint(sling, _tpt);
  }

  public Point getDefaultReleasePoint(Rectangle sling) {
    return tp.findReleasePoint(sling, Math.PI / 4);
  }

  public Point getReleasePoint(Rectangle sling, Point _tpt) {
    Point releasePoint = null;
    // estimate the trajectory
    List<Point> pts = this.getReleasePoints(sling, _tpt);

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
        releasePoint = this.getDefaultReleasePoint(sling);
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

  public GameState executeShot(Rectangle sling, Shot shot, GameState state,
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

  public Shot createShotObject(ABObject abObject, Rectangle sling, Point _tpt,
      Point releasePoint) {
    Shot shot = null;
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

  public Shot createShot(ABObject abObject, Rectangle sling, Point _tpt,
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

    return this.createShotObject(abObject, sling, _tpt, releasePoint);
  }

  protected double distance(Point p1, Point p2) {
    return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y)));
  }

  protected String getDataPath() {
    return DATA_PATH + this.getClass().getSimpleName() + "/";
  }

  protected String getLevelInfoPath() {
    return this.getDataPath() + "level-" + this.currentLevel + "/";
  }

  public String getScreenshotPath() {
    return DATA_PATH + "/screenshots/";
  }

  protected void saveHeuristicHandler() {
    if (!this.currentHeuristicHandler.getHeuristics().isEmpty()) {
      Util.saveXML(this.currentHeuristicHandler, this.getLevelInfoPath()
          + "heuristics.xml");
    }
  }

  protected Match createMatch(GameState state) {
    Match match = new Match();
    int score = this.aRobot.current_score;

    if (state == GameState.WON) {
      score = this.aRobot.getScore();
    } else {
      this.currentHeuristic.bad();
    }

    match.setLevel(this.currentLevel);
    match.setScore(score);
    match.setHeuristic(this.currentHeuristic.getName());
    match.setShots(this.listShots);
    match.setType(state.toString());

    return match;
  }

  protected void saveMatch(Match match) {
    Util.saveXML(match, this.getLevelInfoPath() + "matches.xml", true);
    Util.saveXML(match, this.getDataPath() + "matches.xml", true);
  }

  protected Integer getNextLevel() {
    this.currentLevel = ++this.currentLevel % (this.totalLevels + 1);
    if (this.currentLevel <= 0) {
      this.currentLevel = 1;
    }
    return this.currentLevel;
  }

  protected void resetLevelInformation() {
    this.firstShot = true;
    this.listShots = new ArrayList<Shot>();
    this.aRobot.current_score = 0;
    this.prevTarget = null;
    this.currentHeuristicHandler = new HeuristicHandler();
    this.currentHeuristic = null;
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
    boolean continueProccess = this.beforeLoadNextLevel();

    if (continueProccess) {
      try {
        Thread.sleep(this.sleepTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      aRobot.loadLevel(this.getNextLevel());
      tp = new TrajectoryPlanner();

      this.resetLevelInformation();
      this.afterLoadNextLevel();
    }
  }

  private void onLostState() {
    this.failedAttempts++;
    boolean continueProccess = this.beforeRestartLevel();

    if (continueProccess) {
      System.out.println("Restart");
      this.resetLevelInformation();

      aRobot.restartLevel();
      this.afterRestartLevel();
    }
  }

  private void initializeLevelsListController(int total) {
    for (int i = 1; i <= total; i++) {
      this.levelsListController.add(i);
    }
  }
}
