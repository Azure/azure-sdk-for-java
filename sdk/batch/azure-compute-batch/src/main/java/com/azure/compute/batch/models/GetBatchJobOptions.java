package com.azure.compute.batch.models;

import com.azure.core.http.RequestConditions;

public class GetBatchJobOptions extends BatchGetOptions{
    private RequestConditions requestConditions;

    /**
     * Gets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @return The HTTP options for conditional requests.
     */
    public RequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @param requestConditions The HTTP options for conditional requests.
     */
    public void setRequestConditions(RequestConditions requestConditions) {
        this.requestConditions = requestConditions;
    }

}
