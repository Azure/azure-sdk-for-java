/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.implementation;

import com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.Transformation;
import com.microsoft.azure.arm.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

class TransformationImpl extends CreatableUpdatableImpl<Transformation, TransformationInner, TransformationImpl> implements Transformation, Transformation.Definition, Transformation.Update {
    private final StreamAnalyticsManager manager;
    private String resourceGroupName;
    private String jobName;
    private String transformationName;
    private String cifMatch;
    private String cifNoneMatch;
    private String uifMatch;

    TransformationImpl(String name, StreamAnalyticsManager manager) {
        super(name, new TransformationInner());
        this.manager = manager;
        // Set resource name
        this.transformationName = name;
        //
    }

    TransformationImpl(TransformationInner inner, StreamAnalyticsManager manager) {
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.transformationName = inner.name();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.id(), "resourcegroups");
        this.jobName = IdParsingUtils.getValueFromIdByName(inner.id(), "streamingjobs");
        this.transformationName = IdParsingUtils.getValueFromIdByName(inner.id(), "transformations");
        //
    }

    @Override
    public StreamAnalyticsManager manager() {
        return this.manager;
    }

    @Override
    public Observable<Transformation> createResourceAsync() {
        TransformationsInner client = this.manager().inner().transformations();
        return client.createOrReplaceAsync(this.resourceGroupName, this.jobName, this.transformationName, this.inner(), this.cifMatch, this.cifNoneMatch)
            .map(innerToFluentMap(this));
    }

    @Override
    public Observable<Transformation> updateResourceAsync() {
        TransformationsInner client = this.manager().inner().transformations();
        return client.updateAsync(this.resourceGroupName, this.jobName, this.transformationName, this.inner(), this.uifMatch)
            .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<TransformationInner> getInnerAsync() {
        TransformationsInner client = this.manager().inner().transformations();
        return client.getAsync(this.resourceGroupName, this.jobName, this.transformationName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }


    @Override
    public String etag() {
        return this.inner().etag();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String query() {
        return this.inner().query();
    }

    @Override
    public Integer streamingUnits() {
        return this.inner().streamingUnits();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public TransformationImpl withExistingStreamingjob(String resourceGroupName, String jobName) {
        this.resourceGroupName = resourceGroupName;
        this.jobName = jobName;
        return this;
    }

    @Override
    public TransformationImpl withIfNoneMatch(String ifNoneMatch) {
        this.cifNoneMatch = ifNoneMatch;
        return this;
    }

    @Override
    public TransformationImpl withIfMatch(String ifMatch) {
        if (isInCreateMode()) {
            this.cifMatch = ifMatch;
        } else {
            this.uifMatch = ifMatch;
        }
        return this;
    }

    @Override
    public TransformationImpl withName(String name) {
        this.inner().withName(name);
        return this;
    }

    @Override
    public TransformationImpl withQuery(String query) {
        this.inner().withQuery(query);
        return this;
    }

    @Override
    public TransformationImpl withStreamingUnits(Integer streamingUnits) {
        this.inner().withStreamingUnits(streamingUnits);
        return this;
    }

}
