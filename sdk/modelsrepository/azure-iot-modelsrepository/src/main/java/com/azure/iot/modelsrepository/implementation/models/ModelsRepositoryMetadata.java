// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

/**
 * {@link ModelsRepositoryMetadata} is designated to store
 * information about models repository.
 */
public class ModelsRepositoryMetadata {

    private final String commitId;
    private final String publishDateUtc;
    private final String sourceRepo;
    private final Integer totalModelCount;
    private final RepositoryFeatures features;

    public ModelsRepositoryMetadata(String commitId,
                             String publishDateUtc,
                             String sourceRepo,
                             Integer totalModelCount,
                             RepositoryFeatures features) {
        this.commitId = commitId;
        this.publishDateUtc = publishDateUtc;
        this.sourceRepo = sourceRepo;
        this.totalModelCount = totalModelCount;
        this.features = features;
    }

    public ModelsRepositoryMetadata() {
        this.commitId = null;
        this.publishDateUtc = null;
        this.sourceRepo = null;
        this.totalModelCount = null;
        this.features = null;
    }

    public String getCommitId() {
        return this.commitId;
    }

    public String getPublishDateUtc() {
        return this.publishDateUtc;
    }

    public String getSourceRepo() {
        return this.sourceRepo;
    }

    public Integer getTotalModelCount() {
        return this.totalModelCount;
    }

    public RepositoryFeatures getFeatures() {
        return this.features;
    }
}
