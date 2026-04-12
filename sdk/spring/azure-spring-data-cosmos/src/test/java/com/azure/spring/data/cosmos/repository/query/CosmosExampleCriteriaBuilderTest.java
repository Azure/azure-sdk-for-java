// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.mapping.CosmosPersistentProperty;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.Part;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link CosmosExampleCriteriaBuilder}.
 */
public class CosmosExampleCriteriaBuilderTest {

    private MappingContext<?, CosmosPersistentProperty> mappingContext;

    @BeforeEach
    public void setUp() {
        CosmosMappingContext context = new CosmosMappingContext();
        context.setInitialEntitySet(Set.of(Person.class));
        context.afterPropertiesSet();
        this.mappingContext = context;
    }

    @Test
    public void testAllNullProbeShouldReturnAllCriteria() {
        Person probe = new Person();
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertNotNull(criteria);
        assertEquals(CriteriaType.ALL, criteria.getType());
    }

    @Test
    public void testSingleStringFieldProbe() {
        Person probe = new Person();
        probe.setFirstName("John");
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertNotNull(criteria);
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals("firstName", criteria.getSubject());
        assertEquals("John", criteria.getSubjectValues().get(0));
    }

    @Test
    public void testMultipleFieldsProbe() {
        Person probe = new Person();
        probe.setFirstName("John");
        probe.setLastName("Doe");
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertNotNull(criteria);
        assertEquals(CriteriaType.AND, criteria.getType());
        assertEquals(2, criteria.getSubCriteria().size());
    }

    @Test
    public void testIntegerFieldUsesIsEqual() {
        Person probe = new Person();
        probe.setAge(25);
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertNotNull(criteria);
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals("age", criteria.getSubject());
        assertEquals(25, criteria.getSubjectValues().get(0));
    }

    @Test
    public void testStringContainingMatcher() {
        Person probe = new Person();
        probe.setFirstName("ohn");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.CONTAINING, criteria.getType());
    }

    @Test
    public void testStringStartingWithMatcher() {
        Person probe = new Person();
        probe.setFirstName("Jo");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.STARTS_WITH, criteria.getType());
    }

    @Test
    public void testStringEndingWithMatcher() {
        Person probe = new Person();
        probe.setFirstName("hn");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.ENDING);
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.ENDS_WITH, criteria.getType());
    }

    @Test
    public void testCaseInsensitiveMatching() {
        Person probe = new Person();
        probe.setFirstName("john");
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase();
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.STRING_EQUALS, criteria.getType());
        assertEquals(Part.IgnoreCaseType.ALWAYS, criteria.getIgnoreCase());
    }

    @Test
    public void testIgnoredPaths() {
        Person probe = new Person();
        probe.setFirstName("John");
        probe.setLastName("Doe");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnorePaths("lastName");
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals("firstName", criteria.getSubject());
    }

    @Test
    public void testNullHandlingInclude() {
        Person probe = new Person();
        probe.setFirstName("John");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIncludeNullValues()
            .withIgnorePaths("hobbies", "shippingAddresses", "age", "passportIdsByCountry");
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertNotNull(criteria);
        assertEquals(CriteriaType.AND, criteria.getType());
    }

    @Test
    public void testAnyMatchingUsesOr() {
        Person probe = new Person();
        probe.setFirstName("John");
        probe.setLastName("Doe");
        ExampleMatcher matcher = ExampleMatcher.matchingAny();
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.OR, criteria.getType());
    }

    @Test
    public void testVersionFieldIsSkipped() {
        Person probe = new Person();
        probe.setFirstName("John");
        probe.set_etag("some-etag");
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals("firstName", criteria.getSubject());
    }

    @Test
    public void testNullIdIsSkipped() {
        Person probe = new Person();
        probe.setFirstName("John");
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.IS_EQUAL, criteria.getType());
        assertEquals("firstName", criteria.getSubject());
    }

    @Test
    public void testNonNullIdIsIncluded() {
        Person probe = new Person();
        probe.setId("person-1");
        probe.setFirstName("John");
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.AND, criteria.getType());
        assertEquals(2, criteria.getSubCriteria().size());
    }

    @Test
    public void testPropertySpecificMatcher() {
        Person probe = new Person();
        probe.setFirstName("Jo");
        probe.setLastName("Doe");
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withMatcher("firstName", ExampleMatcher.GenericPropertyMatcher::startsWith)
            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatcher::exact);
        Example<Person> example = Example.of(probe, matcher);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.AND, criteria.getType());
        Criteria firstNameCriteria = criteria.getSubCriteria().get(0);
        Criteria lastNameCriteria = criteria.getSubCriteria().get(1);
        assertEquals(CriteriaType.STARTS_WITH, firstNameCriteria.getType());
        assertEquals(CriteriaType.IS_EQUAL, lastNameCriteria.getType());
    }

    @Test
    public void testStringAndIntegerMixedProbe() {
        Person probe = new Person();
        probe.setFirstName("John");
        probe.setAge(30);
        Example<Person> example = Example.of(probe);
        Criteria criteria = CosmosExampleCriteriaBuilder.buildCriteria(example, mappingContext);
        assertEquals(CriteriaType.AND, criteria.getType());
        assertEquals(2, criteria.getSubCriteria().size());
    }
}
