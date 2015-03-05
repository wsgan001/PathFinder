package src;

import world.World;

/**
 * Created by Andrew on 3/5/2015.
 */
public class TestHarness {

    public static void main (String[] args) {

        try {

            boolean uncert = true;
            String file = "maps/L_map.txt";

            World w1 = new World(file, uncert);
            uncertainRobot ur1_1 = new uncertainRobot(w1.numCols(), w1.numRows(), w1.getEndPos(), uncert);
            uncertainRobot2 ur2_1 = new uncertainRobot2(w1.numCols(), w1.numRows(), w1.getEndPos(), uncert);
            ur2_1.addToWorld(w1);
            ur2_1.travelToDestination();

        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }
}
