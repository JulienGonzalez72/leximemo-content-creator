package main;

import org.lexidia.dialogo.segmentation.controller.ControllerText;
import org.lexidia.dialogo.segmentation.reading.ReadThread;

public class ReadingThread extends ReadThread {
	
	public ReadingThread(ControllerText controler) {
		super(controler);
	}
	
	@Override
	public void run() {
		getControler().updateCurrentPhrase();
		
		getControler().showPage(getControler().getPageOfPhrase(getN()));
		
		getControler().removeAllHighlights();
		
		getControler().highlightPhrase(getN());
	}
	
}
