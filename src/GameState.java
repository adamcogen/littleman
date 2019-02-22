import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;

/**
 * The GameState class for Littleman, which handles distribution and management of all 
 * information to other classes necessary for the game to run. All information 
 * comes through the GameState class, and is either used there or distributed to 
 * somewhere else. All other classes are instantiated here. This class also 
 * manages the game's state, including gravity, movement,  etc. 
 * 
 * changelog:
 * 1.1: added in-map warps
 * 1.2: acceleration due to gravity, more neatly organized classes, 
 * 		new climb type (jumpable climb), improved collision
 * 		and climbability checking system, implemented warps to maps of 
 *		different dimensions
 * 1.3  improved key press processing (multiple directions at once,
 * 		movement speed no longer dependent on computer's key repeat 
 * 		delay), improved in-map warp file format (map number 'n' now 
 *      specifies that warp destination is same as current map)
 *
 * @version Version 1.3
 * @author Adam Cogen
 *
 */
public class GameState implements Observer {
	private int map = 18; //current map number
	private MapFileReader mapFileData; //the class that will read data stored in map files
	private Map sMap; //the class that will store map data 
	private GameWindow gamePanel; //the class that will display the game and sense key presses
	private Player player; //the class that will store information about the player / sprite
	private int jumpStep; //keeps track of which step of the jump animation the player is on during jumps
	private Timer jumpTimer; //the timer which will start during jumps, starting the jump animation 
	private Timer fallTimer; //the timer that will start while falling short distances. at a certain speed, a the fast fall timer takes this timer's place
	private Timer fastFallTimer; //the fast fall timer, which handles falling over longer distances and at higher speed (with acceleration)
	boolean fastFalling = false; //is the fast fall timer running? 
	private static final int PARTIAL_JUMP_HEIGHT = 1; // one third of the jump height, used to increment player Y on each step of jumpTimer, which has three steps.
	private static final int PARTIAL_MOVE_SIZE = 1; //one third of total move size
	private static final int MOVE_SIZE = 3 * PARTIAL_MOVE_SIZE; //used to check collision before moving to a new location that is distance moveSize away
	private static final int LEFT_EDGE_WARP_OFFSET = 15; //used to calibrate the position of edge warp on the left side of the map
	private static final int RIGHT_EDGE_WARP_OFFSET = 5; //used to calibrate the position of edge warp on the right side of the map
	private static final int TOP_EDGE_WARP_OFFSET = 2; //used to calibrate the position of edge warp at the top of the map
	private static final int BOTTOM_EDGE_WARP_OFFSET = 22; //used to calibrate the position of edge warp at the bottom of the map
	private static final int LEFT_COLLISION_OFFSET = 1; //the difference between the player's x position and its left side, for collision purposes etc.
	private static final int RIGHT_COLLISION_OFFSET = 8; //the difference between the player's x position and its right side, for collision purposes etc.
	private static final int TOP_COLLISION_OFFSET = -22; //the difference between the player's y position and its top edge, for collision purposes etc.
	private static final int DOWN_COLLISION_OFFSET = -1; //the difference between the player's y position and its bottom edge, for collision purposes etc.
	private boolean moving = false; //is the player moving? true when arrow key(s) are being pressed
	private Timer moveTimer; //timer to move the player while arrow key(s) being pressed
	private int[] pressedKeys; //an array that holds data about which arrow keys are currently being pressed
	private boolean initLeft = false; //when left arrow is pressed, character moves left immediately one time without starting moveTimer, to prevent lag. if that move has already happened, this will be false. if it needs to happen still, this boolean will be true.
	private boolean initUp = false; //when up arrow is pressed, character moves up immediately one time without starting moveTimer, to prevent lag. if that move has already happened, this will be false. if it needs to happen still, this boolean will be true.
	private boolean initRight = false; //when right arrow is pressed, character moves right immediately one time without starting moveTimer, to prevent lag. if that move has already happened, this will be false. if it needs to happen still, this boolean will be true.
	private boolean initDown = false; //when down arrow is pressed, character moves down immediately one time without starting moveTimer, to prevent lag. if that move has already happened, this will be false. if it needs to happen still, this boolean will be true.
	private static final int MOVE_TIMER_FREQUENCY = 100; //how often does the moveTimer tick? determines how quickly the character will move when holding down an arrow key
	/*
	 * 
	 * there are two acceleration-due-to-gravity timers:
	 * fallTimer, and fastFallTimer. 
	 * this is necessary because fallTimer maintains the appearance of the old 
	 * fall animation, while fastFallTimer allows for better acceleration due 
	 * to gravity.
	 * after a certain amount of time falling, fallTimer turns off and is
	 * replaced by fastFallTimer, to allow for better looking acceleration.
	 * this implementation is way over complicated; for a much more elegant
	 * and more effective implementation, see the Bouncing Ball project. 
	 * 
	 * some more complicated notes on how acceleration-due-to-gravity works:
	 * ~   every fastFallTimer clock tick, use a while loop to move down 1 pixel 
	 *     at a time, (fastGravityStart / FAST_GRAVITY_DIVIDER) times.
	 * ~   every iteration through the while loop, fastGravityStart increments
	 *     by FAST_GRAVITY_ACCELERATION, so that the value of 
	 *     (fastGravityStart / FAST_GRAVITY_DIVIDER) becomes larger every clock tick. 
	 *     this means that for every clock tick, the player moves down 1 pixel at
	 *     time, a greater number of times (the while loops runs more times).
	 * ~   fastGravityStart will stop incrementing once 
	 *     (fastGravityStart / GRAVITY_DIVIDER) equals TERMINAL_VELOCITY.
	 * ~   every time the player hits water, climbable block, or solid ground, 
	 *     the variable fastGravityStart resets back to FAST_GRAVITY_INITIAL_SPEED
	 *     because their fall is broken.
	 *     
	 * ~   Some default gravity values: 
	 *     FAST_FALL_TIMER_FREQUENCY = 30,
	 *     FAST_GRAVITY_INITIAL_SPEED = 9, 
	 *     FAST_GRAVITY_DIVIDER = 6,
	 *     FAST_GRAVITY_ACCELERATION = .2,
	 *     terminal velocity = 8,
	 *     USE_GRAVITY_ACCELERATION = true
	 *     IN_MAP_WARP_RESETS_FALL_SPEED = false, 
	 *     EDGE_WARP_RESETS_FALL_SPEED = false.
	 *  
	 */

