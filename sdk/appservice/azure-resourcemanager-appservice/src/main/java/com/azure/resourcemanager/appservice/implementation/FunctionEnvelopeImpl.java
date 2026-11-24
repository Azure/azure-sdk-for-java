// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.appservice.fluent.models.FunctionEnvelopeInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.Map;

/** Implementation for {@link FunctionEnvelope}. */
public class FunctionEnvelopeImpl extends WrapperImpl<FunctionEnvelopeInner> implements FunctionEnvelope {

    FunctionEnvelopeImpl(FunctionEnvelopeInner innerModel) {
        super(innerModel);
    }

    @Override
    public String functionAppId() {
        return this.innerModel().functionAppId();
    }

    @Override
    public String scriptRootPathHref() {
        return this.innerModel().scriptRootPathHref();
    }

    @Override
    public String scriptHref() {
        return this.innerModel().scriptHref();
    }

    @Override
    public String configHref() {
        return this.innerModel().configHref();
    }

    @Override
    public String secretsFileHref() {
        return this.innerModel().secretsFileHref();
    }

    @Override
    public String href() {
        return this.innerModel().href();
    }

    @Override
    public Object config() {
        return this.innerModel().config();
    }

    @Override
    public Map<String, String> files() {
        return this.innerModel().files();
    }

    @Override
    public String testData() {
        return this.innerModel().testData();
    }
}
