// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/**
 * Contains classes needed for type introspection, mostly used by data binding
 * functionality. Most of this functionality is needed to properly handled
 * generic types, and to simplify and unify processing of things Jackson needs
 * to determine how contained types (of {@link java.util.Collection} and
 * {@link java.util.Map} classes) are to be handled.
 *<p>
 * With 2.9, an additional type ({@link com.azure.json.implementation.jackson.core.type.WritableTypeId})
 * was added to help handling of type identifiers needed to support polymorphic
 * type serialization, deserialization.
 */
package com.typespec.json.implementation.jackson.core.type;
