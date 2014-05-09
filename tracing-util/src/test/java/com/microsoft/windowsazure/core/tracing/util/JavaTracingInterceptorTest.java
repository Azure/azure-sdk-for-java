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
package com.microsoft.windowsazure.core.tracing.util;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import com.microsoft.windowsazure.tracing.CloudTracing;

public class JavaTracingInterceptorTest
{
    private ByteArrayOutputStream logContent;
    
    @Test
    public void testInformationSendRequest()
    {
        // Arrange
        logContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(logContent));

        CloudTracing.addTracingInterceptor(new JavaTracingInterceptor());

        HttpRequest httpRequest = new HttpPost("http://www.bing.com");

        // Set Headers
        httpRequest.setHeader("Content-Type", "application/xml");
        httpRequest.setHeader("x-ms-version", "2012-03-01");

        // Act
        CloudTracing.sendRequest("test", httpRequest);

        // Assert
        String result = logContent.toString();
        assertTrue(result.contains("INFO: invocationId: test\r\nrequest: POST http://www.bing.com HTTP/1.1"));

        CloudTracing.information("hello there");
        result = logContent.toString();
        assertTrue(result.contains("INFO: hello there"));
    }
}
