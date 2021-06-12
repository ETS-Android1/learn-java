package com.gaspar.learnjava.formatter;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatterTest {

    public static Formatter formatter;

    @BeforeClass
    public static void init() {
        formatter = new Formatter();
    }

    @Test
    public void testLineBreakAndTab() {
        //not actual code
        String code = "code\ncode\tcode";
        String res = formatter.formatContent(code);
        //the \t is kept for some reason in the result, this is not a mistake
        String expected = "code\n<br/>code\t&nbsp;&nbsp;&nbsp;&nbsp;code";
        assertEquals(expected, res);
    }

    @Test
    public void testDeclarationRegex() {
        String dec = "String s;";
        String res = formatter.formatContent(dec);
        assertEquals("<font color=\"" + FormatColor.CLASS_COLOR + "\">String</font> s;", res);
    }

    @Test
    public void testNumericLiteral() {
        String numLit = "int x = 3;\ndouble d = -5.6;";
        String res = formatter.formatContent(numLit);
        String expected = "<font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">int</font> x = <font color=\"" + FormatColor.NUMERIC_LITERAL_COLOR + "\">3</font>;\n";
        expected += "<br/><font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">double</font> d = <font color=\"" + FormatColor.NUMERIC_LITERAL_COLOR + "\">-5.6</font>;";
        assertEquals(expected, res);
    }

    @Test
    public void testTextLiteral() {
        String dec = "String s = \"Hello\";";
        String res = formatter.formatContent(dec);
        assertEquals("<font color=\"" + FormatColor.CLASS_COLOR + "\">String</font> s = <font color=\"" + FormatColor.TEXT_LITERAL_COLOR + "\">\"Hello\"</font>;", res);
    }

    @Test
    public void testSingleLineComment() {
        String comment = "int x = 3; //this is a comment";
        String res = formatter.formatContent(comment);
        String expected = "<font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">int</font> x = <font color=\"" + FormatColor.NUMERIC_LITERAL_COLOR + "\">3</font>; ";
        expected += "<font color=\"" + FormatColor.COMMENT_COLOR + "\">//this is a comment</font>";
        assertEquals(expected, res);
    }

    @Test
    public void testMultiLineComment() {
        String comment = "int x = 3; /*this is a comment*/";
        String res = formatter.formatContent(comment);
        String expected = "<font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">int</font> x = <font color=\"" + FormatColor.NUMERIC_LITERAL_COLOR + "\">3</font>; ";
        expected += "<font color=\"" + FormatColor.COMMENT_COLOR + "\">/*this is a comment*/</font>";
        assertEquals(expected, res);
    }

    @Test
    public void testAnnotation() {
        String annotated = "@Override\nvoid func() {}";
        String res = formatter.formatContent(annotated);
        String expected = "<font color=\"" + FormatColor.ANNOTATION_COLOR + "\">@Override</font>\n";
        //void is using primitive type color, this is not a mistake
        expected += "<br/><font color=\"" + FormatColor.PRIMITIVE_COLOR + "\">void</font> <font color=\"" + FormatColor.METHOD_MEMBER_COLOR + "\">func</font>() {}";
        assertEquals(expected, res);
    }
}
