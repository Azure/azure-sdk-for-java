// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.EntityDeclaration;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

/**
 * Simple base implementation that can be used either as a placeholder,
 * or a base for 'real' entity declaration implementations.
 */
public class EntityDeclarationEventImpl extends BaseEventImpl implements EntityDeclaration {
    protected final String mName;

    public EntityDeclarationEventImpl(Location loc, String name) {
        super(loc);
        mName = name;
    }

    /*
    ///////////////////////////////////////////
    // EntityDeclaration
    ///////////////////////////////////////////
     */

    @Override
    public String getBaseURI() {
        return "";
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getNotationName() {
        return null;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getReplacementText() {
        return null;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return ENTITY_DECLARATION;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write("<!ENTITY ");
            w.write(getName());
            w.write(" \"");
            // Should really quote... for now, let's not bother:
            String content = getReplacementText();
            if (content != null) {
                w.write(content);
            }
            w.write("\">");
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        // Really shouldn't be output. But if we must...
        StringWriter strw = new StringWriter();
        writeAsEncodedUnicode(strw);
        w.writeRaw(strw.toString());
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

        if (!(o instanceof EntityDeclaration))
            return false;

        EntityDeclaration other = (EntityDeclaration) o;
        return stringsWithNullsEqual(getName(), other.getName())
            && stringsWithNullsEqual(getBaseURI(), other.getBaseURI())
            && stringsWithNullsEqual(getNotationName(), other.getNotationName())
            && stringsWithNullsEqual(getPublicId(), other.getPublicId())
            && stringsWithNullsEqual(getReplacementText(), other.getReplacementText())
            && stringsWithNullsEqual(getSystemId(), other.getSystemId());
    }

    @Override
    public int hashCode() {
        // Since we don't have much data, this is easy...
        return mName.hashCode();
    }
}
