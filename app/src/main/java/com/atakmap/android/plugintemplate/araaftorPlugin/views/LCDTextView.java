package com.atakmap.android.plugintemplate.araaftorPlugin.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class LCDTextView extends TextView {
    public LCDTextView(Context context) {
        super(context);
        setFont();
    }

    public LCDTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public LCDTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/dsdigi.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}