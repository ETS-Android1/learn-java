package com.gaspar.learnjava.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.questions.MultiChoiceQuestion;
import com.gaspar.learnjava.curriculum.questions.Question;
import com.gaspar.learnjava.curriculum.questions.SingleChoiceQuestion;
import com.gaspar.learnjava.curriculum.questions.TextQuestion;
import com.gaspar.learnjava.curriculum.questions.TrueOrFalseQuestion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for {@link Question} objects, similarly to {@link ComponentAdapter}, which is for {@link com.gaspar.learnjava.curriculum.components.Component}s.
 * <p>
 * Each {@link Question} subclass defines its own view holder, and these holders are used in the adapter.
 * {@link Question.QuestionType} is used to define the view types.
 */
public class QuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * List of {@link Question}s that this adapter manages.
     */
    @NonNull
    private final List<Question> questions;

    /**
     * An animation used by certain questions.
     */
    private final Animation tickAnimation;

    /**
     * Creates a question adapter.
     * @param questions List of {@link Question}s to be displayed by the adapter.
     * @param context Context.
     */
    public QuestionAdapter(@NonNull List<Question> questions, @NonNull final Context context) {
        this.questions = questions;
        //loaded here, only once
        tickAnimation = AnimationUtils.loadAnimation(context, R.anim.tick);
    }

    /**
     * Finds the view type of the element, by position.
     * @param position The position.
     * @return The view type, on of {@link Question.QuestionType} constants.
     */
    @Override
    @Question.QuestionType
    public int getItemViewType(int position) {
        return questions.get(position).getType();
    }

    /**
     * Creates and initializes view holders based on the view type.
     * @param parent The parent which will display the view.
     * @param viewType The type of the view.
     * @return A {@link RecyclerView.ViewHolder} for the view.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Question.QuestionType int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Question.QuestionType.MULTI_CHOICE:
                view = inflater.inflate(R.layout.question_multi_choice, parent, false);
                holder = new MultiChoiceQuestion.MultiChoiceHolder(view);
                break;
            case Question.QuestionType.SINGLE_CHOICE:
                view = inflater.inflate(R.layout.question_single_choice, parent, false);
                holder = new SingleChoiceQuestion.SingleChoiceHolder(view);
                break;
            case Question.QuestionType.TEXT:
                view = inflater.inflate(R.layout.question_text, parent, false);
                holder = new TextQuestion.TextQuestionHolder(view);
                break;
            case Question.QuestionType.TRUE_OR_FALSE:
                view = inflater.inflate(R.layout.question_true_false, parent, false);
                holder = new TrueOrFalseQuestion.TrueFalseHolder(view);
                break;
            default: //should not happen
                throw new RuntimeException("Unknown question type: " + viewType);
        }
        return holder;
    }

    /**
     * Fills the view holder's cached views with the correct data.
     * @param holder The view holder, created in {@link #createViewHolder(ViewGroup, int)}.
     * @param position The position of the view holder.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        @Question.QuestionType int viewType = getItemViewType(position);
        switch (viewType) {
            case Question.QuestionType.MULTI_CHOICE:
                MultiChoiceQuestion.MultiChoiceHolder multiChoiceHolder = (MultiChoiceQuestion.MultiChoiceHolder)holder;
                MultiChoiceQuestion multiChoiceQuestion = (MultiChoiceQuestion)questions.get(position);
                displayMultiChoiceQuestion(multiChoiceHolder, multiChoiceQuestion);
                break;
            case Question.QuestionType.SINGLE_CHOICE:
                SingleChoiceQuestion.SingleChoiceHolder singleChoiceHolder = (SingleChoiceQuestion.SingleChoiceHolder)holder;
                SingleChoiceQuestion singleChoiceQuestion = (SingleChoiceQuestion)questions.get(position);
                displaySingleChoiceQuestion(singleChoiceHolder, singleChoiceQuestion);
                break;
            case Question.QuestionType.TEXT:
                TextQuestion.TextQuestionHolder textQuestionHolder = (TextQuestion.TextQuestionHolder)holder;
                TextQuestion textQuestion = (TextQuestion)questions.get(position);
                displayTextQuestion(textQuestionHolder, textQuestion);
                break;
            case Question.QuestionType.TRUE_OR_FALSE:
                TrueOrFalseQuestion.TrueFalseHolder trueFalseHolder = (TrueOrFalseQuestion.TrueFalseHolder)holder;
                TrueOrFalseQuestion trueOrFalseQuestion = (TrueOrFalseQuestion)questions.get(position);
                displayTrueFalseQuestion(trueFalseHolder, trueOrFalseQuestion);
                break;
            default: //should not happen
                throw new RuntimeException("Unknown question type: " + viewType);
        }
    }

    /**
     * Displays the properties of a {@link MultiChoiceQuestion} to the cached views in its holder.
     * @param multiChoiceHolder The view holder.
     * @param multiChoiceQuestion The question.
     */
    private void displayMultiChoiceQuestion(@NonNull final MultiChoiceQuestion.MultiChoiceHolder multiChoiceHolder, @NonNull MultiChoiceQuestion multiChoiceQuestion) {
        //set text
        multiChoiceHolder.questionTextView.setText(multiChoiceQuestion.getText());
        //create answers
        multiChoiceQuestion.fillAnswersLayout(multiChoiceHolder.answersLayout);
        //get selected answers from question and check them
        for(int i=0; i<multiChoiceHolder.answersLayout.getChildCount(); i++) {
            CheckBox answer = (CheckBox)multiChoiceHolder.answersLayout.getChildAt(i);
            answer.setOnCheckedChangeListener(null);
            //set the saved selected answers as checked
            answer.setChecked(multiChoiceQuestion.getSelectedAnswerIndices().contains(i));
            //set listeners to each answer
            final int fixedI = i;
            answer.setOnCheckedChangeListener((compoundButton,isChecked) -> {
                if(isChecked) { //remove or add to set depending on checked state
                    multiChoiceQuestion.getSelectedAnswerIndices().add(fixedI);
                } else {
                    multiChoiceQuestion.getSelectedAnswerIndices().remove(fixedI);
                }
            });
        }
        //should this question show it's answer?
        if(multiChoiceQuestion.isDisplayAnswer()) {
            multiChoiceQuestion.lockQuestion(multiChoiceHolder.answersLayout);
            multiChoiceQuestion.showCorrectAnswer(multiChoiceHolder.questionIcon, multiChoiceHolder.answersLayout);
        }
    }

    /**
     * Displays the properties of a {@link SingleChoiceQuestion} to the cached views in its holder.
     * @param singleChoiceHolder The view holder.
     * @param singleChoiceQuestion The question.
     */
    private void displaySingleChoiceQuestion(@NonNull final SingleChoiceQuestion.SingleChoiceHolder singleChoiceHolder, @NonNull SingleChoiceQuestion singleChoiceQuestion) {
        //set text
        singleChoiceHolder.questionTextView.setText(singleChoiceQuestion.getText());
        //create answers, if they dont exist already
        singleChoiceQuestion.fillAnswersLayout(singleChoiceHolder.answersLayout);
        //LogUtils.logError("Displaying single choice question: " + singleChoiceQuestion.getText());
        //LogUtils.logError("Selected answer is: " + singleChoiceQuestion.getSelectedAnswerIndex());
        for(int i=0; i<singleChoiceHolder.answersLayout.getChildCount(); i++) {
            RadioButton answer = (RadioButton)singleChoiceHolder.answersLayout.getChildAt(i);
            answer.setOnCheckedChangeListener(null);
            //is this saved as selected?
            if(i == singleChoiceQuestion.getSelectedAnswerIndex()) { //simple setChecked DOES NOT WORK!!!!
                singleChoiceHolder.answersLayout.check(singleChoiceHolder.answersLayout.getChildAt(i).getId());
            }
            //set listener
            final int fixedI = i;
            answer.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if(isChecked) singleChoiceQuestion.setSelectedAnswerIndex(fixedI);
            });
        }
        //should this question show it's answer?
        if(singleChoiceQuestion.isDisplayAnswer()) {
            singleChoiceQuestion.lockQuestion(singleChoiceHolder.answersLayout);
            singleChoiceQuestion.showCorrectAnswer(singleChoiceHolder.questionIcon, singleChoiceHolder.answersLayout);
        }
    }

    /**
     * A utility class that can remove text watcher from edit text.
     * @see <a href="https://stackoverflow.com/a/64795488/4925616">Source: stackoverflow answer</a>
     */
    static class ReflectionTextWatcher {

        @SuppressWarnings("unchecked")
        public static void removeAll(EditText editText) {
            try {
                Field field = findField("mListeners", editText.getClass());
                if (field != null) {
                    field.setAccessible(true);
                    ArrayList<TextWatcher> list = (ArrayList<TextWatcher>) field.get(editText); //IllegalAccessException
                    if (list != null) {
                        list.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private static Field findField(String name, Class<?> type) {
            for (Field declaredField : type.getDeclaredFields()) {
                if (declaredField.getName().equals(name)) {
                    return declaredField;
                }
            }
            if (type.getSuperclass() != null) {
                return findField(name, type.getSuperclass());
            }
            return null;
        }
    }

    /**
     * Displays the properties of a {@link TextQuestion} to the cached views in its holder.
     * @param textQuestionHolder The view holder.
     * @param textQuestion The question.
     */
    private void displayTextQuestion(@NonNull final TextQuestion.TextQuestionHolder textQuestionHolder, @NonNull TextQuestion textQuestion) {
        //set text
        textQuestionHolder.questionTextView.setText(textQuestion.getText());
        ReflectionTextWatcher.removeAll(textQuestionHolder.answerEditText);
        //get answer
        textQuestionHolder.answerEditText.setText(textQuestion.getEnteredAnswer());
        //set listener
        textQuestionHolder.answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //save updated answer to question
                textQuestion.setEnteredAnswer(charSequence.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        //should this question show it's answer?
        if(textQuestion.isDisplayAnswer()) {
            textQuestion.lockQuestion(textQuestionHolder.answerEditText);
            textQuestion.showCorrectAnswer(textQuestionHolder);
        }
    }

    /**
     * Displays the properties of a {@link TrueOrFalseQuestion} to the cached views in its holder.
     * @param trueFalseHolder The view holder.
     * @param trueOrFalseQuestion The question.
     */
    private void displayTrueFalseQuestion(@NonNull final TrueOrFalseQuestion.TrueFalseHolder trueFalseHolder, @NonNull TrueOrFalseQuestion trueOrFalseQuestion) {
        //set text
        trueFalseHolder.questionTextView.setText(trueOrFalseQuestion.getText());
        //set current button
        if(trueOrFalseQuestion.getSelectedValue() == TrueOrFalseQuestion.SelectedValues.TRUE) {
            trueFalseHolder.trueTextView.setBackgroundResource(R.drawable.selected_background);
            trueFalseHolder.falseTextView.setBackgroundResource(R.drawable.unselected_background);
        } else if(trueOrFalseQuestion.getSelectedValue() == TrueOrFalseQuestion.SelectedValues.FALSE) {
            trueFalseHolder.falseTextView.setBackgroundResource(R.drawable.selected_background);
            trueFalseHolder.trueTextView.setBackgroundResource(R.drawable.unselected_background);
        } else {
            trueFalseHolder.falseTextView.setBackgroundResource(R.drawable.unselected_background);
            trueFalseHolder.trueTextView.setBackgroundResource(R.drawable.unselected_background);
        }
        //set listeners
        trueFalseHolder.trueTextView.setOnClickListener((view) -> {
            trueOrFalseQuestion.setSelectedValue(TrueOrFalseQuestion.SelectedValues.TRUE);
            trueFalseHolder.trueTextView.setBackgroundResource(R.drawable.selected_background);
            trueFalseHolder.falseTextView.setBackgroundResource(R.drawable.unselected_background);
            trueFalseHolder.trueTextView.startAnimation(tickAnimation);
        });
        trueFalseHolder.falseTextView.setOnClickListener((view) -> {
            trueOrFalseQuestion.setSelectedValue(TrueOrFalseQuestion.SelectedValues.FALSE);
            trueFalseHolder.falseTextView.setBackgroundResource(R.drawable.selected_background);
            trueFalseHolder.trueTextView.setBackgroundResource(R.drawable.unselected_background);
            trueFalseHolder.falseTextView.startAnimation(tickAnimation);
        });
        //should this question show it's answer?
        if(trueOrFalseQuestion.isDisplayAnswer()) {
            trueOrFalseQuestion.lockQuestion(trueFalseHolder.trueTextView, trueFalseHolder.falseTextView);
            trueOrFalseQuestion.showCorrectAnswer(trueFalseHolder.trueTextView, trueFalseHolder.falseTextView, trueFalseHolder.questionIcon);
        }
    }

    /**
     * @return The amount of items the adapter manages, which is the amount of questions.
     */
    @Override
    public int getItemCount() {
        return questions.size();
    }
}
