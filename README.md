# Bayes Filter Robot with Adaptive Exploration

A Java implementation of an intelligent robot that combines **Bayes filter localization**, **value iteration planning**, and **adaptive exploration** to navigate uncertain grid world environments. The robot learns its location through probabilistic inference and uses smart exploration strategies to escape dead-ends and find optimal paths.

## Features

- **Bayes Filter Localization** - Maintains probability distribution over possible positions
- **Value Iteration (MDP)** - Computes optimal policy using dynamic programming
- **Adaptive Exploration** - Dynamically adjusts exploration based on confidence and stuck detection
- **Automatic Navigation** - Fully autonomous navigation with two strategy modes
- **Headless Mode** - Fast testing without GUI rendering
- **Comprehensive Testing Framework** - Automated comparison scripts with detailed reports

## Quick Start

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Bash shell (for testing scripts)

### Running the System

1. **Compile the code**:
   ```bash
   cd Server && javac BayesWorld.java World.java
   cd ../Robot && javac theRobot.java World.java
   ```

2. **Start the Server** (in one terminal):
   ```bash
   cd Server
   java BayesWorld <world_file> <move_prob> <sensor_accuracy> <initial_position>
   ```
   
   Example:
   ```bash
   java BayesWorld ../Mundos/mundo_maze2.txt 0.9 0.9 unknown
   ```

3. **Start the Robot** (in another terminal):
   ```bash
   cd Robot
   java theRobot <mode> <delay_ms> <use_exploration> <headless>
   ```
   
   Examples:
   ```bash
   # Adaptive Exploration with GUI (recommended for complex mazes)
   java theRobot automatic 500 true false
   
   # Best Utility Only (no exploration)
   java theRobot automatic 500 false false
   
   # Headless mode for fast testing
   java theRobot automatic 1 true true
   
   # Manual control
   java theRobot manual 250 true false
   ```

### Parameters Explained

**Server Parameters:**
- `world_file`: Path to grid world file (e.g., `../Mundos/mundo_maze2.txt`)
- `move_prob`: Probability of intended movement (0.0-1.0, typically 0.9)
- `sensor_accuracy`: Probability sensor is correct (0.0-1.0, typically 0.9)
- `initial_position`: `known` (specify x,y) or `unknown` (uniform prior)

**Robot Parameters:**
- `mode`: `automatic` (AI control) or `manual` (keyboard control)
- `delay_ms`: Milliseconds between actions (1 for testing, 250-500 for visualization)
- `use_exploration`: `true` (adaptive exploration) or `false` (best utility only)
- `headless`: `true` (no GUI, faster) or `false` (show visualization)

### Manual Control Keys
- `i` - Move North (up)
- `,` - Move South (down)  
- `j` - Move West (left)
- `l` - Move East (right)
- `k` - Stay in place

### Available Worlds
Located in `Mundos/` directory:
- `mundo_15_15.txt` - 15×15 simple open world
- `mundo_30_30.txt` - 30×30 larger world
- `mundo_maze.txt` - Complex maze environment
- `mundo_maze2.txt` - Alternative maze (recommended for testing)

### Running Tests

```bash
# Quick test (3 trials, ~2 minutes)
./quick_test.sh

# Full comparison (10 trials, ~5 minutes)  
./run_comparison.sh

# Comprehensive multi-world test (~30 minutes)
./test_comparison.sh
```

Test results are saved to `test_results/` directory with detailed reports.

## Implementation Overview

### System Architecture

**Client-Server Architecture:**

1. **BayesWorld Server** (`Server/BayesWorld.java`)
   - Maintains ground truth robot position
   - Simulates noisy sensors and uncertain movement
   - Provides sonar readings (4 directions: N, S, E, W)
   - Manages terminal states (goal/stairwell detection)
   - Communicates via socket on port 3333

2. **theRobot Client** (`Robot/theRobot.java`)
   - Implements complete autonomous navigation system:
     - **Bayes Filter**: Probabilistic localization
     - **Value Iteration**: MDP-based optimal policy computation
     - **Action Selection**: Adaptive exploration or best utility
   - Maintains belief state (probability distribution)
   - Provides real-time GUI visualization (optional)

### Grid World Environment

