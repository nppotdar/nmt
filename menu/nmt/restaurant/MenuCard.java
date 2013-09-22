package nmt.restaurant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.transition.FadeTransition;
import org.mt4j.sceneManagement.transition.FlipTransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.opengl.GLFBO;

import processing.core.PImage;

public class MenuCard extends AbstractScene {

	private MTApplication mtApp;
	protected Iscene scene3;
	final ItemsList itemsList;
	
    
	
//    private String imagePath = System.getProperty("user.dir") + File.separator + "examples"+  File.separator +"basic"+  File.separator + "scenes" + File.separator + "data" + File.separator;
	private String imagePath =  "examples" +  MTApplication.separator + "nmt" + MTApplication.separator + "data" + MTApplication.separator;
	
	public void refreshList()
	{
		MTList lista = new MTList(mtApp.width/2,mtApp.height/8, mtApp.width/2, 6*mtApp.height/8, mtApp);
		MTTextArea[] textField= new MTTextArea[3];
		ArrayList<Item> list = itemsList.getFilteredList();
		if(list == null)
			{
			MTTextArea tmp = new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "arial.ttf", 20, new MTColor(5, 10, 55, 2), new MTColor(0, 0, 0, 0))); 
			tmp.setText("Sorry no Item of this choice available");
			this.getCanvas().addChild(tmp);
			tmp.setPositionGlobal(new Vector3D(mtApp.width/2, mtApp.height/4f));
			}
		else
		{
		
		// adding the values to the cells
		final MTColor labelColor = new MTColor(255, 255, 255, 255);
		
		for (int i = 0; i < list.size(); i++) {
			final MTListCell cell = new MTListCell( mtApp.width/2, 50, mtApp);	
			
			// declaring twzt fields
			textField[0]= new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "urw.ttf", 
					22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 0))); 
			textField[1]= new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "urw.ttf", 
					22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 0))); 
			textField[2]= new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "urw.ttf", 
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
			cell.registerInputProcessor(new TapProcessor(mtApp, 15));
			cell.addGestureListener(TapProcessor.class, new IGestureEventListener() {
	          public boolean processGestureEvent(MTGestureEvent ge) {
	                  TapEvent te = (TapEvent)ge;
	                  switch (te.getTapID()) { 
	                  case TapEvent.BUTTON_DOWN:
	                      cell.setFillColor(new MTColor(180, 12, 76, 30));
	                      break;
	                  case TapEvent.BUTTON_UP:
	                      cell.setFillColor(labelColor);
	                      break;
	                  case TapEvent.BUTTON_CLICKED:
	  					mtApp.pushScene();
						
							scene3 = new ItemDetails(mtApp, "Scene 3",t);
							mtApp.addScene(scene3);
						//Do the scene change
						mtApp.changeScene(scene3);
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
	
	public MenuCard(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.mtApp = mtApplication;
		
		PImage bgd = new PImage();
		MTRectangle background = new MTRectangle(0,0,mtApp.width, mtApp.height , mtApp);
		//Set the background color
		this.setClearColor(new MTColor(150, 188, 146, 255));
		
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));
		
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
		
		
		itemsList = new ItemsList();
		refreshList();


		//Button to return to the previous scene
		PImage arrow = mtApplication.loadImage(imagePath + "arrowRight.png");
		MTImageButton previousSceneButton = new MTImageButton(arrow, mtApplication);
		previousSceneButton.setNoStroke(true);
		if (MT4jSettings.getInstance().isOpenGlMode())
			previousSceneButton.setUseDirectGL(true);
		previousSceneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getID()) {
				case TapEvent.BUTTON_CLICKED:
					itemsList.clearAllFilters();
					mtApp.popScene();
					break;
				default:
					break;
				}
			}
		});
		getCanvas().addChild(previousSceneButton);
		previousSceneButton.scale(-1, 1, 1, previousSceneButton.getCenterPointLocal(), TransformSpace.LOCAL);
		previousSceneButton.setPositionGlobal(new Vector3D(0, mtApp.height - previousSceneButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));
		
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
		nvfilterButton.setPositionGlobal(new Vector3D(nvfilterButton.getWidthXY(TransformSpace.GLOBAL) + 5, mtApp.height - nvfilterButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));
		
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
		vfilterButton.setPositionGlobal(new Vector3D(vfilterButton.getWidthXY(TransformSpace.GLOBAL) + 150, mtApp.height - vfilterButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));
		
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
		chinesefilterButton.setPositionGlobal(new Vector3D(chinesefilterButton.getWidthXY(TransformSpace.GLOBAL) + 150, mtApp.height-( mtApp.height - chinesefilterButton.getHeightXY(TransformSpace.GLOBAL) - 5), 0));


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
		indianfilterButton.setPositionGlobal(new Vector3D(indianfilterButton.getWidthXY(TransformSpace.GLOBAL) + 50, mtApp.height-( mtApp.height - indianfilterButton.getHeightXY(TransformSpace.GLOBAL) - 5), 0));

		//Set a scene transition - Flip transition only available using opengl supporting the FBO extenstion
		if (MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(mtApp))
			this.setTransition(new FlipTransition(mtApp, 700));
		else{
			this.setTransition(new FadeTransition(mtApp));
		}
	}

	@Override
	public void init() {
		System.out.println("Entered scene: " +  this.getName());
	}

	@Override
	public void shutDown() {
		System.out.println("Left scene: " +  this.getName());
	}

}
