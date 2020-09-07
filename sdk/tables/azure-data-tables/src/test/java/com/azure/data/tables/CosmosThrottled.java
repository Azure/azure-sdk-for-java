// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.AzureException;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.models.TableServiceErrorException;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CosmosThrottled<T> {
    protected final T client;
    protected final boolean isPlaybackMode;

    protected CosmosThrottled(T client, boolean isPlaybackMode) {
        this.client = client;
        this.isPlaybackMode = isPlaybackMode;
    }

    public abstract boolean isCosmos();

    public void runVoid(Consumer<T> action) {
        run(c -> {
            action.accept(c);
            return null;
        });
    }

    public T getClient() {
        return client;
    }

    public <TResult> TResult run(Function<T, TResult> action) {
        if (!isCosmos()) {
            return action.apply(client);
        }

        int retryCount = 0;
        int delay = 1500;
        while (true) {
            try {
                return action.apply(client);
            } catch (TableServiceErrorException e) {
                if (e.getResponse().getStatusCode() != 429) {
                    throw e;
                }

                if (++retryCount > 10) {
                    throw e;
                }

                // Disable retry throttling in Playback mode.
                if (!isPlaybackMode) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        throw new AzureException(interruptedException);
                    }
                    delay *= 2;
                }
            }
        }
    }

    public static CosmosThrottled<TableServiceAsyncClient> get(TableServiceAsyncClient client, boolean isPlaybackMode) {
        return new CosmosThrottled<TableServiceAsyncClient>(client, isPlaybackMode) {
            @Override
            public boolean isCosmos() {
                return client.getServiceUrl().contains("cosmos.azure.com");
            }
        };
    }

    public static CosmosThrottled<TableServiceClient> get(TableServiceClient client, boolean isPlaybackMode) {
        return new CosmosThrottled<TableServiceClient>(client, isPlaybackMode) {
            @Override
            public boolean isCosmos() {
                return client.getServiceUrl().contains("cosmos.azure.com");
            }
        };
    }

    public static CosmosThrottled<TableAsyncClient> get(TableAsyncClient client, boolean isPlaybackMode) {
        return new CosmosThrottled<TableAsyncClient>(client, isPlaybackMode) {
            @Override
            public boolean isCosmos() {
                return client.getTableUrl().contains("cosmos.azure.com");
            }
        };
    }

    public static CosmosThrottled<TableClient> get(TableClient client, boolean isPlaybackMode) {
        return new CosmosThrottled<TableClient>(client, isPlaybackMode) {
            @Override
            public boolean isCosmos() {
                return client.getTableUrl().contains("cosmos.azure.com");
            }
        };
    }

    public static CosmosThrottled<AzureTableImpl> get(AzureTableImpl client, boolean isPlaybackMode) {
        return new CosmosThrottled<AzureTableImpl>(client, isPlaybackMode) {
            @Override
            public boolean isCosmos() {
                return client.getUrl().contains("cosmos.azure.com");
            }
        };
    }
}
