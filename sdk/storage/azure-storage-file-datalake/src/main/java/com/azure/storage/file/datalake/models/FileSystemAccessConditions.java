package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;

public class FileSystemAccessConditions {
    private final ModifiedAccessConditions modifiedAccessConditions;
    private final LeaseAccessConditions leaseAccessConditions;

    public FileSystemAccessConditions(ModifiedAccessConditions modifiedAccessConditions,
        LeaseAccessConditions leaseAccessConditions) {
        this.modifiedAccessConditions = modifiedAccessConditions;
        this.leaseAccessConditions = leaseAccessConditions;
    }

    public ModifiedAccessConditions getModifiedAccessConditions() {
        return modifiedAccessConditions;
    }

    public LeaseAccessConditions getLeaseAccessConditions() {
        return leaseAccessConditions;
    }
}
