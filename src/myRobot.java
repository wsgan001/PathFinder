package src;

/**
 * Created by Andrew on 2/21/2015.
 */

import world.Robot;
import world.World;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class myRobot extends Robot {

    private int numRows;
    private  int numCols;
    private Point endPos;
    private boolean is_uncertian;

    public myRobot(int numCols, int numRows, Point endPos, boolean uncert_flag) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.endPos = endPos;
        this.is_uncertian = uncert_flag;
    }

    public void travelToDestination() {

        if (this.is_uncertian) {
            System.out.println("Run uncertian Pathfinder");
        }

        else {
            System.out.println("Run the certian pathfinder");
            this.run_a_star();
            this.move(new Point(1,1));
            this.move(new Point(2,0));

        }


    }

    public void run_a_star() {
        System.out.println("running A* algo to find path");
        ArrayList<Node> graph = construct_graph();
        System.out.println("Constructed Graph");
    }

    public ArrayList<Node> construct_graph() {
        ArrayList<Node> graph = new ArrayList<Node>();
        Node[][] temp = new Node[this.numCols][this.numRows];

        for (int i = 0; i < this.numCols; i++) {
            for (int j = 0; j < this.numRows; j++) {
                Point pt = new Point(i,j);
                String current_obj = this.pingMap(pt);
                Node n = new Node(current_obj, pt);
                temp[i][j] = n;
                if (( j - 1) >= 0) {
                    // the position to the top
                    if (temp[i][j-1] != null) n.add_neighbor(temp[i][j-1]);
                }
                if ((i - 1) >= 0) {
                    // the position to the left
                    if (temp[i-1][j] != null) n.add_neighbor(temp[i-1][j]);
                }
                if ((( j + 1) < this.numRows) && ((i - 1) >= 0)){
                    // position to the top left
                    if (temp[i-1][j+1] != null) n.add_neighbor(temp[i-1][j+1]);
                }
                if (((i - 1) >= 0) && (( j - 1) >= 0)) {
                    // position to the top right
                    if (temp[i-1][j-1] != null) n.add_neighbor(temp[i-1][j-1]);
                }
                graph.add(n);
            }
        }

        return graph;
    }



    public static void main (String[] args) {

        try {

            World myWorld = new World("maps/myMap.txt", false);

            System.out.println(myWorld.getStartPos());

            System.out.println(myWorld.getEndPos());

            System.out.println(myWorld.numCols());

            System.out.println(myWorld.numRows());

            myRobot robo = new myRobot(myWorld.numCols(), myWorld.numRows(), myWorld.getEndPos(), false);

            robo.addToWorld(myWorld);

            robo.travelToDestination();

            System.out.println("Went to location");

            System.out.println(robo.pingMap(new Point(4,4)));
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
