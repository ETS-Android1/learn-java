/**
 * The package of everything that can be displayed in an exam, called {@link com.gaspar.learnjava.curriculum.questions.Question}s.
 * Each question type has its own subclass.
 * <p>
 * Questions can be efficiently displayed inside a {@link androidx.recyclerview.widget.RecyclerView},
 * using {@link com.gaspar.learnjava.adapters.QuestionAdapter}.
 * @see com.gaspar.learnjava.curriculum.questions.TextQuestion
 * @see com.gaspar.learnjava.curriculum.questions.TrueOrFalseQuestion
 * @see com.gaspar.learnjava.curriculum.questions.MultiChoiceQuestion
 * @see com.gaspar.learnjava.curriculum.questions.SingleChoiceQuestion
 */
package com.gaspar.learnjava.curriculum.questions;