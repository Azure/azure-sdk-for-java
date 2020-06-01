/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.core.serializer.json.gson.GsonJsonSerializerBuilder;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosItemCustomSerializerTests extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosItemCustomSerializerTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().jsonSerializer(new GsonJsonSerializerBuilder().build()).buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        CosmosItemResponse<GsonPojo> itemResponse = container.createItem(gsonPojo);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(itemResponse.getItem(), gsonPojo);

        GsonPojo gsonPojo1 = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        CosmosItemResponse<GsonPojo> itemResponse1 = container.createItem(gsonPojo1, new CosmosItemRequestOptions());
        validateItemResponse(itemResponse1.getItem(), gsonPojo1);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        CosmosItemResponse<GsonPojo> itemResponse = container.createItem(gsonPojo);
        validateItemResponse(itemResponse.getItem(), gsonPojo);

        gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        CosmosItemResponse<GsonPojo> itemResponse1 = container.createItem(gsonPojo, new CosmosItemRequestOptions());
        validateItemResponse(itemResponse1.getItem(), gsonPojo);

        // Test for conflict
        try {
            container.createItem(gsonPojo, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readItem() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        container.createItem(gsonPojo);

        CosmosItemResponse<GsonPojo> readResponse1 = container.readItem(gsonPojo.getId(),
            new PartitionKey(gsonPojo.getMypk()), new CosmosItemRequestOptions(), GsonPojo.class);
        validateItemResponse(readResponse1.getItem(), gsonPojo);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceItem() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        CosmosItemResponse<GsonPojo> itemResponse = container.createItem(gsonPojo);
        validateItemResponse(itemResponse.getItem(), gsonPojo);

        gsonPojo = new GsonPojo(gsonPojo.getId(), gsonPojo.getMypk(), "Jane", "Doe", "I don't like pizza...");
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        // replace document
        CosmosItemResponse<GsonPojo> replace = container.replaceItem(gsonPojo, gsonPojo.getId(),
            new PartitionKey(gsonPojo.getMypk()), options);

        validateItemResponse(replace.getItem(), gsonPojo);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteItem() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        container.createItem(gsonPojo);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(gsonPojo.getId(),
            new PartitionKey(gsonPojo.getMypk()), options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }


    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllItems() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        container.createItem(gsonPojo);

        QueryRequestOptions feedOptions = new QueryRequestOptions()
            .setPartitionKey(new PartitionKey(gsonPojo.getMypk()));

        CosmosPagedIterable<GsonPojo> feedResponseIterator3 = container.readAllItems(feedOptions, GsonPojo.class);
        Iterator<GsonPojo> pojoIterator = feedResponseIterator3.iterator();

        assertThat(pojoIterator.hasNext()).isTrue();
        validateItemResponse(pojoIterator.next(), gsonPojo);
        assertThat(pojoIterator.hasNext()).isFalse();
    }


    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryItems() {
        GsonPojo gsonPojo = createItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        container.createItem(gsonPojo);

        String query = String.format("SELECT * from c where c.id = '%s'", gsonPojo.getId());
        QueryRequestOptions feedOptions = new QueryRequestOptions()
            .setPartitionKey(new PartitionKey(gsonPojo.getMypk()));

        CosmosPagedIterable<GsonPojo> feedResponseIterator1 =
            container.queryItems(query, feedOptions, GsonPojo.class);

        // Very basic validation
        Iterator<GsonPojo> pojoIterator = feedResponseIterator1.iterator();
        assertThat(pojoIterator.hasNext()).isTrue();
        validateItemResponse(pojoIterator.next(), gsonPojo);

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<GsonPojo> feedResponseIterator3 =
            container.queryItems(querySpec, feedOptions, GsonPojo.class);

        pojoIterator = feedResponseIterator3.iterator();
        assertThat(pojoIterator.hasNext()).isTrue();
        validateItemResponse(pojoIterator.next(), gsonPojo);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() {
        List<String> actualIds = new ArrayList<>();
        String partitionKey = UUID.randomUUID().toString();
        GsonPojo properties = createItem(UUID.randomUUID().toString(), partitionKey);
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = createItem(UUID.randomUUID().toString(), partitionKey);
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = createItem(UUID.randomUUID().toString(), partitionKey);
        container.createItem(properties);
        actualIds.add(properties.getId());


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0),
            actualIds.get(1), actualIds.get(2));
        QueryRequestOptions feedOptions = new QueryRequestOptions().setPartitionKey(new PartitionKey(partitionKey));
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<CosmosItemProperties> feedResponseIterator1 =
            container.queryItems(query, feedOptions, CosmosItemProperties.class);

        do {
            Iterable<FeedResponse<CosmosItemProperties>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<CosmosItemProperties> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);

    }

    private static GsonPojo createItem(String id, String mypk) {
        return new GsonPojo(id, mypk, "John", "Doe", "I like pizza!");
    }

    private static void validateItemResponse(GsonPojo actual, GsonPojo expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        assertThat(actual.getSecret()).isNull();
    }

    public static final class GsonPojo {
        @Expose
        @JsonIgnore
        private String id;

        @Expose
        @JsonIgnore
        private String mypk;

        @Expose
        @JsonIgnore
        private String firstName;

        @Expose
        @JsonIgnore
        private String lastName;

        private transient String secret;

        private GsonPojo(String id, String mypk, String firstName, String lastName, String secret) {
            this.id = id;
            this.mypk = mypk;
            this.firstName = firstName;
            this.lastName = lastName;
            this.secret = secret;
        }

        public String getId() {
            return id;
        }

        public String getMypk() {
            return mypk;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getSecret() {
            return secret;
        }
    }
}
