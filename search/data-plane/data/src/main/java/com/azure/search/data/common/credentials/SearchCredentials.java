package com.azure.search.data.common.credentials;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

/**
 * api key credential that is put into a header to authorize requests.
 */
public class SearchCredentials implements ServiceClientCredentials {

    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new SearchInterceptor(this));
    }

    public SearchCredentials(String apiKey) {
        this.apiKey = apiKey;
    }
}
