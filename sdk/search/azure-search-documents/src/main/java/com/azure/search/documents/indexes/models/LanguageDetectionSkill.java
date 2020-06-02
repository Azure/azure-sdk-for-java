// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A skill that detects the language of input text and reports a single
 * language code for every document submitted on the request. The language code
 * is paired with a score indicating the confidence of the analysis.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Text.LanguageDetectionSkill")
@Fluent
public final class LanguageDetectionSkill extends SearchIndexerSkill {
}