Grid cells encode different terrain types:
- `0` = Open space (navigable)
- `1` = Wall (obstacle, blocks movement)
- `2` = Stairwell (terminal state, lose condition)
- `3` = Goal (terminal state, win condition)

### Movement Model

**Stochastic Transitions:**
- Intended action succeeds with probability `moveProb` (typically 0.9)
- Unintended actions occur with probability `(1 - moveProb) / 4`
- Wall collisions result in staying in place
- All 5 actions possible: NORTH, SOUTH, EAST, WEST, STAY

## Core Algorithms

### 1. Bayes Filter Localization

**Three-step recursive estimation:**

#### Prediction (Motion Model)
```java
double[][] transitionModel(int action)
```
- Propagates belief through stochastic transition model
- For each source position, computes probability of reaching each destination
- Accounts for movement uncertainty: `P(s'|s,a) = moveProb` (intended) or `(1-moveProb)/4` (unintended)
- Handles wall collisions (robot stays at source if destination blocked)

#### Correction (Sensor Model)
```java
void sensorModel(double[][] predictionProbs, String sonars)
```
- Updates belief using Bayes rule: `P(s|z) ∝ P(z|s) * P(s)`
- Computes sensor likelihood for each position
- Sonar readings: 4-bit string encoding wall presence (N,S,E,W)
- Sensor likelihood: `P(z|s) = sensorAccuracy^correct * (1-sensorAccuracy)^incorrect`

#### Normalization
```java
void normalizeProbabilities(double[][] probArray)
```
- Ensures probability distribution sums to 1.0
- Maintains valid probability measure

**Mathematical Foundation:**

Prediction: `P(X_t | u_t) = Σ P(X_t | u_t, x_{t-1}) * P(x_{t-1})`

Correction: `P(X_t | z_t) ∝ P(z_t | X_t) * P(X_t | u_t)`

### 2. Value Iteration (MDP Planning)

**Bellman Optimality Equation:**

```java
void valueIteration()
```

Computes optimal value function `V*(s)` iteratively:

```
V(s) ← R(s) + γ * max_a Σ P(s'|s,a) * V(s')
```

**Implementation Details:**
- Reward structure:
  - Open spaces: -0.04 (living cost)
  - Goal: +1.0 (terminal)
  - Stairwell: -10.0 (terminal)
- Discount factor: γ = 0.99
- Convergence: ε = 0.001 (max value change)
- Considers all 5 actions (N, S, E, W, STAY)
- Accounts for stochastic transitions

**Output:** Value of each state representing expected cumulative reward

### 3. Adaptive Exploration Strategy

**Two Action Selection Modes:**

#### A. Best Utility Only (Pure Exploitation)
```java
int getBestUtilityAction()
```
- Selects action maximizing expected utility
- Computes: `EU(a) = Σ P(s) * Σ P(s'|s,a) * V(s')`
- Integrates belief state with value function
- No randomness - fully deterministic

#### B. Dynamic Exploration (Adaptive Epsilon-Greedy)
```java
int automaticAction(boolean useBestUtility)
```

**Adaptive exploration rate:**
```
ε = base_ε + (stuck_bonus) - (confidence_penalty)
```

**Components:**
1. **Base exploration**: 5% random actions
2. **Stuck detection**: +30% per consecutive STAY action
3. **Confidence reduction**: Up to -70% when belief confidence > 50%
4. **Maximum cap**: 60% exploration rate

**Benefits:**
- Escapes local optima and dead-ends
- Increases exploration when stuck
- Reduces exploration when localized
- Never explores with STAY (forces movement)

**Algorithm:**
```java
if (random() < adaptiveExplorationRate) {
    return randomAction(NORTH, SOUTH, EAST, WEST);
} else {
    return getBestUtilityAction();
}
```

## Experimental Results

### Test Methodology
- 10 trials per method per world
- Server configuration: 0.9 moveProb, 0.9 sensorAccuracy, unknown start
- Headless mode for fast execution
- Metrics: success rate, average steps, consistency

### Results: Simple World (mundo_15_15)

| Method | Success Rate | Avg Steps | Efficiency |
|--------|-------------|-----------|------------|
| **Best Utility** | 70% | 133.9 | Baseline |
| **Dynamic Exploration** | 50% | 40.2 | **-70%** ✓ |

