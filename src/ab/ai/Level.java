package ab.ai;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Level {
  private int id;
  private int number_of_pigs;
  private int number_of_birds;
  private int max_score_avaliable;
  private int number_of_tnts;
  private int number_of_blocks;

  public Level() {

  }

  public Level(int id, int number_of_pigs, int number_of_birds,
      int number_of_tnts, int number_of_blocks) {
    super();
    this.id = id;
    this.number_of_pigs = number_of_pigs;
    this.number_of_birds = number_of_birds;
    this.number_of_tnts = number_of_tnts;
    this.number_of_blocks = number_of_blocks;
    this.max_score_avaliable = this.calcMaxScoreAvaliable();
  }

  private int calcMaxScoreAvaliable() {
    return 10000 * (this.number_of_tnts + this.number_of_birds - 1) + 5000
        * this.number_of_pigs + 500 * this.number_of_blocks;
  }
  
  public void defineMaxScoreAvaliable() {
    this.max_score_avaliable = this.calcMaxScoreAvaliable();
  }

  @XmlAttribute
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @XmlAttribute
  public int getNumber_of_pigs() {
    return number_of_pigs;
  }

  public void setNumber_of_pigs(int number_of_pigs) {
    this.number_of_pigs = number_of_pigs;
  }

  @XmlAttribute
  public int getNumber_of_birds() {
    return number_of_birds;
  }

  public void setNumber_of_birds(int number_of_birds) {
    this.number_of_birds = number_of_birds;
  }

  @XmlAttribute
  public int getMax_score_avaliable() {
    return max_score_avaliable;
  }

  public void setMax_score_avaliable(int max_score_avaliable) {
    this.max_score_avaliable = max_score_avaliable;
  }

  @XmlAttribute
  public int getNumber_of_tnts() {
    return number_of_tnts;
  }

  public void setNumber_of_tnts(int number_of_tnts) {
    this.number_of_tnts = number_of_tnts;
  }

  @XmlAttribute
  public int getNumber_of_blocks() {
    return number_of_blocks;
  }

  public void setNumber_of_blocks(int number_of_blocks) {
    this.number_of_blocks = number_of_blocks;
  }

  public String toString() {
    return String
        .format(
            "Level [id:%d, max_score_avaliable: %d ,number_of_birds: %d, number_of_blocks: %d, "
                + "number_of_pigs: %d, number_of_tnts: %d]", this.getId(),
            this.getMax_score_avaliable(), this.getNumber_of_birds(),
            this.getNumber_of_blocks(), this.getNumber_of_pigs(),
            this.getNumber_of_tnts());
  }
}
