// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

/**
 * Add-on interface used to indicate things that may be "blueprint" objects
 * which can not be used as is, but are used for creating usable per-process
 * (serialization, deserialization) instances, using
 * {@link #createInstance} method.
 *<p>
 * Note that some implementations may choose to implement {@link #createInstance}
 * by simply returning 'this': this is acceptable if instances are stateless.
 * 
 * @see DefaultPrettyPrinter
 * 
 * @since 2.1
 */
public interface Instantiatable<T>
{
    /**
     * Method called to ensure that we have a non-blueprint object to use;
     * it is either this object (if stateless), or a newly created object
     * with separate state.
     *
     * @return Actual instance to use
     */
    T createInstance();
}
