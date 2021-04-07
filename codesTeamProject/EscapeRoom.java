package codesTeamProject;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.*;

import codesEK280.CommonsEK;

public class EscapeRoom extends JPanel implements MouseListener {
	
	private static final long serialVersionUID = 1L;
	private Canvas3D canvas;
	public static PickTool pickTool;
	
	/* A function to position viewer to 'eye' location */
	private void defineViewer (SimpleUniverse simple_U, Point3d eye) {

	    TransformGroup viewTransform = simple_U.getViewingPlatform().getViewPlatformTransform();
		Point3d center = new Point3d(0, 0, 0);               //Define the point where the eye looks at
		Vector3d up = new Vector3d(0, 1, 0);                 //Define camera's up direction
		Transform3D view_TM = new Transform3D();
		view_TM.lookAt(eye, center, up);
		view_TM.invert();  
	    viewTransform.setTransform(view_TM);                 //Set the TransformGroup of ViewingPlatform
	    
	}
	
	/* A function to allow key navigation with the ViewingPlateform */
	private KeyNavigatorBehavior keyNavigation (SimpleUniverse simple_U) {

		ViewingPlatform view_platfm = simple_U.getViewingPlatform();
		TransformGroup view_TG = view_platfm.getViewPlatformTransform();
		KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(view_TG);
		BoundingSphere view_bounds = new BoundingSphere(new Point3d(), 20.0);
		keyNavBeh.setSchedulingBounds(view_bounds);
		return keyNavBeh;
		
	}
	
	public void mousePressed (MouseEvent e) {}
	
	public void mouseReleased (MouseEvent e) {}
	
	public void mouseEntered (MouseEvent e) {}
	
	public void mouseExited (MouseEvent e) {}
	
	public void mouseClicked (MouseEvent event) {
		
		//Mouse coordinates
		int x = event.getX();
		int y = event.getY(); 
		Point3d point3d = new Point3d(), center = new Point3d();
		canvas.getPixelLocationInImagePlate(x, y, point3d); 		//Obtain AWT pixel in ImagePlate coordinates
		canvas.getCenterEyeInImagePlate(center); 					//Obtain eye's position in IP coordinates

		Transform3D transform3D = new Transform3D(); 				//Matrix to relate ImagePlate coordinates~
		canvas.getImagePlateToVworld(transform3D); 					//To Virtual World coordinates
		transform3D.transform(point3d); 							//Transform 'point3d' with 'transform3D'
		transform3D.transform(center); 								//Transform 'center' with 'transform3D'

		Vector3d mouseVec = new Vector3d();
		mouseVec.sub(point3d, center);
		mouseVec.normalize();
		pickTool.setShapeRay(point3d, mouseVec); 									//Send a PickRay for intersection
		
		if (pickTool.pickClosest() != null) {
			
			PickResult pickResult = pickTool.pickClosest(); 						//Obtain the closest hit
			Shape3D box = (Shape3D) pickResult.getNode(PickResult.SHAPE3D);
			Appearance app = box.getAppearance();
			
			//Get results after clicking the painting Use the result to make appropriate changes
			if ((int) box.getUserData() == 0) { 			//Retrieve 'UserData'
				
				System.out.println("Hi");
				app.setTexture(texState("spiral.jpg"));
				box.setUserData(1); 						//Set 'UserData' to a new value
				
				DoorIndicator.setJupiter(true);
				DoorIndicator.checkJupiter();
				checkDoor();
				
			}
			
		}
		
	}
	
	/* A function to add ambient light and a point light to 'sceneBG' */
	static void addLights (BranchGroup sceneBG, Color3f clr) {		
		
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
		
		AmbientLight amLgt = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
		amLgt.setInfluencingBounds(bounds);
	
		Point3f pt  = new Point3f(2.0f, 2.0f, 2.0f);
		Point3f atn = new Point3f(1.0f, 0.0f, 0.0f);
		
		PointLight ptLight = new PointLight(clr, pt, atn);
		ptLight.setInfluencingBounds(bounds);
		
		sceneBG.addChild(amLgt);
		sceneBG.addChild(ptLight);
		
	}
	
	private static Texture texState (String string) {
		
		String filename = "images/" + string;
		TextureLoader loader = new TextureLoader(filename, null);
		ImageComponent2D image = loader.getImage();
		
		if (image == null)
			System.out.println("File not found");
		
		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);

