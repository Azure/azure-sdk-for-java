// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.*;
import com.azure.json.implementation.jackson.core.io.ContentReference;

/**
 * Extension of {@link JsonStreamContext}, which implements
 * core methods needed, and also exposes
 * more complete API to parser implementation classes.
 */
public final class JsonReadContext extends JsonStreamContext {
    // // // Configuration

    /**
     * Parent context for this context; null for root context.
     */
    private final JsonReadContext _parent;

    /*
    /**********************************************************
    /* Simple instance reuse slots; speeds up things a bit (10-15%)
    /* for docs with lots of small arrays/objects (for which
    /* allocation was visible in profile stack frames)
    /**********************************************************
     */

    private JsonReadContext _child;

    /*
    /**********************************************************
    /* Location/state information (minus source reference)
    /**********************************************************
     */

    private String _currentName;

    private int _lineNr;
    private int _columnNr;

    /*
    /**********************************************************
    /* Instance construction, config, reuse
    /**********************************************************
     */

    /**
     * @param parent Parent context, if any ({@code null} for Root context)
     * @param nestingDepth Number of parents this context has (0 for Root context)
     * @param type Type to assign to this context node
     * @param lineNr Line of the starting position of this context
     * @param colNr Column of the starting position of this context
     *
     * @since 2.15
     */
    public JsonReadContext(JsonReadContext parent, int nestingDepth, int type, int lineNr, int colNr) {
        super();
        _parent = parent;
        _type = type;
        _lineNr = lineNr;
        _columnNr = colNr;
        _index = -1;
        _nestingDepth = nestingDepth;
    }

    /**
     * Internal method to allow instance reuse: DO NOT USE unless you absolutely
     * know what you are doing.
     * Clears up state (including "current value"), changes type to one specified;
     * resets current duplicate-detection state (if any).
     * Parent link left as-is since it is {@code final}.
     *<p>
     * NOTE: Public since 2.12.
     *
     * @param type Type to assign to this context node
     * @param lineNr Line of the starting position of this context
     * @param colNr Column of the starting position of this context
     */
    public void reset(int type, int lineNr, int colNr) {
        _type = type;
        _index = -1;
        _lineNr = lineNr;
        _columnNr = colNr;
        _currentName = null;
    }

    /*
    /**********************************************************
    /* Factory methods
    /**********************************************************
     */

    public static JsonReadContext createRootContext() {
        return new JsonReadContext(null, 0, TYPE_ROOT, 1, 0);
    }

    public JsonReadContext createChildArrayContext(int lineNr, int colNr) {
        JsonReadContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new JsonReadContext(this, _nestingDepth + 1, TYPE_ARRAY, lineNr, colNr);
        } else {
            ctxt.reset(TYPE_ARRAY, lineNr, colNr);
        }
        return ctxt;
    }

    public JsonReadContext createChildObjectContext(int lineNr, int colNr) {
        JsonReadContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new JsonReadContext(this, _nestingDepth + 1, TYPE_OBJECT, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(TYPE_OBJECT, lineNr, colNr);
        return ctxt;
    }

    /*
    /**********************************************************
    /* Abstract method implementations, overrides
    /**********************************************************
     */

    @Override
    public String getCurrentName() {
        return _currentName;
    }

    @Override
    public JsonReadContext getParent() {
        return _parent;
    }

    @Override
    public JsonLocation startLocation(ContentReference srcRef) {
        // We don't keep track of offsets at this level (only reader does)
        long totalChars = -1L;
        return new JsonLocation(srcRef, totalChars, _lineNr, _columnNr);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Method that can be used to both clear the accumulated references
     * (specifically value set with {@code #setCurrentValue(Object)})
     * that should not be retained, and returns parent (as would
     * {@link #getParent()} do). Typically called when closing the active
     * context when encountering {@link JsonToken#END_ARRAY} or
     * {@link JsonToken#END_OBJECT}.
     *
     * @return Parent context of this context node, if any; {@code null} for root context
     *
     * @since 2.7
     */
    public JsonReadContext clearAndGetParent() {
        // could also clear the current name, but seems cheap enough to leave?
        return _parent;
    }

    /*
    /**********************************************************
    /* State changes
    /**********************************************************
     */

    public boolean expectComma() {
        /* Assumption here is that we will be getting a value (at least
         * before calling this method again), and
         * so will auto-increment index to avoid having to do another call
         */
        int ix = ++_index; // starts from -1
        return (_type != TYPE_ROOT && ix > 0);
    }

    public void setCurrentName(String name) {
        _currentName = name;
    }

}
