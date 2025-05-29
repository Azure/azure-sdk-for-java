// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Objects;

/**
 * This class represents an HTTP ETag. An ETag value could be strong or weak ETag.
 * For more information, check out <a href="https://en.wikipedia.org/wiki/HTTP_ETag">Wikipedia's HTTP ETag</a>.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class ETag {
    private static final ClientLogger LOGGER = new ClientLogger(ETag.class);

    static final String WEAK_ETAG_PREFIX_QUOTE = "W/\"";
    static final String ASTERISK = "*";

    /**
     * An ETag with value {@code *}, which represents any resource.
     */
    public static final ETag ALL = new ETag(ASTERISK);
    private static final ETag NULL = new ETag(null);

    private final String eTag;

    /**
     * Creates a new instance of {@link ETag}.
     *
     * @param eTag The HTTP entity tag string value.
     */
    private ETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Creates a new instance of {@link ETag}.
     * <p>
     * This method will validate that the {@code eTag} is a valid ETag value. Valid ETag values are as follows:
     * <ul>
     *     <li>{@code *}, representing the special value {@link #ALL}</li>
     *     <li>String value beginning and ending with {@code "}, representing a normal ETag</li>
     *     <li>String value beginning with {@code W/"} and ending with {@code "}, representing a weak ETag</li>
     * </ul>
     *
     * If {@code eTag} is null a special null valued ETag will be returned. If the {@code eTag} doesn't meet any of the
     * valid ETag values an {@link IllegalArgumentException} will be thrown.
     *
     * @param eTag The HTTP entity tag string value.
     * @return A new instance of {@link ETag}.
     * @throws IllegalArgumentException If the {@code eTag} is not a valid ETag value.
     */
    public static ETag fromString(String eTag) {
        // If the value is null or "*", create the ETag.
        if (eTag == null) {
            return NULL;
        } else if (ASTERISK.equals(eTag)) {
            return ALL;
        }

        boolean endsWithQuote = eTag.charAt(eTag.length() - 1) == '"';
        boolean startsWithQuote = eTag.charAt(0) == '"';
        boolean startsWithWeakETagPrefix = eTag.startsWith(WEAK_ETAG_PREFIX_QUOTE);
        if (!endsWithQuote || (!startsWithQuote && !startsWithWeakETagPrefix)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("eTag", eTag)
                .log(
                    "The ETag is invalid, it should be null, '*', be wrapped in quotes, or be wrapped in quotes prefixed by W/",
                    IllegalArgumentException::new);
        }

        return new ETag(eTag);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ETag)) {
            return false;
        }

        return Objects.equals(eTag, ((ETag) o).eTag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eTag);
    }

    @Override
    public String toString() {
        return eTag;
    }

    /**
     * Checks if the {@code eTag} a valid ETag value. Valid ETags show below,
     *  - The special character, '*'.
     *  - A strong ETag, which the value is wrapped in quotes, ex, "12345".
     *  - A weak ETag, which value is wrapped in quotes and prefixed by "W/", ex, W/"12345".
     *
     * @param eTag ETag string value.
     */
    private void checkValidETag(String eTag) {

    }
}
