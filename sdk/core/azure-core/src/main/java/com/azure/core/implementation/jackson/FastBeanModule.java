// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class FastBeanModule extends Module {
    private static final FastBeanModule MODULE;

    static {
        MODULE = new FastBeanModule();
    }

    static Module getModule() {
        return MODULE;
    }

    @Override
    public String getModuleName() {
        return "FastBeanModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanDeserializerModifier(new FastBeanDeserializerAccessor());
        context.addBeanSerializerModifier(new FastBeanSerializerAccessor());
    }
}
