package com.microsoft.azure.v3;

import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    public String url;
    public Map<String,String> headers;
    public Object data;
}