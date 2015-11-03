package ab.ai;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.Vision;

public class SceneState {
  private BufferedImage  screenshot;
  private List<ABObject> birds;
  private List<ABObject> pigs;
  private List<ABObject> blocks;
  private List<ABObject> tnts;
  private Rectangle      sling;

  public SceneState(BufferedImage screenshot) {
    this.screenshot = screenshot;
    Vision vision = new Vision(this.screenshot);
    this.birds = vision.findBirdsMBR();
    this.pigs = vision.findPigsMBR();
    this.blocks = vision.findBlocksMBR();
    this.tnts = vision.findTNTs();
    this.sling = vision.findSlingshotMBR();
  }

  public List<ABObject> getBirds() {
    return birds;
  }

  public List<ABObject> getPigs() {
    return pigs;
  }

  public List<ABObject> getBlocks() {
    return blocks;
  }

  public List<ABObject> getTnts() {
    return tnts;
  }

  public Rectangle getSling() {
    return sling;
  }
}
