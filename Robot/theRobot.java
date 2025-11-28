
import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.*;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.net.*;


// This class draws the probability map and value iteration map that you create to the window
// You need only call updateProbs() and updateValues() from your theRobot class to update these maps
class mySmartMap extends JComponent implements KeyListener {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int STAY = 4;

    int currentKey;

    int winWidth, winHeight;
    double sqrWdth, sqrHght;
    Color gris = new Color(170,170,170);
    Color myWhite = new Color(220, 220, 220);
    World mundo;
    
    int gameStatus;

    double[][] probs;
    double[][] vals;
    
    public mySmartMap(int w, int h, World wld) {
        mundo = wld;
        probs = new double[mundo.width][mundo.height];
        vals = new double[mundo.width][mundo.height];
        winWidth = w;
        winHeight = h;
        
        sqrWdth = (double)w / mundo.width;
        sqrHght = (double)h / mundo.height;
        currentKey = -1;
        
        addKeyListener(this);
        
        gameStatus = 0;
    }
    
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }
    
    public void setWin() {
        gameStatus = 1;
        repaint();
    }
    
    public void setLoss() {
        gameStatus = 2;
        repaint();
    }
    
    public void updateProbs(double[][] _probs) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                probs[x][y] = _probs[x][y];
            }
        }
        
        repaint();
    }
    
    public void updateValues(double[][] _vals) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                vals[x][y] = _vals[x][y];
            }
        }
        
        repaint();
    }

    public void paint(Graphics g) {
        paintProbs(g);
        //paintValues(g);
    }

    public void paintProbs(Graphics g) {
        double maxProbs = 0.0;
        int mx = 0, my = 0;
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (probs[x][y] > maxProbs) {
                    maxProbs = probs[x][y];
                    mx = x;
                    my = y;
                }
                if (mundo.grid[x][y] == 1) {
                    g.setColor(Color.black);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 0) {
                    //g.setColor(myWhite);
                    
                    int col = (int)(255 * Math.sqrt(probs[x][y]));
                    if (col > 255)
                        col = 255;
                    g.setColor(new Color(255-col, 255-col, 255));
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 2) {
                    g.setColor(Color.red);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 3) {
                    g.setColor(Color.green);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
            
            }
            if (y != 0) {
                g.setColor(gris);
                g.drawLine(0, (int)(y * sqrHght), (int)winWidth, (int)(y * sqrHght));
            }
        }
        for (int x = 0; x < mundo.width; x++) {
                g.setColor(gris);
                g.drawLine((int)(x * sqrWdth), 0, (int)(x * sqrWdth), (int)winHeight);
        }
        
        //System.out.println("repaint maxProb: " + maxProbs + "; " + mx + ", " + my);
        
        g.setColor(Color.green);
        g.drawOval((int)(mx * sqrWdth)+1, (int)(my * sqrHght)+1, (int)(sqrWdth-1.4), (int)(sqrHght-1.4));
        
        if (gameStatus == 1) {
            g.setColor(Color.green);
            g.drawString("You Won!", 8, 25);
        }
        else if (gameStatus == 2) {
            g.setColor(Color.red);
            g.drawString("You're a Loser!", 8, 25);
        }
    }
    
    public void paintValues(Graphics g) {
        double maxVal = -99999, minVal = 99999;
        int mx = 0, my = 0;
        
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (mundo.grid[x][y] != 0)
                    continue;
                
                if (vals[x][y] > maxVal)
                    maxVal = vals[x][y];
                if (vals[x][y] < minVal)
                    minVal = vals[x][y];
            }
        }
        if (minVal == maxVal) {
            maxVal = minVal+1;
        }

        int offset = winWidth+20;
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (mundo.grid[x][y] == 1) {
                    g.setColor(Color.black);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 0) {
                    //g.setColor(myWhite);
                    
                    //int col = (int)(255 * Math.sqrt((vals[x][y]-minVal)/(maxVal-minVal)));
                    int col = (int)(255 * (vals[x][y]-minVal)/(maxVal-minVal));
                    if (col > 255)
                        col = 255;
                    g.setColor(new Color(255-col, 255-col, 255));
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 2) {
                    g.setColor(Color.red);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 3) {
                    g.setColor(Color.green);
                    g.fillRect((int)(x * sqrWdth)+offset, (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
            
            }
            if (y != 0) {
                g.setColor(gris);
                g.drawLine(offset, (int)(y * sqrHght), (int)winWidth+offset, (int)(y * sqrHght));
            }
        }
        for (int x = 0; x < mundo.width; x++) {
                g.setColor(gris);
                g.drawLine((int)(x * sqrWdth)+offset, 0, (int)(x * sqrWdth)+offset, (int)winHeight);
        }
    }

    
    public void keyPressed(KeyEvent e) {
        //System.out.println("keyPressed");
    }
    public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased");
    }
    public void keyTyped(KeyEvent e) {
        char key = e.getKeyChar();
        //System.out.println(key);
        
        switch (key) {
            case 'i':
                currentKey = NORTH;
                break;
            case ',':
                currentKey = SOUTH;
                break;
            case 'j':
                currentKey = WEST;
                break;
            case 'l':
                currentKey = EAST;
                break;
            case 'k':
                currentKey = STAY;
                break;
        }
    }
}


