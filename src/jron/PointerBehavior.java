package jron;
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;

public class PointerBehavior extends Behavior {
    private WakeupOnAWTEvent wakeupMoved = null;
    private WakeupOnAWTEvent wakeupReleased = null;
    private WakeupCriterion[] wakeupArray = null;
    private WakeupCondition wakeupCondition = null;
    
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
    
    TransformGroup m_TransformGroup = null;
    private PlayFrame component = null;
    
    public PointerBehavior(TransformGroup tg, PlayFrame component) {
        m_TransformGroup = tg;
        this.component = component;
        wakeupMoved = new WakeupOnAWTEvent(MouseEvent.MOUSE_MOVED);
        wakeupReleased = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
        wakeupArray = new WakeupCriterion[] {wakeupMoved, wakeupReleased} ;
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
            }
        }
        //Set wakeup criteria for next time
        wakeupOn(wakeupCondition);
    }
    //Process a keyboard event
    private void processAWTEvent(AWTEvent[] events) {
        for( int n = 0; n < events.length; n++) {
            if( events[n] instanceof MouseEvent) {
                MouseEvent event = (MouseEvent) events[n];
                if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                	nextDisc = ++nextDisc % 3;
                    component.fireDisc(nextDisc);

                
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
                    m_TransformGroup.getTransform(t3d);
                    t3d.setTranslation( translate );
                    m_TransformGroup.setTransform( t3d );
                }
            }
        }
    }
}