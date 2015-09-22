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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LazyHashMapTests {
    @Test
    public void lazyByDefaultTests() throws Exception {
        // Arrange
        LazyHashMap<String, String> lazyHashMap = new LazyHashMap<String, String>();

        // Act
        boolean initialized = lazyHashMap.isInitialized();

        // Assert
        assertEquals(false, initialized);
    }

    @Test
    public void lazyAddTests() throws Exception {
        // Arrange
        LazyHashMap<String, String> lazyHashMap = new LazyHashMap<String, String>();

        // Act
        lazyHashMap.put("Key", "Value");
        boolean initialized = lazyHashMap.isInitialized();

        // Assert
        assertEquals(true, initialized);
    }
}
