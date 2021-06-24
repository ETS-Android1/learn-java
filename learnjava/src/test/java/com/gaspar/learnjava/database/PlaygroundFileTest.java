package com.gaspar.learnjava.database;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link PlaygroundFile}.
 */
public class PlaygroundFileTest {

    @Test
    public void testClean() {
        String content = "public\t static {\nvoid";
        String expected = "public static {void";
        PlaygroundFile playgroundFile = new PlaygroundFile("Main.java", content);
        Assert.assertEquals(expected, playgroundFile.getCleanedContent());
    }

    @Test
    public void testCleanWithLiterals() {
        String content = "public\t static {\nvoid\t \"hello\thello\"";
        String expected = "public static {void \"hello\thello\"";
        PlaygroundFile playgroundFile = new PlaygroundFile("Main.java", content);
        Assert.assertEquals(expected, playgroundFile.getCleanedContent());
    }

    @Test
    public void testCleanWithWeirdSymbol() {
        String content = "hello\u00a0 world";
        PlaygroundFile file = new PlaygroundFile("Main.java", content);
        Assert.assertEquals("hello world", file.getCleanedContent());
    }
}