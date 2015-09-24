/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 **To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo.other;

import java.awt.Point;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Shot implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2279112400720899118L;

	private int x;
	private int y;
	private int dx;
	private int dy;
	private int t_shot;
	private int t_tap;

	private Point releasePoint;

	@XmlAttribute
	public int getDx() {
		return dx;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	@XmlAttribute
	public int getDy() {
		return dy;
	}

	public Shot(int x, int y, int dx, int dy, int t_shot, int t_tap,
			Point releasePoint) {
		super();
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.t_shot = t_shot;
		this.t_tap = t_tap;
		this.releasePoint = releasePoint;
	}

	public Shot(int x, int y, int dx, int dy, int t_shot, int t_tap) {
		super();
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.t_shot = t_shot;
		this.t_tap = t_tap;
	}

	public Shot(int x, int y, int dx, int dy, int t_shot) {
		super();
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.t_shot = t_shot;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public Shot() {
		x = 0;
		y = 0;
		dx = 0;
		dy = 0;
		t_shot = 0;
		t_tap = 0;
	}

	public Shot(int x, int y, int t_shot, int t_tap) {
		super();
		this.x = x;
		this.y = y;
		this.t_shot = t_shot;
		this.t_tap = t_tap;
	}

	@XmlAttribute
	public int getX() {
		return x;
	}

	public Point getReleasePoint() {
		return this.releasePoint;
	}
	
	@XmlAttribute
	public int getReleasePointX() {
		return this.releasePoint.x;
	}
	
	@XmlAttribute
	public int getReleasePointY() {
		return this.releasePoint.y;
	}

	public void setX(int x) {
		this.x = x;
	}

	@XmlAttribute
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@XmlAttribute
	public int getT_shot() {
		return t_shot;
	}

	public void setT_shot(int t_shot) {
		this.t_shot = t_shot;
	}

	@XmlAttribute
	public int getT_tap() {
		return t_tap;
	}

	public void setT_tap(int t_tap) {
		this.t_tap = t_tap;
	}

	public String toString() {
		String result = "";
		if (x == 0 && y == 0) {
			if (t_tap != 0)
				result += "tap at:  " + t_tap;
		} else
			result += "Shoot from: (" + (x + dx) + "  " + (y + dy) + " )"
					+ " at time  " + t_shot;

		return result;

	}

}
