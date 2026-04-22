// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;

import java.util.Arrays;

import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_KEY;
import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_OBJECT;
import static org.junit.jupiter.api.Assertions.*;

public class CosmosQueryUnitTest {

    @Test
    public void testDocumentQueryCreate() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, CRITERIA_KEY,
                Arrays.asList(CRITERIA_OBJECT), Part.IgnoreCaseType.NEVER);

        final CosmosQuery query = new CosmosQuery(criteria);

        assertEquals(criteria, query.getCriteria());
        assertEquals(Sort.unsorted(), query.getSort());
        assertEquals(Pageable.unpaged(), query.getPageable());
        assertEquals(Part.IgnoreCaseType.NEVER, criteria.getIgnoreCase());
    }
}
