// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.azure.json.implementation.jackson.core.JsonStreamContext;
import com.azure.json.implementation.jackson.core.JsonToken;

/**
 * Extension of {@link JsonStreamContext}, which implements
 * core methods needed, and also exposes
 * more complete API to generator implementation classes.
 */
public class JsonWriteContext extends JsonStreamContext {
    // // // Return values for writeValue()

    public final static int STATUS_OK_AS_IS = 0;
    public final static int STATUS_OK_AFTER_COMMA = 1;
    public final static int STATUS_OK_AFTER_COLON = 2;
    public final static int STATUS_OK_AFTER_SPACE = 3; // in root context
    public final static int STATUS_EXPECT_VALUE = 4;
    public final static int STATUS_EXPECT_NAME = 5;

    /**
     * Parent context for this context; null for root context.
     */
    protected final JsonWriteContext _parent;

    /*
    /**********************************************************
    /* Simple instance reuse slots; speed up things a bit (10-15%)
    /* for docs with lots of small arrays/objects
    /**********************************************************
     */

    protected JsonWriteContext _child;

    /*
    /**********************************************************
    /* Location/state information (minus source reference)
    /**********************************************************
     */

    /**
     * Name of the field of which value is to be written; only
     * used for OBJECT contexts
     */
    protected String _currentName;

    /**
     * @since 2.5
     */
    protected Object _currentValue;

    /**
     * Marker used to indicate that we just wrote a name, and
     * now expect a value to write
     */
    protected boolean _gotName;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected JsonWriteContext(int type, JsonWriteContext parent) {
        super();
        _type = type;
        _parent = parent;
        _nestingDepth = parent == null ? 0 : parent._nestingDepth + 1;
        _index = -1;
    }

    /* @since 2.10 */
    protected JsonWriteContext(int type, JsonWriteContext parent, Object currValue) {
        super();
        _type = type;
        _parent = parent;
        _nestingDepth = parent == null ? 0 : parent._nestingDepth + 1;
        _index = -1;
        _currentValue = currValue;
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
     *
     * @return This context instance to allow call-chaining
     */
    public JsonWriteContext reset(int type) {
        _type = type;
        _index = -1;
        _currentName = null;
        _gotName = false;
        _currentValue = null;
        return this;
    }

    /**
     * Internal method to allow instance reuse: DO NOT USE unless you absolutely
     * know what you are doing.
     * Clears up state, changes type to one specified, assigns "current value";
     * resets current duplicate-detection state (if any).
     * Parent link left as-is since it is {@code final}.
     *<p>
     * NOTE: Public since 2.12.
     *
     * @param type Type to assign to this context node
     * @param currValue Current value to assign to this context node
     *
     * @return This context instance to allow call-chaining
     *
     * @since 2.10
     */
    public JsonWriteContext reset(int type, Object currValue) {
        _type = type;
        _index = -1;
        _currentName = null;
        _gotName = false;
        _currentValue = currValue;
        return this;
    }

    @Override
    public Object getCurrentValue() {
        return _currentValue;
    }

    @Override
    public void setCurrentValue(Object v) {
        _currentValue = v;
    }

    /*
    /**********************************************************
    /* Factory methods
    /**********************************************************
     */

    /**
     * @deprecated Since 2.3; use method that takes argument
     *
     * @return Context instance created
     */
    @Deprecated
    public static JsonWriteContext createRootContext() {
        return new JsonWriteContext(TYPE_ROOT, null);
    }

    public JsonWriteContext createChildArrayContext() {
        JsonWriteContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new JsonWriteContext(TYPE_ARRAY, this);
            return ctxt;
        }
        return ctxt.reset(TYPE_ARRAY);
    }

    public JsonWriteContext createChildObjectContext() {
        JsonWriteContext ctxt = _child;
        if (ctxt == null) {
            _child = ctxt = new JsonWriteContext(TYPE_OBJECT, this);
            return ctxt;
        }
        return ctxt.reset(TYPE_OBJECT);
    }

    @Override
    public final JsonWriteContext getParent() {
        return _parent;
    }

    @Override
    public final String getCurrentName() {
        return _currentName;
    }

    /**
     * Method that can be used to both clear the accumulated references
     * (specifically value set with {@link #setCurrentValue(Object)})
     * that should not be retained, and returns parent (as would
     * {@link #getParent()} do). Typically called when closing the active
     * context when encountering {@link JsonToken#END_ARRAY} or
     * {@link JsonToken#END_OBJECT}.
     *
     * @return Parent context of this context node, if any; {@code null} for root context
     *
     * @since 2.7
     */
    public JsonWriteContext clearAndGetParent() {
        _currentValue = null;
        // could also clear the current name, but seems cheap enough to leave?
        return _parent;
    }

    /**
     * Method that writer is to call before it writes a name of Object property.
     *
     * @param name Property name being written
     *
     * @return Index of the field entry (0-based)
     *
     * @throws JsonProcessingException if duplicate check restriction is violated
     */
    public int writeFieldName(String name) throws JsonProcessingException {
        if ((_type != TYPE_OBJECT) || _gotName) {
            return STATUS_EXPECT_VALUE;
        }
        _gotName = true;
        _currentName = name;
        return (_index < 0) ? STATUS_OK_AS_IS : STATUS_OK_AFTER_COMMA;
    }

    public int writeValue() {
        // Most likely, object:
        if (_type == TYPE_OBJECT) {
            if (!_gotName) {
                return STATUS_EXPECT_NAME;
            }
            _gotName = false;
            ++_index;
            return STATUS_OK_AFTER_COLON;
        }

        // Ok, array?
        if (_type == TYPE_ARRAY) {
            int ix = _index;
            ++_index;
            return (ix < 0) ? STATUS_OK_AS_IS : STATUS_OK_AFTER_COMMA;
        }

        // Nope, root context
        // No commas within root context, but need space
        ++_index;
        return (_index == 0) ? STATUS_OK_AS_IS : STATUS_OK_AFTER_SPACE;
    }
}
