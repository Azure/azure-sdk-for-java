/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.gremlin;

import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import example.springdata.gremlin.domain.Network;
import example.springdata.gremlin.domain.Person;
import example.springdata.gremlin.domain.Relation;
import example.springdata.gremlin.repository.NetworkRepository;
import example.springdata.gremlin.repository.PersonRepository;
import example.springdata.gremlin.repository.RelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class Application {

    private static final String PERSON_ID = "89757";
    private static final String PERSON_ID_0 = "0123456789";
    private static final String PERSON_ID_1 = "666666";
    private static final String PERSON_NAME = "person-name";
    private static final String PERSON_NAME_0 = "person-No.0";
    private static final String PERSON_NAME_1 = "person-No.1";
    private static final String PERSON_AGE = "4";
    private static final String PERSON_AGE_0 = "18";
    private static final String PERSON_AGE_1 = "27";

    private static final String RELATION_ID = "2333";
    private static final String RELATION_NAME = "brother";

    private final Person person = new Person(PERSON_ID, PERSON_NAME, PERSON_AGE);
    private final Person person0 = new Person(PERSON_ID_0, PERSON_NAME_0, PERSON_AGE_0);
    private final Person person1 = new Person(PERSON_ID_1, PERSON_NAME_1, PERSON_AGE_1);
    private final Relation relation = new Relation(RELATION_ID, RELATION_NAME, person0, person1);
    private final Network network = new Network();

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private RelationRepository relationRepo;

    @Autowired
    private NetworkRepository networkRepo;

    @Autowired
    private GremlinFactory factory;

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void setup() {
        this.networkRepo.deleteAll();

        this.network.getEdges().add(this.relation);
        this.network.getVertexes().add(this.person);
        this.network.getVertexes().add(this.person0);
        this.network.getVertexes().add(this.person1);

        this.networkRepo.save(this.network);
    }
}
