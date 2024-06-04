// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.io.UncheckedIOException;

/**
 * A set of resources that are used by the assistant's tools. The resources are specific to the type of tool.
 * For example, the `code_interpreter` tool requires a list of file IDs, while the `file_search` tool requires
 * a list of vector store IDs.
 */
@Immutable
public final class CreateFileSearchToolResourceOptions {
    /**
     * The vector store attached to this assistant. There can be a maximum of 1 vector store attached to the assistant.
     */
    private final CreateFileSearchToolResourceVectorStoreIds vectorStoreIds;

    /**
     * A helper to create a vector store with file_ids and attach it to this assistant. There can be a maximum of 1 vector
     * store attached to the assistant.
     */
    private final CreateFileSearchToolResourceVectorStoreOptionsList vectorStores;

    /**
     * Creates an instance of CreateFileSearchToolResourceOptions class.
     *
     * @param vectorStores the vector stores to set.
     */
    public CreateFileSearchToolResourceOptions(CreateFileSearchToolResourceVectorStoreOptionsList vectorStores) {
        this.vectorStoreIds = null;
        this.vectorStores = vectorStores;
    }

    /**
     * Creates an instance of CreateFileSearchToolResourceOptions class.
     *
     * @param vectorStoreIds the vector store IDs to set.
     */
    public CreateFileSearchToolResourceOptions(CreateFileSearchToolResourceVectorStoreIds vectorStoreIds) {
        this.vectorStoreIds = vectorStoreIds;
        this.vectorStores = null;
    }

    /**
     * Get the vector store attached to this assistant. There can be a maximum of 1 vector store attached to the assistant.
     *
     * @return the vectorStores value.
     */
    public CreateFileSearchToolResourceVectorStoreOptionsList getVectorStores() {
        return this.vectorStores;
    }

    /**
     * Ge the vector store IDs attached to this assistant. There can be a maximum of 1 vector store attached to the assistant.
     *
     * @return the vectorStoreIds value.
     */
    public CreateFileSearchToolResourceVectorStoreIds getVectorStoreIds() {
        return this.vectorStoreIds;
    }

    /**
     * Creates a new instance of CreateFileSearchToolResourceOptions based on a JSON string.
     *
     * @param responseFormatBinaryData input JSON string
     * @throws IllegalArgumentException If the provided JSON string does not match the expected format.
     * @return a new instance of CreateFileSearchToolResourceOptions
     */
    public static CreateFileSearchToolResourceOptions fromBinaryData(BinaryData responseFormatBinaryData) {
        if (responseFormatBinaryData == null) {
            return null;
        }
        try {
            CreateFileSearchToolResourceVectorStoreIds vectorStoreIds = responseFormatBinaryData.toObject(CreateFileSearchToolResourceVectorStoreIds.class);
            if (vectorStoreIds != null) {
                return new CreateFileSearchToolResourceOptions(vectorStoreIds);
            }

        } catch (UncheckedIOException e) {
            CreateFileSearchToolResourceVectorStoreOptionsList vectorStoreOptions =
                responseFormatBinaryData.toObject(CreateFileSearchToolResourceVectorStoreOptionsList.class);
            if (vectorStoreOptions != null) {
                return new CreateFileSearchToolResourceOptions(vectorStoreOptions);
            }
        }
        throw new IllegalArgumentException("The provided JSON string does not match the expected format.");
    }
}
