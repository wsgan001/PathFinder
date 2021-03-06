/*

Robot that a best first approach to deal with uncertainty

 */

package src;

import world.Robot;
import world.World;
import java.awt.Point;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;


public class uncertainRobot extends Robot {

    private int numRows;
    private  int numCols;
    private Point endPos;
    private boolean is_uncertian;
    private Node[][] grid;
    private int max_number_of_moves;


    public uncertainRobot(int numCols, int numRows, Point endPos, boolean uncert_flag) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.endPos = endPos;
        this.is_uncertian = uncert_flag;
        this.grid = new Node[this.numRows][this.numCols];
        this.max_number_of_moves = this.get_max_moves();
    }

    public int get_max_moves() {
        int ceil = this.numRows*this.numCols*2;
        return (ceil > 20) ? ceil : 20;
    }

    public void run_random_trace() {
        this.init_grid();
        while (!this.getPosition().equals(this.endPos) && this.getNumMoves() < this.max_number_of_moves) {
            this.move_towards_destination();
        }
    }

    public void init_grid() {
        Point start = this.getPosition();
        this.grid[start.x][start.y] = new Node("S", start);
        this.grid[this.endPos.x][this.endPos.y] = new Node("F", this.endPos);
    }

    public void move_towards_destination() {

        ArrayList<Node> possible_moves = this.ping_layer();
        Node end = new Node("O", this.endPos);

        Point current_pos = this.getPosition();

        while (this.getPosition().equals(current_pos)) {
            Node next_move = this.get_next(possible_moves);
            if (next_move == null) {
                return;
            }
            Point next = next_move.get_position();
            Node grid_node = this.grid[next.x][next.y];
            if (grid_node != null && grid_node.get_symbol().equals("X")) {
                possible_moves.remove(next_move);
                continue;
            } else {
                this.move(next_move.get_position());
            }

            if (this.getPosition().equals(current_pos)) {   // Hit an obstacle
                this.grid[next.x][next.y] = new Node("X", next_move.get_position());
                possible_moves.remove(next_move);
            } else {
                this.grid[this.getX()][this.getY()] = new Node("O", next_move.get_position());
                System.out.print("Moved To: ");
                System.out.println(next_move.get_position());
                if (Node.manhattan_distance(end, next_move) >= Node.manhattan_distance(end, new Node("",current_pos))) {
                    System.out.println("Moved farther away, or did not move any closer");
                    // in this context, the current position is the last position the robot encountered
                    // since we have moved location after current pos was declared
                    // yay variable names
                    this.grid[current_pos.x][current_pos.y] = new Node("X", current_pos);
                }
            }
            if (this.endPos.equals(this.getPosition())) {
                return;
            }
        }
    }

    public Node get_next(ArrayList<Node> possible_moves) {
        Node end = new Node("O", this.endPos);

        if (!possible_moves.isEmpty()) {

            Node next_move = possible_moves.get(0);

            for (int i = 1; i < possible_moves.size(); i++) {
                if (Node.manhattan_distance(end, possible_moves.get(i)) < Node.manhattan_distance(end, next_move)) {
                    next_move = possible_moves.get(i);
                }
            }
            return next_move;
        }
        return null;
    }

    public ArrayList<Node> ping_layer() {
        // Does not actually ping
        Point curr = this.getPosition();
        ArrayList<Node> possible_moves = new ArrayList<Node>();
        for (int i = curr.x-1; i <= curr.x+1; i++) {
            for (int j = curr.y-1; j <= curr.y+1; j++) {
                if (i < 0 || j < 0 || i >= this.numRows || j >= this.numCols) {
                    continue;
                }
                else if (i == curr.x && j == curr.y) {
                    continue;
                }
                else {
                    Point next = new Point(i, j);
                    //String val = this.pingMap(next);
                    possible_moves.add(new Node("", next));
                }
            }
        }
        return possible_moves;
    }


    public void travelToDestination() {

        if (this.is_uncertian) {
            System.out.println("Run uncertain Pathfinder");
            this.run_random_trace();
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
            System.out.println("just for a breakpoint");
            if (path == null) {
                ;
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

    public void execute_moves (ArrayList<Node> path) {
        // once a path has been determined, mosey down the path
        // Method assumes path is backwards (i.e index of 'F' is 0, index of 'S' is path.size() -1)
        int len = path.size()-2;

        for (int i = len; i >= 0; i--) {
            Point pt = path.get(i).get_position();
            this.move(pt);
        }
    }

    public static void main (String[] args) {

        try {

            World myWorld = new World("maps/L_map.txt", false);

            src.uncertainRobot2 robo = new uncertainRobot2(myWorld.numCols(), myWorld.numRows(), myWorld.getEndPos(), false);

            robo.addToWorld(myWorld);

            robo.travelToDestination();

            robo.travelToDestination();

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
