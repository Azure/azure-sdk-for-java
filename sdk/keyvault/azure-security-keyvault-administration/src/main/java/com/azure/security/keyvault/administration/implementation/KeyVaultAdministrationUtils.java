// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation;

import com.azure.security.keyvault.administration.implementation.models.Error;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultError;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
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
     * Convert a {@link KeyVaultErrorException} to a {@link KeyVaultAdministrationException}.
     *
     * @param exception The {@link KeyVaultErrorException}.
     *
     * @return An instance of the public {@link KeyVaultAdministrationException}.
     */
    public static KeyVaultAdministrationException toKeyVaultAdministrationException(KeyVaultErrorException exception) {
        if (exception == null) {
            return null;
        }

        return new KeyVaultAdministrationException(exception.getMessage(), exception.getResponse(),
            toKeyVaultError(exception.getValue()));
    }

    /**
     * Convert an implementation {@link KeyVaultError} to a public {@link KeyVaultAdministrationError}.
     *
     * @param keyVaultError The {@link KeyVaultError} returned by the service.
     *
     * @return An instance of the public {@link KeyVaultAdministrationError}.
     */
    public static KeyVaultAdministrationError toKeyVaultError(KeyVaultError keyVaultError) {

        if (keyVaultError == null) {
            return null;
        }

        return createKeyVaultErrorFromError(keyVaultError.getError());
    }

    /**
     * Convert an error {@link Error} internal to an implementation {@link KeyVaultError} to a public
     * {@link KeyVaultAdministrationError}.
     *
     * @param error The {@link Error} internal to an implementation {@link KeyVaultError} returned by the service.
     *
     * @return An instance of the public {@link KeyVaultAdministrationError}.
     */
    public static KeyVaultAdministrationError createKeyVaultErrorFromError(Error error) {
        if (error == null) {
            return null;
        }

        return new KeyVaultAdministrationError(error.getCode(), error.getMessage(),
            createKeyVaultErrorFromError(error.getInnerError()));
    }

    /**
     * Maps a {@link Throwable} to {@link KeyVaultAdministrationException} if it's an instance of
     * {@link KeyVaultErrorException}, else it returns the original throwable.
     *
     * @param throwable A {@link Throwable}.
     *
     * @return A {@link Throwable} that is either an instance of the public {@link KeyVaultAdministrationException} or the
     * original {@link Throwable}.
     */
    public static Throwable mapThrowableToKeyVaultAdministrationException(Throwable throwable) {
        if (throwable instanceof KeyVaultErrorException) {
            return toKeyVaultAdministrationException((KeyVaultErrorException) throwable);
        } else {
            return throwable;
        }
    }
}
