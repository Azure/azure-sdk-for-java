// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import com.azure.json.implementation.jackson.core.ErrorReportConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * Abstraction that encloses information about content being processed --
 * input source or output target, streaming or
 * not -- for the purpose of including pertinent information in
 * location (see {@link com.azure.json.implementation.jackson.core.JsonLocation})
 * objections, most commonly to be printed out as part of {@code Exception}
 * messages.
 *
 * @since 2.13
 */
public class ContentReference
    // sort of: we will read back as "UNKNOWN"
    implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Constant that may be used when source/target content is not known
     * (or not exposed).
     *<p>
     * NOTE: As of 2.13 assume to contain Binary content, meaning that no
     * content snippets will be included.
     */
    protected final static ContentReference UNKNOWN_CONTENT = new ContentReference(false, null);

    /**
     * As content will be redacted by default in Jackson 2.16 and later,
     * we'll use a new marker reference for slightly different description
     * from "unknown", to indicate explicit removal of source/content reference
     * (as opposed to it missing from not being available or so)
     *
     * @since 2.16
     */
    protected final static ContentReference REDACTED_CONTENT = new ContentReference(false, null);

    /**
     * Reference to the actual underlying content.
     */
    protected final transient Object _rawContent;

    /**
     * For static content, indicates offset from the beginning
     * of static array.
     * {@code -1} if not in use.
     */
    protected final int _offset;

    /**
     * For static content, indicates length of content in
     * the static array.
     * {@code -1} if not in use.
     */
    protected final int _length;

    /**
     * Marker flag to indicate whether included content is textual or not:
     * this is taken to mean, by default, that a snippet of content may be
     * displayed for exception messages.
     */
    protected final boolean _isContentTextual;

    /**
     * max raw content to return as configured
     *
     * @since 2.16
     */
    protected final int _maxRawContentLength;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    /**
     * @deprecated Since 2.16. Use {@link #ContentReference(Object)} instead.
     */
    @Deprecated
    protected ContentReference(boolean isContentTextual, Object rawContent) {
        this(isContentTextual, rawContent, -1, -1);
    }

    /**
     * @since 2.16
     */
    protected ContentReference(Object rawContent) {
        this(true, rawContent, -1, -1);
    }

    /**
     * @since 2.16
     */
    protected ContentReference(boolean isContentTextual, Object rawContent, int offset, int length) {
        _isContentTextual = isContentTextual;
        _rawContent = rawContent;
        _offset = offset;
        _length = length;
        _maxRawContentLength = ErrorReportConfiguration.defaults().getMaxRawContentLength();
    }

    /**
     * Accessor for getting a placeholder for cases where actual content
     * is not known (or is not something that system wants to expose).
     *
     * @return Placeholder "unknown" (or "empty") instance to use instead of
     *    {@code null} reference
     */
    public static ContentReference unknown() {
        return UNKNOWN_CONTENT;
    }

    /**
     * Accessor for getting a placeholder when actual content
     * is not to be exposed: different from {@link #unknown()} where
     * content is not available to be referenced.
     *
     * @return Placeholder instance to use in cases where reference is explicitly
     *   blocked, usually for security reasons.
     *
     * @since 2.16
     */
    public static ContentReference redacted() {
        return REDACTED_CONTENT;
    }

    /**
     * @since 2.16
     */
    public static ContentReference construct(Object rawContent) {
        return new ContentReference(rawContent);
    }

    /*
    /**********************************************************************
    /* Serializable overrides
    /**********************************************************************
     */

    // For JDK serialization: can/should not retain raw content, so need
    // not read or write anything

    private void readObject(ObjectInputStream in) throws IOException {
        // nop: but must override the method
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // nop: but must override the method
    }

    protected Object readResolve() {
        return UNKNOWN_CONTENT;
    }

    /*
    /**********************************************************************
    /* Basic accessors
    /**********************************************************************
     */

    public Object getRawContent() {
        return _rawContent;
    }

    /*
    /**********************************************************************
    /* Standard method overrides
    /**********************************************************************
     */

    // Just needed for JsonLocation#equals(): although it'd seem we only need
    // to care about identity, for backwards compatibility better compare
    // bit more
    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ContentReference))
            return false;
        ContentReference otherSrc = (ContentReference) other;

        // 16-Jan-2022, tatu: First ensure offset/length the same
        if ((_offset != otherSrc._offset) || (_length != otherSrc._length)) {
            return false;
        }

        // 16-Jan-2022, tatu: As per [core#739] we'll want to consider some
        //   but not all content cases with real equality: the concern here is
        //   to avoid expensive comparisons and/or possible security issues
        final Object otherRaw = otherSrc._rawContent;

        if (_rawContent == null) {
            return (otherRaw == null);
        } else if (otherRaw == null) {
            return false;
        }

        if ((_rawContent instanceof File) || (_rawContent instanceof URL) || (_rawContent instanceof URI)) {
            return _rawContent.equals(otherRaw);
        }
        return _rawContent == otherSrc._rawContent;
    }

    // Just to appease LGTM...
    @Override
    public int hashCode() {
        return Objects.hashCode(_rawContent);
    }
}
