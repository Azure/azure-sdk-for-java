// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.io.NumberInput;

/**
 * Implementation of
 * <a href="http://tools.ietf.org/html/draft-ietf-appsawg-json-pointer-03">JSON Pointer</a>
 * specification.
 * Pointer instances can be used to locate logical JSON nodes for things like
 * tree traversal (see {@link TreeNode#at}).
 * It may be used in future for filtering of streaming JSON content
 * as well (not implemented yet for 2.3).
 *<p>
 * Instances are fully immutable and can be cached, shared between threads.
 * 
 * @author Tatu Saloranta
 *
 * @since 2.3
 */
public class JsonPointer
{
    /**
     * Character used to separate segments.
     *
     * @since 2.9
     */
    public final static char SEPARATOR = '/';

    /**
     * Marker instance used to represent segment that matches current
     * node or position (that is, returns true for
     * {@link #matches()}).
     */
    protected final static JsonPointer EMPTY = new JsonPointer();
    
    /**
     * Reference to rest of the pointer beyond currently matching
     * segment (if any); null if this pointer refers to the matching
     * segment.
     */
    protected final JsonPointer _nextSegment;

    /**
     * Reference from currently matching segment (if any) to node
     * before leaf.
     * Lazily constructed if/as needed.
     *<p>
     * NOTE: we'll use `volatile` here assuming that this is unlikely to
     * become a performance bottleneck. If it becomes one we can probably
     * just drop it and things still should work (despite warnings as per JMM
     * regarding visibility (and lack thereof) of unguarded changes).
     * 
     * @since 2.5
     */
    protected volatile JsonPointer _head;

    /**
     * We will retain representation of the pointer, as a String,
     * so that {@link #toString} should be as efficient as possible.
     */
    protected final String _asString;
    
    protected final String _matchingPropertyName;

    protected final int _matchingElementIndex;

    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */
    
    /**
     * Constructor used for creating "empty" instance, used to represent
     * state that matches current node.
     */
    protected JsonPointer() {
        _nextSegment = null;
        _matchingPropertyName = "";
        _matchingElementIndex = -1;
        _asString = "";
    }

    // Constructor used for creating non-empty Segments
    protected JsonPointer(String fullString, String segment, JsonPointer next) {
        _asString = fullString;
        _nextSegment = next;
        // Ok; may always be a property
        _matchingPropertyName = segment;
        // but could be an index, if parsable
        _matchingElementIndex = _parseIndex(segment);
    }

    // @since 2.5
    protected JsonPointer(String fullString, String segment, int matchIndex, JsonPointer next) {
        _asString = fullString;
        _nextSegment = next;
        _matchingPropertyName = segment;
        _matchingElementIndex = matchIndex;
    }

    /*
    /**********************************************************
    /* Factory methods
    /**********************************************************
     */

    /**
     * Factory method that parses given input and construct matching pointer
     * instance, if it represents a valid JSON Pointer: if not, a
     * {@link IllegalArgumentException} is thrown.
     *
     * @param expr Pointer expression to compile
     *
     * @return Compiled {@link JsonPointer} path expression
     *
     * @throws IllegalArgumentException Thrown if the input does not present a valid JSON Pointer
     *   expression: currently the only such expression is one that does NOT start with
     *   a slash ('/').
     */
    public static JsonPointer compile(String expr) throws IllegalArgumentException
    {
        // First quick checks for well-known 'empty' pointer
        if ((expr == null) || expr.length() == 0) {
            return EMPTY;
        }
        // And then quick validity check:
        if (expr.charAt(0) != '/') {
            throw new IllegalArgumentException("Invalid input: JSON Pointer expression must start with '/': "+"\""+expr+"\"");
        }
        return _parseTail(expr);
    }

    /**
     * Alias for {@link #compile}; added to make instances automatically
     * deserializable by Jackson databind.
     *
     * @param expr Pointer expression to compile
     *
     * @return Compiled {@link JsonPointer} path expression
     */
    public static JsonPointer valueOf(String expr) { return compile(expr); }

