package com.azure.spring.data.cosmos.core.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.support.ExampleMatcherAccessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryByExampleCriteriaBuilderTest {

    public static class TestEntity {
        String name;
        String description;

        TestEntity(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    @Test
    void testGetPredicateWithExactMatch() {
        TestEntity probe = new TestEntity("testName", "testDescription");
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.EXACT);
        Example<TestEntity> example = Example.of(probe, matcher);

        Criteria criteria = QueryByExampleCriteriaBuilder.getPredicate(example);

        assertNotNull(criteria);
        assertEquals(CriteriaType.AND, criteria.getType());
    }

    @Test
    void testGetPredicateWithContainingMatch() {
        TestEntity probe = new TestEntity("test", "description");
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);
        Example<TestEntity> example = Example.of(probe, matcher);

        Criteria criteria = QueryByExampleCriteriaBuilder.getPredicate(example);

        assertNotNull(criteria);
        assertEquals(CriteriaType.AND, criteria.getType());
    }

    @Test
    void testGetPredicateWithNullHandlerInclude() {
        TestEntity probe = new TestEntity(null, "description");
        ExampleMatcher matcher = ExampleMatcher.matching().withNullHandler(ExampleMatcher.NullHandler.INCLUDE);
        Example<TestEntity> example = Example.of(probe, matcher);

        Criteria criteria = QueryByExampleCriteriaBuilder.getPredicate(example);

        assertNotNull(criteria);
        assertEquals(CriteriaType.AND, criteria.getType());
    }

    @Test
    void testGetPredicatesWithEmptyProbe() {
        TestEntity probe = new TestEntity("", "");
        ExampleMatcher matcher = ExampleMatcher.matching();

        List<Criteria> criteriaList =
            QueryByExampleCriteriaBuilder.getPredicates(
                "", probe, TestEntity.class, new ExampleMatcherAccessor(matcher), null);

        assertNotNull(criteriaList);
        assertFalse(criteriaList.isEmpty());
    }
}
