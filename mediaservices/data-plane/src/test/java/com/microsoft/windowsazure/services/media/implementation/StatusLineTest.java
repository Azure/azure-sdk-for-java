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

package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import javax.activation.DataSource;

import org.junit.Test;

import com.microsoft.windowsazure.core.utils.InputStreamDataSource;

public class StatusLineTest {

    @Test
    public void testCanCreateStatus() throws Exception {
        // Arrange
        String httpResponse = "HTTP/1.1 200 OK";
        int expectedStatus = 200;
        String expectedReason = "OK";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                httpResponse.getBytes());
        DataSource dataSource = new InputStreamDataSource(byteArrayInputStream,
                "defaultContentType");

        // Act
        StatusLine statusLine = StatusLine.create(dataSource);

        // Assert
        assertEquals(expectedStatus, statusLine.getStatus());
        assertEquals(expectedReason, statusLine.getReason());
    }

    @Test
    public void testGetSetStatus() {
        // Arrange
        String httpResponse = "HTTP/1.1 200 OK";
        int expectedStatus = 300;
        String expectedReason = "NotOK";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                httpResponse.getBytes());
        DataSource dataSource = new InputStreamDataSource(byteArrayInputStream,
                "defaultContentType");
        StatusLine statusLine = StatusLine.create(dataSource);

        // Act
        statusLine.setStatus(expectedStatus);
        statusLine.setReason(expectedReason);

        // Assert
        assertEquals(expectedStatus, statusLine.getStatus());
        assertEquals(expectedReason, statusLine.getReason());
    }

    @Test
    public void testGetSetReason() {

    }

}
