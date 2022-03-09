// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.Importance;
import com.azure.spring.data.cosmos.domain.PageableMemo;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PageableMemoRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PageableMemoRepositoryIT {

    private static final int TOTAL_CONTENT_SIZE = 500;

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private PageableMemoRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CosmosFactory cosmosFactory;

    private static Set<PageableMemo> memoSet;
    private static Set<PageableMemo> normalMemos;

    private static boolean isSetupDone;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreated(template, PageableMemo.class);

        if (isSetupDone) {
            return;
        }
        memoSet = new HashSet<>();
        final Random random = new Random();
        final Importance[] importanceValues = Importance.values();

        //  Create larger documents with size more than 10 kb
        for (int i = 0; i < TOTAL_CONTENT_SIZE; i++) {
            final String id = UUID.randomUUID().toString();
            final String message = UUID.randomUUID().toString();
            final int randomIndex = random.nextInt(3);
            final PageableMemo memo = new PageableMemo(id, message, new Date(), importanceValues[randomIndex]);
            repository.save(memo);
            memoSet.add(memo);
        }

        // Set of memos with NORMAL importance
        normalMemos = memoSet.stream()
            .filter(m -> m.getImportance().equals(Importance.NORMAL))
            .collect(Collectors.toSet());

        isSetupDone = true;
    }

    @Test
    public void testFindAllWithPageSizeLessThanReturned() {
        final Set<PageableMemo> memos = findAllWithPageSize(20);
        boolean equal = memos.equals(memoSet);
        assertThat(equal).isTrue();
    }

    @Test
    public void testFindAllWithPageSizeLessThanTotal() {
        final Set<PageableMemo> memos = findAllWithPageSize(200);
        boolean equal = memos.equals(memoSet);
        assertThat(equal).isTrue();
    }

    @Test
    public void testFindAllWithPageSizeGreaterThanTotal() {
        final Set<PageableMemo> memos = findAllWithPageSize(10000);
        boolean equal = memos.equals(memoSet);
        assertThat(equal).isTrue();
    }

    @Test
    public void testOffsetAndLimitLessThanTotal() {
        final int skipCount = 50;
        final int takeCount = 200;
        verifyItemsWithOffsetAndLimit(skipCount, takeCount, takeCount);
    }

    @Test
    public void testOffsetAndLimitEqualToTotal() {
        final int skipCount = 100;
        final int takeCount = 300;
        verifyItemsWithOffsetAndLimit(skipCount, takeCount, takeCount);
    }


    @Test
    public void testOffsetAndLimitGreaterThanTotal() {
        final int skipCount = 300;
        final int takeCount = 300;
        verifyItemsWithOffsetAndLimit(skipCount, takeCount, TOTAL_CONTENT_SIZE - skipCount);
    }

    @Test
    public void testFindByImportanceUsingSliceWithPageSizeLessThanReturned() {
        final Set<PageableMemo> memos = findByWithSlice(20);
        boolean equal = memos.equals(normalMemos);
        assertThat(equal).isTrue();
    }

    @Test
    public void testFindByImportanceUsingSliceWithPageSizeLessThanTotal() {
        final Set<PageableMemo> memos = findByWithSlice(200);
        boolean equal = memos.equals(normalMemos);
        assertThat(equal).isTrue();
    }

    @Test
    public void testFindByImportanceUsingSliceWithPageSizeGreaterThanTotal() {
        final Set<PageableMemo> memos = findByWithSlice(10000);
        boolean equal = memos.equals(normalMemos);
        assertThat(equal).isTrue();
    }

    private Flux<FeedResponse<PageableMemo>> getItemsWithOffsetAndLimit(int skipCount, int takeCount) {
        final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);

        final String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;

        final CosmosAsyncClient cosmosAsyncClient = applicationContext.getBean(CosmosAsyncClient.class);
        return cosmosAsyncClient.getDatabase(cosmosFactory.getDatabaseName())
                           .getContainer(collectionManager.getContainerName(PageableMemo.class))
                           .queryItems(query, options, PageableMemo.class)
                           .byPage();
    }

    private void verifyItemsWithOffsetAndLimit(int skipCount, int takeCount, int verifyCount) {
        final List<PageableMemo> itemsWithOffsetAndLimit = new ArrayList<>();
        final Flux<FeedResponse<PageableMemo>> itemsWithOffsetAndLimitFlux =
            getItemsWithOffsetAndLimit(skipCount, takeCount);
        StepVerifier.create(itemsWithOffsetAndLimitFlux)
                    .thenConsumeWhile(cosmosItemPropertiesFeedResponse -> {
                        itemsWithOffsetAndLimit.addAll(cosmosItemPropertiesFeedResponse.getResults());
                        return true;
                    })
                    .verifyComplete();
        assertThat(itemsWithOffsetAndLimit.size()).isEqualTo(verifyCount);
    }

    private Set<PageableMemo> findAllWithPageSize(int pageSize) {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Page<PageableMemo> page = repository.findAll(pageRequest);
        final Set<PageableMemo> outputSet = new HashSet<>(page.getContent());
        while (page.hasNext()) {
            final Pageable pageable = page.nextPageable();
            page = repository.findAll(pageable);
            outputSet.addAll((page.getContent()));
        }
        return outputSet;
    }

    private Set<PageableMemo> findByWithSlice(int pageSize) {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Slice<PageableMemo> slice = repository.findByImportance(Importance.NORMAL, pageRequest);
        final Set<PageableMemo> outputSet = new HashSet<>(slice.getContent());
        while (slice.hasNext()) {
            final Pageable pageable = slice.nextPageable();
            slice = repository.findByImportance(Importance.NORMAL, pageable);
            outputSet.addAll((slice.getContent()));
        }
        return outputSet;
    }
}
