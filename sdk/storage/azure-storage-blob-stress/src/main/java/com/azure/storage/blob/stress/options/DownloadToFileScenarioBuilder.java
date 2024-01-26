package com.azure.storage.blob.stress.options;

import com.azure.storage.blob.stress.scenarios.DownloadToFileStressScenario;
import com.azure.storage.stress.StorageStressScenario;

import java.nio.file.Path;
import java.util.UUID;

public class DownloadToFileScenarioBuilder extends BlobScenarioBuilder
{
    private Path directoryPath;

    public Path getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(Path directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public StorageStressScenario build() {
        return new DownloadToFileStressScenario(this);
    }
}
