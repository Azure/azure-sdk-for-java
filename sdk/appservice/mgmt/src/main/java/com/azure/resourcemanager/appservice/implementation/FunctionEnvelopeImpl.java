// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.appservice.fluent.inner.FunctionEnvelopeInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.Map;

/** Implementation for {@link FunctionEnvelope}. */
public class FunctionEnvelopeImpl extends WrapperImpl<FunctionEnvelopeInner> implements FunctionEnvelope {

    FunctionEnvelopeImpl(FunctionEnvelopeInner innerModel) {
        super(innerModel);
    }

    @Override
    public String functionAppId() {
        return this.inner().functionAppId();
    }

    @Override
    public String scriptRootPathHref() {
        return this.inner().scriptRootPathHref();
    }

    @Override
    public String scriptHref() {
        return this.inner().scriptHref();
    }

    @Override
    public String configHref() {
        return this.inner().configHref();
    }

    @Override
    public String secretsFileHref() {
        return this.inner().secretsFileHref();
    }

    @Override
    public String href() {
        return this.inner().href();
    }

    @Override
    public Object config() {
        return this.inner().config();
    }

    @Override
    public Map<String, String> files() {
        return this.inner().files();
    }

    @Override
    public String testData() {
        return this.inner().testData();
    }
}
