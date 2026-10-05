// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ValidatableContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies that the various upload and download option types implement {@link ValidatableContent} so that content
 * validation can be configured and inspected agnostically across operations.
 */
public class ValidatableContentTests {

    private static Stream<Arguments> validatableOptions() {
        BinaryData data = BinaryData.fromString("data");
        PageRange pageRange = new PageRange().setStart(0).setEnd(511);
        return Stream.of(Arguments.of("AppendBlobAppendBlockOptions", new AppendBlobAppendBlockOptions(data)),
            Arguments.of("PageBlobUploadPagesOptions", new PageBlobUploadPagesOptions(pageRange, data)),
            Arguments.of("BlockBlobStageBlockOptions", new BlockBlobStageBlockOptions("blockId", data)),
            Arguments.of("BlobDownloadContentOptions", new BlobDownloadContentOptions()),
            Arguments.of("BlobDownloadStreamOptions", new BlobDownloadStreamOptions()),
            Arguments.of("BlobInputStreamOptions", new BlobInputStreamOptions()));
    }

    @ParameterizedTest
    @MethodSource("validatableOptions")
    public void optionsImplementValidatableContent(String name, Object options) {
        ValidatableContent validatable = assertInstanceOf(ValidatableContent.class, options, name);

        // Defaults to null (no content validation configured).
        assertNull(validatable.getContentValidationAlgorithm());

        // Fluent setter returns the same instance and the value round-trips through the interface.
        ValidatableContent returned = validatable.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        assertSame(validatable, returned, name);
        assertSame(ContentValidationAlgorithm.CRC64, validatable.getContentValidationAlgorithm());
    }

    @Test
    public void configureAgnostically() {
        // A single helper can operate on any supported options type via the interface.
        AppendBlobAppendBlockOptions appendOptions = new AppendBlobAppendBlockOptions(BinaryData.fromString("data"));
        applyCrc64(appendOptions);
        assertSame(ContentValidationAlgorithm.CRC64, appendOptions.getContentValidationAlgorithm());
    }

    private static void applyCrc64(ValidatableContent content) {
        content.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
    }
}
