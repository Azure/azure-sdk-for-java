// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

public class AttributeEventImpl extends BaseEventImpl implements Attribute {
    final QName mName;
    final String mValue;
    final boolean mWasSpecified;

    public AttributeEventImpl(Location loc, String localName, String uri, String prefix, String value,
        boolean wasSpecified) {
        super(loc);
        mValue = value;
        if (prefix == null) {
            if (uri == null) {
                mName = new QName(localName);
            } else {
                mName = new QName(uri, localName);
            }
        } else {
            if (uri == null) {
                uri = ""; // only because QName will barf otherwise...
            }
            mName = new QName(uri, localName, prefix);
        }
        mWasSpecified = wasSpecified;
    }

    public AttributeEventImpl(Location loc, QName name, String value, boolean wasSpecified) {
        super(loc);
        mName = name;
        mValue = value;
        mWasSpecified = wasSpecified;
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return ATTRIBUTE;
    }

    @Override
    public boolean isAttribute() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        /* Specs don't really specify exactly what to output... but
         * let's do a reasonable guess:
         */
        String prefix = mName.getPrefix();
        try {
            if (prefix != null && !prefix.isEmpty()) {
                w.write(prefix);
                w.write(':');
            }
            w.write(mName.getLocalPart());
            w.write('=');
            w.write('"');
            writeEscapedAttrValue(w, mValue);
            w.write('"');
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        QName n = mName;
        w.writeAttribute(n.getPrefix(), n.getLocalPart(), n.getNamespaceURI(), mValue);
    }

    /*
    ///////////////////////////////////////////
    // Attribute implementation
    ///////////////////////////////////////////
     */

    @Override
    public String getDTDType() {
        /* !!! TBI: 07-Sep-2004, TSa: Need to figure out an efficient way
         *    to pass this info...
         */
        return "CDATA";
    }

    @Override
    public QName getName() {
        return mName;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public boolean isSpecified() {
        return mWasSpecified;
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
        if (!(o instanceof Attribute))
            return false;

        Attribute other = (Attribute) o;
        if (mName.equals(other.getName()) && mValue.equals(other.getValue())) {
            /* But now; do we care about compatibility of
             * DTD/Schema datatype and whether it's created
             * from attribute defaulting? Let's start by being
             * conservative and require those to match
             */
            if (isSpecified() == other.isSpecified()) {
                return stringsWithNullsEqual(getDTDType(), other.getDTDType());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        /* Hmmh. Definitely need hashCode of name; but how about
         * value? That's potentially more expensive. But, if
         * using code wants to avoid value, it should key off name
         * anyway.
         */
        return mName.hashCode() ^ mValue.hashCode();
    }

    /*
    ///////////////////////////////////////////
    // Internal methods
    ///////////////////////////////////////////
     */

    protected static void writeEscapedAttrValue(Writer w, String value) throws IOException {
        int i = 0;
        int len = value.length();
        do {
            int start = i;
            char c = '\u0000';

            for (; i < len; ++i) {
                c = value.charAt(i);
                if (c == '<' || c == '&' || c == '"') {
                    break;
                }
            }
            int outLen = i - start;
            if (outLen > 0) {
                w.write(value, start, outLen);
            }
            if (i < len) {
                if (c == '<') {
                    w.write("&lt;");
                } else if (c == '&') {
                    w.write("&amp;");
                } else {
                    w.write("&quot;");

                }
            }
        } while (++i < len);
    }
}
