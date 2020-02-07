/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.gremlin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootApplication
public class GremlinApplication implements CommandLineRunner {

    @Autowired
    private PersonRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(GremlinApplication.class, args);
    }

    public void run(String... vars) {
        final Person person = new Person("fake-id", "fake-name", 123);

        this.repository.deleteAll();
        this.repository.save(person);

        final Optional<Person> foundPerson = this.repository.findById(person.getId());
        Assert.isTrue(foundPerson.isPresent(), "optional of Person should be present");
        Assert.state(foundPerson.get().equals(person), "should be the equals");

        log.info("Find users by id: {}", foundPerson.get().toString());

        final List<Person> foundPersons = this.repository.findByNameAndLevel(person.getName(), person.getLevel());
        Assert.isTrue(foundPersons.size() == 1, "should be only one element");
        Assert.state(foundPersons.get(0).getId().equals(person.getId()), "should be the same id");

        log.info("Find users by name and level: {}", foundPersons.get(0).toString());
    }
}