	//normal fall timer stuff: you probably shouldn't change these values.
	//if you want to experiment with acceleration due to gravity,
	//see the fastFallTimer variables.
	private static final double GRAVITY_INITIAL_SPEED = 4; //real initial speed is (int)(GRAVITY_INITIAL_SPEED / GRAVITY_DIVIDER)
	private double gravityStart = GRAVITY_INITIAL_SPEED; //gravityStart will be incremented to increase fall speed over time
	private static final double GRAVITY_DIVIDER = 2;  //actual initial speed will be (int)(GRAVITY_INITIAL_SPEED / GRAVITY_DIVIDER)
	private static final double GRAVITY_ACCELERATION = .2; //gravityStart increments by this much with every clock tick, speeding up fall over time
	private static final int FALL_TIMER_FREQUENCY = 120; //frequency of fallTimer in milliseconds
	private static final int JUMP_TIMER_FREQUENCY = 120; //frequency of jumpTimer in milliseconds

	//some general gravity settings:
	private static final boolean USE_GRAVITY_ACCELERATION = true; //should we use acceleration due to gravity?
	private static final boolean IN_MAP_WARP_RESETS_FALL_SPEED = false; //does an in-map warp reset fall speed? 
	private static final boolean EDGE_WARP_RESETS_FALL_SPEED = false; //does an edge warp reset fall speed?

	//fastFallTimer stuff: if you want to experiment with acceleration due to gravity,
	//change these values.
	private static final int FAST_FALL_TIMER_FREQUENCY = 30; //frequency of the fastFallTimer, for faster fall speeds
	private static final double FAST_GRAVITY_INITIAL_SPEED = 9; //real intitial speed is (int)(FAST_GRAVITY_INITIAL_SPEED / FAST_GRAVITY_DIVIDER)
	private double fastGravityStart = FAST_GRAVITY_INITIAL_SPEED; //fastGravityStart will be incremented to increase fall speed over time
	private static final double FAST_GRAVITY_DIVIDER = 6;  //real intitial speed is (int)(FAST_GRAVITY_INITIAL_SPEED / FAST_GRAVITY_DIVIDER)
	private static final double FAST_GRAVITY_ACCELERATION = .2; //fastGravityStart increments by this much with every clock tick, speeding upfall over time
	private static final int TERMINAL_VELOCITY = 15; //terminal velocity (in pixels-per-fastFallTimer-clock-tick)


	/**
	 * The Game class consolidates all information 
	 * from other classes and keeps track of everything.
	 * It is basically the main control center of the game. 
	 */
	public GameState(int mapNumber) {
		//map = mapNumber; //comment this to use map number from fields, uncomment to use map number from main method

		jumpStep = 3;
		gamePanel = new GameWindow(this);
		player = new Player();
		changeMap(map); //initializes necessary classes and information to load a map, put into a method for use with both constructor and warps
		setCharX(mapFileData.getSpawnX());
		setCharY(mapFileData.getSpawnY());

		pressedKeys = new int[5];

		jumpTimer = getJumpTimer();
		fastFallTimer = getFastFallTimer();
		fallTimer = getFallTimer();

		checkFall();

		moveTimer = getMoveTimer();

	}

