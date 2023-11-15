package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.HttpFaultInjectingHttpClient;
import com.azure.storage.stress.StorageStressScenario;
import com.azure.storage.stress.StressScenarioBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
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
        boolean sync = !Boolean.parseBoolean(System.getProperty("async"));
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
            run(builder, sync);
        } catch (Exception e) {
            LOGGER.error("Critical failure.", e);
            exit(1);
        }
    }

    public static void run(StressScenarioBuilder builder, boolean sync) {
        StorageStressScenario scenario = builder.build();
        scenario.setup();

        LOGGER.info("Starting the test.");
        if (sync) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(builder.getParallel());
            for (int i = 0; i < builder.getParallel(); i ++) {
                forkJoinPool.execute(() -> scenario.run(Duration.ofSeconds(builder.getTestTimeSeconds())));
            }

            try {
                Thread.sleep(builder.getTestTimeSeconds() * 1000);
            } catch (InterruptedException e) {
                LOGGER.logThrowableAsWarning(e);
            }
            scenario.done();
            // let the threads finish
            forkJoinPool.awaitQuiescence(10, TimeUnit.SECONDS);
        } else {
            Mono<Long> until = Mono.delay(Duration.ofSeconds(builder.getTestTimeSeconds()))
                    .doFinally(l -> scenario.done());

            Mono<Void> runOnce = Mono.defer(() -> scenario.runAsync().onErrorResume(e -> Mono.empty()));
            Flux.range(0, builder.getParallel())
                .flatMap(i -> runOnce.repeat())
                .takeUntilOther(until)
                .parallel(builder.getParallel())
                .runOn(Schedulers.boundedElastic())
                .then()
                // let the threads finish
                .timeout(Duration.ofSeconds(builder.getTestTimeSeconds() + 10))
                .onErrorResume(e -> Mono.empty())
                .block();
        }
        LOGGER.atInfo()
            .addKeyValue("succeeded", scenario.getSuccessfulRunCount())
            .addKeyValue("failed", scenario.getFailedRunCount())
            .log("test ended");

        scenario.teardown();
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
