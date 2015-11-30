package ab.ai.agents;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import weka.core.Instances;
import ab.ai.Match;
import ab.ai.Util;
import ab.ai.Heuristics.BaseSupportersHeuristic;
import ab.ai.Heuristics.BuildingHeuristic;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class FMUAgent extends Agent implements Runnable {
  private int   reloadLevelTimes   = -1;
  private int   reloadedLevelTimes = 0;
  private Match loadedMatch        = null;

  public FMUAgent() {
    super();
    ActionRobot.GoFromMainMenuToLevelSelection();
  }

  public static void main(String[] args) {
    FMUAgent agent = new FMUAgent();
    agent.currentLevel = 1;
    agent.trainning = true;
    agent.run();
  }

  protected Integer getNextLevel() {
    if (this.reloadedLevelTimes >= this.reloadLevelTimes) {
      this.reloadedLevelTimes = 0;
      return super.getNextLevel();
    } else {
      this.reloadedLevelTimes++;
      return this.currentLevel;
    }
  }

  protected void onStartLevel() {
    super.onStartLevel();

    if (!this.trainning) {
      this.loadedMatch = this.listBestShotsMatches
          .getByLevel(this.currentLevel);
    }

    this.currentHeuristicHandler.setLevel(this.currentLevel);
    this.currentHeuristicHandler.add(new BaseSupportersHeuristic());
    this.currentHeuristicHandler.add(new BuildingHeuristic());
    this.currentHeuristic = this.currentHeuristicHandler.randomPick();
  }

  @Override
  protected GameState solve() {
    this.aRobot.click();
    ActionRobot.fullyZoomOut();
    GameState state = this.aRobot.getState();
    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle sling = vision.findSlingshotMBR();
    ABType birdOnSling = vision.getBirdTypeOnSling();

    if (this.waitForSling(vision, sling, screenshot, birdOnSling)) {
      if (this.trainning || this.loadedMatch == null) {
        System.out.println("Tentando resolver o level " + this.currentLevel
            + " usando o " + (this.listShots.size() + 1)
            + " tiro e a heuristica (" + this.currentHeuristic.getName() + ")");

        state = this.currentHeuristic.solve(this, state, vision, sling,
            screenshot, birdOnSling);
      } else {
        Shot shot = null;
        List<Shot> knownShots = this.loadedMatch.getShots();

        if (this.listShots.size() < knownShots.size()) {
          System.out.println("Ja sei resolver");
          shot = knownShots.get(this.listShots.size());
          state = this.executeShot(sling, shot, state, shot.getReleasePoint());
        }
      }
    }

    return state;
  }

  @Override
  protected boolean beforeRestartLevel() {
    this.saveData(GameState.LOST);

    if (this.failedAttempts > 5) {
      this.resetLevelInformation();
      this.aRobot.loadLevel(this.getNextLevel());
      return false;
    }
    return true;
  }

  @Override
  protected void afterRestartLevel() {
    this.reloadedLevelTimes++;
  }

  @Override
  protected boolean beforeLoadNextLevel() {
    this.saveData(GameState.WON);
    return true;
  }

  @Override
  protected void afterLoadNextLevel() {

  }

  @Override
  protected void onShotFinish(Shot currentShot) {
    int current_score = this.aRobot.current_score;
    int shot_score = this.aRobot.getScore();

    if (shot_score > 0) {
      shot_score -= current_score;
    }

    currentShot.setScore(shot_score);
    this.listShots.add(currentShot);

    this.aRobot.current_score += shot_score;
  }

  private void saveData(GameState state) {
    Match match = this.createMatch(state);
    this.saveMatch(match);
    this.saveHeuristicHandler();
    this.saveMatchARFF(this.getLevelInfoPath() + "shots.arff", match);
    this.saveMatchARFF(this.getDataPath() + "shots.arff", match);
    if (GameState.WON == state) {
      this.checkBestShots(match);
    }
  }

  private void checkBestShots(Match match) {
    String filePathName = this.getDataPath() + "best-shots.xml";
    Match bestShotMatch = null;
    int currentLevelIndex = this.listBestShotsMatches
        .getIndexByLevel(this.currentLevel);

    if (currentLevelIndex > -1) {
      bestShotMatch = this.listBestShotsMatches.get(currentLevelIndex);
      if ((match.getScore() > bestShotMatch.getScore())
          || (match.getScore() == bestShotMatch.getScore() && match.getShots()
              .size() < bestShotMatch.getShots().size())) {
        this.listBestShotsMatches.set(currentLevelIndex, match);
      }
    } else {
      this.listBestShotsMatches.add(match);
    }
    this.saveKnowledge(this.listBestShotsMatches, filePathName);
  }

  private void saveMatchARFF(String fileNamePath, Match match) {
    Instances dataSet = Util.createMatchInstanceArff(match);
    Instances dataSetMerged = Util.mergeInstancesArff(dataSet, fileNamePath);
    Util.saveArff(dataSetMerged, fileNamePath);
  }
}
