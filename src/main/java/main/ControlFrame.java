package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.lexidia.dialogo.segmentation.controller.ControllerText;
import org.lexidia.dialogo.segmentation.reading.ReadThread;
import org.lexidia.dialogo.segmentation.reading.ReaderFactory;
import org.lexidia.dialogo.segmentation.view.SegmentedTextFrame;

import it.sauronsoftware.jave.EncoderException;

public class ControlFrame extends JFrame {
	
	private static int imageSize = 40;
	private static Image previousIcon, recordIcon, stopIcon, nextIcon, repeatIcon;
	
	private static SegmentedTextFrame currentFrame;
	
	private JPanel panel = new JPanel();
	private JTabbedPane tabPanel = new JTabbedPane();
	
	private JButton previousButton = new JButton();
	private JButton recordButton = new JButton();
	private JButton nextButton = new JButton();
	private JButton repeatButton = new JButton();
	private JTextField goToField = new JTextField();
	
	private JButton folderButton = new JButton("Sélectionner un répertoire de sorti");
	private JTextField folderField = new JTextField(Constants.DEFAULT_OUTPUT_DIRECTORY);
	private JCheckBox autoExportCheck = new JCheckBox("Exportation automatique");
	private JButton exportButton = new JButton("Exporter");
	private JTextField fileNameField = new JTextField(Constants.DEFAULT_OUTPUT_NAME);
	private JButton textButton = new JButton("Importer un texte segmenté");
	
	private JCheckBox autoRecordCheck = new JCheckBox("Enregistrement automatique", true);
	private JComboBox<String> recordChannelCombo = new JComboBox<>(Constants.CHANNELS);
	private JTextField recordSampleRateField = new JTextField(String.valueOf(Constants.RECORD_SAMPLE_RATE));
	private JTextField recordSampleSizeField = new JTextField(String.valueOf(Constants.RECORD_SAMPLE_SIZE));
	
	private JComboBox<String> exportChannelCombo = new JComboBox<>(Constants.CHANNELS);
	private JTextField exportSampleRateField = new JTextField(String.valueOf(Constants.EXPORT_SAMPLE_RATE));
	private JTextField exportBitRateField = new JTextField(String.valueOf(Constants.EXPORT_BIT_RATE));
	
	private CompletionBar bar;
	
	private ControllerText controller;
	private Map<Integer, Recorder> recorders = new HashMap<Integer, Recorder>();
	private File outputDirectory = new File(Constants.DEFAULT_OUTPUT_DIRECTORY);
	
	private boolean usable = true;

	static {
		loadImages();
	}

	public ControlFrame(ControllerText controllerText) {
		setTitle("Leximemo Content Creator - Contrôles");
		setBounds(0, 50, 500, 600);
		setContentPane(panel);
		
		controller = controllerText;
		bar = new CompletionBar(controller.getPhrasesCount());
		
		/// clic sur la barre de complétion ///
		bar.setClickListener((i) -> {
			if (getRecorder().isRecording()) {
				stopRecording();
			}
			controller.goTo(i);
			updateButtons();
		});
		
		panel.setLayout(new BorderLayout());
		
		/// initialise les onglets ///
		JPanel basePanel = new JPanel(new GridLayout(3, 1)),
				advancedPanel = new JPanel(new GridLayout(2, 1));
		tabPanel.addTab("Accueil", basePanel);
		tabPanel.addTab("Avancé", advancedPanel);
		panel.add(tabPanel, BorderLayout.CENTER);
		
		Function<String, JPanel> subPanelF = (title) -> {
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createTitledBorder(title));
			p.setLayout(new GridLayout(2, 1));
			return p;
		};
		JPanel buttonsPanel = subPanelF.apply("Contrôle"),
				exportPanel = subPanelF.apply("Paramètres d'exportation"),
				paramsPanel = subPanelF.apply("Paramètres d'enregistrement"),
				mp3Panel = subPanelF.apply("Paramètres MP3");
		basePanel.add(buttonsPanel);
		basePanel.add(exportPanel);
		basePanel.add(bar);
		
