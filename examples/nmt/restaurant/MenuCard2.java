package nmt.restaurant;
/*     */ 
/*     */ 
/*     */ import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.FloatBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.Timer;

import nmt.beans.Item;
import nmt.db.DBConnect;
import nmt.db.ItemsList;

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.MultipleDragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.transition.FadeTransition;
import org.mt4j.sceneManagement.transition.FlipTransition;
import org.mt4j.sceneManagement.transition.SlideTransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsBuffers;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.opengl.GLFBO;
import org.mt4j.util.opengl.GLTexture;
import org.mt4j.util.opengl.GLTextureSettings;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import advanced.mtShell.MTShellScene;
/*     */ 
/*     */ public class MenuCard2 extends AbstractScene
/*     */ {
/*     */   private MTApplication app;
/*  55 */   private final int TIMER_INTERVAL = 10;
			protected Iscene scene3;
			protected Iscene funScene;
			final ItemsList itemsList;
			
/*     */ 
/*  59 */   private final float M_PI = 3.141593F;
/*     */ 
/*  62 */   private final int TANK_WIDTH = 124;
/*  63 */   private final int TANK_HEIGHT = 124;
/*  64 */   private final int TANK_DEPTH = 124;
/*     */ 
/*  67 */   private final float WATER_LINE = 6.0F;
/*     */ 
/*  70 */   private final int MESHSIZEX = 126;
/*  71 */   private final int MESHSIZEZ = 126;
/*     */ 
/*  74 */   private float SPRING_CONSTANT = 1.07F;
/*     */ 
/*  85 */   private float DAMPING_CONSTANT = 0.05F;
/*     */ 
/*  96 */   private float perturbX = 0.1F;
/*  97 */   private float perturbY = 0.1F;
/*     */ 
/* 126 */   private float envPerTurbX = 1.25F;
/* 127 */   private float envPerTurbY = 1.25F;
/*     */ 
/* 148 */   private float dt = 0.232F;
/*     */ 
/* 152 */   private float[][] hh = new float[126][126];
/* 153 */   private float[][] ff = new float[126][126];
/* 154 */   private float[][] vv = new float[126][126];
/* 155 */   private float[][][][] fn = new float[2][126][126][3];
/* 156 */   private float[][][] vn = new float[126][126][3];
/*     */ 
/* 160 */   private float[][] extforce = new float[126][126];
/*     */ 
/* 163 */   private final float gravity = 9.8F;
/*     */   private PGraphicsOpenGL pgl;
/*     */   private GL gl;
/*     */   private GLU glu;
/* 172 */   private float zoom = 50.0F;
/*     */ 
/* 174 */   private float eyex = 62.0F;
/* 175 */   private float eyey = 62.0F + this.zoom;
/* 176 */   private float eyez = 62.0F;
/* 177 */   private float atx = 62.0F;
/* 178 */   private float aty = 62.0F;
/* 179 */   private float atz = 62.0F;
/* 180 */   private float upx = 0.0F; private float upy = 0.0F; private float upz = -1.0F;
/*     */ 
/* 182 */   private float phi = 90.0F; private float theta = 0.0F;
/*     */   private GLTexture tex;
/*     */   private GLTexture envTex;
/*     */   private Timer timer;
/*     */   private FloatBuffer[] vertBuffers;
/*     */   private FloatBuffer[] texBuffers;
/*     */   private FloatBuffer[] texEnvBuffers;
/*     */   private int texRatioXRect;
/*     */   private int texRatioYRect;
/*     */   private float perturbXRect;
/*     */   private float perturbYRect;
/*     */   private float envCenterX;
/*     */   private float envCenterY;
/*     */   private float envPerTurbXRect;
/*     */   private float envPerTurbYRect;
/*     */   private boolean hasMultiTexture;
/* 262 */   private String waterImagePath = "nmt" + MTApplication.separator + "data"  + MTApplication.separator;
/*     */ 
/* 405 */   private int counter = 0;
/*     */ 
/* 646 */   private float[] avg = new float[3];
/*     */ 
/* 702 */   float[] a = new float[3];
/* 703 */   float[] b = new float[3];
/* 704 */   float[] c = new float[3];
/*     */ 
/* 707 */   float[] pt0 = new float[3];
/* 708 */   float[] pt1 = new float[3];
/* 709 */   float[] pt2 = new float[3];
/* 710 */   float[] pt3 = new float[3];
/*     */ 
/* 713 */   float[] n0 = new float[3];
/* 714 */   float[] n1 = new float[3];
/*     */ 
/*     */   public static void o(float u, float v, FloatBuffer buf, int index)
/*     */   {
/* 225 */     if (buf == null) {
/* 226 */       return;
/*     */     }
/* 228 */     buf.put(index * 2, u);
/* 229 */     buf.put(index * 2 + 1, v);
/*     */   }
/*     */ 
/*     */   private void s()
/*     */   {
/* 248 */     for (int j = 1; j < 125; ++j)
/* 249 */       for (int i = 1; i < 125; ++i) {
/* 250 */         o(i * this.texRatioXRect + this.vn[i][j][0] * this.perturbXRect, j * this.texRatioYRect + this.vn[i][j][2] * this.perturbYRect, this.texBuffers[j], i * 2);
/* 251 */         o(i * this.texRatioXRect + this.vn[i][(j + 1)][0] * this.perturbXRect, (j + 1) * this.texRatioYRect + this.vn[i][(j + 1)][2] * this.perturbYRect, this.texBuffers[j], i * 2 + 1);
/*     */ 
/* 253 */         o(this.envCenterX + this.vn[i][j][0] * this.envPerTurbXRect, this.envCenterY + this.vn[i][j][2] * this.envPerTurbYRect, this.texEnvBuffers[j], i * 2);
/* 254 */         o(this.envCenterX + this.vn[i][(j + 1)][0] * this.envPerTurbXRect, this.envCenterY + this.vn[i][(j + 1)][2] * this.envPerTurbYRect, this.texEnvBuffers[j], i * 2 + 1);
/*     */       }
/*     */   }

/*     */   public MenuCard2(final MTApplication mtApplication, String name)
/*     */   {
/* 274 */     super(mtApplication, name);
/* 275 */     this.app = mtApplication;

itemsList = new ItemsList();
/* 279 */     setClear(false);
/*     */ 
/* 281 */     this.hasMultiTexture = false;
/*     */ 
/* 287 */     if (MT4jSettings.getInstance().isOpenGlMode()) {
/* 288 */       if (!(Tools3D.isGLExtensionSupported(mtApplication, "GL_ARB_texture_rectangle"))) {
/* 289 */         System.err.println("Your graphics card doesent meet the requirements for running the scene: " + name);
/* 290 */         return;
/*     */       }
/* 292 */       int[] maxTextureUnits = new int[1];
/* 293 */       ((PGraphicsOpenGL)this.app.g).gl.glGetIntegerv(34018, maxTextureUnits, 0);
/* 294 */       int nbTextureUnits = maxTextureUnits[0];
/* 295 */       if ((Tools3D.isGLExtensionSupported(mtApplication, "GL_ARB_multitexture")) && 
/* 296 */         (nbTextureUnits >= 3))
/*     */       {
/* 298 */         this.hasMultiTexture = true;
/*     */       }
/*     */     } else {
/* 301 */       System.err.println(name + " requires OpenGL renderer");
/* 302 */       return;
/*     */     }
/*     */ 
/* 305 */     this.pgl = ((PGraphicsOpenGL)this.app.g);
/* 306 */     this.gl = this.pgl.gl;
/* 307 */     this.glu = this.pgl.glu;
/*     */ 
/* 310 */     this.vertBuffers = new FloatBuffer[126];
/* 311 */     this.texBuffers = new FloatBuffer[126];
/* 312 */     this.texEnvBuffers = new FloatBuffer[126];
/*     */ 
/* 314 */     for (int i = 0; i < this.vertBuffers.length; ++i) {
/* 315 */       this.vertBuffers[i] = ToolsBuffers.createVector3Buffer(252);
/* 316 */       this.texBuffers[i] = ToolsBuffers.createFloatBuffer(504);
/* 317 */       this.texEnvBuffers[i] = ToolsBuffers.createFloatBuffer(504);
/*     */     }
/*     */ 
/* 320 */     n();
/*     */ 
/* 323 */     GLTextureSettings tp = new GLTextureSettings();
/*     */ 
/* 325 */     this.tex = 
/* 331 */       new GLTexture(this.app, 
/* 327 */       this.waterImagePath + 
/* 328 */       "mainmenu.jpg", 
/* 331 */       tp);
/*     */ 		

/* 333 */     this.texRatioXRect = (this.tex.width / 126);
/* 334 */     this.texRatioYRect = (this.tex.height / 126);
/*     */ 
/* 336 */     this.perturbXRect = (this.perturbX * this.tex.width);
/* 337 */     this.perturbYRect = (this.perturbY * this.tex.height);
/*     */ 
/* 340 */     GLTextureSettings envTp = new GLTextureSettings();
/*     */ 
/* 343 */     envTp.wrappingHorizontal = GLTexture.WRAP_MODE.REPEAT;
/* 344 */     envTp.wrappingVertical = GLTexture.WRAP_MODE.REPEAT;
/*     */ 
/* 346 */     this.envTex = 
/* 351 */       new GLTexture(this.app, 
/* 348 */       this.waterImagePath + 
/* 349 */       "Reflectg4.png", 
/* 351 */       envTp);
/*     */ 
/* 358 */     this.envCenterX = 0.5F;
/* 359 */     this.envCenterY = 0.5F;
/* 360 */     this.envPerTurbXRect = this.envPerTurbX;
/* 361 */     this.envPerTurbYRect = this.envPerTurbY;
/*     */ 
/* 364 */     MTRectangle dummyWaterRectangle = new MTRectangle(0.0F, 0.0F, MT4jSettings.getInstance().getScreenWidth(), MT4jSettings.getInstance().getScreenHeight(), this.app);
/* 365 */     dummyWaterRectangle.setNoFill(true);
/* 366 */     dummyWaterRectangle.setNoStroke(true);
/* 367 */     dummyWaterRectangle.unregisterAllInputProcessors();
/* 368 */     MultipleDragProcessor drawProc = new MultipleDragProcessor(this.app);
/* 369 */     dummyWaterRectangle.registerInputProcessor(drawProc);
/* 370 */     dummyWaterRectangle.addGestureListener(MultipleDragProcessor.class, new IGestureEventListener() {
/*     */       public boolean processGestureEvent(MTGestureEvent ge) {
/* 372 */         DragEvent de = (DragEvent)ge;
/* 373 */         float x = de.getDragCursor().getCurrentEvtPosX();
/* 374 */         float y = de.getDragCursor().getCurrentEvtPosY();
/*     */ 
/* 376 */         switch (de.getId())
/*     */         {
/*     */         case 0:
/*     */         case 1:
/* 379 */           int xIndex = (int)ToolsMath.map(x, 0.0F, MT4jSettings.getInstance().getScreenWidth(), 0.0F, 126.0F);
/* 380 */           int yIndex = (int)ToolsMath.map(y, 0.0F, MT4jSettings.getInstance().getScreenHeight(), 0.0F, 126.0F);
/* 381 */           if (xIndex < 0)
/* 382 */             xIndex = 0;
/* 383 */           if (xIndex > 125)
/* 384 */             xIndex = 125;
/* 385 */           if (yIndex < 0)
/* 386 */             yIndex = 0;
/* 387 */           if (yIndex > 125) {
/* 388 */             yIndex = 125;
/*     */           }
/*     */ 
/* 391 */           MenuCard2.this.extforce[xIndex][yIndex] = -3.92F;
/* 392 */           break;
/*     */         case 2:
/*     */         }
/*     */ 
/* 398 */         return false;
/*     */       }
/*     */     });

			

				
/* 401 */     getCanvas().addChild(dummyWaterRectangle);
//Create a textfield
PImage bgd = new PImage();
MTRectangle background = new MTRectangle(0,0,app.width, app.height , app);
//Set the background color
this.setClearColor(new MTColor(150, 188, 146, 255));

this.registerGlobalInputProcessor(new CursorTracer(app, this));

//testing 123
DBConnect x = new DBConnect("select * from nmt_menuitemslist");
ResultSet xrs = x.getRs();
try {
	xrs.next();
	System.out.println(xrs.getString(1));
	System.out.println(xrs.getString(2));
	System.out.println(xrs.getString(3));
	System.out.println(xrs.getString(4));

} catch (SQLException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
System.out.println();



refreshList();


String imagePath =   "examples" +  MTApplication.separator + "nmt" + MTApplication.separator + "data" + MTApplication.separator;;
//Button to return to the previous scene
PImage arrow = mtApplication.loadImage(imagePath  + "backbutton.jpg");
MTImageButton previousSceneButton = new MTImageButton(arrow, mtApplication);
previousSceneButton.setNoStroke(true);
if (MT4jSettings.getInstance().isOpenGlMode())
	previousSceneButton.setUseDirectGL(true);
previousSceneButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getID()) {
		case TapEvent.BUTTON_CLICKED:
			itemsList.clearAllFilters();
			app.popScene();
			break;
		default:
			break;
		}
	}
});
getCanvas().addChild(previousSceneButton);
previousSceneButton.scale(1, 1, 1, previousSceneButton.getCenterPointLocal(), TransformSpace.LOCAL);
previousSceneButton.setPositionGlobal(new Vector3D(100, 50, 0));

