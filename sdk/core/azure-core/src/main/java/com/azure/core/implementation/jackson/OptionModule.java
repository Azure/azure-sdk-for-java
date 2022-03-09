/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.Option;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

/**
 * A Jackson module that registers capability of serializing {@link Option} objects with the Jackson core.
 */
final class OptionModule extends Module {
    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new OptionSerializerProvider());
        context.addTypeModifier(new OptionTypeModifier());
        context.addBeanSerializerModifier(new OptionPropertiesModifier());
    }

    @Override
    public String getModuleName() {
        return "OptionModule";
    }

    @Override
    public Version version() {
        // version is used by Jackson for informational purpose hence OptionModule-0.0.0
        // should be fine.
        return Version.unknownVersion();
    }
}
