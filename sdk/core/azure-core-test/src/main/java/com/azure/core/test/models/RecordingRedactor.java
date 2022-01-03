// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.util.CoreUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
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

    private static final Pattern DELEGATIONKEY_KEY_PATTERN = Pattern.compile("(?:<Value>)(.*)(?:</Value>)");
    private static final Pattern DELEGATIONKEY_CLIENTID_PATTERN = Pattern.compile("(?:<SignedOid>)(.*)(?:</SignedOid>)");
    private static final Pattern DELEGATIONKEY_TENANTID_PATTERN = Pattern.compile("(?:<SignedTid>)(.*)(?:</SignedTid>)");
    private static final Pattern PASSWORD_KEY_PATTERN = Pattern.compile("(?:Password=)(.*?)(?:;)");
    private static final Pattern USER_ID_KEY_PATTERN = Pattern.compile("(?:User ID=)(.*?)(?:;)");
    private static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile("(?:<PrimaryKey>)(.*)(?:</PrimaryKey>)");
    private static final Pattern SECONDARY_KEY_PATTERN = Pattern.compile("(?:<SecondaryKey>)(.*)(?:</SecondaryKey>)");

    private static final List<Function<String, String>> DEFAULT_RECORDING_REDACTORS = loadRedactor();

    private final List<Function<String, String>> recordingRedactors = new ArrayList<>();

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("authHeader")
        .add("accountKey")
        .add("accessToken")
        .add("accountName")
        .add("applicationId")
        .add("apiKey")
        .add("connectionString")
        .add("url")
        .add("host")
        .add("password")
        .add("userName");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()),
        Pattern.CASE_INSENSITIVE);

    /**
     * Creates an instance of {@link RecordingRedactor} with a default set of redactors.
     */
    public RecordingRedactor() {
        this(Collections.emptyList());
    }

    /**
     * Creates an instance of {@link RecordingRedactor} with a default set of redactors and additionally include the
     * provided list of custom redactor functions.
     *
     * @param customRedactors A list of custom redactor functions.
     */
    public RecordingRedactor(List<Function<String, String>> customRedactors) {
        this.recordingRedactors.addAll(DEFAULT_RECORDING_REDACTORS);
        this.recordingRedactors.addAll(customRedactors == null ? Collections.emptyList() : customRedactors);
    }

    /**
     * Redact the sensitive information.
     *
     * @param redactableString the content that will be scan through
     * @return the redacted content
     */
    public String redact(String redactableString) {
        String redactedString = redactableString;
        for (Function<String, String> redactor : recordingRedactors) {
            redactedString = redactor.apply(redactedString);
        }
        return redactedString;
    }

    private static List<Function<String, String>> loadRedactor() {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(RecordingRedactor::redactJsonKeyPatterns);
        redactors.add(RecordingRedactor::redactUserDelegationKey);
        redactors.add(RecordingRedactor::redactUsernameKeyPatterns);
        redactors.add(RecordingRedactor::redactPasswordKeyPatterns);
        redactors.add(RecordingRedactor::redactPrimaryKeyPatterns);
        redactors.add(RecordingRedactor::redactSecondaryKeyPatterns);
        return redactors;
    }

    private static String redactUsernameKeyPatterns(String content) {
        content = redactionReplacement(content, USER_ID_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactPasswordKeyPatterns(String content) {
        content = redactionReplacement(content, PASSWORD_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactJsonKeyPatterns(String content) {
        content = redactionReplacement(content, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactPrimaryKeyPatterns(String content) {
        content = redactionReplacement(content, PRIMARY_KEY_PATTERN.matcher(content), REDACTED);
        return content;
    }

    private static String redactSecondaryKeyPatterns(String content) {
        content = redactionReplacement(content, SECONDARY_KEY_PATTERN.matcher(content), REDACTED);
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
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), replacement);
            }
        }

        return content;
    }
}
