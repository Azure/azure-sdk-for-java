// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/**
 * Package that contains abstractions needed to support optional
 * non-blocking decoding (parsing) functionality.
 * Although parsers are constructed normally via
 * {@link com.typespec.json.implementation.jackson.core.JsonFactory}
 * (and are, in fact, sub-types of {@link com.typespec.json.implementation.jackson.core.JsonParser}),
 * the way input is provided differs.
 *
 * @since 2.9
 */

package com.typespec.json.implementation.jackson.core.async;
