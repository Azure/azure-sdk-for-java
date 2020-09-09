// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

/**
 * Defines values for TextAnalyticsErrorCode.
 */
@Immutable
public final class TextAnalyticsErrorCode extends ExpandableStringEnum<TextAnalyticsErrorCode> implements Serializable {
    private static final long serialVersionUID = 21436310107606058L;

    /**
     * Enum value InvalidRequest.
     */
    public static final TextAnalyticsErrorCode INVALID_REQUEST = fromString("InvalidRequest");

    /**
     * Enum value InvalidArgument.
     */
    public static final TextAnalyticsErrorCode INVALID_ARGUMENT = fromString("InvalidArgument");

    /**
     * Enum value InternalServerError.
     */
    public static final TextAnalyticsErrorCode INTERNAL_SERVER_ERROR = fromString("InternalServerError");

    /**
     * Enum value ServiceUnavailable.
     */
    public static final TextAnalyticsErrorCode SERVICE_UNAVAILABLE = fromString("ServiceUnavailable");

    /**
     * Enum value InvalidParameterValue.
     */
    public static final TextAnalyticsErrorCode INVALID_PARAMETER_VALUE = fromString("InvalidParameterValue");

    /**
     * Enum value InvalidRequestBodyFormat.
     */
    public static final TextAnalyticsErrorCode INVALID_REQUEST_BODY_FORMAT = fromString("InvalidRequestBodyFormat");

    /**
     * Enum value EmptyRequest.
     */
    public static final TextAnalyticsErrorCode EMPTY_REQUEST = fromString("EmptyRequest");

    /**
     * Enum value MissingInputRecords.
     */
    public static final TextAnalyticsErrorCode MISSING_INPUT_RECORDS = fromString("MissingInputRecords");

    /**
     * Enum value InvalidDocument.
     */
    public static final TextAnalyticsErrorCode INVALID_DOCUMENT = fromString("InvalidDocument");

    /**
     * Enum value ModelVersionIncorrect.
     */
    public static final TextAnalyticsErrorCode MODEL_VERSION_INCORRECT = fromString("ModelVersionIncorrect");

    /**
     * Enum value InvalidDocumentBatch.
     */
    public static final TextAnalyticsErrorCode INVALID_DOCUMENT_BATCH = fromString("InvalidDocumentBatch");

    /**
     * Enum value UnsupportedLanguageCode.
     */
    public static final TextAnalyticsErrorCode UNSUPPORTED_LANGUAGE_CODE = fromString("UnsupportedLanguageCode");

    /**
     * Enum value InvalidCountryHint.
     */
    public static final TextAnalyticsErrorCode INVALID_COUNTRY_HINT = fromString("InvalidCountryHint");

    /**
     * Creates or finds a TextAnalyticsErrorCode from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding TextAnalyticsErrorCode.
     */
    @JsonCreator
    public static TextAnalyticsErrorCode fromString(String name) {
        return fromString(name, TextAnalyticsErrorCode.class);
    }
}
