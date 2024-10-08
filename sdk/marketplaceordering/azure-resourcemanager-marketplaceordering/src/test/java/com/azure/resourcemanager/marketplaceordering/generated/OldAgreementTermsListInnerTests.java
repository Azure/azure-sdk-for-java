// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.marketplaceordering.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.marketplaceordering.fluent.models.OldAgreementTermsInner;
import com.azure.resourcemanager.marketplaceordering.fluent.models.OldAgreementTermsListInner;
import com.azure.resourcemanager.marketplaceordering.models.State;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class OldAgreementTermsListInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        OldAgreementTermsListInner model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"id\":\"odjpslwejd\",\"publisher\":\"wryoqpsoacc\",\"offer\":\"zakljlahbc\",\"signDate\":\"2021-05-25T21:39:12Z\",\"cancelDate\":\"2021-06-19T00:38:01Z\",\"state\":\"Active\"},\"id\":\"osygex\",\"name\":\"aojakhmsbzjhcrz\",\"type\":\"vdphlxaolthqtr\"},{\"properties\":{\"id\":\"bpf\",\"publisher\":\"s\",\"offer\":\"zgvfcjrwz\",\"signDate\":\"2021-08-01T19:04:33Z\",\"cancelDate\":\"2021-04-21T06:51:05Z\",\"state\":\"Canceled\"},\"id\":\"lluwfzitonpeq\",\"name\":\"pjkjlxofpdv\",\"type\":\"pfxxy\"},{\"properties\":{\"id\":\"i\",\"publisher\":\"ayhuy\",\"offer\":\"kpode\",\"signDate\":\"2021-04-04T07:27:40Z\",\"cancelDate\":\"2021-05-29T14:24:22Z\",\"state\":\"Canceled\"},\"id\":\"vamih\",\"name\":\"ognarxzxtheotus\",\"type\":\"vyevcciqi\"},{\"properties\":{\"id\":\"un\",\"publisher\":\"wjzrnfygxgisp\",\"offer\":\"vtz\",\"signDate\":\"2021-09-18T00:19:24Z\",\"cancelDate\":\"2021-02-15T20:29:21Z\",\"state\":\"Canceled\"},\"id\":\"jofxqe\",\"name\":\"fjaeq\",\"type\":\"hqjbasvmsmj\"}]}")
            .toObject(OldAgreementTermsListInner.class);
        Assertions.assertEquals("odjpslwejd", model.value().get(0).idPropertiesId());
        Assertions.assertEquals("wryoqpsoacc", model.value().get(0).publisher());
        Assertions.assertEquals("zakljlahbc", model.value().get(0).offer());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-25T21:39:12Z"), model.value().get(0).signDate());
        Assertions.assertEquals(OffsetDateTime.parse("2021-06-19T00:38:01Z"), model.value().get(0).cancelDate());
        Assertions.assertEquals(State.ACTIVE, model.value().get(0).state());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        OldAgreementTermsListInner model = new OldAgreementTermsListInner().withValue(Arrays.asList(
            new OldAgreementTermsInner().withIdPropertiesId("odjpslwejd")
                .withPublisher("wryoqpsoacc")
                .withOffer("zakljlahbc")
                .withSignDate(OffsetDateTime.parse("2021-05-25T21:39:12Z"))
                .withCancelDate(OffsetDateTime.parse("2021-06-19T00:38:01Z"))
                .withState(State.ACTIVE),
            new OldAgreementTermsInner().withIdPropertiesId("bpf")
                .withPublisher("s")
                .withOffer("zgvfcjrwz")
                .withSignDate(OffsetDateTime.parse("2021-08-01T19:04:33Z"))
                .withCancelDate(OffsetDateTime.parse("2021-04-21T06:51:05Z"))
                .withState(State.CANCELED),
            new OldAgreementTermsInner().withIdPropertiesId("i")
                .withPublisher("ayhuy")
                .withOffer("kpode")
                .withSignDate(OffsetDateTime.parse("2021-04-04T07:27:40Z"))
                .withCancelDate(OffsetDateTime.parse("2021-05-29T14:24:22Z"))
                .withState(State.CANCELED),
            new OldAgreementTermsInner().withIdPropertiesId("un")
                .withPublisher("wjzrnfygxgisp")
                .withOffer("vtz")
                .withSignDate(OffsetDateTime.parse("2021-09-18T00:19:24Z"))
                .withCancelDate(OffsetDateTime.parse("2021-02-15T20:29:21Z"))
                .withState(State.CANCELED)));
        model = BinaryData.fromObject(model).toObject(OldAgreementTermsListInner.class);
        Assertions.assertEquals("odjpslwejd", model.value().get(0).idPropertiesId());
        Assertions.assertEquals("wryoqpsoacc", model.value().get(0).publisher());
        Assertions.assertEquals("zakljlahbc", model.value().get(0).offer());
        Assertions.assertEquals(OffsetDateTime.parse("2021-05-25T21:39:12Z"), model.value().get(0).signDate());
        Assertions.assertEquals(OffsetDateTime.parse("2021-06-19T00:38:01Z"), model.value().get(0).cancelDate());
        Assertions.assertEquals(State.ACTIVE, model.value().get(0).state());
    }
}
