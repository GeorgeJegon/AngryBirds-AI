package ab.ai;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ListMatches {
  private List<Match> _matches = new ArrayList<Match>();

  public ListMatches() {
    super();
  }

  @XmlElement
  public List<Match> getMatches() {
    return _matches;
  }

  public void setMatches(List<Match> matches) {
    this._matches = matches;
  }

  public void add(Match match) {
    this._matches.add(match);
  }

  public void set(int index, Match match) {
    this._matches.set(index, match);
  }

  public int size() {
    return this._matches.size();
  }

  public Match get(int index) {
    return this._matches.get(index);
  }

  public int getIndexByLevel(int level) {
    int index = -1;
    for (int i = 0; i < this._matches.size(); i++) {
      Match match = this._matches.get(i);
      if (match.getLevel() == level) {
        return i;
      }
    }

    return index;
  }

  public Match getByLevel(int level) {
    int index = this.getIndexByLevel(level);
    if (index > -1) {
      return this.get(index);
    }
    return null;
  }

}
