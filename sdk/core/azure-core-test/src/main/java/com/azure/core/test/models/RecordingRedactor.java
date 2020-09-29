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
    private static final String REDACTED_UTF_8 = Base64.getEncoder().encodeToString(REDACTED.getBytes(StandardCharsets.UTF_8));

    private static final Pattern ACCESS_TOKEN_KEY_PATTERN = Pattern.compile("(?:\"accessToken\":\")(.*?)(?:\",|\"})");
    private static final Pattern AUTH_HEADER_KEY_PATTERN = Pattern.compile("(?:\"authHeader\":\")(.*?)(?:\",|\"})");
    private static final Pattern ACCOUNT_NAME_KEY_PATTERN = Pattern.compile("(?:\"accountName\":\")(.*?)(?:\",|\"})");
    private static final Pattern ACCOUNT_KEY_PATTERN = Pattern.compile("(?:\"accountKey\":\")(.*?)(?:\",|\"})");
    private static final Pattern APPLICATION_ID_KEY_PATTERN = Pattern.compile("(?:\"applicationId\":\")(.*?)(?:\",|\"})");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?:\"apiKey\":\")(.*?)(?:\",|\"})");
    private static final Pattern CONNECTION_STRING_KEY_PATTERN = Pattern.compile("(?:\"connectionString\":\")(.*?)(?:\",|\"})");
    private static final Pattern DELEGATIONKEY_KEY_PATTERN = Pattern.compile("(?:<Value>)(.*)(?:</Value>)");
    private static final Pattern DELEGATIONKEY_CLIENTID_PATTERN = Pattern.compile("(?:<SignedOid>)(.*)(?:</SignedOid>)");
    private static final Pattern DELEGATIONKEY_TENANTID_PATTERN = Pattern.compile("(?:<SignedTid>)(.*)(?:</SignedTid>)");
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("(?:\"url\":\")(.*?)(?:\",|\"})");
    private static final Pattern HOST_KEY_PATTERN = Pattern.compile("(?:\"host\":\")(.*?)(?:\",|\"})");
    private static final Pattern PASSWORD_KEY_PATTERN = Pattern.compile("(?:Password=)(.*?)(?:;)");
    private static final Pattern PWD_KEY_PATTERN = Pattern.compile("(?:\"password\":\")(.*?)(?:\",|\"})");
    private static final Pattern USERNAME_KEY_PATTERN = Pattern.compile("(?:\"userName\":\")(.*?)(?:\",|\"})");
    private static final Pattern USER_ID_KEY_PATTERN = Pattern.compile("(?:User ID=)(.*?)(?:;)");

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
        redactors.add(RecordingRedactor::redactJsonKeyPatterns);
        redactors.add(RecordingRedactor::redactUserDelegationKey);
        redactors.add(RecordingRedactor::redactUsernameKeyPatterns);
        redactors.add(RecordingRedactor::redactPasswordKeyPatterns);
        return redactors;
    }


    private static String redactAccessToken(String content) {
        content = redactionReplacement(content, ACCESS_TOKEN_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactUsernameKeyPatterns(String content) {
        content = redactionReplacement(content, USERNAME_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, USER_ID_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactPasswordKeyPatterns(String content) {
        content = redactionReplacement(content, PASSWORD_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, PWD_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactJsonKeyPatterns(String content) {
        content = redactionReplacement(content, AUTH_HEADER_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, ACCESS_TOKEN_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, ACCOUNT_NAME_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, ACCOUNT_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, API_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, APPLICATION_ID_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, CONNECTION_STRING_KEY_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, HTTP_URL_PATTERN.matcher(content), REDACTED);
        content = redactionReplacement(content, HOST_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactUserDelegationKey(String content) {
        if (content.contains("<UserDelegationKey>")) {
            content = redactionReplacement(content, DELEGATIONKEY_KEY_PATTERN.matcher(content), REDACTED_UTF_8);
            content = redactionReplacement(content, DELEGATIONKEY_CLIENTID_PATTERN.matcher(content), UUID.randomUUID().toString());
            content = redactionReplacement(content, DELEGATIONKEY_TENANTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        }

        return content;
    }

    private static String redactionReplacement(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            content = content.replace(matcher.group(1), replacement);
        }

        return content;
    }
}
