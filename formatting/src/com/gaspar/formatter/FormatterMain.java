package com.gaspar.formatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This project is used to format Java code with HTML tags, so that it can be displayed by the
 * "Learn Java" appication.  
 * 
 * @author Gáspár Tamás
 */
public abstract class FormatterMain {

	/**
	 * Contains that apsolute path of the folder where this program is running.
	 */
	private static final String APP_PATH = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath();
	
	/**
	 * Entry point. App parses the files from the directory. These files are expected to be .txt
	 * .java files. These files must be in the same folder. Files names should not contain extra dots.
	 * A txt file can be ignored by making it start with '_'.
	 * 
	 * @throws FileNotFoundException If some of the files from the directory couldn't be loaded.
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File currentDir = new File(APP_PATH);
		File[] filesInDir = currentDir.listFiles();
		System.out.println("Starting parsing and formatting...");
		for(File file: filesInDir) {
			if(isFormattableFile(file)) formatAndWrite(file.getName(), readFileAsString(file.getName()));
		}
	}

	/**
	 * Uses a background thread to format and write the original content to a new file.
	 * 
	 * @param originalContent To be formatted.
	 */
	private static void formatAndWrite(String fileName, String originalContent) {
		new Thread(() -> {
			Formatter formatter = new Formatter(" " + originalContent, fileName); //the space is added to make regexes work
			createFormattedFile(fileName, formatter.formatContent());
		}).start();
	}
	
	/**
	 * Reads the content of a file into a string.
	 * 
	 * @param fileName The name of the file. Must be in the same folder.
	 * @return The content of the file as one string.
	 */
	private static String readFileAsString(String fileName) throws IOException {
		String s = new String(Files.readAllBytes(Paths.get(APP_PATH + "\\" + fileName)));
		System.out.println("File read: " + fileName);
		return s;
	}
	
	/**
	 * Creates a new file (same name as the one it was formatted from, but '_formatted' appended). Regardless of original
	 * extension, this will be a .txt file.
	 * 
	 * @param fileName Original file name.
	 * @param formattedContent The formatted content that will be written to the file.
	 */
	private static void createFormattedFile(String fileName, String formattedContent) {
		try(PrintWriter pw = new PrintWriter(formattedFileName(fileName))) {
			System.out.println("Starting to write formatted version of " + fileName);
			formattedContent = formattedContent.substring(1); //remove first space that was added to make regexes work
			pw.write(formattedContent);
			System.out.println("Task completed for " + fileName);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the new, appended file name.
	 */
	private static String formattedFileName(String originalName) {
		String[] split = originalName.split("\\.");
		return "_" + split[0] + "_formatted." + split[1];
	}
	
	/**
	 * Decides if a file should be parsed and converted or ignored. Folders, files with non txt or java extensions
	 * and files starting with '_' are ignored.
	 */
	private static boolean isFormattableFile(File file) {
		if(file.isDirectory()) return false;
		if(file.getName().startsWith("_")) return false;
		String[] split = file.getName().split("\\.");
		if(split[split.length-1].equals("txt") || split[split.length-1].equals("java")) {
			return true;
		} else {
			return false;
		}
	}
}
