package main;

import java.io.*;

public class FileUtils {
	
	/**
	 * Retourne le nom du fichier à exporter (sans l'extension) avec un nom générique et un numéro.
	 * @param genericName le nom générique
	 * @param index le numéro du fichier (correspondant au segment)
	 * @param phrases le nombre total de segments dans le texte
	 */
	public static String getFinalOutputName(String genericName, int index, int phrasesCount) {
		/// calcule la puissance de 10 supérieure à la maximale ///
		int exp = 1;
		int limit = 10;
		while (limit < phrasesCount) {
			limit = (int) Math.pow(10, ++exp);
		}
		
		/// comble avec des 0 ///
		String zeros = "";
		for (int i = 0; i < String.valueOf(limit).length() - String.valueOf(index).length() - 1; i++) {
			zeros += "0";
		}
		
		return genericName + "_" + zeros + index;
	}
	
	/**
	 * Retourne le fichier texte à créer à partir d'un fichier audio.
	 * @param audioFile le fichier audio correspondant
	 * @return un fichier texte, dans le même emplacement que le fichier audio indiqué, non créé
	 */
	public static File getTextFileFromAudio(File audioFile) {
		return new File(audioFile.getAbsolutePath().replace(".wav", ".txt").replace(".mp3", ".txt"));
	}
	
	public static void copy(File srcFile, File destFile) {
		try {
			FileInputStream fis = new FileInputStream(srcFile);
			FileOutputStream fos = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
			fis.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveTextFile(String text, File destFile) {
		try {
			OutputStream ips = null;
			ips = new FileOutputStream(destFile);
			OutputStreamWriter ipsr = new OutputStreamWriter(ips);
			BufferedWriter br = new BufferedWriter(ipsr);
			br.write(text);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getTextFromFile(String path) {
		try {
			File fichierTxt = new File(path);
			InputStream ips = null;
			ips = new FileInputStream(fichierTxt);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String toReturn = "";
			String ligneCourante = br.readLine();
			while (ligneCourante != null) {
				toReturn += ligneCourante + " ";
				ligneCourante = br.readLine();
			}
			br.close();
			return toReturn;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
