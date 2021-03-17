package com.azure.iot.modelsrepository.implementation;

import com.azure.iot.modelsrepository.implementation.models.FetchResult;

import java.util.Map;

class TempCustomType {
    private final FetchResult fetchResult;
    private final Map<String, String> map;

    public TempCustomType(FetchResult fetchResult, Map<String, String> map) {
        this.fetchResult = fetchResult;
        this.map = map;
    }

    public FetchResult getFetchResult() {
        return fetchResult;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
