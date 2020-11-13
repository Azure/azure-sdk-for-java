// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin;

import com.azure.spring.sample.data.gremlin.domain.Network;
import com.azure.spring.sample.data.gremlin.domain.Person;
import com.azure.spring.sample.data.gremlin.domain.Relation;
import com.azure.spring.sample.data.gremlin.repository.NetworkRepository;
import com.azure.spring.sample.data.gremlin.repository.PersonRepository;
import com.azure.spring.sample.data.gremlin.repository.RelationRepository;
import com.azure.spring.data.gremlin.common.GremlinFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class AzureSpringDataSampleGremlinApplication {

    private final Person person1 = new Person("person-id-1", "person-name-1", "10", "address-1");
    private final Person person2 = new Person("person-id-2", "person-name-2", "20", "address-2");
    private final Person person3 = new Person("person-id-3", "person-name-3", "30", "address-3");
    private final Person person4 = new Person("person-id-4", "person-name-4", "40", "address-4");
    private final Relation relation12 = new Relation("relation-id-1", "relation-name-1", person1, person2);
    private final Relation relation13 = new Relation("relation-id-2", "relation-name-2", person1, person3);
    private final Relation relation14 = new Relation("relation-id-3", "relation-name-3", person1, person4);
    private final Relation relation34 = new Relation("relation-id-4", "relation-name-4", person3, person4);
    private final Network network = new Network();

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RelationRepository relationRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private GremlinFactory gremlinFactory;

    public static void main(String... args) {
        SpringApplication.run(AzureSpringDataSampleGremlinApplication.class, args);
    }

    @PostConstruct
    public void setup() {
        personRepository.deleteAll();
        relationRepository.deleteAll();
        networkRepository.deleteAll();

        network.getVertexes().add(person1);
        network.getVertexes().add(person2);
        network.getVertexes().add(person3);
        network.getVertexes().add(person4);
        network.getEdges().add(relation12);
        network.getEdges().add(relation13);
        network.getEdges().add(relation14);
        network.getEdges().add(relation34);

        networkRepository.save(network);
    }
}
