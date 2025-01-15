// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.type;

/**
 * Type abstraction that represents Java type that has been resolved
 * (i.e. has all generic information, if any, resolved to concrete
 * types).
 * Note that this is an intermediate type, and all concrete instances
 * MUST be of type <code>JavaType</code> from "databind" bundle -- this
 * abstraction is only needed so that types can be passed through
 * {@link com.azure.json.implementation.jackson.core.JsonParser#readValueAs} methods.
 *
 * @since 2.0
 */
public abstract class ResolvedType {
    /*
     * /**********************************************************
     * /* Public API, simple property accessors
     * /**********************************************************
     */

    public abstract boolean isAbstract();

    public abstract boolean isThrowable();

    public abstract boolean isInterface();

    public abstract boolean isFinal();

    /*
     * /**********************************************************
     * /* Public API, type parameter access
     * /**********************************************************
     */

    /**
     * @deprecated Since 2.7: does not have meaning as parameters depend on type
     *    resolved.
     *
     * @return Type-erased class of something not usable at this point
     */
    @Deprecated // since 2.7
    public Class<?> getParameterSource() {
        return null;
    }

    /*
     * /**********************************************************
     * /* Public API, other
     * /**********************************************************
     */

}
