package codesTeamProject;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;

import org.jdesktop.j3d.examples.collision.Box;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.vecmath.*;

import codesEK280.CommonsEK;

public class WheelPuzzle extends JPanel {
		
	private static final long serialVersionUID = 1L;
	private static int count = 0;

	private static Point3d pt_zero = new Point3d(0d, 0d, 0d);
	
	private static Shape3D line (Point3f pt) {
		
		LineArray lineArr = new LineArray(2, LineArray.COLOR_3 | LineArray.COORDINATES);
		
		lineArr.setCoordinate(0, pt);
		lineArr.setCoordinate(1, new Point3f(0.0f, 0.0f, 0.0f));
		lineArr.setColor(0, new Color3f(0.0f,0.0f,0.0f));
		lineArr.setColor(1, new Color3f(0.0f,0.0f,0.0f));
		
		Appearance app = new Appearance();
		
		ColoringAttributes ca = new ColoringAttributes(new Color3f(0.0f,0.0f,0.0f), ColoringAttributes.FASTEST);
		app.setColoringAttributes(ca);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		
		return new Shape3D(lineArr, app);

	}
	
	private static TransformGroup createColumn (double scale, Vector3d pos) {	
		
		//Create base TG with 'scale' and 'position'
		Transform3D transM = new Transform3D();
		transM.set(scale, pos);  
		TransformGroup baseTG = new TransformGroup(transM);
		
		//Create a column as a box and add to 'baseTG'
		Shape3D shape = new Box(2.5, 4.5, 1.0);
		baseTG.addChild(shape);

		Appearance app = shape.getAppearance();
		ColoringAttributes ca = new ColoringAttributes();
		
		//Set column's color and make changeable
		ca.setColor(0.6f, 0.3f, 0.0f); 
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		app.setColoringAttributes(ca);
		
		CollisionDetectLines cd = new CollisionDetectLines(shape);
		cd.setSchedulingBounds(new BoundingSphere(pt_zero, 10d));
		
		baseTG.addChild(cd);

		return baseTG;
		
	}
	
	private static BranchGroup Cylinder (float scale, float zV) {
		
		BranchGroup BG = new BranchGroup();
		
		String names[] = {
			"Back",
			"Front"
		};
		
		Color3f colors[] = {
			new Color3f(0.0f, 0.0f, 1.0f),
			new Color3f(1.0f, 0.0f, 0.0f)
		};
		
		Appearance app = new Appearance();
		ColoringAttributes ca = new ColoringAttributes(CommonsEK.Grey, ColoringAttributes.FASTEST);
		
		app.setColoringAttributes(ca);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		app.setMaterial(AppearanceExtra.setMaterial(colors[count]));
		
		Cylinder cylinder = new Cylinder((1f) * scale, zV, app);
		cylinder.setUserData(0);				//Original value 
		cylinder.setName(names[count++]);		//Static variable to change the name based on how many times sphere is called
		
		Transform3D rotate = new Transform3D();
		rotate.rotX(Math.PI/2);

		TransformGroup tg1 = new TransformGroup();
		TransformGroup tg2 = new TransformGroup(rotate);
		
		tg2.addChild(cylinder);
		tg1.addChild(tg2);
		BG.addChild(tg1);
		
		return BG;
		
	}
	
	public static BranchGroup createMeasure() {
		
		BranchGroup BG = new BranchGroup();
		
		BG.addChild(line(new Point3f(1.0f, 5.0f, 0)));
		BG.addChild(line(new Point3f(-1.0f, 5.0f, 0)));
		BG.addChild(createColumn(0.5, new Vector3d(0, 4.35f, 0)));
		BG.addChild(Cylinder(3.0f, 1.0f));
	
		return BG;
		
	}
	
	public static BranchGroup createDial() {
		
		BranchGroup BG = new BranchGroup();
		
		Transform3D trans = new Transform3D();
		//trans.setTranslation(new Vector3f(0,0,1));
		
		TransformGroup tg1 = new TransformGroup(trans);
		TransformGroup tg2 = new TransformGroup();
		tg2.addChild(Cylinder(2, 1.5f));
		
		TransformGroup Col = new TransformGroup();
		Col.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Col.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		Shape3D shape = line(new Point3f( 0.0f,5.0f, 0.0f));
		
		CollisionDetectLines cd = new CollisionDetectLines(shape);
		cd.setSchedulingBounds(new BoundingSphere(pt_zero, 10d)); // detect column's collision
		
		//Col.addChild(cd); // add column with behavior of CollisionDector
		//tg2.addChild(Col);
		
		tg2.addChild(shape);
		
		RotationInterpolator ri1 = ri(500, tg2, 'x', new Point3d(0,0,0));
		BG.addChild(ri1);
		
		//Key presses that pause or resume the rotations depending on key pressed
		DialBehavior sb1 = new DialBehavior(ri1, KeyEvent.VK_Z);	//Press 'z' to stop the rotation
		sb1.setSchedulingBounds(new BoundingSphere());
		BG.addChild(sb1);
				
		tg1.addChild(tg2);
		BG.addChild(tg1);
		
		return BG;
		
	}
	
	public static RotationInterpolator ri (int rotationnumber, TransformGroup tg, char option, Point3d pos) {
		
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D axis = new Transform3D();
		
		switch (option) {
		
		case 'x':
			axis.rotX(Math.PI/2);
			break;
			
		case 'z':
			axis.rotZ(Math.PI/2);
			break;
			
		default: //Case Y
			axis.rotY(Math.PI/2);
			break;
			
		}
		
		Alpha a = new Alpha(-1, rotationnumber);
		RotationInterpolator rot = new RotationInterpolator(a, tg, axis, 0.0f, (float) Math.PI*2);
		rot.setSchedulingBounds(new BoundingSphere(pos, 100));
		
		return rot;
		
	}
	
	public static BranchGroup buildWin() {
		
		BranchGroup BG = new BranchGroup();
		TransformGroup TG = new TransformGroup();
		
		if (CollisionDetectLines.collided && DialBehavior.stopped) {
			AppearanceExtra.addptLights(TG, CommonsEK.Green);
			//RenderText.letters3D("Unlocked", 5.0d , CommonsEK.Yellow);
		}
		
		return BG;
	}

	/* A function to create and return the scene BranchGroup */
	public static BranchGroup createScene() {
		
		BranchGroup sceneBG = new BranchGroup();		     //Create 'objsBG' for content
		TransformGroup sceneTG = new TransformGroup();       //Create a TransformGroup (TG)
		sceneBG.addChild(sceneTG);	                         //Add TG to the scene BranchGroup
		
		sceneBG.addChild(AppearanceExtra.createBackground("backgroundLight.jpg"));

		AppearanceExtra.addLights(sceneTG);
		///sceneBG.addChild(createSceneGraph(Commons.canvas_3D));
		sceneBG.addChild(createMeasure());
		sceneBG.addChild(createDial());
		sceneBG.addChild(buildWin());
		///createContent(sceneBG);
		sceneBG.compile(); 		// optimize objsBG
		return sceneBG;
	}
	
	/*The main entrance of the application via 'MyGUI()' of "CommonXY.java" */
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				CommonsEK.setEye(new Point3d(0.0, 0.35, 15.0));
				new CommonsEK.MyGUI(createScene(), "Wheel Puzzle");
			}
		});
	}
	
}