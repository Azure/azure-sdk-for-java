// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.integration;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CosmosPageRequest;
import com.microsoft.azure.spring.data.cosmosdb.domain.Address;
import com.microsoft.azure.spring.data.cosmosdb.domain.PageablePerson;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.PageablePersonRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PageablePersonRepositoryIT {

    private static final int TOTAL_CONTENT_SIZE = 50;

    private static final CosmosEntityInformation<PageablePerson, String> entityInformation =
        new CosmosEntityInformation<>(PageablePerson.class);

    private static CosmosTemplate staticTemplate;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private PageablePersonRepository repository;

    private static Set<PageablePerson> personSet;

    private static boolean isSetupDone;

    @Before
    public void setUp() {
        if (isSetupDone) {
            return;
        }
        personSet = new HashSet<>();
        template.createContainerIfNotExists(entityInformation);
        staticTemplate = template;

        //  Create larger documents with size more than 10 kb
        for (int i = 0; i < TOTAL_CONTENT_SIZE; i++) {
            final List<String> hobbies = new ArrayList<>();
            hobbies.add(StringUtils.repeat("hobbies-" + UUID.randomUUID().toString(),
                (int) FileUtils.ONE_KB * 10));
            final List<Address> address = new ArrayList<>();
            address.add(new Address("postalCode-" + UUID.randomUUID().toString(),
                "street-" + UUID.randomUUID().toString(),
                "city-" + UUID.randomUUID().toString()));
            final PageablePerson person = new PageablePerson(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                hobbies, address);
            repository.save(person);
            personSet.add(person);
        }
        isSetupDone = true;
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    //  Cosmos DB can return any number of documents less than or equal to requested page size
    //  Because of available RUs, the number of return documents vary.
    //  With documents more than 10 KB, and collection RUs 400,
    //  it usually return documents less than total content size.

    //  This test covers the case where page size is greater than returned documents
    @Test
    public void testFindAllWithPageSizeGreaterThanReturned() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(30, false);
        assertThat(outputSet).isEqualTo(personSet);
    }

    //  This test covers the case where page size is less than returned documents
    @Test
    public void testFindAllWithPageSizeLessThanReturned() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(5, false);
        assertThat(outputSet).isEqualTo(personSet);
    }

    //  This test covers the case where page size is greater than total number of documents
    @Test
    public void testFindAllWithPageSizeGreaterThanTotal() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(120, true);
        assertThat(outputSet).isEqualTo(personSet);
    }

    private Set<PageablePerson> findAllWithPageSize(int pageSize, boolean checkContentLimit) {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Page<PageablePerson> page = repository.findAll(pageRequest);
        final Set<PageablePerson> outputSet = new HashSet<>(page.getContent());
        if (checkContentLimit) {
            //  Make sure CosmosDB returns less number of documents than requested
            //  This will verify the functionality of new pagination implementation
            assertThat(page.getContent().size()).isLessThan(pageSize);
        }
        while (page.hasNext()) {
            final Pageable pageable = page.nextPageable();
            page = repository.findAll(pageable);
            outputSet.addAll((page.getContent()));
        }
        return outputSet;
    }
}
