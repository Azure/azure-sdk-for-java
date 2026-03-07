// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.queryadvisor;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Singleton that loads and exposes the query advice rule messages bundled in
 * {@code query_advice_rules.json}.
 *
 * <p>The singleton is initialized lazily on first access and is safe for concurrent use.
 */
final class QueryAdviceRuleDirectory {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAdviceRuleDirectory.class);

    private static final String DEFAULT_URL_PREFIX = "https://aka.ms/CosmosDB/QueryAdvisor/";

    private static final String RULES_RESOURCE_PATH = "/query/queryadvisor/query_advice_rules.json";

    // Initialization-on-demand holder (thread-safe without synchronization)
    private static final class Holder {
        static final QueryAdviceRuleDirectory INSTANCE = new QueryAdviceRuleDirectory();
    }

    private final String urlPrefix;
    private final Map<String, RuleData> rules;

    /**
     * Returns the singleton instance.
     *
     * @return the singleton {@link QueryAdviceRuleDirectory}
     */
    static QueryAdviceRuleDirectory getInstance() {
        return Holder.INSTANCE;
    }

    private QueryAdviceRuleDirectory() {
        String loadedPrefix = DEFAULT_URL_PREFIX;
        Map<String, RuleData> loadedRules = null;

        try (InputStream is = QueryAdviceRuleDirectory.class.getResourceAsStream(RULES_RESOURCE_PATH)) {
            if (is != null) {
                RulesFile rulesFile = Utils.getSimpleObjectMapper().readValue(is, RulesFile.class);

                if (rulesFile.urlPrefix != null) {
                    loadedPrefix = rulesFile.urlPrefix;
                }

                loadedRules = rulesFile.rules;
            } else {
                LOGGER.warn("query_advice_rules.json not found on classpath; query advice messages will be unavailable.");
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load query_advice_rules.json; query advice messages will be unavailable.", e);
        }

        this.urlPrefix = loadedPrefix;
        this.rules = loadedRules != null ? loadedRules : Collections.emptyMap();
    }

    /**
     * Returns the URL prefix used to build documentation links for advice rules.
     *
     * @return URL prefix string (never {@code null})
     */
    String getUrlPrefix() {
        return this.urlPrefix;
    }

    /**
     * Returns the human-readable message for the given rule ID.
     *
     * @param ruleId the rule identifier, e.g. {@code "QA1000"}
     * @return the message string, or {@code null} if the rule is unknown
     */
    String getRuleMessage(String ruleId) {
        if (ruleId == null) {
            return null;
        }
        RuleData ruleData = this.rules.get(ruleId);
        if (ruleData == null) {
            LOGGER.debug("Unknown query advice rule ID '{}'; entry will be omitted.", ruleId);
        }
        return ruleData != null ? ruleData.message : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class RulesFile {
        @JsonProperty("url_prefix")
        String urlPrefix;

        @JsonProperty("rules")
        Map<String, RuleData> rules;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class RuleData {
        @JsonProperty("message")
        String message;
    }
}
