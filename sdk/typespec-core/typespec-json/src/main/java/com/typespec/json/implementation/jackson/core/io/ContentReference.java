// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.io;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Abstraction that encloses information about content being processed --
 * input source or output target, streaming or
 * not -- for the purpose of including pertinent information in
 * location (see {@link com.typespec.json.implementation.jackson.core.JsonLocation})
 * objections, most commonly to be printed out as part of {@code Exception}
 * messages.
 *
 * @since 2.13
 */
public class ContentReference
    // sort of: we will read back as "UNKNOWN"
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Constant that may be used when source/target content is not known
     * (or not exposed).
     *<p>
     * NOTE: As of 2.13 assume to contain Binary content, meaning that no
     * content snippets will be included.
     */
    protected final static ContentReference UNKNOWN_CONTENT =
            new ContentReference(false, null);

    /**
     * Include at most first 500 characters/bytes from contents; should be enough
     * to give context, but not cause unfortunate side effects in things like
     * logs.
     *
     * @since 2.9
     */
    public static final int DEFAULT_MAX_CONTENT_SNIPPET = 500;

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

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected ContentReference(boolean isContentTextual, Object rawContent) {
        this(isContentTextual, rawContent, -1, -1);
    }

    protected ContentReference(boolean isContentTextual, Object rawContent,
            int offset, int length)
    {
        _isContentTextual = isContentTextual;
        _rawContent = rawContent;
        _offset = offset;
        _length = length;
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

    public static ContentReference construct(boolean isContentTextual, Object rawContent) {
        return new ContentReference(isContentTextual, rawContent);
    }

    public static ContentReference construct(boolean isContentTextual, Object rawContent,
            int offset, int length) {
        return new ContentReference(isContentTextual, rawContent, offset, length);
    }

    /**
     * Factory method for legacy code to use for constructing instances to
     * content about which only minimal amount of information is available.
     * Assumed not to contain textual content (no snippet displayed).
     *
     * @param isContentTextual Is raw content assumed to have textual content
     *    ({@code true}) or binary ({@code false})
     * @param rawContent Underlying raw content access
     *
     * @return Instance with minimal information about content (basically just
     *    raw content reference without offsets
     */
    public static ContentReference rawReference(boolean isContentTextual,
            Object rawContent)
    {
        // Just to avoid russian-doll-nesting, let's:
        if (rawContent instanceof ContentReference) {
            return (ContentReference) rawContent;
        }
        return new ContentReference(isContentTextual, rawContent);
    }

    public static ContentReference rawReference(Object rawContent) {
        return rawReference(false, rawContent);
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

    public boolean hasTextualContent() {
        return _isContentTextual;
    }

    public Object getRawContent() {
        return _rawContent;
    }

    public int contentOffset() { return _offset; }
    public int contentLength() { return _length; }

    /**
     * Internal accessor, overridable, used for checking length (in units in
     * which content is counted, either bytes or chars) to use for truncation
     * (so as not to include full content for humongous sources or targets)
     *
     * @return Maximum content snippet to include before truncating
     */
    protected int maxContentSnippetLength() {
        return DEFAULT_MAX_CONTENT_SNIPPET;
    }

    /*
    /**********************************************************************
    /* Method for constructing descriptions
    /**********************************************************************
     */    

    /**
     * Method for constructing a "source description" when content represented
     * by this reference is read.
     *
     * @return Description constructed
     */
    public String buildSourceDescription() {
        return appendSourceDescription(new StringBuilder(200)).toString();
    }

    /**
     * Method for appending a "source description" when content represented
     * by this reference is read.
     *
     * @param sb StringBuilder to append description to
     *
     * @return StringBuilder passed as argument (for call chaining)
     */
    public StringBuilder appendSourceDescription(StringBuilder sb)
    {
        final Object srcRef = getRawContent();

        if (srcRef == null) {
            sb.append("UNKNOWN");
            return sb;
        }
        // First, figure out what name to use as source type
        Class<?> srcType = (srcRef instanceof Class<?>) ?
                ((Class<?>) srcRef) : srcRef.getClass();
        String tn = srcType.getName();
        // standard JDK types without package
        if (tn.startsWith("java.")) {
            tn = srcType.getSimpleName();
        } else if (srcRef instanceof byte[]) { // then some other special cases
            tn = "byte[]";
        } else if (srcRef instanceof char[]) {
            tn = "char[]";
        }
        sb.append('(').append(tn).append(')');

        // and then, include (part of) contents for selected types
        // (never for binary-format data)
        if (hasTextualContent()) {
            String unitStr = " chars";
            String trimmed;

            // poor man's tuple...
            final int maxLen = maxContentSnippetLength();
            int[] offsets = new int[] { contentOffset(), contentLength() };

            if (srcRef instanceof CharSequence) {
                trimmed = _truncate((CharSequence) srcRef, offsets, maxLen);
            } else if (srcRef instanceof char[]) {
                trimmed = _truncate((char[]) srcRef, offsets, maxLen);
            } else if (srcRef instanceof byte[]) {
                trimmed = _truncate((byte[]) srcRef, offsets, maxLen);
                unitStr = " bytes";
            } else {
                trimmed = null;
            }
            if (trimmed != null) {
                _append(sb, trimmed);
                if (offsets[1] > maxLen) {
                    sb.append("[truncated ").append(offsets[1] - maxLen).append(unitStr).append(']');
                }
            }
        } else {
            // What should we do with binary content? Indicate length, if possible
            if (srcRef instanceof byte[]) {
                int length = contentLength();
                // -1 is marker for "till the end" (should we consider offset then, too?)
                if (length < 0) {
                    length = ((byte[]) srcRef).length;
                }
                sb.append('[')
                    .append(length)
                    .append(" bytes]");
            }
        }
        return sb;
    }

    protected String _truncate(CharSequence cs, int[] offsets, int maxSnippetLen) {
        _truncateOffsets(offsets, cs.length());
        final int start = offsets[0];
        final int length = Math.min(offsets[1], maxSnippetLen);
        return cs.subSequence(start, start + length).toString();
    }

    protected String _truncate(char[] cs, int[] offsets, int maxSnippetLen) {
        _truncateOffsets(offsets, cs.length);
        final int start = offsets[0];
        final int length = Math.min(offsets[1], maxSnippetLen);
        return new String(cs, start, length);
    }

    protected String _truncate(byte[] b, int[] offsets, int maxSnippetLen) {
        _truncateOffsets(offsets, b.length);
        final int start = offsets[0];
        final int length = Math.min(offsets[1], maxSnippetLen);
        return new String(b, start, length, Charset.forName("UTF-8"));
    }

    // Method that is given alleged start/offset pair and needs to adjust
    // these to fit
    protected void _truncateOffsets(int[] offsets, int actualLength) {
        int start = offsets[0];
        // first, move start to be within area
        if (start < 0) { // means effectively "start at beginning"
            start = 0;
        } else if (start >= actualLength) {
            start = actualLength;
        }
        offsets[0] = start;

        // And then ensure that we obey maximum physical length restriction
        int length = offsets[1];
        final int maxLength = actualLength - start;
        if ((length < 0) || (length > maxLength)) {
            offsets[1] = maxLength;
        }
    }

    protected int _append(StringBuilder sb, String content) {
        sb.append('"');
        // [core#658]: make sure to escape non-printable
        for (int i = 0, end = content.length(); i < end; ++i) {
            // 06-Apr-2021, tatu: Gee... there is no "Character.isPrintable()",
            //   and from what I can see things get rather complicated trying
            //   to figure out proper way. Hence, we'll do this
            char ch = content.charAt(i);
            if (!Character.isISOControl(ch) || !_appendEscaped(sb, ch)) {
                sb.append(ch);
            }
        }
        sb.append('"');
        return content.length();
    }

    protected boolean _appendEscaped(StringBuilder sb, int ctrlChar) {
        // We'll escape most, but NOT regular CR or LF
        if (ctrlChar == '\r' || ctrlChar == '\n') {
            return false;
        }
        sb.append('\\');
        sb.append('u');
        sb.append(CharTypes.hexToChar((ctrlChar >> 12) & 0xF));
        sb.append(CharTypes.hexToChar((ctrlChar >> 8) & 0xF));
        sb.append(CharTypes.hexToChar((ctrlChar >> 4) & 0xF));
        sb.append(CharTypes.hexToChar(ctrlChar & 0xF));
        return true;
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
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof ContentReference)) return false;
        ContentReference otherSrc = (ContentReference) other;

        // 16-Jan-2022, tatu: First ensure offset/length the same
        if ((_offset != otherSrc._offset)
                || (_length != otherSrc._length)) {
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

        if ((_rawContent instanceof File)
                || (_rawContent instanceof URL)
                || (_rawContent instanceof URI)
        ) {
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