		return texture;
		
	}
	
	private static TransformGroup wallFront() {
		
		Appearance wall =  DoorIndicator.setAppearance(CommonsEK.Grey, CommonsEK.Grey, "wall.jpg");
		
		Box front = new Box(20f, 4f, 0.05f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, wall);
		
		Transform3D trans = new Transform3D();
		trans.setTranslation(new Vector3f(0, 0, 20f + 0.05f));
		
		TransformGroup wallFront = new TransformGroup(trans);
		wallFront.addChild(front);
		
		return wallFront;
		
	}
	
	private static TransformGroup wallBack() {
		
		TransformGroup wallBack = wallRight();
		
		Transform3D rotator = new Transform3D();
		TransformGroup trans = null;
		
		rotator.rotY(Math.PI / 2);
		trans = new TransformGroup(rotator);
		trans.addChild(wallBack);
		
		return trans;	
		
	}
	
	private static TransformGroup wallRight() {
		
		TransformGroup wallRight = wallFront();
		
		Transform3D rotator = new Transform3D();
		TransformGroup trans = null;
		
		rotator.rotY(Math.PI / 2);
		trans = new TransformGroup(rotator);
		trans.addChild(wallRight);
		
		return trans;	
		
	}
	
	private static TransformGroup wallLeft() {
		
		TransformGroup wallLeft = wallBack();
		
		Transform3D rotator = new Transform3D();
		TransformGroup trans = null;
		
		rotator.rotY(Math.PI / 2);
		trans = new TransformGroup(rotator);
		trans.addChild(wallLeft);
		
		return trans;	
		
	}
	
	private static TransformGroup doorSide1() {
		
		Appearance wall =  DoorIndicator.setAppearance(CommonsEK.Blue, CommonsEK.White, "doorStone.jpg");
		
		Box side = new Box(4f, 3f, 0.05f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, wall);
		
		Transform3D t3D = new Transform3D();
		t3D.set(new Vector3f(1.5f, 3, -4 + 0.3f));
		
		Transform3D rot = new Transform3D();
		rot.rotY(Math.PI / 2);
		
		Transform3D trans = new Transform3D();
		trans.mul(t3D, rot);
		
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(side);
		
		return tg;
		
	}
	
	private static TransformGroup doorSide2() {
		
		TransformGroup tg = doorSide1();
		
		Transform3D t3D = new Transform3D();
		TransformGroup trans = null;
		
		t3D.set(new Vector3f(-3, 0, 0));
		trans = new TransformGroup(t3D);
		trans.addChild(tg);
		
		return trans;
		
	}
	
	private static TransformGroup floor() {
	
		Appearance floor =  DoorIndicator.setAppearance(CommonsEK.Blue, CommonsEK.White, "floor.jpg");
		
		Box ground = new Box(20f, 0.05f, 20f, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, floor);
		
		Transform3D translate = new Transform3D();
		translate.setTranslation(new Vector3f(0, -0.05f, 0));
		
		TransformGroup floorPanel = new TransformGroup(translate);
		floorPanel.addChild(ground);
		
		return floorPanel;
		
	}
	
	private static void addWalls (TransformGroup sceneTG, float scl) {
		
		Transform3D transform = new Transform3D();
		transform.set(scl, new Vector3f(0, 4f, 0));
		
		TransformGroup walls = new TransformGroup(transform);
		walls.addChild(wallFront());
		walls.addChild(wallRight());
		walls.addChild(wallBack());
		walls.addChild(wallLeft());
		
		sceneTG.addChild(walls);
		
	}
	
	private static void addDoorIndicatorRoom (TransformGroup sceneTG, float scale) {
	
		Transform3D t3D = new Transform3D();
		t3D.set(scale, new Vector3f(20 - 8, 0, 0));
		
		Transform3D rot = new Transform3D();
		rot.rotY(-Math.PI / 2);
		
		Transform3D mul = new Transform3D();
		mul.mul(t3D, rot);
		
		TransformGroup tg = new TransformGroup(mul);
		tg.addChild(positionDoor(1.5f));
		tg.addChild(doorSide1());
		tg.addChild(doorSide2());
		
		sceneTG.addChild(tg);
		
	}
	
	static void checkDoor() {
		
		TransformGroup tg = new TransformGroup();
		addDoorIndicatorRoom(tg, 1.0f);
		
		Node door = tg.getChild(0);
		TransformGroup trans = new TransformGroup();
		trans.addChild(door);
		
		Alpha a = new Alpha(1, 10000);
		
		Transform3D axis = new Transform3D();
		axis.rotY(Math.PI / 2);
		
		RotationInterpolator ri = new RotationInterpolator(a, trans, axis, 0.0f, (float) Math.PI * 2.0f);
		ri.setSchedulingBounds(new BoundingSphere());
		
		if (DoorIndicator.moonAttained && DoorIndicator.jupiterAttained && DoorIndicator.neptuneAttained && DoorIndicator.sunAttained) {
			trans.addChild(ri);
		}
		
	}
	
	private static void createRoom (BranchGroup sceneBG, float scale) {
		
		Transform3D t3D = new Transform3D();
		t3D.setScale(scale);
		
		TransformGroup tg = new TransformGroup(t3D);
		tg.addChild(floor());
		addWalls(tg, 1.0f);
		addDoorIndicatorRoom(tg, 1.0f);
		
		sceneBG.addChild(tg);
		
	}
	
	private static TransformGroup positionDoor (float scl) {
		
		BranchGroup bg = new BranchGroup();
		DoorIndicator.createScene(bg);
		
		Transform3D t3D = new Transform3D();
		t3D.set(scl, new Vector3f(0, 3, 0));
		
		TransformGroup tg = new TransformGroup(t3D);
		tg.addChild(bg);
		
		return tg;
		
	}
	
	private static TransformGroup positionPainting (float scl) {
		
		Transform3D t3D = new Transform3D();
		t3D.set(scl, new Vector3f(0, 4, 22.5f - 0.05f));
		
		TransformGroup tg = new TransformGroup(t3D);
		tg.addChild(Painting.pantRocketTouch());
		
		return tg;
		
	}
	
	private static TransformGroup postionRotatingCube (float scl) {
		
		Transform3D t3D = new Transform3D();
		t3D.set(scl, new Vector3f(0, 4, -17 + 0.05f));
		
		TransformGroup tg = new TransformGroup(t3D);
		tg.addChild(RotatingCube.createScene());
		
		return tg;
		
	}
	
	private static TransformGroup positionWheel (float scl) {
		
		Transform3D t3D = new Transform3D();
		t3D.set(scl, new Vector3f(-19.5f, 4, 0));
		
		Transform3D rot = new Transform3D();
		rot.rotY(Math.PI / 2);
		
		Transform3D trans = new Transform3D();
		trans.mul(t3D, rot);
		
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(WheelPuzzle.createScene());
		
		return tg;
		
	}
	
	/* A function to build the content branch and attach to 'scene' */
	private static BranchGroup createScene (BranchGroup sceneBG) {
		
		TransformGroup sceneTG = new TransformGroup();   
		sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		sceneBG.addChild(sceneTG);	
		
		pickTool = new PickTool(sceneBG);
		pickTool.setMode(PickTool.BOUNDS);
		
		addLights(sceneBG, CommonsEK.Grey);
		
		createRoom(sceneBG, 1.0f);
		
		sceneTG.addChild(positionPainting(2.5f));
		sceneTG.addChild(postionRotatingCube(1.0f));
		sceneTG.addChild(positionWheel(0.80f));

		/*
			KeyMovement km = new KeyMovement(sceneTG); 	
			km.setForwardKey(KeyEvent.VK_W);
			km.setLeftKey(KeyEvent.VK_A);
			km.setBackKey(KeyEvent.VK_S);
			km.setRightKey(KeyEvent.VK_D);
			km.setSchedulingBounds(new BoundingSphere());
			sceneBG.addChild(km); 
		*/
		
		return sceneBG;
	
	}
	
	/* A constructor to set up and run the application */
	public EscapeRoom() {
		
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);
		canvas.addMouseListener(this); // NOTE: enable mouse clicking
		
		SimpleUniverse su = new SimpleUniverse(canvas);   // create a SimpleUniverse                                    
		defineViewer(su, new Point3d(8, 4, 6));    // set the viewer's location
		
		BranchGroup scene = new BranchGroup();		
		createScene(scene);                           // add contents to the scene branch
		
		scene.addChild(keyNavigation(su));                   // allow key navigation
		
		scene.compile();		                             // optimize the BranchGroup
		su.addBranchGraph(scene);                            // attach the scene to SimpleUniverse
		
		setLayout(new BorderLayout());
		add("Center", canvas);		
		setVisible(true);
		
	}

	public static void main (String[] args) {
		JFrame frame = new JFrame("Escape Room"); 
		frame.getContentPane().add(new EscapeRoom());         // create an instance of the class
		frame.setSize(600, 600);                             // set the size of the JFrame
		frame.setVisible(true);
	}
	
}