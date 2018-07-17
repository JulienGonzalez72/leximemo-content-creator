package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import javax.sound.sampled.*;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

public class Recorder {
	
	private boolean recording, recorded, autoExport;
	private TargetDataLine line;
	private RecordThread thread;
	private File tempFile;
	private File outputDirectory = new File(Constants.DEFAULT_OUTPUT_DIRECTORY);
	private String outputName = Constants.DEFAULT_OUTPUT_NAME;
	private AudioInputStream recordStream;
	private Consumer<Exception> onRecordException = (e) -> e.printStackTrace();
	
	/**
	 * format d'enregistrement
	 */
	private AudioFormat format = new AudioFormat(
			Constants.RECORD_SAMPLE_RATE, Constants.RECORD_SAMPLE_SIZE, Constants.RECORD_CHANNELS,
			true, true);
	
	private int mp3SampleRate = Constants.EXPORT_SAMPLE_RATE;
	private int mp3BitRate = Constants.EXPORT_BIT_RATE;
	private int mp3Channels = Constants.EXPORT_CHANNELS;

	public void startRecording() {
		recording = true;
		thread = new RecordThread();
		thread.start();
	}
	
	private class RecordThread extends Thread {
		public void run() {
			AudioFormat format = getFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format);
				line.start();
				
				recordStream = new AudioInputStream(line);
				exportTemp();
			} catch (Exception e) {
				recording = false;
				onRecordException.accept(e);
			}
		}
	}
	
	public void stopRecording() {
		recording = false;
		recorded = true;
		line.stop();
		line.close();
		if (autoExport) {
			try {
				export();
			} catch (EncoderException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public void export() throws EncoderException {
		AudioAttributes audioAttributes = new AudioAttributes();
		audioAttributes.setCodec("libmp3lame");
		audioAttributes.setBitRate(mp3BitRate);
		audioAttributes.setChannels(mp3Channels);
		audioAttributes.setSamplingRate(mp3SampleRate);
		EncodingAttributes attributes = new EncodingAttributes();
		attributes.setFormat("mp3");
		attributes.setAudioAttributes(audioAttributes);
		Encoder encoder = new Encoder();
		encoder.encode(tempFile, getOutputFile(), attributes);
	}
	
	public boolean canExport() {
		return tempFile != null && tempFile.exists();
	}
	
	private void exportTemp() {
		try {
			tempFile = new File(Constants.TEMP_PATH + "/audio" + getTempSuffix() + ".wav");
			AudioSystem.write(recordStream, AudioFileFormat.Type.WAVE, tempFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getTempSuffix() {
		Random r = new Random();
		int n;
		do {
			n = r.nextInt(Constants.MAX_TEMP_FILES);
		} while (containsTempSuffix("_" + String.valueOf(n)));
		return "_" + n;
	}
	
	private static boolean containsTempSuffix(String suffix) {
		File[] tempFiles = new File(Constants.TEMP_PATH).listFiles();
		return Arrays.stream(tempFiles)
				.anyMatch((file) -> file.getName().contains(suffix));
	}
	
	public void playRecord() {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(tempFile);
			DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(ais);
			clip.start();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private AudioFormat getFormat() {
		return format;
	}
	
	public void setFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
		this.format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	
	public void setMp3Format(int sampleRate, int bitRate, int channels) {
		mp3SampleRate = sampleRate;
		mp3BitRate = bitRate;
		mp3Channels = channels;
	}
	
	public boolean hasRecorded() {
		return recorded;
	}
	
	public File getOutputFile() {
		return new File(outputDirectory.getAbsolutePath() + "/" + outputName + ".mp3");
	}
	
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public void setAutoExport(boolean autoExport) {
		this.autoExport = autoExport;
	}
	
	public void setOnRecordException(Consumer<Exception> onRecordException) {
		this.onRecordException = onRecordException;
	}
	
}
