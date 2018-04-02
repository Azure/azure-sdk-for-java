/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

/**
 * Used internally. The user agent object, which is used to track the version of the Java SDK of the Azure Cosmos DB database service.
 */
public class UserAgentContainer {

    private static final int MAX_SUFFIX_LENGTH = 64;
    private final String baseUserAgent;
    private String suffix;
    private String userAgent;

    private UserAgentContainer(String sdkName, String sdkVersion) {
        this.baseUserAgent = Utils.getUserAgent(sdkName, sdkVersion);
        this.suffix = "";
        this.userAgent = baseUserAgent;
    }
    
    public UserAgentContainer() {
        this(HttpConstants.Versions.SDK_NAME, HttpConstants.Versions.SDK_VERSION);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        if (suffix.length() > MAX_SUFFIX_LENGTH) {
            suffix = suffix.substring(0, MAX_SUFFIX_LENGTH);
        }

        this.suffix = suffix;
        this.userAgent = baseUserAgent.concat(this.suffix);
    }

    public String getUserAgent() {
        return this.userAgent;
    }
}
