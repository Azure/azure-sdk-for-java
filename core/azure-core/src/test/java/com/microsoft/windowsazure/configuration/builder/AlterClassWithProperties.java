/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.configuration.builder;

import com.microsoft.windowsazure.core.Builder;

import java.util.Map;

public class AlterClassWithProperties implements
        Builder.Alteration<ClassWithProperties> {
    @Override
    public ClassWithProperties alter(String profile,
            ClassWithProperties instance, Builder builder,
            Map<String, Object> properties) {
        instance.setFoo(instance.getFoo() + " - changed");
        return instance;
    }
}