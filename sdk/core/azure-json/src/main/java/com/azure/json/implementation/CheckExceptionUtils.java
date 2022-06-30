// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Utility class for dealing with calls that have checked exceptions.
 */
public final class CheckExceptionUtils {
    /**
     * Makes a call that wraps an {@link IOException} throwing function with an {@link UncheckedIOException}.
     *
     * @param func The function.
     * @param <T> The type the function returns.
     * @return The function return.
     * @throws UncheckedIOException If the function throws an {@link IOException}.
     */
    public static <T> T callWithWrappedIoException(IoExceptionSupplier<T> func) {
        return func.getWithUncheckedIoException();
    }

    /**
     * Makes a call that wraps an {@link IOException} throwing function with an {@link UncheckedIOException}.
     *
     * @param func The function.
     * @throws UncheckedIOException If the function throws an {@link IOException}.
     */
    public static void invokeWithWrappedIoException(IoExceptionInvoker func) {
        func.invokeWithUncheckedIoException();
    }


    public static void validateAndUpdate(IoExceptionInvoker func, Consumer<JsonWriteContext> updated,
        JsonWriteContext context, JsonToken token, boolean fieldAndValue) {
        // Validate the token.
        context.validateToken(token);

        // Perform the operation.
        func.invokeWithUncheckedIoException();

        // Update state if the operation wasn't a field and value.
        if (!fieldAndValue) {
            updated.accept(context.updateContext(token));
        }
    }
}
