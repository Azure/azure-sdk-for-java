package com.azure.storage.file.datalake.models;

public class PathAccessConditions {

    private final ModifiedAccessConditions modifiedAccessConditions;
    private final LeaseAccessConditions leaseAccessConditions;

    public PathAccessConditions(ModifiedAccessConditions modifiedAccessConditions,
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