    /**
     * Accessor for an "empty" expression, that is, one you can get by
     * calling {@link #compile} with "" (empty String).
     *<p>
     * NOTE: this is different from expression for {@code "/"} which would
     * instead match Object node property with empty String ("") as name.
     *
     * @return "Empty" pointer expression instance that matches given root value
     *
     * @since 2.10
     */
    public static JsonPointer empty() { return EMPTY; }

    /**
     * Factory method that will construct a pointer instance that describes
     * path to location given {@link JsonStreamContext} points to.
     *
     * @param context Context to build pointer expression for
     * @param includeRoot Whether to include number offset for virtual "root context"
     *    or not.
     *
     * @return {@link JsonPointer} path to location of given context
     *
     * @since 2.9
     */
    public static JsonPointer forPath(JsonStreamContext context,
            boolean includeRoot)
    {
        // First things first: last segment may be for START_ARRAY/START_OBJECT,
        // in which case it does not yet point to anything, and should be skipped
        if (context == null) {
            return EMPTY;
        }
        // Otherwise if context was just created but is not advanced -- like,
        // opening START_ARRAY/START_OBJECT returned -- drop the empty context.
        if (!context.hasPathSegment()) {
            // Except one special case: do not prune root if we need it
            if (!(includeRoot && context.inRoot() && context.hasCurrentIndex())) {
                context = context.getParent();
            }
        }
        JsonPointer tail = null;

        for (; context != null; context = context.getParent()) {
            if (context.inObject()) {
                String seg = context.getCurrentName();
                if (seg == null) { // is this legal?
                    seg = "";
                }
                tail = new JsonPointer(_fullPath(tail, seg), seg, tail);
            } else if (context.inArray() || includeRoot) {
                int ix = context.getCurrentIndex();
                String ixStr = String.valueOf(ix);
                tail = new JsonPointer(_fullPath(tail, ixStr), ixStr, ix, tail);
            }
            // NOTE: this effectively drops ROOT node(s); should have 1 such node,
            // as the last one, but we don't have to care (probably some paths have
            // no root, for example)
        }
        if (tail == null) {
            return EMPTY;
        }
        return tail;
    }

    private static String _fullPath(JsonPointer tail, String segment)
    {
        if (tail == null) {
            StringBuilder sb = new StringBuilder(segment.length()+1);
            sb.append('/');
            _appendEscaped(sb, segment);
            return sb.toString();
        }
        String tailDesc = tail._asString;
        StringBuilder sb = new StringBuilder(segment.length() + 1 + tailDesc.length());
        sb.append('/');
        _appendEscaped(sb, segment);
        sb.append(tailDesc);
        return sb.toString();
    }

    private static void _appendEscaped(StringBuilder sb, String segment)
    {
        for (int i = 0, end = segment.length(); i < end; ++i) {
            char c = segment.charAt(i);
           if (c == '/') {
               sb.append("~1");
               continue;
           }
           if (c == '~') {
               sb.append("~0");
               continue;
           }
           sb.append(c);
        }
    }
    
    /* Factory method that composes a pointer instance, given a set
     * of 'raw' segments: raw meaning that no processing will be done,
     * no escaping may is present.
     * 
     * @param segments
     * 
     * @return Constructed path instance
     */
    /* TODO!
    public static JsonPointer fromSegment(String... segments)
    {
        if (segments.length == 0) {
            return EMPTY;
        }
        JsonPointer prev = null;
                
        for (String segment : segments) {
            JsonPointer next = new JsonPointer()
        }
    }
    */
    
    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    public boolean matches() { return _nextSegment == null; }
    public String getMatchingProperty() { return _matchingPropertyName; }
    public int getMatchingIndex() { return _matchingElementIndex; }

    /**
     * @return True if the root selector matches property name (that is, could
     * match field value of JSON Object node)
     */
    public boolean mayMatchProperty() { return _matchingPropertyName != null; }

    /**
     * @return True if the root selector matches element index (that is, could
     * match an element of JSON Array node)
     */
    public boolean mayMatchElement() { return _matchingElementIndex >= 0; }

    /**
     * @return  the leaf of current JSON Pointer expression: leaf is the last
     *    non-null segment of current JSON Pointer.
     *
     * @since 2.5
     */
    public JsonPointer last() {
        JsonPointer current = this;
        if (current == EMPTY) {
            return null;
        }
        JsonPointer next;
        while ((next = current._nextSegment) != JsonPointer.EMPTY) {
            current = next;
        }
        return current;
    }

