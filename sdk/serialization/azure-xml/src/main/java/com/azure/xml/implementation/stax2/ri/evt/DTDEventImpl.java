// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import javax.xml.stream.*;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.NotationDeclaration;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;
import com.azure.xml.implementation.stax2.evt.DTD2;

/**
 * Vanilla implementation of a DTD event. Note that as is, it is not
 * really complete, since there is no way to access declared notations
 * and entities, because Stax 1.0 has no method for dispatching calls.
 * As such, it is expected that actual implementations would usually
 * extend this class, instead of using it as is.
 */
public class DTDEventImpl extends BaseEventImpl implements DTD2 {
    final protected String mRootName;

    final protected String mSystemId;

    final protected String mPublicId;

    final protected String mInternalSubset;

    final protected Object mDTD;

    /*
    /**********************************************************************
    /* Lazily constructed objects
    /**********************************************************************
     */

    /**
     * Full textual presentation of the DOCTYPE event; usually only
     * constructed when needed, but sometimes (when using 'broken'
     * older StAX interfaces), may be the only piece that's actually
     * passed.
     */
    protected String mFullText;

    /*
    /**********************************************************************
    /* Constructors
    /**********************************************************************
     */

    public DTDEventImpl(Location loc, String rootName, String sysId, String pubId, String intSubset, Object dtd) {
        super(loc);
        mRootName = rootName;
        mSystemId = sysId;
        mPublicId = pubId;
        mInternalSubset = intSubset;
        mFullText = null;
        mDTD = dtd;
    }

    /**
     * Constructor used when only partial information is available.
     */
    public DTDEventImpl(Location loc, String rootName, String intSubset) {
        this(loc, rootName, null, null, intSubset, null);
    }

    public DTDEventImpl(Location loc, String fullText) {
        this(loc, null, null, null, null, null);
        mFullText = fullText;
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    @Override
    public String getDocumentTypeDeclaration() {
        try {
            return doGetDocumentTypeDeclaration();
        } catch (XMLStreamException sex) { // should never happen
            throw new RuntimeException("Internal error: " + sex);
        }
    }

    @Override
    public List<EntityDeclaration> getEntities() {
        // !!! TODO: create stax2 abstraction to allow accessing this
        return null;
    }

    @Override
    public List<NotationDeclaration> getNotations() {
        // !!! TODO: create stax2 abstraction to allow accessing this
        return null;
    }

    @Override
    public Object getProcessedDTD() {
        return mDTD;
    }

    /*
    /**********************************************************************
    /* Implementation of abstract base methods
    /**********************************************************************
     */

    @Override
    public int getEventType() {
        return DTD;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            // If we get 'raw' (unparsed) DOCTYPE contents, this is easy...
            if (mFullText != null) {
                w.write(mFullText);
                return;
            }

            w.write("<!DOCTYPE");
            if (mRootName != null) {
                // Can only be null for plain XMLStreamReader interface?
                w.write(' ');
                w.write(mRootName);
            }
            if (mSystemId != null) {
                if (mPublicId != null) {
                    w.write(" PUBLIC \"");
                    w.write(mPublicId);
                    w.write('"');
                } else {
                    w.write(" SYSTEM");
                }
                w.write(" \"");
                w.write(mSystemId);
                w.write('"');
            }
            if (mInternalSubset != null) {
                w.write(" [");
                w.write(mInternalSubset);
                w.write(']');
            }
            w.write(">");
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        if (mRootName != null) {
            w.writeDTD(mRootName, mSystemId, mPublicId, mInternalSubset);
            return;
        }

        // Nah, just need to do a "dumb" write...
        w.writeDTD(doGetDocumentTypeDeclaration());
    }

    /*
    /**********************************************************************
    /* Extended interface (DTD2)
    /**********************************************************************
     */

    @Override
    public String getRootName() {
        return mRootName;
    }

    @Override
    public String getSystemId() {
        return mSystemId;
    }

    @Override
    public String getPublicId() {
        return mPublicId;
    }

    @Override
    public String getInternalSubset() {
        return mInternalSubset;
    }

    /*
    /**********************************************************************
    /* Standard method impl
    /**********************************************************************
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof DTD))
            return false;

        DTD other = (DTD) o;

        /* Hmmh. Comparison for this event get very tricky, very fast, if one
         * tries to do it correctly (partly due to Stax2 incompleteness, but not just
         * because of that)... let's actually try to minimize work here
         */
        return stringsWithNullsEqual(getDocumentTypeDeclaration(), other.getDocumentTypeDeclaration());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (mRootName != null)
            hash ^= mRootName.hashCode();
        if (mSystemId != null)
            hash ^= mSystemId.hashCode();
        if (mPublicId != null)
            hash ^= mPublicId.hashCode();
        if (mInternalSubset != null)
            hash ^= mInternalSubset.hashCode();
        if (mDTD != null)
            hash ^= mDTD.hashCode();
        if (hash == 0 && mFullText != null) {
            hash ^= mFullText.hashCode();
        }
        return hash;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected String doGetDocumentTypeDeclaration() throws XMLStreamException {
        if (mFullText == null) {
            int len = 60;
            if (mInternalSubset != null) {
                len += mInternalSubset.length() + 4;
            }
            StringWriter sw = new StringWriter(len);
            writeAsEncodedUnicode(sw);
            mFullText = sw.toString();
        }
        return mFullText;
    }
}
