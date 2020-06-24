// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package example.springdata.cosmosdb;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Application implements CommandLineRunner{

    private static final String ID_1 = "id_1";
    private static final String NAME_1 = "myName_1";

    private static final String ID_2 = "id_2";
    private static final String NAME_2 = "myName_2";

    private static final String ID_3 = "id_3";
    private static final String NAME_3 = "myName_3";

    private static final String EMAIL = "xxx-xx@xxx.com";
    private static final String POSTAL_CODE = "0123456789";
    private static final String STREET = "zixing road";
    private static final String CITY = "shanghai";
    private static final String ROLE_CREATOR = "creator";
    private static final String ROLE_CONTRIBUTOR = "contributor";
    private static final int COST_CREATOR = 234;
    private static final int COST_CONTRIBUTOR = 666;
    private static final Long COUNT = 123L;

    private final Address address = new Address(POSTAL_CODE, STREET, CITY);
    private final Role creator = new Role(ROLE_CREATOR, COST_CREATOR);
    private final Role contributor = new Role(ROLE_CONTRIBUTOR, COST_CONTRIBUTOR);
    private final User user_1 = new User(ID_1, EMAIL, NAME_1, COUNT, address, Arrays.asList(creator, contributor));
    private final User user_2 = new User(ID_2, EMAIL, NAME_2, COUNT, address, Arrays.asList(creator, contributor));
    private final User user_3 = new User(ID_3, EMAIL, NAME_3, COUNT, address, Arrays.asList(creator, contributor));

    @Autowired
    private UserRepository repository;

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        printList(this.repository.findByEmailOrName(this.user_1.getEmail(),
            this.user_1.getName()).collectList().block());

        printList(this.repository.findByCount(COUNT,
            Sort.by(new Sort.Order(Sort.Direction.ASC, "count"))).collectList().block());

        printList(this.repository.findByNameIn(Arrays.asList(this.user_1.getName(),
            "fake-name")).collectList().block());
    }

    @PostConstruct
    public void setup() {
        this.repository.save(user_1).block();
        this.repository.saveAll(Lists.newArrayList(user_2, user_3)).collectList().block();
    }

    @PreDestroy
    public void cleanup() {
        this.repository.deleteAll().block();
    }

    private void printList(List<User> users) {
        users.forEach(System.out::println);
    }
}