//Create a textfield for Filter by Type
MTTextArea classTextField = new MTTextArea(app, FontManager.getInstance().createFont(app, "urw.ttf", 
		18, new MTColor(0,0,0,255), new MTColor(0,0,0,255))); 
classTextField.setNoFill(true);
classTextField.setNoStroke(true);
classTextField.removeAllGestureEventListeners();
classTextField.setText("Classify Veg or Non-Veg");
this.getCanvas().addChild(classTextField);
classTextField.setPositionGlobal(new Vector3D(app.width/8f+50, 6*app.height/8f-50));

//image button for filter non veg
PImage nv = mtApplication.loadImage(imagePath + "nv.jpg");
MTImageButton nvfilterButton = new MTImageButton(nv, mtApplication);
nvfilterButton.setNoStroke(true);
if (MT4jSettings.getInstance().isOpenGlMode())
	nvfilterButton.setUseDirectGL(true);
nvfilterButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getID()) {
		case TapEvent.BUTTON_CLICKED:
			itemsList.getItemsByFilter("type", "meat");
			refreshList();
			break;
		default:
			break;
		}
	}
});
getCanvas().addChild(nvfilterButton);
nvfilterButton.scale(1, 1, 1, nvfilterButton.getCenterPointLocal(), TransformSpace.LOCAL);
nvfilterButton.setPositionGlobal(new Vector3D(nvfilterButton.getWidthXY(TransformSpace.GLOBAL) + 5, app.height - nvfilterButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));

