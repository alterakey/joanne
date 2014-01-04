package com.gmail.altakey.joanne.view;

import java.util.regex.Pattern;
import android.util.Log;

public class Radio {
    private String mScreenName;
    private int mScreenNameColor;
    private int mScreenNameSize;
    private String mText;
    private int mTextColor;
    private int mTextSize;
    private int mDuration;
    private boolean mIsError;

    private static Pattern sEmptyPattern = Pattern.compile("^[\\s　]*$");
    private static Pattern sTidyStage1Pattern = Pattern.compile("[\\s　]{2,}");
    private static Pattern sTidyStage2Pattern = Pattern.compile("^[\\s　]+|[\\s　]+$");

    public int getScreenNameSize() {
        return mScreenNameSize;
    }

    public void setScreenNameSize(final int screenNameSize) {
        mScreenNameSize = screenNameSize;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(final int textSize) {
        mTextSize = textSize;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(final int duration) {
        mDuration = duration;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public void setScreenName(final String screenName) {
        mScreenName = screenName;
    }

    public int getScreenNameColor() {
        return mScreenNameColor;
    }

    public void setScreenNameColor(final int screenNameColor) {
        mScreenNameColor = screenNameColor;
    }

    public String getText() {
        return String.format("<< %s >>", getRawText());
    }

    public String getRawText() {
        return mText;
    }

    public void setText(final String text) {
        mText = text;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(final int textColor) {
        mTextColor = textColor;
    }

    public boolean isError() {
        return mIsError;
    }

    public void setIsError(boolean isError) {
        mIsError = isError;
    }

    public boolean textContains(final String pat) {
        return mText.contains(pat);
    }

    public boolean textContains(final Pattern pat) {
        return pat.matcher(mText).find();
    }

    public void filterText(final Pattern pat, final String replacement) {
        mText = pat.matcher(mText).replaceAll(replacement);
    }

    public void tidyText() {
        tidyText(null);
    }

    public void tidyText(final String nullText) {
        if (nullText != null) {
            mText = Pattern.compile(String.format("(%s[\\s　]+)+([\\s　]+)?", Pattern.quote(nullText))).matcher(mText).replaceAll("$1");
        }
        mText = sTidyStage2Pattern.matcher(sTidyStage1Pattern.matcher(mText).replaceAll(" ")).replaceAll("");
    }

    public boolean isEmpty() {
        return sEmptyPattern.matcher(mText).matches();
    }
}