		JPanel buttonsNorthPanel = new JPanel(),
				buttonsSouthPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(2, 1));
		buttonsPanel.add(buttonsNorthPanel);
		buttonsPanel.add(buttonsSouthPanel);
		
		JPanel exportNorthPanel = new JPanel();
		JPanel exportSouthPanel = new JPanel();
		exportPanel.add(exportNorthPanel);
		exportPanel.add(exportSouthPanel);
		
		paramsPanel.setLayout(new FlowLayout());
		mp3Panel.setLayout(new FlowLayout());
		
		buttonsNorthPanel.add(previousButton);
		previousButton.setIcon(new ImageIcon(previousIcon));
		previousButton.setEnabled(false);
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getRecorder().isRecording()) {
					stopRecording();
				}
				controller.doPrevious();
				updateButtons();
			}
		});

		buttonsNorthPanel.add(recordButton);
		recordButton.setIcon(new ImageIcon(recordIcon));
		recordButton.setEnabled(false);
		recordButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/// commence l'enregistrement ///
				if (!getRecorder().isRecording()) {
					startRecording();
				}
				/// termine l'enregistrement ///
				else {
					stopRecording();
				}
				updateButtons();
			}
		});

		buttonsNorthPanel.add(nextButton);
		nextButton.setIcon(new ImageIcon(nextIcon));
		nextButton.setEnabled(false);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getRecorder().isRecording()) {
					stopRecording();
				}
				controller.doNext();
				if (autoRecordCheck.isSelected()) {
					startRecording();
				}
				updateButtons();
			}
		});

		buttonsNorthPanel.add(repeatButton);
		repeatButton.setIcon(new ImageIcon(repeatIcon));
		repeatButton.setEnabled(false);
		repeatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getRecorder().playRecord();
				updateButtons();
			}
		});
		
		JLabel goToLabel = new JLabel("Passer au segment :");
		buttonsSouthPanel.add(goToLabel);
		buttonsSouthPanel.add(goToField);
		goToField.setPreferredSize(new Dimension(40, 20));
		goToField.setEnabled(false);
		goToField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getRecorder().isRecording()) {
					stopRecording();
				}
				controller.goTo(Integer.parseInt(goToField.getText()) - 1);
				updateButtons();
			}
		});
		
		exportNorthPanel.add(folderButton);
		folderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				int response = chooser.showOpenDialog(null);
				if (response == JFileChooser.APPROVE_OPTION) {
					outputDirectory = chooser.getSelectedFile();
					folderField.setText(chooser.getSelectedFile().getAbsolutePath());
					updateButtons();
				}
			}
		});
		
		exportNorthPanel.add(folderField);
		folderField.setPreferredSize(new Dimension(200, 20));
		folderField.setEnabled(false);
		
		exportNorthPanel.add(autoExportCheck);
		autoExportCheck.setEnabled(false);
		
		JLabel fileNameLabel = new JLabel("Nom du fichier de sorti :");
		exportSouthPanel.add(fileNameLabel);
		exportSouthPanel.add(fileNameField);
		fileNameField.setPreferredSize(new Dimension(100, 20));
		
		exportSouthPanel.add(textButton);
		/// bouton importer un texte ///
		textButton.addActionListener((e) -> {
			int confirmResponse = JOptionPane.showConfirmDialog(null,
					"Etes-vous sûr ? Vous perdrez tous les enregistrement effectués !",
					"Avertissement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (confirmResponse != JOptionPane.YES_OPTION) {
				return;
			}
			
			JFileChooser chooser = new JFileChooser(new File("."));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".txt");
				}
				@Override
				public String getDescription() {
					return "*.txt";
				}
			});
			
			int response = chooser.showOpenDialog(null);
			if (response == JFileChooser.APPROVE_OPTION) {
				/// détruit l'instance courante ///
				currentFrame.setVisible(false);
				setVisible(false);
				/// créé une nouvelle instance avec le texte sélectionné ///
				launch(FileUtils.getTextFromFile(chooser.getSelectedFile().getAbsolutePath()));
			}
		});
		
		exportSouthPanel.add(exportButton);
		/// bouton exporter ///
		exportButton.addActionListener((e) -> {
			export();
		});
		
		/// paramètres d'enregistrement ///
		
		paramsPanel.add(autoRecordCheck);
		
		JLabel recordChannelLabel = new JLabel("Nombre de pistes :");
		paramsPanel.add(recordChannelLabel);
		paramsPanel.add(recordChannelCombo);
		recordChannelCombo.setSelectedIndex(Constants.RECORD_CHANNELS - 1);
		
		JLabel recordSampleRateLabel = new JLabel("Nombre d'échantillons par seconde :");
		paramsPanel.add(recordSampleRateLabel);
		paramsPanel.add(recordSampleRateField);
		recordSampleRateField.setColumns(8);
		
		JLabel sampleSizeLabel = new JLabel("Taille de chaque échantillon (en bits) :");
		paramsPanel.add(sampleSizeLabel);
		paramsPanel.add(recordSampleSizeField);
		recordSampleSizeField.setColumns(4);
		
		advancedPanel.add(paramsPanel);
		makeLineGrid(paramsPanel);
		
		/// paramètres MP3 ///
		
		JLabel exportChannelLabel = new JLabel("Nombre de pistes :");
		mp3Panel.add(exportChannelLabel);
		mp3Panel.add(exportChannelCombo);
		exportChannelCombo.setSelectedIndex(Constants.EXPORT_CHANNELS - 1);
		
		JLabel exportSampleRateLabel = new JLabel("Nombre d'échantillons par seconde :");
		mp3Panel.add(exportSampleRateLabel);
		mp3Panel.add(exportSampleRateField);
		exportSampleRateField.setColumns(8);
		
		JLabel exportBitRateLabel = new JLabel("Fréquence de chaque échantillon (en bits/seconde) :");
		mp3Panel.add(exportBitRateLabel);
		mp3Panel.add(exportBitRateField);
		exportBitRateField.setColumns(8);
		
		advancedPanel.add(mp3Panel);
		makeLineGrid(mp3Panel);
		
		setVisible(true);
	}
	
	private void startRecording() {
		getRecorder().setOutputName(FileUtils.getFinalOutputName(
				fileNameField.getText(), controller.getCurrentPhraseIndex() + 1, controller.getPhrasesCount()));
		getRecorder().setAutoExport(autoExportCheck.isSelected());
		
		getRecorder().setFormat(
				Float.parseFloat(recordSampleRateField.getText()),
				Integer.parseInt(recordSampleSizeField.getText()), 
				getSelectedRecordChannel(),
				true, true);
		getRecorder().setMp3Format(
				Integer.parseInt(exportSampleRateField.getText()),
				Integer.parseInt(exportBitRateField.getText()),
				getSelectedExportChannel());
		
		getRecorder().startRecording();
	}
	
	private void stopRecording() {
		bar.complete(controller.getCurrentPhraseIndex());
		getRecorder().stopRecording();
	}
	
	private void export() {
		if (recorders.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Aucun enregistrement a exporter !", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
		
		final AtomicBoolean success = new AtomicBoolean(true);
		
		recorders.forEach((index, recorder) -> {
			if (recorder.canExport()) {
				recorder.setOutputDirectory(outputDirectory);
				try {
					recorder.export();
					FileUtils.saveTextFile(controller.getPhraseContent(index),
							FileUtils.getTextFileFromAudio(recorder.getOutputFile()));
				} catch (EncoderException e) {
					e.printStackTrace();
					success.set(false);
					return;
				}
			}
		});
		
		if (success.get()) {
			JOptionPane.showMessageDialog(null, "Enregistrements exportés avec succés !", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de l'exportation !", "Erreur",
					JOptionPane.ERROR_MESSAGE);
		}
		
		deleteAllTemp();
	}
	
	private void deleteAllTemp() {
		File[] tempFiles = new File(Constants.TEMP_PATH).listFiles();
		Arrays.stream(tempFiles).forEach((file) -> {
			file.delete();
		});
	}
	
	/**
	 * Méthode qui s'exécute lorsque les contrôles sont prêts à être effectifs.
	 */
	public void init() {
		enableAll();
	}

	/**
	 * Actualise l'état de tous les composants de la fenêtre de contrôle.
	 */
	public void updateButtons() {
		if (usable) {
			previousButton.setEnabled(controller.getCurrentPhraseIndex() > 0);
			recordButton.setEnabled(true);
			recordButton.setIcon(new ImageIcon(getRecorder().isRecording() ? stopIcon : recordIcon));
			nextButton.setEnabled(controller.getCurrentPhraseIndex() < controller.getPhrasesCount() - 1);
			repeatButton.setEnabled(getRecorder().hasRecorded() && !getRecorder().isRecording());
			goToField.setEnabled(!getRecorder().isRecording());
			fileNameField.setEnabled(!getRecorder().isRecording());
		} else {
			previousButton.setEnabled(false);
			recordButton.setEnabled(false);
			recordButton.setIcon(new ImageIcon(recordIcon));
			nextButton.setEnabled(false);
			repeatButton.setEnabled(false);
			goToField.setEnabled(false);
		}
	}

	public void disableAll() {
		usable = false;
		updateButtons();
	}

	public void enableAll() {
		usable = true;
		updateButtons();
	}

	private static void loadImages() {
		previousIcon = getIcon("previous_icon.png");
		recordIcon = getIcon("microphone_icon.png");
		stopIcon = getIcon("stop_icon.png");
		nextIcon = getIcon("next_icon.png");
		repeatIcon = getIcon("repeat_icon.png");
	}
	
	/**
	 * Retourne l'enregistreur correspondant au segment actuel.<br>
	 * S'il n'existe pas, le créé.
	 */
	public Recorder getRecorder() {
		if (!recorders.containsKey(controller.getCurrentPhraseIndex())) {
			Recorder recorder = new Recorder();
			recorder.setOnRecordException((ex) -> {
				ex.printStackTrace();
				updateButtons();
				JOptionPane.showMessageDialog(null, "Les paramètres d'enregistrement sont invalides !", "Erreur",
						JOptionPane.ERROR_MESSAGE);
			});
			recorders.put(controller.getCurrentPhraseIndex(), recorder);
		}
		return recorders.get(controller.getCurrentPhraseIndex());
	}
	
	private int getSelectedRecordChannel() {
		return recordChannelCombo.getSelectedIndex() + 1;
	}
	
	private int getSelectedExportChannel() {
		return exportChannelCombo.getSelectedIndex() + 1;
	}
	
	private static Image getIcon(String imageName) {
		try {
			return ImageIO.read(new File("src/main/resources/images/" + imageName)).getScaledInstance(imageSize, imageSize,
					Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Lance une instance de la fenêtre de segmentation et de contrôle pour le texte indiqué.
	 * @param text le texte segmenté à traiter
	 */
	public static void launch(String text) {
		currentFrame = new SegmentedTextFrame("Leximemo Content Creator");
		currentFrame.setHoleTreatment(false);
		
		currentFrame.init(text, 0, new Font(Font.DIALOG, Font.PLAIN, 20), 500, 100, 500, 500);
		currentFrame.start();
		
		currentFrame.setOnInit(() -> {
			final ControllerText controller = new ControllerText(currentFrame);
			final ControlFrame controlFrame = new ControlFrame(controller);
			controlFrame.init();
			
			controller.setHighlightColors(Color.ORANGE, null, null);
			
			controller.setReaderFactory(new ReaderFactory() {
				public ReadThread createReadThread() {
					return new ReadingThread(controller);
				}
			});
			
			controller.goTo(0);
		});
	}
	
	private static void makeLineGrid(JPanel panel) {
		Component[] components = panel.getComponents();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.removeAll();
		JPanel tempPanel = new JPanel();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JLabel) {
				if (i > 0) {
					panel.add(tempPanel);
				}
				tempPanel = new JPanel();
			}
			tempPanel.add(components[i]);
		}
		if (tempPanel.getComponentCount() > 0) {
			panel.add(tempPanel);
		}
	}
	
}
