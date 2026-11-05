// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Represents options for a transfer operation whose content integrity can be validated. Implemented by the various
 * upload and download option types so that transfer content validation can be configured agnostically across many
 * different operations.
 * <p>
 * This allows application code to enable or inspect content validation without needing to special-case every concrete
 * option type. For example, a helper can accept a {@link ValidatableContent} and apply a single
 * {@link ContentValidationAlgorithm} to any supported upload or download options instance.
 *
 * @see ContentValidationAlgorithm
 */
public interface ValidatableContent {

    /**
     * Gets the algorithm to use for transfer content validation. See {@link ContentValidationAlgorithm} for more
     * details.
     *
     * @return The transfer validation checksum algorithm.
     */
    ContentValidationAlgorithm getContentValidationAlgorithm();

    /**
     * Sets the algorithm to use for transfer content validation. See {@link ContentValidationAlgorithm} for more
     * details.
     *
     * @param contentValidationAlgorithm The transfer validation checksum algorithm.
     * @return The updated options.
     */
    ValidatableContent setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm);
}
