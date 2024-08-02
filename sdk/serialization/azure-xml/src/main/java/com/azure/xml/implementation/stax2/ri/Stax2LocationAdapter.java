// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import javax.xml.stream.Location;

import com.azure.xml.implementation.stax2.XMLStreamLocation2;

/**
 * Simple implementation of {@link XMLStreamLocation2}, which just
 * wraps Stax 1.0 {@link Location} and adds no-operation implementation
 * of the additions.
 */
public class Stax2LocationAdapter implements XMLStreamLocation2 {
    protected final Location mWrappedLocation;

    protected final Location mParentLocation;

    public Stax2LocationAdapter(Location loc) {
        this(loc, null);
    }

    public Stax2LocationAdapter(Location loc, Location parent) {
        mWrappedLocation = loc;
        mParentLocation = parent;
    }

    // // // Basic Stax 1.0 implementation

    @Override
    public int getCharacterOffset() {
        return mWrappedLocation.getCharacterOffset();
    }

    @Override
    public int getColumnNumber() {
        return mWrappedLocation.getColumnNumber();
    }

    @Override
    public int getLineNumber() {
        return mWrappedLocation.getLineNumber();
    }

    @Override
    public String getPublicId() {
        return mWrappedLocation.getPublicId();
    }

    @Override
    public String getSystemId() {
        return mWrappedLocation.getSystemId();
    }

    // // // And stax2 additions

    @Override
    public XMLStreamLocation2 getContext() {
        if (mParentLocation == null) {
            return null;
        }
        if (mParentLocation instanceof XMLStreamLocation2) {
            return (XMLStreamLocation2) mParentLocation;
        }
        return new Stax2LocationAdapter(mParentLocation);
    }
}
