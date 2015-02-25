package src;

/**
 * Created by Andrew on 2/21/2015.
 */

import world.Robot;
import world.World;
import java.awt.Point;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;


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
            ArrayList<Node> graph = this.construct_graph();
            Node start = null;
            Node end = null;

            for (Node n : graph) {
                if (n != null && n.get_symbol().equals("S")) {
                    start = n;
                }
                else if (n != null && n.get_symbol().equals("F")) {
                    end  = n;
                }
            }

            ArrayList<Node> path = this.run_a_star(start, end);
            if (path == null) {
                System.out.println("we did something dumbbbb");
            }
            else {
                this.execute_moves(path);
            }
            System.out.println();
        }
    }

    public ArrayList<Node> run_a_star(Node start, Node goal) {
        ArrayList<Node> closed_set = new ArrayList<Node>();
        ArrayList<Node> open_set = new ArrayList<Node>();
        HashMap<Node, Node> came_from = new HashMap<Node, Node>();

        open_set.add(start);

        HashMap<Node, Integer> g_scores = new HashMap<Node, Integer>();
        g_scores.put(start, 0);

        HashMap<Node, Integer> f_scores = new HashMap<Node, Integer>();
        f_scores.put(start, g_scores.get(start) + Node.manhattan_distance(start, goal)); // CHECK NAME

        while (!open_set.isEmpty()) {

            Node current = open_set.get(0);

            for (int i = 0; i < open_set.size(); i++) {
                if (f_scores.get(open_set.get(i)) < f_scores.get(current)) {
                    current = open_set.get(i);
                }
            }

            if (current.get_symbol().equals("F")) {
                return reconstruct_path(came_from, goal);
            }

            open_set.remove(current);
            closed_set.add(current);

            for (Node n : current.get_neighbors()) {    // CHECK NAME
                if (closed_set.contains(n)) {
                    continue;
                }

                int tentative_g_score = g_scores.get(current) + 1;

                if (!open_set.contains(n) || tentative_g_score < g_scores.get(n)) {
                    came_from.put(n, current);
                    g_scores.put(n, tentative_g_score);
                    f_scores.put(n, g_scores.get(n) + Node.manhattan_distance(n, goal)); // CHECK NAME
                    if (!open_set.contains(n)) {
                        if (!n.get_symbol().equals("X")) { // CHECK NAME
                            open_set.add(n);
                        }
                    }
                }
            }

        }
        return null;
    }

    public ArrayList<Node> reconstruct_path(HashMap<Node, Node> came_from, Node current) {
        ArrayList<Node> total_path = new ArrayList<Node>();
        total_path.add(current);
        while (came_from.containsKey(current)) {
            current = came_from.get(current);
            total_path.add(current);
        }
        return total_path;
    }

    public ArrayList<Node> construct_graph() {
        ArrayList<Node> graph = new ArrayList<Node>();
        Node[][] temp = new Node[this.numRows][this.numCols];

        for (int i = 0; i < this.numRows; i++) {
            for (int j = 0; j < this.numCols; j++) {
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

    public void execute_moves (ArrayList<Node> path) {
        // once a path has been determined, mosey down the path
        // Method assumes path is backwards (i.e index of 'F' is 0, index of 'S' is path.size() -1)
        int len = path.size() -1;
        for (int i = len; i >= 0; i--) {
            Point pt = path.get(i).get_position();
            this.move(pt);
        }
    }

    public static void main (String[] args) {

        try {

            World myWorld = new World("maps/map2.txt", false);

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
