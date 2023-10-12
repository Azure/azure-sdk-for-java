// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/**
 * Base classes used by concrete Parser and Generator implementations;
 * contain functionality that is not specific to JSON or input
 * abstraction (byte vs char).
 * Most formats extend these types, although it is also possible to
 * directly extend {@link com.typespec.json.implementation.jackson.core.JsonParser} or
 * {@link com.typespec.json.implementation.jackson.core.JsonGenerator}.
 */
package com.typespec.json.implementation.jackson.core.base;
