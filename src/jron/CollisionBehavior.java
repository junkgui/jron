package jron;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.PickBounds;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
/*
 * This behavior detects collisions between the branch of a scene, and a
 * collision object. The Java 3D 1.2 picking utilities are used to implement
 * collision detection. The objects in the scene that are collidable should have
 * their user data set. The collision object's user data is used to ignore
 * collisions between the object and itself.
 * 
 * When a collision is detected the trajectory of the collision object is
 * reversed (plus a small random factor) and an Appearance object is modified.
 * 
 * When a collision is not detected the collision object is moved along its
 * current trajectory and the Appearance color is reset.
 * 
 * Collision checking is run after every frame.
 */
class CollisionBehavior extends Behavior {
	//the wake up condition for the behavior
	protected WakeupCondition m_WakeupCondition = null;
	//how often we check for a collision
	private static final int ELAPSED_FRAME_COUNT = 1;
	//the branch that we check for collisions
	private BranchGroup pickRoot = null;
	//the collision object that we are controlling
	private TransformGroup[] collisionObject = null;
	//the appearance object that we are controlling
	//private Appearance objectAppearance = null;
	//cached PickBounds object used for collision detection
	private PickBounds pickBounds = null;
	//cached Material objects that define the collided and
	//missed colors
	//private Material collideMaterial = null;
	//private Material missMaterial = null;
	//the current trajectory of the object
	private Vector3d incrementVector[] = null;
	//the current position of the object
	private Vector3d positionVector[] = null;
	
	public CollisionBehavior(BranchGroup pickRoot,
			TransformGroup[] collisionObject, 
			Vector3d[] posVector,
			Vector3d[] incVector) {
		//save references to the objects
		this.pickRoot = pickRoot;
		this.collisionObject = collisionObject;
		//this.objectAppearance = app;
		incrementVector = incVector;
		positionVector = posVector;
		//create the WakeupCriterion for the behavior
		WakeupCriterion criterionArray[] = new WakeupCriterion[1];
		criterionArray[0] = new WakeupOnElapsedFrames(ELAPSED_FRAME_COUNT);
		//objectAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		for (int i = 0; i < collisionObject.length; i++) {
			collisionObject[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			collisionObject[i].setCapability(Node.ALLOW_BOUNDS_READ);
		}
		//save the WakeupCriterion for the behavior
		m_WakeupCondition = new WakeupOr(criterionArray);
	}
	
	public void initialize() {
		//apply the initial WakeupCriterion
		wakeupOn(m_WakeupCondition);
		
	}
	
	protected void moveCollisionObject() {
		for (int i = 0; i < positionVector.length; i++) {
			Transform3D t3d = new Transform3D();
			positionVector[i].add(incrementVector[i]);
			t3d.setTranslation(positionVector[i]);
			//System.out.println(positionVector);
			collisionObject[i].setTransform(t3d);
		}
	}
	
	public boolean checkCollision(int collisionIndex, PickResult[] resultArray) {
		//System.out.println(resultArray);
		if (resultArray == null || resultArray.length == 0 || resultArray[0] == null)
			return false;
		/*
		 * We use the user data on the nodes to ignore the case of the
		 * collisionObject having collided with itself! The user data also gives
		 * us a good mechanism for reporting the collisions.
		 */
		for (int n = 0; n < resultArray.length; n++) {
			//System.out.println("something "+resultArray[n]);
			int numIntersections = resultArray[n].numIntersections();
			for (int i = 0; i < numIntersections; i++) {
				PickIntersection pi = resultArray[n].getIntersection(i);
				if (pi.getDistance() < 1) {
					//System.out.println(resultArray[n]);
					//int numNodes = resultArray[n].
					//for (int j = 0; )
					Node node = resultArray[n].getObject();
					System.out.println(node);
					if (node == PlayFrame.COLLISION_WALLS[PlayFrame.WALL_FRONT]) {
//						System.out.println("front");
						
						incrementVector[collisionIndex].z = -incrementVector[collisionIndex].z;
					} else if (node == PlayFrame.COLLISION_WALLS[PlayFrame.WALL_BACK]) {
//						System.out.println("back");
						
						incrementVector[collisionIndex].z = -incrementVector[collisionIndex].z;
					} else if (node == PlayFrame.COLLISION_WALLS[PlayFrame.WALL_LEFT]) {
//						System.out.println("left");
						incrementVector[collisionIndex].x = -incrementVector[collisionIndex].x;
						
					} else if (node == PlayFrame.COLLISION_WALLS[PlayFrame.WALL_RIGHT]) {
//						System.out.println("right");
						incrementVector[collisionIndex].x = -incrementVector[collisionIndex].x;
					} else if (collisionIndex >= 3 && "Me".equals(node.getUserData())) {
						incrementVector[collisionIndex].negate();
					} else if (collisionIndex < 3 && "Enemy".equals(node.getUserData())) {
						incrementVector[collisionIndex].negate();	
					}
					return true;
				}
			}
		}
		return false;
	}
	public void processStimulus(Enumeration criteria) {
		while (criteria.hasMoreElements()) {
			WakeupCriterion wakeUp = (WakeupCriterion) criteria.nextElement();
			//every N frames, check for a collision
			if (wakeUp instanceof WakeupOnElapsedFrames) {
				//create a PickBounds
				PickTool pickTool = new PickTool(pickRoot);
				pickTool.setMode(PickTool.GEOMETRY);
//				BoundingSphere bounds = (BoundingSphere) collisionObject
//						.getBounds();
//				pickBounds = new PickBounds(new BoundingSphere(new Point3d(
//						positionVector.x, positionVector.y, positionVector.z),
//						bounds.getRadius()));
				for (int i = 0; i < collisionObject.length; i++) {
					double[] d = new double[3];
					getPositionVector()[i].get(d);
					Point3d p3d = new Point3d(d);
					pickTool.setShapeRay(p3d, getIncrementVector()[i]);
					PickResult[] resultArray = pickTool.pickAll();
					
					checkCollision(i, resultArray);
				}
				
				moveCollisionObject();
				//System.out.println("running");
			}
		}
		//assign the next WakeUpCondition, so we are notified again
		wakeupOn(m_WakeupCondition);
	}
	/**
	 * @return Returns the incrementVector.
	 */
	public Vector3d[] getIncrementVector() {
		return incrementVector;
	}
	/**
	 * @param incrementVector The incrementVector to set.
	 */
	public void setIncrementVector(Vector3d[] incrementVector) {
		this.incrementVector = incrementVector;
	}
	/**
	 * @return Returns the positionVector.
	 */
	public Vector3d[] getPositionVector() {
		return positionVector;
	}
	/**
	 * @param positionVector The positionVector to set.
	 */
	public void setPositionVector(Vector3d[] positionVector) {
		this.positionVector = positionVector;
	}
}