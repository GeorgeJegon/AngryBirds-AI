package ab.ai;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class ListBestShots {
	private List<BestShot> _bestShots = new ArrayList<BestShot>();
	
	@XmlElement
	public List<BestShot> get_bestShots() {
		return _bestShots;
	}

	public void set_bestShots(List<BestShot> _bestShots) {
		this._bestShots = _bestShots;
	}

	public void add(BestShot bs) {
		this._bestShots.add(bs);
	}

	public void set(int index, BestShot bs) {
		this._bestShots.set(index, bs);
	}

	public int size() {
		return this._bestShots.size();
	}

	public BestShot get(int index) {
		return this._bestShots.get(index);
	}

}
