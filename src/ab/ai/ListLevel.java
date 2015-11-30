package ab.ai;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "levels")
public class ListLevel {
  @XmlElement(name = "level")
  private List<Level> list = new ArrayList<Level>();

  public ListLevel() {
    super();
  }

  public ListLevel(List<Level> listLevel) {
    super();
    this.list = listLevel;
  }

  public void setListLevel(List<Level> listLevel) {
    this.list = listLevel;
  }

  public void addLevel(Level level) {
    this.list.add(level);
  }

  public int size() {
    return this.list.size();
  }

  public void add(Level level) {
    this.list.add(level);
  }

  public void set(int index, Level level) {
    this.list.set(index, level);
  }

  public Level get(int index) {
    return this.list.get(index);
  }
}
