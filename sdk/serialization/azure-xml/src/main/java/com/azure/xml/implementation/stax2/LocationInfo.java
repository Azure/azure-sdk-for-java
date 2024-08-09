// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.Location;

/**
 * Interface that specifies additional access methods for accessing
 * full location information of an input location within a stream reader.
 * Access interface may be directly implemented by the reader, or by
 * another (reusable or per-call-instantiated) helper object.
 *<p>
 * Note: instances of LocationInfo are only guaranteed to persist as long
 * as the (stream) reader points to the current element (whatever it is).
 * After next call to <code>streamReader.next</code>, it it possible that
 * the previously accessed LocationInfo points to the old information, new
 * information, or may even contain just garbage. That is, for each new
 * event, <code>getLocationInfo</code> should be called separately.
 */
public interface LocationInfo {
    /*
    /**********************************************************************
    /* Low-level extended "raw" location access methods
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Object-oriented location access methods
    /**********************************************************************
     */

    // // // Existing method from XMLStreamReader:

    Location getLocation();

    // // // New methods:

    /**
     * An optional method that either returns the location object that points the
     * starting position of the current event, or null if implementation
     * does not keep track of it (some may return only end location; and
     * some no location at all).
     *
     * @return Location of the first character of the current event in
     *   the input source (which will also be the starting location
     *   of the following event, if any, or EOF if not), or null (if
     *   implementation does not track locations).
     */
    Location getStartLocation();

}
