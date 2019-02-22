
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the map maker class for Littleman's World Builder.
 * This class implements the map maker's functionality 
 * as well as its GUI, so it is very long. 
 * 
 * changelog:
 * 1.1: in-map warps and map editing capabilities
 * 1.2: improved spawn-point marker, improved cursor calibration, 
 * 		improved in-file format notes, added climb-type editing 
 * 		for jump vs ladder climb, added ability to draw shapes
 * 		from bottom right corner to top left, from top right
 *		corner to bottom left, and from bottom left corner
 *		to top right (whereas before they could only be added
 *		from top left to bottom right).
 * 1.3:	improved key shortcut sensing (+, -, delete keys
 *		now work while in edit menu), improved in-map warp 
 *		file format ('n' now specifies destination is
 *		same as current map), set map number menu now
 *		automatically sets default number to lowest
 *		unused map number in "maps/" file directory
 *
 * @author Adam Cogen
 * @version Version 1.3
 */
public class MapMaker extends JFrame{
	private int gameWidth = 430; //the width of the map
	private int gameHeight = 210; //the height of the map
	private static final int TOOLBOX_WIDTH = 200; //the width of the toolbox
	private static final int TOOLBOX_HEIGHT = 355; //the height of the toolbox
	private MapPreviewPanel panel; //extends JPanel, draws the map on the map frame
	private int map; //an integer that tells the game which file to open
	private int mouseX; //a temporary storing place for mouse X coordinate, used in many click listeners and such
	private int mouseY; //a temporary storing place for mouse Y coordinate, used in many click listeners and such
	private JMenuBar menuBar; //the toolbox menu bar
	private JRadioButton setCollisZero; //radio button to turn off collision while drawing
	private JRadioButton setCollisOne; //radio button to turn on collision while drawing
	private JMenu edit; //the edit category of the toolbox menu
	private JMenu file; //the file category of the toolbox menu
	private JMenuItem save; //the save button in file --> save
	private JMenuItem load; //the load button in file --> load
	private JMenuItem undo; //the undo button in edit --> undo
	private JMenuItem gameDimensions; //edit --> game dimensions
	private JMenuItem spawnPoint; //edit --> spawn point
	private JMenuItem mapNum; //file --> set map number
	private JMenu drawMode; //edit --> draw mode, opens a submenu with drawmode options
	private JRadioButtonMenuItem rectangleMode; //edit --> drawmode --> rectangle, draw rectangles
	private JRadioButtonMenuItem editMode; //edit --> drawmode --> edit, edit all aspects of existing shapes, delete shapes, etc.
	private JRadioButtonMenuItem charPreviewMode; //edit-->drawmode-->sprite preview mode, allows you to place a picture of the game character in order to measure scale and see if he will fit in certain places
	private int newRectStep; //creating a new shape is a 3 (?) step process, and different things happen in each step. this keeps track of which step you're on.
	private int newRectX; //this is the X value of the top left corner of a new shape that is actively being drawn
	private int newRectY; //this is the Y value of the top left corner of a new shape that is actively being drawn
	private int newRectWidth; //this is the width of a shape that is actively being drawn (this helps with rendering a preview of the shape as you move the mouse before clicking to finalize the shape)
	private int newRectHeight;//this is the height of a shape that is actively being drawn (this helps with rendering a preview of the shape as you move the mouse before clicking to finalize the shape)
	private int collisVal; //this value represents the "collision" of a shape, whether it is a rectangle or an oval, and whether it should be rendered in front of or behind the sprite in game. 
	//0 = no collision rectangle behind player, 1 = collision rectangle, 2 = no collision rectangle in front of player, 3 = no collision oval behind player, 4 = no collision oval in front of player, 5 = warp
	private int climbable;//this value represents the "climbability" of a shape -- if you can climb up it, aka walk upwards while next to it or within it without necessarily being on the "ground."
	//0 = can't climb, 1 = can climb and you will not sink over time (good for climbing a ladder or a tree), 2 = can climb but you will sink over time (this is good for actions such as swimming, walking through a cloud, etc).
	private int redVal; //this is the red RGB value of the currently active color, used to draw shapes, draw the sprite preview, etc
	private int greenVal; //this is the green RGB value of the currently active color, used to draw shapes, draw the sprite preview, etc
	private int blueVal; //this is the blue RGB value of the currently active color, used to draw shapes, draw the sprite preview, etc
	private int rectCount; //this keeps track of how many shapes there are on the map / in a file. it changes as you draw / delete / undo etc. 
	private JFrame toolBox; //this is the frame that holds the toolbox
	private ToolBoxPanel toolBoxPanel; //this is the panel that goes in the toolbox
	private JLabel collisLabel; //this is a label for the collision settings within the toolbox
	private JLabel colorLabel; //this is a label for the color settings within the toolbox
	private JLabel redLabel; //this is a label for the red RGB settings in the toolbox
	private JLabel blueLabel; //this is a label for the blue RGB settings in the toolbox
	private JLabel greenLabel; //this is a label for the green RGB settings in the toolbox
	private JTextField redField; //this is a text box for entering the red RGB value in the toolbox
	private JTextField greenField;//this is a text box for entering the green RGB value in the toolbox
	private JTextField blueField;//this is a text box for entering the blue RGB value in the toolbox
	private JButton colorButton;//this button sets the current color to whatever you have in the RGB text boxes within the toolbox
	private JCheckBox collisClimbable;//this check box within the toolbox indicates whether shapes you draw will be climbable at all. when selected, the option for "water" becomes available
	private JLabel climbableLabel;//this is a label for the "climbability" settings within the toolbox
	private JRadioButton setClimbableJump; //this is a radio button to select jump climbable instead of small climbable
	private JRadioButton setClimbableLadder; //this is a radio button to select small climbable instead of jump climbable
	private JLabel spacingLabel1; //this is a label to help manage layout 
	private JLabel spacingLabel2; //this is a label to help manage layout
	private ArrayList<Integer> rectList; //this arraylist of integers holds all of the values that represent a map. it is filled when loading a map, and written to a file when saving a map.
	private JLabel waterLabel; //this is a label for the "water" climbability checkbox within the toolbox
	private JCheckBox collisWater; //this is the checkbox within the toolbox to choose whether you want shapes being drawn to behave like water. only available when the climbable setting is selected.
	private JLabel blockLayer; //this label indicates the section of the toolbox with block layering settings
	private JRadioButton layerFront; //this is a radio button within the toolbox to select that you want shapes being drawn to be rendered in front of the sprite in-game
	private JRadioButton layerBack; //this is a radio button within the toolbox to select that you want shapes being drawn to be rendered in behind the sprite in-game
	private JFrame dimensionsFrame;//this is a closeable frame for the "set dimensions" menu, edit --> map dimensions within the toolbox
	private JPanel dimensionsPanel;//this is the panel that goes in the set dimensions frame
	private JLabel widthLabel; //this is a label to indicate where you can change the width of the map in the set dimensions frame
	private JLabel heightLabel;//this is a label to indicate where you can change the height of the map in the set dimensions frame
	private JTextField widthInput;//this text box is where you input the desired width of the map within the set dimensions window
	private JTextField heightInput;//this text box is where you input the desired height of the map within the set dimensions window
	private JButton setDimensions; //this is the button that applies the new dimensions you've entered in the set dimensions window, and resizes the map window accordingly
	private JFrame spawnFrame;//this is a closeable frame for the "spawn point" menu, edit --> spawn point within the toolbox
	private JPanel spawnPanel;//this is the panel that goes in the spawn point frame
	private JLabel spawnXLabel; //this is the label in toolbox --> edit --> spawnpoint for the desired x value
	private JLabel spawnYLabel; //this is the label in toolbox --> edit --> spawnpoint for the desired y value
	private JTextField spawnXInput; //this is the input in toolbox --> edit --> spawnpoint for the desired x value
	private JTextField spawnYInput; //this is the input in toolbox --> edit --> spawnpoint for the desired y value
	private JButton setSpawn; //this is the button in toolbox --> edit --> spawnpoint to set the desired spawn coordinates
	private JFrame mapNumFrame; //this is a closeable frame for the "set map number" menu, file --> set map number within the toolbox
	private JPanel mapNumPanel; //the panel to be used with toolbox --> file --> set map number
	private JLabel mapNumLabel; //the "Map Number" label in the set map number menu
	private JTextField mapNumInput; //the map number input textbox in the set map number menu
	private JButton setMapNum; //the "Set Map Number" button in the set map number menu
	private int spawnX; //x-value of the spawn point, as set in toolbox --> edit --> spawn point. may be slightly adjust to put the center of the S character at the specified pixel.
	private int spawnY; //y-value of the spawn point, as set in toolbox --> edit --> spawn point. may be slightly adjust to put the center of the S character at the specified pixel.
	private boolean showXYCoords;//this determins whether the mouse X and Y coordinates will be shown in the toolbox. it is never turned off.
	private int dispMouseX; //the mouse x coordinate to display in the tool box x y coordinate section
	private int dispMouseY; //the mouse y coordinate to display in the tool box x y coordinate section
	private JLabel mouseCoordsLabel; //the label in the toolbox that says "Mouse XY:" next to mouse coordinate display
	//private JLabel spacingLabel5; //this is a label full of spacebar characters, for layout purposes.
	private int drawType;  //this is changed as the draw mode changes. it tells different areas of the program what to do within different draw modes, i.e. using if statements.
	//0=rectangle, 1=edit, 2=charpreview, 3 = warp draw
	private int permaSpritePreviewX; //this is the x coordinate to draw the permanent sprite preview -- it is set by clicking. before you click for the first time, it is far off screen (not visible)
	private int permaSpritePreviewY; //this is the y coordinate to draw the permanent sprite preview -- it is set by clicking. before you click for the first time, it is far off screen (not visible)
	private int tempSpritePreviewX; //this is the x coordinate to draw the temporary mouse-movement sprite preview
	private int tempSpritePreviewY; //this is the y coordinate to draw the temporary mouse-movement sprite preview
	private int permaSpritePreviewColorR; //this is the red RGB value to use when drawing the permanent sprite preview. it is set using the color setting in the toolbox.
	private int permaSpritePreviewColorG; //this is the green RGB value to use when drawing the permanent sprite preview. it is set using the color setting in the toolbox.
	private int permaSpritePreviewColorB; //this is the blue RGB value to use when drawing the permanent sprite preview. it is set using the color setting in the toolbox.
	private boolean showPermaSpritePreview; //this boolean determins whether or not the permanent sprite preview should be shown. it is never turned off.
	private String edgeWarpLeft; //a string containing either the map number of the left edge warp or the letter "n," for no left edge warp
	private String edgeWarpRight; //a string containing either the map number of the left edge warp or the letter "n," for no right edge warp
	private String edgeWarpUp; //a string containing either the map number of the left edge warp or the letter "n," for no up edge warp
	private String edgeWarpDown; //a string containing either the map number of the left edge warp or the letter "n," for no down edge warp
	private JFrame edgeWarpFrame; //this is a closeable frame for the "edge warps" menu, edit --> edge warps within the toolbox
	private JPanel edgeWarpPanel; //the panel to be used with toolbox --> edit --> edge warps
	private JLabel edgeWarpLeftLabel; //the word "Left" in the edge warp menu
	private JLabel edgeWarpRightLabel; //the word "Right" in the edge warp menu
	private JLabel edgeWarpUpLabel; //the word "Up" in the edge warp menu
	private JLabel edgeWarpDownLabel; //the word "Down" in the edge warp menu
	private JTextField edgeWarpLeftInput; //the left warp input textbox in the edge warp menu
	private JTextField edgeWarpRightInput; //the right warp input textbox in the edge warp menu
	private JTextField edgeWarpUpInput; //the up warp input textbox in the edge warp menu
	private JTextField edgeWarpDownInput; //the down warp input textbox in the edge warp menu
	private JButton setEdgeWarps; //the "Set Edge Warps" button in the edge warp menu
	private JCheckBox edgeWarpLeftCheck; //check box indicating whether there should be a left edge warp in the set edge warps menu
	private JCheckBox edgeWarpRightCheck; //check box indicating whether there should be a right edge warp in the set edge warps menu
	private JCheckBox edgeWarpUpCheck; //check box indicating whether there should be an up edge warp in the set edge warps menu
	private JCheckBox edgeWarpDownCheck; //check box indicating whether there should be a down edge warp in the set edge warps menu
	private JMenuItem edgeWarps; //called at toolbox --> edit --> edge warps. used to set / edit edge warps.
	private JRadioButtonMenuItem ovalMode; //toolbox --> edit --> draw mode --> oval mode. for drawing no-collision ovals. drawtype = 2
	private JFrame colorChooserFrame; //this is a closeable frame for the color picker that opens when you click the color preview box within the toolbox
	private JPanel colorChooserPanel; //the panel to be used for the color picker that opens when you click the color preview box within the toolbox
	private JColorChooser colorChooser; //the color chooser to display when you click on the toolbox color preview box
	private JButton colorChooserOK; //the OK button in the color chooser accessible by clicking on the toolbox color preview box
	private JButton colorChooserCancel; //the cancel button in the color chooser accessible by clicking on the toolbox color preview box
	private JFrame editMenuFrame; //this is a closeable frame for the "edit" menu, edit --> draw mode --> edit within the toolbox
	private JPanel editMenuPanel; //the panel to be used with toolbox --> edit --> draw mode --> edit
	private JLabel editMenuX1Label; //edit menu label that says "Top Left X"
	private JLabel editMenuY1Label; //edit menu label that says "Top Left Y"
	private JLabel editMenuX2Label; //edit menu label that says "Bottom Right X"
	private JLabel editMenuY2Label; //edit menu label that says "Bottom Right Y"
	private JTextField editMenuX1Input; //textbox for entering top left x value on the edit menu
	private JTextField editMenuY1Input; //textbox for entering top left y value on the edit menu
	private JTextField editMenuX2Input; //textbox for entering bottom right x value on the edit menu
	private JTextField editMenuY2Input; //textbox for entering bottom right y value on the edit menu
	private JTextField editMenuCountInput; //textbox that lets you enter which shape number you would like to select
	private JLabel editMenuCountLabel; //label on edit menu that says "Shape Number:" for checking which shape number is selected
	private JButton editMenuCountPlus; //"+" button on the edit menu for selecting the next highest shape number
	private JButton editMenuCountMinus; //"-" button on the edit menu for selecting the next lowest shape number
	private JLabel editMenuCountFraction; //the label on the edit menu that shows how many shapes there, next to the editMenuCountInput textbox
	private JButton editMenuCountSet; //button to choose the shape number that is entered in the editMenuCountInput
	private JButton editMenuSendToBack; //button to send the currently selected shape to the back layer of the map
	private JButton editMenuBringToFront; //button to bring the currently selected shape to the front layer of the map
	private int editMenuCountVal; // the shape number of the currently selected shape. it starts at 1 instead of 0, so you may have to subtract 1 from it when you use it.
	private int selectionX; //the top left x coordinate of the shape outline to draw for a selection in edit mode
	private int selectionY; //the top left y coordinate of the shape outline to draw for a selection in edit mode
	private int selectionWidth; //the width of the shape outline to draw for a selection in edit mode
	private int selectionHeight; //the height of the shape outline to draw for a selection in edit mode
	private int selectionType; //the selection type of a selection in edit mode -- either rectangle (probably 0) or oval (probably 1)
	private boolean selectionMade; //if in edit mode, has a selection been made? if not, certain buttons and boxes will deactivate within the edit menu
	private JButton editMenuSetCoords; //the button to set new coordinates of the selected shape from within the edit menu
	private JButton editMenuDeleteShape; //the button to delete the selected shape from within the edit menu
	private JFrame loadFrame; //this is a closeable frame for the "load" menu, file --> load within the toolbox
	private JPanel loadPanel; //the panel to be used with toolbox --> file --> load
	private JLabel loadLabel; //the label within the load menu that says "Map Number:"
	private JTextField loadInput; //the textbox for entering the map number to load within the load menu
	private JButton loadMap; //the button to load the map number specified within the loadInput text field in the load menu
	private boolean loading; //is the game currently being loaded? if it is, the main map panel is not rendered until loading is complete. this is to prevent rendering an incomplete map / shape array
	private JLabel editColorLabel; //label on the edit menu edit color section that says "Color:"
	private JLabel editRedLabel; //label on the edit menu edit color section that says "Red:"
	private JLabel editGreenLabel; //label on the edit menu edit color section that says "Green:"
	private JLabel editBlueLabel; //label on the edit menu edit color section that says "Blue:"
	private JTextField editRedField; //textbox on the edit menu edit color section that contains the red value
	private JTextField editGreenField; //textbox on the edit menu edit color section that contains the green value
	private JTextField editBlueField; //textbox on the edit menu edit color section that contains the blue value
	private JButton editColorButton; //button on the edit menu edit color section that applies any changes made to color values. says "Set Color"
	private int editRedVal; //the value in "red" textbox the of the "set color" portion of the edit menu
	private int editGreenVal; //the value in "green" textbox the of the "set color" portion of the edit menu
	private int editBlueVal; //the value in "blue" textbox the of the "set color" portion of the edit menu
	private JMenuItem newMap; //file --> new map menu item
	private JLabel editCollisLabel; //label on the edit menu edit collision section that says "Collision:"
	private JRadioButton editSetCollisZero; //radiobutton on the edit menu edit collision section that says "Off", sets collision to off on current shape
	private JRadioButton editSetCollisOne; //radiobutton on the edit menu edit collision section that says "On", sets collision to on on current shape
	private JLabel editClimbableLabel; //label on the edit menu edit collision/climbability section that says "Climbable:"
	private JCheckBox editCollisClimbable; //check box on the edit menu edit collision/climbability section next to "Climbable:" label, determines whether selected shape is climbable or not
	private JLabel editWaterLabel; //label on the edit menu edit collision/climbability section that says "Watery:"
	private JCheckBox editCollisWater; //check box on the edit menu edit collision/climbability section next to "Watery:" label, determines whether selected shape is climb-type watery or not
	private JLabel climbableTypeLabel; //label for the type of climbability (jump vs ladder)
	private JLabel editClimbableTypeLabel; //edit menu label for the type of climbability (jump vs ladder)
	private JRadioButton editClimbableJump; //edit menu button to change climbability type to jump (instead of ladder)
	private JRadioButton editClimbableLadder; //edit menu button to change climbability type to ladder (instead of jump)
	private JLabel editBlockLayer; //label on the edit menu edit block layering section that says "Block Layering:"
	private JRadioButton editLayerFront; //radiobutton on the edit menu edit block layering that says "In Front Of Sprite" and sets block layering to in front of player
	private JRadioButton editLayerBack; //radiobutton on the edit menu edit block layering that says "Behind Sprite" and sets block layering to behind player
	private JButton editChangeCollis; //button on the edit menu edit collision section that says "Set Attributes", applies any changes made to current shape's collision, climbability, block layering, etc
	private int tempClimbable; //a temporary climbability value, used to maintain the correct status of different buttons in the edit window when climbability changes haven't yet been applied, e.g. deactivate the water setting if climbability is off, even if the change hasn't been applied
	private int tempCollisVal; //a temporary collision value, used to maintain the correct status of different buttons in the edit window when collision changes haven't yet been applied, e.g. deactivate the water setting if climbability is off, even if the change hasn't been applied
	private boolean editHideColor; //when there is nothing selected in edit mode, the edit window color preview box should be greyed out. this is used in an if statement to change the color of the edit window color preview box depending on whether a shape is selected or not.
	private JFrame warpDrawFrame; //the frame to be used for the menu that appears when you are in warp draw mode
	private JPanel warpDrawPanel; //the panel within the warpDrawFrame that holds all elements of the warp draw menu
	private JButton setWarpDraw; //the button that you press to change the destination of warps in the warp draw menu to the values specified in the textboxes
	private JLabel warpToMapLabel; //the label that says "Warp To Map:" in the warp draw menu
	private JTextField warpToMapInput; //the textbox where you specify which map number to set the warp destination to in the warp draw menu
	private JLabel warpToXLabel; //the label in the warp menu that says "Warp To X:"
	private JTextField warpToXInput; //the textbox where you specify x coordinate to set the warp destination to in the warp draw menu
	private JLabel warpToYLabel; //the label in the warp menu that says "Warp To Y:"
	private JTextField warpToYInput; //the textbox where you specify y coordinate to set the warp destination to in the warp draw menu
	private ArrayList<Integer> warpList; // an arraylist that stores the map num, x coord, and y coord for each in-map warp.
	private JRadioButtonMenuItem warpMode; //toolbox --> edit --> draw mode --> warp mode
	private int warpToMapNum; //what map number should the warp being drawn send the sprite to?
	private int warpToXCoord; //what x coordinate should the warp being drawn send the sprite to?
	private int warpToYCoord; //what y coordinate should the warp being drawn send the sprite to?
	private int warpCount; //how many warps are on this map?
	private JCheckBox editWarpOn; //the checkbox in the edit menu that specifies whether the currently selected shape is a warp or not
	private JTextField editWarpMap; //textbox to enter the desired map number destination of a warp from within the edit menu
	private JTextField editWarpX; //textbox to enter the desired x coordinate destination of a warp from within the edit menu
	private JTextField editWarpY; //textbox to enter the desired y coordinate destination of a warp from within the edit menu
	private JButton editWarpButton; //the button in the warp section of the edit menu that lets you apply changes you've made to a selected warp by putting desired values in the editWarpMap, editWarpX, and editWarpY textboxes
	private JLabel editWarpMapLabel; //the label in the edit menu warp section that says "To Map #:"
	private JLabel editWarpXLabel; //the label in the edit menu warp section that says "To X:"
	private JLabel editWarpYLabel; //the label in the edit menu warp section that says "To Y:"
	private JLabel editWarpOnLabel; //the label that says "Warp:" next to the checkbox in the edit menu that indicates whether the selected shape is a warp or not
	private JCheckBox editScrollThruWarps; //the check box in the edit menu that makes the "+" and "-" buttons only scroll through shapes that are warps
	private JLabel editScrollThruWarpsLabel; //the label on the edit menu that says "Scroll Through Warps Only:"
	private boolean scrollWarpsOnly; //a boolean that stores whether the editScrollThruWarps checkbox is checked or not, and thus whether the "+" and "-" buttons should only scroll through shapes that are warps
	private JLabel editWarpTotalLabel; //the label in the edit menu that displays the warp total


