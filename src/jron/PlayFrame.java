package jron;
import java.awt.BorderLayout;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Enumeration;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.HiResCoord;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PositionPathInterpolator;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickTool;
public class PlayFrame extends java.awt.Frame {
	public int rotationX = 0;
	public int rotationY = 0;
	public static final float LOW_Z_WALL = 0.0f;
	public static final float HIGH_Z_WALL = -60.0f;
	public static final float LOW_X_WALL = -12.0f;
	public static final float HIGH_X_WALL = 12.0f;
	public static final float LOW_Y_WALL = -8.0f;
	public static final float HIGH_Y_WALL = 8.0f;
	public BranchGroup wallsBG;
	public PickCanvas pc;
	public Point3f[] point;
	public PositionPathInterpolator path;
	public TransformGroup tgMe;
	private TransformGroup pointer = new TransformGroup();
	private CollisionBehavior collisionBehavior;
	private TransformGroup finalDisc[];
	private TransformGroup collisionWallsGroup;
	private static final double DISC_SPEED_CONSTANT = 0.8;
	public static Shape3D[] COLLISION_WALLS;
	public static final int WALL_FRONT = 0;
	public static final int WALL_BACK = 1;
	public static final int WALL_RIGHT = 2;
	public static final int WALL_LEFT = 3;
	private static ActorKeyBehavior actorKeyBehavior;
	public BranchGroup getWalls() {
		return wallsBG;
	}
	public TransformGroup getPointer() {
		return pointer;
	}
	public Bounds getPlayBounds() {
		return new BoundingSphere(new Point3d(0.0, 0.0, -30.0), 40.0);
		// return new BoundingBox( new Point3d(LOW_X_WALL, LOW_Y_WALL,
		// HIGH_Z_WALL), new Point3d(HIGH_X_WALL, HIGH_Y_WALL, LOW_Z_WALL));
	}
	public void recurseBounds(Group g) {
		Enumeration e = g.getAllChildren();
		g.setBounds(getPlayBounds());
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			System.out.println(n.getBounds());
			n.setBounds(getPlayBounds());
			if (n instanceof Group) {
				recurseBounds((Group) n);
			}
		}
	}
	public PlayFrame() {
		setLayout(new BorderLayout());
		//create the Universe
		VirtualUniverse m_Universe = new VirtualUniverse();
		//create the position for the Locale
		int[] xPos = {0, 0, 0, 0, 0, 0, 0, 0};
		int[] yPos = {0, 0, 0, 0, 0, 0, 0, 0};
		int[] zPos = {0, 0, 0, 0, 0, 0, 0, 0};
		HiResCoord hiResCoord = new HiResCoord(xPos, yPos, zPos);
		//create the Locale and attach to the VirtualUniverse
		javax.media.j3d.Locale locale = new javax.media.j3d.Locale(m_Universe,
				hiResCoord);
		//create the BranchGroup containing the Geometry for the scene
		BranchGroup sceneBranchGroup = createSceneGraph();
		sceneBranchGroup.compile();
		//add the scene BranchGroup to the Locale
		locale.addBranchGraph(sceneBranchGroup);
		//create the ViewPlatform BranchGroup
		BranchGroup vpBranchGroup = new BranchGroup();
		//create a TransformGroup to scale the ViewPlatform
		//(and hence View)
		TransformGroup tg = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.rotX((Math.PI) * -0.14);
		t3d.setTranslation(new Vector3d(0.0, 13.0, 16.0));
		tg.setTransform(t3d);
		//tg.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE);
		//tg.setCapability( TransformGroup.ALLOW_TRANSFORM_READ);
		//create the ViewPlatform
		ViewPlatform vp = new ViewPlatform();
		vp.setViewAttachPolicy(View.RELATIVE_TO_FIELD_OF_VIEW);
		//attach the ViewPlatform to the TransformGroup
		tg.addChild(vp);
		//attach the TransformGroup to the BranchGroup
		vpBranchGroup.addChild(tg);
		//finally, add the ViewPlatform BranchGroup to the Locale
		locale.addBranchGraph(vpBranchGroup);
		//Move the camera BACK a little. Note that Transformation
		//matrices above the ViewPlatform are inverted by the View
		//renderer prior to rendering. By moving the camera back 20
		//meters, you can see geometry objects that are positioned at 0,0,0.
		//create the View object
		View view = new View();
		//create the PhysicalBody and PhysicalEnvironment for the View
		//and attach to the View
		PhysicalBody pb = new PhysicalBody();
		PhysicalEnvironment pe = new PhysicalEnvironment();
		view.setPhysicalEnvironment(pe);
		view.setPhysicalBody(pb);
		//attach the View to the ViewPlatform
		view.attachViewPlatform(vp);
		view.setBackClipDistance(70);
		GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
		gc3D.setSceneAntialiasing(GraphicsConfigTemplate.PREFERRED);
		GraphicsDevice gd[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getScreenDevices();
		Canvas3D c3d = new Canvas3D(gd[0].getBestConfiguration(gc3D));
		//set the size of the Canvas3D
		c3d.setSize(512, 512);
		//add the Canvas3D to the View so that it is rendered into
		view.addCanvas3D(c3d);
		//add the Canvas3D component to a parent AWT or Swing Panel
		add(c3d);
		add(c3d, BorderLayout.CENTER);
		pc = new PickCanvas(c3d, wallsBG);
		pc.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
		pc.setTolerance((float) 0.0);
	}
	public Point3f getOldActorLocation() {
		Transform3D transStart = new Transform3D();
		tgMe.getTransform(transStart);
		Vector3f translation = new Vector3f();
		transStart.get(translation);
		return new Point3f(translation);
	}
	public Vector3d getActorLocation() {
		Transform3D transStart = new Transform3D();
		tgMe.getTransform(transStart);
		Vector3d translation = new Vector3d();
		transStart.get(translation);
		return translation;
	}
	public BranchGroup createSceneGraph() {
		BranchGroup bg = new BranchGroup();
		//bg.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE);
		//bg.setBoundsAutoCompute(true);
		TransformGroup tgEnemy = createActor("Enemy", 0.0f, -5.0f, -55.0f);
		Transform3D posTranEnemy = new Transform3D();
		tgEnemy.getTransform(posTranEnemy);
		Vector3d posVecEnemy = new Vector3d();
		posTranEnemy.get(posVecEnemy);
		//ActorKeyBehavior key1 = new ActorKeyBehavior(tgEnemy);
		//key1.setSchedulingBounds(getPlayBounds());
		//key1.setEnable(true);
		//bg.addChild(key1);
		tgMe = createActor("Me", 0.0f, -5.0f, -5.0f);
		Transform3D posTran = new Transform3D();
		tgMe.getTransform(posTran);
		Vector3d posVec = new Vector3d();
		posTran.get(posVec);
		//bg.addChild(tgEnemy);//createActor(0.0f, -5.0f, -55.0f));
		//bg.addChild(tgMe);
		pointer.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		pointer.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		Node n = ObjectLoaderFlyWheel.getInstance().getLoadedObject(
				"/pointer.dxf", 0);
		pointer.addChild(n);
		Transform3D rot = new Transform3D();
		rot.rotY(-Math.PI / 2.0);
		rot.setScale(.3);
		pointer.setTransform(rot);
		bg.addChild(pointer);
		//PointerBehavior pb = new PointerBehavior(pointer, this);
		//pb.setSchedulingBounds(getPlayBounds());
		//pb.setEnable(true);
		//bg.addChild(pb);
		
		actorKeyBehavior = new ActorKeyBehavior(tgMe, pointer, this);
		actorKeyBehavior.setSchedulingBounds(getPlayBounds());
		actorKeyBehavior.setEnable(true);
		bg.addChild(actorKeyBehavior);
		
		
		TransformGroup[] tgArray = new TransformGroup[6];
		Vector3d[] vecArray = new Vector3d[6];
		vecArray[0] = new Vector3d(-7.0f, -6.0f, -5.0f);
		vecArray[1] = new Vector3d(0.0f, -6.0f, -5.0f);
		vecArray[2] = new Vector3d(7.0f, -6.0f, -5.0f);
		vecArray[3] = new Vector3d(-7.0f, -6.0f, -55.0f);
		vecArray[4] = new Vector3d(0.0f, -6.0f, -55.0f);
		vecArray[5] = new Vector3d(7.0f, -6.0f, -55.0f);
		for (int i = 0; i < tgArray.length; i++) {
			tgArray[i] = createObjectAt("/disk.dxf", 0, vecArray[i], true);
			//bg.addChild(tgArray[i]);
			//recurseForShapes(tgArray[i]);
		}
		BranchGroup platforms = new BranchGroup();
		Vector3d subt = new Vector3d(0, -.5, 0);
		for (int i = 0; i < tgArray.length; i++) {
			Cylinder cylinder = new Cylinder(3.0f, .5f);
			PickTool.setCapabilities(cylinder.getShape(Cylinder.TOP), PickTool.INTERSECT_FULL);
			PickTool.setCapabilities(cylinder.getShape(Cylinder.BODY), PickTool.INTERSECT_FULL);
			PickTool.setCapabilities(cylinder.getShape(Cylinder.BOTTOM), PickTool.INTERSECT_FULL);
			TransformGroup tgCyl = new TransformGroup();
			tgCyl.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			tgCyl.addChild(cylinder);
			Transform3D trans = new Transform3D();
			vecArray[i].add(subt);
			trans.setTranslation(vecArray[i]);
			tgCyl.setTransform(trans);
			platforms.addChild(tgCyl);
		}
		bg.addChild(platforms);
		GravityBehavior grav = new GravityBehavior(new TransformGroup[]{tgMe,
				tgEnemy}, platforms);
		grav.setSchedulingBounds(getPlayBounds());
		grav.setEnable(true);
		bg.addChild(grav);
		createDiscProjectile();
		for (int i = 0; i < finalDisc.length; i++) {
			bg.addChild(finalDisc[i]);
		}
		bg.addChild(createWalls());
		BranchGroup collisionGroup = new BranchGroup();
		collisionGroup.addChild(collisionWallsGroup);
		//bg.addChild(tgMe);
		collisionGroup.addChild(tgEnemy);
		collisionGroup.addChild(tgMe);
		Vector3d[] posVecArray = new Vector3d[]{(Vector3d) posVec.clone(),
				(Vector3d) posVec.clone(), (Vector3d) posVec.clone(),
				(Vector3d) posVecEnemy.clone(), (Vector3d) posVecEnemy.clone(),
				(Vector3d) posVecEnemy.clone(),};
		Vector3d[] incVecArray = new Vector3d[]{new Vector3d(), new Vector3d(),
				new Vector3d(), new Vector3d(), new Vector3d(), new Vector3d(),};
		collisionBehavior = new CollisionBehavior(collisionGroup, finalDisc,
				posVecArray, incVecArray);
		collisionBehavior.setSchedulingBounds(getPlayBounds());
		collisionBehavior.setEnable(false);
		collisionGroup.addChild(collisionBehavior);
		bg.addChild(collisionGroup);
		setupLighting(bg);
		return bg;
	}
	/**
	 * @param group
	 */
	private void recurseForShapes(Node node) {
		System.out.println(node);
		if (node instanceof Shape3D /* || node instanceof Locale */) {
//			System.out.println("setting cap");
			PickTool.setCapabilities(node, PickTool.INTERSECT_FULL);
		} else if (node instanceof Group) {
			Enumeration e = ((Group) node).getAllChildren();
			while (e.hasMoreElements()) {
				recurseForShapes((Node) e.nextElement());
			}
		} else if (node instanceof Link) {
			recurseForShapes(((Link) node).getSharedGroup());
		}
	}
	/**
	 *  
	 */
	private void createDiscProjectile() {
		finalDisc = new TransformGroup[6];
		for (int i = 0; i < finalDisc.length; i++) {
			Transform3D rotateLevelX = new Transform3D();
			rotateLevelX.rotX(-Math.PI / 2.0);
			Transform3D rotateLevelY = new Transform3D();
			rotateLevelY.rotY(-Math.PI / 2.0);
			TransformGroup disc1 = createObjectAt("/disk.dxf", 0, 0.0f, 0.0f,
					0.0f, true);
			Enumeration e = disc1.getAllChildren();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof Shape3D) {
					PickTool.setCapabilities((Node) obj,
							PickTool.INTERSECT_FULL);
				}
			}
			BranchGroup fpbg1 = new BranchGroup();
			fpbg1.setBounds(getPlayBounds());
			fpbg1.addChild(disc1);
			Transform3D yAxis = new Transform3D();
			yAxis.mul(rotateLevelX);
			Alpha rotationAlpha = new Alpha(-1, 4000);
			RotationInterpolator mainRot = new RotationInterpolator(
					rotationAlpha, disc1, yAxis, 0.0f, (float) Math.PI * 2.0f);
			mainRot.setSchedulingBounds(getPlayBounds());
			fpbg1.addChild(mainRot);
			Transform3D trans = new Transform3D();
			trans.mul(rotateLevelX);
			TransformGroup discTG = new TransformGroup();
			discTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			discTG.addChild(fpbg1);
			trans.setTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
			Transform3D scale = new Transform3D();
			scale.setScale(0.15);
			trans.mul(scale);
			discTG.setTransform(trans);
			finalDisc[i] = new TransformGroup();
			finalDisc[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			finalDisc[i].addChild(discTG);
			Alpha alpha = new Alpha(1, 10000);
			yAxis.rotX(2 * Math.PI);
		}
	}
	public void fireDisc(int discNumber) {
		Vector3d startVec = getActorLocation();
		collisionBehavior.getPositionVector()[discNumber] = startVec;
		Vector3d base = (Vector3d) startVec.clone();
		Transform3D t3dbase = new Transform3D();
		t3dbase.set(base);
		Transform3D t3d = new Transform3D();
		pointer.getTransform(t3d);
		Vector3d endVec = new Vector3d();
		t3d.sub(t3dbase);
		t3d.get(endVec);
		t3d.mul((1.0 / endVec.length()) * DISC_SPEED_CONSTANT);
		t3d.get(endVec);
		System.out.println(endVec.length());
		collisionBehavior.getIncrementVector()[discNumber] = endVec;
		collisionBehavior.setEnable(true);
	}
	public BranchGroup createWalls() {
		Appearance app = new Appearance();
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		ColoringAttributes blue = new ColoringAttributes(new Color3f(0.0f,
				0.0f, 1.0f), ColoringAttributes.SHADE_GOURAUD);
		Material material = new Material(new Color3f(0.0f, 0.0f, 0.0f),
				new Color3f(0.0f, 0.0f, 0.1f), new Color3f(0.1f, 0.1f, 0.1f),
				new Color3f(0.0f, 0.0f, 0.1f), 17.0f);
		material.setCapability(Material.ALLOW_COMPONENT_READ);
		material.setLightingEnable(true);
		app.setMaterial(material);
		//Box box = new Box( HIGH_X_WALL-LOW_X_WALL,
		//			HIGH_Y_WALL-LOW_Y_WALL, HIGH_Z_WALL-LOW_Z_WALL,
		//			Primitive.GENERATE_NORMALS, app);
		Box box = new Box(12.0f, 12.0f, -30.0f, Primitive.GENERATE_NORMALS, app);
		COLLISION_WALLS = new Shape3D[4];
		this.collisionWallsGroup = new TransformGroup();
		COLLISION_WALLS[WALL_FRONT] = (Shape3D) box.getShape(Box.FRONT)
				.cloneTree();
		COLLISION_WALLS[WALL_BACK] = (Shape3D) box.getShape(Box.BACK)
				.cloneTree();
		COLLISION_WALLS[WALL_LEFT] = (Shape3D) box.getShape(Box.LEFT)
				.cloneTree();
		COLLISION_WALLS[WALL_RIGHT] = (Shape3D) box.getShape(Box.RIGHT)
				.cloneTree();
		for (int i = 0; i < COLLISION_WALLS.length; i++) {
			this.collisionWallsGroup.addChild(COLLISION_WALLS[i]);
			PickTool.setCapabilities(COLLISION_WALLS[i],
					PickTool.INTERSECT_FULL);
		}
		Transform3D t3dColl = new Transform3D();
		t3dColl.set(new Vector3d(0.0, -5.0, -30.0));
		this.collisionWallsGroup.setTransform(t3dColl);
		TransformGroup tg = new TransformGroup();
		tg.addChild(box);
		Transform3D t3d = new Transform3D();
		t3d.set(new Vector3d(0.0, -5.0, -30.0));
		tg.setTransform(t3d);
		BranchGroup bg = new BranchGroup();
		bg.addChild(tg);
		return bg;
		//bg.addChild(
		//bg.
	}
	public void setupLighting(BranchGroup bg) {
		Color3f light1Color = new Color3f(0.3f, 0.1f, 0.9f);
		Vector3f light1Direction = new Vector3f(0.0f, -1.0f, -1.0f);
		Color3f light2Color = new Color3f(0.3f, 0.1f, 0.9f);
		Vector3f light2Direction = new Vector3f(-1.0f, 0.0f, -1.0f);
		DirectionalLight light1 = new DirectionalLight(light1Color,
				light1Direction);
		light1.setInfluencingBounds(getPlayBounds());
		bg.addChild(light1);
		DirectionalLight light2 = new DirectionalLight(light2Color,
				light2Direction);
		light2.setInfluencingBounds(getPlayBounds());
		bg.addChild(light2);
	}
	public TransformGroup createActor(String name, float x, float y, float z) {
		Transform3D trans = new Transform3D();
		//trans.mul(rotateLevelX);
		TransformGroup finalTG = new TransformGroup();
		finalTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		finalTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		Appearance app = new Appearance();
		
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		ColoringAttributes blue = new ColoringAttributes(new Color3f(0.0f,
				0.0f, 1.0f), ColoringAttributes.SHADE_GOURAUD);
		Material material = new Material(new Color3f(0.0f, 0.0f, 0.0f),
				new Color3f(0.0f, 0.0f, 0.2f), new Color3f(0.1f, 0.1f, 0.1f),
				new Color3f(0.0f, 0.0f, 0.2f), 17.0f);
		material.setCapability(Material.ALLOW_COMPONENT_READ);
		material.setLightingEnable(true);
		app.setMaterial(material);
//		Box shape = new Box(0.5f, 1.0f, 0.5f, Primitive.GENERATE_NORMALS, app);
//		Shape3D[] shapes = new Shape3D[]{shape.getShape(Box.FRONT),
//				shape.getShape(Box.BACK), shape.getShape(Box.LEFT),
//				shape.getShape(Box.RIGHT), shape.getShape(Box.TOP),
//				shape.getShape(Box.BOTTOM),};
//		for (int i = 0; i < shapes.length; i++) {
//			PickTool.setCapabilities(shapes[i], PickTool.INTERSECT_FULL);
//			shapes[i].setUserData(name);
//		}
		Node n = ObjectLoaderFlyWheel.getInstance().getLoadedObject(
				"/player.dxf", 0);
		Link link = (Link) n;
		SharedGroup sg = link.getSharedGroup();
		BranchGroup bg = (BranchGroup) sg.getChild(0);
		Shape3D person = (Shape3D) bg.getChild(0);
		bg.removeAllChildren();
		person.setAppearance(app);
		person.setPickable(true);
		PickTool.setCapabilities(person, PickTool.INTERSECT_FULL);
	
		Transform3D scaler = new Transform3D();
		scaler.setScale(.25);
		scaler.setTranslation(new Vector3f(0F,2F,0F));
		TransformGroup scale = new TransformGroup(scaler);
		scale.addChild(person);
		
		
		//finalTG.addChild(shape);
		finalTG.addChild(scale);
		trans.setTranslation(new Vector3f(x, y, z));
		finalTG.setTransform(trans);
		return finalTG;
	}
	
	public TransformGroup createObjectAt(String location, int childNumber, Vector3d translate, boolean spinning) {
		return createObjectAt(location, childNumber, translate.x, translate.y, translate.z, spinning);
	}
	public TransformGroup createObjectAt(String location, int childNumber,
			double x, double y, double z, boolean spinning) {
		Node obj = ObjectLoaderFlyWheel.getInstance().getLoadedObject(location,
				childNumber);
		Transform3D rotateLevelX = new Transform3D();
		rotateLevelX.rotX(-Math.PI / 2.0);
		Transform3D rotateLevelY = new Transform3D();
		rotateLevelY.rotY(-Math.PI / 2.0);
		TransformGroup objTG = new TransformGroup();
		objTG.addChild(obj.cloneNode(true));
		objTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		BranchGroup objBG = new BranchGroup();
		objBG.addChild(objTG);
		if (spinning) {
			Transform3D yAxis = new Transform3D();
			yAxis.mul(rotateLevelX);
			Alpha rotationAlpha = new Alpha(-1, 4000);
			RotationInterpolator rotate = new RotationInterpolator(
					rotationAlpha, objTG, yAxis, 0.0f, (float) Math.PI * 2.0f);
			//BoundingSphere bounds = new BoundingSphere(new Point3d(x,y,z),
			// 2.0);
			rotate.setSchedulingBounds(getPlayBounds());
			objBG.addChild(rotate);
		}
		Transform3D trans = new Transform3D();
		trans.mul(rotateLevelX);
		TransformGroup finalTG = new TransformGroup();
		finalTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		finalTG.addChild(objBG);
		trans.setTranslation(new Vector3d(x, y, z));
		finalTG.setTransform(trans);
		return finalTG;
	}
	/**
	 * @return Returns the actorKeyBehavior.
	 */
	public static ActorKeyBehavior getActorKeyBehavior() {
		return actorKeyBehavior;
	}
}