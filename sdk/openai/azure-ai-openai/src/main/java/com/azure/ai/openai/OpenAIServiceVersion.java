package com.azure.ai.openai;

import com.azure.core.util.ServiceVersion;

public enum OpenAIServiceVersion implements ServiceVersion {

    V1("v1");

    private final String version;

    OpenAIServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
