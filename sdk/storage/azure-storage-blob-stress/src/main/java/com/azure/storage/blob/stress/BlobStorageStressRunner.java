package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.storage.blob.stress.options.DownloadToFileScenarioBuilder;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.HttpFaultInjectingHttpClient;
import com.azure.storage.stress.StorageStressScenario;
import com.azure.storage.stress.StressScenarioBuilder;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class BlobStorageStressRunner {

    public static void main(String[] args) {
        FaultInjectionProbabilities probabilities = new FaultInjectionProbabilities()
            .setNoResponseIndefinite(0.003D)
            .setNoResponseClose(0.004D)
            .setNoResponseAbort(0.003D)
            .setPartialResponseIndefinite(0.06)
            .setPartialResponseClose(0.06)
            .setPartialResponseAbort(0.06)
            .setPartialResponseFinishNormal(0.06);

        DownloadToFileScenarioBuilder builder = new DownloadToFileScenarioBuilder();
        builder.setBlobPrefix("foo");
        builder.setBlobSize(10 * 1024 * 1024);
        builder.setDirectoryPath(Paths.get("C:\\Users\\jaschrep\\temp"));
        builder.setParallel(500);
        builder.setTestTimeNanoseconds(5000000000L);
        builder.setFaultInjectingClient(new HttpFaultInjectingHttpClient(
            HttpClient.createDefault(), false, probabilities));

        run(builder, true);
    }

    public static void run(DownloadToFileScenarioBuilder builder, boolean sync) {
        List<StorageStressScenario> scenarios = makeScenarios(builder);
        scenarios.get(0).globalSetup();
        for (StorageStressScenario s : scenarios) {
            s.setup();
        }

        if (sync) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(scenarios.size());
            List<Callable<Integer>> operations = new ArrayList<>(scenarios.size());
            for (StorageStressScenario s : scenarios) {
                operations.add(() -> {
                    s.run();
                    return 1;
                });
            }
            forkJoinPool.invokeAll(operations);
            forkJoinPool.awaitQuiescence(builder.getTestTimeNanoseconds() + 100000000, TimeUnit.NANOSECONDS);
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
                .then()
                .block();
        }

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
}
