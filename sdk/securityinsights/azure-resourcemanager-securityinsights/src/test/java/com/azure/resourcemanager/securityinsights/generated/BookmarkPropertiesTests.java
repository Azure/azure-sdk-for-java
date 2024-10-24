// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.securityinsights.fluent.models.BookmarkProperties;
import com.azure.resourcemanager.securityinsights.models.AttackTactic;
import com.azure.resourcemanager.securityinsights.models.BookmarkEntityMappings;
import com.azure.resourcemanager.securityinsights.models.EntityFieldMapping;
import com.azure.resourcemanager.securityinsights.models.IncidentInfo;
import com.azure.resourcemanager.securityinsights.models.IncidentSeverity;
import com.azure.resourcemanager.securityinsights.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

public final class BookmarkPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        BookmarkProperties model = BinaryData.fromString(
            "{\"created\":\"2021-05-02T14:53:40Z\",\"createdBy\":{\"email\":\"xc\",\"name\":\"npc\",\"objectId\":\"344829bb-0e52-4cf1-85aa-d2c83dafc350\"},\"displayName\":\"ocohslkevleg\",\"labels\":[\"buhfmvfaxkffeiit\",\"lvmezyvshxmzsbbz\",\"ggi\",\"rxwburv\"],\"notes\":\"xjnspy\",\"query\":\"ptkoenkoukn\",\"queryResult\":\"dwtiukbldngkp\",\"updated\":\"2021-05-04T07:17:38Z\",\"updatedBy\":{\"email\":\"z\",\"name\":\"o\",\"objectId\":\"b6f0384d-cd06-45b7-9f30-7502cfede6d7\"},\"eventTime\":\"2020-12-27T20:47:04Z\",\"queryStartTime\":\"2021-06-03T11:47:46Z\",\"queryEndTime\":\"2021-05-26T22:48:17Z\",\"incidentInfo\":{\"incidentId\":\"cgygev\",\"severity\":\"Informational\",\"title\":\"yp\",\"relationName\":\"bpizcdrqjsdpydn\"},\"entityMappings\":[{\"entityType\":\"de\",\"fieldMappings\":[{\"identifier\":\"icwifsjtt\",\"value\":\"fbishcbkha\"},{\"identifier\":\"eyeam\",\"value\":\"hagalpbuxwgipwh\"},{\"identifier\":\"ow\",\"value\":\"shwankixzbinje\"}]},{\"entityType\":\"ttmrywnuzoqf\",\"fieldMappings\":[{\"identifier\":\"zrnkcqvyxlwh\",\"value\":\"sicohoqqnwvlry\"},{\"identifier\":\"w\",\"value\":\"eun\"},{\"identifier\":\"qhgyxzkonocukok\",\"value\":\"axuconuq\"},{\"identifier\":\"fkbey\",\"value\":\"wrmjmwvvjektc\"}]}],\"tactics\":[\"CredentialAccess\",\"DefenseEvasion\"],\"techniques\":[\"rsffrzpwvlqdqgbi\"]}")
            .toObject(BookmarkProperties.class);
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-02T14:53:40Z"), model.created());
        Assertions.assertEquals(UUID.fromString("344829bb-0e52-4cf1-85aa-d2c83dafc350"), model.createdBy().objectId());
        Assertions.assertEquals("ocohslkevleg", model.displayName());
        Assertions.assertEquals("buhfmvfaxkffeiit", model.labels().get(0));
        Assertions.assertEquals("xjnspy", model.notes());
        Assertions.assertEquals("ptkoenkoukn", model.query());
        Assertions.assertEquals("dwtiukbldngkp", model.queryResult());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-04T07:17:38Z"), model.updated());
        Assertions.assertEquals(UUID.fromString("b6f0384d-cd06-45b7-9f30-7502cfede6d7"), model.updatedBy().objectId());
        Assertions.assertEquals(OffsetDateTime.parse("2020-12-27T20:47:04Z"), model.eventTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-06-03T11:47:46Z"), model.queryStartTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-26T22:48:17Z"), model.queryEndTime());
        Assertions.assertEquals("cgygev", model.incidentInfo().incidentId());
        Assertions.assertEquals(IncidentSeverity.INFORMATIONAL, model.incidentInfo().severity());
        Assertions.assertEquals("yp", model.incidentInfo().title());
        Assertions.assertEquals("bpizcdrqjsdpydn", model.incidentInfo().relationName());
        Assertions.assertEquals("de", model.entityMappings().get(0).entityType());
        Assertions.assertEquals("icwifsjtt", model.entityMappings().get(0).fieldMappings().get(0).identifier());
        Assertions.assertEquals("fbishcbkha", model.entityMappings().get(0).fieldMappings().get(0).value());
        Assertions.assertEquals(AttackTactic.CREDENTIAL_ACCESS, model.tactics().get(0));
        Assertions.assertEquals("rsffrzpwvlqdqgbi", model.techniques().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        BookmarkProperties model
            = new BookmarkProperties().withCreated(OffsetDateTime.parse("2021-05-02T14:53:40Z"))
                .withCreatedBy(new UserInfo().withObjectId(UUID.fromString("344829bb-0e52-4cf1-85aa-d2c83dafc350")))
                .withDisplayName("ocohslkevleg")
                .withLabels(Arrays.asList("buhfmvfaxkffeiit", "lvmezyvshxmzsbbz", "ggi", "rxwburv"))
                .withNotes("xjnspy")
                .withQuery("ptkoenkoukn")
                .withQueryResult("dwtiukbldngkp")
                .withUpdated(OffsetDateTime.parse("2021-05-04T07:17:38Z"))
                .withUpdatedBy(new UserInfo().withObjectId(UUID.fromString("b6f0384d-cd06-45b7-9f30-7502cfede6d7")))
                .withEventTime(OffsetDateTime.parse("2020-12-27T20:47:04Z"))
                .withQueryStartTime(OffsetDateTime.parse("2021-06-03T11:47:46Z"))
                .withQueryEndTime(OffsetDateTime.parse("2021-05-26T22:48:17Z"))
                .withIncidentInfo(new IncidentInfo().withIncidentId("cgygev")
                    .withSeverity(IncidentSeverity.INFORMATIONAL)
                    .withTitle("yp")
                    .withRelationName("bpizcdrqjsdpydn"))
                .withEntityMappings(
                    Arrays.asList(
                        new BookmarkEntityMappings().withEntityType("de")
                            .withFieldMappings(Arrays.asList(
                                new EntityFieldMapping().withIdentifier("icwifsjtt").withValue("fbishcbkha"),
                                new EntityFieldMapping().withIdentifier("eyeam").withValue("hagalpbuxwgipwh"),
                                new EntityFieldMapping().withIdentifier("ow").withValue("shwankixzbinje"))),
                        new BookmarkEntityMappings().withEntityType("ttmrywnuzoqf")
                            .withFieldMappings(Arrays.asList(
                                new EntityFieldMapping().withIdentifier("zrnkcqvyxlwh").withValue("sicohoqqnwvlry"),
                                new EntityFieldMapping().withIdentifier("w").withValue("eun"),
                                new EntityFieldMapping().withIdentifier("qhgyxzkonocukok").withValue("axuconuq"),
                                new EntityFieldMapping().withIdentifier("fkbey").withValue("wrmjmwvvjektc")))))
                .withTactics(Arrays.asList(AttackTactic.CREDENTIAL_ACCESS, AttackTactic.DEFENSE_EVASION))
                .withTechniques(Arrays.asList("rsffrzpwvlqdqgbi"));
        model = BinaryData.fromObject(model).toObject(BookmarkProperties.class);
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-02T14:53:40Z"), model.created());
        Assertions.assertEquals(UUID.fromString("344829bb-0e52-4cf1-85aa-d2c83dafc350"), model.createdBy().objectId());
        Assertions.assertEquals("ocohslkevleg", model.displayName());
        Assertions.assertEquals("buhfmvfaxkffeiit", model.labels().get(0));
        Assertions.assertEquals("xjnspy", model.notes());
        Assertions.assertEquals("ptkoenkoukn", model.query());
        Assertions.assertEquals("dwtiukbldngkp", model.queryResult());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-04T07:17:38Z"), model.updated());
        Assertions.assertEquals(UUID.fromString("b6f0384d-cd06-45b7-9f30-7502cfede6d7"), model.updatedBy().objectId());
        Assertions.assertEquals(OffsetDateTime.parse("2020-12-27T20:47:04Z"), model.eventTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-06-03T11:47:46Z"), model.queryStartTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-26T22:48:17Z"), model.queryEndTime());
        Assertions.assertEquals("cgygev", model.incidentInfo().incidentId());
        Assertions.assertEquals(IncidentSeverity.INFORMATIONAL, model.incidentInfo().severity());
        Assertions.assertEquals("yp", model.incidentInfo().title());
        Assertions.assertEquals("bpizcdrqjsdpydn", model.incidentInfo().relationName());
        Assertions.assertEquals("de", model.entityMappings().get(0).entityType());
        Assertions.assertEquals("icwifsjtt", model.entityMappings().get(0).fieldMappings().get(0).identifier());
        Assertions.assertEquals("fbishcbkha", model.entityMappings().get(0).fieldMappings().get(0).value());
        Assertions.assertEquals(AttackTactic.CREDENTIAL_ACCESS, model.tactics().get(0));
        Assertions.assertEquals("rsffrzpwvlqdqgbi", model.techniques().get(0));
    }
}
