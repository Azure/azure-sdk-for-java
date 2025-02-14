// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.security.keyvault.administration.implementation.models.Error;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationError;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;

/**
 * A class containing utility methods for the Azure Security Key Vault Administration library.
 */
public final class KeyVaultAdministrationUtils {
    private KeyVaultAdministrationUtils() {
        throw new UnsupportedOperationException("Cannot instantiate KeyVaultAdministrationUtils");
    }

    /**
     * Convert a {@link HttpResponseException} to a {@link KeyVaultAdministrationException}.
     *
     * @param exception The {@link HttpResponseException}.
     *
     * @return An instance of the public {@link KeyVaultAdministrationException}.
     */
    public static KeyVaultAdministrationException toKeyVaultAdministrationException(HttpResponseException exception) {
        if (exception == null) {
            return null;
        }

        return new KeyVaultAdministrationException(exception.getMessage(), exception.getResponse(),
            toKeyVaultAdministrationError(exception.getValue()));
    }

    /**
     * Convert an implementation {@link Error} to a public {@link KeyVaultAdministrationError}.
     *
     * @param value The {@link Error} returned by the service.
     *
     * @return An instance of the public {@link KeyVaultAdministrationError}.
     */
    public static KeyVaultAdministrationError toKeyVaultAdministrationError(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof Error) {
                Error error = (Error) value;

                return new KeyVaultAdministrationError(error.getCode(), error.getMessage(),
                    toKeyVaultAdministrationError(error.getInnerError()));
            } else {
                return new KeyVaultAdministrationError("ServiceError", value.toString(), null);
            }
        }
    }

    /**
     * Maps a {@link Throwable} to {@link KeyVaultAdministrationException} if it's an instance of
     * {@link HttpResponseException}, else it returns the original throwable.
     *
     * @param throwable A {@link Throwable}.
     *
     * @return A {@link Throwable} that is either an instance of the public {@link KeyVaultAdministrationException} or the
     * original {@link Throwable}.
     */
    public static Throwable mapThrowableToKeyVaultAdministrationException(Throwable throwable) {
        if (throwable instanceof HttpResponseException) {
            return toKeyVaultAdministrationException((HttpResponseException) throwable);
        } else {
            return throwable;
        }
    }
}
