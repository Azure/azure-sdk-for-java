// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Generates unique IDs with a category prefix, matching the Foundry ID format:
 * {@code {prefix}_{partitionKey}{entropy}}.
 */
public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PARTITION_KEY_LENGTH = 18;
    private static final int ENTROPY_LENGTH = 32;

    /**
     * Prefix for Response resource IDs, per the protocol prefix table.
     * Response IDs have the form {@code caresp_{partitionKey}{entropy}}.
     */
    static final String RESPONSE_PREFIX = "caresp";

    /**
     * Pattern for a well-formed response ID: the {@code caresp} prefix, a separator,
     * and a 50-character Base62 body (18-char partition key + 32-char entropy), per
     * the protocol ID format.
     */
    private static final Pattern RESPONSE_ID_PATTERN =
        Pattern.compile(RESPONSE_PREFIX + "_[A-Za-z0-9]{" + (PARTITION_KEY_LENGTH + ENTROPY_LENGTH) + "}");

    /**
     * Returns {@code true} if {@code id} is a well-formed response ID
     * ({@code caresp_} + 50 Base62 characters). Used to validate path parameters
     * before lookup.
     *
     * @param id the candidate response ID
     * @return whether the ID is well-formed
     */
    public static boolean isValidResponseId(String id) {
        return id != null && RESPONSE_ID_PATTERN.matcher(id).matches();
    }

    private final String partitionKey;

    public IdGenerator(String partitionKey) {
        this.partitionKey = partitionKey != null ? partitionKey : secureEntropy(PARTITION_KEY_LENGTH);
    }

    /**
     * Extracts the partition key from an existing Foundry-format ID.
     *
     * @param id the existing ID (e.g. "caresp_AbCdEf...")
     * @return the partition key portion
     */
    public static String extractPartitionKey(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        int underscoreIdx = id.indexOf('_');
        if (underscoreIdx < 0 || id.length() < underscoreIdx + 1 + PARTITION_KEY_LENGTH) {
            throw new IllegalArgumentException("ID '" + id + "' does not contain a valid partition key");
        }
        return id.substring(underscoreIdx + 1, underscoreIdx + 1 + PARTITION_KEY_LENGTH);
    }

    private static String secureEntropy(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // Keep only alphanumeric characters and truncate to desired length
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < base64.length() && sb.length() < length; i++) {
            char c = base64.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        // If not enough chars (unlikely), pad with more random
        while (sb.length() < length) {
            RANDOM.nextBytes(bytes);
            base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            for (int i = 0; i < base64.length() && sb.length() < length; i++) {
                char c = base64.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Generates a new ID with the given category prefix.
     *
     * @param category the prefix, e.g. "msg", "resp", "conv", "func"
     * @return a new unique ID string
     */
    public String generate(String category) {
        String prefix = (category == null || category.isEmpty()) ? "id" : category;
        return prefix + "_" + partitionKey + secureEntropy(ENTROPY_LENGTH);
    }

    /**
     * Generates a new Response resource ID using the {@code caresp} prefix.
     *
     * @return a new unique response ID string, e.g. {@code caresp_...}
     */
    public String generateResponseId() {
        return generate(RESPONSE_PREFIX);
    }

    public String generateMessageItemId() {
        return generate("msg");
    }

    public String generateFunctionCallItemId() {
        return generate("fc");
    }

    public String generateReasoningItemId() {
        return generate("rs");
    }

    public String generateFileSearchCallItemId() {
        return generate("fs");
    }

    public String generateWebSearchCallItemId() {
        return generate("ws");
    }

    public String generateCodeInterpreterCallItemId() {
        return generate("ci");
    }

    public String generateImageGenCallItemId() {
        return generate("ig");
    }

    public String generateMcpCallItemId() {
        return generate("mcp");
    }

    public String generateMcpListToolsItemId() {
        return generate("mcplt");
    }

    public String generateCustomToolCallItemId() {
        return generate("ct");
    }
}

