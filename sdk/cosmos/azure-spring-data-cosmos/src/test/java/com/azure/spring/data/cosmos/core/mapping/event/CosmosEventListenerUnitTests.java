// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import static org.assertj.core.api.Assertions.*;

import com.azure.spring.data.cosmos.domain.IPerson;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.domain.Student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.Test;

public class CosmosEventListenerUnitTests {

    @Test
    public void afterLoadEffectGetsHandledCorrectly() {
        SamplePersonEventListener listener = new SamplePersonEventListener();
        listener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), Person.class, "container-1"));
        assertThat(listener.invokedOnAfterLoad).isTrue();
    }

    @Test
    public void afterLoadEventGetsFilteredForDomainType() {
        SamplePersonEventListener personListener = new SamplePersonEventListener();
        SampleStudentEventListener studentListener = new SampleStudentEventListener();
        personListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), Person.class, "container-1"));
        studentListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), Person.class, "container-1"));

        assertThat(personListener.invokedOnAfterLoad).isTrue();
        assertThat(studentListener.invokedOnAfterLoad).isFalse();
    }

    @Test
    public void afterLoadEventGetsFilteredForDomainTypeWorksForSubtypes() {
        SamplePersonEventListener personListener = new SamplePersonEventListener();
        SampleIPersonEventListener contactListener = new SampleIPersonEventListener();
        personListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), Person.class, "container-1"));
        contactListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), Person.class, "container-1"));

        assertThat(personListener.invokedOnAfterLoad).isTrue();
        assertThat(contactListener.invokedOnAfterLoad).isTrue();
    }

    @Test
    public void afterLoadEventGetsFilteredForDomainTypeWorksForSubtypes2() {
        SamplePersonEventListener personListener = new SamplePersonEventListener();
        SampleIPersonEventListener contactListener = new SampleIPersonEventListener();
        personListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), IPerson.class, "container-1"));
        contactListener.onApplicationEvent(new AfterLoadEvent<>(NullNode.getInstance(), IPerson.class, "container-1"));

        assertThat(personListener.invokedOnAfterLoad).isFalse();
        assertThat(contactListener.invokedOnAfterLoad).isTrue();
    }

    @Test
    public void dontInvokePersonCallbackForStudentEvent() {
        CosmosMappingEvent<JsonNode> event = new AfterLoadEvent<>(NullNode.getInstance(), Student.class, "container-1");
        SamplePersonEventListener listener = new SamplePersonEventListener();
        listener.onApplicationEvent(event);
        assertThat(listener.invokedOnAfterLoad).isFalse();
    }

    static class SamplePersonEventListener extends AbstractCosmosEventListener<Person> {

        boolean invokedOnAfterLoad;

        @Override
        public void onAfterLoad(AfterLoadEvent<Person> event) {
            invokedOnAfterLoad = true;
        }
    }

    static class SampleStudentEventListener extends AbstractCosmosEventListener<Student> {

        boolean invokedOnAfterLoad;

        @Override
        public void onAfterLoad(AfterLoadEvent<Student> event) {
            invokedOnAfterLoad = true;
        }
    }

    static class SampleIPersonEventListener extends AbstractCosmosEventListener<IPerson> {

        boolean invokedOnAfterLoad;

        @Override
        public void onAfterLoad(AfterLoadEvent<IPerson> event) {
            invokedOnAfterLoad = true;
        }
    }

}
