package codesTeamProject;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

import codesEK280.CommonsEK;

public class Painting extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private Canvas3D canvas;
	private static PickTool pickTool;

	public static void addLights(BranchGroup sceneBG, Color3f clr) {
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		AmbientLight amLgt = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
		amLgt.setInfluencingBounds(bounds);
		sceneBG.addChild(amLgt);
		Point3f pt = new Point3f(2.0f, 2.0f, 2.0f);
		Point3f atn = new Point3f(1.0f, 0.0f, 0.0f);
		PointLight ptLight = new PointLight(clr, pt, atn);
		ptLight.setInfluencingBounds(bounds);
		sceneBG.addChild(ptLight);
	}

	public static Appearance createAppearance() {
		Appearance Appear = new Appearance();

		TexCoordGeneration tcg = new TexCoordGeneration();
		tcg.setEnable(false);

		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.MODULATE);

		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		Appear.setPolygonAttributes(polyAttrib);
		Appear.setTexture(texState("painting"));
		Appear.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		return Appear;
	}

	// Dont need to change this function to change the geometry of the painting
	public static void paintRocket(BranchGroup scene) {
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		Shape3D shape3D = new Shape3D();
		shape3D.setGeometry(createGeometry());
		shape3D.setAppearance(createAppearance());
		shape3D.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		shape3D.setBounds(bounds);
		shape3D.setName("painting");
		shape3D.setUserData(0);
		TransformGroup objTG = new TransformGroup();
		objTG.addChild(shape3D); // add 'box' to the 'objTG'
		scene.addChild(objTG);

	}

	// Setting the geometry of the painting using QuadArray
	public static Geometry createGeometry() {
		Point3f[] verts = { new Point3f(-1f, 1f, -1f), new Point3f(-1f, -1f, -1f), new Point3f(1f, -1f, -1f),
				new Point3f(1f, 1f, -1f) };
		QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		plane.setCoordinates(0, verts);
		TexCoord2f q = new TexCoord2f(0f, 1f);
		plane.setTextureCoordinate(0, 0, q);
		q.set(0f, 0f);
		plane.setTextureCoordinate(0, 1, q);
		q.set(1f, 0f);
		plane.setTextureCoordinate(0, 2, q);
		q.set(1f, 1f);
		plane.setTextureCoordinate(0, 3, q);

		return plane;
	}

	// Getting texture for the painting, basically using it to give texture to the
	// Rectangle
	private static Texture texState(String string) {
		String filename = "images/" + string + ".jpg";
		TextureLoader loader = new TextureLoader(filename, null);
		ImageComponent2D image = loader.getImage();
		if (image == null)
			System.out.println("File not found");
		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);

		return texture;
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * 
	 * CALL THIS FUNCTION DIRECTLY IN THE MAIN CLASS basically sets the painting and
	 * pick tools to get the mouse input from the user
	 * 
	 */
	public static BranchGroup pantRocketTouch() {
		BranchGroup scene = new BranchGroup();

		paintRocket(scene);
		pickTool = new PickTool(scene);
		pickTool.setMode(PickTool.BOUNDS);

		return scene;
	}

	public static BranchGroup createScene() {
		BranchGroup scene = new BranchGroup();

		// Calling the function to check its functionality
		scene.addChild(pantRocketTouch());

		addLights(scene, CommonsEK.White);
		scene.compile(); // optimize scene BG

		return scene;
	}

	public Painting(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);
		canvas.addMouseListener(this); // NOTE: enable mouse clicking

		SimpleUniverse su = new SimpleUniverse(canvas); // create a SimpleUniverse
		CommonsEK.setEye(new Point3d(2, 2, 6.0));
		CommonsEK.defineViewer(su); // set the viewer's location

		sceneBG.compile();
		su.addBranchGraph(sceneBG); // attach the scene to SimpleUniverse

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(600, 600); // set the size of the JFrame
		frame.setVisible(true);
	}

	/* the main entrance of the application */
	public static void main(String[] args) {
		frame = new JFrame("XY's Assignment 4");
		frame.getContentPane().add(new Painting(createScene()));
	}

	public void mouseClicked(MouseEvent event) {
		int x = event.getX();
		int y = event.getY(); // mouse coordinates
		Point3d point3d = new Point3d(), center = new Point3d();
		canvas.getPixelLocationInImagePlate(x, y, point3d); // obtain AWT pixel in ImagePlate coordinates
		canvas.getCenterEyeInImagePlate(center); // obtain eye's position in IP coordinates

		Transform3D transform3D = new Transform3D(); // matrix to relate ImagePlate coordinates~
		canvas.getImagePlateToVworld(transform3D); // to Virtual World coordinates
		transform3D.transform(point3d); // transform 'point3d' with 'transform3D'
		transform3D.transform(center); // transform 'center' with 'transform3D'

		Vector3d mouseVec = new Vector3d();
		mouseVec.sub(point3d, center);
		mouseVec.normalize();
		pickTool.setShapeRay(point3d, mouseVec); // send a PickRay for intersection
		if (pickTool.pickClosest() != null) {
			PickResult pickResult = pickTool.pickClosest(); // obtain the closest hit
			Shape3D box = (Shape3D) pickResult.getNode(PickResult.SHAPE3D);
			Appearance app = box.getAppearance();
			/*
			 * Get results after clicking the painting Use the result to make appropriate
			 * changes
			 */
			if ((int) box.getUserData() == 0) { // retrieve 'UserData'
				System.out.println("Hi");
				app.setTexture(texState("MarbleTexture"));
				box.setUserData(1); // set 'UserData' to a new value
			}
			app.setTexture(texState("MarbleTexture"));

		}
	}
}