	/**
	 * make a new map maker window and initialize all necessary fields.
	 */
	public MapMaker(){
		
		map = checkMinimumAvailableMapNumber();
		loading = false;
		selectionMade = false;
		warpList = new ArrayList();
		warpCount = 0;
		scrollWarpsOnly = false;
		tempClimbable = -1;
		tempCollisVal = -1;
		edgeWarpLeft = "n";
		edgeWarpRight = "n";
		edgeWarpUp = "n";
		edgeWarpDown = "n";
		permaSpritePreviewColorR = 0;
		permaSpritePreviewColorG = 0;
		permaSpritePreviewColorB = 0;
		showPermaSpritePreview = true;
		hideSpritePreview();
		tempSpritePreviewX = -1000;
		tempSpritePreviewY = -1000;
		drawType = 0;
		dispMouseX = 0;
		dispMouseY = 0;
		showXYCoords = true;
		collisVal = 1;
		climbable = 0;
		redVal = 0;
		greenVal = 0;
		blueVal = 0;
		newRectStep = 0;
		rectList = new ArrayList<Integer>();
		rectCount = 0;
		spawnX = 99; //default spawn point
		spawnY = 99; //default spawn point
		
		initializeMapPreview();
		initializeToolBox();
		initializeGameDimensionsMenu();
		initializePlayerSpawnMenu();
		initializeMapNumberMenu();
		initializeEdgeWarpMenu();
		initializeEditModeMenu();
		initializeLoadMenu();
		initializeInMapWarpDrawMenu();
		initializeListeners();
		//addCustomCursor();
	}
	
