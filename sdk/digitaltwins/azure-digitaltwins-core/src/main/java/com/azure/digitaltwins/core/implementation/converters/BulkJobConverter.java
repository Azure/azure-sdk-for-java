// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.*;
import com.azure.digitaltwins.core.models.ErrorInformation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.EventRoute} and
 * {@link DigitalTwinsEventRoute}.
 */
public final class BulkJobConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.EventRoute} to
     * {@link DigitalTwinsEventRoute}. If the input is null, then the output will be null as well.
     */
    public static DigitalTwinsImportJob map(com.azure.digitaltwins.core.implementation.models.ImportJob input) {
        if (input == null) {
            return null;
        }

        return new DigitalTwinsImportJob(
            input.getId(),
            input.getInputBlobUri(),
            input.getOutputBlobUri(),
            Status.fromString(input.getStatus().toString()),
            input.getCreatedDateTime(),
            input.getLastActionDateTime(),
            input.getFinishedDateTime(),
            input.getPurgeDateTime(),
            mapError(input.getError()));
    }

    /**
     * Maps from {@link DigitalTwinsImportJob} to
     * {@link com.azure.digitaltwins.core.implementation.models.ImportJob}. If the input is null, then the output will be null as well.
     */
    public static com.azure.digitaltwins.core.implementation.models.ImportJob map(DigitalTwinsImportJob input) {
        if (input == null) {
            return null;
        }

        return new com.azure.digitaltwins.core.implementation.models.ImportJob(input.getInputBlobUri(), input.getOutputBlobUri());
    }

    public static ErrorInformation mapError(com.azure.digitaltwins.core.implementation.models.Error input) {
        if(input == null){
            return null;
        }
        ErrorInformation errorInformation = new ErrorInformation(input.getCode(), input.getMessage(), mapDetails(input.getDetails()));
        errorInformation.setInnerError(mapInnerError(input.getInnererror()));
        return errorInformation;
    }

    public static List<ErrorInformation> mapDetails(List<com.azure.digitaltwins.core.implementation.models.Error> inputList) {
        if(inputList == null){
            return null;
        }
        return inputList.stream()
            .map(BulkJobConverter::mapError)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static InnerError mapInnerError(com.azure.digitaltwins.core.implementation.models.InnerError input) {
        if(input == null){
            return null;
        }
        InnerError error = new InnerError();
        error.setCode(input.getCode());
        error.setInnererror(mapInnerError(input.getInnererror()));
        return error;
    }

    private BulkJobConverter() { }
}
