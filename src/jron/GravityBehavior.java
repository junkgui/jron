/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jron;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

/**
 * @author junkgui
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class GravityBehavior extends Behavior {
	
	private WakeupCriterion[] wakeupArray = new WakeupCriterion[3];
	private WakeupCondition wakeupCondition = null;
	
	TransformGroup[] particle;
	BranchGroup platforms;
	Geometry floor;
	Vector3d downVec = new Vector3d(0.0, -1.0, 0.0);

	
	public GravityBehavior(TransformGroup[] particle, 
			BranchGroup platforms/*, Geometry floor*/) {
		this.particle = particle;
		this.platforms = platforms;
		/*this.floor = floor;*/
		
		wakeupArray = new WakeupCriterion[1];
		
		
		wakeupArray[0] = new WakeupOnElapsedFrames(5);
		wakeupCondition = new WakeupOr(wakeupArray);
	}
	/* (non-Javadoc)
	 * @see javax.media.j3d.Behavior#initialize()
	 */
	public void initialize() {
		wakeupOn(wakeupCondition);
	}
	/* (non-Javadoc)
	 * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
	 */
	public void processStimulus(Enumeration criteria) {
		Transform3D t3d = new Transform3D();
		Vector3d v3d = new Vector3d();
		while (criteria.hasMoreElements()) {
			WakeupCriterion wakeUp = (WakeupCriterion) criteria.nextElement();
			//every N frames, check for a collision
			if (wakeUp instanceof WakeupOnElapsedFrames) {
				//create a PickBounds
				PickTool pickTool = new PickTool(platforms);
				pickTool.setMode(PickTool.GEOMETRY);
				for (int i = 0; i < particle.length; i++) {
					double[] d = new double[3];
					particle[i].getTransform(t3d);
					t3d.get(v3d);
					v3d.get(d);
					Point3d p3d = new Point3d(d);
					//System.out.println(p3d+"\n"+downVec);
					pickTool.setShapeRay(p3d, downVec);
					PickResult[] resultArray = pickTool.pickAll();
					
					if (i == 0) {
						if (!checkCollision(i, resultArray)) {
					
							PlayFrame.getActorKeyBehavior().setFalling(true);
						} else {
							PlayFrame.getActorKeyBehavior().setFalling(false);
						}
					}
				}
				
			}
		}
		//assign the next WakeUpCondition, so we are notified again
		wakeupOn(wakeupCondition);
	}
	/**
	 * @param i
	 * @param resultArray
	 */
	private boolean checkCollision(int actor, PickResult[] resultArray) {
		//System.out.println(resultArray);
		if (resultArray == null 
			|| resultArray.length == 0 
			|| resultArray[0] == null) {
			return false;
		} else {
			return true;
		}
	}
}
