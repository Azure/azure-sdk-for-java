// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

/**
 * Context of JSON handling.
 * <p>
 * Writing context is immutable, any calls to {@link #updateContext(JsonWriteOperation)} will result in either a
 * previous context being returned or the creation of a new context.
 */
public final class JsonWriteContext {
    /**
     * Initial writing context.
     */
    public static final JsonWriteContext ROOT = new JsonWriteContext(null, JsonContext.ROOT);

    /**
     * Final writing context.
     */
    public static final JsonWriteContext COMPLETED = new JsonWriteContext(null, JsonContext.COMPLETED);

    private final JsonWriteContext parent;
    private final JsonContext context;

    private JsonWriteContext(JsonWriteContext parent, JsonContext context) {
        this.parent = parent;
        this.context = context;
    }

    /**
     * Determines whether the writing operation is allowed based on the {@link JsonContext}.
     * <p>
     * The following is the allowed {@link JsonWriteOperation JsonWriteOperations} based on the {@link JsonContext}.
     *
     * <ul>
     *     <li>{@link JsonContext#ROOT} - {@link JsonWriteOperation#START_OBJECT},
     *     {@link JsonWriteOperation#START_ARRAY}, {@link JsonWriteOperation#SIMPLE_VALUE}</li>
     *     <li>{@link JsonContext#OBJECT} - {@link JsonWriteOperation#START_OBJECT},
     *     {@link JsonWriteOperation#END_OBJECT}, {@link JsonWriteOperation#FIELD_NAME},
     *     {@link JsonWriteOperation#FIELD_AND_VALUE}</li>
     *     <li>{@link JsonContext#ARRAY} - {@link JsonWriteOperation#START_OBJECT},
     *     {@link JsonWriteOperation#START_ARRAY}, {@link JsonWriteOperation#END_ARRAY},
     *     {@link JsonWriteOperation#SIMPLE_VALUE}</li>
     *     <li>{@link JsonContext#FIELD_VALUE} - {@link JsonWriteOperation#START_OBJECT},
     *     {@link JsonWriteOperation#START_ARRAY}, {@link JsonWriteOperation#SIMPLE_VALUE}</li>
     *     <li>{@link JsonContext#COMPLETED} - none</li>
     * </ul>
     *
     * Any operation that isn't allowed based on the context will result in an {@link IllegalStateException} to be
     * thrown.
     *
     * @param operation The {@link JsonWriteOperation} being checked.
     * @throws IllegalStateException If the {@link JsonWriteOperation} is invalid based on the {@link JsonContext}.
     */
    public void validateOperation(JsonWriteOperation operation) {
        if (context == JsonContext.ROOT) {
            if (!(operation == JsonWriteOperation.START_OBJECT
                || operation == JsonWriteOperation.START_ARRAY
                || operation == JsonWriteOperation.SIMPLE_VALUE)) {
                throw new IllegalStateException("Writing context is 'ROOT', only 'START_OBJECT', 'START_ARRAY',"
                    + " or 'SIMPLE_VALUE' operations are allowed. Attempted: '" + operation + "'.");
            }
        } else if (context == JsonContext.OBJECT) {
            if (!(operation == JsonWriteOperation.START_OBJECT
                || operation == JsonWriteOperation.END_OBJECT
                || operation == JsonWriteOperation.FIELD_NAME
                || operation == JsonWriteOperation.FIELD_AND_VALUE)) {
                throw new IllegalStateException("Writing context is 'OBJECT', only 'START_OBJECT', 'END_OBJECT',"
                    + " 'FIELD_NAME', or 'FIELD_AND_VALUE' operations are allowed. Attempted: '" + operation + "'.");
            }
        } else if (context == JsonContext.ARRAY) {
            if (!(operation == JsonWriteOperation.START_OBJECT
                || operation == JsonWriteOperation.START_ARRAY
                || operation == JsonWriteOperation.END_ARRAY
                || operation == JsonWriteOperation.SIMPLE_VALUE)) {
                throw new IllegalStateException("Writing context is 'ARRAY', only 'START_OBJECT', 'START_ARRAY',"
                    + ", 'END_ARRAY', or 'SIMPLE_VALUE' operations are allowed. Attempted: '" + operation + "'.");
            }
        } else if (context == JsonContext.FIELD_VALUE) {
            if (!(operation == JsonWriteOperation.START_OBJECT
                || operation == JsonWriteOperation.START_ARRAY
                || operation == JsonWriteOperation.SIMPLE_VALUE)) {
                throw new IllegalStateException("Writing context is 'FIELD_VALUE', only 'START_OBJECT', 'START_ARRAY',"
                    + " or 'SIMPLE_VALUE' operations are allowed. Attempted: '" + operation + "'.");
            }
        } else {
            throw new IllegalStateException("Writing context is 'COMPLETED', no further writing operations allowed. "
                + "Attempted: '" + operation + "'.");
        }
    }

