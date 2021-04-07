package codesTeamProject;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;

public class DialBehavior extends Behavior {

	private RotationInterpolator r;
	private int key;
	
	private Boolean paused;
	public static Boolean stopped = false;
	private WakeupOnAWTEvent wEnter;
	
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		wEnter = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
		wakeupOn(wEnter);
	}

	@Override
	public void processStimulus(Iterator<WakeupCriterion> arg0) {
		// TODO Auto-generated method stub
		KeyEvent event = (KeyEvent) wEnter.getAWTEvent() [0];
		if(key == event.getKeyCode()) {
			paused = !paused;			///if paused, unpause, if unpaused, pause
		}
		///pause or play the rotation interpolator r
		if(paused) {
			r.getAlpha().pause();
			stopped = true;
		}
		else {
			r.getAlpha().resume();
			stopped = false;
		}
		wakeupOn(wEnter);
	}
	
	public DialBehavior(RotationInterpolator r, int key) {
		this.r = r;
		this.key = key;
		paused = false;
	}
}