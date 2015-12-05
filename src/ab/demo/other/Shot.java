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
import java.awt.Rectangle;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import ab.planner.TrajectoryPlanner;

@XmlRootElement
@XmlType(propOrder = { "score", "x", "y", "dx", "dy", "releasePointX",
    "releasePointY", "thetaDegrees", "velocity", "targetPointType", "targetType", "birdOnSling" })
public class Shot implements Serializable {

  /**
	 * 
	 */
  private TrajectoryPlanner trajectoryPlanner = new TrajectoryPlanner();
  private static final long serialVersionUID  = 2279112400720899118L;

  private int               x;
  private int               y;
  private int               dx;
  private int               dy;
  private int               t_shot;
  private int               t_tap;
  private int               releasePointX;
  private int               releasePointY;
  private int               score;
  private double            theta;
  private double            velocity;
  private String            targetType;
  private String            birdOnSling;
  private String            targetPointType;

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

  public Shot(int x, int y, int dx, int dy, int t_shot) {
    super();
    this.x = x;
    this.y = y;
    this.dx = dx;
    this.dy = dy;
    this.t_shot = t_shot;
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

  public Shot(int x, int y, int dx, int dy, int t_shot, int t_tap,
      Point releasePoint) {
    super();
    this.x = x;
    this.y = y;
    this.dx = dx;
    this.dy = dy;
    this.t_shot = t_shot;
    this.t_tap = t_tap;
    this.handlerReleasePoint(releasePoint);
  }

  private void handlerReleasePoint(Point releasePoint) {
    double thetaAngle = this.trajectoryPlanner.getReleaseAngle(new Rectangle(
        this.x, this.y), releasePoint);
    double velocity = this.trajectoryPlanner.getVelocity(thetaAngle);

    this.setReleasePointX(releasePoint.x);
    this.setReleasePointY(releasePoint.y);
    this.setTheta(thetaAngle);
    this.setVelocity(velocity);
  }

  @XmlAttribute
  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  @XmlAttribute
  public String getBirdOnSling() {
    return birdOnSling;
  }

  public void setBirdOnSling(String birdOnSling) {
    this.birdOnSling = birdOnSling;
  }

  @XmlAttribute
  public double getVelocity() {
    return this.velocity;
  }

  public void setVelocity(double velocity) {
    this.velocity = velocity;
  }

  @XmlAttribute(name = "theta")
  public double getThetaDegrees() {
    return Math.toDegrees(this.theta);
  }

  public void setThetaDegrees(double theta) {
    this.theta = Math.toRadians(theta);
  }

  @XmlTransient
  public double getTheta() {
    return this.theta;
  }

  public void setTheta(double theta) {
    this.theta = theta;
  }

  @XmlAttribute
  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

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

  public void setDy(int dy) {
    this.dy = dy;
  }

  @XmlAttribute
  public int getX() {
    return x;
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

  public Point getReleasePoint() {
    return new Point(this.releasePointX, this.releasePointY);
  }

  @XmlAttribute
  public int getReleasePointX() {
    return this.releasePointX;
  }

  public void setReleasePointX(int releasePointX) {
    this.releasePointX = releasePointX;
  }

  @XmlAttribute
  public int getReleasePointY() {
    return this.releasePointY;
  }

  public void setReleasePointY(int releasePointY) {
    this.releasePointY = releasePointY;
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

  @XmlAttribute
  public String getTargetPointType() {
    return targetPointType;
  }

  public void setTargetPointType(String targetPointType) {
    this.targetPointType = targetPointType;
  }  

}
