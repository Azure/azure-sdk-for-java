// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.queryadvisor;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private final Map<String, Map<String, String>> rules;

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
        Map<String, Map<String, String>> loadedRules = null;

        try (InputStream is = QueryAdviceRuleDirectory.class.getResourceAsStream(RULES_RESOURCE_PATH)) {
            if (is != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] bytes = baos.toByteArray();
                String json = new String(bytes, StandardCharsets.UTF_8);

                Map<String, Object> root = Utils.getSimpleObjectMapper().readValue(
                    json,
                    new TypeReference<Map<String, Object>>() { });

                Object prefixObj = root.get("url_prefix");
                if (prefixObj instanceof String) {
                    loadedPrefix = (String) prefixObj;
                }

                Object rulesObj = root.get("rules");
                if (rulesObj != null) {
                    // Re-serialize and deserialize to get the typed Map<String, Map<String,String>>
                    String rulesJson = Utils.getSimpleObjectMapper().writeValueAsString(rulesObj);
                    loadedRules = Utils.getSimpleObjectMapper().readValue(
                        rulesJson,
                        new TypeReference<Map<String, Map<String, String>>>() { });
                }
            } else {
                LOGGER.warn("query_advice_rules.json not found on classpath; query advice messages will be unavailable.");
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load query_advice_rules.json; query advice messages will be unavailable.", e);
        }

        this.urlPrefix = loadedPrefix;
        this.rules = loadedRules;
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
        if (this.rules == null || ruleId == null) {
            return null;
        }
        Map<String, String> ruleData = this.rules.get(ruleId);
        if (ruleData == null) {
            return null;
        }
        return ruleData.get("message");
    }
}
