package ab.ai.Heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeuristicHandler {
  private int             level;
  private List<Heuristic> heuristics;
  private Random          randomGenerator;
  private int             maxFrequency;

  public HeuristicHandler() {
    this.heuristics = new ArrayList<Heuristic>();
    this.randomGenerator = new Random(123456789L);
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public List<Heuristic> getHeuristics() {
    return heuristics;
  }

  public void setHeuristics(List<Heuristic> heuristics) {
    this.heuristics = heuristics;
  }

  public int getMaxFrequency() {
    return maxFrequency;
  }

  public void setMaxFrequency(int maxFrequency) {
    this.maxFrequency = maxFrequency;
  }

  public void add(Heuristic heuristic) {
    List<Heuristic> tempList = new ArrayList<Heuristic>();

    if (this.heuristics.isEmpty()) {
      this.heuristics.add(heuristic);
      this.maxFrequency += heuristic.getFrequency();
    } else {
      for (Heuristic h : this.heuristics) {
        if (!heuristic.getClass().equals(h.getClass())) {
          tempList.add(heuristic);
          this.maxFrequency += heuristic.getFrequency();
        }
      }
      this.heuristics.addAll(tempList);
    }

  }

  public void remove(Heuristic heuristic) {

  }

  public Heuristic randomPick() {
    int selectedIndex = this.randomGenerator.nextInt(this.maxFrequency);
    return this.selectByIndex(selectedIndex);
  }

  public Heuristic selectByIndex(int selectedIndex) {
    int index = 1;
    int totalDiff = 0;
    int currentHeuristicLimit = 0;
    
    for (Heuristic h : this.heuristics) {
      currentHeuristicLimit = index * h.getFrequency();
      totalDiff += Heuristic.HEURISTIC_VALUE - h.getFrequency();
      if ((currentHeuristicLimit - totalDiff) > selectedIndex) {
        return h;
      }

      index++;
    }

    return null;
  }

  private void updateMaxFrequency(int decreaseValue) {
    this.maxFrequency -= decreaseValue;
  }
}
