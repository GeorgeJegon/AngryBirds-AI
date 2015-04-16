/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 **To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgent implements Runnable {

    private ActionRobot aRobot;
    private Random randomGenerator;
    public int currentLevel = 1;
    public static int time_limit = 12;
    private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
    TrajectoryPlanner tp;
    private boolean firstShot;
    private int counterGames=0;
    private Point prevTarget;

    private Heuristic heuristic;


    // a standalone implementation of the Naive Agent
    public NaiveAgent() {

        aRobot = new ActionRobot();
        tp = new TrajectoryPlanner();
        prevTarget = null;
        firstShot = true;
        randomGenerator = new Random();
        // --- go to the Poached Eggs episode level selection page ---
        ActionRobot.GoFromMainMenuToLevelSelection();

    }


    // run the client
    public void run() {
        aRobot.loadLevel(currentLevel);
        while (true) {

            GameState state = solve();
            if (state == GameState.WON) {
                counterGames=0;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int score = StateUtil.getScore(ActionRobot.proxy);
                if(!scores.containsKey(currentLevel))
                    scores.put(currentLevel, score);
                else
                {
                    if(scores.get(currentLevel) < score)
                        scores.put(currentLevel, score);
                }
                int totalScore = 0;
                for(Integer key: scores.keySet()){

                    totalScore += scores.get(key);
                    System.out.println(" Level " + key
                            + " Score: " + scores.get(key) + " ");
                }
                System.out.println("Total Score: " + totalScore);
                aRobot.loadLevel(++currentLevel);
                // make a new trajectory planner whenever a new level is entered
                tp = new TrajectoryPlanner();

                // first shot on this level, try high shot first
                firstShot = true;
            } else if (state == GameState.LOST) {
                System.out.println("Restart");
                counterGames++;
                if(counterGames==3)
                    aRobot.loadLevel(++currentLevel);
                else
                    aRobot.restartLevel();
            } else if (state == GameState.LEVEL_SELECTION) {
                System.out
                        .println("Unexpected level selection page, go to the last current level : "
                                + currentLevel);
                aRobot.loadLevel(currentLevel);
            } else if (state == GameState.MAIN_MENU) {
                System.out
                        .println("Unexpected main menu page, go to the last current level : "
                                + currentLevel);
                ActionRobot.GoFromMainMenuToLevelSelection();
                aRobot.loadLevel(currentLevel);
            } else if (state == GameState.EPISODE_MENU) {
                System.out
                        .println("Unexpected episode menu page, go to the last current level : "
                                + currentLevel);
                ActionRobot.GoFromMainMenuToLevelSelection();
                aRobot.loadLevel(currentLevel);
            }

        }

    }

    private double distance(Point p1, Point p2) {
        return Math
                .sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
                        * (p1.y - p2.y)));
    }

    private List<String> blocksDescription(Vision vision){
        List<String> desc = new ArrayList<String>();
        String res="";
        List<ABObject> blocks = vision.findBlocksRealShape();
        ABObject block=null;

        if(!blocks.isEmpty()){
            for(int j=1;j<blocks.size();j++){
                block = blocks.get(j);
                res = "Block Number: "+block.id+",Block Shape: "+block.shape.toString()+",Block Material: "+block.getType().toString();
                System.out.println("Block Number:"+block.id+",Block Shape:"+block.shape.toString()+",Block Material:"+block.getType().toString());
                desc.add(res);
            }
        }
        return desc;
    }



    public GameState solve()
    {

        // captura a imagem
        BufferedImage screenshot = ActionRobot.doScreenShot();

        // processa a imagem
        Vision vision = new Vision(screenshot);

        // encontra o estilingue
        Rectangle sling = vision.findSlingshotMBR();

        // confirma o estilingue
        while (sling == null && aRobot.getState() == GameState.PLAYING) {
            System.out.println("Estilingue não encontrado, por favor remova o zoom");
            ActionRobot.fullyZoomOut();
            screenshot = ActionRobot.doScreenShot();
            vision = new Vision(screenshot);
            sling = vision.findSlingshotMBR();
        }
        // captura todos os porcos
        List<ABObject> pigs = vision.findPigsMBR();

        GameState state = aRobot.getState();

        if (sling != null) {

            if (!pigs.isEmpty()) {

                heuristic = new Heuristic(vision,tp,aRobot,counterGames);

                Point releasePoint = null;
                Shot shot = new Shot();
                int dx,dy;
                Point refPoint = tp.getReferencePoint(sling);
                {
                    Point _tpt = heuristic.solve();

                    ArrayList<Point> pts = tp.estimateLaunchPoint_1(sling, _tpt);
                    ArrayList<Point> pts1=null;

                    if(heuristic.targetIsPig && !pts.contains(_tpt)){
                        pts1 = tp.estimateLaunchPoint(sling, _tpt);
                        if(pts1.contains(_tpt))
                            pts=pts1;
                    }
                    prevTarget = new Point(_tpt.x, _tpt.y);


                    if (firstShot && pts.size() > 1)
                    {
                        releasePoint = pts.get(1);
                    }
                    else if (pts.size() == 1)
                        releasePoint = pts.get(0);
                    else if (pts.size() == 2)
                    {
                        if (randomGenerator.nextInt(6) == 0)
                            releasePoint = pts.get(1);
                        else
                            releasePoint = pts.get(0);
                    }
                    else
                    if(pts.isEmpty())
                    {
                        System.out.println("Não encontrou ponto para acertar o alvo");
                        System.out.println("Tentando tiro em 45 graus");
                        releasePoint = tp.findReleasePoint(sling, Math.PI/4);
                    
                    //Calculando tapping de acordo com o tipo de passaro
                    if (releasePoint != null) {
                        double releaseAngle = tp.getReleaseAngle(sling,
                                releasePoint);
                        System.out.println("Release Point: " + releasePoint);
                        System.out.println("Release Angle: "
                                + Math.toDegrees(releaseAngle));
                        int tapInterval = 0;
                        switch (aRobot.getBirdTypeOnSling())
                        {
                            case RedBird:
                                tapInterval = 0; break;               // começo da trajetoria
                            case YellowBird:
                                tapInterval = 75 + randomGenerator.nextInt(15);break; // 75-90% do caminho
                            case WhiteBird:
                                tapInterval =  80 + randomGenerator.nextInt(10);break; // 80-90% o caminho
                            case BlackBird:
                                tapInterval =  80 + randomGenerator.nextInt(10);break; // 80-90% o caminho
                            case BlueBird:
                                tapInterval =  70 + randomGenerator.nextInt(20);break; // 75-85% o caminho
                            default:
                                tapInterval =  60;
                        }

                        int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
                        dx = (int)releasePoint.getX() - refPoint.x;
                        dy = (int)releasePoint.getY() - refPoint.y;
                        shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
                    }
                    else
                    {
                        return state;
                    }
                }
                {
                    ActionRobot.fullyZoomOut();
                    screenshot = ActionRobot.doScreenShot();
                    vision = new Vision(screenshot);
                    Rectangle _sling = vision.findSlingshotMBR();
                    if(_sling != null)
                    {
                        double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
                        if(scale_diff < 25)
                        {
                            if(dx < 0)
                            {
                                aRobot.cshoot(shot);
                                state = aRobot.getState();
                                if ( state == GameState.PLAYING )
                                {
                                    screenshot = ActionRobot.doScreenShot();
                                    vision = new Vision(screenshot);
                                    List<Point> traj = vision.findTrajPoints();
                                    tp.adjustTrajectory(traj, sling, releasePoint);
                                    firstShot = false;
                                }
                            }
                        }
                        else
                            System.out.println("Mudou o zoom, não consigo executar o tiro, re-segmentando a imagem");
                    }
                    else
                        System.out.println("não encontrei o estilingue, não consigo executar o tiro, re-segmentando a imagem");
                }

            }

        }
        return state;
    }

    public static void main(String args[]) {

        NaiveAgent na = new NaiveAgent();
        if (args.length > 0)
            na.currentLevel = Integer.parseInt(args[1]);
        na.run();

    }
}
