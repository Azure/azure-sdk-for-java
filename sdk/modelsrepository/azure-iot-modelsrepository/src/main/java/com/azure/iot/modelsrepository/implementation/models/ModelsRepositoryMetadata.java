// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

public class ModelsRepositoryMetadata {

    private final String commitId;
    private final String publishDateUtc;
    private final String sourceRepo;
    private final Integer totalModelCount;
    private final RepositoryFeatures features;

    public ModelsRepositoryMetadata(String commitId, String publishDateUtc,
                                    String sourceRepo, Integer totalModelCount, RepositoryFeatures features) {
        this.commitId = commitId;
        this.publishDateUtc = publishDateUtc;
        this.sourceRepo = sourceRepo;
        this.totalModelCount = totalModelCount;
        this.features = features;
    }

    public String getCommitId() {
        return this.commitId;
    }

    public String getPublishDateUtc() {
        return this.publishDateUtc;
    }

    public String getSourceRepo() { return this.sourceRepo; }

    public Integer getTotalModelCount() {
        return this.totalModelCount;
    }

    public RepositoryFeatures getFeatures() {
        return this.features;
    }
}