    /**
     * Updates the context based on the writing operation.
     * <p>
     * Operations {@link JsonWriteOperation#END_OBJECT}, {@link JsonWriteOperation#END_ARRAY}, and {@link
     * JsonWriteOperation#SIMPLE_VALUE} complete the current context and prepare set the parent context for return. If
     * the parent context is {@link JsonWriteContext#ROOT} then {@link JsonWriteContext#COMPLETED} is the updated
     * context as the JSON stream has completed writing. But if the {@link JsonWriteContext} isn't
     * {@link JsonWriteContext#ROOT} and the operation is {@link JsonWriteOperation#END_OBJECT} or
     * {@link JsonWriteOperation#END_ARRAY} and the parent context is {@link JsonContext#FIELD_VALUE} that will be
     * completed as well as the value has completed writing.
     * <p>
     * Operations {@link JsonWriteOperation#START_OBJECT}, {@link JsonWriteOperation#START_ARRAY}, and {@link
     * JsonWriteOperation#FIELD_NAME} create a child context where the current context becomes the parent context.
     * <p>
     * Lastly, {@link JsonWriteOperation#FIELD_AND_VALUE} is a special case as it's performing both open state and
     * close state operations, so it's self-closing. This results in the current context being return.
     *
     * @param operation The {@link JsonWriteOperation} triggering the update.
     * @return The updated writing context.
     */
    public JsonWriteContext updateContext(JsonWriteOperation operation) {
        // Ending an array, object, or writing a simple value completes a writing context, prepare the parent as the
        // return context.
        if (operation == JsonWriteOperation.END_ARRAY
            || operation == JsonWriteOperation.END_OBJECT
            || operation == JsonWriteOperation.SIMPLE_VALUE) {
            JsonWriteContext toReturn = parent;

            // If the parent was ROOT writing has completed, return COMPLETED.
            if (toReturn == ROOT) {
                return COMPLETED;
            }

            // If the parent was a FIELD_VALUE and the operation was ending an array or object complete that context
            // as well and return the grandparent.
            if (toReturn.context == JsonContext.FIELD_VALUE
                && (operation == JsonWriteOperation.END_ARRAY || operation == JsonWriteOperation.END_OBJECT)) {
                return toReturn.parent;
            }

            // Otherwise, just return the parent context.
            return toReturn;
        }

        // The next set of checks are straight forward and return a new sub-context.
        if (operation == JsonWriteOperation.START_OBJECT) {
            return new JsonWriteContext(this, JsonContext.OBJECT);
        } else if (operation == JsonWriteOperation.START_ARRAY) {
            return new JsonWriteContext(this, JsonContext.ARRAY);
        } else if (operation == JsonWriteOperation.FIELD_NAME) {
            return new JsonWriteContext(this, JsonContext.FIELD_VALUE);
        }

        // Otherwise, we had a special scenario of field and value which is a self-closing operation.
        return this;
    }
}
