/**
 * Stores all information related to the player sprite,
 * including x and y position and which step of the walking
 * animation the player is on.
 * 
 * @version Version 1.2
 * @author Adam Cogen
 *
 */
public class Player {
	private int xPos; //current x position of the player sprite
	private int yPos; //current y position of the player sprite
	private int step; //which step of the walking animation is the player sprite currently on?
	private static final int STEP_MAX = 1; //how many different frames are in the walking animation? 1 frame is STEP_MAX = 0, 2 frames is STEP_MAX = 1, etc.
	private static final int MOVE_SIZE = 3; //how large is each movement of the player sprite? i.e. how many pixels
	
	/**
	 * initialize the player sprite, set all necessary values.
	 * @param x starting x position of player sprite
	 * @param y starting y position of player sprite
	 */
	public Player (int x, int y){
		xPos = x;
		yPos = y;
		step = 0;
	}
	/**
	 * initialize the player sprite, set all necessary values.
	 * X and Y position are not specified, and are 
	 * (0,0) until setX(), setY, setXY, etc are
	 * called.
	 */
	public Player(){
		xPos = 0;
		yPos = 0;
		step = 0;
	}
	/**
	 * Get current x position of the player
	 * @return int: current x position of the player
	 */
	public int getX(){
		return xPos;
	}
	/**
	 * Get current y position of the player
	 * @return int: current y position of the player
	 */
	public int getY(){
		return yPos;
	}
	/**
	 * Set x position of the player
	 * @param int x: new x position of the player
	 */
	public void setX(int x){
		xPos = x;
	}
	/**
	 * Set x position of the player
	 * @param int x: new x position of the player
	 */
	public void setY(int y){
		yPos = y;
	}
	/**
	 * increment player's x position by the number of pixels
	 * stored in moveSize
	 */
	public void incrementX(){
		xPos = xPos + MOVE_SIZE;
	}
	/**
	 * increment player's y position by the number of pixels
	 * stored in moveSize
	 */
	public void incrementY(){
		yPos = yPos + MOVE_SIZE;
	}
	/**
	 * decrement player's x position by the number of pixels
	 * stored in moveSize
	 */
	public void decrementX(){
		xPos = xPos - MOVE_SIZE;
	}
	/**
	 * decrement player's y position by the number of pixels
	 * stored in moveSize
	 */
	public void decrementY(){
		yPos = yPos - MOVE_SIZE;
	}
	/**
	 * Set the player sprite's x and y coordinates at the same time.
	 * @param int x: the new x position
	 * @param int y: the new y position
	 */
	public void setXY(int x, int y){
		xPos = x;
		yPos = y;
	}
	/**
	 * Get which step of the walking animation the sprite is on. 
	 * @return int: which step of the walking animation the sprite is on 
	 */
	public int getStep(){
		return step;
	}
	/**
	 * Set which step of the walking animation the sprite is on. 
	 * @param int newstep: new value of step 
	 */
	public void setStep(int newstep){
		step = newstep;
	}
	/**
	 * Increment which step of the walking animation the sprite is  
	 * on. If it is at the last step of the animation, set it back
	 * to the first step.
	 */
	public void incStep(){
		if (step < STEP_MAX){
			step++;
		} else {
			step = 0;
		}
	}
}
