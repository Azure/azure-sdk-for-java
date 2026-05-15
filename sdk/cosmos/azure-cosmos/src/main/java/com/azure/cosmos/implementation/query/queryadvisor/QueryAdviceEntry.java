// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.queryadvisor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a single query advice entry returned by the Cosmos DB query advisor.
 * Each entry contains a rule ID and optional parameters.
 */
class QueryAdviceEntry {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Params")
    private List<String> parameters;

    // Default constructor for Jackson deserialization
    public QueryAdviceEntry() {
    }

    /**
     * Gets the rule identifier (e.g., "QA1000").
     *
     * @return the rule ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the optional list of parameters for the rule message.
     *
     * @return the parameters list, or {@code null} if none
     */
    public List<String> getParameters() {
        return this.parameters;
    }

    /**
     * Formats this entry as a human-readable string using the given rule directory.
     *
     * @param ruleDirectory the rule directory used to look up rule messages and URL prefix
     * @return a formatted string, or {@code null} if the rule is not found in the directory
     */
    String toFormattedString(QueryAdviceRuleDirectory ruleDirectory) {
        if (this.id == null) {
            return null;
        }

        String message = ruleDirectory.getRuleMessage(this.id);

        StringBuilder sb = new StringBuilder();
        sb.append(this.id).append(": ");

        if (message == null) {
            // Unknown rule ID - still surface the ID and a documentation link so the user
            // has something actionable even when the local rules file is outdated.
            sb.append("For more information, please visit ")
              .append(ruleDirectory.getUrlPrefix())
              .append(this.id);
            return sb.toString();
        }

        // Format message with parameters if available
        if (this.parameters != null && !this.parameters.isEmpty()) {
            try {
                sb.append(String.format(message, this.parameters.toArray()));
            } catch (Exception e) {
                // If formatting fails, use the message as-is
                sb.append(message);
            }
        } else {
            sb.append(message);
        }

        // Append documentation link
        sb.append(" For more information, please visit ")
          .append(ruleDirectory.getUrlPrefix())
          .append(this.id);

        return sb.toString();
    }
}
