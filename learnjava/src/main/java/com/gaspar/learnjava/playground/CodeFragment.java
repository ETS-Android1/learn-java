package com.gaspar.learnjava.playground;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.asynctask.LearnJavaExecutor;
import com.gaspar.learnjava.asynctask.QueryPlaygroundFilesTask;
import com.gaspar.learnjava.asynctask.SavePlaygroundFilesTask;
import com.gaspar.learnjava.curriculum.components.CodeComponent;
import com.gaspar.learnjava.database.PlaygroundFile;
import com.gaspar.learnjava.formatter.Formatter;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static com.gaspar.learnjava.curriculum.components.CodeComponent.ZOOM_SIZE_CHANGE;

/**
 * A {@link Fragment} which displays the editable code for the user inside {@link PlaygroundActivity}.
 * Use the {@link CodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CodeFragment extends Fragment {

    /**
     * Name of the default {@link PlaygroundFile} which is always present in the code fragment.
     */
    private static final String MAIN_JAVA_FILE_NAME = "Main.java";

    /**
     * This regex matches conventional Java class names, simply by validating they begin with upper
     * case and contain only letters.
     */
    private static final Pattern CONVENTIONAL_CLASS_NAME_REGEX = Pattern.compile("[A-Z][a-zA-Z]*");

    /**
     * The amount of files the code fragment will allow to create.
     */
    private static final int MAX_FILES = 6;

    /**
     * Controls what is displayed inside the file selector {@link AppCompatSpinner}.
     */
    private ArrayAdapter<String> fileSelectorAdapter;

    /**
     * The {@link PlaygroundFile}s that this fragment knows of. This is loaded from the database on start,
     * and stored in the database when the {@link PlaygroundActivity} is stopped.
     */
    private List<PlaygroundFile> playgroundFiles;

    /**
     * Used to format raw code into {@link android.text.Spannable} that displays code in a nice way.
     */
    private Formatter formatter;

    /**
     * A text watcher which tracks changes in the code area and reformats the code and saves the changes.
     */
    private CodeAreaTextWatcher codeAreaTextWatcher;

    /**
     * Name of the file that is showing in the fragment.
     */
    private String currentDisplayedFileName;

    public CodeFragment() { } // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CodeFragment.
     */
    public static CodeFragment newInstance() {
        CodeFragment fragment = new CodeFragment();
        Bundle args = new Bundle();
        //can add arguments here
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            //can process arguments here
        }
        */
        formatter = new Formatter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View codeFragmentView = inflater.inflate(R.layout.fragment_code, container, false);
        //call the background task that queries saved code from database
        new QueryPlaygroundFilesTask().execute(this);
        //add button listeners
        ImageButton zoomInButton = codeFragmentView.findViewById(R.id.playgroundZoomInButton);
        ImageButton zoomOutButton = codeFragmentView.findViewById(R.id.playgroundZoomOutButton);
        EditText codeArea = codeFragmentView.findViewById(R.id.playgroundCodeArea);
        AppCompatSpinner spinner = codeFragmentView.findViewById(R.id.fileSelectorSpinner);

        zoomInButton.setOnClickListener(view -> playgroundZoomInOnClick(zoomInButton, zoomOutButton, codeArea));
        zoomOutButton.setOnClickListener(view -> playgroundZoomOutOnClick(zoomInButton, zoomOutButton, codeArea));
        ImageButton deleteButton = codeFragmentView.findViewById(R.id.playgroundDeleteButton);
        deleteButton.setOnClickListener(view -> playgroundDeleteOnClick(spinner));
        ImageButton copyButton = codeFragmentView.findViewById(R.id.playgroundCopyButton);
        copyButton.setOnClickListener(view -> playgroundCopyOnClick(codeArea));

        //send event to activity on focus change
        codeArea.setOnFocusChangeListener((view, isFocused) -> {
            PlaygroundActivity.ShowHideFab showHideFab = new PlaygroundActivity.ShowHideFab();
            showHideFab.show = !isFocused;
            EventBus.getDefault().post(showHideFab);
            if(isFocused) {
                view.postDelayed(() -> {
                    if(!view.isFocused()) {
                        view.requestFocus();
                    }
                },300);
            }
        });
        return codeFragmentView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(getView() == null) {
            LogUtils.logError("View was null when calling onStop! Failed to save code changes!");
            return;
        }
        //save files when stopping
        new SavePlaygroundFilesTask(playgroundFiles).execute(getView().getContext());
    }

    /**
     * This method is called after the background task loading the files have finished. Displays the list of
     * queried files in the code fragment. When this method is finished the code fragment is ready for use.
     * @param pf The list of {@link PlaygroundFile}s queried from the database.
     */
    public void displayPlaygroundFiles(@NonNull List<PlaygroundFile> pf) {
        playgroundFiles = pf; //save list for later use
        if(getView() == null) {
            LogUtils.logError("Fragment view was not created when calling displayPlaygroundFiles!");
            return;
        }
        if(playgroundFiles.isEmpty()) {
            //it's possible that the user starts this for the first time, there is nothing in the database. need a Main.java file in this case
            PlaygroundFile mainJavaFile = new PlaygroundFile(MAIN_JAVA_FILE_NAME, buildMainJavaTemplate());
            playgroundFiles.add(mainJavaFile);
        }
        //send files to activity
        EventBus.getDefault().post(playgroundFiles);
        //this will be passed to the adapter
        List<String> spinnerList = new ArrayList<>(playgroundFiles.size() + 1);
        for(PlaygroundFile playgroundFile: playgroundFiles) {
            spinnerList.add(playgroundFile.getFileName());
        }
        String newFileString = getString(R.string.playground_new_file);
        spinnerList.add(newFileString);
        //create adapter and set
        fileSelectorAdapter = new ArrayAdapter<>(this.getContext(), R.layout.view_file_selector_element, spinnerList);
        AppCompatSpinner fileSelectorSpinner = getView().findViewById(R.id.fileSelectorSpinner);
        fileSelectorSpinner.setAdapter(fileSelectorAdapter);
        //add listener
        FileItemSelectedListener listener = new FileItemSelectedListener(this);
        fileSelectorSpinner.setOnItemSelectedListener(listener);
        fileSelectorSpinner.setOnTouchListener(listener);
        //set up code area listener
        final EditText codeArea = getView().findViewById(R.id.playgroundCodeArea);
        codeAreaTextWatcher = new CodeAreaTextWatcher(this, codeArea);
        codeArea.addTextChangedListener(codeAreaTextWatcher);
        //spinner filled and set up, start on Main.java
        switchToPlaygroundFile(MAIN_JAVA_FILE_NAME);
    }

    /**
     * Creates a new file that is added to the code fragment. A dialog is displayed to the user to get
     * the new file's name. It'll then be added to {@link #playgroundFiles} and the spinner. It will
     * also be default selected.
     */
    public void createNewPlaygroundFile() {
        if(getView() == null) {
            LogUtils.logError("Fragment view was not created when calling createNewPlaygroundFile!");
            return;
        }
        final AppCompatSpinner spinner = getView().findViewById(R.id.fileSelectorSpinner);
        //would we go over max file count with this new file?
        if(playgroundFiles.size() < MAX_FILES) { //no, still can create more files
            //inflate dialogs view (in this case null is ok, this is a dialog root)
            @SuppressLint("InflateParams")
            final View dialogView = LayoutInflater.from(getView().getContext()).inflate(R.layout.dialog_new_playground_file, null, false);
            final EditText fileNameInput = dialogView.findViewById(R.id.fileNameInput);
            final TextView incorrectView = dialogView.findViewById(R.id.fileNameIncorrectView);
            fileNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    incorrectView.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });
            //build the dialog
            final AlertDialog alertDialog = new MaterialAlertDialogBuilder(getView().getContext(), ThemeUtils.getThemedDialogStyle())
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {}) //will be overriding it later, so it won't close the dialog
                    .setOnCancelListener(dialogInterface -> {
                        //cancelled, go back go what was originally shown on the spinner
                        //the name of the file currently displayed. this is NOT what is shown by the spinner, the spinner is showing "create new..."
                        int pos = fileSelectorAdapter.getPosition(currentDisplayedFileName);
                        spinner.setSelection(pos);
                    })
                    .create();
            //as soon as it is showing, override positive button
            alertDialog.setOnShowListener(dialogInterface ->
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        String input = fileNameInput.getText().toString();
                        if(CONVENTIONAL_CLASS_NAME_REGEX.matcher(input).matches()) {
                            //good file name
                            String defaultContent = buildDefaultClassTemplate(input);
                            String javaFileExtension = getView().getContext().getString(R.string.java_file_extension);
                            String fileName = input + javaFileExtension;
                            //create new file
                            PlaygroundFile newFile = new PlaygroundFile(fileName, defaultContent);
                            playgroundFiles.add(newFile);
                            //show it
                            switchToPlaygroundFile(fileName);
                            //add the new file to the spinner and show it
                            addNewFileToSpinner(fileName);
                            alertDialog.dismiss();
                        } else {
                            //not a good file name
                            incorrectView.setVisibility(View.VISIBLE);
                        }
                    })
            );
            alertDialog.show();
        } else { //yes, this would be too many files
            new MaterialAlertDialogBuilder(getView().getContext(), ThemeUtils.getThemedDialogStyle())
                    .setMessage(R.string.playground_too_many_files)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        //go back go what was originally shown on the spinner
                        //the name of the file currently displayed. this is NOT what is shown by the spinner, the spinner is showing "create new..."
                        int pos = fileSelectorAdapter.getPosition(currentDisplayedFileName);
                        spinner.setSelection(pos);
                    })
                    .setOnCancelListener(dialogInterface -> {
                        //cancelled, go back go what was originally shown on the spinner
                        //the name of the file currently displayed. this is NOT what is shown by the spinner, the spinner is showing "create new..."
                        int pos = fileSelectorAdapter.getPosition(currentDisplayedFileName);
                        spinner.setSelection(pos);
                    })
                    .show();
        }
    }

    /**
     * Adds a new file to the file selector spinner, and jumps to that value. The new file will be
     * inserted at the end, but before the 'Create new...' item.
     * @param fileName The file name.
     */
    private void addNewFileToSpinner(String fileName) {
        fileSelectorAdapter.insert(fileName, fileSelectorAdapter.getCount() - 1);
    }

    /**
     * Updates this fragment to display the contents of an existing {@link PlaygroundFile}. It is expected
     * that this file is present in {@link #playgroundFiles}.
     * @param fileName The name of the file.
     */
    public void switchToPlaygroundFile(@NonNull String fileName) {
        LogUtils.log("Switching to playground file: " + fileName);
        PlaygroundFile selectedFile = null;
        //find this file object
        for(PlaygroundFile playgroundFile: playgroundFiles) {
            if(playgroundFile.getFileName().equals(fileName)) {
                selectedFile = playgroundFile;
                break;
            }
        }
        if(selectedFile == null) {
            LogUtils.logError("Selected file is not present in the file list!");
            return;
        }
        if(getView() == null) {
            LogUtils.logError("Fragment view was not created when calling switchToPlaygroundFile!");
            return;
        }
        currentDisplayedFileName = fileName;
        //content needs to be formatted first
        String formattedCode = formatter.formatContent(selectedFile.getContent());
        TextView codeArea = getView().findViewById(R.id.playgroundCodeArea);
        codeArea.removeTextChangedListener(codeAreaTextWatcher);
        codeArea.setText(Html.fromHtml(formattedCode, Html.FROM_HTML_MODE_COMPACT));
        //must update the code area listener to track the current playground file
        codeAreaTextWatcher.setPlaygroundFile(selectedFile);
        codeArea.addTextChangedListener(codeAreaTextWatcher);
    }

    /**
     * Called when the zoom in button is clicked, increases code area text size. This is static so other
     * fragments can access it too.
     * @param zoomInButton The zoom in button.
     * @param zoomOutButton The zoom out button.
     * @param codeArea The edit text whose text size changes.
     */
    public static void playgroundZoomInOnClick(@NonNull final View zoomInButton, @NonNull final View zoomOutButton, @NonNull final TextView codeArea) {
        int currentSize = (int)codeArea.getTextSize();
        final ValueAnimator animator = ValueAnimator.ofInt(currentSize, currentSize + ZOOM_SIZE_CHANGE);
        animator.setDuration(AnimationUtils.DURATION);
        //disable zoom buttons while ongoing
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                zoomInButton.setEnabled(true);
                zoomOutButton.setEnabled(true);

            }
        });
        //update text size
        animator.addUpdateListener(pAnimator -> codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)pAnimator.getAnimatedValue()));
        animator.start();
    }

    /**
     * Called when the zoom out button is clicked, decreases code area text size. This is static so other
     * fragments can access it too.
     * @param zoomInButton The zoom in button.
     * @param zoomOutButton The zoom out button.
     * @param codeArea The edit text whose text size changes.
     */
    public static void playgroundZoomOutOnClick(@NonNull final View zoomInButton, @NonNull final View zoomOutButton, @NonNull final TextView codeArea) {
        int currentSize = (int)codeArea.getTextSize();
        int newSize = currentSize - ZOOM_SIZE_CHANGE;
        if(newSize <= 0) return;
        final ValueAnimator animator = ValueAnimator.ofInt(currentSize, newSize);
        animator.setDuration(AnimationUtils.DURATION);
        //disable zoom buttons while ongoing
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                zoomInButton.setEnabled(true);
                zoomOutButton.setEnabled(true);

            }
        });
        //update text size
        animator.addUpdateListener(pAnimator -> codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)pAnimator.getAnimatedValue()));
        animator.start();
    }

    /**
     * Called when the code sample delete button is clicked. For Main.java, it resets the contents to
     * the default, for other playground files, they will get deleted.
     * @param spinner The file selector spinner.
     */
    private void playgroundDeleteOnClick(@NonNull final AppCompatSpinner spinner) {
        if(currentDisplayedFileName == null || currentDisplayedFileName.isEmpty()) { //this can probably happen if the user clicks on the button before the files are loaded
            LogUtils.logError("No selected file when clicking delete!");
            return;
        }
        int position = 0;
        for(PlaygroundFile playgroundFile: playgroundFiles) {
            if(playgroundFile.getFileName().equals(currentDisplayedFileName)) {
                break;
            }
            position++;
        }
        final int fixedPosition = position;
        if(currentDisplayedFileName.equals(MAIN_JAVA_FILE_NAME)) {
            //Main.java needs to reset
            new MaterialAlertDialogBuilder(spinner.getContext(), ThemeUtils.getThemedDialogStyle())
                    .setMessage(R.string.playground_delete_main)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        //confirmed, replace
                        playgroundFiles.get(fixedPosition).setContent(buildMainJavaTemplate());
                        switchToPlaygroundFile(MAIN_JAVA_FILE_NAME); //this will reformat it
                        Snackbar.make(spinner, R.string.playground_main_reset, Snackbar.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        } else {
            new MaterialAlertDialogBuilder(spinner.getContext(), ThemeUtils.getThemedDialogStyle())
                    .setMessage(R.string.playground_delete_normal_file)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        //delete from list
                        playgroundFiles.remove(fixedPosition);
                        //delete from spinner
                        fileSelectorAdapter.remove(currentDisplayedFileName);
                        fileSelectorAdapter.notifyDataSetChanged();
                        //move to main
                        switchToPlaygroundFile(MAIN_JAVA_FILE_NAME);
                        //set spinner to main as well
                        int spinnerPos = fileSelectorAdapter.getPosition(MAIN_JAVA_FILE_NAME);
                        spinner.setSelection(spinnerPos);
                        Snackbar.make(spinner, R.string.playground_file_deleted, Snackbar.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
    }

    /**
     * Called when the copy/CLipSync button is clicked. The behaviour depends on the ClipSync method selected.
     * @param codeArea The code area, whose contents will be copied.
     */
    private void playgroundCopyOnClick(@NonNull final EditText codeArea) {
        /*
        ClipSync related functions are implemented in the parent activity, PlaygroundActivity (it implements
        CodeHostingActivity. So first the parent activity must be accessed.
         */
        PlaygroundActivity playgroundActivity = (PlaygroundActivity)getActivity();
        if(playgroundActivity == null) {
            LogUtils.logError("The parent activity was null, failed to copy/share code!");
            return;
        }
        //create a code component object, to access its useful method, copyOnClick
        CodeComponent codeComponent = new CodeComponent(""); //actual data is not important here
        //call method, rest is handled by parent activity
        codeComponent.copyOnClick(codeArea, playgroundActivity);
    }

    /**
     * Creates a string that is used as the default content of Main.java file. I tried to extract this
     * to the string resources, but the formatting and special characters are very hard to manage there.
     * @return The content of Main.java
     */
    private static String buildMainJavaTemplate() {
        return "public class Main {\n" +
                "\n" +
                "   public static void main(String[] args) {\n" +
                "      System.out.println(\"hello!\");\n" +
                "   }\n" +
                "\n" +
                "}";
    }

    /**
     * Creates a string that is used as the default content of a new file. I tried to extract this
     * to the string resources, but the formatting and special characters are very hard to manage there.
     * @param fileName Name of the file.
     * @return The content of the new file.
     */
    private static String buildDefaultClassTemplate(@NonNull String fileName) {
        return "public class " + fileName + " {\n" +
                "\n" +
                "}";
    }

    /**
     * A listener that can differentiate between items selected from code and by user.
     */
    static class FileItemSelectedListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        /**
         * Code fragment.
         */
        private final CodeFragment codeFragment;

        /**
         * If the user touched the spinner. Only in cases when it was touched will an event be fired.
         */
        private boolean touched;

        /**
         * Create a listener.
         * @param codeFragment Code fragment.
         */
        public FileItemSelectedListener(CodeFragment codeFragment) {
            this.codeFragment = codeFragment;
            touched = false;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            touched = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if(touched) {
                LogUtils.log("Selected from file chooser spinner at position " + position);
                String selected = codeFragment.fileSelectorAdapter.getItem(position);
                if(position == codeFragment.fileSelectorAdapter.getCount() - 1) {
                    //the create new file was selected
                    LogUtils.log("Selected NEW FILE from file chooser spinner!");
                    codeFragment.createNewPlaygroundFile();
                } else {
                    //an existing file was selected
                    codeFragment.switchToPlaygroundFile(selected);
                }
                touched = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }

    }

    /**
     * A text watcher implementation that is attached to the code area, editable by the user. Formats the
     * content and updates the currently selected {@link PlaygroundFile}. The formatting only happens after the
     * user "stopped typing", meaning the edit text did not detect text change in the last {@link #CODE_FORMAT_INTERVAL} milliseconds.
     */
    static class CodeAreaTextWatcher implements TextWatcher {

        /**
         * Stores in milliseconds how much time must pass between two code formatting. This is to
         * prevent overwhelming the app when the user for example keeps the delete button held down.
         */
        private static final int CODE_FORMAT_INTERVAL = 1000;

        /**
         * The code area.
         */
        private final EditText codeArea;

        /**
         * The currently selected playground file that the user is editing.
         */
        private PlaygroundFile playgroundFile;

        /**
         * Formatter used to dynamically format the code samples.
         */
        private final Formatter formatter;

        /**
         * Used in measuring when the user stopped typing.
         */
        private Timer timer;

        /**
         * The code fragment that is watched by the text watcher.
         */
        private final CodeFragment codeFragment;

        /**
         * Detects if the user only typed spaces. No reformatting is needed in this case.
         */
        private boolean onlySpace;

        /**
         * Stores the position of the selector every time the text is updated.
         */
        private int typingStartPosition;

        /**
         * Stores if the user is currently typing.
         */
        private boolean typingStarted;

        /**
         * Stores how much the selector moved from {@link #typingStartPosition}.
         */
        private int selectionOffset;

        /**
         * Create a code are text watcher.
         * @param codeFragment The code fragment that is watched by the text watcher.
         * @param codeArea The edit text of the code area.
         */
        public CodeAreaTextWatcher(@NonNull CodeFragment codeFragment, @NonNull EditText codeArea) {
            this.codeArea = codeArea;
            this.codeFragment = codeFragment;
            formatter = new Formatter();
            playgroundFile = null;
            timer = null;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            if(!typingStarted) {
                typingStartPosition = codeArea.getSelectionStart();
                typingStarted = true;
            }
            //"after" is the amount of characters added (if characters are deleted, it is 0)
            //"count" is the amount of characters removed (if characters are added, it is 0)
            if(after > 0) {
                //characters were added
                selectionOffset += after;
            } else {
                //characters were removed
                selectionOffset -= count;
            }

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            //cancel timer, a new one will be started
            if(timer != null) timer.cancel();
            //check for only space
            onlySpace = true;
            for(int i = start; i < start + count; i++) {
                if(charSequence.charAt(i) != ' ') {
                    onlySpace = false;
                    break;
                }
            }
        }

        //it is allowed to make modifications to the edit text content here
        @Override
        public void afterTextChanged(Editable editable) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LearnJavaExecutor.getInstance().executeOnUiThread(() -> {
                        if(playgroundFile != null) playgroundFile.setContent(codeArea.getText().toString());
                        //format code, this updates playground file list
                        formatCodeArea(codeArea.getText());
                        codeArea.post(() -> {
                            //focus
                            codeArea.requestFocus();
                            InputMethodManager imm = (InputMethodManager) codeArea.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                            if(imm != null) imm.showSoftInput(codeArea, 0);
                            //reapply selection
                            LogUtils.log("Typing started at position: " + typingStartPosition);
                            LogUtils.log("Typing offset: " + selectionOffset);
                            int typingEndPosition = typingStartPosition + selectionOffset;
                            LogUtils.log("Typing ended at position: " + typingEndPosition);
                            codeArea.setSelection(Math.max(0, typingEndPosition));
                            selectionOffset = 0;
                            typingStarted = false;
                        });
                        //send updated files to activity
                        EventBus.getDefault().post(codeFragment.playgroundFiles);
                    });
                }
            }, CODE_FORMAT_INTERVAL);
        }

        /**
         * Gets whatever is typed into the code area and dynamically formats it.
         * The contents of {@link #playgroundFile} is updated to reflect the changes.
         * @param editable The editable from the code area.
         */
        private void formatCodeArea(@NonNull Editable editable) {
            if(onlySpace) { //no need to format only space input
                return;
            }
            //do not trigger callback during replacement
            codeArea.removeTextChangedListener(this);
            String unformatted = editable.toString();
            LogUtils.log("\n"+unformatted);
            //set new text
            String formatted = formatter.formatContent(unformatted);
            //codeArea.getText().clear();
            codeArea.setText(Html.fromHtml(formatted, Html.FROM_HTML_MODE_COMPACT));
            String afterFormatting = codeArea.getText().toString();
            LogUtils.log("\n"+afterFormatting);
            //reattach listener
            codeArea.addTextChangedListener(this);
        }

        /**
         * Update the currently selected playground file. This must be called when the user changes to
         * a new file, so that the text watcher will update the correct file.
         * @param playgroundFile The new file.
         */
        public void setPlaygroundFile(PlaygroundFile playgroundFile) {
            this.playgroundFile = playgroundFile;
        }
    }
}