package com.gaspar.learnjava.curriculum.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.LogUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * This is the class for an interactive {@link CodeComponent} that can be placed inside chapters or tasks.
 * The interactive components consist of constant code and "empty spaces" ({@link EmptySpace}), where the user can enter
 * short code samples. Each space can have multiple correct answers.
 * <p>
 * A UI for checking if the answers are correct is provided. The user can also request for
 * the correct answers to be filled. Unlike normal code component, this one does not support copy and ClipSync.
 * <p>
 * An example of the XML syntax of this component (in real examples, the text will be formatted):
 * <pre>
 * {@code
 * <interactive instruction="Complete the variable declaration!">
 *     <data>
 *        String text ___ "hello" ___
 *     </data>
 *     <answer place="0">=</answer>
 *     <answer place="1">;</answer>
 *     <default place="0">*first edit text will be filled with this*</default> (optional)
 * </interactive>
 * }
 * </pre>
 * <p>
 * It's also possible to have empty spaces require other answers to be present to be acceptable:
 * <pre>
 * {@code
 * <interactive instruction="Complete the sample so that the variables final value is 8!">
 *     <data>
 *     <![CDATA[
 *         <font color="#DF7401">int</font> num = ___;
 *         <br/>num = num ___ 5;
 *     ]]>
 *     </data>
 *     <answer place="0" group="add" required_places="1">3</answer>
 *     <answer place="0" group="subtract" required_places="1">13</answer>
 *     <answer place="0" group="div" required_places="1">40</answer>
 *     <answer place="1" group="add" required_places="0">+</answer>
 *     <answer place="1" group="subtract" required_places="0">-</answer>
 *     <answer place="1" group="div" required_places="0">/</answer>
 * </interactive>
 * }
 * </pre>
 * Multiple required places are to be separated with a comma.
 * <p>
 * Each empty space can have a default answer as well. This must be placed into the DEFAULT
 * tag after the answer tags.
 */
public final class InteractiveComponent extends CodeComponent {

    /**
     * This string marks the location of empty spaces ({@link EmptySpace}) in the formatted
     * code, which is {@link #data}.
     */
    private static final String EMPTY_SPACE_MARKER = "___";

    /**
     * This string marks the line break in the formatted code, which is {@link #data}.
     */
    private static final String FORMATTED_LINE_BREAK = "<br/>";

    /**
     * The instructions given to the user.
     */
    @NonNull
    private final String instruction;

    /**
     * The list of spaces where the user can enter code.
     */
    @NonNull
    private final List<EmptySpace> emptySpaces;

    /**
     * Constructor for interactive component.
     * @param instruction The instructor displayed to the user.
     * @param data Data, which is formatted code, with empty space markers.
     * @param emptySpaces A list of empty spaces, these define which answers are acceptable.
     */
    public InteractiveComponent(@NonNull String instruction, @NonNull String data, @NonNull List<EmptySpace> emptySpaces) {
        super(ComponentType.INTERACTIVE, data);
        this.emptySpaces = emptySpaces;
        this.instruction = instruction;
    }

    /**
     * Creates the most important part of the interactive code sample, the combination of static code and
     * {@link EmptySpace}-es.
     * @param codeAreaInteractive The parent, in which all code and empty space objects are placed.
     */
    public void fillInteractiveCodeArea(@NonNull final LinearLayout codeAreaInteractive) {
        String[] dataLines = data.split(FORMATTED_LINE_BREAK);
        final Context context = codeAreaInteractive.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        int emptySpaceCounter = 0;
        EditText lastEditText = null; //last edit text will get special ime action (done)
        for(String dataLine: dataLines) { //split each line to constant texts (and implicitly, empty spaces)
            dataLine = dataLine.replace("\n", ""); //remove line break from the end...
            String[] separatedData = dataLine.split(EMPTY_SPACE_MARKER);
            //create layout for this line
            LinearLayout lineLayout = (LinearLayout)inflater.inflate(R.layout.view_interactive_line, codeAreaInteractive, false);
            for(int i=0; i<separatedData.length; i++) {
                if(!"".equals(separatedData[i])) { //only if there is actual text
                    TextView codeView = new TextView(context);
                    codeView.setText(Html.fromHtml(separatedData[i], Html.FROM_HTML_MODE_COMPACT));
                    codeView.setTextColor(context.getColor(R.color.code_text_color));
                    lineLayout.addView(codeView);
                }
                if(i < separatedData.length - 1) { //between every constant code part, add the empty space
                    lastEditText = addEmptySpaceView(emptySpaceCounter++, lineLayout, inflater, context);
                }
            }
            codeAreaInteractive.addView(lineLayout);
        }
        if(lastEditText!=null) lastEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    /**
     * Inserts a view where the user can enter input. This view also contains a help icon where
     * the user can ask for the correct solution.
     * @param place Used to get the matching {@link EmptySpace} object.
     * @param parent The layout where the view will be inserted.
     * @param inflater A layout inflater.
     * @param context Context.
     * @return The {@link EditText} of the view. This is used to set IME actions later.
     */
    private EditText addEmptySpaceView(int place, LinearLayout parent, final LayoutInflater inflater, @NonNull final Context context) {
        View emptySpaceView = inflater.inflate(R.layout.view_empty_space, parent, false);
        //bind to EmptySpace object, and add to line
        final EmptySpace emptySpace = emptySpaces.get(place);
        emptySpace.bindView(emptySpaceView);
        parent.addView(emptySpaceView);
        EditText input = emptySpaceView.findViewById(R.id.inputField); //there is a new last edit text
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emptySpaceView.findViewById(R.id.showSolutionButton).setOnClickListener(v -> emptySpace.showSolution(context));

        //show default text if there is one
        emptySpace.getDefaultText().ifPresent(input::setText);
        return input; //is "last edit text"
    }

    /**
     * Sets the zoom button listeners. This is more complex then the superclass method, as
     * there is more then just a text view now. All parameters can be found in {@link InteractiveComponentHolder}.
     * @param zoomIn The zoom in button.
     * @param zoomOut The zoom out button.
     * @param codeAreaInteractive The view which displays code. Unlike in the superclass method, this is
     *                            not just a simple text view.
     */
    public void initZoomButtons(@NonNull ImageButton zoomIn, @NonNull ImageButton zoomOut, @NonNull LinearLayout codeAreaInteractive) {
        final int minFontSize = (int)codeAreaInteractive.getContext().getResources().getDimension(R.dimen.code_text_size);
        zoomIn.setOnClickListener(v -> {
            int currentSize = getCurrentFontSize(codeAreaInteractive);
            final ValueAnimator animator = ValueAnimator.ofInt(currentSize, currentSize + ZOOM_SIZE_CHANGE);
            animator.setDuration(AnimationUtils.DURATION);
            //disable zoom buttons while ongoing
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    zoomIn.setEnabled(false);
                    zoomOut.setEnabled(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    zoomIn.setEnabled(true);
                    zoomOut.setEnabled(true);

                }
            });
            //update text size
            animator.addUpdateListener(pAnimator -> updateTextSizes(codeAreaInteractive, (int)pAnimator.getAnimatedValue()));
            animator.start();
        });
        zoomOut.setOnClickListener(v -> {
            int currentSize = getCurrentFontSize(codeAreaInteractive);
            int newSize = currentSize - ZOOM_SIZE_CHANGE;
            if(newSize < minFontSize) return;
            final ValueAnimator animator = ValueAnimator.ofInt(currentSize, currentSize - ZOOM_SIZE_CHANGE);
            animator.setDuration(AnimationUtils.DURATION);
            //disable zoom buttons while ongoing
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    zoomIn.setEnabled(false);
                    zoomOut.setEnabled(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    zoomIn.setEnabled(true);
                    zoomOut.setEnabled(true);

                }
            });
            //update text size
            animator.addUpdateListener(pAnimator -> updateTextSizes(codeAreaInteractive, (int)pAnimator.getAnimatedValue()));
            animator.start();
        });
    }

    /**
     * Finds the font size of the currently displayed code.
     * @param codeArea The area in which code is displayed.
     * @return The font size.
     */
    private int getCurrentFontSize(@NonNull final LinearLayout codeArea) {
        if(codeArea.getChildCount() == 0) {
            LogUtils.logError("Warning: interactive sample has no code!");
            return (int)codeArea.getContext().getResources().getDimension(R.dimen.code_text_size);
        }
        try {
            View child = codeArea.getChildAt(0);
            if(child instanceof TextView) {
                return  (int)((TextView) child).getTextSize();
            } else { //an edit text
                final EditText inputField = child.findViewById(R.id.inputField);
                return (int)inputField.getTextSize();
            }
        } catch (Exception e) {
            Snackbar.make(codeArea, R.string.code_component_fail, Snackbar.LENGTH_SHORT).show();
            LogUtils.logError("Exception while getting font size!", e);
            return (int)codeArea.getContext().getResources().getDimension(R.dimen.code_text_size);
        }
    }

    /**
     * Updates all view's font size property inside the code area.
     * @param codeArea The code area.
     * @param newSize The new font size.
     */
    private void updateTextSizes(@NonNull final LinearLayout codeArea, int newSize) {
        for(int i=0; i<codeArea.getChildCount(); i++) { //iterate all, change font size everywhere
            final LinearLayout lineLayout = (LinearLayout) codeArea.getChildAt(i);
            for(int j=0; j<lineLayout.getChildCount(); j++) {
                final View child = lineLayout.getChildAt(j);
                if(child instanceof TextView) {
                    ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                } else { //this is an empty space view
                    final EditText inputField = child.findViewById(R.id.inputField);
                    inputField.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                }
            }
        }
    }

    /**
     * Iterates all empty spaces and decides if the answer there is correct or not. If yes, recolors
     * it to green. If not, colors it red. Locks the edit texts.
     * @param context Context.
     * @param view A view in the hierarchy used to display {@link Snackbar}.
     */
    public void checkSolution(@NonNull final Context context, @NonNull final View view) {
        boolean allCorrect = true;
        for(EmptySpace emptySpace: emptySpaces) {
            boolean correctAnswer = emptySpace.checkSolution(context, emptySpaces);
            if(!correctAnswer) allCorrect = false;
        }
        Snackbar.make(view, allCorrect ? R.string.interactive_correct : R.string.interactive_incorrect, Snackbar.LENGTH_SHORT)
            .show();
    }
    
    /**
     * Empties all edit texts and removes coloring.
     * @param context Context.
     */
    public void reset(@NonNull final Context context) {
        for(EmptySpace emptySpace: emptySpaces) {
            emptySpace.resetEmptySpace(context);
        }
    }

    /**
     * @return The instruction of the interactive sample.
     */
    @NonNull
    public String getInstruction() {
        return instruction;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class InteractiveComponentHolder extends RecyclerView.ViewHolder {

        /**
         * A text view which display the instruction.
         */
        public TextView instructionTextView;

        /**
         * The view that displays the formatted interactive code.
         */
        public LinearLayout codeAreaInteractive;

        /**
         * Zoom in button.
         */
        public ImageButton zoomIn;

        /**
         * Zoom out button.
         */
        public ImageButton zoomOut;

        /**
         * Button that resets the progress.
         */
        public ImageButton resetButton;

        /**
         * Button that checks if the solution is correct.
         */
        public ImageButton checkButton;

        /**
         * Creates a view holder for a {@link InteractiveComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_interactive.
         */
        public InteractiveComponentHolder(View componentView) {
            super(componentView);
            instructionTextView = componentView.findViewById(R.id.instruction_view);
            codeAreaInteractive = componentView.findViewById(R.id.codeAreaInteractive);
            zoomIn = componentView.findViewById(R.id.zoomInButtonInteractive);
            zoomOut = componentView.findViewById(R.id.zoomOutButtonInteractive);
            resetButton = componentView.findViewById(R.id.resetButton);
            checkButton = componentView.findViewById(R.id.checkButton);
        }

    }
}
