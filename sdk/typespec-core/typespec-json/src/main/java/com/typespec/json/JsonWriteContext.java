// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

/**
 * Context of JSON handling.
 * <p>
 * Writing context is immutable, any calls to {@link #updateContext(JsonToken)} will result in either a previous context
 * being returned or the creation of a new context.
 *
 * @see JsonWriter
 */
public final class JsonWriteContext {
    /**
     * Initial writing context.
     */
    public static final JsonWriteContext ROOT = new JsonWriteContext(null, JsonWriteState.ROOT);

    /**
     * Final writing context.
     */
    public static final JsonWriteContext COMPLETED = new JsonWriteContext(null, JsonWriteState.COMPLETED);

    private final JsonWriteContext parent;
    private final JsonWriteState context;

    private JsonWriteContext(JsonWriteContext parent, JsonWriteState context) {
        this.parent = parent;
        this.context = context;
    }

    /**
     * Gets the parent {@link JsonWriteContext}.
     * <p>
     * {@link JsonWriteContext#ROOT} and {@link JsonWriteContext#COMPLETED} are terminal writing contexts and don't have
     * parent contexts. These are the only writing contexts that will return null.
     *
     * @return The parent writing context.
     */
    public JsonWriteContext getParent() {
        return parent;
    }

    /**
     * Gets the {@link JsonWriteState} associated to the writing context.
     *
     * @return The {@link JsonWriteState} associated to the writing context.
     */
    public JsonWriteState getWriteState() {
        return context;
    }

