package com.gaspar.formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the code formatting.
 * @author Gáspár Tamás
 */
public class Formatter {
	
	/**
	 * Matches strings in the code. Group 1 holds the matched string. This must be called before any other regex that addes quotation marks, 
	 * or it'll match modifications.
	
	 private static final Pattern STRING_REGEX = Pattern.compile("(\\(?\"\\w*\"\\)?)");
	 */
	
	/**
	 * Regex that matches Java primitive types. Also matches void. Group 2 ($2) holds the matched primitive.
	 */
	private static final Pattern PRIMITIVE_TYPE_REGEX = Pattern.compile("([^A-Z^a-z])(byte|short|int|long|float|double|boolean|char|void)(?![A-Za-z])", Pattern.MULTILINE);
	
	/**
	 * Regex that matches java keywords. Group 2 ($2) holds the matched keyword.
	 */	
	private static final Pattern KEYWORD_REGEX = Pattern.compile("([^A-Z^a-z])(abstract|continue|for|new|switch|assert|default|goto|package|synchronized|" + 
			"do|if|private|this|break|implements|protected|throw|else|import|public|throws|case|enum|instanceof|return|transient|" + 
			"catch|extends|try|final|interface|static|class|finally|strictfp|volatile|const|native|super|while)(?![A-Za-z])",Pattern.MULTILINE);
	
	/**
	 * Regex that matches a word that starts with a capital letter, but not part of an annotation or hexadecimal 
	 * color declaration. Group 1 ($1) holds what was before the matched class name,
	 * while group 2 ($2) holds the matched class name.
	 */
	private static final Pattern CLASS_NAME_REGEX = Pattern.compile("([^\\/^@^#^A-Z^a-z])([A-Z]\\w*)(?![A-Z])", Pattern.MULTILINE);
	
	/**
	 * Regex that matches comments in the code. Group 1 ($1) holds the matched comment.
	 */
	private static final Pattern COMMENT_REGEX = Pattern.compile("(((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|"
			+ "\\*(?!\\/))*\\*\\/)", Pattern.MULTILINE);
	
	/**
	 * Regex that matches the annotations in the code. Group 2 ($2) holds the matched annotation.
	 */
	private static final Pattern ANNOTATION_REGEX = Pattern.compile("([\\s+])(@\\w+)([\\s+])", Pattern.MULTILINE);
	
	/**
	 * Regex that matches class names formatted by {@value Formatter#CLASS_NAME_REGEX}. Group 2 ($2) holds the
	 * actual phrase that was surrounded.
	 */
	private static final Pattern FORMATTED_CLASS_NAME_REGEX = Pattern.compile("(<font color=\"#999900\">)(.*)(</font>)", Pattern.MULTILINE);
	
	/**
	 * Original content of the file.
	 */
	private String fileContent;

	/**
	 * The file name for logging purposes.
	 */
	private String fileName;
	
	public Formatter(String fileContent, String fileName) {
		this.fileContent = fileContent;
		this.fileName = fileName;
	}
	
	/**
	 * Performs the content formatting.
	 * 
	 * @return The formatted code, as a string.
	 */
	public String formatContent() {
		System.out.println("Starting formatting of " + fileName + "...");
		String res = fileContent.replaceAll("<", "&lt;"); //must be called first. does not add "
		//res = STRING_REGEX.matcher(res).replaceAll("<font color=\"#2E8B57\">$1</font>");
		res = PRIMITIVE_TYPE_REGEX.matcher(res).replaceAll("$1<font color=\"#DF7401\">$2</font>");
		res = KEYWORD_REGEX.matcher(res).replaceAll("$1<font color=\"#E65100\">$2</font>");
		res = CLASS_NAME_REGEX.matcher(res).replaceAll("$1<font color=\"#999900\">$2</font>");
		res = ANNOTATION_REGEX.matcher(res).replaceAll("$1<font color=\"red\">$2</font>$3");
		res = COMMENT_REGEX.matcher(res).replaceAll("<font color=\"gray\">$1</font>");
		res = res.replaceAll("\t", "\t&nbsp;&nbsp;&nbsp;&nbsp;").
				replaceAll("(\r\n|\n)", "$1<br/>");
		
		//now there maybe are cases where words with capital letter beginnings INSIDE comments
		//have been surrounded by CLASS_NAME_REGEX replcement.
		Matcher commentMatcher = COMMENT_REGEX.matcher(res);
		while(commentMatcher.find()) {
			String comment = commentMatcher.group(); //this is the comment the matcher found
			String cleanedComment = FORMATTED_CLASS_NAME_REGEX.matcher(comment).replaceAll("$2"); //delete formatting
			res = res.replace(comment, cleanedComment); //update result
		}
		System.out.println("Formatting of " + fileName + " finished!");
		return res;
	}
	
}
