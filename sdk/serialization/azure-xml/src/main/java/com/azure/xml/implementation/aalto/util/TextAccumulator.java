// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

/**
 * Simple utility class used to efficiently accumulate and concatenate
 * text passed in various forms
 */
public final class TextAccumulator {
    private String mText = null;

    private StringBuilder mBuilder = null;

    public TextAccumulator() {
    }

    public boolean hasText() {
        return (mBuilder != null) || (mText != null);
    }

    public void addText(String text) {
        int len = text.length();
        if (len > 0) {
            // Any prior text?
            if (mText != null) {
                mBuilder = new StringBuilder(mText.length() + len);
                mBuilder.append(mText);
                mText = null;
            }
            if (mBuilder != null) {
                mBuilder.append(text);
            } else {
                mText = text;
            }
        }
    }

    public void addText(char[] buf, int start, int end) {
        int len = end - start;
        if (len > 0) {
            // Any prior text?
            if (mText != null) {
                mBuilder = new StringBuilder(mText.length() + len);
                mBuilder.append(mText);
                mText = null;
            } else if (mBuilder == null) {
                /* more efficient to use a builder than a string; and although
                 * could use a char array, StringBuilder has the benefit of
                 * being able to share the array, eventually.
                 */
                mBuilder = new StringBuilder(len);
            }
            mBuilder.append(buf, start, end - start);
        }
    }

    public String getAndClear() {
        String result;

        if (mText != null) {
            result = mText;
            mText = null;
        } else if (mBuilder != null) {
            result = mBuilder.toString();
            mBuilder = null;
        } else {
            result = "";
        }
        return result;
    }
}
