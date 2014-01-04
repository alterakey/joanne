package com.gmail.altakey.joanne.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.gmail.altakey.joanne.R;

public class OutlinedTextView extends TextView {
    private float mOutlineWidth;
    private ColorStateList mOutlineColor;

    public OutlinedTextView(Context context) {
        super(context);
    }

    public OutlinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OutlinedTextView, 0, 0);
        try {
            mOutlineWidth = ta.getDimensionPixelSize(R.styleable.OutlinedTextView_outlineWidth, 0);
            mOutlineColor = ta.getColorStateList(R.styleable.OutlinedTextView_outlineColor);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void onDraw(final Canvas c) {
        final Paint p = getPaint();
        final Paint.Style paintStyle = p.getStyle();
        final float paintStrokeWidth = p.getStrokeWidth();
        final ColorStateList colors = getTextColors();
        try {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(mOutlineWidth);
            setTextColor(mOutlineColor);
            super.onDraw(c);
        } finally {
            p.setStyle(paintStyle);
            p.setStrokeWidth(paintStrokeWidth);
            setTextColor(colors);
            super.onDraw(c);
        }
    }
}
