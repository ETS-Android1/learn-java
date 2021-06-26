package com.gaspar.learnjava.formatter;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Handles the code formatting. See the {@link #formatContent(String)} method to check all formatting applied.
 * Note that code samples in the curriculum are pre-formatted for performance and simplicity reasons. This
 * dynamic formatting is only used at the playground, where the user writes the code.
 * @author Gáspár Tamás
 */
public class Formatter {

    /**
     * Performs the content formatting, coloring the Java code to be more readable. Includes the functionality
     * of {@link #formatWhitespaces(String)}. The result can be safely fed into {@link android.text.Html#fromHtml(String, int)}.
     * @param content The unformatted code.
     * @return The formatted code, as a string.
     */
    public String formatContent(@NonNull String content) {
        content = " " + content; //this space is necessary
        //Replace < with its HTML symbol. Must be called first, as otherwise it would match generated tags
        String res = content.replaceAll("<", "&lt;");
        //replace spaces and line breaks with their HTML tags and characters
        res = formatWhitespaces(res);
        //find strings and color them
        res = RegexConstants.TEXT_LITERAL_REGEX.matcher(res).replaceAll("<font color=\"" + FormatColor.TEXT_LITERAL_COLOR + "\">$1</font>");
        //find numeric literals and color them
        res = RegexConstants.NUMERIC_LITERAL_REGEX.matcher(res).replaceAll("<font color=\"" +FormatColor.NUMERIC_LITERAL_COLOR + "\">$1</font>");
        //find primitive type names and color them
        res = RegexConstants.PRIMITIVE_TYPE_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">$2</font>");
        //find Java keywords and color them
        res = RegexConstants.KEYWORD_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.KEYWORD_COLOR + "\">$2</font>");
        //find class names and color them
        res = RegexConstants.CLASS_NAME_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.CLASS_COLOR + "\">$2</font>");
        //find methods and color them
        res = RegexConstants.METHOD_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.METHOD_MEMBER_COLOR + "\">$2</font>$3");
        //find members and color them
        res = RegexConstants.MEMBER_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.METHOD_MEMBER_COLOR + "\">$2</font>");
        //find annotations and color them
        res = RegexConstants.ANNOTATION_REGEX.matcher(res).replaceAll("$1<font color=\"" + FormatColor.ANNOTATION_COLOR + "\">$2</font>$3");
        //find comments and color them
        res = RegexConstants.COMMENT_REGEX.matcher(res).replaceAll("<font color=\"" + FormatColor.COMMENT_COLOR + "\">$1</font>");

        res = clean(res); //clean formatting from where it is not needed.
        res = res.replaceFirst("&nbsp;", " "); //remove space that was added for regex-es
        return res;
    }

    /**
     * "cleans up" parts that don't need formatting (inside comments or string literals).
     * @param formattedContent The content where everything is formatted, even where it should not be.
     * @return The content where it's only formatted where it is necessary.
     */
    private String clean(@NonNull String formattedContent) {
        //Now there maybe are cases where words inside comments were formatted (primitive names, class names, ect).
        Matcher commentMatcher = RegexConstants.COMMENT_REGEX.matcher(formattedContent);
        while(commentMatcher.find()) {
            String comment = commentMatcher.group(); //this is the comment the matcher found
            //an explicit, new string is required here
            String cleanedComment = new String(comment);
            //remove formatting from each word inside the comment
            final Matcher formattedInsideCommentMatcher = RegexConstants.FORMATTED_REGEX.matcher(comment);
            while(formattedInsideCommentMatcher.find()) {
                String before = formattedInsideCommentMatcher.group(1);
                String word = formattedInsideCommentMatcher.group(2);
                Objects.requireNonNull(word);
                String after = formattedInsideCommentMatcher.group(3);
                //System.out.println("Formatted inside comment: " + (before+word+after));
                cleanedComment = cleanedComment.replaceAll(before+word+after, word); //remove formatting
            }
            formattedContent = formattedContent.replace(comment, cleanedComment); //update result
        }
        //Words inside string literals may also have been formatted, that must be removed
        Matcher literalMatcher = RegexConstants.FORMATTED_STRING_REGEX.matcher(formattedContent);
        while(literalMatcher.find()) {
            String literal = literalMatcher.group(); //found a literal
            //System.out.println("Literal: " + literal);
            String cleanedLiteral = new String(literal);
            final Matcher formattedInsideLiteralMatcher = RegexConstants.FORMATTED_REGEX.matcher(literal);
            while(formattedInsideLiteralMatcher.find()) { //find all formatted words inside the literal
                String before = formattedInsideLiteralMatcher.group(1);
                String word = formattedInsideLiteralMatcher.group(2);
                Objects.requireNonNull(word);
                String after = formattedInsideLiteralMatcher.group(3);
                //System.out.println("Formatted inside literal: " + (before+word+after));
                cleanedLiteral = cleanedLiteral.replaceAll(before+word+after, word); //remove formatting
            }
            formattedContent = formattedContent.replace(literal, cleanedLiteral); //finally, replace the literal with the cleaned version
        }
        return formattedContent;
    }

    /**
     * Formats the string by replacing {@code \n} with {@code <br/>} and so on. Only replaces whitespace
     * characters.
     * @param content The content to be formatted.
     * @return The formatted string.
     */
    public String formatWhitespaces(@NonNull String content) {
        return content.replaceAll("(\r\n|\n)", "<br/>")
                .replaceAll(" ", "&nbsp;");
    }
}
