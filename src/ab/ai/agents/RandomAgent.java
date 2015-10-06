package ab.ai.agents;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ab.ai.BestShot;
import ab.ai.ListBestShots;
import ab.ai.Util;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class RandomAgent extends Agent implements Runnable {
	private int shotNumber = 0;
	private List<Shot> listObjects = new ArrayList<Shot>();
	private ListBestShots bestShots = new ListBestShots();
	private static final String BEST_SHOTS_FILE = "bestshots.xml";

	public RandomAgent() {
		super();
		bestShots = loadBestShots();
		ActionRobot.GoFromMainMenuToLevelSelection();
	}

	@Override
	protected void beforeRestartLevel() {

	}

	@Override
	protected void afterRestartLevel() {

	}

	@Override
	protected void beforeLoadNextLevel() {
		int score = StateUtil.getScore(ActionRobot.proxy);
		BestShot bs = new BestShot(currentLevel, score, listObjects);

		// If is a new level, add to bestShots list.
		if (bestShots.size() == currentLevel - 1) {
			bestShots.add(bs);
			saveBestShots();
		} else { // Update with the new score.
			if (score > bestShots.get(currentLevel - 1).getScore()) {
				bestShots.set(currentLevel - 1, bs);
				saveBestShots();
			}
		}
	}

	@Override
	protected void afterLoadNextLevel() {
		this.listObjects = new ArrayList<Shot>();
		this.shotNumber = 0;
	}

	public ListBestShots loadKnowledge() {
		ListBestShots loaded = (ListBestShots) Util.loadXML(
				ListBestShots.class, BEST_SHOTS_FILE);
		return (loaded != null) ? loaded : new ListBestShots();
	}

	public boolean hasBestShot() {
		BestShot bs = null;
		if (this.bestShots.size() > this.currentLevel - 1) {
			bs = bestShots.get(currentLevel - 1);
			return bs.getShots().size() > shotNumber;
		}
		return false;
	}

	public GameState solve() {
		boolean hasBestShot = this.hasBestShot();

		ABObject abObject = null;
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		Rectangle sling = vision.findSlingshotMBR();
		List<ABObject> objects = new ArrayList<ABObject>();
		List<Shot> shots = new ArrayList<Shot>();

		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
					.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}

		if (hasBestShot) {
			shots = bestShots.get(currentLevel - 1).getShots();
		} else {
			objects = makeActionChoices(vision);
		}

		GameState state = aRobot.getState();

		// If there is a sling, then play, otherwise just skip.
		if (sling != null) {
			Shot shot = null;

			if (hasBestShot) {
				System.out.println("Ja sei um tiro");
				shot = shots.get(shotNumber);
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

	public List<ABObject> makeActionChoices(final Vision vision) {
		List<ABObject> objects = new ArrayList<ABObject>();

		objects.addAll(vision.findPigsRealShape());
		objects.addAll(vision.findBlocksRealShape());
		objects.addAll(vision.findHills());
		objects.addAll(vision.findTNTs());

		return objects.subList(0, 1);
	}

	public static void main(String args[]) {
		RandomAgent na = new RandomAgent();
		if (args.length > 0) {
			na.currentLevel = Integer.parseInt(args[0]);
		}
		na.run();
	}

	private void saveBestShots() {
		this.saveKnowledge(bestShots, BEST_SHOTS_FILE);
	}

	private ListBestShots loadBestShots() {
		return (ListBestShots) this.loadKnowledge(ListBestShots.class,
				BEST_SHOTS_FILE);
	}

	@Override
	protected void onShotFinish(Shot currentShot) {
		int current_score = this.aRobot.current_score;
		int shot_score = this.aRobot.getScore() - current_score;
		
		currentShot.setScore(shot_score);
		this.listObjects.add(currentShot);
		
		this.aRobot.current_score += shot_score; 
	}
}