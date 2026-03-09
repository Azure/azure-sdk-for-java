// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.queryadvisor;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the collection of query advice entries returned by the Cosmos DB query advisor.
 */
public final class QueryAdvice {

    private static final Logger logger = LoggerFactory.getLogger(QueryAdvice.class);
    private final List<QueryAdviceEntry> entries;

    /**
     * Constructs a QueryAdvice from a list of entries, filtering out null values.
     *
     * @param entries list of {@link QueryAdviceEntry} objects (may contain nulls)
     */
    QueryAdvice(List<QueryAdviceEntry> entries) {
        List<QueryAdviceEntry> filtered = new ArrayList<>();
        if (entries != null) {
            for (QueryAdviceEntry entry : entries) {
                if (entry != null) {
                    filtered.add(entry);
                }
            }
        }
        this.entries = filtered;
    }

    /**
     * Gets the list of query advice entries.
     *
     * @return unmodifiable view of the entries (never {@code null})
     */
    List<QueryAdviceEntry> getEntries() {
        return this.entries;
    }

    /**
     * Formats all query advice entries as a multi-line human-readable string.
     *
     * @return formatted string with each entry on a separate line, or an empty string if there
     *         are no valid entries
     */
    @Override
    public String toString() {
        if (this.entries.isEmpty()) {
            return "";
        }

        QueryAdviceRuleDirectory ruleDirectory = QueryAdviceRuleDirectory.getInstance();
        StringBuilder sb = new StringBuilder();

        for (QueryAdviceEntry entry : this.entries) {
            String formatted = entry.toFormattedString(ruleDirectory);
            if (formatted != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(formatted);
            }
        }

        return sb.toString();
    }

    /**
     * Attempts to parse a {@link QueryAdvice} from the raw {@code x-ms-cosmos-query-advice}
     * response header value.
     *
     * <p>The header value is URL-encoded JSON that represents an array of query advice entries.
     * On any parse failure this method logs a warning and returns {@code null} to avoid
     * disrupting callers.
     *
     * @param responseHeader the raw (URL-encoded) response header value
     * @return a {@link QueryAdvice} if parsing succeeds; {@code null} otherwise
     */
    public static QueryAdvice tryCreateFromString(String responseHeader) {
        if (responseHeader == null || responseHeader.isEmpty()) {
            return null;
        }

        try {
            String decoded = URLDecoder.decode(responseHeader, StandardCharsets.UTF_8.toString());
            List<QueryAdviceEntry> entries =
                Utils.getSimpleObjectMapper().readValue(decoded, new TypeReference<List<QueryAdviceEntry>>() { });
            if (entries == null) {
                return null;
            }
            return new QueryAdvice(entries);
        } catch (Exception e) {
            logger.warn("Failed to parse query advice response header", e);
            return null;
        }
    }
}
