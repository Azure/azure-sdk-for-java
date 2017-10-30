package com.microsoft.rest.v2.entities;

import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON {
    public String url;
    public Map<String,String> headers;
    public Object data;
}