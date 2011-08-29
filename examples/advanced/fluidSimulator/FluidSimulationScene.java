/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package advanced.fluidSimulator;

import java.awt.event.KeyEvent;   
import java.nio.FloatBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GL;

import msafluid.MSAFluidSolver2D;


import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.shapes.MTLine;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.camera.MTCamera;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import advanced.physics.physicsShapes.IPhysicsComponent;
import advanced.physics.physicsShapes.PhysicsCircle;
import advanced.physics.physicsShapes.PhysicsRectangle;
import advanced.physics.util.PhysicsHelper;
import advanced.physics.util.UpdatePhysicsAction;

import com.sun.opengl.util.BufferUtil;

import java.util.Random;

/**
 * The Class FluidSimulationScene.
 * 
 * The original fluid simulation code was taken from
 * memo akten (www.memo.tv)
 * 
 */
public class FluidSimulationScene extends AbstractScene{

	private final float FLUID_WIDTH = 120;
	private float invWidth, invHeight;    // inverse of screen dimensions
	private float aspectRatio, aspectRatio2;
	private MSAFluidSolver2D fluidSolver;
	private PImage imgFluid;
	private boolean drawFluid = true;
	
	private int paddleRadius = 60;
	
	private int winningScore = 5;

	public DragEvent de;

	private ParticleSystem particleSystem;
	/////////

	private float timeStep = 1.0f / 60.0f;
	private int constraintIterations = 10;

	/** THE CANVAS SCALE **/
	private float scale = 20;
	private World world;

	private MTComponent physicsContainer;
	private MTTextArea t1;
	private MTTextArea t2;

	private int scorePlayer1;
	private int scorePlayer2;
	private HockeyBall ball;

	private Paddle redCircle;
	private Paddle blueCircle;


	private boolean enableSound = true;

	private MTApplication app;
	
	Timer timer;

	//private String imagesPath =  "advanced" + MTApplication.separator +  "physics"  + MTApplication.separator + "data" +  MTApplication.separator  + "images" +  MTApplication.separator;
	private String imagesPath = "";
	private MTTextArea winner;
	public FluidSimulationScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;

		this.scorePlayer1 = 0;
		this.scorePlayer2 = 0;

		if (!MT4jSettings.getInstance().isOpenGlMode()){
			System.err.println("Scene only usable when using the OpenGL renderer! - See settings.txt");
			return;
		}

		float worldOffset = 10; //Make Physics world slightly bigger than screen borders
		//Physics world dimensions
		AABB worldAABB = new AABB(new Vec2(-worldOffset, -worldOffset), new Vec2((app.width)/scale + worldOffset, (app.height)/scale + worldOffset));
		Vec2 gravity = new Vec2(0, 0);
		boolean sleep = true;
		//Create the physics world
		this.world = new World(worldAABB, gravity, sleep);
		this.registerGlobalInputProcessor(new CursorTracer(app, this));


		//Update the positions of the components according the the physics simulation each frame
		this.registerPreDrawAction(new UpdatePhysicsAction(world, timeStep, constraintIterations, scale));

		physicsContainer = new MTComponent(app);
		//Scale the physics container. Physics calculations work best when the dimensions are small (about 0.1 - 10 units)
		//So we make the display of the container bigger and add in turn make our physics object smaller
		physicsContainer.scale(scale, scale, 1, Vector3D.ZERO_VECTOR);

		//Create borders around the screen
		this.createScreenBorders(physicsContainer);


		///Create gamefield marks
		MTLine line = new MTLine(mtApplication, mtApplication.width/2f/scale, 0, mtApplication.width/2f/scale, mtApplication.height/scale);
		line.setPickable(false);
		line.setStrokeColor(new MTColor(150,150,150));
		line.setStrokeWeight(0.5f);


