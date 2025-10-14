// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.implementation.accesshelpers;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;

/**
 * Class containing helper methods for accessing private members of {@link CreateResponsesRequest}.
 */
public final class CreateResponsesRequestAccessHelper {
    private static CreateResponsesRequestAccessor accessor;

    /**
     * Type defining the methods to set the non-public properties of an {@link CreateResponsesRequest} instance.
     */
    public interface CreateResponsesRequestAccessor {
        /**
         * Sets the stream property of the {@link CreateResponsesRequest}.
         *
         * @param options The {@link CreateResponsesRequest} instance
         * @param stream The boolean value to set private stream property
         */
        void setStream(CreateResponsesRequest options, boolean stream);
    }

    /**
     * The method called from {@link com.azure.ai.openai.responses.models.CreateResponsesRequest} to set it's accessor.
     *
     * @param createResponsesRequestAccessor The accessor.
     */
    public static void setAccessor(final CreateResponsesRequestAccessor createResponsesRequestAccessor) {
        accessor = createResponsesRequestAccessor;
    }

    /**
     * Sets the stream property of the {@link CreateResponsesRequest}.
     *
     * @param options The {@link CreateResponsesRequest} instance
     * @param stream The boolean value to set private stream property
     */
    public static void setStream(CreateResponsesRequest options, boolean stream) {
        accessor.setStream(options, stream);
    }

    private CreateResponsesRequestAccessHelper() {
    }
}
