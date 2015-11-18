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
  private Vision         vision;

  public SceneState(BufferedImage screenshot) {
    this.screenshot = screenshot;
    this.vision = new Vision(this.screenshot);
    this.birds = vision.findBirdsRealShape();
    this.pigs = vision.findPigsRealShape();
    this.blocks = vision.findBlocksRealShape();
    this.tnts = vision.findTNTs();
    this.sling = vision.findSlingshotMBR();
  }

  public Vision getVision() {
    return this.vision;
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