//image button for filter veg 
PImage v = mtApplication.loadImage(imagePath + "vegetarian.jpg");
MTImageButton vfilterButton = new MTImageButton(v, mtApplication);
vfilterButton.setNoStroke(true);
if (MT4jSettings.getInstance().isOpenGlMode())
	vfilterButton.setUseDirectGL(true);
vfilterButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getID()) {
		case TapEvent.BUTTON_CLICKED:
			itemsList.getItemsByFilter("type", "veg");
			refreshList();
			break;
		default:
			break;
		}
	}
});
getCanvas().addChild(vfilterButton);
vfilterButton.scale(1, 1, 1, vfilterButton.getCenterPointLocal(), TransformSpace.LOCAL);
vfilterButton.setPositionGlobal(new Vector3D(vfilterButton.getWidthXY(TransformSpace.GLOBAL) + 150, app.height - vfilterButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));

//image button for filter chinese 
PImage chinese = mtApplication.loadImage(imagePath + "chinese.jpg");
MTImageButton chinesefilterButton = new MTImageButton(chinese, mtApplication);
chinesefilterButton.setNoStroke(true);
if (MT4jSettings.getInstance().isOpenGlMode())
	chinesefilterButton.setUseDirectGL(true);
chinesefilterButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getID()) {
		case TapEvent.BUTTON_CLICKED:
			itemsList.getItemsByFilter("cuisine", "chinese");
			refreshList();
			break;
		default:
			break;
		}
	}
});
getCanvas().addChild(chinesefilterButton);
chinesefilterButton.scale(1, 1, 1, chinesefilterButton.getCenterPointLocal(), TransformSpace.LOCAL);
chinesefilterButton.setPositionGlobal(new Vector3D(chinesefilterButton.getWidthXY(TransformSpace.GLOBAL) + 150, 300, 0));


