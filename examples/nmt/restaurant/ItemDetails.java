package nmt.restaurant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import nmt.beans.Item;
import nmt.beans.ItemReviews;
import nmt.db.DBConnect;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.widgets.MTBackgroundImage;
import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.widgets.MTTextArea.ExpandDirection;
import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
import org.mt4j.components.visibleComponents.widgets.buttons.MTSvgButton;
import org.mt4j.components.visibleComponents.widgets.keyboard.MTKeyboard;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.transition.FadeTransition;
import org.mt4j.sceneManagement.transition.FlipTransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.opengl.GLFBO;

import processing.core.PImage;

public class ItemDetails extends AbstractScene {

	private MTApplication mtApp;
	
	
//	private String imagePath = System.getProperty("user.dir") + File.separator + "examples"+  File.separator +"basic"+  File.separator + "scenes" + File.separator + "data" + File.separator;
	private String imagePath =  "examples" +  MTApplication.separator + "nmt" + MTApplication.separator + "data" + MTApplication.separator;

	public ItemDetails(MTApplication mtApplication, String name, final Item item) {
		super(mtApplication, name);
		this.mtApp = mtApplication;
		
		
		//Set the background color
		if(item.getClassifierType("type").equalsIgnoreCase("veg"))
		{
        MTBackgroundImage bck =  new MTBackgroundImage(mtApplication, mtApplication.loadImage(imagePath + "veg.jpg"), false);
        bck.unregisterAllInputProcessors();
        bck.removeAllGestureEventListeners();
        this.getCanvas().addChild(bck);
		}
		if(item.getClassifierType("type").equalsIgnoreCase("meat"))
		{
        MTBackgroundImage bck =  new MTBackgroundImage(mtApplication, mtApplication.loadImage(imagePath + "meat.jpg"), false);
        bck.unregisterAllInputProcessors();
        bck.removeAllGestureEventListeners();
        this.getCanvas().addChild(bck);
		}
        
        //Create a textfield for Name of the Item
		MTTextArea nameTextField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				30, new MTColor(0, 128, 0, 255), new MTColor(0, 128, 0, 255))); 
		nameTextField.setNoFill(true);
		nameTextField.setNoStroke(true);
		nameTextField.setText(item.getItem_name());
		this.getCanvas().addChild(nameTextField);
		nameTextField.setPositionGlobal(new Vector3D(mtApplication.width/8f, mtApplication.height/8f));
		
		
		//Create a textfield for Price fo the Item
		MTTextArea priceTextField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				30, new MTColor(0, 128, 0, 255), new MTColor(0, 128, 0, 255))); 
		priceTextField.setNoFill(true);
		priceTextField.setNoStroke(true);
		priceTextField.setText(""+item.getItem_price());
		this.getCanvas().addChild(priceTextField);
		priceTextField.setPositionGlobal(new Vector3D(mtApplication.width*3/4f, mtApplication.height/4f));
		
		//Create a textfield for description of the Item
		MTTextArea descriptionTextField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				20, new MTColor(0, 128, 0, 255), new MTColor(0, 128, 0, 255))); 
		descriptionTextField.setNoFill(true);
		descriptionTextField.setNoStroke(true);
		descriptionTextField.setText(""+item.getItem_description());
		this.getCanvas().addChild(descriptionTextField);
		descriptionTextField.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height*1/4f));
		
		
		
		//Create a textfield for Class(starter/dessert.etc) of the Item
		MTTextArea classTextField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				50, new MTColor(255, 255, 255, 255), new MTColor(255, 255, 255, 255))); 
		classTextField.setNoFill(true);
		classTextField.setNoStroke(true);
		classTextField.setText(""+item.getItem_classifiers().get("class"));
		this.getCanvas().addChild(classTextField);
		classTextField.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height/2f));
		
		//Create a textfield for Cuisine of the Item
		MTTextArea cuisineTextField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				50, new MTColor(255, 255, 255, 255), new MTColor(255, 255, 255, 255))); 
		cuisineTextField.setNoFill(true);
		cuisineTextField.setNoStroke(true);
		cuisineTextField.setText(""+item.getItem_classifiers().get("cuisine"));
		this.getCanvas().addChild(cuisineTextField);
		cuisineTextField.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height*3/4f));
		
		
		
		//Button to return to the previous scene
		PImage arrow = mtApplication.loadImage(imagePath + "back_menu.jpg");
		MTImageButton previousSceneButton = new MTImageButton(arrow, mtApplication);
		previousSceneButton.setNoStroke(true);
		if (MT4jSettings.getInstance().isOpenGlMode())
			previousSceneButton.setUseDirectGL(true);
		previousSceneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getID()) {
				case TapEvent.BUTTON_CLICKED:
					mtApp.popScene();
					break;
				default:
					break;
				}
			}
		});
		getCanvas().addChild(previousSceneButton);
		previousSceneButton.scale(1, 1, 1, previousSceneButton.getCenterPointLocal(), TransformSpace.LOCAL);
		previousSceneButton.setPositionGlobal(new Vector3D( 7*mtApp.width/8 , mtApp.height - previousSceneButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));
		
		//Set a scene transition - Flip transition only available using opengl supporting the FBO extenstion
		if (MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(mtApp))
			this.setTransition(new FlipTransition(mtApp, 700));
		else{
			this.setTransition(new FadeTransition(mtApp));
		}
		
		PImage keyboardImg = mtApp.loadImage("examples" + MTApplication.separator + "nmt"+ MTApplication.separator + "data"+ MTApplication.separator 
//				+ "keyb2.png");
				+ "add-review_button.jpg");
		
		final MTImageButton keyboardButton = new MTImageButton(keyboardImg, mtApp);
		keyboardButton.setFillColor(new MTColor(255,255,255,200));
		keyboardButton.setName("KeyboardButton");
		keyboardButton.setNoStroke(true);
