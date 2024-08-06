// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.impl;

import javax.xml.stream.Location;

/**
 * Basic implementation of {@link Location}, used by stream
 * readers and writers.
 */
public class LocationImpl implements Location {
    final protected int _charOffset;
    final protected int _col, _row;

    transient protected String _desc = null;

    public LocationImpl(int charOffset, int row, int col) {
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
    public static LocationImpl fromZeroBased(long rawOffset, int rawRow, int rawCol) {
        // row, column are 1-based, offset 0-based
        // TODO: handle overflow
        int offset = (int) rawOffset;
        return new LocationImpl(offset, rawRow + 1, rawCol + 1);
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
        return null;
    }

    @Override
    public String getSystemId() {
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
            _desc = "[row,col {unknown-source}]: [" + _row + ',' + _col + ']';
        }
        return _desc;
    }
}
