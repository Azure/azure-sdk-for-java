// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.mocks;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.TemplateInput;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MockTemplateInput extends TemplateInput {

    @Override
    public String getPackageName() {
        return "com.azure.v2.dummy.implementation.DummyClientImpl";
    }

    @Override
    public String getServiceInterfaceImplShortName() {
        return "DummyServiceImpl";
    }

    @Override
    public String getServiceInterfaceShortName() {
        return "DummyService";
    }

    @Override
    public String getServiceInterfaceFQN() {
        return "com.azure.v2.dummy.implementation.DummyClientImpl.DummyService";
    }

    @Override
    public Map<String, String> getImports() {
        return Collections.emptyMap();
    }

    @Override
    public List<HttpRequestContext> getHttpRequestContexts() {
        return Collections.emptyList();
    }
}
