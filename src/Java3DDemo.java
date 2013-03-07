

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ncsa.j3d.loaders.dxf.DXFLoader;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.vp.*;
import com.sun.j3d.utils.picking.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import java.util.*;


public class Java3DDemo extends java.awt.Frame {
    
    public int rotationX = 0;
    public int rotationY = 0;
    
    public float startx = 0.0f;
    public float starty = -3.0f;
    public float startz = -3.0f;
    public Point3f pntStart = new Point3f(startx, starty, startz);
    
    public BranchGroup wallsBG;
    public PickCanvas pc;
    public Point3f[] point;
    public PositionPathInterpolator path;
    
    public Java3DDemo() {
        setLayout(new BorderLayout());
        
        GraphicsConfiguration config =
        SimpleUniverse.getPreferredConfiguration();
        
        Canvas3D canvas = new Canvas3D(config);
        
        add("Center", canvas);
        
        SimpleUniverse su = new SimpleUniverse(canvas);
        
        BranchGroup scene = createSceneGraph();
        
        pc = new PickCanvas(canvas, wallsBG);
        pc.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        pc.setTolerance((float)0.0);
        
        ViewingPlatform viewingPlatform = su.getViewingPlatform();
        
        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        viewingPlatform.setNominalViewingTransform();
        
        //scene.compile();
        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
        orbit.setSchedulingBounds(bounds);
        viewingPlatform.setViewPlatformBehavior(orbit);
        su.getViewingPlatform().setNominalViewingTransform();
        su.addBranchGraph(scene);
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == e.VK_UP) {
                    startz += .1;
                } else if (code == e.VK_DOWN) {
                    startz -= .1;
                } else if (code == e.VK_LEFT) {
                    startx += .1;
                } else if (code == e.VK_RIGHT) {
                    startx -= .1;
                }
                System.out.println(startx +", "+starty+", "+startz);
                pntStart = new Point3f(startx, starty, startz);
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            int count = 0;
            public void mouseClicked(MouseEvent e) {
                pc.setShapeLocation(e);
                
                PickResult pr = pc.pickAny();
                Point3f pntDest = new Point3f(pr.getIntersection(0).getPointCoordinates());
                //path.setPosition(count, pnt);
                Vector distances = new Vector();
                Vector points = new Vector();
                double totalDistance = 0.0;
                PickTool pt = new PickTool(wallsBG);
                pt.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
                //pt.setTolerance((float)0.0);
                while (totalDistance < 150) {
                    try {
                    
                    points.add(pntStart);
                    double distance = pntStart.distance(pntDest); 
                    totalDistance += distance;
                    distances.add(new Double(distance));
                    
                    Vector3d bounce = null;
                    if (pntDest.x == 12.0f) {
                        //System.out.println("X");
                        bounce = new Vector3d((double)-(pntDest.x - pntStart.x),
                                                    0.0,
                                                    (double)(pntDest.z - pntStart.z));
                        pntDest = new Point3f(pntDest.x-0.0001f, pntDest.y, pntDest.z);
                    } else if (pntDest.x == -12.0f) {
                        //System.out.println("X");
                        bounce = new Vector3d((double)-(pntDest.x - pntStart.x),
                                                    0.0,
                                                    (double)(pntDest.z - pntStart.z));
                        pntDest = new Point3f(pntDest.x+0.0001f, pntDest.y, pntDest.z);                            
                    } else if (pntDest.z == -60.0f) {
                        //System.out.println("Y");
                        bounce = new Vector3d((double)(pntDest.x - pntStart.x),
                                                    0.0,
                                                    (double)-(pntDest.z - pntStart.z));
                        pntDest = new Point3f(pntDest.x, pntDest.y, pntDest.z+0.0001f);
                    } else if (pntDest.z == 1.0f) {
                        //System.out.println("X");
                        bounce = new Vector3d((double)pntDest.x - pntStart.x,
                                                    0.0,
                                                    (double)-(pntDest.z - pntStart.z));
                        pntDest = new Point3f(pntDest.x, pntDest.y, pntDest.z-0.0001f);
                    }
                   
                    //System.out.println(bounce.toString());
                    bounce.normalize();
                    //System.out.println("distance=" +distance +" pntStart="+pntStart+" pntDest="+pntDest+" bounce="+bounce);
                    pt.setShapeRay(new Point3d(pntDest), bounce);
                    pntStart = pntDest;
                    PickResult pickres = pt.pickAny();
                    //System.out.println(pickres.toString());
                    PickIntersection pickint = pickres.getIntersection(0);
                    //System.out.println(pickint.toString());
                    Point3d p3d = pickint.getPointCoordinates();
                    //System.out.println("dest="+p3d);
                    pntDest = new Point3f(p3d);
                    } catch (Exception ex) {
                        System.out.println("boom");
                        ex.printStackTrace();
                    }
                //count = ++count%5;
                //point[0] = pnt;
                //rotationX = e.getX();
                //rotationY = e.getY();6                //Dimension d = getSize();
                // Point3f[] point = new Point3f[] {new Point3f(-12.0f,1.0f,-20.0f), new Point3f(12.0f,1.0f,-30.0f), new Point3f(-12.0f,1.0f,-40.0f), new Point3f(12.0f,1.0f,-50.0f), new Point3f(-12.0f,1.0f,-80.0f), new Point3f(12.0f,1.0f,-70.0f)};
                // path.setPathArrays(new float[] {0.0f,0.2f,0.4f,0.6f,0.8f,1.0f}, point);
                }
                points.add(pntDest);
                totalDistance += 10.0f;
                float currentTiming =0.0f;
                float[] timings = new float[distances.size()+1];
                Point3f[] pointsArray = new Point3f[timings.length];
                for (int i = 0; i < timings.length-1; i++) {
                    timings[i] = currentTiming;
                    currentTiming += ((Double)distances.get(i)).floatValue()/totalDistance;
                    pointsArray[i] = (Point3f) points.get(i);
                }
                timings[timings.length-1] = 1.0f;
                pointsArray[timings.length-1] = new Point3f(0.0f,-3.0f,-3.0f);//(Point3f) points.get(timings.length-1);
                path.setPathArrays(timings, pointsArray);
                Alpha a = path.getAlpha();
                a.setTriggerTime((new Date()).getTime()-a.getStartTime()+100);
               
            }
        });
    }
    
    public BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();
        //Transform3D rotate = new Transform3D();
        //Transform3D tempRotate = new Transform3D();
        //rotate.rotZ(Math.PI/6.0d);
        //tempRotate.rotY(Math.PI/5.0d);
        //rotate.mul(tempRotate);
        //TransformGroup objRotate = new TransformGroup(rotate);
        
        //TransformGroup objSpin = new TransformGroup(rotate);
        //objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        //objRoot.addChild(objRotate);
        //objRotate.addChild(objSpin);
        //objSpin.addChild(new ColorCube(0.4));
        //Alpha rotationAlpha = new Alpha(-1, 4000);
        //RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objSpin);
        //ObjectFile f = new ObjectFile();
        DXFLoader dxfl = new DXFLoader();
        BranchGroup bg = new BranchGroup();
        SharedGroup disc = null;
        TransformGroup n = null;
        Appearance blueAppearance = null;
        try {
            bg = dxfl.LoadDXF(new InputStreamReader(getClass().getResourceAsStream("/disk2.dxf")));
            Enumeration e = bg.getAllChildren();
            n=(TransformGroup)bg.getChild(1);
            while (e.hasMoreElements()) {
                Group g = (Group) e.nextElement();
                //System.out.println(g);
            }
            int numChildren = bg.numChildren();
            for (int i=0; i < numChildren; i++) {
                bg.removeChild(0);
            }
            //bg.addChild(n);
            //System.out.println(n.getChild(0));
            disc = (SharedGroup)((Link)n.getChild(0)).getSharedGroup().cloneNode(true);
            
            Group sg = (Group) ((Link)n.getChild(0)).getSharedGroup();
            e = sg.getAllChildren();
            //bg.addChild(sg.getChild(0).cloneNode(true));
            while (e.hasMoreElements()) {
                Group tg1 = (Group) e.nextElement();
                Object tg2 = tg1.getChild(0);
                //System.out.println("tg1 = " + tg1 + " tg2 = " +tg2);
                blueAppearance = ((Shape3D) tg2).getAppearance();
            }
            bg = new BranchGroup();
            //bg.addChild(n.getChild(0).cloneNode(true));
            //bg = sg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        TransformGroup farPad1 = new TransformGroup();
        TransformGroup farPad2 = new TransformGroup();
        TransformGroup farPad3 = new TransformGroup();
        TransformGroup nearPad1 = new TransformGroup();
        TransformGroup nearPad2 = new TransformGroup();
        TransformGroup nearPad3 = new TransformGroup();
        
        TransformGroup disc1 = new TransformGroup();
        
        farPad1.addChild(n.getChild(0).cloneNode(true));
        farPad2.addChild(n.getChild(0).cloneNode(true));
        farPad3.addChild(n.getChild(0).cloneNode(true));
        nearPad1.addChild(n.getChild(0).cloneNode(true));
        nearPad2.addChild(n.getChild(0).cloneNode(true));
        nearPad3.addChild(n.getChild(0).cloneNode(true));
        
        disc1.addChild(n.getChild(0).cloneNode(true));
        
        
        
        
        disc1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        farPad2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        farPad3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        nearPad1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        nearPad2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        nearPad3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        BranchGroup fpbg1 = new BranchGroup();
        fpbg1.addChild(disc1);
        
        bg.addChild(farPad1);
        bg.addChild(farPad2);
        bg.addChild(farPad3);
        bg.addChild(nearPad1);
        bg.addChild(nearPad2);
        bg.addChild(nearPad3);
        
        //bg.addChild(disc1);
        
        System.out.println("built groups");
        
        
        Transform3D rotateLevel = new Transform3D();
        rotateLevel.rotX(-Math.PI/2.0);
        
        Transform3D trans = new Transform3D();
        trans.setTranslation(new Vector3f(-7.0f, -6.0f, -20.0f));
        trans.mul(rotateLevel);
        farPad1.setTransform(trans);
        
        trans.setTranslation(new Vector3f(0.0f, -6.0f, -20.0f));
        farPad2.setTransform(trans);
        
        trans.setTranslation(new Vector3f(7.0f, -6.0f, -20.0f));
        farPad3.setTransform(trans);
        
        trans.setTranslation(new Vector3f(-7.0f, -6.0f, -55.0f));
        nearPad1.setTransform(trans);
        
        trans.setTranslation(new Vector3f(0.0f, -6.0f, -55.0f));
        nearPad2.setTransform(trans);
        
        trans.setTranslation(new Vector3f(7.0f, -6.0f, -55.0f));
        nearPad3.setTransform(trans);
        
        
        
        //Transform3D scale = new Transform3D();
        //scale.setScale(0.2);
        //trans.mul(scale);
        //disc1.setTransform(trans);
        
        
        Transform3D yAxis = new Transform3D();
        //yAxis.setTranslation(new Vector3f(0.0f, 0.0f, -20.0f));
        yAxis.mul(rotateLevel);
        Alpha rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate1 = new RotationInterpolator(rotationAlpha, disc1, yAxis, 0.0f, (float) Math.PI*2.0f);
        
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        fpbg1.addChild(rotate1);
        
        
        yAxis = new Transform3D();
        rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate2 = new RotationInterpolator(rotationAlpha, farPad2, yAxis, 0.0f, (float) Math.PI*2.0f);
        //bg.addChild(rotate2);
        bounds = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        yAxis = new Transform3D();
        rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate3 = new RotationInterpolator(rotationAlpha, farPad3, yAxis, 0.0f, (float) Math.PI*2.0f);
        //bg.addChild(rotate3);
        bounds = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        yAxis = new Transform3D();
        rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate4 = new RotationInterpolator(rotationAlpha, nearPad1, yAxis, 0.0f, (float) Math.PI*2.0f);
        //bg.addChild(rotate4);
        bounds = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        yAxis = new Transform3D();
        rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate5 = new RotationInterpolator(rotationAlpha, nearPad2, yAxis, 0.0f, (float) Math.PI*2.0f);
        //bg.addChild(rotate5);
        bounds = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        yAxis = new Transform3D();
        rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotate6 = new RotationInterpolator(rotationAlpha, nearPad3, yAxis, 0.0f, (float) Math.PI*2.0f);
        //bg.addChild(rotate6);
        bounds = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        rotate1.setSchedulingBounds(bounds);
        
        
        
        //bg.addChild(fpbg1);
        TransformGroup discTG = new TransformGroup();
        
        discTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        discTG.addChild(fpbg1);
        trans.setTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
        Transform3D scale = new Transform3D();
        scale.setScale(0.15);
        trans.mul(scale);
        discTG.setTransform(trans);
        
        
        TransformGroup finalDisc = new TransformGroup();
        
        finalDisc.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        finalDisc.addChild(discTG);
        Alpha alpha = new Alpha(1, 10000);
        yAxis.rotX(2*Math.PI);
        point = new Point3f[] {new Point3f (0.0f,-3.0f,-20.0f), new Point3f(0.0f, -3.1f,-20.0f), new Point3f (0.0f,-3.0f,-20.0f)};//new Point3f(-12.0f,-3.0f,-10.0f), new Point3f(12.0f,-3.0f,-20.0f), new Point3f(-12.0f,-3.0f,-30.0f), new Point3f(12.0f,-3.0f,-40.0f), new Point3f(-12.0f,-3.0f,-50.0f), new Point3f(12.0f,-3.0f,-60.0f)};
        path = new PositionPathInterpolator(alpha, 
                                            finalDisc, 
                                            yAxis, 
                                            new float[] {0.0f,0.5f, 1.0f}, 
                                            point) {
                                            };
        //path.wakeupOn(new WakeupOnAWTEvent(AWTEvent.MOUSE_EVENT_MASK));
        
        BoundingSphere bounds2 = new BoundingSphere(new Point3d(-7.0,-6.0,-24.0), 1000.0);
        path.setSchedulingBounds(bounds2);
        
        finalDisc.addChild(path);
        
        
        bg.addChild(finalDisc);
        
        GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        
        Point3f[] pts = new Point3f[8];
        pts[0] = new Point3f(-12.0f, -8.0f, -60.0f);
        pts[1] = new Point3f(12.0f, -8.0f, -60.0f);
        pts[2] = new Point3f(-12.0f, 8.0f, -60.0f);
        pts[3] = new Point3f(-12.0f, -8.0f, 1.0f);
        pts[4] = new Point3f(-12.0f, 8.0f, 1.0f);
        pts[5] = new Point3f(12.0f, -8.0f, 1.0f);
        pts[6] = new Point3f(12.0f, 8.0f, -60.0f);
        pts[7] = new Point3f(12.0f, 8.0f, 1.0f);
        
        int[] indices = {
            //0, 3, 4, 2,
            2, 4, 3, 0,
            //0, 1, 5, 3,
            3, 5, 1, 0,
            //0, 2, 6, 1,
            1, 6, 2, 0,
            //7, 5, 1, 6,
            6, 1, 5, 7,
            //7, 6, 3, 5,
            4, 2, 6, 7,
            //7, 4, 3, 5
            3, 4, 7, 5
        };
        
        gi.setCoordinates(pts);
        gi.setCoordinateIndices(indices);
        NormalGenerator ng = new NormalGenerator();
        ng.setCreaseAngle((float) Math.toRadians(89));
        ng.generateNormals(gi);
        
        GeometryArray cube = gi.getGeometryArray();
        
        //IndexedQuadArray iqa = new IndexedQuadArray(8, GeometryArray.COORDINATES, 20);
        //iqa.setCoordinates(0, pts);
        //iqa.setCoordinateIndices(0, indices);
        
        Appearance app = new Appearance();
        app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        ColoringAttributes blue = new ColoringAttributes(new Color3f(0.0f,0.0f,1.0f), ColoringAttributes.SHADE_GOURAUD);
        //app.setColoringAttributes(blue);
        Material material = new Material(new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.0f, 0.0f, 0.1f), new Color3f(0.1f, 0.1f, 0.1f),new Color3f(0.0f, 0.0f, 0.1f), 17.0f);
        material.setCapability(Material.ALLOW_COMPONENT_READ);
        material.setLightingEnable(true);
        //material.setDiffuseColor(new Color3f(0.0f, 0.0f, 0.5f));
        //material.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        //material.setShininess(17.0f);
        app.setMaterial(material);
        
        Shape3D shape = new Shape3D(cube);
        shape.setAppearance(app);
        PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
        
        wallsBG = new BranchGroup();
        wallsBG.addChild(shape);
        
        bg.addChild(wallsBG);
        //farPad1Trans = new Transform3D();//new Matrix4f(1.0f,0.0f,-1.0f,0.0f,
        //                                            0.0f,2.0f,0.0f,0.0f,
        //                                            1.0f,0.0f,1.0f,3.0f,
        //                                            0.0f,0.0f,0.0f,2.0f));
        
        /*farPad1.setTransform(farPad1Trans);
        farPad2.setTransform(farPad2Trans);
        nearPad1.setTransform(nearPad1Trans);
        nearPad2.setTransform(nearPad2Trans);*/
        
        
        /*Scene s = null;
        try {
            s = dxfl.load("C:/jdk1.3.1/demo/java3d/geometry/galleon.obj");
        } catch (Exception e) {
            e.printStackTrace();
        }
         
         
        objRoot.addChild(s.getSceneGroup());
         */
        //BoundingSphere bounds =
        //    new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
        
        Color3f light1Color = new Color3f(0.3f, 0.1f, 0.9f);
        Vector3f light1Direction  = new Vector3f(0.0f, -1.0f, -1.0f);
        Color3f light2Color = new Color3f(0.3f, 0.1f, 0.9f);
        Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);
        
        DirectionalLight light1
        = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        bg.addChild(light1);
        
        DirectionalLight light2
        = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        bg.addChild(light2);
        
        Color3f light3Color = new Color3f(4.0f, 0.0f, 0.0f);
        Vector3f light3Direction  = new Vector3f(0.0f, 0.0f, -1.0f);
        
        DirectionalLight light3
        = new DirectionalLight(light3Color, light3Direction);
        light2.setInfluencingBounds(bounds);
        discTG.addChild(light3);
        //rotator.setSchedulingBounds(bounds);
        // objSpin.addChild(rotator);
        //su.addBranchGraph(objRoot);
        //objSpin.addChild(bg);
        //objRoot.addChild(objSpin);
        //objRoot.compile();
        
        bg.compile();
        return bg;
        //return objRoot;
    }
    
    public static void main(String[] args) {
        Java3DDemo frame = new Java3DDemo();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(512,512);
        frame.setVisible(true);
    }
}
