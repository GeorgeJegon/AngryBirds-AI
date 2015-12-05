package ab.ai;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ab.utils.ABUtil;
import ab.vision.ABObject;
import ab.vision.ABType;

public class Building {
  private List<ABObject> components = new ArrayList<ABObject>();
  private Rectangle      bounding;
  private int            height;
  private int            x;
  private int            y;

  public Building(List<ABObject> components) {
    this.components = components;
    Point left = this.findLeftCorner();
    this.x = left.x;
    this.y = left.y;
    this.height = this.findHeight();
    this.bounding = this.getBoundingRect();
  }

  public List<ABObject> getComponents() {
    return this.components;
  }

  public Rectangle getBounding() {
    return bounding;
  }

  public List<ABObject> filterComponets(ABType type) {
    return ABUtil.filterObjects(this.components, type);
  }

  public Boolean hasPig(ABObject pig) {
    return this.bounding.intersects(pig.getBounds());
  }

  /**
   * Finds the top left corner of the building which is later stored in x and y
   */
  private Point findLeftCorner() {
    Point temp = new Point(1000, 1000);

    for (ABObject component : this.components) {
      if (component.y < temp.y) {
        temp.y = component.y;
        temp.x = component.x;
      }
    }
    return temp;
  }

  /**
   * Returns the height of the building.
   */
  private int findHeight() {
    int max = 0;

    for (ABObject component : this.components) {
      if (component.y + component.height > max) {
        max = component.y + component.height;
      }
    }
    return max - this.y;
  }

  /**
   * @return creates the bounding rectangle of the building
   */
  private Rectangle getBoundingRect() {
    int mostleft = 1000;
    int mostright = 0;

    for (ABObject component : this.components) {
      if (component.x < mostleft)
        mostleft = component.x;
      if ((component.x + component.width) > mostright)
        mostright = component.x + component.width;
    }
    return new Rectangle(mostleft, this.y, mostright - mostleft, this.height);
  }
}
