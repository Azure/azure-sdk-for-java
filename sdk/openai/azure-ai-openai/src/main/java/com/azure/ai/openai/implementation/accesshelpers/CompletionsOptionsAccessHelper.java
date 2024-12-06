// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation.accesshelpers;

import com.azure.ai.openai.models.ChatCompletionStreamOptions;
import com.azure.ai.openai.models.CompletionsOptions;

/**
 * Class containing helper methods for accessing private members of {@link CompletionsOptions}.
 */
public final class CompletionsOptionsAccessHelper {
    private static CompletionsOptionsAccessor accessor;

    /**
     * Type defining the methods to set the non-public properties of an {@link CompletionsOptions} instance.
     */
    public interface CompletionsOptionsAccessor {
        /**
         * Sets the stream property of the {@link CompletionsOptions}.
         *
         * @param completionsOptions The {@link CompletionsOptions} instance
         * @param stream The boolean value to set private stream property
         */
        void setStream(CompletionsOptions completionsOptions, boolean stream);

        /**
         * Set the streamOptions property of the {@link CompletionsOptions}.
         *
         * @param options The {@link CompletionsOptions} instance
         * @param streamOptions the streamOptions value to set.
         */
        void setStreamOptions(CompletionsOptions options, ChatCompletionStreamOptions streamOptions);
    }

    /**
     * The method called from {@link CompletionsOptions} to set it's accessor.
     *
     * @param completionsOptionsAccessor The accessor.
     */
    public static void setAccessor(final CompletionsOptionsAccessor completionsOptionsAccessor) {
        accessor = completionsOptionsAccessor;
    }

    /**
     * Sets the stream property of the {@link CompletionsOptions}.
     *
     * @param options The {@link CompletionsOptions} instance
     * @param stream The boolean value to set private stream property
     */
    public static void setStream(CompletionsOptions options, boolean stream) {
        accessor.setStream(options, stream);
    }

    /**
     * Set the streamOptions property of the {@link CompletionsOptions}.
     *
     * @param options The {@link CompletionsOptions} instance
     * @param streamOptions the streamOptions value to set.
     */
    public static void setStreamOptions(CompletionsOptions options, ChatCompletionStreamOptions streamOptions) {
        accessor.setStreamOptions(options, streamOptions);
    }

    private CompletionsOptionsAccessHelper() {
    }
}
