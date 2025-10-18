# Bayes Filter Robot Localization

A Java implementation of a probabilistic robot localization system using Bayes filtering in a grid world environment. The robot uses noisy sensors and uncertain movement to maintain a probability distribution over its possible locations.

## Quick Start Guide

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Command line terminal

### Running the System

1. **Start the Server** (in one terminal):
   ```bash
   cd Server
   java BayesWorld <world_file> <move_probability> <sensor_accuracy> <initial_position>
   ```
   
   Example:
   ```bash
   java BayesWorld mundo_15_15.txt 0.9 0.9 unknown
   ```

2. **Start the Robot Client** (in another terminal):
   ```bash
   cd Robot
   java theRobot <mode> <decision_delay_ms>
   ```
   
   Examples:
   ```bash
   # Manual control (use i,j,k,l,comma keys for movement)
   java theRobot manual 250
   
   # Automatic control (requires MDP implementation)
   java theRobot automatic 250
   ```

### Controls (Manual Mode)
- `i` - Move North (up)
- `,` - Move South (down)  
- `j` - Move West (left)
- `l` - Move East (right)
- `k` - Stay in place

### World Files
Available worlds in the `Mundos/` directory:
- `mundo_15_15.txt` - 15x15 basic world
- `mundo_30_30.txt` - 30x30 larger world
- `mundo_maze.txt` - Maze environment
- `mundo_maze2.txt` - Alternative maze

## Implementation Overview

### System Architecture

The system consists of two main components:

1. **BayesWorld Server** (`Server/BayesWorld.java`)
   - Simulates the robot's actual position
   - Provides noisy sensor readings
   - Handles robot movement with uncertainty
   - Manages win/loss conditions

2. **theRobot Client** (`Robot/theRobot.java`)
   - Implements Bayes filter for localization
   - Maintains probability distribution over positions
   - Provides GUI visualization of beliefs
   - Supports both manual and automatic control

### Grid World Environment

The world is represented as a 2D grid where:
- `0` = Open space (traversable)
- `1` = Wall (obstacle)
- `2` = Stairwell (lose condition)
- `3` = Goal (win condition)

### Bayes Filter Implementation

The robot uses a three-step Bayes filter for localization:

#### 1. Prediction Step (Motion Model)
```java
private double[][] transitionModel(int action)
```
- Predicts next position based on intended action
- Accounts for movement uncertainty (robot may not move as intended)
- Handles wall collisions (robot stays in place when hitting walls)
- Uses `moveProb` parameter for intended movement probability

#### 2. Correction Step (Sensor Model)  
```java
private void sensorModel(double[][] predictionProbs, String sonars)
```
- Updates beliefs based on sensor readings
- Sonar sensors detect walls in 4 directions (N, S, E, W)
- Uses `sensorAccuracy` parameter for sensor reliability
- Applies Bayes rule to combine prediction with sensor evidence

#### 3. Normalization
```java
private void normalizeProbabilities(double[][] probArray)
```
- Ensures probability distribution sums to 1.0
- Maintains valid probability distribution

### Key Features

**Probabilistic Localization**: Maintains full probability distribution over robot's possible locations rather than single point estimate.

**Uncertainty Handling**: 
- Movement uncertainty: Robot may not move in intended direction
- Sensor noise: Sonar readings may be incorrect
- Unknown initial position: Can start with uniform prior

**Real-time Visualization**: GUI shows probability heat map with darker colors indicating higher probability areas.

**Robust Implementation**: 
- Handles edge cases (wall collisions, boundary conditions)
- Modular design with helper functions
- Comprehensive error handling

### Mathematical Foundation

The Bayes filter implements the recursive Bayesian estimation:

**Prediction**:
$$P(X_t | u_t, X_{t-1}) = \sum_{x_{t-1}} P(X_t | u_t, x_{t-1}) \cdot P(x_{t-1})$$

**Correction**:
$$P(X_t | z_t) \propto P(z_t | X_t) \cdot P(X_t | u_t)$$

Where:
- $X_t$ = robot position at time t
- $u_t$ = action/control input
- $z_t$ = sensor observation
- $P(X_t | u_t, x_{t-1})$ = motion model
- $P(z_t | X_t)$ = sensor model

### Code Architecture

**Core Classes**:
- `theRobot`: Main robot AI with Bayes filter implementation
- `mySmartMap`: GUI component for probability visualization
- `BayesWorld`: Server simulation environment
- `World`: Grid world representation

**Key Methods**:
- `updateProbabilities()`: Main Bayes filter update
- `transitionModel()`: Motion model implementation  
- `sensorModel()`: Sensor model implementation
- `calculateSensorLikelihood()`: Sensor probability calculation

### Helper Functions

The implementation includes numerous helper functions for maintainability:
- `isValidPosition()`: Position validation
- `isWallAt()`: Wall detection
- `getSourcePosition()` / `getDestinationPosition()`: Movement calculations
- `addTransitionProbability()`: Probability accumulation
- `normalizeProbabilities()`: Distribution normalization

## Parameters

### Command Line Arguments

**Server Parameters**:
- `world_file`: Grid world layout file
- `move_probability`: Probability robot moves in intended direction (0.0-1.0)
- `sensor_accuracy`: Probability sensor reading is correct (0.0-1.0)  
- `initial_position`: "known" or "unknown" starting position

**Client Parameters**:
- `mode`: "manual" or "automatic" control
- `decision_delay_ms`: Milliseconds between actions in automatic mode

### Typical Values
- `move_probability`: 0.8-0.95 (realistic robot movement)
- `sensor_accuracy`: 0.8-0.95 (realistic sensor noise)
- `decision_delay`: 100-500ms (visualization speed)

## Troubleshooting

**Connection Issues**:
- Ensure server is started before client
- Check that port 3333 is available
- Verify both programs run on localhost

**Performance Issues**:
- Larger worlds (30x30) may run slower
- Reduce decision delay for faster execution
- GUI updates can be computationally expensive

**Visualization Problems**:
- Probability colors may be subtle with low probabilities
- Robot location becomes more certain over time
- Restarting provides fresh uniform distribution

## Extending the System

### Adding MDP Planning (Future Work)
The system is designed to support Markov Decision Process planning:
- Implement `valueIteration()` method
- Complete `automaticAction()` for optimal policy
- Add value function visualization

### Custom Worlds
Create new world files following the format:
```
width
height
grid_data_row_by_row
```

### Enhanced Sensors
The sensor model can be extended for:
- Multiple sensor types
- Directional sensors with different accuracies
- Range sensors instead of binary wall detection

This implementation provides a solid foundation for understanding probabilistic robotics, Bayes filtering, and robot localization in uncertain environments.

## AI Disclosure

Parts of this README were generated with assistance from the AI model Claude Sonnet 4. 