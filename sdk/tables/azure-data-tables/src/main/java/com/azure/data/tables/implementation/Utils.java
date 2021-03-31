package com.azure.data.tables.implementation;

import com.azure.data.tables.implementation.models.TableServiceErrorOdataError;
import com.azure.data.tables.implementation.models.TableServiceErrorOdataErrorMessage;
import com.azure.data.tables.models.TableServiceError;
import com.azure.data.tables.models.TableServiceErrorException;

public class Utils {
    /**
     * Convert an implementation {@link com.azure.data.tables.implementation.models.TableServiceError} to a public
     * {@link TableServiceError}. This function maps the service returned
     * {@link com.azure.data.tables.implementation.models.TableServiceErrorOdataError inner OData error} and its
     * contents to the top level {@link TableServiceError error}.
     *
     * @param tableServiceError The {@link com.azure.data.tables.implementation.models.TableServiceError} returned by
     * the service.
     * @return The {@link TableServiceError} returned by the SDK.
     */
    public static TableServiceError toTableServiceError(
        com.azure.data.tables.implementation.models.TableServiceError tableServiceError) {

        String errorCode = null;
        String languageCode = null;
        String errorMessage = null;

        final TableServiceErrorOdataError odataError = tableServiceError.getOdataError();

        if (odataError != null) {
            errorCode = odataError.getCode();
            TableServiceErrorOdataErrorMessage odataErrorMessage = odataError.getMessage();

            if (odataErrorMessage != null) {
                languageCode = odataErrorMessage.getLang();
                errorMessage = odataErrorMessage.getValue();
            }
        }

        return new TableServiceError(errorCode, languageCode, errorMessage);
    }

    /**
     * Convert an implementation {@link com.azure.data.tables.implementation.models.TableServiceErrorException} to a
     * a public {@link TableServiceErrorException}.
     *
     * @param exception The {@link com.azure.data.tables.implementation.models.TableServiceErrorException}.
     * @return The {@link TableServiceErrorException} to be thrown.
     */
    public static TableServiceErrorException toTableServiceErrorException(
        com.azure.data.tables.implementation.models.TableServiceErrorException exception) {

        return new TableServiceErrorException(exception.getMessage(), exception.getResponse(),
            toTableServiceError(exception.getValue()));
    }
}
