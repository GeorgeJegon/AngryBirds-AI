package ab.ai;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ab.demo.other.Shot;

@XmlRootElement
public class BestShot implements Serializable {
	private static final long serialVersionUID = 5003839097166308999L;

	@XmlAttribute
	private int level;
	@XmlAttribute
	private int score;
	@XmlElement
	private List<Shot> shots;

	public BestShot() {

	}

	public BestShot(final int level, final int score, final List<Shot> shots) {
		this.level = level;
		this.score = score;
		this.shots = shots;
	}

	public Shot get(int index) {
		return this.shots.get(index);
	}

	public List<Shot> getShots() {
		return this.shots;
	}

	public int getScore() {
		return this.score;
	}

	@Override
	public String toString() {
		return "BestShot [level=" + this.level + ", score=" + this.score
				+ ", shots=" + this.shots + "]";
	}
}