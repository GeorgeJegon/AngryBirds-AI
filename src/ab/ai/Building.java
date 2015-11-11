package ab.ai;

import java.util.ArrayList;
import java.util.List;

import ab.vision.ABObject;

public class Building {
  private List<ABObject> components = new ArrayList<ABObject>();

  public Building(List<ABObject> components) {
    this.components = components;
  }

  public List<ABObject> getComponents() {
    return this.components;
  }
}
