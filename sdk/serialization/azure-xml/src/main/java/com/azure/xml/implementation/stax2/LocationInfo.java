// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

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

    /**
     * Method that can be used to get exact byte offset (number of bytes
     * read from the stream right before getting to this location) in the
     * stream that is pointed to by this reader, right before the start
     * of the current event.
     *<p>
     * Note: this value MAY be the same as the one returned by
     * {@link #getStartingCharOffset}, but usually only for single-byte
     * character streams (Ascii, ISO-Latin).
     *
     * @return Byte offset (== number of bytes reader so far) within the
     *   underlying stream, if the stream and stream reader are able to
     *   provide this (separate from the character offset, for variable-byte
     *   encodings); -1 if not.
     */
    long getStartingByteOffset();

    /**
     * Method that can be used to get exact character offset (number of chars
     * read from the stream right before getting to this location) in the
     * stream that is pointed to by this reader, right before the start
     * of the current event.
     *<p>
     * Note: this value MAY be the same as the one returned by
     * {@link #getStartingByteOffset}; this is the case for single-byte
     * character streams (Ascii, ISO-Latin), as well as for streams for
     * which byte offset information is not available (Readers, Strings).
     *
     * @return Character offset (== number of bytes reader so far) within the
     *   underlying stream, if the stream and stream reader are able to
     *   provide this (separate from byte offset, for variable-byte
     *   encodings); -1 if not.
     */
    long getStartingCharOffset();

    /**
     * Method that can be used to get exact byte offset (number of bytes
     * read from the stream right before getting to this location) in the
     * stream that is pointed to by this reader, right after the end
     * of the current event.
     *<p>
     * Note: this value MAY be the same as the one returned by
     * {@link #getEndingCharOffset}, but usually only for single-byte
     * character streams (Ascii, ISO-Latin).
     *<p>
     * Note: for lazy-loading implementations, calling this method may
     * require the underlying stream to be advanced and contents parsed;
     * this is why it is possible that an exception be thrown.
     *
     * @return Byte offset (== number of bytes reader so far) within the
     *   underlying stream, if the stream and stream reader are able to
     *   provide this (separate from the character offset, for variable-byte
     *   encodings); -1 if not.
     */
    long getEndingByteOffset() throws XMLStreamException;

    /**
     * Method that can be used to get exact character offset (number of chars
     * read from the stream right before getting to this location) in the
     * stream that is pointed to by this reader, right after the end
     * of the current event.
     *<p>
     * Note: this value MAY be the same as the one returned by
     * {@link #getEndingByteOffset}; this is the case for single-byte
     * character streams (Ascii, ISO-Latin), as well as for streams for
     * which byte offset information is not available (Readers, Strings).
     *<p>
     * Note: for lazy-loading implementations, calling this method may
     * require the underlying stream to be advanced and contents parsed;
     * this is why it is possible that an exception be thrown.
     *
     * @return Character offset (== number of bytes reader so far) within the
     *   underlying stream, if the stream and stream reader are able to
     *   provide this (separate from byte offset, for variable-byte
     *   encodings); -1 if not.
     */
    long getEndingCharOffset() throws XMLStreamException;

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
     *<p>
     * Note: since it is assumed that the start location must either have
     * been collected by now, or is not accessible (i.e. implementation
     * [always] returns null), no exception is allowed to be throws, as
     * no parsing should ever need to be done (unlike with
     * {@link #getEndLocation}).
     *
     * @return Location of the first character of the current event in
     *   the input source (which will also be the starting location
     *   of the following event, if any, or EOF if not), or null (if
     *   implementation does not track locations).
     */
    XMLStreamLocation2 getStartLocation();

    /**
     * A method that returns the current location of the stream reader
     * at the input source. This is somewhere between the start
     * and end locations (inclusive), depending on how parser does it
     * parsing (for non-lazy implementations it's always the end location;
     * for others something else).
     *<p>
     * Since this location information should always be accessible, no
     * further parsing is to be done, and no exceptions can be thrown.
     *
     * @return Location of the next character reader will parse in the
     *   input source.
     */
    XMLStreamLocation2 getCurrentLocation();

    /**
     * An optional method that either returns the location object that points the
     * ending position of the current event, or null if implementation
     * does not keep track of it (some may return only start location; and
     * some no location at all).
     *<p>
     * Note: since some implementations may not yet know the end location
     * (esp. ones that do lazy loading), this call may require further
     * parsing. As a result, this method may throw a parsing or I/O
     * errors.
     *
     * @return Location right after the end
     *   of the current event (which will also be the start location of
     *   the next event, if any, or of EOF otherwise).
     *
     * @throws XMLStreamException If the stream reader had to advance to
     *  the end of the event (to find the location), it may encounter a
     *  parsing (or I/O) error; if so, that gets thrown
     */
    XMLStreamLocation2 getEndLocation() throws XMLStreamException;
}
