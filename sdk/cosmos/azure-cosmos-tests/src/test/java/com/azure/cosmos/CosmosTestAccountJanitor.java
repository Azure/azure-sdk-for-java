// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone cleanup entry point for the long lived shared test accounts.
 * <p>
 * The in-process janitor cannot help when a CI job is cancelled or times out - the JVM is killed
 * before any listener or shutdown hook runs. This class is invoked from the pipeline instead:
 * <ul>
 *     <li>as an always-run post step of a test stage, with {@code --run-id} set to that job's run id,
 *     to delete exactly what the job created;</li>
 *     <li>from the scheduled janitor pipeline, with {@code --older-than}, to sweep orphans left by
 *     jobs that died before their post step could run.</li>
 * </ul>
 * Only databases whose id follows the test naming convention are ever deleted, so hand created
 * fixtures on these accounts are safe.
 * <p>
 * Usage:
 * <pre>
 * mvn -f sdk/cosmos/azure-cosmos-tests/pom.xml exec:java \
 *   -Dexec.mainClass=com.azure.cosmos.CosmosTestAccountJanitor \
 *   -Dexec.classpathScope=test \
 *   -Dexec.args="--account-host &lt;uri&gt; --account-key &lt;key&gt; --older-than 8"
 * </pre>
 */
public final class CosmosTestAccountJanitor {

    private static final int EXIT_BAD_ARGS = 2;
    private static final int EXIT_FAILED = 1;

    private CosmosTestAccountJanitor() {
    }

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            // exec:java runs in the Maven JVM, so only exit explicitly when the caller needs a failure.
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        Map<String, String> parsed;
        try {
            parsed = parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            printUsage();
            return EXIT_BAD_ARGS;
        }

        String host = parsed.get("account-host");
        String key = parsed.get("account-key");
        String runId = parsed.get("run-id");
        String olderThan = parsed.get("older-than");

        if (host == null || key == null) {
            System.err.println("--account-host and --account-key are required");
            printUsage();
            return EXIT_BAD_ARGS;
        }

        // An unresolved pipeline variable arrives as the literal "$(name)". Without this check the client
        // build throws, continueOnError swallows it, and a misconfigured janitor pipeline reports success
        // while sweeping nothing.
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            System.err.println("--account-host must be an http(s) URI but was: " + host);
            return EXIT_BAD_ARGS;
        }

        if (runId != null && olderThan != null) {
            System.err.println("--run-id and --older-than are mutually exclusive");
            printUsage();
            return EXIT_BAD_ARGS;
        }

        if (olderThan == null && runId == null) {
            // Post step of a test job: the same job environment produces the same run id the tests used.
            runId = CosmosTestRunId.get();
        }

        int exitCode = 0;
        try (CosmosAsyncClient client = buildClient(host, key)) {
            CosmosDatabaseForTest.DatabaseManager manager = new CliDatabaseManager(client);

            CosmosDatabaseForTest.CleanupResult result;
            if (olderThan == null) {
                System.out.println("Deleting test databases for run " + runId + " on " + host);
                result = CosmosDatabaseForTest.cleanupDatabasesForRun(manager, runId);
            } else {
                Duration threshold = Duration.ofHours(Long.parseLong(olderThan));
                System.out.println("Deleting test databases older than " + threshold + " on " + host);
                result = CosmosDatabaseForTest.cleanupTestDatabasesOlderThan(manager, threshold);
            }

            result.getDeletedDatabaseIds().forEach(id -> System.out.println("  deleted " + id));

            if (result.isComplete()) {
                System.out.println("Cleanup completed, deleted "
                    + result.getDeletedDatabaseIds().size() + " database(s)");
            } else {
                // Exit non-zero so a sweep that could not delete what it found is not mistaken for a clean
                // account. The pipeline steps set continueOnError, so this surfaces as SucceededWithIssues
                // rather than failing the job - visible in the UI, but it does not block the run.
                System.err.println("Cleanup incomplete: " + result.getFailureCount()
                    + " database(s) could not be deleted");
                exitCode = EXIT_FAILED;
            }
        } catch (Exception e) {
            // Cleanup is best effort - report loudly, but let the caller decide whether to fail the job.
            System.err.println("Cleanup failed: " + e);
            e.printStackTrace();
            exitCode = EXIT_FAILED;
        }

        return exitCode;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsed = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for " + arg);
            }

            parsed.put(arg.substring(2), args[++i]);
        }

        return parsed;
    }

    private static CosmosAsyncClient buildClient(String host, String key) {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        retryOptions.setMaxRetryAttemptsOnThrottledRequests(200);
        retryOptions.setMaxRetryWaitTime(Duration.ofMinutes(5));

        return new CosmosClientBuilder()
            .endpoint(host)
            .key(key)
            .gatewayMode()
            .throttlingRetryOptions(retryOptions)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
    }

    private static void printUsage() {
        System.err.println("Usage: CosmosTestAccountJanitor --account-host <uri> --account-key <key>"
            + " [--run-id <id> | --older-than <hours>]");
        System.err.println("  --run-id     delete databases created by that run. Defaults to the run id of"
            + " the current job environment, which is what a test job post step wants.");
        System.err.println("  --older-than delete test databases older than the given number of hours,"
            + " regardless of which run created them.");
    }

    private static final class CliDatabaseManager implements CosmosDatabaseForTest.DatabaseManager {
        private final CosmosAsyncClient client;

        private CliDatabaseManager(CosmosAsyncClient client) {
            this.client = client;
        }

        @Override
        public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }


        @Override
        public CosmosAsyncDatabase getDatabase(String id) {
            return client.getDatabase(id);
        }
    }
}
