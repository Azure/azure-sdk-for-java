// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

/**
 * Simple container of information about an open element (one for which
 * start tag has been seen, but not yet end; or, for empty tags,
 * START_ELEMENT has been returned but END_ELEMENT not yet)
 */
public final class ElementScope {
    ElementScope mParent;

    PName mName;

    public ElementScope(PName name, ElementScope parent) {
        mParent = parent;
        mName = name;
    }

    public PName getName() {
        return mName;
    }

    public ElementScope getParent() {
        return mParent;
    }

    @Override
    public String toString() {
        if (mParent == null) {
            return mName.toString();
        }
        return mParent + "/" + mName;
    }
}
