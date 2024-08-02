// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.impl;

import com.azure.xml.implementation.stax2.XMLStreamLocation2;

/**
 * Basic implementation of {@link XMLStreamLocation2}, used by stream
 * readers and writers.
 */
public class LocationImpl implements XMLStreamLocation2 {
    private final static LocationImpl EMPTY = new LocationImpl("", "", -1, -1, -1);

    final protected String _publicId, _systemId;

    final protected int _charOffset;
    final protected int _col, _row;

    transient protected String _desc = null;

    public LocationImpl(String pubId, String sysId, int charOffset, int row, int col) {
        _publicId = pubId;
        _systemId = sysId;
        /* Overflow? Can obviously only handle limited range of overflows,
         * but let's do that at least?
         */
        _charOffset = (charOffset < 0) ? Integer.MAX_VALUE : charOffset;
        _col = col;
        _row = row;
    }

    /**
     * Helper method that will adjust given internal zero-based values
     * to 1-based values that should be externally visible.
     */
    public static LocationImpl fromZeroBased(String pubId, String sysId, long rawOffset, int rawRow, int rawCol) {
        // row, column are 1-based, offset 0-based
        // TODO: handle overflow
        int offset = (int) rawOffset;
        return new LocationImpl(pubId, sysId, offset, rawRow + 1, rawCol + 1);
    }

    public static LocationImpl getEmptyLocation() {
        return EMPTY;
    }

    @Override
    public int getCharacterOffset() {
        return _charOffset;
    }

    @Override
    public int getColumnNumber() {
        return _col;
    }

    @Override
    public int getLineNumber() {
        return _row;
    }

    @Override
    public String getPublicId() {
        return _publicId;
    }

    @Override
    public String getSystemId() {
        return _systemId;
    }

    /*
    /**********************************************************************
    /* Stax2 API
    /**********************************************************************
     */

    @Override
    public XMLStreamLocation2 getContext() {
        // !!! TBI
        return null;
    }

    /*
    /**********************************************************************
    /* Overridden standard methods
    /**********************************************************************
     */

    @Override
    public String toString() {
        if (_desc == null) {
            StringBuffer sb = new StringBuffer(100);
            appendDesc(sb);
            _desc = sb.toString();
        }
        return _desc;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void appendDesc(StringBuffer sb) {
        String srcId;

        if (_systemId != null) {
            sb.append("[row,col,system-id]: ");
            srcId = _systemId;
        } else if (_publicId != null) {
            sb.append("[row,col,public-id]: ");
            srcId = _publicId;
        } else {
            sb.append("[row,col {unknown-source}]: ");
            srcId = null;
        }
        sb.append('[');
        sb.append(_row);
        sb.append(',');
        sb.append(_col);

        if (srcId != null) {
            sb.append(',');
            sb.append('"');
            sb.append(srcId);
            sb.append('"');
        }
        sb.append(']');
    }
}
