// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class used to redact the sensitive information when recording
 */
public class RecordingRedactor {
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REDACTED_UTF_8 =  Base64.getEncoder().encodeToString("REDACTED".getBytes(StandardCharsets.UTF_8));
    private static final String USER_DELEGATION_KEY = "UserDelegationKey";

    private static final Pattern ACCESS_TOKEN_KEY_PATTERN = Pattern.compile("(?:\"accessToken\":\")(.*?)(?:\",|\"})");
    private static final Pattern DELEGATIONKEY_KEY_PATTERN = Pattern.compile("(?:<Value>)(.*)(?:</Value>)");
    private static final Pattern DELEGATIONKEY_CLIENTID_PATTERN = Pattern.compile("(?:<SignedOid>)(.*)(?:</SignedOid>)");
    private static final Pattern DELEGATIONKEY_TENANTID_PATTERN = Pattern.compile("(?:<SignedTid>)(.*)(?:</SignedTid>)");

    private static final String[] STOP_WORDS = {ACCESS_TOKEN, USER_DELEGATION_KEY};

    /**
     * Redact the sensitive information.
     *
     * @param redactableString the content that will be scan through
     * @return the redacted content
     */
    public String redact(String redactableString) {

        for (String stopWord : STOP_WORDS) {
            if (redactableString.contains(stopWord)) {
                switch (stopWord) {
                    case USER_DELEGATION_KEY:
                        redactableString = redactUserDelegationKey(redactableString);
                        break;
                    case ACCESS_TOKEN:
                        redactableString = redactAccessToken(redactableString);
                        break;
                    default:
                        break;
                }
            }
        }
        return redactableString;
    }

    private String redactAccessToken(String content) {
        content = redactionReplacement(content, ACCESS_TOKEN_KEY_PATTERN.matcher(content), REDACTED_UTF_8);
        return content;
    }

    private String redactUserDelegationKey(String content) {
        content = redactionReplacement(content, DELEGATIONKEY_KEY_PATTERN.matcher(content), REDACTED_UTF_8);
        content = redactionReplacement(content, DELEGATIONKEY_CLIENTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        content = redactionReplacement(content, DELEGATIONKEY_TENANTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        return content;
    }

    private String redactionReplacement(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            content = content.replace(matcher.group(1), replacement);
        }

        return content;
    }
}
