package jron;
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;

public class ActorKeyBehavior extends Behavior {
	
	private WakeupCriterion[] wakeupArray = new WakeupCriterion[5];
	private WakeupCondition wakeupCondition = null;

	private final float TRANSLATE_LEFT = -0.2f;
	private final float TRANSLATE_RIGHT = 0.2f;
	private final float TRANSLATE_DOWN = 0.2f;
	private final float TRANSLATE_UP = -0.2f;
	
	private int fallingCount = 0;
	
	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean falling = false;
	private boolean fire = false;
	
	private Transform3D reset = new Transform3D();
	
    private WakeupOnAWTEvent wakeupMoved = null;
    private WakeupOnAWTEvent wakeupReleased = null;
    
    double posY = 0.0;
    double posX = 0.0;
    double posZ = 0.0;
    
    int nextDisc = 0;
    
    public double getX() {
        return posX;
    }
    
    public double getY() {
        return posY;
    }
    
    public double getZ() {
        return posZ;
    }
    
    TransformGroup actor = null;
    TransformGroup pointer = null;
    private PlayFrame component = null;

	public ActorKeyBehavior(TransformGroup actor, TransformGroup pointer, PlayFrame component) {
		this.component = component;
		this.actor = actor;
		this.pointer = pointer;
		this.actor.setCapability(ALLOW_BOUNDS_READ);
		this.actor.setCapability(ALLOW_BOUNDS_WRITE);
		
		this.actor.getTransform(reset);

		wakeupArray[0] = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
		wakeupArray[1] = new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED);
		wakeupArray[2] = new WakeupOnElapsedTime(10);
		wakeupArray[3] = new WakeupOnAWTEvent(MouseEvent.MOUSE_MOVED);
        wakeupArray[4] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
		