		MTEllipse centerCircle = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f/scale, mtApplication.height/2f/scale), 80/scale, 80/scale);
		centerCircle.setPickable(false);
		centerCircle.setNoFill(true);
		centerCircle.setStrokeColor(new MTColor(150,150,150));
		centerCircle.setStrokeWeight(0.5f);


		MTEllipse centerCircleInner = new MTEllipse(mtApplication, new Vector3D(mtApplication.width/2f/scale, mtApplication.height/2f/scale), 10/scale, 10/scale);
		centerCircleInner.setPickable(false);
		centerCircleInner.setFillColor(new MTColor(160,160,160));
		centerCircleInner.setStrokeColor(new MTColor(150,150,150));
		centerCircleInner.setStrokeWeight(0.5f);


		//Create the paddles
		PImage paddleTex = mtApplication.loadImage(imagesPath + "paddle.png");
		redCircle = new Paddle(app, new Vector3D(mtApplication.width - 60, mtApplication.height/2f), paddleRadius, world, 1.0f, 0.3f, 0.4f, scale);
		redCircle.setTexture(paddleTex);
		redCircle.setFillColor(new MTColor(0, 0, 0, 255));
		redCircle.setNoFill(true);
		redCircle.setNoStroke(true);
		redCircle.setName("red");
		redCircle.setPickable(false);

		blueCircle = new Paddle(app, new Vector3D(80, mtApplication.height/2f), paddleRadius, world, 1.0f, 0.3f, 0.4f, scale);
		blueCircle.setTexture(paddleTex);
		blueCircle.setFillColor(new MTColor(50,50,255));
		blueCircle.setNoFill(true);
		blueCircle.setNoStroke(true);
		blueCircle.setName("blue");
		blueCircle.setPickable(false);

		//Create the ball
		ball = new HockeyBall(app, new Vector3D(mtApplication.width/2f, mtApplication.height/2f), 38, world, 0.5f, 0.005f, 0.70f, scale);
		PImage ballTex = mtApplication.loadImage(imagesPath + "aa.png");
		ball.setTexture(ballTex);
		ball.setFillColor(new MTColor(255,255,255,255));
		ball.setNoStroke(true);
		ball.setName("ball");
		ball.getBody().applyImpulse(new Vec2(ToolsMath.getRandom(-8f, 8),ToolsMath.getRandom(-8, 8)), ball.getBody().getWorldCenter());


		//Create the GOALS
		HockeyGoal goal1 = new HockeyGoal(new Vector3D(0, mtApplication.height/2f), 50, mtApplication.height/4f, mtApplication, world, 0.0f, 0.1f, 0.0f, scale);
		goal1.setName("goal1");
		goal1.setFillColor(new MTColor(0,0,255));
		goal1.setStrokeColor(new MTColor(0,0,255));

		HockeyGoal goal2 = new HockeyGoal(new Vector3D(mtApplication.width, mtApplication.height/2f), 50, mtApplication.height/4f, mtApplication, world, 0.0f, 0.1f, 0.0f, scale);
		goal2.setName("goal2");
		goal2.setFillColor(new MTColor(255,0,0));
		goal2.setStrokeColor(new MTColor(255,0,0));


		MTRectangle leftSide = new MTRectangle(
				PhysicsHelper.scaleDown(0, scale), PhysicsHelper.scaleDown(0, scale), 
				PhysicsHelper.scaleDown(app.width/2f, scale), PhysicsHelper.scaleDown(app.height, scale)
				, app);
		leftSide.setName("left side");
		leftSide.setNoFill(true); //Make it invisible -> only used for dragging
		leftSide.setNoStroke(true);
		leftSide.unregisterAllInputProcessors();
		leftSide.removeAllGestureEventListeners(DragProcessor.class);
		leftSide.registerInputProcessor(new DragProcessor(app));
		leftSide.addGestureListener(DragProcessor.class, new GameFieldHalfDragListener(blueCircle));

		MTRectangle rightSide = new MTRectangle(
				PhysicsHelper.scaleDown(app.width/2f, scale), PhysicsHelper.scaleDown(0, scale), 
				PhysicsHelper.scaleDown(app.width, scale), PhysicsHelper.scaleDown(app.height, scale)
				, app);
		rightSide.setName("right Side");
		rightSide.setNoFill(true); //Make it invisible -> only used for dragging
		rightSide.setNoStroke(true);
		rightSide.unregisterAllInputProcessors();
		rightSide.removeAllGestureEventListeners(DragProcessor.class);
		rightSide.registerInputProcessor(new DragProcessor(app));
		rightSide.addGestureListener(DragProcessor.class, new GameFieldHalfDragListener(redCircle));

	
		

		//Display Score UI
		MTComponent uiLayer = new MTComponent(mtApplication, new MTCamera(mtApplication));
		uiLayer.setDepthBufferDisabled(true);
		IFont font = FontManager.getInstance().createFont(mtApplication, "arial", 50, new MTColor(255,255,255), new MTColor(0,0,0));

		t1 = new MTTextArea(mtApplication, font);
		t1.setPickable(false);
		t1.setNoFill(true);
		t1.setNoStroke(true);
		t1.setPositionGlobal(new Vector3D(5,30,0));
		uiLayer.addChild(t1);

		t2 = new MTTextArea(mtApplication, font);
		t2.setPickable(false);
		t2.setNoFill(true);
		t2.setNoStroke(true);
		t2.setPositionGlobal(new Vector3D(mtApplication.width - 65 , 30,0));
		uiLayer.addChild(t2);
		this.updateScores();
		
		winner = new MTTextArea(mtApplication, font);
		winner.setPickable(false);
		winner.setNoFill(true);
		winner.setNoStroke(true);
		winner.setPositionGlobal(new Vector3D(mtApplication.width/2,mtApplication.height/2,0));
		uiLayer.addChild(winner);


		invWidth = 1.0f/mtApplication.width;
		invHeight = 1.0f/mtApplication.height;
		aspectRatio = mtApplication.width * invHeight;
		aspectRatio2 = aspectRatio * aspectRatio;

		// Create fluid and set options
		fluidSolver = new MSAFluidSolver2D((int)(FLUID_WIDTH), (int)(FLUID_WIDTH * mtApplication.height/mtApplication.width));
		//        fluidSolver.enableRGB(true).setFadeSpeed(0.003f).setDeltaT(0.5f).setVisc(0.00005f);
		fluidSolver.enableRGB(true).setFadeSpeed(0.003f).setDeltaT(0.8f).setVisc(0.00004f);

		// Create image to hold fluid picture
		imgFluid = mtApplication.createImage(fluidSolver.getWidth(), fluidSolver.getHeight(), PApplet.RGB);

		// Create particle system
		particleSystem = new ParticleSystem(mtApplication, fluidSolver);


		//FIXME make componentInputProcessor?



		this.getCanvas().addChild(new FluidImage(mtApplication));
		this.getCanvas().addChild(physicsContainer);
		physicsContainer.addChild(line);
		physicsContainer.addChild(centerCircle);
		physicsContainer.addChild(centerCircleInner);
		physicsContainer.addChild(redCircle);
		physicsContainer.addChild(blueCircle);
		physicsContainer.addChild(ball);
		physicsContainer.addChild(goal1);
		physicsContainer.addChild(goal2);
		physicsContainer.addChild(0, leftSide);
		physicsContainer.addChild(0, rightSide);
		this.getCanvas().addChild(uiLayer);

		//Set up check for collisions between objects
		this.addWorldContactListener(world);


		this.getCanvas().setDepthBufferDisabled(true);
	}


	/**
	 * The Class FluidImage.
	 */
	private class FluidImage extends MTComponent{
		public FluidImage(PApplet applet) {
			super(applet);
		}
		//@Override
		public void drawComponent(PGraphics g) {
			super.drawComponent(g);
			drawFluidImage();

			g.noSmooth();
			g.fill(255,255,255,255);
			g.tint(255,255,255,255);

			//FIXME TEST
			PGraphicsOpenGL pgl = (PGraphicsOpenGL)g; 
			GL gl = pgl.gl;
			gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL.GL_COLOR_ARRAY);
			gl.glDisable(GL.GL_LINE_SMOOTH);
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
	
	private HockeyBall getBall(){
		return ball;
	}

	private class GameFieldHalfDragListener implements IGestureEventListener{
		private MTComponent comp;

		public GameFieldHalfDragListener(MTComponent dragComp){
			this.comp = dragComp;
			if (comp.getUserData("box2d") == null){
				throw new RuntimeException("GameFieldHalfDragListener has to be given a physics object!");
			}
		}
		public boolean processGestureEvent(MTGestureEvent ge) {
			de = (DragEvent)ge;
			try{
				Body body = (Body)comp.getUserData("box2d");
				MouseJoint mouseJoint;
				Vector3D to = new Vector3D(de.getTo());
				//Un-scale position from mt4j to box2d
				PhysicsHelper.scaleDown(to, scale);

				InputCursor m = de.getDragCursor();
				AbstractCursorInputEvt prev = m.getPreviousEvent();
				if (prev == null)
					prev = m.getCurrentEvent();

				Vector3D pos = new Vector3D(m.getCurrentEvtPosX(), m.getCurrentEvtPosY(), 0);
				Vector3D prevPos = new Vector3D(prev.getPosX(), prev.getPosY(), 0);
				

				//System.out.println("Pos: " + pos);
				float mouseNormX = pos.x * invWidth;
				float mouseNormY = pos.y * invHeight;
				//System.out.println("MouseNormPosX: " + mouseNormX + "," + mouseNormY);
				float mouseVelX = (pos.x - prevPos.x) * invWidth;
				float mouseVelY = (pos.y - prevPos.y) * invHeight;

				addForce(mouseNormX, mouseNormY, mouseVelX, mouseVelY);


				switch (de.getId()) 

				{
				case DragEvent.GESTURE_DETECTED:
					comp.sendToFront();
					body.wakeUp();
					body.setXForm(new Vec2(to.x,  to.y), body.getAngle());
					mouseJoint = PhysicsHelper.createDragJoint(world, body, to.x, to.y);
					comp.setUserData(comp.getID(), mouseJoint);
					break;
				case DragEvent.GESTURE_UPDATED:
					mouseJoint = (MouseJoint) comp.getUserData(comp.getID());
					if (mouseJoint != null){
						boolean onCorrectGameSide = ((MTComponent)de.getTargetComponent()).containsPointGlobal(de.getTo());
						//System.out.println(((MTComponent)de.getTargetComponent()).getName()  + " Contains  " + to + " -> " + contains);
						if (onCorrectGameSide){
							mouseJoint.setTarget(new Vec2(to.x, to.y));	
						}
					}
					break;
				case DragEvent.GESTURE_ENDED:
					mouseJoint = (MouseJoint) comp.getUserData(comp.getID());
					if (mouseJoint != null){
						comp.setUserData(comp.getID(), null);
						//Only destroy the joint if it isnt already (go through joint list and check)
						for (Joint joint = world.getJointList(); joint != null; joint = joint.getNext()) {
							JointType type = joint.getType();
							switch (type) {
							case MOUSE_JOINT:
								MouseJoint mj = (MouseJoint)joint;
								if (body.equals(mj.getBody1()) || body.equals(mj.getBody2())){
									if (mj.equals(mouseJoint)) {
										world.destroyJoint(mj);
									}
								}
								break;
							default:
								break;
							}
						}
					}
					mouseJoint = null;
					break;
				default:
					break;
				}
			}catch (Exception e) {
				System.err.println(e.getMessage());
			}
			return false;
		}
	}


	private class Paddle extends PhysicsCircle{
		public Paddle(PApplet applet, Vector3D centerPoint, float radius,
				World world, float density, float friction, float restitution, float worldScale) {
			super(applet, centerPoint, radius, world, density, friction, restitution, worldScale);
		} 
		@Override
		protected void bodyDefB4CreationCallback(BodyDef def) {
			super.bodyDefB4CreationCallback(def);
			def.fixedRotation = true;
			def.linearDamping = 0.5f;
		}
	}

	private class HockeyBall extends PhysicsCircle{
		public HockeyBall(PApplet applet, Vector3D centerPoint, float radius,
				World world, float density, float friction, float restitution, float worldScale) {
			super(applet, centerPoint, radius, world, density, friction, restitution, worldScale);
		} 

		@Override
		protected void circleDefB4CreationCallback(CircleDef def) {
			super.circleDefB4CreationCallback(def);
			def.radius = def.radius -5/scale;
		}
		@Override
		protected void bodyDefB4CreationCallback(BodyDef def) {
			super.bodyDefB4CreationCallback(def);
			//			def.linearDamping = 0.15f;
			def.linearDamping = 0.25f;
			def.isBullet = true;
			def.angularDamping = 0.9f;

			//			def.fixedRotation = true;
		}
	}


	private class HockeyGoal extends PhysicsRectangle {
		public HockeyGoal(Vector3D centerPosition, float width, float height,
				PApplet applet, World world, float density, float friction,float restitution, float scale) {
			super(centerPosition, width, height, applet, world, density, friction,restitution, scale);
		}

		@Override
		protected void bodyDefB4CreationCallback(BodyDef def) {
			def.isBullet = true;
			super.bodyDefB4CreationCallback(def);
		}

		@Override
		protected void polyDefB4CreationCallback(PolygonDef def) {
			super.polyDefB4CreationCallback(def);
			def.isSensor = true; //THIS AS SENSOR!
		}
	}

	private void createScreenBorders(MTComponent parent){
		//Left border 
		float borderWidth = 50f;
		float borderHeight = app.height;
		Vector3D pos = new Vector3D(-(borderWidth/2f) , app.height/2f);
		PhysicsRectangle borderLeft = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderLeft.setName("borderLeft");
		parent.addChild(borderLeft);
		//Right border
		pos = new Vector3D(app.width + (borderWidth/2), app.height/2);
		PhysicsRectangle borderRight = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderRight.setName("borderRight");
		parent.addChild(borderRight);
		//Top border
		borderWidth = app.width;
		borderHeight = 50f;
		pos = new Vector3D(app.width/2, -(borderHeight/2));
		PhysicsRectangle borderTop = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderTop.setName("borderTop");
		parent.addChild(borderTop);
		//Bottom border
		pos = new Vector3D(app.width/2 , app.height + (borderHeight/2));
		PhysicsRectangle borderBottom = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderBottom.setName("borderBottom");
		parent.addChild(borderBottom);
	}





	private void addWorldContactListener(World world){
		world.setContactListener(new ContactListener() {
			public void result(ContactResult point) {
				//				System.out.println("Result contact");
			}
			//@Override
			public void remove(ContactPoint point) {
				//				System.out.println("remove contact");
			}
			//@Override
			public void persist(ContactPoint point) {
				//				System.out.println("persist contact");
			}
			//@Override
			public void add(ContactPoint point) {
				//				/*
				Shape shape1 = point.shape1;
				Shape shape2 = point.shape2;
				final Body body1 = shape1.getBody();
				final Body body2 = shape2.getBody();
				Object userData1 = body1.getUserData();
				Object userData2 = body2.getUserData();

				if (userData1 instanceof IPhysicsComponent  && userData2 instanceof IPhysicsComponent) { //Check for ball/star collision
					IPhysicsComponent physObj1 = (IPhysicsComponent) userData1;
					IPhysicsComponent physObj2 = (IPhysicsComponent) userData2;
					//					System.out.println("Collided: " + mt4jObj1 + " with " + mt4jObj2);
					if (physObj1 instanceof MTComponent && physObj2 instanceof MTComponent) {
						MTComponent comp1 = (MTComponent) physObj1;
						MTComponent comp2 = (MTComponent) physObj2;

						//Check if one of the components is the BALL
						MTComponent ball = isHit("ball", comp1, comp2);
						final MTComponent theBall = ball;

						//Check if one of the components is the GOAL
						MTComponent goal1 = isHit("goal1", comp1, comp2);
						MTComponent goal2 = isHit("goal2", comp1, comp2);

						//Check if a puck was involved
						MTComponent bluePuck = isHit("blue", comp1, comp2);
						MTComponent redPuck = isHit("red", comp1, comp2);

						//Check if a border was hit
						MTComponent border = null;
						if (comp1.getName() != null && comp1.getName().startsWith("border")){
							border = comp1;
						}else if (comp2.getName() != null && comp2.getName().startsWith("border")){
							border = comp2;
						}

						if (ball != null){
							//CHECK IF BALL HIT A PADDLE

							InputCursor m = de.getDragCursor();
							AbstractCursorInputEvt prev = m.getPreviousEvent();
							if (prev == null)
								prev = m.getCurrentEvent();

							Vector3D pos = new Vector3D(m.getCurrentEvtPosX(), m.getCurrentEvtPosY(), 0);
							Vector3D prevPos = new Vector3D(prev.getPosX(), prev.getPosY(), 0);

							float mouseNormX = pos.x * invWidth;
							float mouseNormY = pos.y * invHeight + 15;
							float mouseVelX = (pos.x - prevPos.x) * invWidth *250;
							float mouseVelY = (pos.y - prevPos.y) * invHeight *250;

							addForce(mouseNormX, mouseNormY, mouseVelX, mouseVelY);

							if (enableSound && (bluePuck != null || redPuck != null)){
								System.out.println("PUCK HIT BALL!");
								
								Random randNumGen = new Random();
								
								int lightSaberSound = randNumGen.nextInt(3);
								
								if(lightSaberSound == 0)
								{
									new AePlayWave("ltsaberhit01.wav").start();
								}
								else if(lightSaberSound == 1)
								{
									new AePlayWave("ltsaberhit05.wav").start();
								}
								else // (lightSaberSound == 2)
								{
									new AePlayWave("ltsaberhit06.wav").start();
								}
								
								
								
															
								
								if(bluePuck != null){
									getBall().setFillColor(new MTColor(35,96,250,255));
								}
								
								if(redPuck != null){
									getBall().setFillColor(new MTColor(255,50,50,255));
								}
							}


							//Check if BALL HIT A GOAL 
							if (goal1 != null || goal2 != null){
								if (goal1 != null){
									System.out.println("GOAL FOR PLAYER 2!");
									scorePlayer2++;
								}else if (goal2 != null){
									System.out.println("GOAL FOR PLAYER 1!");
									scorePlayer1++;
								}

								//Update scores
								updateScores();
								//Play goal sound
								//								triggerSound(goalHit);

								if (scorePlayer1 >= winningScore || scorePlayer2 >= winningScore){
									
									if(scorePlayer1>scorePlayer2){
										winner.setText("Player 1 Wins!!!");
										t1.setEnabled(false);
							        	t2.setEnabled(false);
							        	
										}
									else{
										winner.setText("Player 2 Wins!!!");
										t1.setEnabled(false);
							        	t2.setEnabled(false);}
									
									timer = new Timer();
									timer.schedule(new RemindTask(), 7000);
									timer.schedule(new GameTask(), 3000);
							        timer.schedule(new Begin3Task(), 6000);
							        timer.schedule(new Begin2Task(), 5000);
							        timer.schedule(new BeginTask(), 4000);
							        
							        
								}else{

									//Reset ball
									if (theBall.getUserData("resetted") == null){ //To make sure that we call destroy only once
										theBall.setUserData("resetted", true); 
										app.invokeLater(new Runnable() {
											public void run() {
									        	
									        	IPhysicsComponent a = (IPhysicsComponent)theBall;
									        	a.getBody().setXForm(new Vec2(getMTApplication().width/2f/scale, getMTApplication().height/2f/scale), a.getBody().getAngle());
												//											a.getBody().setLinearVelocity(new Vec2(0,0));
												a.getBody().setLinearVelocity(new Vec2(ToolsMath.getRandom(-8, 8),ToolsMath.getRandom(-8, 8)));
												a.getBody().setAngularVelocity(0);
												theBall.setUserData("resetted", null); 
									            

											}
										});
									}
								}

							}

							//If ball hit border Play sound
							if (enableSound && border != null){
								/*
								triggerSound(wallHit);
								 */
							}
							
							
						}
					}
				}else{ //if at lest one if the colliding bodies' userdata is not a physics shape

				}
				//				*/
			}
		});
	}

    class RemindTask extends TimerTask {
        public void run() {
        	winner.clear();
        	t1.setEnabled(true);
        	t2.setEnabled(true);
			reset();
            timer.cancel(); //Terminate the timer thread
        }
    }
    class GameTask extends TimerTask {
        public void run() {
        	winner.setText("New Game in");
            
        }

    }
    
    class BeginTask extends TimerTask {
        public void run() {
        	winner.setText("3");
            
        }
    }
    
    class Begin2Task extends TimerTask {
        public void run() {
        	winner.setText("2");
           
        }
     }
    
    class Begin3Task extends TimerTask {
        public void run() {
        	winner.setText("1");
            
        }
     }



	private MTComponent isHit(String componentName, MTComponent comp1, MTComponent comp2){
		MTComponent hitComp = null;
		if (comp1.getName() != null && comp1.getName().equalsIgnoreCase(componentName)){
			hitComp = comp1;
		}else if (comp2.getName() != null && comp2.getName().equalsIgnoreCase(componentName)){
			hitComp = comp2;
		}
		return hitComp;
	}

	private void updateScores(){
		if(t1.isEnabled() || t2.isEnabled()){
		t1.setText(Integer.toString(scorePlayer1));
		t2.setText(Integer.toString(scorePlayer2));
		}
	}

	private void reset(){
		if (ball.getUserData("resetted") == null){ //To make sure that we call destroy only once
			ball.setUserData("resetted", true); 
			app.invokeLater(new Runnable() {
				public void run() {
					IPhysicsComponent a = (IPhysicsComponent)ball;
					a.getBody().setXForm(new Vec2(getMTApplication().width/2f/scale, getMTApplication().height/2f/scale), a.getBody().getAngle());
					//					a.getBody().setLinearVelocity(new Vec2(0,0));
					a.getBody().setLinearVelocity(new Vec2(ToolsMath.getRandom(-8, 8),ToolsMath.getRandom(-8, 8)));
					a.getBody().setAngularVelocity(0);
					ball.setUserData("resetted", null); 
				}
			});
		}
		this.scorePlayer1 = 0;
		this.scorePlayer2 = 0;
		this.updateScores();
	}


	//@Override
	public void drawAndUpdate(PGraphics graphics, long timeDelta) {
		//		this.drawFluidImage();
		super.drawAndUpdate(graphics, timeDelta);

	}



	// add force and dye to fluid, and create particles
	private void addForce(float x, float y, float dx, float dy) {
		float speed = dx * dx  + dy * dy * aspectRatio2;    // balance the x and y components of speed with the screen aspect ratio

		if(speed > 0) {
			if(x < 0){ 
				x = 0; 
			}else if(x > 1){
				x = 1;
			}if(y < 0){ 
				y = 0; 
			}else if(y > 1){ 
				y = 1;
			}

			float colorMult = 5;
			float velocityMult = 30.0f;

			int index = fluidSolver.getIndexForNormalizedPosition(x, y);

			//	        PApplet.color drawColor;
			app.colorMode(PApplet.HSB, 360, 1, 1);
			float hue = ((x + y) * 180 + app.frameCount) % 360;
			int drawColor = app.color(hue, 1, 1);
			app.colorMode(PApplet.RGB, 1);  

			fluidSolver.rOld[index]  += app.red(drawColor) 	* colorMult;
			fluidSolver.gOld[index]  += app.green(drawColor) 	* colorMult;
			fluidSolver.bOld[index]  += app.blue(drawColor) 	* colorMult;

			//Particles
			particleSystem.addParticles(x * app.width, y * app.height, 10);

			fluidSolver.uOld[index] += dx * velocityMult;
			fluidSolver.vOld[index] += dy * velocityMult;


			//FIXME TEST
			app.colorMode(PApplet.RGB, 255);  
		}
	}


	private void drawFluidImage(){
		app.colorMode(PApplet.RGB, 1);  

		fluidSolver.update();
		if(drawFluid) {
			for(int i=0; i<fluidSolver.getNumCells(); i++) {
				int d = 2;
				imgFluid.pixels[i] = app.color(fluidSolver.r[i] * d, fluidSolver.g[i] * d, fluidSolver.b[i] * d);
			}  
			imgFluid.updatePixels();//  fastblur(imgFluid, 2);


			app.textureMode(app.NORMALIZED);
			app.beginShape(app.QUADS);
			app.texture(imgFluid);
			app.vertex(0, 0, 0, 0);
			app.vertex(app.width, 0, 1, 0);
			app.vertex(app.width, app.height, 1, 1);
			app.vertex(0, app.height, 0, 1);
			app.endShape();

		} 
		particleSystem.updateAndDraw();

		app.colorMode(PApplet.RGB, 255);  
	}


	//@Override
	public void init() {
		app.registerKeyEvent(this);
		this.getMTApplication().registerKeyEvent(this);
	}

	//@Override
	public void shutDown() {
		app.unregisterKeyEvent(this);
		this.getMTApplication().unregisterKeyEvent(this);
	}


	/**
	 * 
	 * @param e
	 */
	public void keyEvent(KeyEvent e){
		int evtID = e.getID();
		if (evtID != KeyEvent.KEY_PRESSED)
			return;
		switch (e.getKeyCode()){
		case KeyEvent.VK_BACK_SPACE:
			app.popScene();
			break;
		default:
			break;
		}
	}



	private class Particle {
		private final static float MOMENTUM = 0.5f;
		private final static float FLUID_FORCE = 0.6f;

		private float x, y;
		private float vx, vy;
		//private float radius;       // particle's size
		protected float alpha;
		private float mass;
		private PApplet p;
		private float invWidth;
		private float invHeight;
		private MSAFluidSolver2D fluidSolver;


		public Particle(PApplet p, MSAFluidSolver2D fluidSolver, float invWidth, float invHeight){
			this.p = p;
			this.invWidth = invWidth;
			this.invHeight = invHeight;
			this.fluidSolver = fluidSolver;
		}

		public void init(float x, float y) {
			this.x = x;
			this.y = y;
			vx = 0;
			vy = 0;
			//radius = 5;
			alpha = p.random(0.3f, 1);
			mass = p.random(0.1f, 1);
		}


		public void update() {
			// only update if particle is visible
			if(alpha == 0) return;

			// read fluid info and add to velocity
			int fluidIndex = fluidSolver.getIndexForNormalizedPosition(x * invWidth, y * invHeight);
			vx = fluidSolver.u[fluidIndex] * p.width * mass * FLUID_FORCE + vx * MOMENTUM;
			vy = fluidSolver.v[fluidIndex] * p.height * mass * FLUID_FORCE + vy * MOMENTUM;

			// update position
			x += vx;
			y += vy;

			// bounce of edges
			if(x<0) {
				x = 0;
				vx *= -1;
			}else if(x > p.width) {
				x = p.width;
				vx *= -1;
			}

			if(y<0) {
				y = 0;
				vy *= -1;
			}else if(y > p.height) {
				y = p.height;
				vy *= -1;
			}

			// hackish way to make particles glitter when the slow down a lot
			if(vx * vx + vy * vy < 1) {
				vx = p.random(-1, 1);
				vy = p.random(-1, 1);
			}

			// fade out a bit (and kill if alpha == 0);
			alpha *= 0.999;
			if(alpha < 0.01) 
				alpha = 0;

		}


		public void updateVertexArrays(int i, FloatBuffer posBuffer, FloatBuffer colBuffer) {
			int vi = i * 4;
			posBuffer.put(vi++, x - vx);
			posBuffer.put(vi++, y - vy);
			posBuffer.put(vi++, x);
			posBuffer.put(vi++, y);

			int ci = i * 6;
			colBuffer.put(ci++, alpha);
			colBuffer.put(ci++, alpha);
			colBuffer.put(ci++, alpha);
			colBuffer.put(ci++, alpha);
			colBuffer.put(ci++, alpha);
			colBuffer.put(ci++, alpha);
		}


		public void drawOldSchool(GL gl) {
			gl.glColor3f(alpha, alpha, alpha);
			gl.glVertex2f(x-vx, y-vy);
			gl.glVertex2f(x, y);
		}

	}//end particle class




	public class ParticleSystem{
		private FloatBuffer posArray;
		private FloatBuffer colArray;
		private final static int maxParticles = 5000;
		private int curIndex;

		boolean renderUsingVA = true;

		private Particle[] particles;
		private PApplet p;
		private MSAFluidSolver2D fluidSolver;
		private float invWidth;
		private float invHeight;

		private boolean drawFluid;

		public ParticleSystem(PApplet p, MSAFluidSolver2D fluidSolver) {
			this.p = p;
			this.fluidSolver = fluidSolver;
			this.invWidth = 1.0f/p.width;
			this.invHeight = 1.0f/p.height;

			this.drawFluid = true;

			particles = new Particle[maxParticles];

			for(int i=0; i<maxParticles; i++) {
				particles[i] = new Particle(p, this.fluidSolver, invWidth, invHeight);
			}

			curIndex = 0;

			posArray = BufferUtil.newFloatBuffer(maxParticles * 2 * 2);// 2 coordinates per point, 2 points per particle (current and previous)
			colArray = BufferUtil.newFloatBuffer(maxParticles * 3 * 2);
		}


		public void updateAndDraw(){
			PGraphicsOpenGL pgl = (PGraphicsOpenGL)p.g;         // processings opengl graphics object
			GL gl = pgl.beginGL();                // JOGL's GL object

			gl.glEnable( GL.GL_BLEND );             // enable blending

			if(!drawFluid) 
				fadeToColor(p, gl, 0, 0, 0, 0.05f);

			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);  // additive blending (ignore alpha)
			gl.glEnable(GL.GL_LINE_SMOOTH);        // make points round
			gl.glLineWidth(1);


			if(renderUsingVA) {
				for(int i=0; i<maxParticles; i++) {
					if(particles[i].alpha > 0) {
						particles[i].update();
						particles[i].updateVertexArrays(i, posArray, colArray);
					}
				}    
				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0, posArray);

				gl.glEnableClientState(GL.GL_COLOR_ARRAY);
				gl.glColorPointer(3, GL.GL_FLOAT, 0, colArray);

				gl.glDrawArrays(GL.GL_LINES, 0, maxParticles * 2);
			} 
			else {
				gl.glBegin(GL.GL_LINES);               // start drawing points
				for(int i=0; i<maxParticles; i++) {
					if(particles[i].alpha > 0) {
						particles[i].update();
						particles[i].drawOldSchool(gl);    // use oldschool renderng
					}
				}
				gl.glEnd();
			}

			//			gl.glDisable(GL.GL_BLEND);
			//Reset blendfunction
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			pgl.endGL();
		}


		public void fadeToColor(PApplet p, GL gl, float r, float g, float b, float speed) {
			//			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glColor4f(r, g, b, speed);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex2f(0, 0);
			gl.glVertex2f(p.width, 0);
			gl.glVertex2f(p.width, p.height);
			gl.glVertex2f(0, p.height);
			gl.glEnd();
		}


		public void addParticles(float x, float y, int count ){
			for(int i=0; i<count; i++) addParticle(x + p.random(-15, 15), y + p.random(-15, 15));
		}


		public void addParticle(float x, float y) {
			particles[curIndex].init(x, y);
			curIndex++;
			if(curIndex >= maxParticles) curIndex = 0;
		}



		public boolean isDrawFluid() {
			return drawFluid;
		}

		public void setDrawFluid(boolean drawFluid) {
			this.drawFluid = drawFluid;
		}

	}//end psystem class

	
}


