/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.blob.implementation;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;

public class ISO8601DateConverterTests {
    @Test
    public void shortFormatWorks() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58Z";

        // Act
        Date result = converter.parse(value);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void longFormatWorks() throws Exception {
        // Arrange
        ISO8601DateConverter converter = new ISO8601DateConverter();
        String value = "2012-01-12T00:35:58.1234567Z";

        // Act
        Date result = converter.parse(value);

        // Assert
        assertNotNull(result);
    }
}
