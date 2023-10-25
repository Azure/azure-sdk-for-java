package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
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

public class BlobStorageStressRunner {

    public static void main(String[] args) {
        FaultInjectionProbabilities probabilities = getDefaultFaultProbabilities();
        DownloadToFileScenarioBuilder builder = new DownloadToFileScenarioBuilder();
        builder.setBlobPrefix("foo");
        builder.setBlobSize(10 * 1024 * 1024);
        try {
            Path tmpdir = Files.createTempDirectory("tmpDirPrefix");
            builder.setDirectoryPath(Paths.get(tmpdir.toString()));
        } catch (Exception e) {
            System.err.println("Unable to create tmpDirPrefix directory.");
            e.printStackTrace();
        }

        builder.setParallel(100);
        builder.setTestTimeSeconds(30);
        builder.setFaultInjectingClient(new HttpFaultInjectingHttpClient(
            HttpClient.createDefault(), false, probabilities));

        try {
            run(builder, true);
        } catch (Exception e) {
            System.err.println("Critical failure.");
            e.printStackTrace();
        }
    }

    public static void run(StressScenarioBuilder builder, boolean sync) {
        List<StorageStressScenario> scenarios = makeScenarios(builder);
        scenarios.get(0).globalSetup();
        for (StorageStressScenario s : scenarios) {
            s.setup();
        }

        if (sync) {
            System.out.println("start: " + java.time.LocalTime.now());
            ForkJoinPool forkJoinPool = new ForkJoinPool(scenarios.size());
            for (StorageStressScenario s : scenarios) {
                forkJoinPool.execute(() -> s.run(Duration.ofSeconds(builder.getTestTimeSeconds())));
            }
            System.out.println("scenarios started: " + java.time.LocalTime.now());
            forkJoinPool.awaitQuiescence(builder.getTestTimeSeconds() + 1, TimeUnit.SECONDS);
            System.out.println("awaited: " + java.time.LocalTime.now());
        } else {
            // Exceptions like OutOfMemoryError are handled differently by the default Reactor schedulers. Instead of terminating the
            // Flux, the Flux will hang and the exception is only sent to the thread's uncaughtExceptionHandler and the Reactor
            // Schedulers.onHandleError.  This handler ensures the perf framework will fail fast on any such exceptions.
            Schedulers.onHandleError((t, e) -> {
                System.err.print(t + " threw exception: ");
                e.printStackTrace();
                System.exit(1);
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

        System.out.println("Success: " + scenarios.stream().mapToInt(StorageStressScenario::getSuccessfulRunCount).sum());
        System.out.println("failed: " + scenarios.stream().mapToInt(StorageStressScenario::getFailedRunCount).sum());

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
