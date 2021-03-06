/*
runs a better, statistically motivated approach to deal with uncertainty
 */

package src;

import world.Robot;
import world.World;
import java.awt.Point;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;


public class uncertainRobot2 extends Robot {

    private int numRows;
    private  int numCols;
    private Point endPos;
    private boolean is_uncertian;
    private Node[][] grid;
    private int max_number_of_moves;


    public uncertainRobot2(int numCols, int numRows, Point endPos, boolean uncert_flag) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.endPos = endPos;
        this.is_uncertian = uncert_flag;
        this.grid = new Node[this.numRows][this.numCols];
        this.max_number_of_moves = this.get_max_moves();
    }

    public int get_max_moves() {
        int ceil = this.numRows*this.numCols;
        return (ceil > 20) ? ceil*2 : 20;
    }

    public void run_layered_trace() {
        this.init_grid();
        while (!this.getPosition().equals(this.endPos) && this.getNumMoves() < this.max_number_of_moves) {
            this.move_towards_destination_layered();
        }
    }

    public void move_towards_destination_layered() {

        int radius = 6;
        ArrayList<Node> path = null;
        do {
            Node[][] subgrid = this.ping_layer_radius(radius);
            Node start = null;
            Node end = null;
            ArrayList<Node> subgraph = construct_subgraph(subgrid);
            for (Node n : subgraph) {
                if (this.getPosition().equals(n.get_position())) {
                    start = n;
                }
            }

            Node fake_end = new Node("", this.endPos);
            int current_shortest_distance = Node.manhattan_distance(start, fake_end);
            for (int i = 0; i < subgraph.size(); i++) {
                if (subgraph.get(i).get_symbol().equals("O") || subgraph.get(i).get_symbol().equals("F") || subgraph.get(i).get_symbol().equals("S")) {
                    if (current_shortest_distance > Node.manhattan_distance(subgraph.get(i), fake_end)) {
                        current_shortest_distance = Node.manhattan_distance(subgraph.get(i), fake_end);
                        end = subgraph.get(i);
                    }
                }
            }

            path = run_a_star(start, end);
            radius += 1;
        } while (path == null);

        this.execute_moves(path);
    }

    public void init_grid() {
        Point start = this.getPosition();
        this.grid[start.x][start.y] = new Node("S", start);
        this.grid[this.endPos.x][this.endPos.y] = new Node("F", this.endPos);
    }


    public Node[][] ping_layer_radius(int r /* r stands for radius =) */) {
        Point curr = this.getPosition();
        int minx = (curr.x-r >= 0) ? curr.x-r : 0;
        int miny = (curr.y-r >= 0) ? curr.y-r : 0;
        int maxx = (curr.x+r < this.numRows ) ? curr.x+r : this.numRows-1;
        int maxy = (curr.y+r < this.numCols ) ?  curr.y+r : this.numCols-1;
        int relx = maxx-minx+1;
        int rely = maxy-miny+1;
        Node[][] subgrid = new Node[relx][rely];

        ArrayList<Node> possible_moves = new ArrayList<Node>();
        for (int i = curr.x-r; i <= curr.x+r; i++) {
            for (int j = curr.y-r; j <= curr.y+r; j++) {
                if (i < 0 || j < 0 || i >= this.numRows || j >= this.numCols) {
                    continue;
                }
                else {
                    Point next = new Point(i, j);
                    String val = this.determine_value(next, 3);
                    Node n = new Node(val, next);
                    subgrid[i-minx][j-miny] = n;
                }

            }
        }
        return subgrid;
    }

    public String determine_value(Point target, int multiplier) {
        if (this.grid[target.x][target.y] != null) {
            return this.grid[target.x][target.y].get_symbol();
        } else {
            String val;
            int count = 0;
            Point current = this.getPosition();
            int distance = (int) Math.floor(Point.distance(target.x, target.y, current.x, current.y));
            int trys = distance * multiplier;
            for (int i = 0; i <= trys; i++) {
                val = this.pingMap(target);
                if (val.equals("X")) count--;
                else count++;
            }
            if (count <= 0) return "X";
            else return "O";
        }
    }

    public void travelToDestination() {

        if (this.is_uncertian) {
            System.out.println("Run Uncertain Pathfinder");
            this.run_layered_trace();
        }

        else {
            System.out.println("Run the certain pathfinder");
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

            if (current.equals(goal)) {
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
                // ERROR was here! originally had this.numRows instead of .numCols
                if ((( j + 1) < this.numCols) && ((i - 1) >= 0)){
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

    public ArrayList<Node> construct_subgraph(Node[][] temp) {
        ArrayList<Node> graph = new ArrayList<Node>();
        int rows = temp.length - 1;
        int cols = temp[0].length - 1;
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                Node n = temp[i][j];
                if (( j - 1) >= 0) {
                    // the position to the left
                    if (temp[i][j-1] != null) n.add_neighbor(temp[i][j-1]);
                }
                if ((i - 1) >= 0) {
                    // the position to the top
                    if (temp[i-1][j] != null) n.add_neighbor(temp[i-1][j]);
                }
                if ((( j + 1) <= cols) && ((i - 1) >= 0)){
                    // position to the top right
                    if (temp[i-1][j+1] != null) n.add_neighbor(temp[i-1][j+1]);
                }
                if (((i - 1) >= 0) && (( j - 1) >= 0)) {
                    // position to the top left
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
        int len = path.size()-2;
        Point last_position = this.getPosition();
        for (int i = len; i >= 0; i--) {
            Point pt = path.get(i).get_position();
            System.out.print("Trying to move to: ");
            System.out.println(pt);
            this.move(pt);
            // in case you cannot move anymore, stop moving
            if (last_position.equals(this.getPosition())) {
                System.out.println("Hit an X =(");
                this.grid[pt.x][pt.y] = new Node("X", pt);
                break;
            }
            else {
                this.grid[this.getX()][this.getY()] = new Node("O", this.getPosition());
                last_position = this.getPosition();
            }
        }
    }

    public static void main (String[] args) {

        try {

            World myWorld = new World("maps/myInputFile10.txt", false);

            src.uncertainRobot2 robo = new src.uncertainRobot2(myWorld.numCols(), myWorld.numRows(), myWorld.getEndPos(), false);

            robo.addToWorld(myWorld);

            robo.travelToDestination();

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
