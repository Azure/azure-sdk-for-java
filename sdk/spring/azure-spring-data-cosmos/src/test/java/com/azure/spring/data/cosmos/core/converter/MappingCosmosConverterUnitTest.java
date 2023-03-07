// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.converter;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.convert.ObjectMapperFactory;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.Importance;
import com.azure.spring.data.cosmos.domain.Memo;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.domain.PersonWithEtag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MappingCosmosConverterUnitTest {
    private static final SimpleDateFormat DATE = new SimpleDateFormat(TestConstants.DATE_FORMAT);
    private static final SimpleDateFormat TIMEZONE_DATE = new SimpleDateFormat(TestConstants.DATE_TIMEZONE_FORMAT);

    private MappingCosmosConverter mappingCosmosConverter;

    @Mock
    ApplicationContext applicationContext;

    @Before
    public void setUp() {
        final CosmosMappingContext mappingContext = new CosmosMappingContext();
        final ObjectMapper objectMapper = new ObjectMapper();

        mappingContext.setApplicationContext(applicationContext);
        mappingContext.afterPropertiesSet();
        mappingContext.getPersistentEntity(Address.class);

        mappingCosmosConverter = new MappingCosmosConverter(mappingContext, objectMapper);
    }

    @Test
    public void covertAddressToDocumentCorrectly() {
        final Address testAddress = new Address(TestConstants.POSTAL_CODE, TestConstants.CITY, TestConstants.STREET);
        final JsonNode jsonNode = mappingCosmosConverter.writeJsonNode(testAddress);

        assertThat(jsonNode.get("id").asText()).isEqualTo(testAddress.getPostalCode());
        assertThat(jsonNode.get(TestConstants.PROPERTY_CITY).asText()).isEqualTo(testAddress.getCity());
        assertThat(jsonNode.get(TestConstants.PROPERTY_STREET).asText()).isEqualTo(testAddress.getStreet());
    }

    @Test
    public void convertDocumentToAddressCorrectly() {
        final ObjectNode objectNode = ObjectMapperFactory.getObjectMapper().createObjectNode();
        objectNode.put(TestConstants.PROPERTY_CITY, TestConstants.CITY);
        objectNode.put(TestConstants.PROPERTY_STREET, TestConstants.STREET);
        objectNode.put(TestConstants.PROPERTY_ID, TestConstants.POSTAL_CODE);

        final Address address = mappingCosmosConverter.read(Address.class, objectNode);

        assertThat(address.getPostalCode()).isEqualTo(TestConstants.POSTAL_CODE);
        assertThat(address.getCity()).isEqualTo(TestConstants.CITY);
        assertThat(address.getStreet()).isEqualTo(TestConstants.STREET);
    }

    @Test
    public void canWritePojoWithDateToDocument() throws ParseException {
        final Memo memo = new Memo(TestConstants.ID_1, TestConstants.MESSAGE, DATE.parse(TestConstants.DATE_STRING),
                Importance.NORMAL);
        final JsonNode jsonNode = mappingCosmosConverter.writeJsonNode(memo);

        assertThat(jsonNode.get(TestConstants.PROPERTY_ID).asText()).isEqualTo(memo.getId());
        assertThat(jsonNode.get(TestConstants.PROPERTY_MESSAGE).asText()).isEqualTo(memo.getMessage());
        assertThat(jsonNode.get(TestConstants.PROPERTY_DATE).asLong()).isEqualTo(memo.getDate().getTime());
    }

    @Test
    public void canReadPojoWithDateFromDocument() throws ParseException {
        final ObjectNode jsonObject = ObjectMapperFactory.getObjectMapper().createObjectNode();
        jsonObject.put(TestConstants.PROPERTY_MESSAGE, TestConstants.MESSAGE);

        final long date = DATE.parse(TestConstants.DATE_STRING).getTime();
        jsonObject.put(TestConstants.PROPERTY_DATE, date);
        jsonObject.put(TestConstants.PROPERTY_ID, TestConstants.ID_1);

        final Memo memo = mappingCosmosConverter.read(Memo.class, jsonObject);
        assertThat(memo.getId()).isEqualTo(TestConstants.ID_1);
        assertThat(memo.getMessage()).isEqualTo(TestConstants.MESSAGE);
        assertThat(memo.getDate().getTime()).isEqualTo(date);
    }

    @Test
    public void convertDateValueToMilliSeconds() throws ParseException {
        final Date date = TIMEZONE_DATE.parse(TestConstants.DATE_TIMEZONE_STRING);
        final long time = (Long) MappingCosmosConverter.toCosmosDbValue(date);

        assertThat(time).isEqualTo(TestConstants.MILLI_SECONDS);
    }

    @Test
    public void mapsVersionFieldToEtagAndRemovesVersionField() {
        final PersonWithEtag person = new PersonWithEtag(TestConstants.ID_1, TestConstants.FIRST_NAME,
            TestConstants.LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);

        final String etagValue = UUID.randomUUID().toString();
        person.setEtag(etagValue);

        final JsonNode jsonNode = mappingCosmosConverter.writeJsonNode(person);

        assertThat(jsonNode.get(TestConstants.PROPERTY_ETAG_RENAMED)).isNull();
        assertThat(jsonNode.get(TestConstants.PROPERTY_ETAG_DEFAULT).asText()).isEqualTo(etagValue);
    }

    @Test
    public void mapsEtagOntoVersionField() {
        final String etagValue = UUID.randomUUID().toString();
        final ObjectNode objectNode = ObjectMapperFactory.getObjectMapper().createObjectNode();
        objectNode.put(TestConstants.PROPERTY_ID, TestConstants.ID_1);
        objectNode.put(TestConstants.PROPERTY_FIRST_NAME, TestConstants.FIRST_NAME);
        objectNode.put(TestConstants.PROPERTY_LAST_NAME, TestConstants.LAST_NAME);
        objectNode.put(TestConstants.PROPERTY_ETAG_DEFAULT, etagValue);

        final PersonWithEtag person = mappingCosmosConverter.read(PersonWithEtag.class, objectNode);

        assertThat(person.getEtag()).isEqualTo(etagValue);
    }

    @Test
    public void mapsDefaultEtag() {
        final String etagValue = UUID.randomUUID().toString();
        final ObjectNode objectNode = ObjectMapperFactory.getObjectMapper().createObjectNode();
        objectNode.put(TestConstants.PROPERTY_ID, TestConstants.ID_1);
        objectNode.put(TestConstants.PROPERTY_FIRST_NAME, TestConstants.FIRST_NAME);
        objectNode.put(TestConstants.PROPERTY_LAST_NAME, TestConstants.LAST_NAME);
        objectNode.put(TestConstants.PROPERTY_ETAG_DEFAULT, etagValue);

        final Person person = mappingCosmosConverter.read(Person.class, objectNode);

        assertThat(person.get_etag()).isEqualTo(etagValue);

        final JsonNode jsonNode = mappingCosmosConverter.writeJsonNode(person);

        assertThat(jsonNode.get(TestConstants.PROPERTY_ETAG_DEFAULT).asText()).isEqualTo(etagValue);
    }
}

