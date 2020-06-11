// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class used to redact the sensitive information when recording
 */
public class RecordingRedactor {
    private static final String REDACTED = "REDACTED";
    private static final String REDACTED_UTF_8 = Base64.getEncoder().encodeToString("REDACTED".getBytes(StandardCharsets.UTF_8));

    private static final Pattern ACCESS_TOKEN_KEY_PATTERN = Pattern.compile("(?:\"accessToken\":\")(.*?)(?:\",|\"})");
    private static final Pattern DELEGATIONKEY_KEY_PATTERN = Pattern.compile("(?:<Value>)(.*)(?:</Value>)");
    private static final Pattern DELEGATIONKEY_CLIENTID_PATTERN = Pattern.compile("(?:<SignedOid>)(.*)(?:</SignedOid>)");
    private static final Pattern DELEGATIONKEY_TENANTID_PATTERN = Pattern.compile("(?:<SignedTid>)(.*)(?:</SignedTid>)");

    private static final List<Function<String, String>> RECORDING_REDACTORS = loadRedactor();

    /**
     * Redact the sensitive information.
     *
     * @param redactableString the content that will be scan through
     * @return the redacted content
     */
    public String redact(String redactableString) {
        String redactedString = redactableString;
        for (Function<String, String> redactor : RECORDING_REDACTORS) {
            redactedString = redactor.apply(redactedString);
        }
        return redactedString;
    }

    private static List<Function<String, String>> loadRedactor() {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(RecordingRedactor::redactAccessToken);
        redactors.add(RecordingRedactor::redactUserDelegationKey);
        return redactors;
    }

    private static String redactAccessToken(String content) {
        content = redactionReplacement(content, ACCESS_TOKEN_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactUserDelegationKey(String content) {
        content = redactionReplacement(content, DELEGATIONKEY_KEY_PATTERN.matcher(content), REDACTED_UTF_8);
        content = redactionReplacement(content, DELEGATIONKEY_CLIENTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        content = redactionReplacement(content, DELEGATIONKEY_TENANTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        return content;
    }

    private static String redactionReplacement(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            content = content.replace(matcher.group(1), replacement);
        }

        return content;
    }
}
