// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EncryptionCosmosChangeFeedTest extends EncryptionAsyncApiCrudTest {
    private final static String CHANGE_FEED_PK = "changeFeedPK";
    private Map<String, EncryptionPojo> createdItems = new HashMap<>();

    @Factory(dataProvider = "clientBuilders")
    public EncryptionCosmosChangeFeedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        populateItems();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning() {
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(CHANGE_FEED_PK)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);

        CosmosPagedFlux<EncryptionPojo> feedResponseIterator = cosmosEncryptionAsyncContainer
            .queryChangeFeed(changeFeedOption, EncryptionPojo.class);
        String continuationToken = null;
        List<EncryptionPojo> changeFeedResultList = new ArrayList<>();
        Iterator<FeedResponse<EncryptionPojo>> responseIterator =
            feedResponseIterator.byPage(continuationToken).toIterable().iterator();
        while (responseIterator.hasNext()) {
            FeedResponse<EncryptionPojo> feedResponse = responseIterator.next();
            changeFeedResultList.addAll(feedResponse.getResults());
        }

        assertThat(changeFeedResultList.size()).isEqualTo(createdItems.size());
        for (EncryptionPojo pojo : changeFeedResultList) {
            validateResponse(pojo, createdItems.get(pojo.getId()));
        }
    }


    private void populateItems() {
        for (int i = 0; i < 10; i++) {
            EncryptionPojo properties = getItem(UUID.randomUUID().toString());
            properties.setMypk("changeFeedPK");
            CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
                new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
            createdItems.put(itemResponse.getItem().getId(), itemResponse.getItem());
        }
    }


}
