package com.azure.storage.blob.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.stress.builders.BlobScenarioBuilder;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.blob.stress.scenarios.infra.BlobStressScenario;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

public class DownloadStressScenario extends BlobStressScenario<BlobScenarioBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadStressScenario.class);

    public DownloadStressScenario(BlobScenarioBuilder builder) {
        super(builder, /*singletonBlob*/true, /*initializeBlob*/true);
    }

    @Override
    public void run(Duration timeout) {
        long endTimeNano = System.nanoTime() + timeout.toNanos();
        long timeoutNano;

        while ((timeoutNano = endTimeNano - System.nanoTime()) > 0) {

        }
    }

    @Override
    public Mono<Void> runAsync() {
        return null;
    }
}
