/**
 * This is the package for adapters. Adapters manage views within scrollable views, such as
 * {@link android.widget.ListView} or {@link androidx.recyclerview.widget.RecyclerView}. Some
 * adapters are for simple list views, for cases where there aren't too many views present:
 * <ul>
 *     <li>{@link com.gaspar.learnjava.adapters.CourseAdapter}: manages course views in {@link com.gaspar.learnjava.CoursesActivity}.</li>
 *     <li>{@link com.gaspar.learnjava.adapters.TaskAdapter}: manages task views in {@link com.gaspar.learnjava.TasksActivity}.</li>
 *     <li>{@link com.gaspar.learnjava.adapters.ExamAdapter}: manages course views in {@link com.gaspar.learnjava.ExamsActivity}.</li>
 * </ul>
 * The more sophisticated adapters are for {@link androidx.recyclerview.widget.RecyclerView}, where a lot
 * of views need to be managed. These adapters maintain multiple view types:
 * <ul>
 *     <li>{@link com.gaspar.learnjava.adapters.ComponentAdapter}: manages {@link com.gaspar.learnjava.curriculum.components.Component}
 *     objects displayed in chapters and tasks.</li>
 *     <li>{@link com.gaspar.learnjava.adapters.QuestionAdapter}: manages {@link com.gaspar.learnjava.curriculum.questions.Question}
 *     objects inside an exam ({@link com.gaspar.learnjava.ExamActivity}).</li>
 * </ul>
 */
package com.gaspar.learnjava.adapters;