// This is the main class that you will add to in order to complete the lab
public class theRobot extends JFrame {
    // Mapping of actions to integers
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int STAY = 4;

    // Value iteration constants
    public static final double REWARD_OPEN = -0.04;     // State rewards
    public static final double REWARD_GOAL = 1.0;
    public static final double REWARD_STAIRWELL = -10.0;

    public static final double GAMMA_FACTOR = 0.99;      // Discount factor for future rewards
    public static final double CONVERGENCE_EPSILON = 0.001;   // Convergence threshold

    // Random exploration constant
    public static final double EXPLORATION_EPSILON = 0.2; // Chance to explore

    Color bkgroundColor = new Color(230,230,230);
    
    static mySmartMap myMaps; // instance of the class that draw everything to the GUI
    String mundoName;
    
    World mundo; // mundo contains all the information about the world.  See World.java
    double moveProb, sensorAccuracy;  // stores probabilities that the robot moves in the intended direction
                                      // and the probability that a sonar reading is correct, respectively
    
    // variables to communicate with the Server via sockets
    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    
    // variables to store information entered through the command-line about the current scenario
    boolean isManual = false; // determines whether you (manual) or the AI (automatic) controls the robots movements
    boolean knownPosition = false;
    int startX = -1, startY = -1;
    int decisionDelay = 250;
    
    // Stores probability map of robot location
    double[][] probs;

    // Stores computed value of being in each state (x, y)
    double[][] Vs;
    
