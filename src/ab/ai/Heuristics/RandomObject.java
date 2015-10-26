package ab.ai.Heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.vision.ABObject;
import ab.vision.Vision;

public class RandomObject extends Heuristic {
  private Random randomGenerator;

  public RandomObject() {
    super();
    this.randomGenerator = new Random();
  }

  @Override
  public ABObject solve(Vision vision) {
    List<ABObject> listObjects = new ArrayList<ABObject>();
    listObjects.addAll(vision.findBlocksRealShape());
    listObjects.addAll(vision.findPigsRealShape());

    return listObjects.get(this.randomGenerator.nextInt(listObjects.size()));
  }

}
