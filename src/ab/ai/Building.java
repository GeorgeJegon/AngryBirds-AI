package ab.ai;

import java.util.ArrayList;
import java.util.List;

import ab.utils.ABUtil;
import ab.vision.ABObject;
import ab.vision.ABType;

public class Building {
  private List<ABObject> components = new ArrayList<ABObject>();

  public Building(List<ABObject> components) {
    this.components = components;
  }

  public List<ABObject> getComponents() {
    return this.components;
  }

  public List<ABObject> filterComponets(ABType type) {
    return ABUtil.filterObjects(this.components, type);
  }
}
