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
package com.microsoft.windowsazure.core;

import java.util.Map;

public interface Builder {

    <S, T> T build(String profile, Class<S> service, Class<T> instance,
            Map<String, Object> properties);

    interface Factory<T> {
        <S> T create(String profile, Class<S> service, Builder builder,
                Map<String, Object> properties);
    }

    interface Alteration<T> {
        T alter(String profile, T instance, Builder builder,
                Map<String, Object> properties);
    }

    interface Registry {
        <T> Registry add(Class<T> service);

        <T, TImpl> Registry add(Class<T> service, Class<TImpl> implementation);

        <T> Registry add(Factory<T> factory);

        <S, T> void alter(Class<S> service, Class<T> instance,
                Alteration<T> alteration);
    }

    interface Exports {
        void register(Registry registry);
    }
}
