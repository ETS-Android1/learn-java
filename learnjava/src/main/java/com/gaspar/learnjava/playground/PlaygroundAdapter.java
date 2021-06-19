package com.gaspar.learnjava.playground;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * This class manages the fragments inside {@link PlaygroundActivity}.
 * @see InputFragment
 * @see OutputFragment
 * @see CodeFragment
 */
public class PlaygroundAdapter extends FragmentStateAdapter {

    /**
     * Creates an adapter.
     * @param fragmentActivity The activity in which the adapter displays the contents. This will be
     *                         an instance of {@link PlaygroundActivity}.
     */
    public PlaygroundAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Create a fragment for a given position. Also supplies the fragment with arguments.
     * @param position The position, one of {@link PlaygroundTab} constants.
     * @return The created fragment.
     */
    @NonNull
    @Override
    public Fragment createFragment(@PlaygroundTab int position) {
        Fragment fragment = null;
        switch (position) {
            case PlaygroundTab.TAB_CODE:
                fragment = CodeFragment.newInstance();
                break;
            case PlaygroundTab.TAB_INPUT:
                fragment = InputFragment.newInstance();
                break;
            case PlaygroundTab.TAB_OUTPUT:
                fragment = OutputFragment.newInstance();
                break;
        }
        return fragment;
    }

    /**
     * Counts the fragments managed by the adapter.
     * @return The fragment count, same as the amount of {@link PlaygroundTab} constants.
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
