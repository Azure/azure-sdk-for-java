// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.evt.XMLEvent2;

/**
 * This abstract base class implements common functionality for
 * Stax2 reference implementation's event API part.
 *
 * @author Tatu Saloranta
 */
public abstract class BaseEventImpl implements XMLEvent2 {
    /**
     * Location where token started; exact definition may depends
     * on event type.
     *<p>
     * TODO: remove direct dependencies to this by sub-classes; rename
     *
     * @deprecated Call {@link #getLocation()} instead.
     */
    @Deprecated //
    protected final Location mLocation;

    protected BaseEventImpl(Location loc) {
        mLocation = loc;
    }

    /*
    /**********************************************************************
    /* Skeleton XMLEvent API
    /**********************************************************************
     */

    @Override
    public Characters asCharacters() {
        return (Characters) this;
    }

    @Override
    public EndElement asEndElement() {
        return (EndElement) this;
    }

    @Override
    public StartElement asStartElement() {
        return (StartElement) this;
    }

    @Override
    public abstract int getEventType();

    @Override
    public Location getLocation() {
        return mLocation;
    }

    @Override
    public QName getSchemaType() {
        return null;
    }

    @Override
    public boolean isAttribute() {
        return false;
    }

    @Override
    public boolean isCharacters() {
        return false;
    }

    @Override
    public boolean isEndDocument() {
        return false;
    }

    @Override
    public boolean isEndElement() {
        return false;
    }

    @Override
    public boolean isEntityReference() {
        return false;
    }

    @Override
    public boolean isNamespace() {
        return false;
    }

    @Override
    public boolean isProcessingInstruction() {
        return false;
    }

    @Override
    public boolean isStartDocument() {
        return false;
    }

    @Override
    public boolean isStartElement() {
        return false;
    }

    @Override
    public abstract void writeAsEncodedUnicode(Writer w) throws XMLStreamException;

    /*
    /**********************************************************************
    /* XMLEvent2 (StAX2)
    /**********************************************************************
     */

    @Override
    public abstract void writeUsing(XMLStreamWriter2 w) throws XMLStreamException;

    /*
    /**********************************************************************
    /* Overridden standard methods
    /**********************************************************************
     */

    /**
     * Declared abstract to force redefinition by sub-classes
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Declared abstract to force redefinition by sub-classes
     */
    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return "[Stax Event #" + getEventType() + "]";
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    protected void throwFromIOE(IOException ioe) throws XMLStreamException {
        throw new XMLStreamException(ioe.getMessage(), ioe);
    }

    /**
     * Comparison method that will consider null Strings to be
     * equivalent to empty Strings for comparison purposes; and
     * compare equality with that caveat.
     */
    protected static boolean stringsWithNullsEqual(String s1, String s2) {
        if (s1 == null || s1.isEmpty()) {
            return (s2 == null) || (s2.isEmpty());
        }
        return s1.equals(s2);
    }

    protected static boolean iteratedEquals(Iterator<?> it1, Iterator<?> it2) {
        if (it1 == null || it2 == null) { // if one is null, both have to be
            return (it1 == it2);
        }
        // Otherwise, loop-de-loop...
        while (it1.hasNext()) {
            if (!it2.hasNext()) {
                return false;
            }
            Object o1 = it1.next();
            Object o2 = it2.next();

            if (!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }

    protected static int addHash(Iterator<?> it, int baseHash) {
        int hash = baseHash;
        if (it != null) {
            while (it.hasNext()) {
                hash ^= it.next().hashCode();
            }
        }
        return hash;
    }
}
