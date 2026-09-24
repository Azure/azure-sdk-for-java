// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Fail.fail;

/**
 * Deletes the databases and containers created by this test run, and fails the run when a test left
 * resources behind.
 * <p>
 * Three nets, widest last:
 * <ol>
 *     <li>{@link CosmosTestResourceRegistry} contents, deleted at the end of the run.</li>
 *     <li>an account query for databases carrying this run's id, which catches resources created
 *     without going through the sanctioned helpers.</li>
 *     <li>a JVM shutdown hook, which is the only thing that runs when the JVM dies unexpectedly.</li>
 * </ol>
 * Databases belonging to other runs are never touched, so this is safe to run
 * against the long lived shared accounts where several matrix legs execute concurrently.
 * <p>
 * Registered alongside {@link CosmosNettyLeakDetectorFactory} in every {@code *-testng.xml} suite.
 */
public final class CosmosTestResourceJanitor implements IExecutionListener, IInvokedMethodListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosTestResourceJanitor.class);
    private static final String ENABLED_PROPERTY = "COSMOS.TEST_RESOURCE_JANITOR_ENABLED";
    private static final String FAIL_ON_LEAK_PROPERTY = "COSMOS.TEST_RESOURCE_JANITOR_FAIL_ON_LEAK";
    private static final Duration CLIENT_CLOSE_GRACE = Duration.ofSeconds(30);
    private static final Duration ACCOUNT_SWEEP_TIMEOUT = Duration.ofMinutes(5);

    private static final Object SHUTDOWN_HOOK_LOCK = new Object();
    private static volatile boolean shutdownHookRegistered = false;
    private static volatile boolean cleanupCompleted = false;

    @Override
    public void onExecutionStart() {
        if (!isEnabled()) {
            LOGGER.info("Cosmos test resource janitor is disabled via -D{}=false", ENABLED_PROPERTY);
            return;
        }

        registerShutdownHook();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        CosmosTestResourceRegistry.setCurrentTest(
            testResult.getTestClass().getName() + "." + method.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        CosmosTestResourceRegistry.setCurrentTest(null);
    }

    @Override
    public void onExecutionFinish() {
        if (!isEnabled() || cleanupCompleted) {
            // Guards against the listener being registered more than once for a suite; only the first
            // invocation does the work.
            return;
        }

        CleanupReport report;
        try {
            report = cleanup(/* failFast */ false);
        } finally {
            cleanupCompleted = true;
        }

        List<String> leaks = report.leaks;

        if (!report.completed) {
            // Never report an all clear here: cleanup did not finish, so "found nothing" and "did not
            // look" are indistinguishable. This is the normal outcome on suites that sever connectivity.
            LOGGER.warn("Cosmos test resource cleanup did not complete for run {} - resources may have been"
                + " left behind and the always-run pipeline post step is the only remaining backstop for"
                + " this run",
                CosmosTestRunId.get());
        } else if (leaks.isEmpty()) {
            LOGGER.info("No leaked Cosmos test resources for run {}{}",
                CosmosTestRunId.get(),
                supportsDatabaseQueries() ? "" : " (registry-only cleanup; account sweep not supported here)");
        }

        if (leaks.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder()
            .append("Cosmos test resources were leaked by run ")
            .append(CosmosTestRunId.get())
            .append(". Cleanup has attempted to delete them, but the tests that created them must delete")
            .append(" them themselves - see sdk/cosmos/AGENTS.md. Leaked:");
        for (String leak : leaks) {
            message.append(System.lineSeparator()).append("  - ").append(leak);
        }

        LOGGER.error(message.toString());

        // TestNG writes its reports before execution listeners run, so this never reaches the published
        // JUnit XML - the ADO Tests tab shows a fully green run on a red job. Raise an ADO issue so the
        // leak is visible where triage actually looks. Single line on purpose: ADO logging commands
        // terminate at the first newline, which would truncate the list mid-entry.
        boolean failing = shouldFailOnLeak();
        if (CosmosTestRunId.isCi()) {
            // Downgraded to a warning when the fail-on-leak escape hatch is off, so opting out of failing
            // also opts out of a red annotation.
            System.out.println("##vso[task.logissue type=" + (failing ? "error" : "warning") + "]"
                + "Cosmos test resources leaked by run " + CosmosTestRunId.get()
                + " - see the build log for the full list");
        }

        if (failing) {
            fail(message.toString());
        }
    }

    /**
     * Deletes everything this run created that is still around.
     *
     * @param failFast when true (shutdown hook path) skip the account wide sweeps and only delete what
     * the registry knows about, so the JVM is not held open by metadata queries during shutdown.
     * @return the leaked resources, and whether cleanup ran to completion.
     */
    private static CleanupReport cleanup(boolean failFast) {
        List<CosmosTestResourceRegistry.TrackedResource> tracked = CosmosTestResourceRegistry.leakedSnapshot();
        boolean shouldSweepAccount = !failFast && supportsDatabaseQueries();

        if (tracked.isEmpty() && !shouldSweepAccount) {
            return new CleanupReport(new ArrayList<>(), true);
        }

        Set<String> leaks = new LinkedHashSet<>();
        boolean completed = true;
        CosmosAsyncClient client = null;
        try {
            client = buildHouseKeepingClient();
            final CosmosAsyncClient cleanupClient = client;
            leaks.addAll(deleteTrackedResources(tracked, resource -> deleteTracked(cleanupClient, resource)));

            if (shouldSweepAccount) {
                SweepResult sweep = sweepAccount(cleanupClient);
                leaks.addAll(sweep.leaks);
                // Individual deletes inside the sweep are caught and logged, so a sweep can return
                // normally having deleted nothing because everything failed.
                completed &= sweep.complete;
            }
        } catch (Exception e) {
            completed = false;
            LOGGER.error("Cosmos test resource cleanup failed", e);
        } finally {
            CosmosTestResourceRegistry.clear();
            // On the timeout path the interrupted sweep worker may still be unwinding and can briefly
            // touch the client after this closes it. That is benign: the worker's interrupt flag survives,
            // so its remaining operations fail fast into deleteMatching's per-database catch, and
            // boundedElastic threads are daemons so none of this can hold the JVM open.
            closeQuietly(client);
        }

        return new CleanupReport(new ArrayList<>(leaks), completed);
    }

    /**
     * Decides what to delete and what counts as a leak. Split out from the account plumbing so it can be
     * unit tested with a fake outcome function - this ordering is load bearing and easy to break.
     * <p>
     * Databases are deleted first and the second loop skips containers underneath a database that is now
     * absent: once a database is gone so are its containers, and without this a long suite ends by issuing
     * hundreds of serial 404 probes against an account that is already throttling. The two loops must stay
     * separate passes - fusing them would make the skip set depend on registration order and silently
     * reintroduce the probe storm.
     *
     * @param tracked everything the registry still holds.
     * @param deleter performs the delete and reports what it found.
     * @return descriptions of the resources that had genuinely leaked.
     */
    static List<String> deleteTrackedResources(
        List<CosmosTestResourceRegistry.TrackedResource> tracked,
        Function<CosmosTestResourceRegistry.TrackedResource, DeleteOutcome> deleter) {

        List<String> leaks = new ArrayList<>();
        // "Absent by any means": DELETED and ALREADY_GONE both imply the containers are gone too.
        // DELETE_FAILED must not be here - those containers really are still there and were never tried.
        Set<String> absentDatabases = new HashSet<>();

        for (CosmosTestResourceRegistry.TrackedResource resource : tracked) {
            if (!resource.isDatabase()) {
                continue;
            }

            DeleteOutcome outcome = deleter.apply(resource);
            if (outcome == DeleteOutcome.DELETED || outcome == DeleteOutcome.ALREADY_GONE) {
                absentDatabases.add(resource.getDatabaseId());
            }
            if (outcome != DeleteOutcome.ALREADY_GONE) {
                leaks.add(describe(resource, outcome));
            }
        }

        for (CosmosTestResourceRegistry.TrackedResource resource : tracked) {
            if (resource.isDatabase() || absentDatabases.contains(resource.getDatabaseId())) {
                continue;
            }

            DeleteOutcome outcome = deleter.apply(resource);
            if (outcome != DeleteOutcome.ALREADY_GONE) {
                leaks.add(describe(resource, outcome));
            }
        }

        return leaks;
    }

    /**
     * Catches databases created without going through the registry, as long as they follow the naming
     * convention. Several tests create databases on their own clients and are only covered by this sweep,
     * so it must run even when the registry came back empty.
     * <p>
     * Bounded, because this runs at the very end of a job that may already be near its timeout and some
     * suites (manual-http-network-fault) deliberately sever connectivity. The result is collected on the
     * worker and returned, rather than written into a shared collection, so a timed-out sweep contributes
     * nothing and cannot race the caller.
     */
    private static SweepResult sweepAccount(CosmosAsyncClient client) {
        // The timeout deliberately propagates as an exception: that is what marks the cleanup incomplete
        // in cleanup(). Swallowing it here (onErrorReturn, or a catch inside this method) would restore
        // the false all-clear, where "found nothing" and "never looked" become indistinguishable - and no
        // test would catch that.
        return Mono.fromCallable(() -> {
            List<String> swept = new ArrayList<>();

            CosmosDatabaseForTest.CleanupResult runScoped =
                CosmosDatabaseForTest.cleanupDatabasesForCurrentRun(new JanitorDatabaseManager(client));
            for (String databaseId : runScoped.getDeletedDatabaseIds()) {
                swept.add("database " + databaseId + " (deleted; created by <not registered>)");
            }

            // Deliberately no age based sweep here: a test run only ever deletes its own resources, so it
            // can never race a concurrently executing leg on the same shared account.
            return new SweepResult(swept, runScoped.isComplete());
        }).subscribeOn(Schedulers.boundedElastic()).timeout(ACCOUNT_SWEEP_TIMEOUT).block();
    }

    private static final class SweepResult {
        private final List<String> leaks;
        private final boolean complete;

        private SweepResult(List<String> leaks, boolean complete) {
            this.leaks = leaks;
            this.complete = complete;
        }
    }

    private static String describe(CosmosTestResourceRegistry.TrackedResource resource, DeleteOutcome outcome) {
        return outcome == DeleteOutcome.DELETED
            ? resource + " - deleted"
            : resource + " - STILL PRESENT, delete failed";
    }

    private static DeleteOutcome deleteTracked(
        CosmosAsyncClient client,
        CosmosTestResourceRegistry.TrackedResource resource) {

        try {
            if (resource.isDatabase()) {
                client.getDatabase(resource.getDatabaseId()).delete().block();
            } else {
                client.getDatabase(resource.getDatabaseId())
                    .getContainer(resource.getContainerId())
                    .delete()
                    .block();
            }

            LOGGER.warn("Deleted leaked {}", resource);
            return DeleteOutcome.DELETED;
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                // The test cleaned up but did not deregister - not a leak.
                return DeleteOutcome.ALREADY_GONE;
            }

            LOGGER.error("Failed to delete leaked {}", resource, e);
            return DeleteOutcome.DELETE_FAILED;
        } catch (Exception e) {
            LOGGER.error("Failed to delete leaked {}", resource, e);
            return DeleteOutcome.DELETE_FAILED;
        }
    }

    enum DeleteOutcome {
        /** The resource was still present and this cleanup deleted it - the test leaked it. */
        DELETED,
        /** The resource was already gone - the test deleted it but did not deregister. */
        ALREADY_GONE,
        /** The resource is still present and could not be deleted here. */
        DELETE_FAILED
    }

    private static final class CleanupReport {
        private final List<String> leaks;
        private final boolean completed;

        private CleanupReport(List<String> leaks, boolean completed) {
            this.leaks = leaks;
            this.completed = completed;
        }
    }

    private static void registerShutdownHook() {
        synchronized (SHUTDOWN_HOOK_LOCK) {
            if (shutdownHookRegistered) {
                return;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (cleanupCompleted) {
                    return;
                }

                LOGGER.warn("JVM is shutting down before the test run finished - "
                    + "attempting best effort cleanup of Cosmos test resources");
                cleanup(/* failFast */ true);
            }, "cosmos-test-resource-janitor"));

            shutdownHookRegistered = true;
        }
    }

    private static CosmosAsyncClient buildHouseKeepingClient() {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        // Metadata operations get throttled with 429/3200 ("high rate of metadata requests") when many
        // legs clean up at once; the SDK default of 9 attempts is not enough for a bulk delete.
        retryOptions.setMaxRetryAttemptsOnThrottledRequests(200);
        retryOptions.setMaxRetryWaitTime(Duration.ofMinutes(5));

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .throttlingRetryOptions(retryOptions)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
    }

    private static void closeQuietly(CosmosAsyncClient client) {
        if (client == null) {
            return;
        }

        try {
            Mono.fromRunnable(client::close).timeout(CLIENT_CLOSE_GRACE).onErrorResume(t -> Mono.empty()).block();
        } catch (Exception e) {
            LOGGER.warn("Failed to close the janitor client", e);
        }
    }

    private static boolean isEnabled() {
        return !"false".equalsIgnoreCase(System.getProperty(ENABLED_PROPERTY));
    }

    private static boolean shouldFailOnLeak() {
        return !"false".equalsIgnoreCase(System.getProperty(FAIL_ON_LEAK_PROPERTY));
    }

    /**
     * The vNext emulator does not implement querying databases, so the account wide sweeps are skipped
     * there - the registry based cleanup still applies. Mirrors the carve out in
     * {@code TestSuiteBase.afterSuitEmulatorVNext}.
     */
    private static boolean supportsDatabaseQueries() {
        return !Boolean.parseBoolean(System.getProperty("COSMOS.EMULATOR_VNEXT_ENABLED", "false"));
    }

    private static final class JanitorDatabaseManager implements CosmosDatabaseForTest.DatabaseManager {
        private final CosmosAsyncClient client;

        private JanitorDatabaseManager(CosmosAsyncClient client) {
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