**Conclusion:** Simple worlds favor exploitation - Best Utility is more reliable

### Results: Complex Maze (mundo_maze2)

| Method | Success Rate | Avg Steps | Consistency |
|--------|-------------|-----------|-------------|
| **Best Utility** | 60% | 395.2 | Poor (154-748) |
| **Dynamic Exploration** | **80%** ✓ | **163.1** ✓ | **Excellent** (109-256) ✓ |

**Conclusion:** Dynamic Exploration dominates in complex environments
- **+33% success rate** (80% vs 60%)
- **-58.7% fewer steps** (163 vs 395)
- **4× better consistency** (range 147 vs 594)

### Key Insights

**Why Dynamic Exploration Wins in Mazes:**
1. Escapes dead-ends through adaptive exploration
2. Increases exploration when stuck (detects consecutive STAY actions)
3. Reduces exploration when localized (confidence > 50%)
4. Finds optimal paths consistently

**Why Best Utility Struggles:**
- Gets trapped in local optima
- Can't escape dead-ends without exploration
- Takes 2.4× more steps wandering aimlessly
- High variance (some runs take 748 steps!)

## Code Architecture

**Core Classes:**
- `theRobot`: Main AI system integrating localization, planning, and control
- `mySmartMap`: GUI visualization of probability distribution and values
- `BayesWorld`: Server-side ground truth simulator
- `World`: Grid world data structure

**Key Methods:**
- `valueIteration()`: Computes optimal policy via dynamic programming
- `automaticAction()`: Action selection with exploration strategy
- `updateProbabilities()`: Bayes filter update (prediction + correction)
- `computeExpectedUtility()`: Integrates belief state with value function
- `transitionModel()`: Stochastic state transition
- `sensorModel()`: Sensor measurement update

**Helper Functions:**
- `isValidPosition()` / `isWallAt()`: Environment queries
- `getDestinationPosition()`: Movement calculations
- `calculateSensorLikelihood()`: Sensor probability
- `computeAdaptiveExplorationRate()`: Dynamic ε calculation
- `trackAction()`: Stuck detection

## Configuration Parameters

### Exploration Constants (in `theRobot.java`)

```java
public static final double BASE_EXPLORATION_EPSILON = 0.05;  // Base exploration rate
public static final double MAX_EXPLORATION_EPSILON = 0.6;     // Maximum when stuck
public static final double STUCK_INCREMENT = 0.3;             // Increase per STAY
public static final double CONFIDENCE_THRESHOLD = 0.5;        // High confidence level
```

### Value Iteration Constants

```java
public static final double REWARD_OPEN = -0.04;        // Living cost
public static final double REWARD_GOAL = 1.0;          // Goal reward
public static final double REWARD_STAIRWELL = -10.0;   // Stairwell penalty
public static final double GAMMA_FACTOR = 0.99;        // Discount factor
public static final double CONVERGENCE_EPSILON = 0.001; // VI convergence
```

### Typical Runtime Values
- **move_probability**: 0.8-0.95 (realistic robot uncertainty)
- **sensor_accuracy**: 0.8-0.95 (realistic sensor noise)
- **decision_delay**: 
  - 1ms for headless testing
  - 250-500ms for visualization
  - 100ms for demos

## Performance & Optimization

### Headless Mode
Running without GUI provides significant speedup:
- Enable with 4th parameter: `java theRobot automatic 1 true true`
- ~10× faster execution (no rendering overhead)
- Perfect for automated testing and benchmarking
- All algorithms run identically - only visualization disabled

### Value Iteration Convergence
- Typical convergence: 5-15 iterations for small worlds
- Larger worlds may require 20-50 iterations
- Convergence monitored via maximum value change (ε < 0.001)
- Full state value grid printed to console on completion

## Testing Framework

### Automated Test Scripts

**`quick_test.sh`** - Rapid testing (3 trials, ~2 min)
```bash
./quick_test.sh
```
Compares both methods on mundo_15_15 for quick validation.

**`run_comparison.sh`** - Full comparison (10 trials, ~5 min)
```bash
./run_comparison.sh
```
Currently configured for mundo_maze2. Generates detailed statistical report.

