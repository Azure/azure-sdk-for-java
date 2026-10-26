// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Identity of the current test run, embedded into the name of every database created by the test
 * suite.
 * <p>
 * Cleanup on the long lived shared test accounts (thin client, GSI, canary) has to be able to tell
 * "resources my run created" apart from "resources another run is actively using", because several
 * matrix legs and pipeline runs share a single account. The run id is that discriminator.
 */
public final class CosmosTestRunId {

    private static final int MAX_LENGTH = 16;
    private static final int HASH_BYTES = 7;
    private static final String RUN_ID = computeRunId();
    private static final boolean IS_CI =
        System.getenv("BUILD_BUILDID") != null || System.getenv("SYSTEM_JOBID") != null;

    private CosmosTestRunId() {
    }

    /**
     * Returns the identifier of the current test run. Stable for the lifetime of the JVM and safe to
     * embed in a Cosmos resource id (lower case alphanumerics only, at most 16 characters).
     *
     * @return the run id.
     */
    public static String get() {
        return RUN_ID;
    }

    /**
     * @return true when running inside an Azure DevOps pipeline.
     */
    public static boolean isCi() {
        return IS_CI;
    }

    private static String computeRunId() {
        String explicit = System.getProperty("COSMOS.TEST_RUN_ID");
        if (StringUtils.isNotEmpty(explicit)) {
            return sanitize(explicit);
        }

        String buildId = StringUtils.defaultString(System.getenv("BUILD_BUILDID"), "");
        String attempt = StringUtils.defaultString(System.getenv("SYSTEM_JOBATTEMPT"), "1");

        // System.JobId is a GUID that is unique per job and stable for the whole job, including its post
        // steps, so the hash input differs for every concurrently running leg. The id is a truncated
        // digest, so a collision is not impossible - it is ~2^-56 per pair - but uniqueness no longer
        // depends on job display names happening to differ. That matters because a run scoped delete on a
        // shared account must never match another leg's id.
        String jobId = System.getenv("SYSTEM_JOBID");
        if (StringUtils.isNotEmpty(jobId)) {
            // The build id is carried along purely so a stray database can be traced back to a build.
            return compose(buildId + "x", shortHash(buildId + "|" + jobId + "|" + attempt));
        }

        if (StringUtils.isNotEmpty(buildId)) {
            // Fallback for agents that do not expose SYSTEM_JOBID. Uniqueness between concurrent legs of
            // one build then rests entirely on the hash, so the hash must survive truncation intact.
            String jobName = StringUtils.defaultString(System.getenv("SYSTEM_JOBDISPLAYNAME"),
                StringUtils.defaultString(System.getenv("AGENT_JOBNAME"), ""));
            return compose(buildId + "x", shortHash(buildId + "|" + jobName + "|" + attempt));
        }

        String user = StringUtils.defaultString(System.getProperty("user.name"), "dev");
        return compose("l" + user, shortHash(user + "|" + ProcessHandleCompat.currentPid()));
    }

    /**
     * Joins a human readable prefix and a uniqueness hash within {@link #MAX_LENGTH}.
     * <p>
     * The hash is never truncated - it is the only part that distinguishes concurrent runs, and losing
     * even a few characters of it makes two runs collide and delete each other's in-flight databases.
     * Only the readable prefix is trimmed, and from the end, so the leading digits of a build id (the
     * most significant ones) survive. Callers hash the whole input, prefix included, so that whatever is
     * trimmed here is still represented in the hash.
     */
    private static String compose(String readablePrefix, String hash) {
        String cleanedHash = clean(hash);
        String cleanedPrefix = clean(readablePrefix);
        int budget = Math.max(0, MAX_LENGTH - cleanedHash.length());
        String trimmedPrefix = cleanedPrefix.length() <= budget
            ? cleanedPrefix
            : cleanedPrefix.substring(0, budget);

        String composed = trimmedPrefix + cleanedHash;
        return composed.isEmpty() ? "unknown" : composed;
    }

    /**
     * Truncated SHA-256, rendered base36. Deliberately not CRC32: this hash is the only thing keeping two
     * concurrently running jobs from sharing a run id, and a run scoped delete that matched another job
     * would delete its in-flight databases. 56 bits keeps a collision negligible while staying short
     * enough to leave room for the readable build id.
     */
    private static String shortHash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));

            long truncated = 0L;
            for (int i = 0; i < HASH_BYTES; i++) {
                truncated = (truncated << 8) | (digest[i] & 0xFFL);
            }

            return Long.toString(truncated, 36);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every JRE, so this cannot happen.
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String sanitize(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) {
            cleaned = "unknown";
        }

        return cleaned.length() <= MAX_LENGTH ? cleaned : cleaned.substring(0, MAX_LENGTH);
    }

    private static String clean(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /**
     * Java 8 baseline compatible process id lookup.
     */
    private static final class ProcessHandleCompat {
        private static long currentPid() {
            String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            int atIndex = jvmName.indexOf('@');
            if (atIndex > 0) {
                try {
                    return Long.parseLong(jvmName.substring(0, atIndex));
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }

            return 0L;
        }
    }
}
