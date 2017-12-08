/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.microsoft.azure.cognitiveservices.spellcheck.implementation.SpellCheckInner;

/**
 * The Answer model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Answer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "SpellCheck", value = SpellCheckInner.class)
})
public class Answer extends Response {
}
