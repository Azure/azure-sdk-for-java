/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * The Answer model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Answer.class)
@JsonTypeName("Answer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Computation", value = Computation.class),
    @JsonSubTypes.Type(name = "SearchResultsAnswer", value = SearchResultsAnswer.class)
})
public class Answer extends Response {
    /**
     * The followUpQueries property.
     */
    @JsonProperty(value = "followUpQueries", access = JsonProperty.Access.WRITE_ONLY)
    private List<Query> followUpQueries;

    /**
     * Get the followUpQueries value.
     *
     * @return the followUpQueries value
     */
    public List<Query> followUpQueries() {
        return this.followUpQueries;
    }

}
