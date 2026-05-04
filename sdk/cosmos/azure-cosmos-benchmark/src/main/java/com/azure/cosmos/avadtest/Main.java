package com.azure.cosmos.avadtest;

import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.health.HealthMonitor;
import com.azure.cosmos.avadtest.health.HealthServer;
import com.azure.cosmos.avadtest.ingestor.Ingestor;
import com.azure.cosmos.avadtest.metrics.SoakMetrics;
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

    @Parameter(names = "--produced", description = "Produced log file (for reconcile mode)")
    private String producedFile;

    @Parameter(names = "--consumed", description = "Consumed log file (for reconcile mode)")
    private String consumedFile;

    @Parameter(names = "--lv", description = "LV consumed log file (for parity check)")
    private String lvFile;

    @Parameter(names = "--avad", description = "AVAD consumed log file (for parity check)")
    private String avadFile;

    @Parameter(names = "--health-port", description = "Health server port (default: 8080)")
    private int healthPort = 8080;

    @Parameter(names = "--run-id", description = "Soak run identifier (for health monitor)")
    private String runId = "soak-default";

    @Parameter(names = "--gap-sla-minutes", description = "Minutes before an unconsumed event is flagged as a gap")
    private int gapSlaMinutes = 10;

    @Parameter(names = {"-h", "--help"}, description = "Help", help = true)
    private boolean help;

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
        SoakMetrics metrics = new SoakMetrics();
        HealthServer healthServer = new HealthServer(metrics, healthPort);
        healthServer.start();

        TestConfig config = TestConfig.fromEnv();
        try (Ingestor ingestor = new Ingestor(config)) {
            healthServer.setReady(true);
            ingestor.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runLvReader() throws Exception {
        SoakMetrics metrics = new SoakMetrics();
        HealthServer healthServer = new HealthServer(metrics, healthPort);
        healthServer.start();

        TestConfig config = TestConfig.fromEnv();
        try (LatestVersionReader reader = new LatestVersionReader(config)) {
            healthServer.setReady(true);
            reader.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runAvadReader() throws Exception {
        SoakMetrics metrics = new SoakMetrics();
        HealthServer healthServer = new HealthServer(metrics, healthPort);
        healthServer.start();

        TestConfig config = TestConfig.fromEnv();
        try (AvadReader reader = new AvadReader(config)) {
            healthServer.setReady(true);
            reader.run();
            return 0;
        } finally {
            healthServer.stop();
        }
    }

    private int runHealthMonitor() {
        TestConfig config = TestConfig.fromEnv();
        HealthMonitor monitor = new HealthMonitor(config, runId, gapSlaMinutes);
        try {
            return monitor.runChecks();
        } finally {
            monitor.close();
        }
    }

    private int runReconcile() throws Exception {
        if (producedFile != null && consumedFile != null) {
            return Reconciler.reconcile(producedFile, consumedFile);
        } else if (lvFile != null && avadFile != null) {
            return Reconciler.parity(lvFile, avadFile);
        } else {
            log.error("Reconcile mode requires either --produced + --consumed or --lv + --avad");
            return 1;
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
