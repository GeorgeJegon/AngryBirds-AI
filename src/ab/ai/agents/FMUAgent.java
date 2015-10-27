package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ab.ai.Match;
import ab.ai.Util;
import ab.ai.Heuristics.Heuristic;
import ab.ai.Heuristics.RandomObject;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class FMUAgent extends Agent implements Runnable {
  private static final String MATCHES_FILE = "src/ab/data/FMUAgent/matches.xml";

  public FMUAgent() {
    super();
    ActionRobot.GoFromMainMenuToLevelSelection();
  }

  public static void main(String[] args) {
    FMUAgent agent = new FMUAgent();
    agent.run();
  }

  @Override
  protected GameState solve() {
    this.aRobot.click();
    ActionRobot.fullyZoomOut();
    GameState state = this.aRobot.getState();
    Heuristic heuristic = new RandomObject();

    BufferedImage screenshot = ActionRobot.doScreenShot();
    Vision vision = new Vision(screenshot);
    Rectangle sling = vision.findSlingshotMBR();
    Shot shot = null;
    ABObject target = null;
    Point objectCenter = null;
    Point releasePoint = null;

    if (this.waitForSling(vision, sling, screenshot)) {
      state = this.aRobot.getState();


      target = heuristic.solve(vision);
      objectCenter = target.getCenter();
      releasePoint = getReleasePoint(sling, objectCenter);

      shot = createShot(target, sling, objectCenter, releasePoint);

      if (shot == null) {
        System.err.println("No Release Point Found");
        return state;
      }

      state = executeShot(sling, shot, state, releasePoint);
    }

    return state;
  }

  protected void saveMatch(GameState state) {
    Match match = new Match();
    int score = this.aRobot.current_score;
    
    if (state == GameState.WON) {
      score = this.aRobot.getScore();
    }

    match.setLevel(this.currentLevel);
    match.setScore(score);
    match.setShots(this.listShots);
    match.setType(state.toString());
    
    Util.saveXML(match, MATCHES_FILE, true);
  }

  @Override
  protected void beforeRestartLevel() {
    // TODO Auto-generated method stub
    this.saveMatch(GameState.LOST);
  }

  @Override
  protected void afterRestartLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void beforeLoadNextLevel() {
    // TODO Auto-generated method stub
    this.saveMatch(GameState.WON);
  }

  @Override
  protected void afterLoadNextLevel() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onShotFinish(Shot currentShot) {
    int current_score = this.aRobot.current_score;
    int shot_score = this.aRobot.getScore() - current_score;

    currentShot.setScore(shot_score);
    this.listShots.add(currentShot);

    this.aRobot.current_score += shot_score;
  }
}
