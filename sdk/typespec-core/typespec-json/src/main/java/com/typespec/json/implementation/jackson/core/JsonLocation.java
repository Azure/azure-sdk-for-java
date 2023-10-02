// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.io.ContentReference;

/**
 * Object that encapsulates Location information used for reporting
 * parsing (or potentially generation) errors, as well as current location
 * within input streams.
 *<p>
 * NOTE: users should be careful if using {@link #equals} implementation as
 * it may or may not compare underlying "content reference" for equality.
 * Instead if would make sense to explicitly implementing equality checks
 * using specific criteria caller desires.
 */
public class JsonLocation
    implements java.io.Serializable
{
    private static final long serialVersionUID = 2L; // in 2.13

    /**
     * @deprecated Since 2.13 use {@link ContentReference#DEFAULT_MAX_CONTENT_SNIPPET} instead
     */
    @Deprecated
    public static final int MAX_CONTENT_SNIPPET = 500;

    /**
     * Shared immutable "N/A location" that can be returned to indicate
     * that no location information is available.
     *<p>
     * NOTE: before 2.9, Location was given as String "N/A"; with 2.9 it was
     * removed so that source should be indicated as "UNKNOWN".
     */
    public final static JsonLocation NA = new JsonLocation(ContentReference.unknown(),
            -1L, -1L, -1, -1);

    protected final long _totalBytes;
    protected final long _totalChars;

    protected final int _lineNr;
    protected final int _columnNr;

    /**
     * Reference to input source; never null (but may be that of
     * {@link ContentReference#unknown()}).
     *
     * @since 2.13 (before we have {@code _sourceRef} (Object-valued)
     */
    protected final ContentReference _contentReference;

    /**
     * Lazily constructed description for source; constructed if and
     * when {@link #sourceDescription()} is called, retained.
     *
     * @since 2.13
     */
    protected transient String _sourceDescription;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    public JsonLocation(ContentReference contentRef, long totalChars,
            int lineNr, int colNr)
    {
        this(contentRef, -1L, totalChars, lineNr, colNr);
    }

    public JsonLocation(ContentReference contentRef, long totalBytes, long totalChars,
            int lineNr, int columnNr)
    {
        // 14-Mar-2021, tatu: Defensive programming, but also for convenience...
        if (contentRef == null) {
            contentRef = ContentReference.unknown();
        }
        _contentReference = contentRef;
        _totalBytes = totalBytes;
        _totalChars = totalChars;
        _lineNr = lineNr;
        _columnNr = columnNr;
    }

    @Deprecated // since 2.13
    public JsonLocation(Object srcRef, long totalChars, int lineNr, int columnNr) {
        this(_wrap(srcRef), totalChars, lineNr, columnNr);
    }

    @Deprecated // since 2.13
    public JsonLocation(Object srcRef, long totalBytes, long totalChars,
            int lineNr, int columnNr) {
        this(_wrap(srcRef), totalBytes, totalChars, lineNr, columnNr);
    }

    protected static ContentReference _wrap(Object srcRef) {
        if (srcRef instanceof ContentReference) {
            return (ContentReference) srcRef;
        }
        return ContentReference.construct(false, srcRef);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    /**
     * Accessor for information about the original input source content is being
     * read from. Returned reference is never {@code null} but may not contain
     * useful information.
     *<p>
     * NOTE: not getter, on purpose, to avoid inlusion if serialized using
     * default Jackson serializer.
     *
     * @return Object with information about input source.
     *
     * @since 2.13 (to replace {@code getSourceRef})
     */
    public ContentReference contentReference() {
        return _contentReference;
    }

    /**
     * Reference to the original resource being read, if one available.
     * For example, when a parser has been constructed by passing
     * a {@link java.io.File} instance, this method would return
     * that File. Will return null if no such reference is available,
     * for example when {@link java.io.InputStream} was used to
     * construct the parser instance.
     *
     * @return Source reference this location was constructed with, if any; {@code null} if none
     *
     * @deprecated Since 2.13 Use {@link #contentReference} instead
     */
    @Deprecated
    public Object getSourceRef() {
        return _contentReference.getRawContent();
    }

    /**
     * Access for getting line number of this location, if available.
     * Note that line number is typically not available for binary formats.
     *
     * @return Line number of the location (1-based), if available; {@code -1} if not.
     */
    public int getLineNr() { return _lineNr; }

    /**
     * Access for getting column position of this location, if available.
     * Note that column position is typically not available for binary formats.
     *
     * @return Column position of the location (1-based), if available; {@code -1} if not.
     */
    public int getColumnNr() { return _columnNr; }

    /**
     * @return Character offset within underlying stream, reader or writer,
     *   if available; {@code -1} if not.
     */
    public long getCharOffset() { return _totalChars; }

    /**
     * @return Byte offset within underlying stream, reader or writer,
     *   if available; {@code -1} if not.
     */
    public long getByteOffset() { return _totalBytes; }

    /**
     * Accessor for getting a textual description of source reference
     * (Object returned by {@link #getSourceRef()}), as included in
     * description returned by {@link #toString()}.
     *<p>
     * Note: implementation will simply call
     * {@link ContentReference#buildSourceDescription()})
     *<p>
     * NOTE: not added as a "getter" to prevent it from getting serialized.
     *
     * @return Description of the source reference (see {@link #getSourceRef()}
     *
     * @since 2.9
     */
    public String sourceDescription() {
        // 04-Apr-2021, tatu: Construct lazily but retain
        if (_sourceDescription == null) {
            _sourceDescription = _contentReference.buildSourceDescription();
        }
        return _sourceDescription;
    }

    /**
     * Accessor for a brief summary of Location offsets (line number, column position,
     * or byte offset, if available).
     *
     * @return Description of available relevant location offsets; combination of
     *    line number and column position or byte offset
     *
     * @since 2.13
     */
    public String offsetDescription() {
        return appendOffsetDescription(new StringBuilder(40)).toString();
    }

    // @since 2.13
    public StringBuilder appendOffsetDescription(StringBuilder sb)
    {
        // 04-Apr-2021, tatu: [core#694] For binary content, we have no line
        //    number or column position indicators; try using what we do have
        //    (if anything)

        if (_contentReference.hasTextualContent()) {
            sb.append("line: ");
            // should be 1-based, but consider -1 to be canonical "got none"
            if (_lineNr >= 0) {
                sb.append(_lineNr);
            } else {
                sb.append("UNKNOWN");
            }
            sb.append(", column: ");
            if (_columnNr >= 0) { // same here
                sb.append(_columnNr);
            } else {
                sb.append("UNKNOWN");
            }
        } else {
            // 04-Apr-2021, tatu: Ideally byte formats would not need line/column
            //    info, but for backwards-compatibility purposes (Jackson 2.x),
            //    will leave logic here
            if (_lineNr > 0) { // yes, require 1-based in case of allegedly binary content
                sb.append("line: ").append(_lineNr);
                if (_columnNr > 0) {
                    sb.append(", column: ");
                    sb.append(_columnNr);
                }
            } else {
                sb.append("byte offset: #");
                // For binary formats, total bytes should be the canonical offset
                // for token/current location
                if (_totalBytes >= 0) {
                    sb.append(_totalBytes);
                } else {
                    sb.append("UNKNOWN");
                }
            }
        }
        return sb;
    }

    /*
    /**********************************************************************
    /* Standard method overrides
    /**********************************************************************
     */

    @Override
    public int hashCode()
    {
        int hash = (_contentReference == null) ? 1 : 2;
        hash ^= _lineNr;
        hash += _columnNr;
        hash ^= (int) _totalChars;
        hash += (int) _totalBytes;
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof JsonLocation)) return false;
        JsonLocation otherLoc = (JsonLocation) other;

        if (_contentReference == null) {
            if (otherLoc._contentReference != null) return false;
        } else if (!_contentReference.equals(otherLoc._contentReference)) {
            return false;
        }

        return (_lineNr == otherLoc._lineNr)
            && (_columnNr == otherLoc._columnNr)
            && (_totalChars == otherLoc._totalChars)
            && (_totalBytes == otherLoc._totalBytes)
            ;
    }

    @Override
    public String toString()
    {
        final String srcDesc = sourceDescription();
        StringBuilder sb = new StringBuilder(40 + srcDesc.length())
                .append("[Source: ")
                .append(srcDesc)
                .append("; ");
        return appendOffsetDescription(sb)
                .append(']')
                .toString();
    }
}
