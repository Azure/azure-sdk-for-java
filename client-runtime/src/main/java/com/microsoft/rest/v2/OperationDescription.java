package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpRequest;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

public class OperationDescription implements Serializable {
    private Serializable pollStrategyData;
    private Map<String, String> headers;
    private String callerMethod;
    private String httpMethod;
    private URL url;
    private String fullyQualifiedMethodName;

    public OperationDescription() {
        this.fullyQualifiedMethodName = null;
        this.pollStrategyData = null;
        this.headers = null;
        this.url = null;
        this.httpMethod = null;
        this.callerMethod = null;
    }

    public OperationDescription(String fullyQualifiedMethodName,
                                Serializable pollStrategyData,
                                HttpRequest originalHttpRequest) {
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.pollStrategyData = pollStrategyData;
        this.headers = originalHttpRequest.headers().toMap();
        this.url = originalHttpRequest.url();
        this.httpMethod = originalHttpRequest.httpMethod().toString();
        this.callerMethod = originalHttpRequest.callerMethod();
    }

    public Serializable pollStrategyData() {
        return this.pollStrategyData;
    }

    public String callerMethod() {
        return this.callerMethod;
    }

    public URL url() {
        return this.url;
    }

    public String httpMethod() {
        return this.httpMethod;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public String fullyQualifiedMethodName() {
        return this.fullyQualifiedMethodName;
    }

    String methodName() {
        int lastIndex = this.fullyQualifiedMethodName.lastIndexOf(".");
        return this.fullyQualifiedMethodName.substring(lastIndex + 1);
    }

    String className() {
        int lastIndex = this.fullyQualifiedMethodName.lastIndexOf(".");
        return this.fullyQualifiedMethodName.substring(0, lastIndex);
    }
}