	private Timer getFallTimer() {

		/**
		 * Timer listener is involved with the 'down' direction of move().
		 * Handles short jumps, such as jumping while on solid ground.
		 * Once a fall accelerates to a certain speed, this timer stops
		 * and the fastFallTimer starts. 
		 * 
		 * @author Adam Cogen
		 *
		 */
		class TimerListenerDown implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * If the player is not on the ground, move down. Try this twice. 
				 * Repeating this step is better than moving down all at once because 
				 * it checks the ground below the player more often, preventing the 
				 * player from falling too far through the ground. 
				 */
				for (int i = 0; i < ((int) (gravityStart / GRAVITY_DIVIDER)); i++){
					if(!isOnGround(player.getX(), player.getY()) && checkClimb() == 0 && !fastFalling){
						setCharY(player.getY() + PARTIAL_JUMP_HEIGHT);
						if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){
							//if the player is off the edge of the map, edgeWarp down.
							edgeWarp('d');
						}

						if(USE_GRAVITY_ACCELERATION) {
							/*
							 * If the player has not reached terminal velocity, increase the fall speed
							 * by the gravity acceleration constant.
							 */
							if (gravityStart <= TERMINAL_VELOCITY * GRAVITY_DIVIDER){
								gravityStart += GRAVITY_ACCELERATION;
							}
							/*
							 * if the fall speed is greater than 3 pixels per clock tick, stop the fallTimer
							 * and start the fastFallTimer.
							 */
							if (gravityStart / GRAVITY_DIVIDER >= 3){
								fastFalling = true;
								fastFallTimer.start();
								fallTimer.stop();
							}
						}
					}
				}
				/*
				 * Perform the same step one more time. If the player was in "water" or on 
				 * something climbable this whole time, none of these steps were
				 * performed, but this is addressed next.
				 */
				if (!isOnGround(player.getX(), player.getY()) && checkClimb() == 0 && !fastFalling){
					setCharY(player.getY() + PARTIAL_JUMP_HEIGHT);
					if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){
						edgeWarp('d');
					}
				} else if (!isOnGround(player.getX(), player.getY()) && checkClimb() == 2){
					/*
					 * If the player is in water, move it down one increment here.
					 * This way, in water, the player moves down one increment 
					 * per timer tick, rather than 3 increments if it was falling 
					 * in air. 
					 */
					setCharY(player.getY() + PARTIAL_JUMP_HEIGHT);
					if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){
						edgeWarp('d');
					}
					resetFallSpeed();

				} else if (fastFalling){
					/*
					 * If for some reason the fastFall timer has started but the fallTimer
					 * is still running (this is possible by jumping in rapid succession),
					 * the fallTimer will still be stopped here, so nothing will go wrong.
					 */
					fallTimer.stop();
				} else {
					/*
					 * If none of the previous cases are true, the fall timer should 
					 * stop, as the player has landed on solid ground (or on something
					 * climbable).
					 */
					fallTimer.stop();
					fastFallTimer.stop();
					setStep(0);
					resetFallSpeed();
					checkClimb();
					checkFall();
				}
				refreshChar();
				gamePanel.refreshImage();
			}
		}

		return new Timer(FALL_TIMER_FREQUENCY, new TimerListenerDown());

	}

	private Timer getFastFallTimer() {
		/**
		 * Timer that during a fall after a certain velocity is reached.
		 * Handles fast falling and has a higher rate of occurrence so that
		 * the fall movement appears smoother.
		 * 
		 * @author adamcogen
		 *
		 */
		class TimerListenerDownFast implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * If the player is not on the ground, move down. Try this twice. 
				 * Repeating this step is better than moving down all at once because 
				 * it checks the ground below the player more often, preventing the 
				 * player from falling too far into the ground. 
				 */
				for (int i = 0; i < ((int) (fastGravityStart / FAST_GRAVITY_DIVIDER)); i++){
					if(!isOnGround(player.getX(), player.getY()) && checkClimb() == 0){
						setCharY(player.getY() + PARTIAL_JUMP_HEIGHT);
						if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){
							//if the player is off the edge of the map, edgeWarp down.
							edgeWarp('d');
						}

						/*
						 * If the player has not reached terminal velocity, increase the fall speed.
						 */
						if (fastGravityStart <= TERMINAL_VELOCITY * FAST_GRAVITY_DIVIDER){
							fastGravityStart += FAST_GRAVITY_ACCELERATION;
						}
					}
				}
				/*
				 * Perform the same step one more time. If the player was in "water" or on 
				 * something climbable this whole time, none of these steps were
				 * performed, but this is addressed next.
				 */
				if (!isOnGround(player.getX(), player.getY()) && checkClimb() == 0){
					setCharY(player.getY() + PARTIAL_JUMP_HEIGHT);
					if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){
						edgeWarp('d');
					}
				}
				/*
				 * if the fastFallTimer gets to the else statement, it just means player isn't freefalling anymore,
				 * so stop the timer.
				 */
				else {
					fallTimer.stop();
					fastFallTimer.stop();
					setStep(0);
					resetFallSpeed();
					checkClimb();
					checkFall();
				}
				refreshChar();
				gamePanel.refreshImage();
			}
		}
		return new Timer(FAST_FALL_TIMER_FREQUENCY, new TimerListenerDownFast());
	}

	private Timer getJumpTimer() {
		/**
		 * Timer listener is involved with the 'up' direction of moveChar.
		 * Reads and increments the jumpStep variable, which keeps track of 
		 * which step of the jump (step 0, step 1, step 2, etc.) we are on.
		 * stops the jump at step 4 (there are only 3 steps). moves char's
		 * Y position in a different way depending on which step we are one,
		 * then repaints the panel after each step is complete.
		 * @author adamcogen
		 *
		 */
		class TimerListenerUp implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * since the first two steps of the jump are identical,
				 * if we just used the jumpStep variable in the switch
				 * statement, we would need to rewrite the exact same
				 * code twice for case 0 and case 1. the switchJumpStep
				 * variable's value is based upon the jumpStep value,
				 * but it is logically processed so that the same code
				 * can be used for both jumpStep == 0 and jumpStep == 1.
				 */
				int switchJumpStep = 2;
				if(jumpStep == 0 || jumpStep == 1){
					switchJumpStep = 0;
				} else if (jumpStep == 2){
					switchJumpStep = 1;
				} else if (jumpStep == 3){
					switchJumpStep = 2;
				}
				switch(switchJumpStep){
				case 0:
					if (checkCollision('d', player.getX(), player.getY() - MOVE_SIZE)){
						setCharY(player.getY() - 3);
						if (player.getY() <= 0 - TOP_EDGE_WARP_OFFSET){
							edgeWarp('u');
						}
					}
					break;
				case 1:
					jumpTimer.stop();
					setStep(0);
					checkFall();
					break;
				}
				jumpStep++;
				refreshChar();
				gamePanel.refreshImage();
			}
		}
		return new Timer(JUMP_TIMER_FREQUENCY, new TimerListenerUp());
	}

	private Timer getMoveTimer() {
		/**
		 * 
		 * MoveTimerListener is the ActionListener for the moveTimer, which calls
		 * the move(char direction) method, causing the character to move. 
		 * Different combinations of keys being held down cause different movements.
		 * Necessary movements will happen whenever the timer ticks, so increasing
		 * the frequency of timer ticks will make the player move faster. 
		 * 
		 * @author Adam Cogen
		 *
		 */
		class MoveTimerListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean move1 = false; //did the first move happen?
				boolean move2 = false; //did the second move happen?

				if(pressedKeys[0] == 1){ //left cases
					if(pressedKeys[1] == 1){ //left and up
						move1 = move('u');
						move2 = move('l');
						if((move1 && move2 && (checkClimb() == 1 || checkClimb() == 3))){ //if both moves didn't happen, no need to increment step
							player.incStep();	
						}
					} else if (pressedKeys[3] == 1){ //left and down
						move1 = move('d');
						move2 = move('l');
						if((move1 && move2 && (checkClimb() == 1 || checkClimb() == 2 || checkClimb() == 3))){
							player.incStep();	
						}
					}  else { //left only 
						move('l');
					}
				} else if (pressedKeys[2] == 1){ //right cases
					if(pressedKeys[1] == 1){ //right and up
						move1 = move('u');
						move2 = move('r');
						if((move1 && move2 && (checkClimb() == 1 || checkClimb() == 3))){
							player.incStep();	
						}
					} else if (pressedKeys[3] == 1){ //right and down
						move1 = move('d');
						move2 = move('r');
						if((move1 && move2 && (checkClimb() == 1 ||checkClimb() == 2 || checkClimb() == 3))){
							player.incStep();	
						}
					} else { //right only
						move('r');
					}
				} else if (pressedKeys[1] == 1){ //up only
					move('u');
				} else if (pressedKeys[3] == 1){ //down only
					move('d');
				}
			}
		}
		return new Timer(MOVE_TIMER_FREQUENCY, new MoveTimerListener());
	}

	/**
	 * Check if the player is on the ground or on something climbable.
	 * If not, start the fall timer, unless it or the fastFallTimer is already
	 * running.
	 */
	public void checkFall(){
		int currentX = player.getX();
		int currentY = player.getY();
		if(!isOnGround(currentX, currentY) && checkClimb() != 1 && checkClimb() != 3){
			if(!fastFalling){
				fallTimer.start();
			}
		}
	}

	/**
	 * Move the player in a specified direction if 
	 * that movement is possible. This is called
	 * after an arrow key is pressed. Different 
	 * directions have different implications 
	 * (up can start the jumpTimer, start "climbing,"
	 * or start "swimming," depending on the climbability
	 * of the current location).
	 * @param dir A character specifying the direction to move.
	 * 'l' = left, 'r' = right, 'u' = up, 'd' = down.
	 * @return true if the move happened, false otherwise
	 */
	public boolean move(char dir){

		boolean moved = false; //this will be used to make it so that the legs only move when the player actually moves

		if (dir == 'r'){ //if the right arrow was pressed
			/*
			 * each direction's movements are 3 pixels, and operate on a for loop, which moves
			 * one pixel at a time. this allows for more accurate collision detection, 
			 * allowing the player to move only 1 or 2 pixels if a wall is 1 or 2 pixels 
			 * away. this prevents strange looking gaps between the player and the wall.
			 */
			for(int i = 0; i < 3; i++){
				if(checkCollision('r', player.getX() + PARTIAL_MOVE_SIZE, player.getY())){
					moved = true; 
					setCharX(player.getX() + PARTIAL_MOVE_SIZE);
				}
				//check if the player is off the map and needs to be edge-warped
				if (player.getX() >= gamePanel.getFrameWidth() + RIGHT_EDGE_WARP_OFFSET){
					edgeWarp(dir);
				}
			}

		} else if (dir == 'l'){ //if the left arrow was pressed
			//see comment in (dir == 'r') for explanation about for loop
			for(int i = 0; i < 3; i++){
				if(checkCollision('l', player.getX() - PARTIAL_MOVE_SIZE, player.getY())){
					setCharX(player.getX() - PARTIAL_MOVE_SIZE);
					moved = true; 
				}
				//check if the player is off the map and needs to be edge-warped
				if (player.getX() <= 0 - LEFT_EDGE_WARP_OFFSET){
					edgeWarp(dir);
				}
			}
		} else if (dir == 'u'){ //if the up arrow was pressed
			/*
			 * up option 1: you are on the ground, in water, or on jumpable climb. 
			 * there is room to jump without hitting something. start the jump timer,
			 * which will allow you to jump.
			 */
			if(( (isOnGround(player.getX(), player.getY()) && checkClimb() == 0) || checkClimb() == 2 || checkClimb() == 3) && checkCollision('u', player.getX(), player.getY() - (3 *PARTIAL_JUMP_HEIGHT)) && jumpStep == 3){
				//System.out.println("ya");
				moved = true;
				fallTimer.stop();
				setCharY(player.getY() - (3 * PARTIAL_JUMP_HEIGHT));
				setStep(0);
				jumpStep = 0;
				jumpTimer.start();
			} 
			/*
			 * up option 2: you are not necessarily on the ground, but there is room 
			 * to jump without hitting something, and  you can climb. Move the char
			 * up by the move size, similar to how you would move left or right,
			 * without starting the jumpTimer.
			 */
			else if((checkClimb() == 1)){
				for(int i = 0; i < 3; i++){
					if (checkCollision('u', player.getX(), player.getY() - PARTIAL_JUMP_HEIGHT)){
						moved = true;
						setCharY(player.getY() - PARTIAL_JUMP_HEIGHT); //non-stylized jump
						//check if the player is off the map and needs to be edge-warped
						if (player.getY() <= 0 - TOP_EDGE_WARP_OFFSET){
							edgeWarp(dir);
						}
					}
				}
			} 
		} else if (dir == 'd'){ //if the down arrow was pressed
			/*
			 * If there is room to move down without colliding with something,
			 * (this happens in the air, in water, or in something climbable)
			 * then move the character down.
			 */
			for(int i = 0; i < 3; i++){
				if((checkCollision('d', player.getX(), player.getY() + PARTIAL_MOVE_SIZE) && jumpStep == 3)){
					moved = true;
					setCharY(player.getY() + PARTIAL_MOVE_SIZE);
					//check if the player is off the map and needs to be edge-warped
					if (player.getY() >= gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET){ 
						edgeWarp(dir);
					}
				}
			}
		} else if (dir == 's'){ //if right shift key was pressed
			//do nothing. this can be implemented with various debug functions if necessary
		}
		if (moved){
			player.incStep();
		}
		refreshChar();
		gamePanel.refreshImage();
		checkClimb();

		//check if the character is on stable ground after moving
		if (jumpStep == 3){
			/*
			 * note: jumpStep will only equal 3 when the jumpTimer is not currently running.
			 * this prevents the fallTimer from starting in checkFall() while the player is
			 * still on the way up in a jump. 
			 */
			checkFall();
		}
		return moved;
	}

	/**
	 * Return the total number of arrow keys currently being held down
	 */
	public int keyArrayTotal(){
		int total = 0;
		for( int value : pressedKeys){
			total += value;
		}
		return total;
	}

	/**
	 * This observer is notified by the GamePanel whenever an
	 * arrow key is pressed or released. The moveTimer is then 
	 * started or stopped based on which arrow key was pressed
	 * or released.
	 * Also note that after an arrow key is pressed, this method
	 * will call the move(char direction) method itself (as 
	 * opposed to leaving all calls to the moveTimer). This is
	 * because otherwise the first movement will only happen
	 * if it is held down long enough for a timer tick to happen.
	 * The move() method is called once by this method for each
	 * arrow key press. This resets every time that arrow key is
	 * released. 
	 */
	@Override
	public void update(Observable o, Object arg) {

		if ((char) arg == 'l'){ //left arrow was pressed
			if(pressedKeys[2] == 0){ //right not being pressed
				if(!initLeft){ //if the initial left movement hasn't happened, do it
					move('l');
					initLeft = true; //the initial left movement has now happened.
				}
				pressedKeys[0] = 1;
				//moveExecuted = false;
				if(moving == false){
					moveTimer.start();
					moving = true;
				}
			}
		} else if ((char) arg == 'r'){ //right arrow was pressed
			if(pressedKeys[0] == 0){ //left not being pressed
				if(!initRight){ //if the initial right movement hasn't happened, do it
					move('r');
					initRight = true; //the initial right movement has now happened.
				}
				pressedKeys[2] = 1;
				//moveExecuted = false;
				if(moving == false){
					moveTimer.start();
					moving = true;
				}
			}
		} else if ((char) arg == 'u'){ //up arrow was pressed
			if(pressedKeys[3] == 0){ //down not being pressed
				if(!initUp){ //if the initial up movement hasn't happened, do it
					move('u');
					initUp = true; //the initial up movement has now happened.
				}
				pressedKeys[1] = 1;
				//moveExecuted = false;
				if(moving == false){
					moveTimer.start();
					moving = true;
				}
			}
		} else if ((char) arg == 'd'){ //down arrow was pressed
			if(pressedKeys[1] == 0){ //up not being pressed
				if(!initDown){ //if the initial down movement hasn't happened, do it
					move('d');
					initDown = true; //the initial down movement has now happened.
				}
				pressedKeys[3] = 1;
				//moveExecuted = false;
				if(moving == false){
					moveTimer.start();
					moving = true;
				}
			}
		} else if ((char) arg == 's'){ //shift key was pressed
			move('s'); //this can by implemented for various debug functions if needed
		} else if ((char) arg == 'n'){ //key was released, NOT CURRENTLY IMPLEMENTED
			//no use for this yet
		} else if ((char) arg == '0'){ //left arrow released
			//if(moveExecuted == true){
			pressedKeys[0] = 0;
			initLeft = false; //the initial left movement is reset. next time left is pressed, the initial movement will happen.
			if(keyArrayTotal() == 0){
				moveTimer.stop();
				moving = false;
			}
			//}
		} else if ((char) arg == '1'){ //up arrow released
			//if(moveExecuted == true){
			pressedKeys[1] = 0;
			initUp = false; //the initial up movement is reset. next time up is pressed, the initial movement will happen.
			if(keyArrayTotal() == 0){
				moveTimer.stop();
				moving = false;
			}
			//}
		} else if ((char) arg == '2'){ //right arrow released
			//if(moveExecuted == true){
			pressedKeys[2] = 0;
			initRight = false; //the initial right movement is reset. next time right is pressed, the initial movement will happen.
			if(keyArrayTotal() == 0){
				moveTimer.stop();
				moving = false;
			}
			//}
		} else if ((char) arg == '3'){ //down arrow released
			//if(moveExecuted == true){
			pressedKeys[3] = 0;
			initDown = false; //the initial down movement is reset. next time down is pressed, the initial movement will happen.
			if(keyArrayTotal() == 0){
				moveTimer.stop();
				moving = false;
			}
			//}
		} else if ((char) arg == '4'){ //shift key released, NOT CURRENTLY IMPLEMENTED

		}
	}

	/**
	 * Consolidate all char data; update the x, y, and step fields within the sPanel.
	 * This is called whenever a change is made to the player's x position or y position
	 * ( it is called within setCharX(), setCharY() ) so that other classes that rely on 
	 * this information (sPanel) will know that it has changed. 
	 */
	public void refreshChar(){
		gamePanel.setStep(player.getStep());
		gamePanel.setCharX(player.getX());
		gamePanel.setCharY(player.getY());
	}

	/**
	 * This changes the current map to a specified map,
	 * and initializes all necessary classes and fields.
	 * This is called for in-map-warps, edge-warps,
	 * and within the constructor (in this case, 
	 * the initial game map is the parameter). 
	 * @param newMap the map to change to
	 */
	public void changeMap(int newMap){
		mapFileData = new MapFileReader(newMap);
		sMap = new Map(newMap);

		//initialize sMap with sFile data
		//shape data
		sMap.setShapeData(mapFileData.getRectangleData());
		sMap.setShapeCount(mapFileData.getShapeCount());
		//edgewarps
		sMap.setEdgeWarpDown(mapFileData.getEdgeWarpDown());
		sMap.setEdgeWarpLeft(mapFileData.getEdgeWarpLeft());
		sMap.setEdgeWarpRight(mapFileData.getEdgeWarpRight());
		sMap.setEdgeWarpUp(mapFileData.getEdgeWarpUp());
		//normWarps
		sMap.setWarpCount(mapFileData.getWarpCount());
		sMap.setWarpList(mapFileData.getWarpList());

		//initialize GamePanel with information from MapFileData
		//shape data
		gamePanel.setShapeData(mapFileData.getRectangleData());
		gamePanel.setShapeCount(mapFileData.getShapeCount());
		//game size
		gamePanel.setFrameHeight(mapFileData.getFrameHeight());
		gamePanel.setFrameWidth(mapFileData.getFrameWidth());
		//player animation current step
		gamePanel.setStep(player.getStep());
		//initialize and refresh panel
		refreshChar();
		gamePanel.refreshImage();
		gamePanel.refreshSize();

	}

	/**
	 * perform an edge warp in the specified direction.
	 * @param direction char representing the direction 
	 * 		  to perform the edge warp in. 
	 * 		  'l' is left, 'r' is right, 'u' is up, 'd' 
	 * 		  is down.
	 */
	public void edgeWarp(char direction){
		if (direction == 'l'){ //left
			map = sMap.getEdgeWarpLeft();
			changeMap(map);
			setCharX(gamePanel.getFrameWidth() + RIGHT_EDGE_WARP_OFFSET);
		} else if (direction == 'r'){ //right
			map = sMap.getEdgeWarpRight();
			changeMap(map);
			setCharX(0 - LEFT_EDGE_WARP_OFFSET);
		} else if (direction == 'u'){ //up
			map = sMap.getEdgeWarpUp();
			changeMap(map);
			setCharY(gamePanel.getFrameHeight() + BOTTOM_EDGE_WARP_OFFSET);
		} else if (direction == 'd'){ //down
			map = sMap.getEdgeWarpDown();
			changeMap(map);
			setCharY(0 - TOP_EDGE_WARP_OFFSET);
		}
		refreshChar();
		if(EDGE_WARP_RESETS_FALL_SPEED){
			resetFallSpeed();
		}
		checkFall();
	}

	/**
	 * checks the climbability of the current position by calling
	 * Map.getClimb(x, y). returns an int.
	 * Also handles calls to normWarp(), since climbability and in-map-warp
	 * numbers are stored in the same value in map files. 
	 * 
	 * @return the climbability of current spot or the normWarp value at that spot.
	 *		   0 = not climbable. 1 = climbable. 2 = watery. 
	 * 		   value greater than or equal to 10 represents
	 * 		   an in-map-warp, with the number (climbability value - 10).
	 * 
	 * 
	 * in-map-warp takes first priority, then jump climbable, 
	 * then ladder climbable, then water, then cantclimb.
	 * 
	 */
	public int checkClimb(){
		/*
		 * 
		 * These work by checking the climbability in lines forming a box
		 * on each side of the player. If any side has something climbable,
		 * then the player can climb. Different types of climbability
		 * take priority over others (for instance if you can climb, there
		 * is no need to sink in water). Look at the main comment for this
		 * method to see priority order.
		 * 
		 * The top of the climbable range on the character is slightly above its
		 * arms (because it has short arms, and you can't climb something with
		 * your head).
		 * 
		 * note: anywhere that -11 apppears near a sChar.getY() value, it is used 
		 * to put the top bound of climb sensing at the arm height of the player
		 * 
		 * To see the climbability box, go to the GamePanel class and set the 
		 * boolean field showHitBox to true before starting the game. The red 
		 * dot in the middle of the character is one pixel below the top of
		 * the climbability range.
		 * 
		 */
		int priority = 0;
		int climb;
		int i;
		//left
		i = DOWN_COLLISION_OFFSET;
		while(i >= -11){ //upCollisionOffset){ 
			climb = sMap.getClimb(player.getX() + LEFT_COLLISION_OFFSET, player.getY() + i);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i--;
		}
		//right
		i = DOWN_COLLISION_OFFSET;
		while(i >= -11){ //upCollisionOffset){
			climb = sMap.getClimb(player.getX() + RIGHT_COLLISION_OFFSET, player.getY() + i);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i--;
		}
		//up
		i = LEFT_COLLISION_OFFSET;
		while(i <= RIGHT_COLLISION_OFFSET){
			climb = sMap.getClimb(player.getX() + i, player.getY() + -11); //upCollisionOffset);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i++;
		}
		//down
		i = LEFT_COLLISION_OFFSET;
		while(i <= RIGHT_COLLISION_OFFSET){
			climb = sMap.getClimb(player.getX() + i, player.getY() + DOWN_COLLISION_OFFSET);
			//climb = sMap.getClimb(sChar.getX() + i, sChar.getY() + downCollisionOffset + 1); //check the spot below player. this makes you unable to jump on ladder climbable and water
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i++;
		}

		if (priority >= 10){
			normWarp(priority - 10);
		}

		return priority;
	}


	/**
	 * change the player's x position, then update it 
	 * in all classes that need to know it has changed.
	 * @param newX the new x position for the player, as an int
	 */
	public void setCharX(int newX){
		player.setX(newX);
		refreshChar();
	}

	/**
	 * change the player's x position, then update it 
	 * in all classes that need to know it has changed.
	 * @param newY the new y position for the player, as an int
	 */
	public void setCharY(int newY){
		player.setY(newY);
		refreshChar();
	}

	/**
	 * change the player's step variable, then update it 
	 * in all classes that need to know it has changed.
	 * @param newStep the new step value, as an int
	 */
	public void setStep(int newStep){
		player.setStep(newStep);
		refreshChar();
	}

	/**
	 * Perform an in-map warp, or "normal warp", which can be placed
	 * anywhere in the map.
	 * Normal warps specify which map they lead to, and at what x and y 
	 * positions. This method reads those values one at a time from
	 * warpList[][] within the SMap class, then makes appropriate changes
	 * to the game to warp the player to the specified destination.
	 * @param warpNumber
	 */
	public void normWarp(int warpNumber){
		int newMap = sMap.getInMapWarpValue(warpNumber, 0);
		int newX = sMap.getInMapWarpValue(warpNumber, 1);
		int newY = sMap.getInMapWarpValue(warpNumber, 2);
		changeMap(newMap);
		setCharX(newX);
		setCharY(newY);
		if(IN_MAP_WARP_RESETS_FALL_SPEED){
			resetFallSpeed();
		}
		checkFall();
	}

	/**
	 * this method works by checking collision at every pixel in a 
	 * straight line on the specified side. All of the lines form
	 * a line around the character. To see the collision box, 
	 * go to the GamePanel class and set the boolean field 
	 * showHitBox to true before starting the game.
	 * If the player moves left, it is necessary to check 
	 * left collision, etc. 
	 * @param side: char representing which side to check ('l' left, 'r' right, 'u' up, 'd' down)
	 * @param x: the x value to check
	 * @param y: the y value to check
	 * @return boolean: true if the player can go there, false if they can't
	 */
	public boolean checkCollision(char side, int x, int y){
		int i = 0;
		if(side == 'l'){
			//System.out.println("left");
			i = DOWN_COLLISION_OFFSET;
			while(i >= TOP_COLLISION_OFFSET){
				if (!sMap.getCollision(x + LEFT_COLLISION_OFFSET + 1, y + i)){
					return false;
				}
				i--;
			}
		} else if (side == 'r'){
			//System.out.println("right");
			i = DOWN_COLLISION_OFFSET;
			while(i >= TOP_COLLISION_OFFSET){
				if (!sMap.getCollision(x + RIGHT_COLLISION_OFFSET - 1, y + i)){
					return false;
				}
				i--;
			}
		} else if (side == 'u'){
			//System.out.println("up");
			i = LEFT_COLLISION_OFFSET + 1;
			while(i <= RIGHT_COLLISION_OFFSET - 1){
				if (!(sMap.getCollision(x + i, y + TOP_COLLISION_OFFSET - 1))){
					return false;
				}
				i++;
			}
		} else if (side == 'd'){
			//System.out.println("down");
			i = LEFT_COLLISION_OFFSET + 1;
			while(i <= RIGHT_COLLISION_OFFSET - 1){
				if (!(sMap.getCollision(x + i, y + DOWN_COLLISION_OFFSET + 1))){
					return false;
				}
				i++;
			}
		}
		return true;
	}

	/**
	 * this is here to improve readability of the code, so that it is easy to see when 
	 * we are checking whether the character is on the ground or not. Calling isOnGround
	 * is a lot easier to remember and read than a call to and negation of checkCollision.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isOnGround(int x, int y){
		return !checkCollision('d', x, y);
	}

	/**
	 * When the player hits the ground, something climbable, or water, this is called.
	 * Resets necessary values so that fall acceleration, etc. is reset for the next 
	 * fall.
	 */
	public void resetFallSpeed(){
		if(checkClimb() != 2) {
			fastFalling = false;
			fastFallTimer.stop();
			fallTimer.stop();
		}
		gravityStart = GRAVITY_INITIAL_SPEED;
		fastGravityStart = FAST_GRAVITY_INITIAL_SPEED;
	}

	/**
	 * Instantiate the GameState class.
	 * @param args: int indicating which map number to start the game on (this parameter is not currently implented)
	 */
	public static void main(String [] args){
		GameState game = new GameState(12);
	}

}

