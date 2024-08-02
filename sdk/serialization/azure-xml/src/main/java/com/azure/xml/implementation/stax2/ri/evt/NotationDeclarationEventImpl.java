// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;
import com.azure.xml.implementation.stax2.evt.NotationDeclaration2;

public class NotationDeclarationEventImpl extends BaseEventImpl implements NotationDeclaration2 {
    /**
     * Name/id of the notation, used to reference declaration.
     */
    final String mName;

    final String mPublicId;

    final String mSystemId;

    public NotationDeclarationEventImpl(Location loc, String name, String pubId, String sysId) {
        super(loc);
        mName = name;
        mPublicId = pubId;
        mSystemId = sysId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getPublicId() {
        return mPublicId;
    }

    @Override
    public String getSystemId() {
        return mSystemId;
    }

    /**
     * Empty base implementation: sub-classes should implement
     */
    @Override
    public String getBaseURI() {
        return "";
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return NOTATION_DECLARATION;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write("<!NOTATION ");
            w.write(mName);
            if (mPublicId != null) {
                w.write("PUBLIC \"");
                w.write(mPublicId);
                w.write('"');
            } else {
                w.write("SYSTEM");
            }
            if (mSystemId != null) {
                w.write(" \"");
                w.write(mSystemId);
                w.write('"');
            }
            w.write('>');
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    /**
     * This method does not make much sense for this event type -- the reason
     * being that the notation declarations can only be written as part of
     * a DTD (internal or external subset), not separately. Can basically
     * choose to either skip silently (output nothing), or throw an
     * exception.
     */
    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        /* Fail silently, or throw an exception? Let's do latter; at least
         * then we'll get useful (?) bug reports!
         */
        throw new XMLStreamException("Can not write notation declarations using an XMLStreamWriter");
    }

    /*
    ///////////////////////////////////////////
    // Standard method impl
    ///////////////////////////////////////////
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;

        if (!(o instanceof NotationDeclaration2))
            return false;

        NotationDeclaration2 other = (NotationDeclaration2) o;
        // should we consider Base URI here?
        return stringsWithNullsEqual(getName(), other.getName())
            && stringsWithNullsEqual(getPublicId(), other.getPublicId())
            && stringsWithNullsEqual(getSystemId(), other.getSystemId())
            && stringsWithNullsEqual(getBaseURI(), other.getBaseURI());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (mName != null)
            hash ^= mName.hashCode();
        if (mPublicId != null)
            hash ^= mPublicId.hashCode();
        if (mSystemId != null)
            hash ^= mSystemId.hashCode();
        return hash;
    }
}
