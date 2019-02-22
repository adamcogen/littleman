
/**
 * 
 * Stores and interprets the data for a  map.
 * Holds collision, climbability and warp data.
 * 
 * The GameState class reads map data using a MapFileReaader instance,
 * and passes that data to a Map instance.
 * 
 * @version Version 1.3
 * @author Adam Cogen
 *
 */
public class Map {
	private int map; //the map number 
	private int spawnX; //the x value of the spawn point, as read from the file
	private int spawnY; //the y value of the spawn point, as read from the file
	private int[][] shapeData; //stores all of the shape data from the file. first value is the shape number, second value is the specific piece of data from that shape
	private int edgeWarpLeft; //if there is a left edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpRight; //if there is a right edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpUp; //if there is a up edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpDown; //if there is a down edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpConstant; //a constant meant to allow for the programmer to change how far the sprite should be before an edgewarp happens. all sides are equalized within the program, so changing the value of this field will change all sides evenly.
	private int shapeCount; //how many shapes are in the file?
	//private String mapFile; //the map number converted into a file name, e.g. 1 turns into 001.txt, 20 turns into 020.txt
	private int warpCount; //how many warps are there within the map?
	private int[][] warpList; //a list storing in-map-warps. each warp has a map number, an x value, and a  y value,

	/**
	 * Create a new sMap class by initializing the map number.
	 * @param mapNumber The map number of the loaded map
	 */
	public Map(int mapNumber){
		map = mapNumber;
	}
	/**
	 * Set the shapeData array, which holds information about all shapes to be drawn.
	 * @param data The 2-dimensional int array containing the shape info
	 */
	public void setShapeData(int[][] data){
		shapeData = data;
	}
	/**
	 * Set the shape count. Shape count is stored within the map file, 
	 * and is used to read through the shape data properly.
	 * @param val The shapeCount value
	 */
	public void setShapeCount(int val){
		shapeCount = val;
	}
	/**
	 * Set the warp count. Warp count is stored within the map file,
	 * and is used to read through the warp data properly.
	 * @param val The warpCount value
	 */
	public void setWarpCount(int val){
		warpCount = val;
	}
	/**
	 * Set the warpList array, which holds information about destinations of in-map-warps.
	 * @param list The 2-dimensional int array containing the warp data
	 */
	public void setWarpList(int[][] list){
		warpList = list;
	}

	/**
	 * check the climbability of the specified location. 
	 * 
	 * return an int that represents the climbability.
	 * if if there is no climbability, return 0.
	 * if the specified location is ladder climbable, return 1.
	 * if the specified location is watery, return 2. 
	 * if the specified location is jump climbable, return 3.
	 * @return int: the climbability value of a particular x and y position
	 */
	public int getClimb(int x, int y){
		int rectLeft = 0;
		int rectRight = 0;
		int rectTop = 0;
		int rectBottom = 0;
		int canClimb = 0;
		int charWidthR = 0;
		int charWidthL = 0;
		int charHeightU = 0;
		int charHeightD = 0;
		checkAll: for (int i = 0; i < shapeCount; i++){
			rectLeft = readNum(i, 0);
			rectRight = rectLeft + readNum(i, 2);
			rectTop = readNum(i, 1);
			rectBottom = rectTop + readNum(i, 3);
			if(((x <= rectLeft + charWidthR && x >= rectLeft + charWidthR) || (x >= rectRight + charWidthL && x <= rectRight + charWidthL) || (x >= rectLeft + charWidthL - 3 && x <= rectRight + charWidthR)) && (y >= rectTop + charHeightU && y <= rectBottom + charHeightD)){
				canClimb = readNum(i, 5);
				if (canClimb == 0 || canClimb >= 10){
					if(canClimb >= 10){
						return(canClimb); // do an in-map-warp! returns warp number + 10 (this is how the value is stored in the file).
					}
				} else {
					if (canClimb == 2){
						return 2;
					} else if (canClimb == 1){
						return 1;
					} else if (canClimb == 3){
						return 3;
					}

				}
			}
		}
		return 0;
	}