	private void initializeMapPreview() {
		//main frame
				panel = new MapPreviewPanel();
				this.add(panel);
				this.setTitle("Map Maker: New Map");
				this.refreshFrameSize();
				this.pack();
				this.setResizable(false);
				this.setLocation(TOOLBOX_WIDTH + 10, 0);
				this.setVisible(true);
				this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void initializeToolBox() {
		//toolbox frame    
				toolBox = new JFrame();
				toolBox.setSize(TOOLBOX_WIDTH, TOOLBOX_HEIGHT);
				toolBox.setDefaultCloseOperation(EXIT_ON_CLOSE);
				toolBox.setTitle("Toolbox");
				//toolbox menu
				menuBar = new JMenuBar();
				file = new JMenu("File");
				edit = new JMenu("Edit");
				save = new JMenuItem("Save");
				load = new JMenuItem("Load");
				newMap = new JMenuItem("New Map");
				gameDimensions = new JMenuItem("Map Dimensions");
				spawnPoint = new JMenuItem("Spawn Point");
				mapNum = new JMenuItem("Set Map Number");
				drawMode = new JMenu("Draw Mode");
				save.setEnabled(false);
				file.add(newMap);
				file.add(mapNum);
				file.add(save);
				file.add(load);
				undo = new JMenuItem("Undo Draw Shape");
				edit.add(undo);
				edgeWarps = new JMenuItem("Edge Warps");
				drawMode = new JMenu("Draw Mode");
				edit.add(drawMode);
				edit.add(gameDimensions);
				edit.add(spawnPoint);
				edit.add(edgeWarps);
				rectangleMode = new JRadioButtonMenuItem("Rectangle", true);
				editMode = new JRadioButtonMenuItem("Edit");
				charPreviewMode = new JRadioButtonMenuItem("Sprite Preview Mode");
				ovalMode = new JRadioButtonMenuItem("Oval Mode");
				warpMode = new JRadioButtonMenuItem("Warp Mode");
				drawMode.add(rectangleMode);
				drawMode.add(editMode);
				drawMode.add(charPreviewMode);
				drawMode.add(ovalMode);
				drawMode.add(warpMode);
				ButtonGroup drawModeButtons = new ButtonGroup();
				drawModeButtons.add(rectangleMode);
				drawModeButtons.add(ovalMode);
				drawModeButtons.add(editMode);
				drawModeButtons.add(charPreviewMode);
				drawModeButtons.add(warpMode);
				menuBar.add(file);
				menuBar.add(edit);
				toolBox.setJMenuBar(menuBar);
				collisLabel = new JLabel("Collision:");
				setCollisZero = new JRadioButton("Off");
				setCollisOne = new JRadioButton("On", true);
				ButtonGroup collisButtons = new ButtonGroup();
				collisButtons.add(setCollisZero);
				collisButtons.add(setCollisOne);
				toolBoxPanel = new ToolBoxPanel();
				toolBoxPanel.add(collisLabel);
				toolBox.add(toolBoxPanel);
				toolBoxPanel.add(setCollisOne);
				toolBoxPanel.add(setCollisZero);
				climbableLabel = new JLabel("Climbable:");
				collisClimbable = new JCheckBox();
				toolBoxPanel.add(climbableLabel);
				toolBoxPanel.add(collisClimbable);
				waterLabel = new JLabel("Watery: ");
				collisWater = new JCheckBox();
				toolBoxPanel.add(waterLabel);
				toolBoxPanel.add(collisWater);
				collisWater.setEnabled(false);
				waterLabel.setEnabled(false);
				toolBox.setResizable(false);
				colorLabel = new JLabel("Color: ");
				redLabel = new JLabel("Red: ");
				greenLabel = new JLabel("Green: ");
				blueLabel = new JLabel("Blue: ");
				redField = new JTextField("0", 3);
				greenField = new JTextField("0", 3);
				blueField = new JTextField("0", 3);
				colorButton = new JButton("Set Color");
				toolBoxPanel.add(colorLabel);
				spacingLabel2 = new JLabel("           ");
				mouseCoordsLabel = new JLabel ("          " + "Mouse XY:" + "                                        ");
				toolBoxPanel.add(spacingLabel2);
				toolBoxPanel.add(redLabel);
				toolBoxPanel.add(redField);
				toolBoxPanel.add(greenLabel);
				toolBoxPanel.add(greenField);
				toolBoxPanel.add(blueLabel);
				toolBoxPanel.add(blueField);
				toolBoxPanel.add(colorButton);
				toolBoxPanel.add(mouseCoordsLabel);
				if(rectList.isEmpty()){
					undo.setEnabled(false);
				}

				//toolbox: block layering (front vs back)
				blockLayer = new JLabel("Block Layering: ");
				layerFront = new JRadioButton("In Front Of Sprite");
				layerBack = new JRadioButton("Behind Sprite", true);
				ButtonGroup layerGroup = new ButtonGroup();
				layerGroup.add(layerFront);
				layerGroup.add(layerBack);
				blockLayer.setEnabled(false);
				layerFront.setEnabled(false);
				layerBack.setEnabled(false);
				toolBoxPanel.add(blockLayer);
				toolBoxPanel.add(layerFront);
				toolBoxPanel.add(layerBack);
				//toolbox: climbability (jump vs climb)
				climbableTypeLabel = new JLabel("          Climb Type:        ");
				setClimbableJump = new JRadioButton("Jump");
				setClimbableLadder = new JRadioButton("Ladder");
				toolBoxPanel.add(climbableTypeLabel);
				toolBoxPanel.add(setClimbableJump);
				toolBoxPanel.add(setClimbableLadder);
				climbableTypeLabel.setEnabled(false);
				setClimbableJump.setEnabled(false);
				setClimbableLadder.setEnabled(false);
				setClimbableLadder.setSelected(true);
				ButtonGroup climbTypeGroup = new ButtonGroup();
				climbTypeGroup.add(setClimbableJump);
				climbTypeGroup.add(setClimbableLadder);
				
				//toolbox: color chooser
				colorChooserFrame = new JFrame();
				colorChooserPanel = new JPanel();
				colorChooser = new JColorChooser(new Color(redVal, greenVal, blueVal));
				colorChooserPanel.add(colorChooser);
				colorChooserOK = new JButton("Choose Color");
				colorChooserCancel = new JButton("Cancel");
				colorChooserPanel.add(colorChooserOK);
				colorChooserPanel.add(colorChooserCancel);
				colorChooserPanel.setPreferredSize(new Dimension(698, 430));
				colorChooserFrame.add(colorChooserPanel);
				colorChooserFrame.pack();
				colorChooserFrame.setResizable(false);
				
				toolBox.setVisible(true);
	}
	
	public void initializeGameDimensionsMenu() {
		//game dimensions menu
				dimensionsFrame = new JFrame();
				dimensionsFrame.setSize(150, 150);
				dimensionsFrame.setTitle("Dimensions");
				dimensionsFrame.setResizable(false);
				dimensionsPanel = new JPanel();
				widthLabel = new JLabel("Width:");
				heightLabel = new JLabel("Height:");
				spacingLabel1 = new JLabel("    ");
				widthInput = new JTextField("" + gameWidth, 3);
				heightInput = new JTextField("" + gameHeight, 3);
				setDimensions = new JButton("Set Dimensions");
				dimensionsPanel.add(widthLabel);
				dimensionsPanel.add(widthInput);
				dimensionsPanel.add(spacingLabel1);
				dimensionsPanel.add(heightLabel);
				dimensionsPanel.add(heightInput);
				dimensionsPanel.add(setDimensions);
				dimensionsFrame.add(dimensionsPanel);
	}
	
	public void initializePlayerSpawnMenu() {
		//spawn menu
				spawnFrame = new JFrame();
				spawnFrame.setSize(150, 150);
				spawnFrame.setTitle("Spawn");
				spawnFrame.setResizable(false);
				spawnPanel = new JPanel();
				spawnXLabel = new JLabel("Spawn X:");
				spawnYLabel = new JLabel("Spawn Y:");
				spawnXInput = new JTextField("" + spawnX, 3);
				spawnYInput = new JTextField("" + spawnY, 3);
				setSpawn = new JButton("Set Spawn");
				spawnPanel.add(spawnXLabel);
				spawnPanel.add(spawnXInput);
				spawnPanel.add(spawnYLabel);
				spawnPanel.add(spawnYInput);
				spawnPanel.add(setSpawn);
				spawnFrame.add(spawnPanel);
		
	}
	
	public void initializeMapNumberMenu() {
		//map number menu
				mapNumFrame = new JFrame();
				mapNumFrame.setSize(150, 100);
				mapNumFrame.setTitle("Map #");
				mapNumFrame.setResizable(false);
				mapNumPanel = new JPanel();
				mapNumLabel = new JLabel("Map Number:");
				mapNumInput = new JTextField("" + map, 3);
				setMapNum = new JButton("Set Map Number");
				mapNumPanel.add(mapNumLabel);
				mapNumPanel.add(mapNumInput);
				mapNumPanel.add(setMapNum);
				mapNumFrame.add(mapNumPanel);
	}
	
	public void initializeEdgeWarpMenu() {
		//edgewarp menu
				edgeWarpFrame = new JFrame();
				edgeWarpFrame.setSize(150, 200);
				edgeWarpFrame.setTitle("Edges");
				edgeWarpFrame.setResizable(false);
				edgeWarpPanel = new JPanel();
				edgeWarpLeftLabel = new JLabel("Left:");
				edgeWarpRightLabel = new JLabel("Right:");
				edgeWarpUpLabel = new JLabel("Up:");
				edgeWarpDownLabel = new JLabel("Down:");
				edgeWarpLeftLabel.setEnabled(false);
				edgeWarpRightLabel.setEnabled(false);
				edgeWarpUpLabel.setEnabled(false);
				edgeWarpDownLabel.setEnabled(false);
				edgeWarpLeftCheck = new JCheckBox(" ");
				edgeWarpRightCheck = new JCheckBox(" ");
				edgeWarpUpCheck = new JCheckBox(" ");
				edgeWarpDownCheck = new JCheckBox(" ");
				edgeWarpLeftInput = new JTextField(3);
				edgeWarpRightInput = new JTextField(3);
				edgeWarpUpInput = new JTextField(3);
				edgeWarpDownInput = new JTextField(3);
				edgeWarpLeftInput.setEnabled(false);
				edgeWarpRightInput.setEnabled(false);
				edgeWarpUpInput.setEnabled(false);
				edgeWarpDownInput.setEnabled(false);
				setEdgeWarps = new JButton("Set Edge Warps");
				edgeWarpPanel.add(edgeWarpLeftCheck);
				edgeWarpPanel.add(edgeWarpLeftLabel);
				edgeWarpPanel.add(edgeWarpLeftInput);
				edgeWarpPanel.add(edgeWarpRightCheck);
				edgeWarpPanel.add(edgeWarpRightLabel);
				edgeWarpPanel.add(edgeWarpRightInput);
				edgeWarpPanel.add(edgeWarpUpCheck);
				edgeWarpPanel.add(edgeWarpUpLabel);
				edgeWarpPanel.add(edgeWarpUpInput);
				edgeWarpPanel.add(edgeWarpDownCheck);
				edgeWarpPanel.add(edgeWarpDownLabel);
				edgeWarpPanel.add(edgeWarpDownInput);
				edgeWarpPanel.add(setEdgeWarps);
				edgeWarpFrame.add(edgeWarpPanel);
	}
	
	public void initializeEditModeMenu() {
		//edit mode menu
				editMenuFrame = new JFrame();
				editMenuFrame.setSize(240, 825);
				editMenuFrame.setTitle("Edit Mode");
				editMenuFrame.setResizable(false);
				editMenuFrame.setLocation(TOOLBOX_WIDTH + 10 + gameWidth + 30 + 10, 0);
				editMenuPanel = new EditMenuPanel();
				editMenuX1Label = new JLabel("       Top Left X:");
				editMenuY1Label = new JLabel("       Top Left Y:");
				editMenuX2Label = new JLabel("Bottom Right X:");
				editMenuY2Label = new JLabel("Bottom Right Y:");
				editMenuX1Input = new JTextField(3);
				editMenuY1Input = new JTextField(3);
				editMenuX2Input = new JTextField(3);
				editMenuY2Input = new JTextField(3);
				editMenuSetCoords = new JButton("Set Coordinates");
				editMenuDeleteShape = new JButton("Delete Shape");
				editMenuCountLabel = new JLabel("           Shape Number:");
				editMenuCountInput = new JTextField("" + rectCount, 2);
				editMenuCountFraction = new JLabel("/ " + rectCount);
				editMenuCountPlus = new JButton("+");
				editMenuCountSet = new JButton("Set");
				editMenuCountMinus = new JButton("-");
				editMenuSendToBack = new JButton("Send To Back");
				editMenuBringToFront = new JButton("Bring To Front");
				editColorLabel = new JLabel("            Color: ");
				editRedLabel = new JLabel("      Red: ");
				editGreenLabel = new JLabel("   Green: ");
				editBlueLabel = new JLabel("Blue: ");
				editRedField = new JTextField("0", 3);
				editGreenField = new JTextField("0", 3);
				editBlueField = new JTextField("0", 3);
				editColorButton = new JButton("Change Color");
				editRedVal = 0;
				editGreenVal = 0;
				editBlueVal = 0;
				editMenuPanel.add(editMenuX1Label);
				editMenuPanel.add(editMenuX1Input);
				editMenuPanel.add(editMenuY1Label);
				editMenuPanel.add(editMenuY1Input);
				editMenuPanel.add(editMenuX2Label);
				editMenuPanel.add(editMenuX2Input);
				editMenuPanel.add(editMenuY2Label);
				editMenuPanel.add(editMenuY2Input);
				editMenuPanel.add(editMenuSetCoords);
				editMenuPanel.add(editColorLabel);
				editMenuPanel.add(editRedLabel);
				editMenuPanel.add(editRedField);
				editMenuPanel.add(editGreenLabel);
				editMenuPanel.add(editGreenField);
				editMenuPanel.add(editBlueLabel);
				editMenuPanel.add(editBlueField);
				editMenuPanel.add(editColorButton);
				editCollisLabel = new JLabel("               Collision:");
				editSetCollisZero = new JRadioButton("Off");
				editSetCollisOne = new JRadioButton("On", true);
				ButtonGroup editCollisButtons = new ButtonGroup();
				editCollisButtons.add(editSetCollisZero);
				editCollisButtons.add(editSetCollisOne);
				editMenuPanel.add(editCollisLabel);
				editMenuFrame.add(editMenuPanel);
				editMenuPanel.add(editSetCollisOne);
				editMenuPanel.add(editSetCollisZero);
				editClimbableLabel = new JLabel("Climbable:");
				editCollisClimbable = new JCheckBox();
				editMenuPanel.add(editClimbableLabel);
				editMenuPanel.add(editCollisClimbable);
				editWaterLabel = new JLabel("Watery: ");
				editCollisWater = new JCheckBox();
				editMenuPanel.add(editWaterLabel);
				editMenuPanel.add(editCollisWater);
				editBlockLayer = new JLabel("Block Layering: ");
				editLayerFront = new JRadioButton("In Front Of Sprite");
				editLayerBack = new JRadioButton("Behind Sprite", true);
				ButtonGroup editLayerGroup = new ButtonGroup();
				editLayerGroup.add(editLayerFront);
				editLayerGroup.add(editLayerBack);
				editCollisWater.setEnabled(false);
				editWaterLabel.setEnabled(false);
				editBlockLayer.setEnabled(false);
				editLayerFront.setEnabled(false);
				editLayerBack.setEnabled(false);
				editClimbableTypeLabel = new JLabel("Climb Type:");
				editClimbableJump = new JRadioButton("Jump");
				editClimbableLadder = new JRadioButton("Ladder");
				editClimbableTypeLabel.setEnabled(false);
				editClimbableJump.setEnabled(false);
				editClimbableLadder.setEnabled(false);
				ButtonGroup editClimbabilityTypeGroup = new ButtonGroup();
				editClimbabilityTypeGroup.add(editClimbableJump);
				editClimbabilityTypeGroup.add(editClimbableLadder);
				editClimbableLadder.setSelected(true);
				editChangeCollis = new JButton("Set Attributes");
				editMenuPanel.add(editClimbableTypeLabel);
				editMenuPanel.add(editClimbableJump);
				editMenuPanel.add(editClimbableLadder);
				editMenuPanel.add(editBlockLayer);
				editMenuPanel.add(editLayerFront);
				editMenuPanel.add(editLayerBack);
				editMenuPanel.add(editChangeCollis);
				
				//edit menu: warp stuff
				editWarpOn = new JCheckBox("");
				editWarpOnLabel = new JLabel("                 Warp:");
				editWarpMap = new JTextField(3);
				editWarpX = new JTextField(3);
				editWarpY = new JTextField(3);
				editWarpButton = new JButton("Set Warp Properties");
				editWarpMapLabel = new JLabel("            To Map #:");
				editWarpXLabel = new JLabel("                   To X:");
				editWarpYLabel = new JLabel("              To Y:");
				editScrollThruWarps = new JCheckBox();
				editScrollThruWarpsLabel = new JLabel("Scroll Through Warps Only:");
				editWarpTotalLabel = new JLabel("(" + warpCount + " total)");
				editMenuPanel.add(editWarpOnLabel);
				editMenuPanel.add(editWarpOn);
				editMenuPanel.add(editWarpTotalLabel);
				editMenuPanel.add(editWarpMapLabel);
				editMenuPanel.add(editWarpMap);
				editMenuPanel.add(editWarpXLabel);
				editMenuPanel.add(editWarpX);
				editMenuPanel.add(editWarpYLabel);
				editMenuPanel.add(editWarpY);
				editMenuPanel.add(editWarpButton);
				editMenuPanel.add(editScrollThruWarpsLabel);
				editMenuPanel.add(editScrollThruWarps);
				
				//bottom section of edit menu:
				editMenuPanel.add(editMenuCountLabel);
				editMenuPanel.add(editMenuCountInput);
				editMenuPanel.add(editMenuCountFraction);
				editMenuPanel.add(editMenuCountMinus);
				editMenuPanel.add(editMenuCountSet);
				editMenuPanel.add(editMenuCountPlus);
				editMenuPanel.add(editMenuSendToBack);
				editMenuPanel.add(editMenuBringToFront);
				editMenuPanel.add(editMenuDeleteShape);
				editMenuFrame.add(editMenuPanel);
				adjustEditMenu();
	}
	
	public void initializeLoadMenu() {
		//load menu
				loadFrame = new JFrame();
				loadFrame.setSize(150, 100);
				loadFrame.setTitle("Map #");
				loadFrame.setResizable(false);
				loadPanel = new JPanel();
				loadLabel = new JLabel("Map Number:");
				loadInput = new JTextField("" + map, 3);
				loadMap = new JButton("Load Map");
				loadPanel.add(loadLabel);
				loadPanel.add(loadInput);
				loadPanel.add(loadMap);
				loadFrame.add(loadPanel);
	}
	
	public void initializeInMapWarpDrawMenu() {
		//warp draw menu
				warpDrawFrame = new JFrame();
				warpDrawFrame.setSize(150, 150);
				warpDrawFrame.setLocation(TOOLBOX_WIDTH + 10 + gameWidth + 30 + 10, 0);
				warpDrawFrame.setTitle("Warp");
				warpDrawFrame.setResizable(false);
				warpDrawPanel = new JPanel();
				warpToMapLabel = new JLabel("Warp To Map:");
				warpToMapInput = new JTextField(3);
				warpToXLabel = new JLabel("Warp To X:");
				warpToXInput = new JTextField(3);
				warpToYLabel = new JLabel("Warp To Y:");
				warpToYInput = new JTextField(3);
				setWarpDraw = new JButton("Set Warp Draw");
				warpDrawPanel.add(warpToMapLabel);
				warpDrawPanel.add(warpToMapInput);
				warpDrawPanel.add(warpToXLabel);
				warpDrawPanel.add(warpToXInput);
				warpDrawPanel.add(warpToYLabel);
				warpDrawPanel.add(warpToYInput);
				warpDrawPanel.add(setWarpDraw);
				warpDrawFrame.add(warpDrawPanel);
	}
	
	//	public void addCustomCursor() {
	//		//add a custom cursor
	//		Toolkit toolkit = Toolkit.getDefaultToolkit();
	//		Image image = toolkit.getImage("cursor.png");
	//		Cursor c = toolkit.createCustomCursor(image, new Point(panel.getX(), panel.getY()), "img");
	//		panel.setCursor(c);
	//	}
	
	public void initializeListeners() {
		save.addActionListener(getSaveButtonListener());
		undo.addActionListener(getUndoButtonListener());		
		charPreviewMode.addActionListener(getPlayerPreviewDrawModeButtonListener());
		warpMode.addActionListener(getWarpDrawModeButtonListener());
		rectangleMode.addActionListener(getRectangleDrawModeButtonListener());
		ovalMode.addActionListener(getOvalDrawModeButtonListener());
		editMode.addActionListener(getEditDrawModeButtonListener());
		setWarpDraw.addActionListener(getSetWarpSettingsButtonListener());
		editScrollThruWarps.addItemListener(getEditScrollThruWarpsOnlyCheckboxListener());
		editWarpButton.addActionListener(getEditWarpsSetButtonListener());
		editMenuSetCoords.addActionListener(getEditMenuSetCoordsListener());
		editMenuDeleteShape.addActionListener(getEditMenuDeleteListener());
		this.addKeyListener(getKeyShortcutsListener());

		/*
		 * Add KeyShortcuts KeyListener to every component in the editMenuFrame,
		 * for convenience
		 */
		editMenuFrame.addKeyListener(getKeyShortcutsListener());
		for(Component index : getAllComponents(editMenuFrame)){
			index.addKeyListener(getKeyShortcutsListener());
		}

		editMenuCountSet.addActionListener(getEditMenuSetCountListener());
		editMenuCountPlus.addActionListener(getEditMenuPlusListener());
		editMenuCountMinus.addActionListener(getEditMenuMinusListener());
		editMenuSendToBack.addActionListener(getEditMenuSendToBackListener());
		editMenuBringToFront.addActionListener(EditMenuSendToFrontListener());
		edgeWarps.addActionListener(getEdgeWarpsMenuListener());
		newMap.addActionListener(getNewMapMenuListener());
		load.addActionListener(getLoadMenuListener());
		loadMap.addActionListener(getloadMapButtonListener());
		gameDimensions.addActionListener(getMapDimensionsMenuListener());
		mapNum.addActionListener(getMapNumMenuListener());
		setMapNum.addActionListener(getSetMapNumButtonListener());
		setSpawn.addActionListener(getSetSpawnButtonListener());
		spawnPoint.addActionListener(getSpawnMenuListener());
		setDimensions.addActionListener(getSetDimensionsButtonListener());
		setCollisZero.addActionListener(getSetCollisButtonZero());
		setCollisOne.addActionListener(getSetCollisButtonOne());
		layerFront.addActionListener(getLayerButtonFront());
		layerBack.addActionListener(getLayerButtonBack());
		collisClimbable.addItemListener(getSetClimbableListener());
		setClimbableJump.addActionListener(getSetClimbableJumpListener());
		setClimbableLadder.addActionListener(getSetClimbableLadderListener());		
		collisWater.addItemListener(getSetWaterListener());
		colorButton.addActionListener(getSetColorListener());
		editColorButton.addActionListener(getEditSetColorListener());
		editSetCollisZero.addActionListener(getEditCollisButtonZero());
		editSetCollisOne.addActionListener(getEditCollisButtonOne());		
		editLayerFront.addActionListener(getEditLayerButtonFront());
		editLayerBack.addActionListener(getEditLayerButtonBack());
		editCollisClimbable.addItemListener(getEditClimbableListener());
		editCollisWater.addItemListener(getEditWaterListener());		
		editClimbableJump.addActionListener(getEditClimbableJumpListener());
		editClimbableLadder.addActionListener(getEditClimbableLadderListener());
		editChangeCollis.addActionListener(getEditSetAttributesButtonListener());		
		edgeWarpLeftCheck.addItemListener(getLeftEdgeWarpCheck());
		edgeWarpRightCheck.addItemListener(getRightEdgeWarpCheck());
		edgeWarpUpCheck.addItemListener(getUpEdgeWarpCheck());
		edgeWarpDownCheck.addItemListener(getDownEdgeWarpCheck());		
		setEdgeWarps.addActionListener(getEdgeWarpsButtonListener());
		toolBoxPanel.addMouseListener(getColorChooserListener());
		colorChooserOK.addActionListener(getColorChooserOKListener());
		colorChooserCancel.addActionListener(getColorChooserCancelListener());
		this.addMouseListener(getMouseEventsListener());
		this.addMouseMotionListener(getMouseMoveTempShapeListener());
	}
	
	private ActionListener getSaveButtonListener() {
		/**
		 * when you click file --> save within the toolbox, this 
		 * action listener calls the save() method. this button won't
		 * be active unless you have already set the map number 
		 * in toolbox-->file-->set map number, or if you 
		 * loaded a map.
		 * @author adamcogen
		 *
		 */
		class saveButtonListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				save();	
			}
		}
		return new saveButtonListener();
	}
	
	private ActionListener getUndoButtonListener() {
		/**
		 * when you click file-->undo, the undo method is called.
		 * this is not a fully functioning undo, as all it can do is
		 * delete the last shape in the file / on the map. this
		 * is useful if you make a dumb mistake and would rather
		 * redraw a shape than go all the way to edit mode to delete it
		 * or fix the mistake, but this feature isn't good for much else.
		 * if there are no shapes left, the undo button becomes unavailable.
		 * @author adamcogen
		 *
		 */
		class UndoButtonListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				undo();
				if(rectList.isEmpty()){
					undo.setEnabled(false);
				}
				selectionMade = false;
			}
		}
		return new UndoButtonListener();
	}

	private ActionListener getPlayerPreviewDrawModeButtonListener() {
		/**
		 * when you click toolbox-->edit-->drawmode-->sprite preview,
		 * this action listener is called, it deactivates buttons in the
		 * toolbox that won't be needed, and it sets the drawtype to the proper
		 * value, so that other areas of the program (paintComponent(), 
		 * Mouse Motion Listener, etc) know what to do.
		 * @author adamcogen
		 *
		 */

		class CharPreviewDrawModeListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				drawType = 1;
				collisLabel.setEnabled(false);
				setCollisZero.setEnabled(false);

				setCollisOne.setEnabled(false);
				climbableLabel.setEnabled(false);
				waterLabel.setEnabled(false);
				collisClimbable.setEnabled(false);
				collisWater.setEnabled(false);
				blockLayer.setEnabled(false);
				layerFront.setEnabled(false);
				layerBack.setEnabled(false);
				setClimbableJump.setEnabled(false);
				setClimbableLadder.setEnabled(false);
				climbableTypeLabel.setEnabled(false);
				//System.out.println(drawType);
				editMenuFrame.setVisible(false);
				warpDrawFrame.setVisible(false);
				panel.repaint();
			}
		}
		return new CharPreviewDrawModeListener();
	}
	
	private ActionListener getWarpDrawModeButtonListener() {
		/**
		 * when you click toolbox-->edit-->drawmode-->warp mode,
		 * this action listener is called, it deactivates buttons in the
		 * toolbox that won't be needed, and it sets the drawtype to the proper
		 * value, so that other areas of the program (paintComponent(), 
		 * Mouse Motion Listener, etc) know what to do.
		 * @author adamcogen
		 *
		 */

		class WarpDrawModeListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				drawType = 4;
				setCollisZero.setSelected(true);
				collisLabel.setEnabled(false);
				setCollisZero.setEnabled(false);
				setCollisOne.setEnabled(false);
				climbableLabel.setEnabled(false);
				waterLabel.setEnabled(false);
				collisClimbable.setEnabled(false);
				collisWater.setEnabled(false);
				blockLayer.setEnabled(false);
				layerFront.setEnabled(false);
				layerBack.setEnabled(false);
				setClimbableJump.setEnabled(false);
				setClimbableLadder.setEnabled(false);
				climbableTypeLabel.setEnabled(false);
				//System.out.println(drawType);
				editMenuFrame.setVisible(false);

				collisVal = 5;
				climbable = 10 + warpCount;

				warpDrawFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				warpDrawFrame.setVisible(true);
				panel.repaint();
			}
		}
		return new WarpDrawModeListener();
	}
	
	private ActionListener getRectangleDrawModeButtonListener() {
		/**
		 * action listener for toolbox-->edit-->drawmode-->rectangle.
		 * activates the necessary buttons in the toolbox, sets drawtype
		 * to 0. this is the default draw setting and the most versatile.
		 * @author adamcogen
		 *
		 */
		class RectangleDrawModeListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println("wtf");
				drawType = 0;
				collisLabel.setEnabled(true);
				setCollisZero.setEnabled(true);
				setCollisOne.setEnabled(true);
				climbableLabel.setEnabled(true);
				collisClimbable.setEnabled(true);
				if (collisVal == 0 || collisVal == 2){
					blockLayer.setEnabled(true);
					layerFront.setEnabled(true);
					layerBack.setEnabled(true);
				}
				if(collisClimbable.isSelected()){
					//climbable = 1;
					collisWater.setEnabled(true);
					waterLabel.setEnabled(true);
					setClimbableJump.setEnabled(true);
					setClimbableLadder.setEnabled(true);
					climbableTypeLabel.setEnabled(true);
					if(setClimbableLadder.isSelected()){
						climbable = 1;
					}
					if(setClimbableJump.isSelected()){
						climbable = 3;
					}
					if(collisWater.isSelected()){
						climbable = 2;
					}
				} else {
					climbable = 0;
				}
				if(setCollisZero.isSelected() && layerFront.isSelected()){
					collisVal = 2;
				} else {
					collisVal = 0;
				}
				if (setCollisOne.isSelected()){
					collisVal = 1;
				}
				editMenuFrame.setVisible(false);
				warpDrawFrame.setVisible(false);
				panel.repaint();
				//System.out.println(climbable);
				//System.out.println(collisVal);
			}
		}
		return new RectangleDrawModeListener();
	}
	
	private ActionListener getOvalDrawModeButtonListener() {
		/**
		 * action listener for toolbox-->edit-->drawmode-->oval.
		 * deactivates the necessary buttons in the toolbox, sets drawtype
		 * to 2. oval is limited, and cannot have collision, although 
		 * rectangles can be drawn behind ovals to give the illusion that
		 * the ovals do have collision.
		 * @author adamcogen
		 *
		 */
		class OvalDrawModeListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				drawType = 2;
				collisLabel.setEnabled(true);
				setCollisZero.setEnabled(true);

				setCollisOne.setEnabled(false);
				setCollisZero.setSelected(true);
				climbableLabel.setEnabled(false);
				waterLabel.setEnabled(false);
				collisClimbable.setEnabled(false);
				collisWater.setEnabled(false);
				blockLayer.setEnabled(true);
				layerFront.setEnabled(true);
				layerBack.setEnabled(true);
				setClimbableJump.setEnabled(false);
				setClimbableLadder.setEnabled(false);
				climbableTypeLabel.setEnabled(false);
				climbable = 0;
				if(layerFront.isSelected()){
					collisVal = 4;
				} else {
					collisVal = 3;
				}
				editMenuFrame.setVisible(false);
				warpDrawFrame.setVisible(false);
				//System.out.println(climbable);
				//System.out.println(collisVal);
				panel.repaint();
			}
		}
		return new OvalDrawModeListener();
	}
	
	private ActionListener getEditDrawModeButtonListener() {
		/**
		 * action listener for toolbox --> edit --> drawmode --> edit mode. 
		 * this useful draw mode lets you delete stuff and edit
		 * any attributes of existing shapes. sets drawtype to 3, disables
		 * unnecessary buttons, and reveals the edit mode window.
		 * @author adamcogen
		 *
		 */
		class EditDrawModeListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println("ya..");
				drawType = 3;
				collisLabel.setEnabled(false);
				setCollisZero.setEnabled(false);

				setCollisOne.setEnabled(false);
				climbableLabel.setEnabled(false);
				waterLabel.setEnabled(false);
				collisClimbable.setEnabled(false);
				collisWater.setEnabled(false);
				blockLayer.setEnabled(false);
				layerFront.setEnabled(false);
				layerBack.setEnabled(false);
				setClimbableJump.setEnabled(false);
				setClimbableLadder.setEnabled(false);
				climbableTypeLabel.setEnabled(false);


				adjustEditMenu();
				editMenuFrame.setVisible(true);
				warpDrawFrame.setVisible(false);
				editMenuFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				panel.repaint();
			}
		}
		return new EditDrawModeListener();
	}
	
	private ActionListener getSetWarpSettingsButtonListener() {
		/**
		 * action listener for the "set warp settings" button in
		 * edit --> drawmode --> warp mode menu. sets the destination
		 * of any warp that will be drawn.
		 * @author adamcogen
		 *
		 */
		class WarpModeSetButtonListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				warpToMapNum = Integer.parseInt(warpToMapInput.getText());
				warpToXCoord = Integer.parseInt(warpToXInput.getText());
				warpToYCoord = Integer.parseInt(warpToYInput.getText());
			}
		}
		return new WarpModeSetButtonListener();
	}
	
	private ItemListener getEditScrollThruWarpsOnlyCheckboxListener() {
		/**
		 * this item listener is for the "scroll through warps only" checkbox 
		 * in the edit menu. checking this makes it so that the "shape number"
		 * set, + and - buttons, and the shape count fraction are changed to 
		 * only scroll through and show warps. this is for convenience; since
		 * warps are so powerful, they should be easy to find and edit,
		 * even if they are small and there are many other shapes.
		 * @author adamcogen
		 *
		 */
		class EditScrollThruWarpsOnlyCheckListener implements ItemListener{

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED){
					scrollWarpsOnly = true;
				} else {
					scrollWarpsOnly = false;
				}
				adjustEditMenu();
			}

		}
		return new EditScrollThruWarpsOnlyCheckListener();
	}
	
	private ActionListener getEditWarpsSetButtonListener() {
		/**
		 * button listener for applying changes to a warp from within the edit mode menu
		 */
		class EditWarpSetButtonListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int newWarpMapNum = Integer.parseInt(editWarpMap.getText());
				int newWarpX = Integer.parseInt(editWarpX.getText());
				int newWarpY = Integer.parseInt(editWarpY.getText());
				int tempWarpNum = ((int) rectList.get(((editMenuCountVal - 1) * 9) + 5) - 10);
				//int index = (int) warpList.get(warpNum * 3);
				warpList.set((tempWarpNum * 3), newWarpMapNum);
				warpList.set((tempWarpNum * 3) + 1, newWarpX);
				warpList.set((tempWarpNum * 3) + 2, newWarpY);
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditWarpSetButtonListener();
	}
	
	private ActionListener getEditMenuSetCoordsListener() {
		/**
		 * this is the action listener for the button on the edit menu 
		 * that allows you to change the coordinates of an existing shape.
		 * @author adamcogen
		 *
		 */
		class EditMenuSetCoordsListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int newX1 = Integer.parseInt(editMenuX1Input.getText()) + 14;
				int newY1 = Integer.parseInt(editMenuY1Input.getText()) + 15;
				int newWidth = Integer.parseInt(editMenuX2Input.getText()) - newX1 + 15;
				int newHeight = Integer.parseInt(editMenuY2Input.getText()) - newY1 + 15;
				int index = (editMenuCountVal - 1) * 9;

				//adjust any shape values as necessary based on which new corner is which

				/*
				 *  newRect width < 0, newRect height > 0: bottom left
				 * x = x - width 
				 * y = y
				 * width = -width
				 * height = height
				 * 
				 */
				if(newWidth < 0 && newHeight > 0){
					newX1 = newX1 + newWidth;
					newWidth = -newWidth;
					/*
					 * newRect width < 0, newRect height < 0: top left
					 * x = x - width 
					 * y = y - height
					 * width = -width
					 * height = -height
					 * 
					 */
				} else if (newWidth < 0 && newHeight < 0){
					newX1 = newX1 + newWidth;
					newWidth = -newWidth;
					newY1 = newY1 + newHeight;
					newHeight = -newHeight;
					/*
					 * newRect width > 0, newRect height < 0: top right
					 * x = x
					 * y = y - height
					 * width = width
					 * height = -height
					 * 
					 */
				} else if (newWidth > 0 && newHeight < 0){
					newY1 = newY1 + newHeight;
					newHeight = -newHeight;
					/*
					 * newRect width > 0, newRect height > 0: bottom left
					 * x = x
					 * y = y
					 * width = width
					 * height = height
					 * This is the default case, no changes need to be made to any values before drawing shape
					 */ 
				}


				rectList.set(index, newX1);
				rectList.set(index + 1, newY1);
				rectList.set(index + 2, newWidth);
				rectList.set(index + 3, newHeight);
				selectionX = newX1;
				selectionY = newY1;
				selectionHeight = newHeight;
				selectionWidth = newWidth;
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuSetCoordsListener();
	}
	
	private ActionListener getEditMenuDeleteListener() {
		/**
		 * this is the action listener for the button on the edit menu
		 * that allows you to delete existing shapes. this button deletes 
		 * all of the selected shape's data from the shape array..permanently.
		 * @author adamcogen
		 *
		 */
		class EditMenuDeleteListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				if ((int) rectList.get(index + 4) == 5){
					//System.out.println(rectList.get(index + 5));
					int tempWarpNum = (int) rectList.get(index + 5) - 10;
					//System.out.println(tempWarpNum);
					for (int i = 0; i < 3; i++){
						warpList.remove(tempWarpNum * 3);
					}
					warpCount--;
					adjustWarpData(tempWarpNum + 10);
				}
				for (int i = 0; i < 9; i++){
					rectList.remove(index);
				}
				rectCount--;
				selectionMade = false;
				editMenuCountFraction.setText("/ " + rectCount);
				int currentSetNum = Integer.parseInt(editMenuCountInput.getText());
				if(currentSetNum == rectCount + 1){
					editMenuCountInput.setText("" + (currentSetNum - 1));
				}
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuDeleteListener();
	}
	
	private KeyListener getKeyShortcutsListener() {
		/**
		 * 
		 * KeyListener to check for clicks to the delete key, 
		 * which deletes the selected shape in edit mode,
		 * and plus and minus keys, which increment the 
		 * currently selected shape number
		 * 
		 * @author adamcogen
		 *
		 */
		class KeyShortcuts implements KeyListener{

			@Override
			public void keyTyped(KeyEvent e) {
				//nothin

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE){
					//System.out.println("ya");
					editMenuDeleteShape.doClick();
				}
				if (e.getKeyCode() == KeyEvent.VK_EQUALS || e.getKeyCode() == KeyEvent.VK_PLUS){
					//System.out.println("ya");
					editMenuCountPlus.doClick();
				}
				if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_UNDERSCORE){
					//System.out.println("ya");
					editMenuCountMinus.doClick();
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				//nothin

			}

		}
		return new KeyShortcuts();
	}

	private ActionListener getEditMenuSetCountListener() {
		/**
		 * this action listener is for the "set" button at the bottom 
		 * that allows you to choose a shape number to select and 
		 * jump right to it.
		 * @author adamcogen
		 *
		 */
		class EditMenuSetCountListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println(rectCount * 9);
				int tempCountVal = Integer.parseInt(editMenuCountInput.getText());
				if (tempCountVal <= rectCount && tempCountVal > 0){
					//System.out.println(tempCountVal);
					editMenuCountVal = tempCountVal;
				} else if (tempCountVal > rectCount){
					editMenuCountVal = rectCount;
					editMenuCountInput.setText("" + editMenuCountVal);
				} else if (tempCountVal <= 0){
					editMenuCountVal = 1;
					editMenuCountInput.setText("" + editMenuCountVal);
					//selectionMade = false;
				}

				//				if(!scrollWarpsOnly){
				for (int i = rectCount - 1; i >= editMenuCountVal - 1; i--){
					int leftX = (int) rectList.get((i*9));
					int rightX = leftX + (int) rectList.get((i*9) + 2);
					int topY = (int) rectList.get((i*9)+1);
					int bottomY = topY + (int) rectList.get((i*9) + 3);
					//selectX selectY selectWidth selectHeight
					//System.out.println("shape number " + i);
					selectionMade = true;
					selectionType = (int) rectList.get((i*9) + 4);
					selectionX = leftX;
					selectionY = topY;
					selectionWidth = rightX - leftX;
					selectionHeight = bottomY - topY;
					editMenuX1Input.setText("" + (leftX - 14));
					editMenuY1Input.setText("" + (topY - 15));
					editMenuX2Input.setText("" + (rightX - 15));
					editMenuY2Input.setText("" + (bottomY - 15));
				}
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuSetCountListener();
	}

	private ActionListener getEditMenuPlusListener() {
	/**
	 * this is the action listener for the plus button that allows you
	 * to increase which shape number you have selected.
	 * @author adamcogen
	 *
	 */
	class EditMenuPlusListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(!scrollWarpsOnly){
				if (editMenuCountVal < rectCount){
					editMenuCountVal++;
					editMenuCountInput.setText("" + editMenuCountVal);
				}
			} else {
				//determine the index of the next warp rectangle
				//then go to it
				getNextWarp: for (int i = editMenuCountVal; i < rectCount; i++){
					//System.out.println(i);
					if((int) rectList.get((i * 9) + 4) == 5){
						editMenuCountVal = i + 1;
						//System.out.println(editMenuCountVal);
						editMenuCountInput.setText("" + editMenuCountVal);
						break getNextWarp;
					}
				}
			}

			checkAll: for (int i = rectCount - 1; i >= editMenuCountVal - 1; i--){
				int leftX = (int) rectList.get((i*9));
				int rightX = leftX + (int) rectList.get((i*9) + 2);
				int topY = (int) rectList.get((i*9)+1);
				int bottomY = topY + (int) rectList.get((i*9) + 3);
				//selectX selectY selectWidth selectHeight
				//System.out.println("shape number " + i);
				selectionMade = true;
				selectionType = (int) rectList.get((i*9) + 4);
				selectionX = leftX;
				selectionY = topY;
				selectionWidth = rightX - leftX;
				selectionHeight = bottomY - topY;
				editMenuX1Input.setText("" + (leftX - 14));
				editMenuY1Input.setText("" + (topY - 15));
				editMenuX2Input.setText("" + (rightX - 15));
				editMenuY2Input.setText("" + (bottomY - 15));
			}
			adjustEditMenu();
			panel.repaint();
		}
	}
		return new EditMenuPlusListener();
	}
	
	private ActionListener getEditMenuMinusListener() {
		/**
		 * this is the action listener for the minus button that allows you
		 * to decrease which shape number you have selected.
		 * @author adamcogen
		 *
		 */
		class EditMenuMinusListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!scrollWarpsOnly){
					if (editMenuCountVal > 1){
						editMenuCountVal--;
						editMenuCountInput.setText("" + editMenuCountVal);
					}
				} else {
					getPrevWarp: for (int i = editMenuCountVal - 2; i >= 0; i--){
						//System.out.println("-loop: " + i);
						if((int) rectList.get((i * 9) + 4) == 5){
							editMenuCountVal = i + 1;
							//System.out.println("-value: " + editMenuCountVal);
							editMenuCountInput.setText("" + editMenuCountVal);
							//editMenuCountVal--;
							//if((int) rectList.get((i * 9) + 4) == 5){
							break getPrevWarp;
							//}
						}
					}
				}
				checkAll: for (int i = rectCount - 1; i >= editMenuCountVal - 1; i--){
					int leftX = (int) rectList.get((i*9));
					int rightX = leftX + (int) rectList.get((i*9) + 2);
					int topY = (int) rectList.get((i*9)+1);
					int bottomY = topY + (int) rectList.get((i*9) + 3);
					//selectX selectY selectWidth selectHeight
					//System.out.println("shape number " + i);
					selectionMade = true;
					selectionType = (int) rectList.get((i*9) + 4);
					selectionX = leftX;
					selectionY = topY;
					selectionWidth = rightX - leftX;
					selectionHeight = bottomY - topY;
					editMenuX1Input.setText("" + (leftX - 14));
					editMenuY1Input.setText("" + (topY - 15));
					editMenuX2Input.setText("" + (rightX - 15));
					editMenuY2Input.setText("" + (bottomY - 15));
				}
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuMinusListener();
	}
	
	private ActionListener getEditMenuSendToBackListener() {
		/**
		 * this is the action listener for the "send to back" button on the 
		 * edit window. this button sends all data for the current shape
		 * to the very beginning of the shapes arraylist, moving it
		 * to the back layer of shapes for rendering, i.e. all the other
		 * shapes will now overlap it. 
		 * @author adamcogen
		 *
		 */
		class EditMenuToBackListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				int[] temp = new int[9];
				for(int i = 0; i < 9; i++){
					temp[i] = (int) rectList.get(index);
					//					System.out.println("" + temp[i]);
					//					System.out.println("" + rectList.get(index));
					rectList.remove(index);
				}
				for (int i = 8; i >= 0; i--){
					rectList.add(0, temp[i]);
				}
				editMenuCountInput.setText("" + 1);
				editMenuCountVal = 1;
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuToBackListener();
	}
	
	private ActionListener EditMenuSendToFrontListener() {
		/**
		 * this is the action listener for the "bring to front" button on the 
		 * edit window. this button brings all data for the current shape
		 * to the very end of the shapes arraylist, moving it
		 * to the back layer of shapes for rendering, i.e. it will now
		 * overlap all the other shapes
		 * @author adamcogen
		 *
		 */
		class EditMenuToFrontListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				int[] temp = new int[9];
				for(int i = 0; i < 9; i++){
					temp[i] = (int) rectList.get(index);
					//System.out.println("" + temp[i]);
					//System.out.println("" + rectList.get(index));
					rectList.remove(index);
				}
				for (int i = 0; i < 9; i++){
					rectList.add(temp[i]);
				}
				editMenuCountInput.setText("" + rectCount);
				editMenuCountVal = rectCount;
				adjustEditMenu();
				panel.repaint();
			}
		}
		return new EditMenuToFrontListener();
	}
	
	private ActionListener getEdgeWarpsMenuListener() {
		/**
		 * this is the action listener for toolbox --> edit --> edge warps.
		 * this opens the menu that allows you to change the way different 
		 * mapes are connected to each other.
		 * @author adamcogen
		 *
		 */
		class EdgeWarpsMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				edgeWarpFrame.setVisible(true);
			}
		}
		return new EdgeWarpsMenuListener();
	}
	
	private ActionListener getNewMapMenuListener() {
		/**
		 * this is the action listener for toolbox--> file --> new map.
		 * it calls newMap(), which clears the shapes array,
		 * sets the shape count to 0, and disables saving 
		 * until the map number is set or another map is 
		 * loaded. basically just clears all the rectangles
		 * from the screen.
		 * @author adamcogen
		 *
		 */
		class NewMapMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				newMap();
			}
		}
		return new NewMapMenuListener();
	}
	
	private ActionListener getLoadMenuListener() {
		/**
		 * this is the action listener for toolbox --> file --> load.
		 * it opens the load map window.
		 * @author adamcogen
		 *
		 */
		class LoadMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFrame.setVisible(true);
			}
		}
		return new LoadMenuListener();
	}
	
	private ActionListener getloadMapButtonListener() {
		/**
		 * this is the action listener for the "load map" button 
		 * within the load game window. it calls the load() method,
		 * which basically sets everything to match what is saved in 
		 * the file for whichever map number you enter in the load
		 * map window.
		 * @author adamcogen
		 *
		 */
		class loadMapButtonListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				load();
			}
		}
		return new loadMapButtonListener();
	}
	
	private ActionListener getMapDimensionsMenuListener() {
		/**
		 * this is the action listener for toolbox --> edit --> map dimensions.
		 * it opens a window that lets you change the dimensions of the map.
		 * @author adamcogen
		 *
		 */
		class MapDimensionsMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				dimensionsFrame.setVisible(true);
			}
		}
		return new MapDimensionsMenuListener();
	}
	
	private ActionListener getMapNumMenuListener() {
		/**
		 * this is the action listener for toolbox --> file --> set map number.
		 * if you did not load a map, this must be set before you can save. 
		 * this is to prevent thoughtless accidental overwriting of a default 
		 * map number.
		 * @author adamcogen
		 *
		 */
		class MapNumMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				mapNumFrame.setVisible(true);
			}
		}
		return new MapNumMenuListener();
	}
	
	private ActionListener getSetMapNumButtonListener() {
		/**
		 * this is the action listener for the "set map number"
		 * button in the set map number window. after you click this,
		 * you can save a file that was not loaded.
		 * @author adamcogen
		 *
		 */
		class MapNumButtonListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				map = Integer.parseInt(mapNumInput.getText());
				refreshFrameTitle();
				save.setEnabled(true);
			}
		}
		return new MapNumButtonListener();
	}
	
	private ActionListener getSetSpawnButtonListener() {
		/**
		 * this is the action listener for the button in the spawn point
		 * menu that sets the spawn point to the desired value. 
		 * @author adamcogen
		 *
		 */
		class SpawnButtonListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				spawnX = Integer.parseInt(spawnXInput.getText());
				spawnY = Integer.parseInt(spawnYInput.getText());
				panel.repaint();
			}
		}
		return new SpawnButtonListener();
	}
	
	private ActionListener getSpawnMenuListener() {
		/**
		 * this is the action listener for toolbox --> edit --> spawn point.
		 * makes the spawn frame visible.
		 * @author adamcogen
		 *
		 */
		class SpawnMenuListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				spawnFrame.setVisible(true);
			}
		}
		return new SpawnMenuListener();
	}
	
	private ActionListener getSetDimensionsButtonListener() {
	/**
	 * this is the action listener for the "set new dimensions" button 
	 * in the set dimensions menu. changes the game dimensions and refreshes
	 * the map window accordingly.
	 * @author adamcogen
	 *
	 */
	class DimensionsButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			gameWidth = Integer.parseInt(widthInput.getText());
			gameHeight = Integer.parseInt(heightInput.getText());
			//panel.setPreferredSize(new Dimension(GAME_WIDTH + 30, GAME_HEIGHT + 30));
			refreshFrameSize();
			panel.repaint();
		}
	}
		return new DimensionsButtonListener();
	}
	
	private ActionListener getSetCollisButtonZero() {
		/**
		 * this is the action listener for the button to turn
		 * collision off within the toolbox. 
		 * this button also activates the buttons to change layering.
		 * this is also what will be selected when you are in oval 
		 * drawing mode. this action listener checks to see if you 
		 * are in oval drawing mode, and if you are, it turns off 
		 * and deactivates the necessary buttons / settings.
		 * @author adamcogen
		 *
		 */
		class CollisButtonZero implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				if(drawType == 0){
					if(layerBack.isSelected()){
						collisVal = 0;
					} else {
						collisVal = 2;
					}
				}
				blockLayer.setEnabled(true);
				layerFront.setEnabled(true);
				layerBack.setEnabled(true);
			}
		}
		return new CollisButtonZero();
	}
	
	private ActionListener getSetCollisButtonOne() {
		/**
		 * this is the action listener for the button to turn 
		 * collision on within the toolbox. 
		 * only available in rectangle mode. climbaility still
		 * matters, because you can climb up the side of a 
		 * rectangle with collision turned on. 
		 * this action listener activates the necessary buttons
		 * if they are deactivated and adjusts settings 
		 * as necessary.
		 * @author adamcogen
		 *
		 */
		class CollisButtonOne implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				blockLayer.setEnabled(false);
				layerFront.setEnabled(false);
				layerBack.setEnabled(false);
				collisVal = 1;
			}
		}
		return new CollisButtonOne();
	}
	
	private ActionListener getLayerButtonFront() {
		/**
		 * this is the action listener for the front-layering button
		 * in the toolbox. clicking it causes any shapes with no
		 * collision that are drawn to be rendered in front of the
		 * character in game. however, it should be noted that
		 * these shapes are not automatically rendered in front
		 * of back-layer shapes in the map maker, though they will be 
		 * in game. this can be confusing. try using the "bring to front"
		 * buttons on the edit menu to match the map maker preview
		 * to how the map will appear in game. 
		 * sets collision to either 2 (for front-layer rectangles)
		 * or 4 (for front-layer ovals).
		 * @author adamcogen
		 *
		 */
		class LayerButtonFront implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				if (drawType == 0){
					collisVal = 2;
				} else if (drawType == 2){
					collisVal = 4;
				}
				//System.out.println(collisVal);
			}
		}
		return new LayerButtonFront();
	}
	
	private ActionListener getLayerButtonBack() {
		/**
		 * this is the action listener for the back-layering button
		 * in the toolbox. clicking it causes any shapes with no
		 * collision that are drawn to be rendered behind the
		 * character in game. behind the character is the default setting. 
		 * * sets collision to either 0 (for back-layer rectangles)
		 * or 3 (for back-layer ovals).
		 * @author adamcogen
		 *
		 */
		class LayerButtonBack implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				if (drawType == 0){
					collisVal = 0;
				} else if (drawType == 2){
					collisVal = 3;
				}
			}
		}
		return new LayerButtonBack();
	}
	
	private ItemListener getSetClimbableListener() {
		/**
		 * this is the item listener checks if the rectangle to 
		 * be currently drawn is climbable. if this is checked,
		 * the water setting becomes available.
		 * climbability is not available for ovals.
		 * @author adamcogen
		 *
		 */
		class ClimbableListener implements ItemListener{
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					climbable = 1;
					collisWater.setEnabled(true);
					waterLabel.setEnabled(true);

					if (!collisWater.isSelected()){
						setClimbableJump.setEnabled(true);
						setClimbableLadder.setEnabled(true);
						climbableTypeLabel.setEnabled(true);
						if(setClimbableLadder.isSelected()){
							climbable = 1;
						}
						if(setClimbableJump.isSelected()){
							climbable = 3;
						}
					}

					if (collisWater.isSelected()){
						climbable = 2;
					}
				} else {
					climbable = 0;
					collisWater.setEnabled(false);
					waterLabel.setEnabled(false);
					setClimbableJump.setEnabled(false);
					setClimbableLadder.setEnabled(false);
					climbableTypeLabel.setEnabled(false);
				}
				//System.out.println(climbable);
			}
		}
		return new ClimbableListener();
	}
	
	private ActionListener getSetClimbableJumpListener() {
		/**
		 * action listener for the toolbox Climb Type: Jump button
		 * @author adamcogen
		 *
		 */
		class ClimbableJumpListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				climbable = 3;
			}

		}
		return new ClimbableJumpListener();
	}
	
	private ActionListener getSetClimbableLadderListener() {
		/**
		 * action listener for the toolbox Climb Type: Ladder button
		 * @author adamcogen
		 *
		 */
		class ClimbableLadderListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				climbable = 1;
			}

		}
		return new ClimbableLadderListener();
	}
	
	private ItemListener getSetWaterListener() {
		/**
		 * this is the item listener checks if the rectangle to 
		 * be currently drawn is swimmable / watery. unavailable
		 * unless the climbable check box is selected as well.
		 * climbability and wateryness is not available for ovals.
		 */
		class WaterListener implements ItemListener{
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					climbable = 2;
					setClimbableJump.setEnabled(false);
					setClimbableLadder.setEnabled(false);
					climbableTypeLabel.setEnabled(false);
				} else {
					setClimbableJump.setEnabled(true);
					setClimbableLadder.setEnabled(true);
					climbableTypeLabel.setEnabled(true);
					if(setClimbableLadder.isSelected()){
						climbable = 1;
					}
					if(setClimbableJump.isSelected()){
						climbable = 3;
					}
				}
			}
		}
		return new WaterListener();
	}
	
	private ActionListener getSetColorListener() {
		/**
		 * this action listener is called when you click 
		 * the "set color"  button in the toolbox.
		 * it allows you to set the color 
		 * of shapes or sprite previews that will be 
		 * drawn. it also causes the color preview 
		 * box to change to the selected color.
		 * @author adamcogen
		 *
		 */
		class SetColorListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					redVal = Integer.parseInt(redField.getText());
				} catch (NumberFormatException nfe) {
					redVal = 0;
					redField.setText("0"); 
				}
				try{
					greenVal = Integer.parseInt(greenField.getText());
				} catch (NumberFormatException nfe) {
					greenVal = 0;
					greenField.setText("0");
				} 
				try{
					blueVal = Integer.parseInt(blueField.getText());
				} catch (NumberFormatException nfe) {
					blueVal = 0;
					blueField.setText("0");
				}

				if (redVal > 255){
					redVal = 255;
					redField.setText("255");
				}
				if (redVal < 0){
					redVal = 0;
					redField.setText("0");
				}
				if (blueVal > 255){
					blueVal = 255;
					blueField.setText("255");
				}
				if (blueVal < 0){
					blueVal = 0;
					blueField.setText("0");
				}
				if (greenVal > 255){
					greenVal = 255;
					greenField.setText("255");
				}
				if (greenVal < 0){
					greenVal = 0;
					greenField.setText("0");
				}

				toolBoxPanel.repaint();
			}
		}
		return new SetColorListener();
	}
	
	private ActionListener getEditSetColorListener() {
		/**
		 * this action listener is called when you click 
		 * the "change color"  button in the edit menu.
		 * it allows you to retroactively change the color 
		 * of an existing shape. it also causes
		 * the color preview box to change as the 
		 * color of the selected shape changes.
		 * @author adamcogen
		 *
		 */
		class EditSetColorListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					editRedVal = Integer.parseInt(editRedField.getText());
				} catch (NumberFormatException nfe) {
					editRedVal = 0;
					editRedField.setText("0"); 
				}
				try{
					editGreenVal = Integer.parseInt(editGreenField.getText());
				} catch (NumberFormatException nfe) {
					editGreenVal = 0;
					editGreenField.setText("0");
				} 
				try{
					editBlueVal = Integer.parseInt(editBlueField.getText());
				} catch (NumberFormatException nfe) {
					editBlueVal = 0;
					editBlueField.setText("0");
				}
				if (editRedVal > 255 || editRedVal < 0){
					editRedVal = fixColorRange(editRedVal);
					editRedField.setText("" + editRedVal);	
				}
				if (editBlueVal > 255 || editBlueVal < 0){
					editBlueVal = fixColorRange(editBlueVal);
					editBlueField.setText("" + editBlueVal);	
				}
				if (editGreenVal > 255 || editGreenVal < 0){
					editGreenVal = fixColorRange(editGreenVal);
					editGreenField.setText("" + editGreenVal);	
				}
				int index = (editMenuCountVal - 1) * 9;
				rectList.set(index + 6, editRedVal);
				//System.out.println(rectList.get(ind + 6));
				rectList.set(index + 7, editGreenVal);
				//System.out.println(rectList.get(ind + 7));
				rectList.set(index + 8, editBlueVal);
				//System.out.println(rectList.get(ind + 8));
				editMenuPanel.repaint();
				panel.repaint();
			}
		}
		return new EditSetColorListener();
	}
	
	private ActionListener getEditCollisButtonZero() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the collision 
		 * setting of a rectangle. sets the
		 * rectangle to have no collision.
		 * @author adamcogen
		 *
		 */
		class EditCollisButtonZero implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				if((int) rectList.get(index + 4) == 0 || (int) rectList.get(index + 4) == 1 || (int) rectList.get(index + 4) == 2){
					if(editLayerBack.isSelected()){
						tempCollisVal = 0;
					} else {
						tempCollisVal = 2;
					}
				}
				editBlockLayer.setEnabled(true);
				editLayerFront.setEnabled(true);
				editLayerBack.setEnabled(true);
			}
		}
		return new EditCollisButtonZero();
	}
	
	private ActionListener getEditCollisButtonOne() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the collision 
		 * setting of a rectangle. sets the
		 * rectangle to have collision.
		 * @author adamcogen
		 *
		 */
		class EditCollisButtonOne implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				editBlockLayer.setEnabled(false);
				editLayerFront.setEnabled(false);
				editLayerBack.setEnabled(false);
				tempCollisVal = 1;
			}
		}
		return new EditCollisButtonOne();
	}
	
	private ActionListener getEditLayerButtonFront() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the "layer" 
		 * setting of a shape. sets the
		 * shape to render in front of 
		 * the sprite in-game.
		 * @author adamcogen
		 *
		 */
		class EditLayerButtonFront implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				if (((int) rectList.get(index + 4) == 0) || ((int) rectList.get(index + 4) == 1) || ((int) rectList.get(index + 4) == 2)){
					tempCollisVal = 2;
				} else if (((int) rectList.get(index + 4) == 3) || ((int) rectList.get(index + 4) == 4)){
					tempCollisVal = 4;
				}
				//System.out.println(collisVal);
			}
		}
		return new EditLayerButtonFront();
	}
	
	private ActionListener getEditLayerButtonBack() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the "layer" 
		 * setting of a shape. sets the
		 * shape to render behind the 
		 * sprite in-game.
		 * @author adamcogen
		 *
		 */
		class EditLayerButtonBack implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				if (((int) rectList.get(index + 4) == 0) || ((int) rectList.get(index + 4) == 1) || ((int) rectList.get(index + 4) == 2)){
					tempCollisVal = 0;
				} else if (((int) rectList.get(index + 4) == 3) || ((int) rectList.get(index + 4) == 4)){
					tempCollisVal = 3;
				}
			}
		}
		return new EditLayerButtonBack();
	}
	
	private ItemListener getEditClimbableListener() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the "climbable" 
		 * setting of a rectangle.
		 * @author adamcogen
		 *
		 */
		class EditClimbableListener implements ItemListener{
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					editCollisWater.setEnabled(true);
					editWaterLabel.setEnabled(true);
					editClimbableTypeLabel.setEnabled(true);
					editClimbableJump.setEnabled(true);
					editClimbableLadder.setEnabled(true);
					if (editCollisWater.isSelected()){
						tempClimbable = 2;
						editClimbableTypeLabel.setEnabled(false);
						editClimbableJump.setEnabled(false);
						editClimbableLadder.setEnabled(false);
					}
					if (editClimbableJump.isSelected()){
						tempClimbable = 3;
					} else {
						tempClimbable = 1;
					}
				} else {
					tempClimbable = 0;
					editCollisWater.setEnabled(false);
					editWaterLabel.setEnabled(false);
					editClimbableTypeLabel.setEnabled(false);
					editClimbableJump.setEnabled(false);
					editClimbableLadder.setEnabled(false);
				}
				//System.out.println(climbable);
			}
		}
		return new EditClimbableListener();
	}
	
	private ItemListener getEditWaterListener() {
		/**
		 * this item listener, located within 
		 * the edit window, allows you to 
		 * retroactively change the "water" 
		 * setting of a rectangle.
		 * @author adamcogen
		 *
		 */
		class EditWaterListener implements ItemListener{
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					editClimbableTypeLabel.setEnabled(false);
					editClimbableJump.setEnabled(false);
					editClimbableLadder.setEnabled(false);
					tempClimbable = 2;
				} else {
					editClimbableTypeLabel.setEnabled(true);
					editClimbableJump.setEnabled(true);
					editClimbableLadder.setEnabled(true);
					if (editClimbableJump.isSelected()){
						tempClimbable = 3;
					} else {
						tempClimbable = 1;
					}
				}
				//System.out.println(climbable);
			}
		}
		return new EditWaterListener();
	}
	
	private ActionListener getEditClimbableJumpListener() {
		/**
		 * action listener for edit menu
		 * climb type: jump
		 * button.
		 * @author adamcogen
		 *
		 */
		class EditClimbableJumpListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				tempClimbable = 3;
			}
		}
		return new EditClimbableJumpListener();
	}
	
	private ActionListener getEditClimbableLadderListener() {
		/**
		 * action listener for edit menu
		 * climb type: ladder
		 * button.
		 * @author adamcogen
		 *
		 */
		class EditClimbableLadderListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				tempClimbable = 1;
			}
		}
		return new EditClimbableLadderListener();
	}
	
	private ActionListener getEditSetAttributesButtonListener() {
		/**
		 * this action listener is for the "set attributes" button 
		 * in the edit window. it applies any changes you have made
		 * to shapes regarding collision, climbability, layering, 
		 * etc. in the edit window.
		 * @author adamcogen
		 *
		 */
		class EditSetAttributesButton implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = (editMenuCountVal - 1) * 9;
				if (tempCollisVal == -1){
					tempCollisVal = (int) rectList.get(index + 4);
				}
				//System.out.println(tempCollisVal);
				if (tempClimbable == -1){
					tempClimbable = (int) rectList.get(index + 5);
				}
				rectList.set(index + 4, tempCollisVal);
				if ((int) rectList.get(index + 4) != 3 && ((int) rectList.get(index + 4) != 4)){
					rectList.set(index + 5, tempClimbable);
				} else {
					rectList.set(index + 5, 0);
				}
			}
		}
		return new EditSetAttributesButton();
	}
	
	private ItemListener getLeftEdgeWarpCheck() {
		/**
		 * this item listener checks to see if the map has a 
		 * left side edge warp. if yes, it sets it to whatever
		 * you enter in the box. if no, it sets it to the letter
		 * "n," which the program reads as "no edge warp."
		 * @author adamcogen
		 *
		 */
		class LeftEdgeWarpCheck implements ItemListener{
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					edgeWarpLeftLabel.setEnabled(true);
					edgeWarpLeftInput.setEnabled(true);
				} else {
					edgeWarpLeftLabel.setEnabled(false);
					edgeWarpLeftInput.setEnabled(false);
					//					edgeWarpLeft = "n";
				}
				//System.out.println(edgeWarpLeft);
			}
		}
		return new LeftEdgeWarpCheck();
	}
	
	private ItemListener getRightEdgeWarpCheck() {
		/**
		 * this item listener checks to see if the map has a 
		 * right side edge warp. if yes, it sets it to whatever
		 * you enter in the box. if no, it sets it to the letter
		 * "n," which the program reads as "no edge warp."
		 * @author adamcogen
		 *
		 */
		class RightEdgeWarpCheck implements ItemListener{
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					edgeWarpRightLabel.setEnabled(true);
					edgeWarpRightInput.setEnabled(true);
				} else {
					edgeWarpRightLabel.setEnabled(false);
					edgeWarpRightInput.setEnabled(false);
					//					edgeWarpLeft = "n";
				}
				//System.out.println(edgeWarpLeft);
			}
		}
		return new RightEdgeWarpCheck();
	}
	
	private ItemListener getUpEdgeWarpCheck() {
		/**
		 * this item listener checks to see if the map has a 
		 * upwards edge warp. if yes, it sets it to whatever
		 * you enter in the box. if no, it sets it to the letter
		 * "n," which the program reads as "no edge warp."
		 * @author adamcogen
		 *
		 */
		class UpEdgeWarpCheck implements ItemListener{
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					edgeWarpUpLabel.setEnabled(true);
					edgeWarpUpInput.setEnabled(true);
				} else {
					edgeWarpUpLabel.setEnabled(false);
					edgeWarpUpInput.setEnabled(false);
				}
				//System.out.println(edgeWarpLeft);
			}
		}
		return new UpEdgeWarpCheck();
	}
	
	private ItemListener getDownEdgeWarpCheck() {
		/**
		 * this item listener checks to see if the map has a 
		 * downwards edge warp. if yes, it sets it to whatever
		 * you enter in the box. if no, it sets it to the letter
		 * "n," which the program reads as "no edge warp."
		 * @author adamcogen
		 *
		 */
		class DownEdgeWarpCheck implements ItemListener{
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					edgeWarpDownLabel.setEnabled(true);
					edgeWarpDownInput.setEnabled(true);
				} else {
					edgeWarpDownLabel.setEnabled(false);
					edgeWarpDownInput.setEnabled(false);
				}
				//System.out.println(edgeWarpLeft);
			}
		}
		return new DownEdgeWarpCheck();
	}
	
	private ActionListener getEdgeWarpsButtonListener() {
		/**
		 * this is the action listener for the "set edge warps" button 
		 * in toolbox --> edit --> edge warps. it sets the edge warps 
		 * to whatever you have entered / checked in the edge waprs menu.
		 * @author adamcogen
		 *
		 */
		class EdgeWarpsButtonListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				if (edgeWarpLeftInput.getText().equals("") || !edgeWarpLeftInput.isEnabled()){
					edgeWarpLeft = "n";
				} else {
					edgeWarpLeft = edgeWarpLeftInput.getText();
				}

				if (edgeWarpRightInput.getText().equals("") || !edgeWarpRightInput.isEnabled()){
					edgeWarpRight = "n";
				} else {
					edgeWarpRight = edgeWarpRightInput.getText();
				}

				if (edgeWarpUpInput.getText().equals("") || !edgeWarpUpInput.isEnabled()){
					edgeWarpUp = "n";
				} else {
					edgeWarpUp = edgeWarpUpInput.getText();
				}

				if (edgeWarpDownInput.getText().equals("") || !edgeWarpDownInput.isEnabled()){
					edgeWarpDown = "n";
				} else {
					edgeWarpDown = edgeWarpDownInput.getText();
				}

				//System.out.println("" + edgeWarpLeft + edgeWarpRight + edgeWarpUp + edgeWarpDown);
			}
		}
		return new EdgeWarpsButtonListener();
	}
	
	private MouseListener getColorChooserListener() {
		/**
		 * clicking the color preview box in the toolbox opens a color chooser window.
		 * this mouse listener is added to the toolbox, where it listens for clicks
		 * within the color preview box, and opens the color chooser when they happen.
		 * @author adamcogen
		 *
		 */
		class ColorChooserListener implements MouseListener{

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getX() > 57 && e.getX() < 97 && e.getY() > 70 && e.getY() < 80){
					colorChooserFrame.setVisible(true);
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// nothin
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				// nothin
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// nothin
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// nothin
			}
		}
		return new ColorChooserListener();
	}
	
	private ActionListener getColorChooserOKListener() {
		/**
		 * clicking the color preview box in the toolbox opens a color chooser window.
		 * this action listener takes care of the "choose color" button in the color chooser.
		 * @author adamcogen
		 *
		 */
		class ColorChooserOKListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				redField.setText("" + colorChooser.getColor().getRed());
				greenField.setText("" + colorChooser.getColor().getGreen());
				blueField.setText("" + colorChooser.getColor().getBlue());
				colorChooserFrame.setVisible(false);	
			}

		}
		return new ColorChooserOKListener();
	}
	
	private ActionListener getColorChooserCancelListener() {
		/**
		 * clicking the color preview box in the toolbox opens a color chooser window.
		 * this action listener takes care of the "cancel" button in the color chooser.
		 * @author adamcogen
		 *
		 */
		class ColorChooserCancelListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				colorChooserFrame.setVisible(false);
			}
		}
		return new ColorChooserCancelListener();
	}
	
	private MouseListener getMouseEventsListener() {
		/**
		 * mouse listener to take care of everything in the main map window that
		 * operates on clicks, including sprite previews, shape drawing,
		 * and selection, depending on which draw mode you are in.
		 * @author adamcogen
		 *
		 */
		class MouseEvents implements MouseListener{

			@Override
			public void mouseClicked(MouseEvent e) {
				if(drawType == 0 || drawType == 2 || drawType == 4){
					if (newRectStep == 0){//rectangle drawmode, oval drawmode, warp drawmode
						newRectStep++;
						newRectX = e.getX() - 1;
						newRectY = e.getY() - 25;
						mouseX = e.getX() - 1;
						mouseY = e.getY() - 25;
						panel.repaint();
					} else if (newRectStep == 1){
						newRectStep++;
						newRectWidth = e.getX() - newRectX;
						newRectHeight = e.getY() - 23 - newRectY;
						panel.repaint();
					}
					if(rectList.isEmpty()){
						undo.setEnabled(true);
					}

				} else if(drawType == 1){//sprite drawmode
					permaSpritePreviewColorR = redVal;
					permaSpritePreviewColorG = greenVal;
					permaSpritePreviewColorB = blueVal;
					permaSpritePreviewX = (e.getX() - 5); // - 5 to help center char on the mouse
					permaSpritePreviewY = (e.getY() - 23) + 11; //+ 11 to help center char on the mouse
					panel.repaint();

				} else if(drawType == 3){//edit drawmode
					mouseX = e.getX() - 1;
					mouseY = e.getY() - 25;
					checkAll: for (int i = rectCount - 1; i >= 0; i--){
						int leftX = (int) rectList.get((i*9));
						int rightX = leftX + (int) rectList.get((i*9) + 2);
						int topY = (int) rectList.get((i*9)+1);
						int bottomY = topY + (int) rectList.get((i*9) + 3);
						if (mouseX >= leftX && mouseX <= rightX  && mouseY >= topY && mouseY <= bottomY){
							//selectX selectY selectWidth selectHeight
							//System.out.println("shape number " + i);
							selectionMade = true;
							selectionType = (int) rectList.get((i*9) + 4);
							selectionX = leftX;
							selectionY = topY;
							selectionWidth = rightX - leftX;
							selectionHeight = bottomY - topY;
							editMenuX1Input.setText("" + (leftX - 14));
							editMenuY1Input.setText("" + (topY - 15));
							editMenuX2Input.setText("" + (rightX - 15));
							editMenuY2Input.setText("" + (bottomY - 15));
							editMenuCountVal = i + 1;
							editMenuCountInput.setText("" + editMenuCountVal);
							panel.repaint();
							adjustEditMenu();
							break checkAll;
						}
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// nothing	
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				//nothin	
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				//nothing	
			}
			@Override
			public void mouseExited(MouseEvent e) {
				//nothin	
			}	
		}
		return new MouseEvents();
	}
	
	private MouseMotionListener getMouseMoveTempShapeListener() {
		/**
		 * this mouse motion listener keeps track of any mouse movement,
		 * and uses it to draw temporary shape previews when necessary,
		 * to draw the mouse sprite preview when necessary,
		 * and to update the mouse X and Y coordinates that
		 * are displayed in the toolbox.
		 * @author adamcogen
		 *
		 */
		class MouseMoveTempRect implements MouseMotionListener{

			@Override
			public void mouseDragged(MouseEvent e) {
				//nothin
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				if((drawType == 0 || drawType == 2 || drawType == 4) && newRectStep == 1){
					mouseX = e.getX();
					mouseY = e.getY() - 23;
					panel.repaint();
				}
				if (showXYCoords){
					dispMouseX = e.getX()- 15;
					dispMouseY = e.getY() - 40;
					toolBoxPanel.repaint();
				}
				if(drawType == 1){
					tempSpritePreviewX = (e.getX()) - 5; // - 5 to help center char on the mouse
					tempSpritePreviewY = (e.getY() - 23) + 11; //+ 11 to help center char on the mouse
					panel.repaint();
				}
			}
		}
		return new MouseMoveTempRect();
	}

	/**
	 * the main method. opens the map maker. there are no required parameters.
	 * @param args Ignore this
	 */
	public static void main(String [] args){
		MapMaker mapMaker1 = new MapMaker();
	}

	/**
	 * MapPreviewPanel extends JPanel, and is used for the main map preview window. it takes 
	 * care of all of the shape drawing and that kind of thing. lots of stuff
	 * going on. handles temporary shape rendering for shape previews while drawing,
	 * differentiates between ovals and rectangles, adds data from 
	 * temporary variables to the actual shape arraylist when necessary, shows the
	 * spawn point and sprite preview, draws the border around selected 
	 * shapes in edit mode, etc.
	 *
	 */
	class MapPreviewPanel extends JPanel{
		public void paintComponent(Graphics g){

			//System.out.println(loading);
			if (!loading){
				//don't do any of this if the map maker is in the process of loading a new map

				//check the values of colors entered into the toolbox
				if (redVal > 255 || redVal < 0){
					redVal = fixColorRange(redVal);
					redField.setText("" + redVal);	
				}
				if (blueVal > 255 || blueVal < 0){
					blueVal = fixColorRange(blueVal);
					blueField.setText("" + blueVal);	
				}
				if (greenVal > 255 || greenVal < 0){
					greenVal = fixColorRange(greenVal);
					greenField.setText("" + greenVal);	
				}

				//draw shapes?
				for(int i = 0; rectCount > 0 && i < rectCount; i++){

					int tempRedVal = (int) rectList.get(i* (9) + 6);
					int tempGreenVal = (int) rectList.get(i* (9) + 7);
					int tempBlueVal = (int) rectList.get(i* (9) + 8);

					if (tempRedVal > 255 || tempRedVal < 0){
						tempRedVal = fixColorRange(tempRedVal);	
					}
					if (tempBlueVal > 255 || tempBlueVal < 0){
						tempBlueVal = fixColorRange(tempBlueVal);
					}
					if (tempGreenVal > 255 || tempGreenVal < 0){
						tempGreenVal = fixColorRange(tempGreenVal);
					}

					g.setColor(new Color(tempRedVal, tempGreenVal, tempBlueVal));

					if((int) rectList.get(((i) * 9) + 4) == 0 || (int) rectList.get(((i) * 9) + 4) == 1 || (int) rectList.get(((i) * 9) + 4) == 2 || (int) rectList.get(((i) * 9) + 4) == 5){
						g.fillRect((int) rectList.get(i* (9)), (int) rectList.get(i * (9) + 1), (int) rectList.get(i * (9) + 2), (int) rectList.get(i * (9) + 3));
					} else if ((int) rectList.get(((i) * 9) + 4) == 3 || (int) rectList.get(((i) * 9) + 4) == 4){
						g.fillOval((int) rectList.get(i* (9)), (int) rectList.get(i * (9) + 1), (int) rectList.get(i * (9) + 2), (int) rectList.get(i * (9) + 3));
					}

				}

				g.setColor(new Color(redVal, greenVal, blueVal));

				if (newRectStep == 1){
					//if there has been one click while in a draw mode, render a temporary shape that changes with mouse movements

					//temporary variables for drawing shape previews:
					int tempWidth = mouseX - newRectX;
					int tempHeight = mouseY - newRectY;
					int tempX = newRectX;
					int tempY = newRectY;

					//adjust any shape values as necessary based on mouse position and first click position
					/*
					 *  newRect width < 0, newRect height > 0: bottom left
					 * x = x - width 
					 * y = y
					 * width = -width
					 * height = height
					 * 
					 */
					if(tempWidth < 0 && tempHeight > 0){
						tempX = tempX + tempWidth;
						tempWidth = -tempWidth;
						/*
						 * newRect width < 0, newRect height < 0: top left
						 * x = x - width 
						 * y = y - height
						 * width = -width
						 * height = -height
						 * 
						 */
					} else if (tempWidth < 0 && tempHeight < 0){
						tempX = tempX + tempWidth;
						tempWidth = -tempWidth;
						tempY = tempY + tempHeight;
						tempHeight = -tempHeight;
						/*
						 * newRect width > 0, newRect height < 0: top right
						 * x = x
						 * y = y - height
						 * width = width
						 * height = -height
						 * 
						 */
					} else if (tempWidth > 0 && tempHeight < 0){
						tempY = tempY + tempHeight;
						tempHeight = -tempHeight;
						/*
						 * newRect width > 0, newRect height > 0: bottom left
						 * x = x
						 * y = y
						 * width = width
						 * height = height
						 * This is the default case, no changes need to be made to any values before drawing shape preview
						 */ 
					}


					if(drawType == 0 || drawType == 4){
						//rectangle mode shape preview
						g.fillRect(tempX, tempY, tempWidth, tempHeight);
					} else if (drawType == 2){
						//oval mode shape preview
						g.fillOval(tempX, tempY, tempWidth, tempHeight);
					}
				} else if (newRectStep == 2){ 
					//if there has been a second click, make the temporary shape permanent in the position it was 
					//clicked, and add its data to the shape array list.
					//also increment the shape count.
					boolean added = false; //keeps track of whether to do the rest of the add steps or not
					/*
					 * newRect width > 0, newRect height > 0: bottom left
					 * x = x
					 * y = y
					 * width = width
					 * height = height
					 * 
					 */
					if(newRectWidth > 0 && newRectHeight > 0){
						rectList.add(newRectX);
						rectList.add(newRectY);
						rectList.add(newRectWidth);
						rectList.add(newRectHeight);
						added = true;
						/*
						 * * newRect width < 0, newRect height > 0: bottom left
						 * x = x - width 
						 * y = y
						 * width = -width
						 * height = height
						 * 
						 */
					} else if (newRectWidth < 0 && newRectHeight > 0){
						rectList.add(newRectX + newRectWidth);
						rectList.add(newRectY);
						rectList.add(-newRectWidth);
						rectList.add(newRectHeight);
						added = true;
						/*
						 * newRect width < 0, newRect height < 0: top left
						 * x = x - width 
						 * y = y - height
						 * width = -width
						 * height = -height
						 * 
						 */
					} else if (newRectWidth < 0 && newRectHeight < 0){
						rectList.add(newRectX + newRectWidth);
						rectList.add(newRectY + newRectHeight);
						rectList.add(-newRectWidth);
						rectList.add(-newRectHeight);
						added = true;
						/*
						 * newRect width > 0, newRect height < 0: top right
						 * x = x
						 * y = y - height
						 * width = width
						 * height = -height
						 * 
						 */
					} else if (newRectWidth > 0 && newRectHeight < 0){
						rectList.add(newRectX);
						rectList.add(newRectY + newRectHeight);
						rectList.add(newRectWidth);
						rectList.add(-newRectHeight);
						added = true;
					} else {
						//if anything didn't seem right along the way, reset the new shape process
						newRectStep = 0;
					}
					if (added){
						rectList.add(collisVal);
						rectList.add(climbable);
						rectList.add(redVal);
						rectList.add(greenVal); 
						rectList.add(blueVal);
						rectCount++;
						if(drawType == 4){
							warpList.add(warpToMapNum);
							warpList.add(warpToXCoord);
							warpList.add(warpToYCoord);
							warpCount++;
						}
						//editMenuCountFraction.setText("/ " + rectCount);
						adjustEditMenu();
						//draw the newly added shapes using the data that is now in the shape list.
						if((int) rectList.get(((rectCount - 1) * 9) + 4) == 0 || (int) rectList.get(((rectCount - 1) * 9) + 4) == 1 || (int) rectList.get(((rectCount - 1) * 9) + 4) == 2 || (int) rectList.get(((rectCount - 1) * 9) + 4) == 5){
							g.fillRect(newRectX, newRectY, newRectWidth, newRectHeight);
							//System.out.println(rectList.get(((rectCount - 1) * 9) + 4));
						} else if ((int) rectList.get(((rectCount - 1) * 9) + 4) == 3 || (int) rectList.get(((rectCount - 1) * 9) + 4) == 4){
							g.fillOval(newRectX, newRectY, newRectWidth, newRectHeight);
						}
						newRectStep = 0; //reset the new shape process
						added = false;
					}
				}

				//draw an char with s in body and head at the coordinates of the spawn point
				g.setColor(Color.black);
				g.drawString("s", spawnX + 12, spawnY + 19);
				g.setColor(Color.gray);
				int charSpawnX = spawnX+10; //x offset to draw the spawn point preview
				int charSpawnY = spawnY+26; //y offset to draw the spawn point preview
				g.drawString("H", charSpawnX, charSpawnY); 
				g.drawString("- -", charSpawnX - 5, charSpawnY - 6);
				g.drawString("O", charSpawnX, charSpawnY - 5);
				g.drawString("o", charSpawnX + 2, charSpawnY - 15);

				if(showPermaSpritePreview){
					//drawChar. clickable permanent sprite preview
					g.setColor(new Color (permaSpritePreviewColorR, permaSpritePreviewColorG, permaSpritePreviewColorB));
					g.drawString("H", permaSpritePreviewX, permaSpritePreviewY); 
					g.drawString("- -", permaSpritePreviewX - 5, permaSpritePreviewY - 6);
					g.drawString("O", permaSpritePreviewX, permaSpritePreviewY - 5);
					g.drawString("o", permaSpritePreviewX + 2, permaSpritePreviewY - 15);
				}

				if(drawType == 1){
					//drawChar. mouse motion sprite preview
					g.setColor(new Color(redVal, greenVal, blueVal));
					g.drawString("H", tempSpritePreviewX, tempSpritePreviewY); 
					g.drawString("- -", tempSpritePreviewX - 5, tempSpritePreviewY - 6);
					g.drawString("O", tempSpritePreviewX, tempSpritePreviewY - 5);
					g.drawString("o", tempSpritePreviewX + 2, tempSpritePreviewY - 15);
				}

				if(drawType == 3 && selectionMade){ //draw the border around a selected shape in edit mode
					//g.setColor(new Color(redVal, greenVal, blueVal));
					g.setColor(Color.RED);
					if(selectionType == 0 || selectionType == 1 || selectionType == 2 || selectionType == 5){
						//for rectangles and warps
						g.drawRect(selectionX, selectionY, selectionWidth, selectionHeight);
						g.drawRect(selectionX + 1, selectionY + 1, selectionWidth, selectionHeight);
						g.drawRect(selectionX - 1, selectionY - 1, selectionWidth, selectionHeight);
					} else if (selectionType == 3 || selectionType == 4){
						//for ovals
						g.drawOval(selectionX, selectionY, selectionWidth, selectionHeight);
						g.drawOval(selectionX + 1, selectionY + 1, selectionWidth, selectionHeight);
						g.drawOval(selectionX - 1, selectionY - 1, selectionWidth, selectionHeight);
					}
				}

				g.setColor(Color.black);
				g.drawRect(15, 15, gameWidth, gameHeight);
				this.repaint();
			}
		}
	}

	/**
	 * this removes all data of the most recently drawn shape from the shape arraylist
	 * and lowers the shape count by one. it is basically just a limited delete button
	 * and does nothing else, but it is much faster to delete the most recent shape
	 * this way than to have to go all the way into edit mode and select it, then go
	 * back to whatever mode you were in before. undo also takes care of the clearing
	 * the last 3 values out of the warplist if it was a warp that was undone.
	 */
	public void undo(){
		if(rectList.size() > 0){
			//System.out.println(rectList.get(rectList.size() - 5));
			if ((int) rectList.get(rectList.size() - 5) == 5){
				for(int i = 0; i < 3; i++){
					warpList.remove(warpList.size() - 1);
				}
				warpCount--;
			}
			for (int i = 0; i < 9; i++){
				//System.out.println(rectList);
				rectList.remove(rectList.size() - 1);
			}
			rectCount--;
			adjustEditMenu();
			panel.repaint();
		}
	}

	/**
	 * this method refreshes the frame size. 
	 * this was necessary since the whole MapMaker
	 * class extends JFrame, and it was not sensible 
	 * to call this.pack() from within an 
	 * internal class. also a useful method in 
	 * general.
	 */
	public void refreshFrameSize(){
		//this.setSize(GAME_WIDTH + 30, GAME_HEIGHT + 30);
		panel.setPreferredSize(new Dimension(gameWidth + 30, gameHeight + 30));
		this.add(panel);
		this.pack();
	}

	/**
	 * this method refreshed the frame title. 
	 * this was necessary since the whole MapMaker
	 * class extends JFrame, and it was not sensible 
	 * to call this.setTitle() from within an 
	 * internal class. also a useful method in 
	 * general.
	 */
	public void refreshFrameTitle(){
		this.setTitle("Map Maker: Map #" + map);
	}

	/**
	 * this method clears the shape array, sets the shape count
	 * to zero, disables the save and undo buttons, etc.
	 * basically just primes the whole program for a new map.
	 */
	public void newMap(){
		rectList.clear();
		rectCount = 0;
		warpList.clear();
		warpCount = 0;
		selectionMade = false;
		save.setEnabled(false);
		undo.setEnabled(false);
		this.setTitle("Map Maker: New Map");
		edgeWarpLeftCheck.setSelected(false);
		edgeWarpRightCheck.setSelected(false);
		edgeWarpUpCheck.setSelected(false);
		edgeWarpDownCheck.setSelected(false);
		map = checkMinimumAvailableMapNumber();
		mapNumInput.setText("" + map);
		adjustEditMenu();
		hideSpritePreview();
		panel.repaint();
	}

	/**
	 * this method sets the loading boolean to true at the start,
	 * loads all needed information from the specified map file,
	 * then sets the loading boolean back to false and 
	 * renders the loaded map. 
	 */
	public void load(){
		loading = true;
		rectList.clear();
		rectCount = 0;
		warpList.clear();
		warpCount = 0;
		selectionMade = false;
		//set map number from the text box in the load menu
		map = Integer.parseInt(loadInput.getText());
		mapNumInput.setText("" + map);
		refreshFrameTitle();
		//enable saving
		save.setEnabled(true);
		//get all the info from the file
		String fileName = "maps/" + map + ".txt";
		Scanner scan1;
		try {
			//get the game dimensions
			scan1 = new Scanner(new FileReader(fileName));
			int width = 0;
			int height = 0;
			width = scan1.nextInt();
			height = scan1.nextInt();
			gameHeight = height;
			gameWidth = width;
			widthInput.setText("" + gameWidth);
			heightInput.setText("" + gameHeight);
			panel.setPreferredSize(new Dimension(gameWidth, gameHeight));
			this.add(panel);
			this.pack();
			//get the spawn point
			spawnX = (scan1.nextInt() + 5);
			spawnY = (scan1.nextInt() - 11);
			spawnXInput.setText("" + spawnX);
			spawnYInput.setText("" + spawnY);
			//get the edgewarps
			String left = scan1.next();
			if (left.equals("n")){
				edgeWarpLeftLabel.setEnabled(false);
				edgeWarpLeftInput.setEnabled(false);
				edgeWarpLeftCheck.setSelected(false);
			} else {
				edgeWarpLeftLabel.setEnabled(true);
				edgeWarpLeftInput.setEnabled(true);
				edgeWarpLeftCheck.setSelected(true);
				edgeWarpLeftInput.setText(left);
				edgeWarpLeft = left;
			}
			String right = scan1.next();
			if (right.equals("n")){
				edgeWarpRightLabel.setEnabled(false);
				edgeWarpRightInput.setEnabled(false);
				edgeWarpRightCheck.setSelected(false);
			} else {
				edgeWarpRightLabel.setEnabled(true);
				edgeWarpRightInput.setEnabled(true);
				edgeWarpRightCheck.setSelected(true);
				edgeWarpRightInput.setText(right);
				edgeWarpRight = right;
			}
			String up = scan1.next();
			if (up.equals("n")){
				edgeWarpUpLabel.setEnabled(false);
				edgeWarpUpInput.setEnabled(false);
				edgeWarpUpCheck.setSelected(false);
			} else {
				edgeWarpUpLabel.setEnabled(true);
				edgeWarpUpInput.setEnabled(true);
				edgeWarpUpCheck.setSelected(true);
				edgeWarpUpInput.setText(up);
				edgeWarpUp = up;
			}
			String down = scan1.next();
			if (down.equals("n")){
				edgeWarpDownLabel.setEnabled(false);
				edgeWarpDownInput.setEnabled(false);
				edgeWarpDownCheck.setSelected(false);
			} else {
				edgeWarpDownLabel.setEnabled(true);
				edgeWarpDownInput.setEnabled(true);
				edgeWarpDownCheck.setSelected(true);
				edgeWarpDownInput.setText(down);
				edgeWarpDown = down;
			}
			//get the shape count
			rectCount = scan1.nextInt();
			editMenuCountFraction.setText("/ " + rectCount);
			//allow undoing, if there are shapes in the file
			if (rectCount > 0) {
				undo.setEnabled(true);
			}
			//get the shape data from the file
			for (int i = 0; i < (rectCount); i++){
				for(int j = 0; j < 2; j++){
					rectList.add(scan1.nextInt() + 15);
				}
				for (int k = 0; k < 7; k++){
					rectList.add(scan1.nextInt());
				}
			}
			//fill the warpList
			warpCount = scan1.nextInt();
			for (int i = 0; i < (warpCount); i++){
				for(int j = 0; j < 3; j++){
					String tempWarpMapNum = scan1.next();
					if(tempWarpMapNum.equals("n")){
						tempWarpMapNum = "" + map;
					}
					warpList.add(Integer.parseInt(tempWarpMapNum));
				}
			}
			//System.out.println(warpList);
			scan1.close();
			refreshFrameSize();
			adjustEditMenu();
			loading = false;
			hideSpritePreview();
			panel.repaint();
		} catch (FileNotFoundException fnfe) {
			System.out.println("no map file found for map "+ map);
			loading = false;
		}

	}
	
	private void hideSpritePreview(){
		permaSpritePreviewX = -1000;
		permaSpritePreviewY = -1000;
	}

	/**
	 * this method saves all of the data within the shape array, as well as data such as
	 * the map dimensions, the edge warps, the rectangle count, the spawn point,
	 * etc. it also contains some harmless info about file format / layout, for anyone
	 * viewing the files in a text editor.
	 */
	public void save(){
		//roundEdges();
		try {
			//the file to save to, in the maps folder:
			PrintWriter writer = new PrintWriter("maps/" + map + ".txt");
			//write a line with the map dimensions
			writer.println(gameWidth + " " + (gameHeight));
			//write a line with the spawn point, corrected to closely match the spawn point in map maker 
			writer.println((spawnX - 5) + " " + (spawnY + 11)); //-5 and +11 just to help center the sprite on the spawn point correctly. don't worry about it
			//write a line with the 4 edge warp values, or n if there isn't one
			writer.println(edgeWarpLeft + " " + edgeWarpRight + " " + edgeWarpUp + " " + edgeWarpDown);
			//write a line with the shape count
			writer.println(rectCount);
			//this loop goes thru and writes a line for each shape, containing all the shape's data
			for(int i = 0; rectCount > 0 && i < rectCount; i++){
				writer.println((((int) rectList.get(i * 9) - 15) + " " + ((int) rectList.get((i * 9) + 1) - 15) + " " + (rectList.get((i * 9) + 2)) + " " + (rectList.get((i * 9) + 3)) + " " + (rectList.get((i * 9) + 4)) + " " + (rectList.get((i * 9) + 5)) + " " + (rectList.get((i * 9) + 6)) + " " + (rectList.get((i * 9) + 7)) + " " + (rectList.get((i * 9) + 8))));
			}
			//write a line with the warp count
			writer.println(warpCount);
			//this loop goes thru and writes a line for each warp, containing the target map num, the target x coord, and the target y coord
			//if the destination map number is the same as the current map number, then 'n' is written to the file (so that changing file name wont ruin the warp)
			String tempWarpMapNum;
			for(int i = 0; warpCount > 0 && i < warpCount; i++){
				if((int) warpList.get(i * 3) == map){
					tempWarpMapNum = "n";
				} else {
					tempWarpMapNum = "" + (int) warpList.get(i * 3);
				}
				writer.println(tempWarpMapNum + " " + (int) (warpList.get((i * 3) + 1)) + " " + (int) (warpList.get((i * 3) + 2)));
			}

			//everything below here does not affect the map, it is just comments to help people who may be reading the file from a text editor
			writer.println();
			writer.println("//file format:");
			writer.println();
			writer.println("mapWidthInPixels mapHeightInPixels");
			writer.println("playerSpawnPointXPosition playerSpawnPointYPosition");
			writer.println("mapNumberOfLeftEdgeWarp mapNumberOfRightEdgeWarp mapNumberOfTopEdgeWarp mapNumberOfBottomEdgeWarp //the value 'n' means none, in which case player just warps to the opposite side of the current map");
			writer.println("numberOfShapesToReadFromTheFile");
			writer.println("topLeftCornerXPosition topLeftCornerYPosition width height collisionType climbabilityType colorRedValue colorGreenValue colorBlueValue //each of these lines represents one shape. this same format is repeated for each shape on the map");
			writer.println("numberOfInMapWarpsToReadFromTheFile");
			writer.println("warpToMapNumber warpToXCoordinate warpToYCoordinate //each of these lines represents one in-map warp, this same format is repeated for each warp on the map");
			writer.println();
			writer.println("//collision type can be: 0 (no collision, draw behind player), 1 (collision on), 2 (no collision, draw in front of player), 3 (no collision oval, draw behind player), 4 (no collision oval, draw in front of player), or 5 (warp. is not drawn, but will teleport the player).");
			writer.println("//climbability type can be: 0 (can't climb), 1 (can ladder climb: no gravity applies, and can climb one step at a time), 2 (watery: can climb, but slower gravity applies), or 3 (can jump climb: no gravity applies, and can climb quickly by jumping). anything greater than or equal to 10 is to keep track of which warp is assigned to which warp rectangle (e.g. 10 = warp 0, 11 = warp 1, etc.).");
			writer.println();
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("something went wrong in the file writer");
		}
	}

	/**
	 * this refreshes the edit menu. it is called often, and it 
	 * activates / deactivates buttons as necessary, repaints the color preview box,
	 * and fills in text boxes with updated values as the selected shape
	 * changes. 
	 */
	public void adjustEditMenu(){
		editWarpTotalLabel.setText("(" + warpCount + " total)");
		if (selectionMade){
			//set a local variable to the selected shape, for convenience
			int index = editMenuCountVal - 1;
			//turn on some important buttons and things
			editChangeCollis.setEnabled(true);
			editMenuSendToBack.setEnabled(true);
			editMenuBringToFront.setEnabled(true);
			editMenuDeleteShape.setEnabled(true);
			editMenuSetCoords.setEnabled(true);
			editColorButton.setEnabled(true);
			editColorLabel.setEnabled(true);
			editRedLabel.setEnabled(true);
			editGreenLabel.setEnabled(true);
			editBlueLabel.setEnabled(true);
			editRedField.setEnabled(true);
			editGreenField.setEnabled(true);
			editBlueField.setEnabled(true);
			editHideColor = false;
			editMenuX1Label.setEnabled(true);
			editMenuX2Label.setEnabled(true);
			editMenuY1Label.setEnabled(true);
			editMenuY2Label.setEnabled(true);
			editMenuX1Input.setEnabled(true);
			editMenuX2Input.setEnabled(true);
			editMenuY1Input.setEnabled(true);
			editMenuY2Input.setEnabled(true);
			editSetCollisOne.setEnabled(true);
			editCollisLabel.setEnabled(true);
			editSetCollisZero.setEnabled(true);
			editCollisClimbable.setEnabled(true);
			editClimbableLabel.setEnabled(true);
			editMenuCountPlus.setEnabled(true);
			editMenuCountMinus.setEnabled(true);
			//initialize shape count fraction
			editMenuCountFraction.setText("/ " + rectCount);
			if(!scrollWarpsOnly){
				if (index == 0){
					editMenuCountMinus.setEnabled(false);
				}
				if (index == rectCount - 1){
					editMenuCountPlus.setEnabled(false);
				}
			} else {
				if ((int) rectList.get((index * 9) + 5) == 10){
					editMenuCountMinus.setEnabled(false);
				}
				if ((int) rectList.get((index * 9) + 5) - 10 == (warpCount - 1)){
					editMenuCountPlus.setEnabled(false);
				}
			}
			//initialize coordinate box values
			int leftX = (int) rectList.get(index *9);
			int rightX = leftX + (int) rectList.get((index*9) + 2);
			int topY = (int) rectList.get((index*9)+1);
			int bottomY = topY + (int) rectList.get((index*9) + 3);
			editMenuX1Input.setText("" + (leftX - 14));
			editMenuY1Input.setText("" + (topY - 15));
			editMenuX2Input.setText("" + (rightX - 15));
			editMenuY2Input.setText("" + (bottomY - 15));
			//initialize color box and values
			editRedVal = (int) rectList.get((index*9) + 6);
			editRedField.setText("" + editRedVal);
			editGreenVal = (int) rectList.get((index*9) + 7);
			editGreenField.setText("" + editGreenVal);
			editBlueVal = (int) rectList.get((index*9) + 8);
			editBlueField.setText("" + editBlueVal);
			//initialize collision values and buttons
			if((int) rectList.get((index*9) + 4) == 0 || (int) rectList.get((index*9) + 4) == 3){ //collis off, layering back
				editSetCollisZero.setSelected(true);
				editLayerBack.setSelected(true);
				editLayerBack.setEnabled(true);
				editLayerFront.setEnabled(true);
				editBlockLayer.setEnabled(true);
			} else if ((int) rectList.get((index*9) + 4) == 1){ //collis on 
				editSetCollisOne.setSelected(true); 
				editLayerBack.setEnabled(false);
				editLayerFront.setEnabled(false);
				editBlockLayer.setEnabled(false);
			} else if ((int) rectList.get((index*9) + 4) == 2 || (int) rectList.get((index*9) + 4) == 4) { //collis off, layering front
				editSetCollisZero.setSelected(true);
				editLayerFront.setSelected(true);
				editLayerBack.setEnabled(true);
				editLayerFront.setEnabled(true);
				editBlockLayer.setEnabled(true);
			}
			if((int) rectList.get((index*9) + 4) == 3 || (int) rectList.get((index*9) + 4) == 4){ //oval 
				editSetCollisOne.setEnabled(false);
				editCollisClimbable.setEnabled(false);
				editCollisWater.setEnabled(false);
				editWaterLabel.setEnabled(false);
				editClimbableLabel.setEnabled(false);
				editWarpOnLabel.setEnabled(false);
				editWarpOn.setEnabled(false);
				editWarpOn.setSelected(false);
				editWarpMap.setEnabled(false);
				editWarpX.setEnabled(false);
				editWarpY.setEnabled(false);
				editWarpButton.setEnabled(false);
				editWarpXLabel.setEnabled(false);
				editWarpYLabel.setEnabled(false);
				editWarpMapLabel.setEnabled(false);
			} else if ((int) rectList.get((index*9) + 4) == 0 || (int) rectList.get((index*9) + 4) == 1 || (int) rectList.get((index*9) + 4) == 2){ //rectangle
				editSetCollisOne.setEnabled(true);
				editCollisClimbable.setEnabled(true);
				editClimbableLabel.setEnabled(true);
				editWarpOnLabel.setEnabled(false);
				editWarpOn.setEnabled(false);
				editWarpOn.setSelected(false);
				editWarpMap.setEnabled(false);
				editWarpX.setEnabled(false);
				editWarpY.setEnabled(false);
				editWarpButton.setEnabled(false);
				editWarpXLabel.setEnabled(false);
				editWarpYLabel.setEnabled(false);
				editWarpMapLabel.setEnabled(false);
				if(editCollisClimbable.isSelected()){
					editWaterLabel.setEnabled(true);
					editCollisWater.setEnabled(true);
				} else {
					editWaterLabel.setEnabled(false);
					editCollisWater.setEnabled(false);
				}
			} else if ((int) rectList.get((index*9) + 4) == 5){ // warp
				int tempWarpNum = ((int) rectList.get(((editMenuCountVal - 1) * 9) + 5) - 10);
				//int tempGetWarpVal = (int) warpList.get(tempWarpNum * 3);
				editWarpMap.setText("" + warpList.get(tempWarpNum * 3));
				editWarpX.setText("" + warpList.get((tempWarpNum * 3) + 1));
				editWarpY.setText("" + warpList.get((tempWarpNum * 3) + 2));
				editCollisLabel.setEnabled(false);
				editChangeCollis.setEnabled(false);
				editSetCollisZero.setSelected(true);
				editSetCollisZero.setEnabled(false);
				editLayerBack.setEnabled(false);
				editLayerFront.setEnabled(false);
				editBlockLayer.setEnabled(false);
				editSetCollisOne.setEnabled(false);
				editCollisClimbable.setEnabled(false);
				editCollisWater.setEnabled(false);
				editWaterLabel.setEnabled(false);
				editClimbableLabel.setEnabled(false);
				editWarpOnLabel.setEnabled(true);
				editWarpOn.setEnabled(false);
				editWarpOn.setSelected(true);
				editWarpMap.setEnabled(true);
				editWarpX.setEnabled(true);
				editWarpY.setEnabled(true);
				editWarpButton.setEnabled(true);
				editWarpXLabel.setEnabled(true);
				editWarpYLabel.setEnabled(true);
				editWarpMapLabel.setEnabled(true);
			}

			//initialize climb values
			if((int) rectList.get((index*9) + 5) == 0){ //climb off
				editCollisClimbable.setSelected(false);
			} else if ((int) rectList.get((index*9) + 5) == 1){ //ladder climb on, water off
				editCollisClimbable.setSelected(true); 
				editClimbableLadder.setSelected(true);
				editCollisWater.setSelected(false);
			} else if ((int) rectList.get((index*9) + 5) == 2){ //climb on, water on
				editCollisClimbable.setSelected(true);
				editCollisWater.setSelected(true);
			} else if ((int) rectList.get((index*9) + 5) == 3){ //jump climb on, water off
				editCollisClimbable.setSelected(true);
				editClimbableJump.setSelected(true);
				editCollisWater.setSelected(false);
			} else if ((int) rectList.get((index*9) + 5) >= 10){ //warp 		
				editCollisClimbable.setSelected(false);
				editCollisWater.setSelected(false);
			} 

		} else {
			//if no selection is made, disable buttons and things that cause errors
			editChangeCollis.setEnabled(false);
			editMenuSendToBack.setEnabled(false);
			editMenuBringToFront.setEnabled(false);
			editMenuDeleteShape.setEnabled(false);
			editMenuSetCoords.setEnabled(false);
			editColorButton.setEnabled(false);
			editColorLabel.setEnabled(false);
			editRedLabel.setEnabled(false);
			editGreenLabel.setEnabled(false);
			editBlueLabel.setEnabled(false);
			editRedField.setEnabled(false);
			editGreenField.setEnabled(false);
			editBlueField.setEnabled(false);
			editMenuX1Label.setEnabled(false);
			editMenuX2Label.setEnabled(false);
			editMenuY1Label.setEnabled(false);
			editMenuY2Label.setEnabled(false);
			editMenuX1Input.setEnabled(false);
			editMenuX2Input.setEnabled(false);
			editMenuY1Input.setEnabled(false);
			editMenuY2Input.setEnabled(false);
			editSetCollisOne.setEnabled(false);
			editCollisLabel.setEnabled(false);
			editSetCollisZero.setEnabled(false);
			editCollisClimbable.setEnabled(false);
			editClimbableLabel.setEnabled(false);
			editWarpOn.setEnabled(false);
			editWarpMap.setEnabled(false);
			editWarpX.setEnabled(false);
			editWarpY.setEnabled(false);
			editWarpButton.setEnabled(false);
			editWarpOnLabel.setEnabled(false);
			editWarpXLabel.setEnabled(false);
			editWarpYLabel.setEnabled(false);
			editWarpMapLabel.setEnabled(false);
			editHideColor = true;
			editClimbableJump.setEnabled(false);
			editClimbableLadder.setEnabled(false);
			editClimbableTypeLabel.setEnabled(false);

			editMenuCountFraction.setText("/ " + rectCount);
			if(rectCount > 0){
				editMenuCountSet.setEnabled(true);
			} else {
				editMenuCountSet.setEnabled(false);
			}
			editMenuCountPlus.setEnabled(false);
			editMenuCountMinus.setEnabled(false);
		}
		editMenuPanel.repaint();

	}

	/**
	 * Correct any values that are not within the range 0 to 255, inclusive.
	 * Values less than 0 will become 0, values greater than 255 will become 255.
	 * Meant to make sure that color RGB values are valid.
	 * 
	 * @param value The original value as an int
	 * @return int: the value adjusted so that 0 <= value <= 255
	 */
	public int fixColorRange(int value){
		if (value > 255){
			value = 255;
		} else if (value < 0){
			value = 0;
		}
		return value;
	}

	/**
	 * this one is a doozy.. when a warp is deleted, this method goes through and 
	 * subtracts one from the collision value (which keeps track of warp number)
	 * of each warp that is AFTER the deleted warp in the shape list.
	 * otherwise, deleting warp 2 when there were 3 warps present would mean that 
	 * the warps would be numbered 0, 1, 3, 4. then, when a new warp was drawn,
	 * it would probably go into spot 2 or something, and the warplist would be
	 * completely whacked out before that even happened.. so this takes care
	 * of that problem. 
	 * @param warpNumDeleted the number of the warp that was deleted (starts at 10)
	 */
	public void adjustWarpData(int warpNumDeleted){
		for(int i = 0; rectCount > 0 && i < rectCount; i++){
			if ((int) rectList.get((i *9) + 4) == 5 && (int) rectList.get((i *9) + 5) > warpNumDeleted){
				rectList.set(((i*9) + 5), ((int) rectList.get((i *9) + 5) - 1));
			}

		}
	}

	/**
	 * Return the minimum unused map number (file name) in the maps directory
	 * used to automatically set the map number to a value that won't cause 
	 * user to accidentally overwrite existing files
	 */
	public int checkMinimumAvailableMapNumber(){
		File mapDirectory = new File("maps/");
		File[] allFileNames = mapDirectory.listFiles();

		int index = 0;
		String tempName = "0";
		int val = -1;
		int max = -1;

		while(allFileNames.length > 0 && index < allFileNames.length){
			tempName = (allFileNames[index].getName());
			tempName = tempName.substring(0, tempName.length() - 4);
			val = Integer.parseInt(tempName);
			if(val > max){
				max = val;
			}
			index++;
		}
		//System.out.println(max);
		if(max > -1){
			int[] freq = new int[max + 1];
			index = 0;
			while(allFileNames.length > 0 && index < allFileNames.length){
				tempName = (allFileNames[index].getName());
				tempName = tempName.substring(0, tempName.length() - 4);
				val = Integer.parseInt(tempName);
				freq[val]++;
				index++;
			}
			index = 0;
			while(freq[index] > 0){
				index++;
			}
		} else {
			index = 0;
		}


		return index;
	}

	/**
	 * Recursively create an ArrayList of all Components in the specified container
	 * Used to add KeyListeners to every non-textbox component in a particular 
	 * JFrame (the edit menu, for example).
	 * @param container The container to add all components from
	 * @return The list of all components
	 */
	public ArrayList<Component> getAllComponents(Container container) {
		Component[] components = container.getComponents();
		ArrayList<Component> componentList = new ArrayList<Component>();
		for (Component current : components) {
			if(!(current instanceof JTextField)){
				componentList.add(current);
				if (current instanceof Container)
					componentList.addAll(getAllComponents((Container) current));
			}
		}
		return componentList;
	}

	/**
	 * ToolBoxPanel extends JPanel. it is used as the toolbox panel,
	 * and draws such things as the toolbox color preview box 
	 * and the mouse X Y coordinates display
	 * @author adamcogen
	 *
	 */
	class ToolBoxPanel extends JPanel{

		public void paintComponent(Graphics g){

			if (redVal > 255 || redVal < 0){
				redVal = fixColorRange(redVal);
				redField.setText("" + redVal);	
			}
			if (blueVal > 255 || blueVal < 0){
				blueVal = fixColorRange(blueVal);
				blueField.setText("" + blueVal);	
			}
			if (greenVal > 255 || greenVal < 0){
				greenVal = fixColorRange(greenVal);
				greenField.setText("" + greenVal);	
			}

			//System.out.println(redVal + " " + greenVal+ " " + blueVal);
			g.setColor(new Color(redVal, greenVal, blueVal));
			g.fillRect(57, 70, 40, 10);
			g.setColor(Color.BLACK);
			g.drawRect(57, 70, 40, 10);
			g.drawString("X: " + dispMouseX + "   Y: " + dispMouseY, 90, 170);
		}
	}

	/**
	 * EditMenuPanel extends JPanel, and it is used for the edit mode window.
	 * it displays the color preview box basically. in the edit mode 
	 * window, this color preview box changes depending on 
	 * whether a shape is selected or not. 
	 *
	 */
	class EditMenuPanel extends JPanel{

		public void paintComponent(Graphics g){

			if (editRedVal > 255 || editRedVal < 0){
				editRedVal = fixColorRange(editRedVal);
				editRedField.setText("" + editRedVal);	
			}
			if (editBlueVal > 255 || editBlueVal < 0){
				editBlueVal = fixColorRange(editBlueVal);
				editBlueField.setText("" + editBlueVal);	
			}
			if (editGreenVal > 255 || editGreenVal < 0){
				editGreenVal = fixColorRange(editGreenVal);
				editGreenField.setText("" + editGreenVal);	
			}

			//System.out.println(redVal + " " + greenVal+ " " + blueVal);
			if (!editHideColor){
				g.setColor(new Color(editRedVal, editGreenVal, editBlueVal));
				g.fillRect(112, 172, 20, 10);
				g.setColor(Color.black);
				g.drawRect(112, 172, 20, 10);
			} else {
				g.setColor(new Color(204, 204, 204));
				g.fillRect(112, 172, 20, 10);
				g.setColor(new Color(153, 153, 153));
				g.drawRect(112, 172, 20, 10);
			}
		}
	}

}
