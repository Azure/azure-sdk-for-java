// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.PageablePerson;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PageablePersonRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
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

    private static final int TOTAL_CONTENT_SIZE = 25;
    public static final int ONE_KB = 1024;

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
                ONE_KB * 10));
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
    //  it usually return documents less than total 12 documents.

    //  This test covers the case where page size is greater than returned documents limit
    @Test
    public void testFindAllWithPageSizeGreaterThanReturnedLimit() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(15, true);
        boolean equals = outputSet.equals(personSet);
        assertThat(equals).isTrue();
    }

    //  This test covers the case where page size is less than returned documents limit
    @Test
    public void testFindAllWithPageSizeLessThanReturnedLimit() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(5, false);
        boolean equals = outputSet.equals(personSet);
        assertThat(equals).isTrue();
    }

    //  This test covers the case where page size is greater than total number of documents
    @Test
    public void testFindAllWithPageSizeGreaterThanTotal() {
        final Set<PageablePerson> outputSet = findAllWithPageSize(50, true);
        boolean equals = outputSet.equals(personSet);
        assertThat(equals).isTrue();
    }

    private Set<PageablePerson> findAllWithPageSize(int pageSize, boolean checkContentLimit) {
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Page<PageablePerson> page = repository.findAll(pageRequest);
        List<PageablePerson> content = page.getContent();
        final Set<PageablePerson> outputSet = new HashSet<>(content);
        if (checkContentLimit) {
            //  Make sure CosmosDB returns less number of documents than requested
            //  This will verify the functionality of new pagination implementation
            assertThat(content.size()).isLessThan(pageSize);
        }
        while (page.hasNext()) {
            final Pageable pageable = page.nextPageable();
            page = repository.findAll(pageable);
            content = page.getContent();
            outputSet.addAll((content));
        }
        return outputSet;
    }
}
