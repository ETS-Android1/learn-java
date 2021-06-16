package com.gaspar.learnjava.adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.components.AdvancedComponent;
import com.gaspar.learnjava.curriculum.components.BoxedComponent;
import com.gaspar.learnjava.curriculum.components.CodeComponent;
import com.gaspar.learnjava.curriculum.components.Component;
import com.gaspar.learnjava.curriculum.components.ImageComponent;
import com.gaspar.learnjava.curriculum.components.InteractiveComponent;
import com.gaspar.learnjava.curriculum.components.TextComponent;
import com.gaspar.learnjava.curriculum.components.TitleComponent;
import com.gaspar.learnjava.utils.ListTagHandler;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * An adapter for a {@link RecyclerView} which can display all types of {@link Component} views.
 * Used in {@link com.gaspar.learnjava.ChapterActivity} and {@link com.gaspar.learnjava.TaskActivity} to display components efficiently.
 * <p>
 * This adapter supports multiple view types, one for each component. The {@link Component.ComponentType} is
 * used to tell the view type, or the {@link #FOOTER_VIEW_TYPE}, in case of a footer.
 * <p>
 * Each {@link Component} subclass provides a view holder which extends {@link RecyclerView.ViewHolder}, and
 * these are used in the adapter.
 * <p>
 * It's possible to specify a version of this which inserts a 'footer' at the end of the recycler view,
 * for example a button. For this, use the {@link #ComponentAdapter(List, AppCompatActivity, Function, BiConsumer)} constructor.
 */
public class ComponentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * View type of the footer. This must not match any of the {@link com.gaspar.learnjava.curriculum.components.Component.ComponentType} constants.
     */
    private static final int FOOTER_VIEW_TYPE = -1;

    /**
     * {@link Component} objects which are used to create the views in the {@link RecyclerView}. This is extracted from either
     * a {@link com.gaspar.learnjava.curriculum.Chapter} or a {@link com.gaspar.learnjava.curriculum.Task} after their components
     * have been parsed.
     */
    @NonNull
    private final List<Component> components;

    /**
     * An activity in which this adapter displays its contents.
     */
    @NonNull
    private final AppCompatActivity activity;

    /**
     * Used by the footer view to get the recycler view which belong to it. Basically a version of {@link #createViewHolder(ViewGroup, int)}.
     */
    @Nullable
    private final Function<ViewGroup, RecyclerView.ViewHolder> footerViewHolderGenerator;

    /**
     * Used by the footer view to bind its data to the view. Basically a version of {@link #bindViewHolder(RecyclerView.ViewHolder, int)}.
     */
    @Nullable
    private final BiConsumer<RecyclerView.ViewHolder, Integer> footerViewHolderBinder;

    /**
     * Creates an adapter from a list of {@link Component}s.
     * @param components The list of components.
     * @param activity An activity in which this adapter displays its contents.
     */
    public ComponentAdapter(@NonNull List<Component> components, @NonNull AppCompatActivity activity) {
        this.components = components;
        this.activity = activity;
        footerViewHolderGenerator = null;
        footerViewHolderBinder = null;
    }

    /**
     * Creates an adapter from a list of {@link Component}s. This will use a footer view at the bottom.
     * @param components The list of components.
     * @param activity An activity in which this adapter displays its contents.
     */
    public ComponentAdapter(@NonNull List<Component> components,
                            @NonNull AppCompatActivity activity,
                            @NonNull Function<ViewGroup, RecyclerView.ViewHolder> footerViewHolderGenerator,
                            @NonNull BiConsumer<RecyclerView.ViewHolder, Integer> footerViewHolderBinder) {
        this.components = components;
        this.activity = activity;
        this.footerViewHolderGenerator = footerViewHolderGenerator;
        this.footerViewHolderBinder = footerViewHolderBinder;
    }

    /**
     * Creates a view holder for the given position, by inflating the proper view and using the {@link RecyclerView.ViewHolder}
     * implementation from the correct subclass of {@link Component}.
     * @param parent The view in which the new element will be placed.
     * @param viewType The type of the view, one of {@link Component.ComponentType} constants.
     * @return A view holder for this view.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Component.ComponentType int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        final LayoutInflater inflater = LayoutInflater.from(activity);
        switch (viewType) {
            case Component.ComponentType.ADVANCED:
                view = inflater.inflate(R.layout.component_advanced, parent, false);
                holder = new AdvancedComponent.AdvancedComponentHolder(view);
                break;
            case Component.ComponentType.BOXED:
                view = inflater.inflate(R.layout.component_boxed, parent, false);
                holder = new BoxedComponent.BoxedComponentHolder(view);
                break;
            case Component.ComponentType.CODE:
                view = inflater.inflate(R.layout.component_code, parent, false);
                holder = new CodeComponent.CodeComponentHolder(view);
                break;
            case Component.ComponentType.IMAGE:
                view = inflater.inflate(R.layout.component_image, parent, false);
                holder = new ImageComponent.ImageComponentHolder(view);
                break;
            case Component.ComponentType.INTERACTIVE:
                view = inflater.inflate(R.layout.component_interactive, parent, false);
                holder = new InteractiveComponent.InteractiveComponentHolder(view);
                break;
            case Component.ComponentType.TEXT:
                view = inflater.inflate(R.layout.component_text, parent, false);
                holder = new TextComponent.TextComponentHolder(view);
                break;
            case Component.ComponentType.TITLE:
                view = inflater.inflate(R.layout.component_title, parent, false);
                holder = new TitleComponent.TitleComponentHolder(view);
                break;
            default: //footer
                holder = footerViewHolderGenerator.apply(parent);
        }
        return holder;
    }

    /**
     * Assigns data from the component to the view. This is done with the help of the view holder, which
     * caches the important views, so that {@link View#findViewById(int)} does not have to be used here.
     * The view holder was created in {@link #onCreateViewHolder(ViewGroup, int)}.
     * @param holder The view holder.
     * @param position The position of this view.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        @Component.ComponentType int viewType = getItemViewType(position);
        switch (viewType) {
            case Component.ComponentType.ADVANCED:
                AdvancedComponent.AdvancedComponentHolder advancedHolder = (AdvancedComponent.AdvancedComponentHolder)holder;
                AdvancedComponent advancedComponent = (AdvancedComponent) components.get(position);
                //set text
                advancedHolder.titleTextView.setText(Html.fromHtml(advancedComponent.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                advancedHolder.contentTextView.setText(Html.fromHtml(advancedComponent.getContent(), Html.FROM_HTML_MODE_COMPACT));
                //special formatting for dark mode
                if(ThemeUtils.isDarkTheme()) {
                    int color = ContextCompat.getColor(activity, android.R.color.black);
                    advancedHolder.titleTextView.setTextColor(color);
                    advancedHolder.contentTextView.setTextColor(color);
                }
                break;
            case Component.ComponentType.BOXED:
                BoxedComponent.BoxedComponentHolder boxedHolder = (BoxedComponent.BoxedComponentHolder)holder;
                BoxedComponent boxedComponent = (BoxedComponent) components.get(position);
                //set text
                boxedHolder.titleTextView.setText(Html.fromHtml(boxedComponent.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                boxedHolder.contentTextView.setText(Html.fromHtml(boxedComponent.getContent(), Html.FROM_HTML_MODE_COMPACT));
                break;
            case Component.ComponentType.CODE:
                CodeComponent.CodeComponentHolder codeHolder = (CodeComponent.CodeComponentHolder)holder;
                CodeComponent codeComponent = (CodeComponent) components.get(position);
                //set text
                codeHolder.codeArea.setText(Html.fromHtml(codeComponent.getFormattedCode(), Html.FROM_HTML_MODE_COMPACT));
                //initialize zoom and copy buttons
                codeComponent.initZoomButtons(codeHolder.zoomIn, codeHolder.zoomOut, codeHolder.codeArea);
                codeHolder.copyButton.setOnClickListener(v -> codeComponent.copyOnClick(codeHolder.codeArea, activity));
                break;
            case Component.ComponentType.IMAGE:
                ImageComponent.ImageComponentHolder imageHolder = (ImageComponent.ImageComponentHolder)holder;
                ImageComponent imageComponent = (ImageComponent) components.get(position);
                //set image
                imageHolder.imageView.setImageDrawable(imageComponent.getImageDrawable());
                break;
            case Component.ComponentType.INTERACTIVE:
                InteractiveComponent.InteractiveComponentHolder interactiveHolder = (InteractiveComponent.InteractiveComponentHolder)holder;
                InteractiveComponent interactiveComponent = (InteractiveComponent) components.get(position);
                //set text
                interactiveHolder.instructionTextView.setText(interactiveComponent.getInstruction());
                //set interactive code area
                if(interactiveHolder.codeAreaInteractive.getChildCount() == 0) {
                    //not initialized, so fill it
                    interactiveComponent.fillInteractiveCodeArea(interactiveHolder.codeAreaInteractive);
                }
                //initialize buttons
                interactiveComponent.initZoomButtons(interactiveHolder.zoomIn, interactiveHolder.zoomOut, interactiveHolder.codeAreaInteractive);
                interactiveHolder.resetButton.setOnClickListener(v -> interactiveComponent.reset(activity));
                interactiveHolder.checkButton.setOnClickListener(v -> interactiveComponent.checkSolution(activity, interactiveHolder.codeAreaInteractive));
                break;
            case Component.ComponentType.TEXT:
                TextComponent.TextComponentHolder textHolder = (TextComponent.TextComponentHolder)holder;
                TextComponent textComponent = (TextComponent) components.get(position);
                //set text
                if(textComponent.isDisplaysList()) {
                    textHolder.textView.setText(Html.fromHtml(textComponent.getText(), Html.FROM_HTML_MODE_COMPACT, null, new ListTagHandler()));
                } else {
                    textHolder.textView.setText(Html.fromHtml(textComponent.getText(), Html.FROM_HTML_MODE_COMPACT));
                }
                break;
            case Component.ComponentType.TITLE:
                TitleComponent.TitleComponentHolder titleHolder = (TitleComponent.TitleComponentHolder)holder;
                TitleComponent titleComponent = (TitleComponent) components.get(position);
                //set text
                titleHolder.titleTextView.setText(titleComponent.getTitle());
                break;
            default: //footer
                footerViewHolderBinder.accept(holder, position);
        }
    }

    /**
     * Find the type of a view in this {@link RecyclerView} by position.
     * @param position The position of the view.
     * @return The type of the view.
     */
    @Override
    public int getItemViewType(int position) {
        if(usingFooterView()) {
            //in this case, the last position is a footer view
            if(position == getItemCount() - 1) {
                return FOOTER_VIEW_TYPE;
            } else {
                return components.get(position).getType();
            }
        } else {
            return components.get(position).getType();
        }
    }

    /**
     * @return The amount of views in the {@link RecyclerView}.
     */
    @Override
    public int getItemCount() {
        if(usingFooterView()) {
            return components.size() + 1; //for the footer
        } else {
            return components.size();
        }
    }

    /**
     * @return If this adapter is using a footer view.
     */
    public boolean usingFooterView() {
        return footerViewHolderGenerator != null;
    }
}