//image button to filter indian
PImage indian = mtApplication.loadImage(imagePath + "indian.jpg");
MTImageButton indianfilterButton = new MTImageButton(indian, mtApplication);
indianfilterButton.setNoStroke(true);
if (MT4jSettings.getInstance().isOpenGlMode())
	indianfilterButton.setUseDirectGL(true);
indianfilterButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getID()) {
		case TapEvent.BUTTON_CLICKED:
			itemsList.getItemsByFilter("cuisine", "indian");
			refreshList();
			break;
		default:
			break;
		}
	}
});
getCanvas().addChild(indianfilterButton);
indianfilterButton.scale(1, 1, 1, indianfilterButton.getCenterPointLocal(), TransformSpace.LOCAL);
indianfilterButton.setPositionGlobal(new Vector3D(indianfilterButton.getWidthXY(TransformSpace.GLOBAL) + 5, 300, 0));

//Set a scene transition - Flip transition only available using opengl supporting the FBO extenstion
if (MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(app))
	this.setTransition(new FlipTransition(app, 700));
else{
	this.setTransition(new FadeTransition(app));
}

/*     */   }
/*     */ 
/*     */   public void drawAndUpdate(PGraphics graphics, long timeDelta)
/*     */   {
/*     */     MenuCard2 tmp1_0 = this; tmp1_0.counter = (int)(tmp1_0.counter + timeDelta);
/*     */ 
/* 412 */     if (this.counter > 10)
/*     */     {
/* 415 */       this.counter -= 10;
/* 416 */       q((int)timeDelta);
/*     */     }
/*     */ 
/* 422 */     clear(graphics);
/* 423 */     this.pgl.beginGL();
/*     */ 
/* 426 */     this.gl.glMatrixMode(5889);
/* 427 */     this.gl.glPushMatrix();
/* 428 */     this.gl.glLoadIdentity();
/* 429 */     this.glu.gluPerspective(60.0D, 1.0D, 1.0D, 1000.0D);
/*     */ 
/* 436 */     this.gl.glMatrixMode(5888);
/* 437 */     this.gl.glLoadIdentity();
/* 438 */     this.glu.gluLookAt(this.eyex, this.eyey, this.eyez, this.atx, this.aty, this.atz, this.upx, this.upy, this.upz);
/*     */ 
/* 440 */     z();
/* 441 */     t();
/* 442 */     s();
/* 443 */     v(this.gl);
/*     */ 
/* 445 */     this.gl.glMatrixMode(5889);
/* 446 */     this.gl.glPopMatrix();
/* 447 */     this.gl.glMatrixMode(5888);
/* 448 */     this.pgl.endGL();
/*     */ 
/* 450 */     super.drawAndUpdate(graphics, timeDelta);
/*     */   }
/*     */ 
/*     */   private void q(int val)
/*     */   {
/* 455 */     u();
/* 456 */     m();
/*     */   }
/*     */ 
/*     */   private void n()
/*     */   {
/* 463 */     for (int i = 0; i < 126; ++i)
/* 464 */       for (int j = 0; j < 126; ++j)
/* 465 */         this.hh[i][j] = (this.vv[i][j] = this.ff[i][j] = this.extforce[i][j] = 0.0F);
/*     */   }
/*     */ 
/*     */   private void u()
/*     */   {
/* 476 */     for (int i = 1; i < 125; ++i)
/* 477 */       for (int j = 1; j < 125; ++j) {
/* 478 */         this.ff[i][j] = 0.0F;
/* 479 */         float n_this = this.hh[i][j];
/*     */ 
/* 481 */         float n_adj = this.hh[(i - 1)][j];
/* 482 */         this.ff[i][j] += -this.SPRING_CONSTANT * (n_this - n_adj);
/* 483 */         this.ff[i][j] -= this.DAMPING_CONSTANT * (this.vv[i][j] - this.vv[(i - 1)][j]);
/*     */ 
/* 485 */         n_adj = this.hh[(i + 1)][j];
/* 486 */         this.ff[i][j] += -this.SPRING_CONSTANT * (n_this - n_adj);
/* 487 */         this.ff[i][j] -= this.DAMPING_CONSTANT * (this.vv[i][j] - this.vv[(i + 1)][j]);
/*     */ 
/* 489 */         n_adj = this.hh[i][(j - 1)];
/* 490 */         this.ff[i][j] += -this.SPRING_CONSTANT * (n_this - n_adj);
/* 491 */         this.ff[i][j] -= this.DAMPING_CONSTANT * (this.vv[i][j] - this.vv[i][(j - 1)]);
/*     */ 
/* 493 */         n_adj = this.hh[i][(j + 1)];
/* 494 */         this.ff[i][j] += -this.SPRING_CONSTANT * (n_this - n_adj);
/* 495 */         this.ff[i][j] -= this.DAMPING_CONSTANT * (this.vv[i][j] - this.vv[i][(j + 1)]);
/*     */ 
/* 498 */         this.ff[i][j] += this.extforce[i][j];
/* 499 */         this.extforce[i][j] = 0.0F;
/*     */       }
/*     */   }
/*     */ 
/*     */   private void m()
/*     */   {
/* 508 */     for (int i = 0; i < 126; ++i) {
/* 509 */       for (int j = 0; j < 126; ++j) {
/* 510 */         this.vv[i][j] += this.ff[i][j] * this.dt;
/* 511 */         this.hh[i][j] += this.vv[i][j];
/*     */         int tmp60_59 = j;
/*     */         float[] tmp60_58 = this.hh[i]; tmp60_58[tmp60_59] = (float)(tmp60_58[tmp60_59] + this.hh[i][j] * -0.0135D);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 537 */     for (int j = 0; j < 125; ++j)
/* 538 */       for (int i = 0; i < 126; ++i) {
/* 539 */         ToolsBuffers.setInBuffer(i, this.hh[i][j], j, this.vertBuffers[j], i * 2);
/* 540 */         ToolsBuffers.setInBuffer(i, this.hh[i][(j + 1)], j + 1, this.vertBuffers[j], i * 2 + 1);
/*     */       }
/*     */   }
/*     */ 
/*     */   private void v(GL gl)
/*     */   {
/* 551 */     gl.glPushMatrix();
/* 552 */     gl.glTranslatef(0.0F, 6.0F, 0.0F);
/*     */ 
/* 554 */     gl.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*     */ 
/* 563 */     gl.glEnableClientState(32884);
/*     */ 
/* 565 */     if (this.hasMultiTexture)
/*     */     {
/* 567 */       gl.glClientActiveTexture(33984);
/* 568 */       gl.glEnableClientState(32888);
/* 569 */       gl.glEnable(this.tex.getTextureTarget());
/* 570 */       gl.glBindTexture(this.tex.getTextureTarget(), this.tex.getTextureID());
/*     */ 
/* 572 */       gl.glActiveTexture(33986);
/* 573 */       gl.glClientActiveTexture(33986);
/* 574 */       gl.glEnableClientState(32888);
/* 575 */       gl.glEnable(this.envTex.getTextureTarget());
/* 576 */       gl.glBindTexture(this.envTex.getTextureTarget(), this.envTex.getTextureID());
/* 577 */       gl.glTexEnvi(8960, 8704, 260);
/*     */ 
/* 580 */       for (int j = 0; j < 125; ++j) {
/* 581 */         FloatBuffer currBuff = this.vertBuffers[j];
/* 582 */         FloatBuffer texBuff = this.texBuffers[j];
/* 583 */         FloatBuffer texEnvBuf = this.texEnvBuffers[j];
/*     */ 
/* 585 */         gl.glClientActiveTexture(33984);
/* 586 */         gl.glTexCoordPointer(2, 5126, 0, texBuff);
/*     */ 
/* 588 */         gl.glClientActiveTexture(33986);
/* 589 */         gl.glTexCoordPointer(2, 5126, 0, texEnvBuf);
/*     */ 
/* 591 */         gl.glVertexPointer(3, 5126, 0, currBuff);
/*     */ 
/* 593 */         gl.glDrawArrays(5, 0, currBuff.capacity() / 3);
/*     */       }
/*     */ 
/* 596 */       gl.glDisableClientState(32884);
/*     */ 
/* 598 */       gl.glClientActiveTexture(33986);
/* 599 */       gl.glDisableClientState(32888);
/* 600 */       gl.glDisable(this.envTex.getTextureTarget());
/*     */ 
/* 602 */       gl.glClientActiveTexture(33984);
/* 603 */       gl.glDisableClientState(32888);
/*     */ 
/* 605 */       gl.glActiveTexture(33984);
/* 606 */       gl.glBindTexture(this.tex.getTextureTarget(), 0);
/* 607 */       gl.glDisable(this.tex.getTextureTarget());
/*     */     }
/*     */     else
/*     */     {
/* 611 */       gl.glEnableClientState(32888);
/* 612 */       gl.glEnable(this.tex.getTextureTarget());
/* 613 */       gl.glBindTexture(this.tex.getTextureTarget(), this.tex.getTextureID());
/*     */ 
/* 615 */       for (int j = 0; j < 125; ++j) {
/* 616 */         FloatBuffer currBuff = this.vertBuffers[j];
/* 617 */         FloatBuffer texBuff = this.texBuffers[j];
/* 618 */         gl.glTexCoordPointer(2, 5126, 0, texBuff);
/* 619 */         gl.glVertexPointer(3, 5126, 0, currBuff);
/* 620 */         gl.glDrawArrays(5, 0, currBuff.capacity() / 3);
/*     */       }
/*     */ 
/* 623 */       gl.glDisableClientState(32884);
/* 624 */       gl.glDisableClientState(32888);
/*     */ 
/* 634 */       gl.glActiveTexture(33984);
/* 635 */       gl.glBindTexture(this.tex.getTextureTarget(), 0);
/* 636 */       gl.glDisable(this.tex.getTextureTarget());
/*     */     }
/*     */ 
/* 639 */     gl.glPopMatrix();
/*     */   }
/*     */ 
/*     */   private void t()
/*     */   {
/* 653 */     for (int i = 0; i < 126; ++i)
/* 654 */       for (int j = 0; j < 126; ++j)
/*     */       {
/* 656 */         this.avg[0] = (this.avg[1] = this.avg[2] = 0.0F);
/*     */ 
/* 659 */         if ((j < 125) && (i < 125)) {
/* 660 */           add(this.avg, this.fn[0][i][j], this.avg);
/*     */         }
/*     */ 
/* 663 */         if ((j < 125) && (i > 0)) {
/* 664 */           add(this.avg, this.fn[0][(i - 1)][j], this.avg);
/* 665 */           add(this.avg, this.fn[1][(i - 1)][j], this.avg);
/*     */         }
/*     */ 
/* 668 */         if ((j > 0) && (i < 125)) {
/* 669 */           add(this.avg, this.fn[0][i][(j - 1)], this.avg);
/* 670 */           add(this.avg, this.fn[1][i][(j - 1)], this.avg);
/*     */         }
/*     */ 
/* 673 */         if ((j > 0) && (i > 0)) {
/* 674 */           add(this.avg, this.fn[1][(i - 1)][(j - 1)], this.avg);
/*     */         }
/*     */ 
/* 677 */         norm(this.avg);
/* 678 */         copy(this.avg, this.vn[i][j]);
/*     */       }
/*     */   }
/*     */ 
/*     */   private void z()
/*     */   {
/* 724 */     for (int i = 0; i < 125; ++i)
/* 725 */       for (int j = 0; j < 125; ++j)
/*     */       {
/* 727 */         this.pt0[0] = i; this.pt0[1] = this.hh[i][j]; this.pt0[2] = j;
/* 728 */         this.pt1[0] = (i + 1); this.pt1[1] = this.hh[(i + 1)][j]; this.pt1[2] = j;
/* 729 */         this.pt2[0] = i; this.pt2[1] = this.hh[i][(j + 1)]; this.pt2[2] = (j + 1);
/* 730 */         this.pt3[0] = (i + 1); this.pt3[1] = this.hh[(i + 1)][(j + 1)]; this.pt3[2] = (j + 1);
/*     */ 
/* 733 */         sub(this.pt0, this.pt1, this.a);
/* 734 */         sub(this.pt2, this.pt1, this.b);
/* 735 */         sub(this.pt3, this.pt1, this.c);
/*     */ 
/* 738 */         cross(this.a, this.b, this.n0);
/* 739 */         norm(this.n0);
/*     */ 
/* 741 */         cross(this.b, this.c, this.n1);
/* 742 */         norm(this.n1);
/*     */ 
/* 744 */         copy(this.n0, this.fn[0][i][j]);
/* 745 */         copy(this.n1, this.fn[1][i][j]);
/*     */       }
/*     */   }
/*     */ 
/*     */   private void cross(float[] a, float[] b, float[] result)
/*     */   {
/* 754 */     result[0] = (a[1] * b[2] - (a[2] * b[1]));
/* 755 */     result[1] = (a[2] * b[0] - (a[0] * b[2]));
/* 756 */     result[2] = (a[0] * b[1] - (a[1] * b[0]));
/*     */   }
/*     */ 
/*     */   private void add(float[] a, float[] b, float[] result)
/*     */   {
/* 761 */     result[0] = (a[0] + b[0]);
/* 762 */     result[1] = (a[1] + b[1]);
/* 763 */     result[2] = (a[2] + b[2]);
/*     */   }
/*     */ 
/*     */   private void sub(float[] a, float[] b, float[] result)
/*     */   {
/* 768 */     result[0] = (a[0] - b[0]);
/* 769 */     result[1] = (a[1] - b[1]);
/* 770 */     result[2] = (a[2] - b[2]);
/*     */   }
/*     */ 
/*     */   private void copy(float[] a, float[] b)
/*     */   {
/* 775 */     b[0] = a[0];
/* 776 */     b[1] = a[1];
/* 777 */     b[2] = a[2];
/*     */   }
/*     */ 
/*     */   private void norm(float[] v)
/*     */   {
/* 782 */     float vlen = ToolsMath.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
/* 783 */     v[0] /= vlen;
/* 784 */     v[1] /= vlen;
/* 785 */     v[2] /= vlen;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 795 */     this.app.frameRate(43.0F);
/* 796 */     this.app.registerKeyEvent(this);
/*     */   }
/*     */ 
/*     */   public void shutDown()
/*     */   {
/* 802 */     this.app.frameRate(MT4jSettings.getInstance().getMaxFrameRate());
/* 803 */     this.app.unregisterKeyEvent(this);
/*     */   }
/*     */ 
/*     */   public void mouseEvent(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void keyEvent(KeyEvent e)
/*     */   {
/* 827 */     int evtID = e.getID();
/* 828 */     if (evtID != 401)
/* 829 */       return;
/* 830 */     switch (e.getKeyCode())
/*     */     {
/*     */     case 67:
/* 833 */       n();
/* 834 */       break;
/*     */     case 70:
/* 836 */       System.out.println("FPS: " + this.app.frameRate);
/* 837 */       break;
/*     */     case 8:
/* 839 */       this.app.popScene();
/* 840 */       break;
/*     */     case 123:
/* 842 */       this.app.saveFrame();
/* 843 */       break;
/*     */     case 80:
/* 845 */       System.out.println("Spring Constant: " + this.SPRING_CONSTANT);
/* 846 */       System.out.println("Spring Damping: " + this.DAMPING_CONSTANT);
/* 847 */       System.out.println("Refraction Factor: " + this.perturbX);
/* 848 */       System.out.println("dt: " + this.dt);
/* 849 */       System.out.println("Reflection Factor: " + this.envPerTurbX);
/*     */     }
/*     */   }
public void refreshList()
{
	MTList lista = new MTList(app.width/2,app.height/8, app.width/2, 6*app.height/8, app);
	MTTextArea[] textField= new MTTextArea[3];
	ArrayList<Item> list = itemsList.getFilteredList();
	if(list.size() == 0)
		{
			itemsList.clearAllFilters();
			refreshList();
		}
	else
	{
	
	// adding the values to the cells
	final MTColor labelColor = new MTColor(255, 255, 255, 255);
	
	for (int i = 0; i < list.size(); i++) {
		final MTListCell cell = new MTListCell( app.width/2, 50, app);	
		
		// declaring twzt fields
		textField[0]= new MTTextArea(app, FontManager.getInstance().createFont(app, "urw.ttf", 
				22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 0))); 
		textField[1]= new MTTextArea(app, FontManager.getInstance().createFont(app, "urw.ttf", 
				22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 0))); 
		textField[2]= new MTTextArea(app, FontManager.getInstance().createFont(app, "urw.ttf", 
				22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 255))); 
		final Item t = list.get(i);
		
		//text field entries
		textField[2].setText(list.get(i).getItem_name() + "              "+list.get(i).getClassifierType("cuisine")+"/"+list.get(i).getClassifierType("class"));
		
		textField[0].setSizeLocal(50, 50);
		textField[1].setSizeLocal(100, 100);
		//cell operations
		cell.setFillColor(labelColor);
		cell.addChildren(textField);
		cell.unregisterAllInputProcessors();
		cell.registerInputProcessor(new TapProcessor(app, 15));
		cell.setBoundsAutoCompute(true);
		cell.addGestureListener(TapProcessor.class, new IGestureEventListener() {
          public boolean processGestureEvent(MTGestureEvent ge) {
                  TapEvent te = (TapEvent)ge;
                  switch (te.getTapID()) { 
                  case TapEvent.BUTTON_DOWN:
                      
                      break;
                  case TapEvent.BUTTON_UP:
                      cell.setFillColor(labelColor);
                      break;
                  case TapEvent.BUTTON_CLICKED:
                	  app.pushScene();
					
						scene3 = new ItemDetails(app, "Scene 3",t);
						app.addScene(scene3);
					
					//Do the scene change
						app.changeScene(scene3);
					break;
                         
                  }
                  return false;
          }
  });

	  lista.addListElement(cell);
	} 
	this.getCanvas().addChild(lista);
   
	
	}
}
/*     */ 
/*     */   public boolean destroy()
/*     */   {
/* 860 */     boolean destroyed = super.destroy();
/* 861 */     if (destroyed)
/*     */     {
/* 863 */       this.tex.destroy();
/* 864 */       this.envTex.destroy();
/*     */     }
/*     */ 
/* 867 */     return destroyed;
/*     */   }
/*     */ }

/* Location:           /home/ameya/workspace3/MT4j/examples/advanced/water/lib/waterScene.jar
 * Qualified Name:     scenes.WaterSceneExportObf
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */