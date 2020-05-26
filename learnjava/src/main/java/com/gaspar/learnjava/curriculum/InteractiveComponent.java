package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gaspar.learnjava.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class for an interactive_component code component that can be placed inside chapters or tasks.
 * The interactive_component components consist of constant code and "empty spaces", where the user can enter
 * short code samples. Each space can have multiple correct answers.
 * <p>
 * An interface for checking if the answers are correct is provided. The user can also request for
 * the correct answers to be filled.
 * <p>
 * An example of the XML syntax of this component (in real examples, the text will be formatted):
 * <code>
 *     <interactive instruction="Complete the variable declaration!">
 *         <data>
 *             String text ___ "hello" ___
 *         </data>
 *         <answer place="0">=</answer>
 *         <answer place="1">;</answer>
 *     </interactive>
 * </code>
 */
public final class InteractiveComponent extends Component {

    /**
     * The instructions given to the user.
     */
    private final String instruction;
    /**
     * The list of spaces where the user can enter code.
     */
    private List<EmptySpace> emptySpaces;

    public InteractiveComponent(String instruction, String data, List<EmptySpace> emptySpaces) {
        super(ComponentType.INTERACTIVE, data);
        this.emptySpaces = emptySpaces;
        this.instruction = instruction;
    }

    /**
     * Creates the interactive_component view, using the formatted data, and the empty spaces. Binds an
     * EditText to each empty space.
     *
     * @param context Context.
     * @param parent The view group this question view will be added. This method DOES NOT add the
     *               inflated view.
     * @return The created view ready to be added.
     */
    @Override
    @SuppressWarnings("deprecation") //says it's unused, but it isn't...
    public View createComponentView(Context context, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final String EMPTY_SPACE_MARKER = context.getString(R.string.empty_space_marker);
        final String FORMATTED_LINE_BREAK = context.getString(R.string.formatted_line_break);

        View interactiveView = inflater.inflate(R.layout.interactive_component, parent, false);
        ((TextView)interactiveView.findViewById(R.id.instruction_view)).setText(instruction);
        //split data into lines
        String[] dataLines = data.split(FORMATTED_LINE_BREAK);
        LinearLayout codeArea = interactiveView.findViewById(R.id.codeArea);
        int emptySpaceCounter = 0;
        EditText lastEditText = null; //last edit text will get special ime action (done)
        for(String dataLine: dataLines) { //split each line to constant texts (and implicitly, empty spaces)
            String[] separatedData = dataLine.split(EMPTY_SPACE_MARKER);
            boolean beginsWithEmptySpace = dataLine.startsWith(EMPTY_SPACE_MARKER);
            boolean endsWithEmptySpace = dataLine.endsWith(EMPTY_SPACE_MARKER);
            //create layout for this line
            LinearLayout lineLayout = (LinearLayout)inflater.inflate(R.layout.interactive_line, codeArea, false);
            if(beginsWithEmptySpace) lastEditText = addEmptySpaceView(emptySpaceCounter++, lineLayout, inflater, context);
            for(int i=0; i<separatedData.length; i++) {
                if("".equals(separatedData[i])) { //"fake" code at start or at end, skip
                    continue;
                }
                //real code, insert a text view
                separatedData[i] = separatedData[i].replace("\n", ""); //remove line break from the end...
                TextView codeView = new TextView(context);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    codeView.setText(Html.fromHtml(separatedData[i], Html.FROM_HTML_MODE_COMPACT));
                } else {
                    codeView.setText(Html.fromHtml(separatedData[i])); //deprecated but new android wont use this anyways
                }
                codeView.setTextColor(context.getResources().getColor(R.color.code_text_color));
                lineLayout.addView(codeView);
                if(i < separatedData.length - 1) { //between every constant code part, add the empty space
                   lastEditText = addEmptySpaceView(emptySpaceCounter++, lineLayout, inflater, context);
                }
            }
            if(endsWithEmptySpace) lastEditText = addEmptySpaceView(emptySpaceCounter++, lineLayout, inflater, context);
            codeArea.addView(lineLayout);
        }
        if(lastEditText!=null) lastEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //set button listeners
        initZoomButtons(interactiveView);
        interactiveView.findViewById(R.id.resetButton).setOnClickListener(v -> reset(context));
        interactiveView.findViewById(R.id.checkButton).setOnClickListener(v -> checkSolution(context));
        return interactiveView;
    }

    private EditText addEmptySpaceView(int place, LinearLayout parent, final LayoutInflater inflater, @NonNull final Context context) {
        View emptySpaceView = inflater.inflate(R.layout.empty_space_view, parent, false);
        //bind to EmptySpace object, and add to line
        final EmptySpace emptySpace = emptySpaces.get(place);
        emptySpace.bindView(emptySpaceView);
        parent.addView(emptySpaceView);
        EditText input = emptySpaceView.findViewById(R.id.inputField); //there is a new last edit text
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emptySpaceView.findViewById(R.id.showSolutionButton).setOnClickListener(v -> emptySpace.showSolution(context));
        return input; //is "last edit text"
    }

    @Override
    protected void initZoomButtons(@NonNull View codeSampleView) {
        ImageButton zoomIn = codeSampleView.findViewById(R.id.zoomInButton);
        ImageButton zoomOut = codeSampleView.findViewById(R.id.zoomOutButton);
        final int minFontSize = (int)codeSampleView.getContext().getResources().getDimension(R.dimen.code_text_size);
        final LinearLayout codeArea = codeSampleView.findViewById(R.id.codeArea);
        zoomIn.setOnClickListener(v -> {
            for(int i=0; i<codeArea.getChildCount(); i++) { //iterate all, change font size everywhere
                final LinearLayout lineLayout = (LinearLayout) codeArea.getChildAt(i);
                for(int j=0; j<lineLayout.getChildCount(); j++) {
                    final View child = lineLayout.getChildAt(j);
                    if(child instanceof TextView) {
                        ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                ((TextView) child).getTextSize() + ZOOM_SIZE_CHANGE);
                    } else { //this is an empty space view
                        final EditText inputField = child.findViewById(R.id.inputField);
                        inputField.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputField.getTextSize() + ZOOM_SIZE_CHANGE);
                    }
                }
            }
        });
        zoomOut.setOnClickListener(v -> {
            for(int i=0; i<codeArea.getChildCount(); i++) { //iterate all, change font size everywhere
                final LinearLayout lineLayout = (LinearLayout) codeArea.getChildAt(i);
                for(int j=0; j<lineLayout.getChildCount(); j++) {
                    final View child = lineLayout.getChildAt(j);
                    if(child instanceof TextView) {
                        float newSize = ((TextView) child).getTextSize() - ZOOM_SIZE_CHANGE;
                        if(newSize < minFontSize) return;
                        ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                    } else { //this is an empty space view
                        final EditText inputField = child.findViewById(R.id.inputField);
                        float newSize = inputField.getTextSize() - ZOOM_SIZE_CHANGE;
                        if(newSize < minFontSize) return;
                        inputField.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                    }
                }
            }
        });
    }

    /**
     * Iterates all empty spaces and decides if the answer there is correct or not. If yes, recolors
     * it to green. If not, colors it red. Locks the edit texts.
     */
    private void checkSolution(@NonNull final Context context) {
        for(EmptySpace emptySpace: emptySpaces) {
            emptySpace.checkSolution(context);
        }
    }
    /**
     * Empties all edit texts and removes coloring.
     */
    private void reset(@NonNull final Context context) {
        for(EmptySpace emptySpace: emptySpaces) {
            emptySpace.resetEmptySpace(context);
        }
    }
    /**
     * This class stores represents a spot where the user can enter input inside an interactive code sample.
     */
    public static class EmptySpace {

        private final int place; //the "index" of this empty space
        private final List<String> answers; //the answers accepted by this empty space
        private View emptySpaceView; //this is bound later, is created from "empty_space_view.xml"

        EmptySpace(int place, String answer) {
            this.place = place;
            this.answers = new ArrayList<>();
            answers.add(answer);
        }

        int getPlace() {
            return place;
        }

        public List<String> getAnswers() {
            return answers;
        }
        //called when the view for this empty space is being created.
        void bindView(final View view) {
            this.emptySpaceView = view;
        }

        //checks if the edit text has an acceptable answer
        private boolean isCorrect() {
            EditText input = emptySpaceView.findViewById(R.id.inputField);
            return answers.contains(input.getText().toString().trim());
        }
        //checks if the entered answer is correct (also disables input)
        void checkSolution(@NonNull final Context context) {
            if(emptySpaceView == null) throw new RuntimeException("View not bound!");
            final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
            inputField.setEnabled(false);
            if(isCorrect()) {
                inputField.setTextColor(context.getResources().getColor(android.R.color.black));
                inputField.setBackgroundResource(R.drawable.correct_answer_background);
            } else { //incorrect, show red background and help icon
                inputField.setTextColor(context.getResources().getColor(android.R.color.black));
                inputField.setBackgroundResource(R.drawable.incorrect_background);
                emptySpaceView.findViewById(R.id.showSolutionButton).setVisibility(View.VISIBLE);
            }
        }
        //clears text and colored background (also enables it)
        void resetEmptySpace(@NonNull final Context context) {
            if(emptySpaceView == null) throw new RuntimeException("View not bound!");
            final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
            inputField.setText("");
            //reset to original style
            inputField.setBackgroundResource(R.drawable.edit_text_line_background);
            inputField.setTextColor(context.getResources().getColor(R.color.code_text_color));
            inputField.setEnabled(true);
            emptySpaceView.findViewById(R.id.showSolutionButton).setVisibility(View.GONE);
        }
        //shows correct solution (the first one)
        void showSolution(@NonNull final Context context) {
            resetEmptySpace(context);
            final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
            inputField.setText(answers.get(0));
        }
        //appends a correct answer to this empty space's answer list
        void addAnswer(String answer) {
            answers.add(answer);
        }
    }

    /**
     * Can build a list of EmptySpace objects from XML answer tags.
     */
    public static class EmptySpaceListBuilder {

        private List<EmptySpace> emptySpaces;

        public EmptySpaceListBuilder() {
            emptySpaces = new ArrayList<>();
        }

        public void addEmptySpaceAnswer(int place, @NonNull String answer) {
            boolean wasPresent = false;
            for(EmptySpace emptySpace: emptySpaces) {
                if(emptySpace.getPlace() == place) { //there is an empty space with this place already
                    emptySpace.addAnswer(answer);
                    wasPresent = true;
                    break;
                }
            }
            if(!wasPresent) { //did not see this place before
                emptySpaces.add(new EmptySpace(place, answer));
            }
        }

        public List<EmptySpace> finishBuilding() {
            return emptySpaces;
        }
    }
}
