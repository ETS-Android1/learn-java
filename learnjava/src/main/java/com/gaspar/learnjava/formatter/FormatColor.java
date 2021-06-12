package com.gaspar.learnjava.formatter;

import androidx.annotation.StringDef;


@StringDef({
        FormatColor.TEXT_LITERAL_COLOR,  FormatColor.PRIMITIVE_COLOR, FormatColor.KEYWORD_COLOR,
        FormatColor.CLASS_COLOR, FormatColor.ANNOTATION_COLOR, FormatColor.COMMENT_COLOR,
        FormatColor.METHOD_MEMBER_COLOR, FormatColor.NUMERIC_LITERAL_COLOR
})
public @interface FormatColor {

    /**
     * Dark green color for string and character literals.
     */
    String TEXT_LITERAL_COLOR = "#2E8B57";

    /**
     * Lighter orange color for primitive types.
     */
    String PRIMITIVE_COLOR = "#DF7401";

    /**
     * Darker orange color for Java keywords.
     */
    String KEYWORD_COLOR = "#E65100";

    /**
     * Lighter green/yellow color for class names.
     */
    String CLASS_COLOR = "#999900";

    /**
     * Red color for annotations.
     */
   String ANNOTATION_COLOR = "red";

    /**
     * Gray color for comments.
     */
   String COMMENT_COLOR = "gray";

    /**
     * Light blue color for methods and members.
     */
    String METHOD_MEMBER_COLOR = "#ADD8E6";

    /**
     * Dandelion color for numeric literals.
     */
    String NUMERIC_LITERAL_COLOR = "#F0E130";
}