//		keyboardButton.translateGlobal(new Vector3D(5,5,0));
		keyboardButton.translateGlobal(new Vector3D(-2,mtApp.height-keyboardButton.getWidthXY(TransformSpace.GLOBAL)+2,0));
		this.getCanvas().addChild(keyboardButton);

		keyboardButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getID()) {
				case TapEvent.BUTTON_CLICKED:
					//Flickr Keyboard
			        final MTKeyboard keyb = new MTKeyboard(mtApp);
			        keyb.setFillColor(new MTColor(30, 30, 30, 210));
			        keyb.setStrokeColor(new MTColor(0,0,0,255));
			        
			        final MTTextArea t = new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "arial.ttf", 50, 
			        		new MTColor(0,0,0,255), //Fill color 
							new MTColor(0,0,0,255))); //Stroke color
			        t.setExpandDirection(ExpandDirection.UP);
					t.setStrokeColor(new MTColor(0,0 , 0, 255));
					t.setFillColor(new MTColor(205,200,177, 255));
					t.unregisterAllInputProcessors();
					t.setEnableCaret(true);
					t.snapToKeyboard(keyb);
					keyb.addTextInputListener(t);
			        
			        //Review Button for the keyboard
			        MTSvgButton flickrButton = new MTSvgButton( "advanced" + MTApplication.separator +  "flickrMT" + MTApplication.separator + "data" + MTApplication.separator
							+ "Flickr_Logo.svg", mtApp);
			        flickrButton.scale(0.4f, 0.4f, 1, new Vector3D(0,0,0), TransformSpace.LOCAL);
			        flickrButton.translate(new Vector3D(0, 15,0));
			        flickrButton.setBoundsPickingBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);
			        
			            
			        //Add actionlistener to the review button
			        flickrButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if (arg0.getSource() instanceof MTComponent){
								switch (arg0.getID()) {
								case TapEvent.BUTTON_CLICKED:
							        String review = "";
							        review = t.getText();
							        System.out.println("review is \"" + t.getText() + "\"");
							        t.clear();
							        keyb.close();
							        item.addItemReview(review);
								default:
									break;
								}
							}
						}
					});
					keyb.addChild(flickrButton);
					getCanvas().addChild(keyb);
					keyb.setPositionGlobal(new Vector3D(mtApp.width/2f, mtApp.height/2f,0));
					break;
				default:
					break;
				}
			}
		});
		
		//reviews current
        MTList lista = new MTList(mtApp.width/4,mtApp.height/2+mtApp.height/4, mtApp.width/2, 100, mtApp);
        int noOfReviews  = item.getItem_reviews().getNumber();
        ArrayList<String> reviewList= item.getItem_reviews().getReviews();
        
        
		MTTextArea[] textField= new MTTextArea[item.getItem_reviews().getNumber()];
		
		{
		
		// adding the values to the cells
		final MTColor labelColor = new MTColor(255, 255, 255, 255);
		
		for (int i = 0; i < reviewList.size(); i++) {
			final MTListCell cell = new MTListCell( mtApp.width/2, 50, mtApp);	
			
			// declaring twzt fields
			textField[0]= new MTTextArea(mtApp, FontManager.getInstance().createFont(mtApp, "urw.ttf", 
					22, new MTColor(0, 0, 0, 255), new MTColor(0, 0, 0, 0))); 
			final String t = reviewList.get(i);
			
			//text field entries
			textField[0].setText( reviewList.get(i));
	
			//cell operations
			cell.setFillColor(labelColor);
			cell.addChild(textField[0]);
			cell.unregisterAllInputProcessors();
			
	
		  lista.addListElement(cell);
		} 
		this.getCanvas().addChild(lista);
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
