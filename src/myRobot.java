package src;

/**
 * Created by Andrew on 2/21/2015.
 */

import world.Robot;
import world.World;
import java.awt.Point;

public class myRobot extends Robot {

    public void travelToDestination() {
        // see if this point is available
        Point start = this.getPosition();
        System.out.print("Starting Location: ");
        System.out.println(start);
        Point next = new Point(start.x +1, start.y);
        String thing_at_next = super.pingMap(next);
        System.out.println(thing_at_next);
        if (thing_at_next.equals("O")) {
            System.out.print("Moving to valid Point: ");
            System.out.println(super.move( next ));
        }
        System.out.print("Moving to invalid point (not adjacent): ");
        System.out.println(super.move(new Point(5, 3)));
    }

    public static void main (String[] args) {

        try {
            World myWorld = new World("maps/myMap.txt", false);

            System.out.println(myWorld.getStartPos());

            System.out.println(myWorld.getEndPos());

            System.out.println(myWorld.numCols());

            System.out.println(myWorld.numRows());

            myRobot robo = new myRobot();

            robo.addToWorld(myWorld);

            robo.travelToDestination();

            System.out.println("Went to location");
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