    // Helper function to normalize a probability array
    private void normalizeProbabilities(double[][] probArray) {
        double totalProb = 0.0;
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                totalProb += probArray[x][y];
            }
        }
        
        // Normalize if total > 0
        if (totalProb > 0) {
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    probArray[x][y] /= totalProb;
                }
            }
        }
    }

    // Helper function to check if coordinates are valid and navigable
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < mundo.width && y >= 0 && y < mundo.height && mundo.grid[x][y] == 0;
    }

    // Helper function to check if there's a wall or boundary at given coordinates
    private boolean isWallAt(int x, int y) {
        return x < 0 || x >= mundo.width || y < 0 || y >= mundo.height || mundo.grid[x][y] == 1;
    }

    // Helper function to get the intended source position for a given action
    private int[] getSourcePosition(int destX, int destY, int action) {
        int sourceX = destX, sourceY = destY;
        switch (action) {
            case NORTH: sourceY = destY + 1; break;
            case SOUTH: sourceY = destY - 1; break;
            case EAST: sourceX = destX - 1; break;
            case WEST: sourceX = destX + 1; break;
            case STAY: sourceX = destX; sourceY = destY; break;
        }
        return new int[]{sourceX, sourceY};
    }
    
    // Get destination position for a given action from source
    private int[] getDestinationPosition(int sourceX, int sourceY, int action) {
        int destX = sourceX, destY = sourceY;
        switch (action) {
            case NORTH: destY = sourceY - 1; break;
            case SOUTH: destY = sourceY + 1; break;
            case EAST: destX = sourceX + 1; break;
            case WEST: destX = sourceX - 1; break;
            case STAY: destX = sourceX; destY = sourceY; break;
        }
        return new int[]{destX, destY};
    }
    
    // Calculate total sensor likelihood for a position given sonar readings
    private double calculateSensorLikelihood(int x, int y, String sonars) {
        int correct = 0;
        int incorrect = 0;

        // actual walls: North, South, East, West
        boolean[] actual = new boolean[] {
            isWallAt(x, y-1), // North
            isWallAt(x, y+1), // South
            isWallAt(x+1, y), // East
            isWallAt(x-1, y)  // West
        };

        // assume sonars string has at least 4 chars (same assumption as original code)
        for (int i = 0; i < 4; i++) {
            boolean sensed = (sonars.charAt(i) == '1');
            if (actual[i] == sensed) correct++;
            else incorrect++;
        }

        return Math.pow(sensorAccuracy, correct) * Math.pow(1.0 - sensorAccuracy, incorrect);
    }
    

    
    // Helper function to perform the correction step of the Bayes filter
    private void sensorModel(double[][] predictionProbs, String sonars) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (!isValidPosition(x, y)) {
                    probs[x][y] = 0.0;
                    continue;
                }
                
                // Calculate sensor likelihood for this position
                double sensorLikelihood = calculateSensorLikelihood(x, y, sonars);
                
                // Apply unnormalized Bayes update
                probs[x][y] = sensorLikelihood * predictionProbs[x][y];
            }
        }
    }
    
    public theRobot(String _manual, int _decisionDelay) {
        // initialize variables as specified from the command-line
        if (_manual.equals("automatic"))
            isManual = false;
        else
            isManual = true;
        decisionDelay = _decisionDelay;
        
        // get a connection to the server and get initial information about the world
        initClient();
    
        // Read in the world
        mundo = new World(mundoName);
        
        // set up the GUI that displays the information you compute
        int width = 500;
        int height = 500;
        int bar = 20;
        setSize(width,height+bar);
        getContentPane().setBackground(bkgroundColor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, width, height+bar);
        myMaps = new mySmartMap(width, height, mundo);
        getContentPane().add(myMaps);
        
        setVisible(true);
        setTitle("Probability and Value Maps");
        
        doStuff(); // Function to have the robot move about its world until it gets to its goal or falls in a stairwell
    }
    
    // this function establishes a connection with the server and learns
    //   1 -- which world it is in
    //   2 -- it's transition model (specified by moveProb)
    //   3 -- it's sensor model (specified by sensorAccuracy)
    //   4 -- whether it's initial position is known.  if known, its position is stored in (startX, startY)
    public void initClient() {
        int portNumber = 3333;
        String host = "localhost";
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            mundoName = sin.readLine();
            moveProb = Double.parseDouble(sin.readLine());
            sensorAccuracy = Double.parseDouble(sin.readLine());
            System.out.println("Need to open the mundo: " + mundoName);
            System.out.println("moveProb: " + moveProb);
            System.out.println("sensorAccuracy: " + sensorAccuracy);
            
            // find out of the robots position is know
            String _known = sin.readLine();
            if (_known.equals("known")) {
                knownPosition = true;
                startX = Integer.parseInt(sin.readLine());
                startY = Integer.parseInt(sin.readLine());
                System.out.println("Robot's initial position is known: " + startX + ", " + startY);
            }
            else {
                System.out.println("Robot's initial position is unknown");
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    // function that gets human-specified actions
    // 'i' specifies the movement up
    // ',' specifies the movement down
    // 'l' specifies the movement right
    // 'j' specifies the movement left
    // 'k' specifies the movement stay
    int getHumanAction() {
        System.out.println("Reading the action selected by the user");
        while (myMaps.currentKey < 0) {
            try {
                Thread.sleep(50);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        int a = myMaps.currentKey;
        myMaps.currentKey = -1;
        
        System.out.println("Action: " + a);
        
        return a;
    }
    
    // initializes the probabilities of where the AI is
    void initializeProbabilities() {
        probs = new double[mundo.width][mundo.height];
        // if the robot's initial position is known, reflect that in the probability map
        if (knownPosition) {
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if ((x == startX) && (y == startY))
                        probs[x][y] = 1.0;
                    else
                        probs[x][y] = 0.0;
                }
            }
        }
        else {  // otherwise, set up a uniform prior over all the positions in the world that are open spaces
            int count = 0;
            
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if (mundo.grid[x][y] == 0)
                        count++;
                }
            }
            
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    if (mundo.grid[x][y] == 0)
                        probs[x][y] = 1.0 / count;
                    else
                        probs[x][y] = 0;
                }
            }
        }
        
        myMaps.updateProbs(probs);
    }

    
    // Given an action taken and sonar readings received, update the probability map with a Bayes filter
    void updateProbabilities(int action, String sonars) {
        // transition model
        double[][] predictionProbs = transitionModel(action);
        
        // sensor model
        sensorModel(predictionProbs, sonars);
        
        // normalization
        normalizeProbabilities(probs);

        myMaps.updateProbs(probs);
    }
    
    // Perform the transition step of the Bayes filter
    private double[][] transitionModel(int action) {
        double[][] predictionProbs = new double[mundo.width][mundo.height];
        double unintendedProb = (1.0 - moveProb) / 4.0;
        
        // For each current source position in the world
        for (int sourceY = 0; sourceY < mundo.height; sourceY++) {
            for (int sourceX = 0; sourceX < mundo.width; sourceX++) {
                if (!isValidPosition(sourceX, sourceY)) {
                    continue; // Skip invalid source positions
                }
                
                // Calculate where the robot would end up with each possible action
                // Intended action (with probability moveProb)
                int[] intendedDest = getDestinationPosition(sourceX, sourceY, action);
                int finalX = intendedDest[0];
                int finalY = intendedDest[1];
                
                // If intended destination hits a wall, robot stays at source
                if (isWallAt(finalX, finalY)) {
                    finalX = sourceX;
                    finalY = sourceY;
                }
                
                // Add intended transition probability
                if (isValidPosition(finalX, finalY)) {
                    predictionProbs[finalX][finalY] += moveProb * probs[sourceX][sourceY];
                }
                
                // Unintended actions (each with probability unintendedProb)
                int[] unintendedActions = {NORTH, SOUTH, EAST, WEST, STAY};
                for (int unintendedAction : unintendedActions) {
                    if (unintendedAction == action) continue; // Skip intended action
                    
                    int[] unintendedDest = getDestinationPosition(sourceX, sourceY, unintendedAction);
                    int unintendedFinalX = unintendedDest[0];
                    int unintendedFinalY = unintendedDest[1];
                    
                    // If unintended destination hits a wall, robot stays at source
                    if (isWallAt(unintendedFinalX, unintendedFinalY)) {
                        unintendedFinalX = sourceX;
                        unintendedFinalY = sourceY;
                    }
                    
                    // Add unintended transition probability
                    if (isValidPosition(unintendedFinalX, unintendedFinalY)) {
                        predictionProbs[unintendedFinalX][unintendedFinalY] += unintendedProb * probs[sourceX][sourceY];
                    }
                }
            }
        }
        
        return predictionProbs;
    }

    // Perform value iteration to compute the value of each state
    void valueIteration() {
        // Populates the 'Vs' array with computed values for each state using the constants defined in the robot class
        
        // Initialize Vs array
        Vs = new double[mundo.width][mundo.height];
        
        // Initialize all values to 0
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                Vs[x][y] = 0.0;
            }
        }
        
        double maxDelta;
        int iterations = 0;
        
        // Iterate until convergence
        do {
            maxDelta = 0.0;
            double[][] newVs = new double[mundo.width][mundo.height];
            
            // For each state in the grid
            for (int y = 0; y < mundo.height; y++) {
                for (int x = 0; x < mundo.width; x++) {
                    // Skip walls
                    if (mundo.grid[x][y] == 1) {
                        newVs[x][y] = 0.0;
                        continue;
                    }
                    
                    // Get the reward for this state
                    double reward;
                    if (mundo.grid[x][y] == 0) {
                        reward = REWARD_OPEN;  // Open space
                    } else if (mundo.grid[x][y] == 3) {
                        reward = REWARD_GOAL;  // Goal
                    } else if (mundo.grid[x][y] == 2) {
                        reward = REWARD_STAIRWELL;  // Stairwell
                    } else {
                        throw new IllegalStateException("Unexpected grid value");
                    }
                    
                    // Terminal states have fixed values equal to their rewards
                    if (mundo.grid[x][y] == 2 || mundo.grid[x][y] == 3) {
                        newVs[x][y] = reward;
                        continue;
                    }
                    
                    // For non-terminal states, compute the value using Bellman equation
                    // Find the maximum expected value over all possible actions
                    double maxActionValue = Double.NEGATIVE_INFINITY;
                    
                    int[] actions = {NORTH, SOUTH, EAST, WEST, STAY};
                    for (int action : actions) {
                        double expectedValue = 0.0;
                        
                        // Calculate expected value for this action
                        // considering the transition probabilities
                        double unintendedProb = (1.0 - moveProb) / 4.0;
                        
                        // For each possible actual action (intended + unintended)
                        int[] possibleActions = {NORTH, SOUTH, EAST, WEST, STAY};
                        for (int actualAction : possibleActions) {
                            // Determine probability of this actual action occurring
                            double actionProb;
                            if (actualAction == action) {
                                actionProb = moveProb;  // Intended action
                            } else {
                                actionProb = unintendedProb;  // Unintended action
                            }
                            
                            // Get destination for this actual action
                            int[] dest = getDestinationPosition(x, y, actualAction);
                            int destX = dest[0];
                            int destY = dest[1];
                            
                            // If destination is a wall or out of bounds, stay in current position
                            if (isWallAt(destX, destY)) {
                                destX = x;
                                destY = y;
                            }
                            
                            // Add to expected value: probability * value of destination state
                            expectedValue += actionProb * Vs[destX][destY];
                        }
                        
                        // Keep track of the maximum expected value across all actions
                        if (expectedValue > maxActionValue) {
                            maxActionValue = expectedValue;
                        }
                    }
                    
                    // Bellman update: V(s) = R(s) + gamma * max_a sum_s' P(s'|s,a) * V(s')
                    newVs[x][y] = reward + GAMMA_FACTOR * maxActionValue;
                    
                    // Track maximum change for convergence check
                    double delta = Math.abs(newVs[x][y] - Vs[x][y]);
                    if (delta > maxDelta) {
                        maxDelta = delta;
                    }
                }
            }
            
            // Update Vs with new values
            Vs = newVs;
            iterations++;
            
        } while (maxDelta > CONVERGENCE_EPSILON);
        
        System.out.println("Value iteration converged after " + iterations + " iterations");
        
        // Print all values in a grid with even spacing
        System.out.println("State values (rows = y, cols = x):");
        double maxV = Double.NEGATIVE_INFINITY;
        double minV = Double.POSITIVE_INFINITY;
        for (int y = 0; y < mundo.height; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < mundo.width; x++) {
                double v = Vs[x][y];
                sb.append(String.format("%8.2f", v)); // fixed-width field for even spacing
                if (x < mundo.width - 1) sb.append(' ');
                if (v > maxV) maxV = v;
                if (v < minV) minV = v;
            }
            System.out.println(sb.toString());
        }
        System.out.println(String.format("min=%.2f max=%.2f", minV, maxV));
        
        // Update the GUI with computed values
        myMaps.updateValues(Vs);
    }
    
    // Automatically selects an action for the using the maximum expected utility principle
    int automaticAction(boolean useBestUtility) {
        // Maximum expected utility action selection
        if (useBestUtility) {
            return getBestUtilityAction();
        }

        // Epsilon-greedy action selection
        if (Math.random() < EXPLORATION_EPSILON) {
            // Explore: choose random action
            int[] actions = {NORTH, SOUTH, EAST, WEST};
            System.out.println("Random action exploration");
            return actions[(int)(Math.random() * actions.length)];
        } else {
            // Exploit: choose best known action
            return getBestUtilityAction();
        }
    }

    int getBestUtilityAction() {
        double maxUtility = Double.NEGATIVE_INFINITY;
        int bestAction = STAY;

        for (int action : new int[]{NORTH, SOUTH, EAST, WEST, STAY}) {
            double utility = computeExpectedUtility(action);
            if (utility > maxUtility) {
                maxUtility = utility;
                bestAction = action;
            }
        }

        System.out.println("Automatic Action: " + bestAction);
        return bestAction;
    }

    // Compute expected utility of taking a given action
    double computeExpectedUtility(int action) {
        double expectedUtility = 0.0;
        double unintendedProb = (1.0 - moveProb) / 4.0;
        
        // Sum over all possible current positions weighted by probability
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (probs[x][y] <= 0.0 || !isValidPosition(x, y)) {
                    continue; // Only consider valid, possible positions
                }
                
                // Compute expected value over all possible outcomes for the position
                double positionExpectedValue = 0.0;
                
                int[] possibleOutcomes = {NORTH, SOUTH, EAST, WEST, STAY};
                for (int outcome : possibleOutcomes) {
                    // Determine probability of this outcome
                    double outcomeProb = (outcome == action) ? moveProb : unintendedProb;
                    
                    // Get destination for this outcome
                    int[] dest = getDestinationPosition(x, y, outcome);
                    int destX = dest[0];
                    int destY = dest[1];
                    
                    // If destination hits a wall, robot stays at current position
                    if (isWallAt(destX, destY)) {
                        destX = x;
                        destY = y;
                    }
                    
                    // Weight outcome value by outcome probability
                    positionExpectedValue += outcomeProb * Vs[destX][destY];
                }

                // Weight this position's expected value by belief probability
                expectedUtility += probs[x][y] * positionExpectedValue;
            }
        }

        return expectedUtility;
    }

    void doStuff() {
        System.out.println("Starting to do stuff");
        int action;
        
        valueIteration();  // Performs value iteration to compute the values of each state
        initializeProbabilities();  // Initializes the location (probability) map
        
        while (true) {
            try {
                if (isManual)
                    action = getHumanAction();  // get the action selected by the user
                else
                    action = automaticAction(false); // get the action selected by your AI
                
                sout.println(action); // send the action to the Server
                
                // get sonar readings after the robot moves
                String sonars = sin.readLine();
                System.out.println("Sonars: " + sonars);
            
                updateProbabilities(action, sonars);
                
                if (sonars.length() > 4) {  // check to see if the robot has reached its goal or fallen down stairs
                    if (sonars.charAt(4) == 'w') {
                        System.out.println("I won!");
                        myMaps.setWin();
                        break;
                    }
                    else if (sonars.charAt(4) == 'l') {
                        System.out.println("I lost!");
                        myMaps.setLoss();
                        break;
                    }
                }
                else {
                    // Update probabilities knowing the robot is NOT at a goal or stairwell
                    for (int y = 0; y < mundo.height; y++) {
                        for (int x = 0; x < mundo.width; x++) {
                            if (mundo.grid[x][y] == 2 || mundo.grid[x][y] == 3) {
                                probs[x][y] = 0.0;
                            }
                        }
                    }
                    
                    normalizeProbabilities(probs);
                    myMaps.updateProbs(probs);
                }
                Thread.sleep(decisionDelay);  // delay that is useful to see what is happening when the AI selects actions
                                              // decisionDelay is specified by the send command-line argument, which is given in milliseconds
            }
            catch (IOException e) {
                System.out.println(e);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // java theRobot [manual/automatic] [delay]
    public static void main(String[] args) {
        theRobot robot = new theRobot(args[0], Integer.parseInt(args[1]));  // starts up the robot
    }
}