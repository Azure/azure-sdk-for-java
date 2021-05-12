// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.data.tables.implementation.models.TableServiceErrorOdataError;
import com.azure.data.tables.implementation.models.TableServiceErrorOdataErrorMessage;
import com.azure.data.tables.models.TableServiceError;
import com.azure.data.tables.models.TableServiceException;

/**
 * A class containing utility methods for the Azure Data Tables library.
 */
public final class TableUtils {
    private TableUtils() {
        throw new UnsupportedOperationException("Cannot instantiate TablesUtils");
    }

    /**
     * Convert an implementation {@link com.azure.data.tables.implementation.models.TableServiceError} to a public
     * {@link TableServiceError}. This function maps the service returned
     * {@link com.azure.data.tables.implementation.models.TableServiceErrorOdataError inner OData error} and its
     * contents to the top level {@link TableServiceError error}.
     *
     * @param tableServiceError The {@link com.azure.data.tables.implementation.models.TableServiceError} returned by
     * the service.
     *
     * @return The {@link TableServiceError} returned by the SDK.
     */
    public static TableServiceError toTableServiceError(
        com.azure.data.tables.implementation.models.TableServiceError tableServiceError) {

        String errorCode = null;
        String languageCode = null;
        String errorMessage = null;

        if (tableServiceError != null) {
            final TableServiceErrorOdataError odataError = tableServiceError.getOdataError();

            if (odataError != null) {
                errorCode = odataError.getCode();
                TableServiceErrorOdataErrorMessage odataErrorMessage = odataError.getMessage();

                if (odataErrorMessage != null) {
                    languageCode = odataErrorMessage.getLang();
                    errorMessage = odataErrorMessage.getValue();
                }
            }
        }

        return new TableServiceError(errorCode, languageCode, errorMessage);
    }

    /**
     * Convert an implementation {@link com.azure.data.tables.implementation.models.TableServiceErrorException} to a a
     * public {@link TableServiceException}.
     *
     * @param exception The {@link com.azure.data.tables.implementation.models.TableServiceErrorException}.
     *
     * @return The {@link TableServiceException} to be thrown.
     */
    public static TableServiceException toTableServiceException(
        com.azure.data.tables.implementation.models.TableServiceErrorException exception) {

        return new TableServiceException(exception.getMessage(), exception.getResponse(),
            toTableServiceError(exception.getValue()));
    }

    /**
     * Maps a {@link Throwable} to {@link TableServiceException} if it's an instance of
     * {@link com.azure.data.tables.implementation.models.TableServiceErrorException}, else it returns the original
     * throwable.
     *
     * @param throwable A throwable.
     *
     * @return A Throwable that is either an instance of {@link TableServiceException} or the original throwable.
     */
    public static Throwable mapThrowableToTableServiceException(Throwable throwable) {
        if (throwable instanceof com.azure.data.tables.implementation.models.TableServiceErrorException) {
            return toTableServiceException(
                (com.azure.data.tables.implementation.models.TableServiceErrorException) throwable);
        } else {
            return throwable;
        }
    }
}
