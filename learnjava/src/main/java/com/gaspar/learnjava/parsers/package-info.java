/**
 * Classes in this package are responsible to convert XML files and images, located in the
 * assets folder into {@link com.gaspar.learnjava.curriculum.components.Component}s and
 * {@link com.gaspar.learnjava.curriculum.questions.Question}s. These are then get displayed inside
 * activities.
 * <p>
 * Each component and question defines an XML structure, which the parsers must recognize, and create the
 * appropriate object from it. Most components and questions have a simple structure, which can be easily
 * understood by looking at a chapter or exam XML file. The more complex components such as {@link com.gaspar.learnjava.curriculum.components.InteractiveComponent}
 * and {@link com.gaspar.learnjava.curriculum.questions.MultiChoiceQuestion }have their structure documented at class level.
 */
package com.gaspar.learnjava.parsers;