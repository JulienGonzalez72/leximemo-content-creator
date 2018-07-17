package main;

public class Constants {
	
	public static final String DEFAULT_OUTPUT_NAME = "take on me";
	public static final String DEFAULT_OUTPUT_DIRECTORY = "C:/users/julien/desktop/xport";
	
	public static final String TEMP_PATH = "src/main/resources/temp";
	public static final int MAX_TEMP_FILES = 1000;
	
	public static final String[] CHANNELS = {"Mono", "Stéréo"};
	
	/// paramètres d'enregistrement ///
	
	public static final float RECORD_SAMPLE_RATE = 48000;
	public static final int RECORD_SAMPLE_SIZE = 16;
	public static final int RECORD_CHANNELS = 2;
	
	/// paramètres d'exportation MP3 ///
	
	public static final int EXPORT_BIT_RATE = 128000;
	public static final int EXPORT_SAMPLE_RATE = 48000;
	public static final int EXPORT_CHANNELS = 2;
	
}
