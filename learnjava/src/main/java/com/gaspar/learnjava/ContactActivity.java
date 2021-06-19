package com.gaspar.learnjava;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * This activity displays information about helping me improve the app. Allows quick email contact.
 */
public class ContactActivity extends ThemedActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        toolbar = findViewById(R.id.toolbarContact);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //load contact options to linear layout
        String[] contactMeWhen = getResources().getStringArray(R.array.contact_me_when);
        LinearLayout contactMeWhenLayout = findViewById(R.id.contactMeWhenLayout);
        for(String contactOption: contactMeWhen) {
            TextView textView = createListPoint(contactOption);
            contactMeWhenLayout.addView(textView);
        }
    }

    /**
     * Creates a text view from a string.
     * @param text The text it will contain.
     * @return The created view.
     */
    private TextView createListPoint(String text) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,5,0,5);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.medium_text_size));
        return textView;
    }

    /**
     * Called when the user clicks the write email button. Brings up an email activity selection.
     */
    public void contactOnClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                + getString(R.string.my_email)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.default_email_subject));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_chooser)));
    }

    /**
     * Called when the back button is clicked on the toolbar.
     * @return True.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
