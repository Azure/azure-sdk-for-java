// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.StartDocument;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

public class StartDocumentEventImpl extends BaseEventImpl implements StartDocument {
    private final boolean mStandaloneSet;
    private final boolean mIsStandalone;
    private final String mVersion;
    private final boolean mEncodingSet;
    private final String mEncodingScheme;
    private final String mSystemId;

    public StartDocumentEventImpl(Location loc, XMLStreamReader r) {
        super(loc);
        mStandaloneSet = r.standaloneSet();
        mIsStandalone = r.isStandalone();
        /* 06-Aug-2006, TSa: Specs (class javadoc) actually specify that
         *   the default should be "1.0", as opposed to stream reader that
         *   should return null if no declaration exists. So, let's do
         *   defaulting here if needed
         */
        {
            String version = r.getVersion();
            if (version == null || version.isEmpty()) {
                version = "1.0";
            }
            mVersion = version;
        }
        mEncodingScheme = r.getCharacterEncodingScheme();
        mEncodingSet = (mEncodingScheme != null && !mEncodingScheme.isEmpty());
        mSystemId = (loc != null) ? loc.getSystemId() : "";
    }

    /**
     * Method called by event factory, when constructing start document
     * event.
     */
    public StartDocumentEventImpl(Location loc) {
        this(loc, (String) null);
    }

    public StartDocumentEventImpl(Location loc, String encoding) {
        this(loc, encoding, null);
    }

    public StartDocumentEventImpl(Location loc, String encoding, String version) {
        this(loc, encoding, version, false, false);
    }

    public StartDocumentEventImpl(Location loc, String encoding, String version, boolean standaloneSet,
        boolean isStandalone) {
        super(loc);
        mEncodingScheme = encoding;
        mEncodingSet = (encoding != null && !encoding.isEmpty());
        mVersion = version;
        mStandaloneSet = standaloneSet;
        mIsStandalone = isStandalone;
        mSystemId = "";
    }

    @Override
    public boolean encodingSet() {
        return mEncodingSet;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return mEncodingScheme;
    }

    @Override
    public String getSystemId() {
        return mSystemId;
    }

    @Override
    public String getVersion() {
        return mVersion;
    }

    @Override
    public boolean isStandalone() {
        return mIsStandalone;
    }

    @Override
    public boolean standaloneSet() {
        return mStandaloneSet;
    }

    /*
    /**********************************************************************
    /* Implementation of abstract base methods
    /**********************************************************************
     */

    @Override
    public int getEventType() {
        return START_DOCUMENT;
    }

    @Override
    public boolean isStartDocument() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        // Need to output the XML declaration?
        try {
            w.write("<?xml version=\"");
            if (mVersion == null || mVersion.isEmpty()) {
                w.write("1.0");
            } else {
                w.write(mVersion);
            }
            w.write('"');
            if (mEncodingSet) {
                w.write(" encoding=\"");
                w.write(mEncodingScheme);
                w.write('"');
            }
            if (mStandaloneSet) {
                if (mIsStandalone) {
                    w.write(" standalone=\"yes\"");
                } else {
                    w.write(" standalone=\"no\"");
                }
            }
            w.write(" ?>");
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        w.writeStartDocument();
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
        if (!(o instanceof StartDocument))
            return false;

        StartDocument other = (StartDocument) o;

        return (encodingSet() == other.encodingSet())
            && (isStandalone() == other.isStandalone())
            && (standaloneSet() == other.standaloneSet())
            && stringsWithNullsEqual(getCharacterEncodingScheme(), other.getCharacterEncodingScheme())
            && stringsWithNullsEqual(getSystemId(), other.getSystemId())
            && stringsWithNullsEqual(getVersion(), other.getVersion());
    }

    @Override
    public int hashCode() {
        int hash = 0;

        if (encodingSet())
            ++hash;
        if (isStandalone())
            --hash;
        if (standaloneSet())
            hash ^= 1;
        if (mVersion != null)
            hash ^= mVersion.hashCode();
        if (mEncodingScheme != null)
            hash ^= mEncodingScheme.hashCode();
        if (mSystemId != null)
            hash ^= mSystemId.hashCode();
        return hash;
    }
}
