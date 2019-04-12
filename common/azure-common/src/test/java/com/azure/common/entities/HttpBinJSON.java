// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    public String url;
    public Map<String,String> headers;
    public Object data;
}