**`test_comparison.sh`** - Multi-world test (~30 min)
```bash
./test_comparison.sh
```
Tests all worlds with comprehensive analysis.

### Test Output

Results saved to `test_results/`:
- `comparison_report_<timestamp>.txt` - Full results with statistics
- Individual trial logs: `trial_<method>_<number>.log`
- Success rate, average steps, time, and consistency metrics
- Automatic winner determination

## Troubleshooting

**Server Connection Issues:**
- Start server before client
- Verify port 3333 is available
- Check both run on localhost (127.0.0.1)

**Robot Gets Stuck:**
- Enable dynamic exploration: `java theRobot automatic 500 true false`
- Check if value iteration converged properly
- Verify world has path to goal

**Performance Issues:**
- Use headless mode for testing: `true` as 4th parameter
- Reduce decision delay to 1ms
- Larger worlds (30×30) naturally slower

**Testing Problems:**
- Ensure scripts are executable: `chmod +x *.sh`
- Compile before testing: `javac *.java` in both directories
- Kill any stuck processes: `pkill -f "java BayesWorld"`

## Extending the System

### Custom Worlds
World file format:
```
<width>
<height>
<grid_row_0>
<grid_row_1>
...
```
Where each cell is: 0 (open), 1 (wall), 2 (stairwell), 3 (goal)

### Tuning Exploration

Adjust constants in `theRobot.java`:
- Increase `BASE_EXPLORATION_EPSILON` for more exploration
- Adjust `STUCK_INCREMENT` to control stuck response
- Modify `CONFIDENCE_THRESHOLD` to change localization behavior
- Change `MAX_EXPLORATION_EPSILON` to cap exploration rate

### Enhanced Features (Future Work)
- **Information Gain Exploration**: Explore high-uncertainty regions
- **Frontier-Based Navigation**: Explore boundaries of known/unknown
- **History Tracking**: Avoid revisiting dead-ends
- **Multi-Hypothesis Tracking**: Handle ambiguous situations
- **Range Sensors**: Extended sensor models beyond binary walls

## Documentation

- **README.md** (this file) - Complete system overview
- **MAZE_RESULTS.md** - Detailed mundo_maze2 test analysis
- **COMPARISON_REPORT.md** - mundo_15_15 comparison results
- **TESTING_GUIDE.md** - Quick reference for running tests

## Project Structure

```
bayes-filter-bot/
├── Server/
│   ├── BayesWorld.java    # Simulation server
│   └── World.java         # World data structure
├── Robot/
│   ├── theRobot.java      # Main AI implementation
│   └── World.java         # World data structure (client)
├── Mundos/
│   ├── mundo_15_15.txt    # Simple world
│   ├── mundo_30_30.txt    # Large world
│   ├── mundo_maze.txt     # Maze 1
│   └── mundo_maze2.txt    # Maze 2 (test world)
├── test_results/          # Test output directory
├── quick_test.sh          # Fast 3-trial test
├── run_comparison.sh      # 10-trial comparison
└── test_comparison.sh     # Multi-world test
```

## Key Contributions

This project demonstrates:
1. **Probabilistic Robotics** - Complete Bayes filter implementation for localization
2. **MDP Planning** - Value iteration with stochastic transitions
3. **Intelligent Exploration** - Adaptive epsilon-greedy with confidence and stuck detection
4. **Integration** - Combining localization, planning, and control in uncertain environments
5. **Empirical Validation** - Comprehensive testing showing 80% success in complex mazes

The adaptive exploration strategy is particularly novel, using:
- Dynamic exploration rate adjustment based on stuck-ness
- Confidence-based exploration reduction
- Integration with Bayes filter belief state

### Performance Highlights
- **80% success rate** in complex mazes (vs 60% baseline)
- **58.7% fewer steps** when successful
- **4× better consistency** than pure exploitation
- Handles unknown starting positions effectively

## License & Attribution

This implementation was developed for CS 470 (Artificial Intelligence) and demonstrates practical applications of probabilistic reasoning, MDPs, and intelligent exploration strategies in robotics.

**AI Assistance Disclosure:** Documentation generated in part by Claude Sonnet 4.5