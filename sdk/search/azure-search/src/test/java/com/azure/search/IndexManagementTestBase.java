// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.search.models.Index;
import com.azure.search.test.environment.models.ModelComparer;

import java.util.Objects;

public abstract class IndexManagementTestBase extends SearchServiceTestBase {
    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchServiceClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix("search.windows.net")
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchServiceClientBuilder()
                .serviceName("searchServiceName")
                .searchDnsSuffix("search.windows.net")
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    public abstract void createIndexReturnsCorrectDefinition();

    public abstract void createIndexReturnsCorrectDefaultValues();

    public abstract void createIndexFailsWithUsefulMessageOnUserError();

    public abstract void getIndexReturnsCorrectDefinition();

    public abstract void getIndexThrowsOnNotFound();

    public abstract void existsReturnsTrueForExistingIndex();

    public abstract void existsReturnsFalseForNonExistingIndex();

    public abstract void deleteIndexIfNotChangedWorksOnlyOnCurrentResource();

    public abstract void deleteIndexIfExistsWorksOnlyWhenResourceExists();

    public abstract void deleteIndexIsIdempotent();

    public abstract void canCreateAndDeleteIndex();

    protected static boolean assertIndexesEqual(Index expected, Index actual) {
        return Objects.equals(expected.getName(), actual.getName())
            && ModelComparer.collectionEquals(expected.getFields(), actual.getFields())
            && ModelComparer.collectionEquals(expected.getScoringProfiles(), actual.getScoringProfiles())
            && Objects.equals(expected.getDefaultScoringProfile(), actual.getDefaultScoringProfile())
            && Objects.equals(expected.getCorsOptions(), actual.getCorsOptions())
            && ModelComparer.collectionEquals(expected.getSuggesters(), actual.getSuggesters())
            && ModelComparer.collectionEquals(expected.getAnalyzers(), actual.getAnalyzers())
            && ModelComparer.collectionEquals(expected.getTokenizers(), actual.getTokenizers())
            && ModelComparer.collectionEquals(expected.getTokenFilters(), actual.getTokenFilters())
            && ModelComparer.collectionEquals(expected.getCharFilters(), actual.getCharFilters());
    }
}
