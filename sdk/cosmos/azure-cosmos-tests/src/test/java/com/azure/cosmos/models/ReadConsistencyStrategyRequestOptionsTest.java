// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ReadConsistencyStrategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadConsistencyStrategyRequestOptionsTest {

    @DataProvider(name = "allReadConsistencyStrategies")
    public Object[][] allReadConsistencyStrategies() {
        return new Object[][] {
            { ReadConsistencyStrategy.DEFAULT },
            { ReadConsistencyStrategy.EVENTUAL },
            { ReadConsistencyStrategy.SESSION },
            { ReadConsistencyStrategy.LATEST_COMMITTED },
            { ReadConsistencyStrategy.GLOBAL_STRONG },
        };
    }

    @Test(groups = "unit", dataProvider = "allReadConsistencyStrategies")
    public void cosmosItemRequestOptionsSetGetReadConsistencyStrategy(ReadConsistencyStrategy strategy) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        assertThat(options.getReadConsistencyStrategy()).isNull();

        CosmosItemRequestOptions returned = options.setReadConsistencyStrategy(strategy);
        assertThat(returned).isSameAs(options);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(strategy);
    }

    @Test(groups = "unit")
    public void cosmosItemRequestOptionsSetNullReadConsistencyStrategy() {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.SESSION);

        options.setReadConsistencyStrategy(null);
        assertThat(options.getReadConsistencyStrategy()).isNull();
    }

    @Test(groups = "unit", dataProvider = "allReadConsistencyStrategies")
    public void cosmosQueryRequestOptionsSetGetReadConsistencyStrategy(ReadConsistencyStrategy strategy) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        assertThat(options.getReadConsistencyStrategy()).isNull();

        CosmosQueryRequestOptions returned = options.setReadConsistencyStrategy(strategy);
        assertThat(returned).isSameAs(options);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(strategy);
    }

    @Test(groups = "unit")
    public void cosmosQueryRequestOptionsSetNullReadConsistencyStrategy() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);

        options.setReadConsistencyStrategy(null);
        assertThat(options.getReadConsistencyStrategy()).isNull();
    }

    @Test(groups = "unit", dataProvider = "allReadConsistencyStrategies")
    public void cosmosChangeFeedRequestOptionsSetGetReadConsistencyStrategy(ReadConsistencyStrategy strategy) {
        CosmosChangeFeedRequestOptions options =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
                FeedRange.forFullRange());
        // CosmosChangeFeedRequestOptions defaults to DEFAULT
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.DEFAULT);

        CosmosChangeFeedRequestOptions returned = options.setReadConsistencyStrategy(strategy);
        assertThat(returned).isSameAs(options);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(strategy);
    }

    @Test(groups = "unit")
    public void cosmosChangeFeedRequestOptionsSetNullReadConsistencyStrategy() {
        CosmosChangeFeedRequestOptions options =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
                FeedRange.forFullRange());
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.GLOBAL_STRONG);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.GLOBAL_STRONG);

        options.setReadConsistencyStrategy(null);
        assertThat(options.getReadConsistencyStrategy()).isNull();
    }

    @Test(groups = "unit", dataProvider = "allReadConsistencyStrategies")
    public void cosmosReadManyRequestOptionsSetGetReadConsistencyStrategy(ReadConsistencyStrategy strategy) {
        CosmosReadManyRequestOptions options = new CosmosReadManyRequestOptions();
        assertThat(options.getReadConsistencyStrategy()).isNull();

        CosmosReadManyRequestOptions returned = options.setReadConsistencyStrategy(strategy);
        assertThat(returned).isSameAs(options);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(strategy);
    }

    @Test(groups = "unit")
    public void cosmosReadManyRequestOptionsSetNullReadConsistencyStrategy() {
        CosmosReadManyRequestOptions options = new CosmosReadManyRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.EVENTUAL);

        options.setReadConsistencyStrategy(null);
        assertThat(options.getReadConsistencyStrategy()).isNull();
    }

    @Test(groups = "unit", dataProvider = "allReadConsistencyStrategies")
    public void cosmosRequestOptionsSetGetReadConsistencyStrategy(ReadConsistencyStrategy strategy) {
        CosmosRequestOptions options = new CosmosRequestOptions();
        assertThat(options.getReadConsistencyStrategy()).isNull();

        CosmosRequestOptions returned = options.setReadConsistencyStrategy(strategy);
        assertThat(returned).isSameAs(options);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(strategy);
    }

    @Test(groups = "unit")
    public void cosmosRequestOptionsSetNullReadConsistencyStrategy() {
        CosmosRequestOptions options = new CosmosRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);
        assertThat(options.getReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.SESSION);

        options.setReadConsistencyStrategy(null);
        assertThat(options.getReadConsistencyStrategy()).isNull();
    }
}
