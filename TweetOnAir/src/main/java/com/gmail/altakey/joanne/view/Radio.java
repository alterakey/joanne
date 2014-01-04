package com.gmail.altakey.joanne.view;

public class Radio {
    private String mScreenName;
    private int mScreenNameColor;
    private int mScreenNameSize;
    private String mText;
    private int mTextColor;
    private int mTextSize;
    private int mDuration;
    private boolean mIsError;

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
}
