package codesTeamProject;

import java.util.Iterator;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnCollisionEntry;
import org.jogamp.java3d.WakeupOnCollisionExit;
import org.jogamp.vecmath.Color3f;

import codesEK280.CommonsEK;

/* This behavior of collision detection highlights the
    object when it is in a state of collision. */

public class CollisionDetectLines extends Behavior {
	private boolean inCollision;
	private Shape3D shape;
	private ColoringAttributes shapeColoring;
	private Appearance shapeAppearance;
	private WakeupOnCollisionEntry wEnter;
	private WakeupOnCollisionExit wExit;
	public static Boolean collided = true;

	public CollisionDetectLines(Shape3D s) {
		shape = s; // save the original color of 'shape"
		shapeAppearance = shape.getAppearance();
		shapeColoring = shapeAppearance.getColoringAttributes();
		///allow appearance to change transparency
		inCollision = false;
	}

	public void initialize() { // USE_GEOMETRY USE_BOUNDS
		wEnter = new WakeupOnCollisionEntry(shape, WakeupOnCollisionEntry.USE_GEOMETRY);
		wExit = new WakeupOnCollisionExit(shape, WakeupOnCollisionExit.USE_GEOMETRY);
		wakeupOn(wEnter); // initialize the behavior
	}

	public void processStimulus(Iterator<WakeupCriterion> criteria) {
		Color3f newClr = CommonsEK.Green;
		ColoringAttributes colour = new ColoringAttributes(newClr, ColoringAttributes.FASTEST);
		inCollision = !inCollision; // collision has taken place

		if (inCollision) { // change color to highlight 'shape'
			shapeAppearance.setColoringAttributes(colour);
			
			if (DialBehavior.stopped) {
				DoorIndicator.setSun(true);
				DoorIndicator.checkSun();
			}
			
			collided = true;
			wakeupOn(wExit); // keep the color until no collision
			
		} 
		
		else { // change color back to its original
			shapeAppearance.setColoringAttributes(shapeColoring);
			collided = false;
			wakeupOn(wEnter); // wait for collision happens
		}
		
	}
}