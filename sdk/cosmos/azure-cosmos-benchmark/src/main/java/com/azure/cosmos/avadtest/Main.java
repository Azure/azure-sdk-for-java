package com.azure.cosmos.avadtest;

import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.health.HealthMonitor;
import com.azure.cosmos.avadtest.health.HealthServer;
import com.azure.cosmos.avadtest.ingestor.Ingestor;
import com.azure.cosmos.avadtest.reader.AvadReader;
import com.azure.cosmos.avadtest.reader.LatestVersionReader;
import com.azure.cosmos.avadtest.reconciliation.Reconciler;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Parameter(names = "--mode", required = true,
        description = "Mode: ingestor, lv-reader, avad-reader, reconcile, health-monitor")
    private String mode;

    @Parameter(names = "--source", description = "Source to reconcile from (e.g., ingestor)")
    private String reconcileSource;

    @Parameter(names = "--against", description = "Source to reconcile against (e.g., cfp-avad)")
    private String reconcileAgainst;

    @Parameter(names = "--full", description = "Run full reconciliation suite")
    private boolean reconcileFull;

    @Parameter(names = "--health-port", description = "Health server port (default: 8080)")
    private int healthPort = 8080;

    @Parameter(names = "--gap-sla-minutes", description = "Minutes before an unconsumed event is flagged as a gap")
    private int gapSlaMinutes = 10;

    @Parameter(names = "--config", description = "Path to JSON config file (env vars override JSON values)")
    private String configFile;

    @Parameter(names = {"-h", "--help"}, description = "Help", help = true)
    private boolean help;

    private TestConfig loadConfig() throws Exception {
        if (configFile != null) {
            log.info("Loading config from: {}", configFile);
            return TestConfig.fromJson(configFile);
        }
        return TestConfig.fromEnv();
    }

    private int run() throws Exception {
        log.info("Starting cosmos-avad-test in mode: {}", mode);

        switch (mode) {
            case "ingestor":
                return runIngestor();
            case "lv-reader":
                return runLvReader();
            case "avad-reader":
                return runAvadReader();
            case "reconcile":
                return runReconcile();
            case "health-monitor":
                return runHealthMonitor();
            default:
                log.error("Unknown mode: {}. Use: ingestor, lv-reader, avad-reader, reconcile, health-monitor", mode);
                return 1;
        }
    }

    private int runIngestor() throws Exception {
        HealthServer healthServer = new HealthServer(healthPort);
        healthServer.start();

        TestConfig config = loadConfig();
        try (Ingestor ingestor = new Ingestor(config)) {
            healthServer.setReady(true);
            ingestor.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runLvReader() throws Exception {
        HealthServer healthServer = new HealthServer(healthPort);
        healthServer.start();

        TestConfig config = loadConfig();
        try (LatestVersionReader reader = new LatestVersionReader(config)) {
            healthServer.setReady(true);
            reader.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runAvadReader() throws Exception {
        HealthServer healthServer = new HealthServer(healthPort);
        healthServer.start();

        TestConfig config = loadConfig();
        try (AvadReader reader = new AvadReader(config)) {
            healthServer.setReady(true);
            reader.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runHealthMonitor() throws Exception {
        TestConfig config = loadConfig();
        HealthMonitor monitor = new HealthMonitor(config, config.runId(), gapSlaMinutes);
        try {
            return monitor.runChecks();
        } finally {
            monitor.close();
        }
    }

    private int runReconcile() throws Exception {
        TestConfig config = loadConfig();
        try (Reconciler reconciler = new Reconciler(config)) {
            if (reconcileFull) {
                return reconciler.runFullSuite();
            } else if (reconcileSource != null && reconcileAgainst != null) {
                return reconciler.reconcilePair(reconcileSource, reconcileAgainst);
            } else {
                log.error("Reconcile mode requires --full or --source + --against");
                return 1;
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.setProgramName("cosmos-avad-test");

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            log.error("Invalid arguments: {}", e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (main.help) {
            jc.usage();
            return;
        }

        try {
            System.exit(main.run());
        } catch (Exception e) {
            log.error("Fatal error", e);
            System.exit(1);
        }
    }
}