		wakeupCondition = new WakeupOr(wakeupArray);
	}
	
	//Override Behavior's initialize method to set up wakeup criteria
	public void initialize() {
		//Establish initial wakeup criteria
		wakeupOn(wakeupCondition);
	}
	//Override Behavior's stimulus method to handle the event.
	public void processStimulus(Enumeration criteria) {
		WakeupOnAWTEvent ev;
		WakeupCriterion genericEvt;
		AWTEvent[] events;
		while (criteria.hasMoreElements()) {
			genericEvt = (WakeupCriterion) criteria.nextElement();
			if (genericEvt instanceof WakeupOnAWTEvent) {
				ev = (WakeupOnAWTEvent) genericEvt;
				events = ev.getAWTEvent();
				processAWTEvent(events);
			} else if (genericEvt instanceof WakeupOnElapsedTime) {
				transform();
			}
		}
		//Set wakeup criteria for next time
		wakeupOn(wakeupCondition);
	}
	//Process a keyboard event
	private void processAWTEvent(AWTEvent[] events) {
		//System.out.println(pointer.getBounds().toString());
		for (int n = 0; n < events.length; n++) {
            if( events[n] instanceof MouseEvent) {
                MouseEvent event = (MouseEvent) events[n];
                //System.out.println(MouseEvent.MOUSE_RELEASED+", "+event.getID()+" "+event.getPoint());
                if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                	fire = true;
                
                } else if( event.getID() == MouseEvent.MOUSE_MOVED ) {
                    int currentX = event.getX();
                    int currentY = event.getY();
                    int totalX = component.getBounds().width;
                    int totalY = component.getBounds().height;
                    posY = -(PlayFrame.LOW_Y_WALL+(((double) currentY)/((double) totalY))*(PlayFrame.HIGH_Y_WALL-PlayFrame.LOW_Y_WALL));
                    double xPercent = ((double) currentX)/ ((double) totalX);
                    double xMode = xPercent*4.0f;
                    posX = 0.0;
                    posZ = 0.0;
                    double xLength = (double)(PlayFrame.HIGH_X_WALL - PlayFrame.LOW_X_WALL);
                    double zLength = (double)(PlayFrame.LOW_Z_WALL - PlayFrame.HIGH_Z_WALL);
                    //System.out.println("out: "+xPercent+" "+xMode+" "+xLength+" "+zLength);
                    if (xMode >= 0.0 && xMode < 1.0) {
                        posX = -(PlayFrame.LOW_X_WALL + xLength*xMode);
                        posZ = PlayFrame.LOW_Z_WALL;
                    } else if (xMode >= 1.0 && xMode < 2.0) {
                        xMode -= 1.0;
                        posX = PlayFrame.LOW_X_WALL;
                        posZ = PlayFrame.LOW_Z_WALL-(zLength*xMode);
                    } else if (xMode >= 2.0 && xMode < 3.0) {
                        xMode -= 2.0;
                        posX = PlayFrame.LOW_X_WALL + xLength*xMode;
                        posZ = PlayFrame.HIGH_Z_WALL;
                    } else {
                        xMode -= 3.0;
                        posX = PlayFrame.HIGH_X_WALL;
                        posZ = (PlayFrame.HIGH_Z_WALL+(zLength*xMode));
                    }
                    Vector3d translate = new Vector3d(posX, posY, posZ);
                    //System.out.println(posX+", "+posY+", "+posZ);
                    Transform3D t3d = new Transform3D();
                    pointer.getTransform(t3d);
                    t3d.setTranslation( translate );
                    pointer.setTransform( t3d );
                }
            }

			if (events[n] instanceof KeyEvent) {
				KeyEvent eventKey = (KeyEvent) events[n];
				if (eventKey.getID() == KeyEvent.KEY_PRESSED) {
					int keyCode = eventKey.getKeyCode();
					int keyChar = eventKey.getKeyChar();
					
					switch (keyCode) {
						case KeyEvent.VK_LEFT :
							left = true;
							break;
						case KeyEvent.VK_RIGHT :
							right = true;
							break;
						case KeyEvent.VK_UP :
							up = true;
							break;
						case KeyEvent.VK_DOWN :
							down = true;
							break;
						case KeyEvent.VK_A :
							left = true;
							break;
						case KeyEvent.VK_D :
							right = true;
							break;
						case KeyEvent.VK_W :
							up = true;
							break;
						case KeyEvent.VK_S :
							down = true;
							break;
						
					}
					
				}
				if (eventKey.getID() == KeyEvent.KEY_RELEASED) {
					int keyCode = eventKey.getKeyCode();
					int keyChar = eventKey.getKeyChar();
					
					switch (keyCode) {
						case KeyEvent.VK_LEFT :
							left = false;
							break;
						case KeyEvent.VK_RIGHT :
							right = false;
							break;
						case KeyEvent.VK_UP :
							up = false;
							break;
						case KeyEvent.VK_DOWN :
							down = false;
							break;
						case KeyEvent.VK_A :
							left = false;
							break;
						case KeyEvent.VK_D :
							right = false;
							break;
						case KeyEvent.VK_W :
							up = false;
							break;
						case KeyEvent.VK_S :
							down = false;
							break;
						case KeyEvent.VK_SPACE :
							fire = true;
							break;
					}
					
				}
			}
		}
	}
	/**
	 * 
	 */
	private void transform() {
		//System.out.println(left+" "+right+" "+up+" "+down);
		Vector3f translate = new Vector3f();
		Transform3D t3d = new Transform3D();
		actor.getTransform(t3d);
		t3d.get(translate);
		if (left)
			translate.x += TRANSLATE_LEFT;
			//break;
		if (right)
			translate.x += TRANSLATE_RIGHT;
			//break;
		if (up)
			translate.z += TRANSLATE_UP;
			//break;
		if (down)
			translate.z += TRANSLATE_DOWN;
		if (falling) {
			double val = Math.max(fallingCount-3, 1.0);
			double secondsSquare = (val*val)/100;
			if (fallingCount <= 3) {
				translate.y += 0.1;
			} else {
				translate.y -= 0.1*secondsSquare;
			}
			fallingCount++;
		}
		if (fire) {
			nextDisc = ++nextDisc % 3;
            component.fireDisc(nextDisc);
            fire = false;
		}
		
		if (translate.y < -20.0) {
			reset();
			actor.setTransform(reset);
		} else {
			t3d.setTranslation(translate);
			actor.setTransform(t3d);
		}
		
		
		//System.out.println(pointer.getParent().getBounds().toString());
	}
	
	public void reset() {
		up = false;
		down = false;
		left = false;
		right = false;
		falling = false;
		fallingCount = 0;
	}
	
	/**
	 * @return Returns the down.
	 */
	public boolean isDown() {
		return down;
	}
	/**
	 * @return Returns the left.
	 */
	public boolean isLeft() {
		return left;
	}
	/**
	 * @return Returns the right.
	 */
	public boolean isRight() {
		return right;
	}
	/**
	 * @return Returns the up.
	 */
	public boolean isUp() {
		return up;
	}
	/**
	 * @return Returns the falling.
	 */
	public boolean isFalling() {
		return falling;
	}
	/**
	 * @param falling The falling to set.
	 */
	public void setFalling(boolean falling) {
		if (this.falling && !falling) {
			fallingCount = 0;
			Vector3f translate = new Vector3f();
			Transform3D t3d = new Transform3D();
			actor.getTransform(t3d);
			t3d.get(translate);
			Vector3d reseter = new Vector3d();
			reset.get(reseter);
			translate.y = (float) reseter.y;
			t3d.setTranslation(translate);
			actor.setTransform(t3d);
		}
		this.falling = falling;
	}
}