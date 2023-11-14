package com.azure.storage.blob.stress.builders;

import com.azure.storage.blob.stress.scenarios.DownloadStressScenario;
import com.azure.storage.stress.StorageStressScenario;

public class DownloadScenarioBuilder extends BlobScenarioBuilder {
    @Override
    public StorageStressScenario build() {
        return new DownloadStressScenario(this);
    }
}
