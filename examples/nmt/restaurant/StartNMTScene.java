package nmt.restaurant;

import org.mt4j.MTApplication;

import scenes.WaterSceneExportObf;

public class StartNMTScene extends MTApplication {
	private static final long serialVersionUID = 1L;

	public static void main(String args[]){
		initialize();
	}
	
	@Override
	public void startUp(){
		this.addScene(new MainMenu(this, "Main Menu"));
	}
}
