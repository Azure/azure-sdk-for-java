// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for TextAnalyticsErrorCode.
 */
public final class TextAnalyticsErrorCode extends ExpandableStringEnum<TextAnalyticsErrorCode> {
    /**
     * Enum value invalidRequest.
     */
    public static final TextAnalyticsErrorCode INVALID_REQUEST = fromString("invalidRequest");

    /**
     * Enum value invalidArgument.
     */
    public static final TextAnalyticsErrorCode INVALID_ARGUMENT = fromString("invalidArgument");

    /**
     * Enum value internalServerError.
     */
    public static final TextAnalyticsErrorCode INTERNAL_SERVER_ERROR = fromString("internalServerError");

    /**
     * Enum value serviceUnavailable.
     */
    public static final TextAnalyticsErrorCode SERVICE_UNAVAILABLE = fromString("serviceUnavailable");

    /**
     * Enum value invalidParameterValue.
     */
    public static final TextAnalyticsErrorCode INVALID_PARAMETER_VALUE = fromString("invalidParameterValue");

    /**
     * Enum value invalidRequestBodyFormat.
     */
    public static final TextAnalyticsErrorCode INVALID_REQUEST_BODY_FORMAT = fromString("invalidRequestBodyFormat");

    /**
     * Enum value emptyRequest.
     */
    public static final TextAnalyticsErrorCode EMPTY_REQUEST = fromString("emptyRequest");

    /**
     * Enum value missingInputRecords.
     */
    public static final TextAnalyticsErrorCode MISSING_INPUT_RECORDS = fromString("missingInputRecords");

    /**
     * Enum value invalidDocument.
     */
    public static final TextAnalyticsErrorCode INVALID_DOCUMENT = fromString("invalidDocument");

    /**
     * Enum value modelVersionIncorrect.
     */
    public static final TextAnalyticsErrorCode MODEL_VERSION_INCORRECT = fromString("modelVersionIncorrect");

    /**
     * Enum value invalidDocumentBatch.
     */
    public static final TextAnalyticsErrorCode INVALID_DOCUMENT_BATCH = fromString("invalidDocumentBatch");

    /**
     * Enum value unsupportedLanguageCode.
     */
    public static final TextAnalyticsErrorCode UNSUPPORTED_LANGUAGE_CODE = fromString("unsupportedLanguageCode");

    /**
     * Enum value invalidCountryHint.
     */
    public static final TextAnalyticsErrorCode INVALID_COUNTRY_HINT = fromString("invalidCountryHint");

    /**
     * Creates or finds a TextAnalyticsErrorCode from its string representation.
     *
     * @param name the string name to look for.
     * @return the corresponding TextAnalyticsErrorCode.
     */
    @JsonCreator
    public static TextAnalyticsErrorCode fromString(String name) {
        return fromString(name, TextAnalyticsErrorCode.class);
    }
}
