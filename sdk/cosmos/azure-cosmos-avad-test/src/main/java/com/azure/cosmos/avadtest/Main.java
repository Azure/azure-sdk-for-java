package com.azure.cosmos.avadtest;

import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.health.HealthMonitor;
import com.azure.cosmos.avadtest.health.HealthServer;
import com.azure.cosmos.avadtest.ingestor.Ingestor;
import com.azure.cosmos.avadtest.metrics.SoakMetrics;
import com.azure.cosmos.avadtest.reader.AvadReader;
import com.azure.cosmos.avadtest.reader.LatestVersionReader;
import com.azure.cosmos.avadtest.reconciliation.Reconciler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "cosmos-avad-test",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "AVAD E2E test: ingestor, LV reader, AVAD reader, reconciler")
public final class Main implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Option(names = "--mode", required = true,
        description = "Mode: ingestor, lv-reader, avad-reader, reconcile")
    private String mode;

    @Option(names = "--produced", description = "Produced log file (for reconcile mode)")
    private String producedFile;

    @Option(names = "--consumed", description = "Consumed log file (for reconcile mode)")
    private String consumedFile;

    @Option(names = "--lv", description = "LV consumed log file (for parity check)")
    private String lvFile;

    @Option(names = "--avad", description = "AVAD consumed log file (for parity check)")
    private String avadFile;

    @Option(names = "--health-port", defaultValue = "8080",
        description = "Health server port (default: 8080)")
    private int healthPort;

    @Option(names = "--run-id", defaultValue = "soak-default",
        description = "Soak run identifier (for health monitor)")
    private String runId;

    @Option(names = "--gap-sla-minutes", defaultValue = "10",
        description = "Minutes before an unconsumed event is flagged as a gap")
    private int gapSlaMinutes;

    @Override
    public Integer call() throws Exception {
        log.info("Starting cosmos-avad-test in mode: {}", mode);

        return switch (mode) {
            case "ingestor" -> runIngestor();
            case "lv-reader" -> runLvReader();
            case "avad-reader" -> runAvadReader();
            case "reconcile" -> runReconcile();
            case "health-monitor" -> runHealthMonitor();
            default -> {
                log.error("Unknown mode: {}. Use: ingestor, lv-reader, avad-reader, reconcile", mode);
                yield 1;
            }
        };
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
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