    /**
     * Mutant factory method that will return
     *<ul>
     * <li>`tail` if `this` instance is "empty" pointer, OR
     *  </li>
     * <li>`this` instance if `tail` is "empty" pointer, OR
     *  </li>
     * <li>Newly constructed {@link JsonPointer} instance that starts with all segments
     *    of `this`, followed by all segments of `tail`.
     *  </li>
     *</ul>
     * 
     * @param tail {@link JsonPointer} instance to append to this one, to create a new pointer instance
     *
     * @return Either `this` instance, `tail`, or a newly created combination, as per description above.
     */
    public JsonPointer append(JsonPointer tail) {
        if (this == EMPTY) {
            return tail;
        }
        if (tail == EMPTY) {
            return this;
        }
        // 21-Mar-2017, tatu: Not superbly efficient; could probably improve by not concatenating,
        //    re-decoding -- by stitching together segments -- but for now should be fine.

        String currentJsonPointer = _asString;
        if (currentJsonPointer.endsWith("/")) {
            //removes final slash
            currentJsonPointer = currentJsonPointer.substring(0, currentJsonPointer.length()-1);
        }
        return compile(currentJsonPointer + tail._asString);
    }

    /**
     * Method that may be called to see if the pointer head (first segment)
     * would match property (of a JSON Object) with given name.
     *
     * @param name Name of Object property to match
     *
     * @return {@code True} if the pointer head matches specified property name
     *
     * @since 2.5
     */
    public boolean matchesProperty(String name) {
        return (_nextSegment != null) && _matchingPropertyName.equals(name);
    }

    /**
     * Method that may be called to check whether the pointer head (first segment)
     * matches specified Object property (by name) and if so, return
     * {@link JsonPointer} that represents rest of the path after match.
     * If there is no match, {@code null} is returned.
     *
     * @param name Name of Object property to match
     *
     * @return Remaining path after matching specified property, if there is match;
     *    {@code null} otherwise
     */
    public JsonPointer matchProperty(String name) {
        if ((_nextSegment != null) && _matchingPropertyName.equals(name)) {
            return _nextSegment;
        }
        return null;
    }

    /**
     * Method that may be called to see if the pointer would match
     * Array element (of a JSON Array) with given index.
     *
     * @param index Index of Array element to match
     *
     * @return {@code True} if the pointer head matches specified Array index
     * 
     * @since 2.5
     */
    public boolean matchesElement(int index) {
        return (index == _matchingElementIndex) && (index >= 0);
    }

    /**
     * Method that may be called to check whether the pointer head (first segment)
     * matches specified Array index and if so, return
     * {@link JsonPointer} that represents rest of the path after match.
     * If there is no match, {@code null} is returned.
     *
     * @param index Index of Array element to match
     *
     * @return Remaining path after matching specified index, if there is match;
     *    {@code null} otherwise
     *
     * @since 2.6
     */
    public JsonPointer matchElement(int index) {
        if ((index != _matchingElementIndex) || (index < 0)) {
            return null;
        }
        return _nextSegment;
    }

    /**
     * Accessor for getting a "sub-pointer" (or sub-path), instance where current segment
     * has been removed and pointer includes rest of the segments.
     * For example, for JSON Pointer "/root/branch/leaf", this method would
     * return pointer "/branch/leaf".
     * For matching state (last segment), will return {@code null}.
     *<p>
     * Note that this is a very cheap method to call as it simply returns "next" segment
     * (which has been constructed when pointer instance was constructed).
     *
     * @return Tail of this pointer, if it has any; {@code null} if this pointer only
     *    has the current segment
     */
    public JsonPointer tail() {
        return _nextSegment;
    }

    /**
     * Accessor for getting a pointer instance that is identical to this
     * instance except that the last segment has been dropped.
     * For example, for JSON Pointer "/root/branch/leaf", this method would
     * return pointer "/root/branch" (compared to {@link #tail()} that
     * would return "/branch/leaf").
     *<p>
     * Note that whereas {@link #tail} is a very cheap operation to call (as "tail" already
     * exists for single-linked forward direction), this method has to fully
     * construct a new instance by traversing the chain of segments.
     *
     * @return Pointer expression that contains same segments as this one, except for
     *    the last segment.
     *
     * @since 2.5
     */
    public JsonPointer head() {
        JsonPointer h = _head;
        if (h == null) {
            if (this != EMPTY) {
                h = _constructHead();
            }
            _head = h;
        }
        return h;
    }

