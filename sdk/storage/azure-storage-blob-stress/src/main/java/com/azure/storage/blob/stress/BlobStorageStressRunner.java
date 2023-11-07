package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.HttpFaultInjectingHttpClient;
import com.azure.storage.stress.StorageStressScenario;
import com.azure.storage.stress.StressScenarioBuilder;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class BlobStorageStressRunner {

    private static final ClientLogger LOGGER = new ClientLogger(BlobStorageStressRunner.class);

    public static void main(String[] args) {
        FaultInjectionProbabilities probabilities = getDefaultFaultProbabilities();

        // TODO: parse cmd line arguments
        // e.g. like it's don in perf-tests or in azure-messaging-servicebus-stress

        DownloadToFileScenarioBuilder builder = new DownloadToFileScenarioBuilder();
        builder.setBlobPrefix("foo");
        int size = Integer.parseInt(System.getProperty("blobSizeBytes"));
        //int size = 1024;
        builder.setBlobSize(size);
        boolean useFaultInjection = Boolean.parseBoolean(System.getProperty("faultInjection"));
        //boolean useFaultInjection = true;
        int parallel = Integer.parseInt(System.getProperty("parallel"));
        //int parallel = 100;


        try {
            Path tmpdir = Files.createTempDirectory("tmpDirPrefix");
            builder.setDirectoryPath(Paths.get(tmpdir.toString()));
        } catch (Exception e) {
            LOGGER.error("Unable to create tmpDirPrefix directory.", e);
            e.printStackTrace();
        }

        builder.setParallel(parallel);
        int seconds = Integer.parseInt(System.getProperty("testTime"));
        //int seconds = 90;
        builder.setTestTimeSeconds(seconds);
        if (useFaultInjection) {
            System.out.println("Using fault injection");
            builder.setFaultInjectingClient(new HttpFaultInjectingHttpClient(
                HttpClient.createDefault(), false, probabilities));
        }

        try {
            run(builder, true);
        } catch (Exception e) {
            LOGGER.error("Critical failure.", e);
            exit(1);
        }
    }

    public static void run(StressScenarioBuilder builder, boolean sync) {
        List<StorageStressScenario> scenarios = makeScenarios(builder);
        scenarios.get(0).globalSetup();
        for (StorageStressScenario s : scenarios) {
            s.setup();
        }

        if (sync) {
            LOGGER.info("Starting the test");
            ForkJoinPool forkJoinPool = new ForkJoinPool(scenarios.size());
            for (StorageStressScenario s : scenarios) {
                forkJoinPool.execute(() -> s.run(Duration.ofSeconds(builder.getTestTimeSeconds())));
            }
            LOGGER.info("Starting scenarios.");
            forkJoinPool.awaitQuiescence(builder.getTestTimeSeconds() + 1, TimeUnit.SECONDS);
            LOGGER.info("Scenario ended.");
        } else {
            // Exceptions like OutOfMemoryError are handled differently by the default Reactor schedulers. Instead of terminating the
            // Flux, the Flux will hang and the exception is only sent to the thread's uncaughtExceptionHandler and the Reactor
            // Schedulers.onHandleError.  This handler ensures the perf framework will fail fast on any such exceptions.
            Schedulers.onHandleError((t, e) -> {
                LOGGER.error("Critical failure.", e);
                exit(1);
            });

            Flux.fromIterable(scenarios)
                .parallel(scenarios.size())
                .runOn(Schedulers.parallel())
                .flatMap(StorageStressScenario::runAsync)
                .sequential()
                .timeout(Duration.ofSeconds(builder.getTestTimeSeconds()))
                .then()
                .block();
        }

        LOGGER.atInfo()
            .addKeyValue("succeeded", scenarios.stream().mapToInt(StorageStressScenario::getSuccessfulRunCount).sum())
            .addKeyValue("failed", scenarios.stream().mapToInt(StorageStressScenario::getFailedRunCount).sum())
            .log("test ended");

        for (StorageStressScenario s : scenarios) {
            s.teardown();
        }
        scenarios.get(0).globalTeardown();
    }

    private static List<StorageStressScenario> makeScenarios(StressScenarioBuilder builder) {
        List<StorageStressScenario> scenarios = new ArrayList<>(builder.getParallel());
        for (int i = 0; i < builder.getParallel(); i++) {
            scenarios.add(builder.build());
        }
        return scenarios;
    }

    // randomly creating network failures
    private static FaultInjectionProbabilities getDefaultFaultProbabilities() {
        return new FaultInjectionProbabilities()
            .setNoResponseIndefinite(0.003D)
            .setNoResponseClose(0.004D)
            .setNoResponseAbort(0.003D)
            .setPartialResponseIndefinite(0.06)
            .setPartialResponseClose(0.06)
            .setPartialResponseAbort(0.06)
            .setPartialResponseFinishNormal(0.06);
    }
}