	/**
	 * read a number from the shape array.
	 * @param rectangle the number of the shape to read
	 * @param value the index of the value to read, 0-8
	 * @return the specified value from the specified rectangle
	 */
	public int readNum(int rectangle, int value){
		return shapeData[rectangle][value];
	}

	/**
	 * check the collision of the spot the sprite is about to go to.
	 * can it go there? 
	 * usually called right before trying to move to a new location.
	 * @param x the x coordinate to check
	 * @param y the y coordinate to check
	 * @return boolean: true if player can walk through the specified x, y position. false if not.
	 */
	public boolean getCollision(int x, int y){
		int xMin = 0;
		int xMax = 0;
		int yMin = 0;
		int yMax = 0;
		int charWidthR = 0;
		int charWidthL = 0;
		int charHeightU = 0;
		int charHeightD = 0;
		checkAll: for (int i = 0; i < shapeCount; i++){
			if (readNum(i, 4) == 1){
				xMin = readNum(i, 0);
				yMin = readNum(i, 1);
				xMax = xMin + readNum(i, 2);
				yMax = yMin + readNum(i, 3);
				if(x >= xMin + charWidthR && x <= xMax + charWidthL && y >= yMin + charHeightU && y <= yMax + charHeightD){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Return the map number of the edge warp on the left side of the map.
	 * @return int: map number of left edge warp
	 */
	public int getEdgeWarpLeft(){
		return edgeWarpLeft;
	}

	/**
	 * Return the map number of the edge warp on the right side of the map.
	 * @return int: map number of right edge warp
	 */
	public int getEdgeWarpRight(){
		return edgeWarpRight;
	}

	/**
	 * Return the map number of the edge warp at the top of the map.
	 * @return int: map number of top edge warp
	 */
	public int getEdgeWarpUp(){
		return edgeWarpUp;
	}

	/**
	 * Return the map number of the edge warp at the bottom of the map.
	 * @return int: map number of bottom edge warp
	 */
	public int getEdgeWarpDown(){
		return edgeWarpDown;
	}

	/**
	 * Set the map number of the edge warp on the left side of the map.
	 * Used when passing data from MapFileReader to GameState to Map.
	 * @param edgeWarpL int: the map number of the new left edge warp 
	 */
	public void setEdgeWarpLeft(int edgeWarpL){
		edgeWarpLeft = edgeWarpL;
	}
	
	/**
	 * Set the map number of the edge warp on the right side of the map.
	 * Used when passing data from MapFileReader to GameState to Map.
	 * @param edgeWarpR int: the map number of the new right edge warp 
	 */
	public void setEdgeWarpRight(int edgeWarpR){
		edgeWarpRight = edgeWarpR;
	}
	
	/**
	 * Set the map number of the edge warp at the top of the map.
	 * Used when passing data from MapFileReader to GameState to Map.
	 * @param edgeWarpU int: the map number of the new up edge warp 
	 */
	public void setEdgeWarpUp(int edgeWarpU){
		edgeWarpUp = edgeWarpU;
	}
	
	/**
	 * Set the map number of the edge warp at the bottom of the map.
	 * Used when passing data from MapFileReader to GameState to Map.
	 * @param edgeWarpD int: the map number of the new down edge warp 
	 */
	public void setEdgeWarpDown(int edgeWarpD){
		edgeWarpDown = edgeWarpD;
	}
	
	/**
	 * Return the specified value at the specified warp number within the
	 * warpList. This will be called to perform in-map-warps. 
	 * @param warpNumber Which warp number in the list are we checking?
	 * @param value Which value within that warp are we returning? 0 = map number, 1 = x position, 2 = y position. 
	 * @return specified value from the warpList
	 */
	public int getInMapWarpValue(int warpNumber, int value){
		return warpList[warpNumber][value];
	}

}