    /*
    /**********************************************************
    /* Standard method overrides
    /**********************************************************
     */

    @Override public String toString() { return _asString; }
    @Override public int hashCode() { return _asString.hashCode(); }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof JsonPointer)) return false;
        return _asString.equals(((JsonPointer) o)._asString);
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private final static int _parseIndex(String str) {
        final int len = str.length();
        // [core#133]: beware of super long indexes; assume we never
        // have arrays over 2 billion entries so ints are fine.
        if (len == 0 || len > 10) {
            return -1;
        }
        // [core#176]: no leading zeroes allowed
        char c = str.charAt(0);
        if (c <= '0') {
            return (len == 1 && c == '0') ? 0 : -1;
        }
        if (c > '9') {
            return -1;
        }
        for (int i = 1; i < len; ++i) {
            c = str.charAt(i);
            if (c > '9' || c < '0') {
                return -1;
            }
        }
        if (len == 10) {
            long l = NumberInput.parseLong(str);
            if (l > Integer.MAX_VALUE) {
                return -1;
            }
        }
        return NumberInput.parseInt(str);
    }
    
    protected static JsonPointer _parseTail(String input) {
        final int end = input.length();

        // first char is the contextual slash, skip
        for (int i = 1; i < end; ) {
            char c = input.charAt(i);
            if (c == '/') { // common case, got a segment
                return new JsonPointer(input, input.substring(1, i),
                        _parseTail(input.substring(i)));
            }
            ++i;
            // quoting is different; offline this case
            if (c == '~' && i < end) { // possibly, quote
                return _parseQuotedTail(input, i);
            }
            // otherwise, loop on
        }
        // end of the road, no escapes
        return new JsonPointer(input, input.substring(1), EMPTY);
    }

    /**
     * Method called to parse tail of pointer path, when a potentially
     * escaped character has been seen.
     * 
     * @param input Full input for the tail being parsed
     * @param i Offset to character after tilde
     *
     * @return Pointer instance constructed
     */
    protected static JsonPointer _parseQuotedTail(String input, int i) {
        final int end = input.length();
        StringBuilder sb = new StringBuilder(Math.max(16, end));
        if (i > 2) {
            sb.append(input, 1, i-1);
        }
        _appendEscape(sb, input.charAt(i++));
        while (i < end) {
            char c = input.charAt(i);
            if (c == '/') { // end is nigh!
                return new JsonPointer(input, sb.toString(),
                        _parseTail(input.substring(i)));
            }
            ++i;
            if (c == '~' && i < end) {
                _appendEscape(sb, input.charAt(i++));
                continue;
            }
            sb.append(c);
        }
        // end of the road, last segment
        return new JsonPointer(input, sb.toString(), EMPTY);
    }

    protected JsonPointer _constructHead()
    {
        // ok; find out who we are to drop
        JsonPointer last = last();
        if (last == this) {
            return EMPTY;
        }
        // and from that, length of suffix to drop
        int suffixLength = last._asString.length();
        JsonPointer next = _nextSegment;
        return new JsonPointer(_asString.substring(0, _asString.length() - suffixLength), _matchingPropertyName,
                _matchingElementIndex, next._constructHead(suffixLength, last));
    }

    protected JsonPointer _constructHead(int suffixLength, JsonPointer last)
    {
        if (this == last) {
            return EMPTY;
        }
        JsonPointer next = _nextSegment;
        String str = _asString;
        return new JsonPointer(str.substring(0, str.length() - suffixLength), _matchingPropertyName,
                _matchingElementIndex, next._constructHead(suffixLength, last));
    }

    private static void _appendEscape(StringBuilder sb, char c) {
        if (c == '0') {
            c = '~';
        } else if (c == '1') {
            c = '/';
        } else {
            sb.append('~');
        }
        sb.append(c);
    }
}