    /**
     * Determines whether the {@link JsonToken} is allowed to be written based on the {@link JsonWriteState}.
     * <p>
     * The following is the allowed {@link JsonToken JsonTokens} based on the {@link JsonWriteState}.
     *
     * <ul>
     *     <li>{@link JsonWriteState#ROOT} - {@link JsonToken#START_OBJECT}, {@link JsonToken#START_ARRAY},
     *     {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL}, {@link JsonToken#NUMBER}, {@link JsonToken#STRING}</li>
     *     <li>{@link JsonWriteState#OBJECT} - {@link JsonToken#END_OBJECT}, {@link JsonToken#FIELD_NAME}</li>
     *     <li>{@link JsonWriteState#ARRAY} - {@link JsonToken#START_OBJECT}, {@link JsonToken#START_ARRAY},
     *     {@link JsonToken#END_ARRAY}, {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL}, {@link JsonToken#NUMBER},
     *     {@link JsonToken#STRING}</li>
     *     <li>{@link JsonWriteState#FIELD} - {@link JsonToken#START_OBJECT}, {@link JsonToken#START_ARRAY},
     *     {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL}, {@link JsonToken#NUMBER}, {@link JsonToken#STRING}</li>
     *     <li>{@link JsonWriteState#COMPLETED} - none</li>
     * </ul>
     *
     * Any token that isn't allowed based on the context will result in an {@link IllegalStateException}.
     * <p>
     * Field and value APIs in {@link JsonWriter}, such as {@link JsonWriter#writeStringField(String, String)}, will
     * validate with {@link JsonToken#FIELD_NAME} as they're self-closing operations.
     *
     * @param token The {@link JsonToken} that is being validated for being writable in the current state.
     * @throws IllegalStateException If the {@link JsonToken} is invalid based on the {@link JsonWriteState}.
     */
    public void validateToken(JsonToken token) {
        if (context == JsonWriteState.ROOT) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'ROOT', only 'START_OBJECT', 'START_ARRAY',"
                    + " 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '" + token + "'.");
            }
        } else if (context == JsonWriteState.OBJECT) {
            if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY || token == JsonToken.END_ARRAY
                || isSimpleValue(token)) {
                throw new IllegalStateException("Writing context is 'OBJECT', only 'END_OBJECT' and 'FIELD_NAME'"
                    + " tokens are allowed. Attempted: '" + token + "'.");
            }
        } else if (context == JsonWriteState.ARRAY) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'ARRAY', only 'START_OBJECT', 'START_ARRAY',"
                    + ", 'END_ARRAY', 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '"
                    + token + "'.");
            }
        } else if (context == JsonWriteState.FIELD) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'FIELD', only 'START_OBJECT', 'START_ARRAY',"
                    + " 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '" + token + "'.");
            }
        } else {
            throw new IllegalStateException("Writing context is 'COMPLETED', no further tokens are allowed. "
                + "Attempted: '" + token + "'.");
        }
    }

    /**
     * Updates the context based on the {@link JsonToken} that was written.
     * <p>
     * Tokens {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL}, {@link JsonToken#NUMBER}, and {@link JsonToken#STRING}
     * can mutate the current state in three different ways. If the current context is {@link JsonWriteContext#ROOT}
     * then {@link JsonWriteContext#COMPLETED} is the updated context as the JSON stream has completed writing. If the
     * current context is {@link JsonWriteState#ARRAY} then this context is returned without mutation as writing an
     * element to an array doesn't complete the array. Otherwise, the parent context is returned as the only other legal
     * context is {@link JsonWriteState#FIELD} and writing a value completes the field.
     * <p>
     * Tokens {@link JsonToken#END_OBJECT} and {@link JsonToken#END_ARRAY} complete the current context and prepare set
     * the parent context for return. If the parent context is {@link JsonWriteContext#ROOT} then
     * {@link JsonWriteContext#COMPLETED} is the updated context as the JSON stream has completed writing. Otherwise,
     * if the parent context is {@link JsonWriteState#FIELD} that will be completed as well as the field has completed
     * writing.
     * <p>
     * Tokens {@link JsonToken#START_OBJECT}, {@link JsonToken#START_ARRAY}, and {@link JsonToken#FIELD_NAME} create a
     * child context where the current context becomes the parent context.
     * <p>
     * Field and value APIs in {@link JsonWriter}, such as {@link JsonWriter#writeStringField(String, String)}, are
     * self-closing operations that will maintain the current context.
     *
     * @param token The {@link JsonToken} triggering the update.
     * @return The updated writing context.
     */
    public JsonWriteContext updateContext(JsonToken token) {
        // Simple value has three scenarios:
        //
        // - Current context is the root, writing a simple value completes the JSON stream and the writing context
        //   becomes COMPLETE.
        // - Current context isn't the root, writing context becomes the parent context and the context is completed.
        // - Current context is ARRAY, writing context stays the same.
        if (isSimpleValue(token)) {
            if (context == JsonWriteState.ROOT) {
                return COMPLETED;
            } else if (context == JsonWriteState.ARRAY) {
                return this;
            } else {
                return parent;
            }
        }

        // Ending an array or object has three scenarios, but before the scenarios play out the current context is
        // completed. The scenarios are:
        //
        // - Parent context is the root, closing the array or object completes the JSON stream and the writing context
        //   becomes complete.
        // - Parent context is a FIELD_VALUE, closing the array or object completes the field value and the writing
        //   context becomes the grandparent context.
        // - The parent context is a wrapping array or object, return the parent context.
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
            JsonWriteContext toReturn = parent;

            // Parent context is the root, complete writing by returning the COMPLETED context.
            if (toReturn.context == JsonWriteState.ROOT) {
                return COMPLETED;
            }

            // Parent context is a FIELD_VALUE, close the field context by returning the grandparent context.
            if (toReturn.context == JsonWriteState.FIELD) {
                return toReturn.parent;
            }

            // Otherwise, just return the parent context.
            return toReturn;
        }

        // The next set of checks are straight forward and return a new sub-context.
        if (token == JsonToken.START_OBJECT) {
            return new JsonWriteContext(this, JsonWriteState.OBJECT);
        } else if (token == JsonToken.START_ARRAY) {
            return new JsonWriteContext(this, JsonWriteState.ARRAY);
        } else if (token == JsonToken.FIELD_NAME) {
            return new JsonWriteContext(this, JsonWriteState.FIELD);
        }

        // Otherwise, we had a special scenario of field and value which is a self-closing token.
        return this;
    }

    private static boolean isSimpleValue(JsonToken token) {
        return token == JsonToken.BOOLEAN
            || token == JsonToken.NULL
            || token == JsonToken.NUMBER
            || token == JsonToken.STRING;
    }
}
