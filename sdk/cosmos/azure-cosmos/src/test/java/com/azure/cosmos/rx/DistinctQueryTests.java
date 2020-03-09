// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosPagedFlux;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.UnorderedDistinctMap;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DistinctQueryTests extends TestSuiteBase {
    private final String FIELD = "name";
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private ArrayList<CosmosItemProperties> docs = new ArrayList<>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public DistinctQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    private static String GetRandomName(Random rand) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name_" + rand.nextInt(100));

        return stringBuilder.toString();
    }

    private static City GetRandomCity(Random rand) {
        int index = rand.nextInt(3);
        switch (index) {
            case 0:
                return City.LosAngeles;
            case 1:
                return City.NewYork;
            case 2:
                return City.Seattle;
        }

        return City.LosAngeles;
    }

    private static double GetRandomIncome(Random rand) {
        return rand.nextDouble() * Double.MAX_VALUE;
    }

    private static int GetRandomAge(Random rand) {
        return rand.nextInt(100);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(boolean qmEnabled) {
        String query = "SELECT DISTINCT c.name from c";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        options.setPopulateQueryMetrics(qmEnabled);
        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<CosmosItemProperties> queryObservable = createdCollection.queryItems(query, options,
                                                                                             CosmosItemProperties.class);
        
        List<Object> nameList = docs.stream().map(d -> d.get(FIELD)).collect(Collectors.toList());
        List<Object> distinctNameList = nameList.stream().distinct().collect(Collectors.toList());

        FeedResponseListValidator<CosmosItemProperties> validator =
            new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(distinctNameList.size())
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                                     .requestChargeGreaterThanOrEqualTo(1.0)
                                     .build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(), validator, TIMEOUT);
    }

    @Test(groups = {"long"}, timeOut = TIMEOUT)
    public void queryDistinctDocuments() {

        List<String> queries = Arrays.asList(
            // basic distinct queries
            "SELECT %s VALUE null",
            "SELECT %s VALUE false",
            "SELECT %s VALUE true",
            "SELECT %s VALUE 1",
            "SELECT %s VALUE 'a'",
            "SELECT %s VALUE [null, true, false, 1, 'a']",
            "SELECT %s false AS p",
            "SELECT %s 1 AS p",
            "SELECT %s 'a' AS p",

            "SELECT %s VALUE null FROM c",
            "SELECT %s VALUE false FROM c",
            "SELECT %s VALUE 1 FROM c",
            "SELECT %s VALUE 'a' FROM c",
            "SELECT %s null AS p FROM c",
            "SELECT %s false AS p FROM c",
            "SELECT %s 1 AS p FROM c",
            "SELECT %s 'a' AS p FROM c",

            // number value distinct queries
            "SELECT %s VALUE c.income from c",
            "SELECT %s VALUE c.age from c",
            "SELECT %s c.income, c.income AS income2 from c",
            "SELECT %s c.income, c.age from c",

            // string value distinct queries
            "SELECT %s  c.name from c",
            "SELECT %s VALUE c.city from c",
            "SELECT %s VALUE c.partitionKey from c",
            "SELECT %s c.name, c.name AS name2 from c",
            "SELECT %s c.name, c.city from c",

            // array distinct queries
            "SELECT %s c.children from c",
            "SELECT %s c.children, c.children AS children2 from c",

            // object value distinct queries
            "SELECT %s VALUE c.pet from c",
            "SELECT %s c.pet, c.pet AS pet2 from c",

            // scalar expressions distinct query
            "SELECT %s VALUE ABS(c.age) FROM c",
            "SELECT %s VALUE LEFT(c.name, 1) FROM c",
            "SELECT %s VALUE c.name || ', ' || (c.city ?? '') FROM c",
            "SELECT %s VALUE ARRAY_LENGTH(c.children) FROM c",
            "SELECT %s VALUE IS_DEFINED(c.city) FROM c",
            "SELECT %s VALUE (c.children[0].age ?? 0) + (c.children[1].age ?? 0) FROM c",

            // distinct queries with order by : Value order by queries are not supported yet 
            "SELECT %s  c.name FROM c ORDER BY c.name ASC",
            "SELECT %s  c.age FROM c ORDER BY c.age",
            "SELECT %s  c.city FROM c ORDER BY c.city",
            "SELECT %s  c.city FROM c ORDER BY c.age",
            "SELECT %s  LEFT(c.name, 1) FROM c ORDER BY c.name",

            // distinct queries with top and no matching order by
            "SELECT %s TOP 2147483647 VALUE c.age FROM c",

            // distinct queries with top and  matching order by
            "SELECT %s TOP 2147483647  c.age FROM c ORDER BY c.age",

            // distinct queries with aggregates
            "SELECT %s VALUE MAX(c.age) FROM c",

            // distinct queries with joins
            "SELECT %s VALUE c.age FROM p JOIN c IN p.children",
            "SELECT %s p.age AS ParentAge, c.age ChildAge FROM p JOIN c IN p.children",
            "SELECT %s VALUE c.name FROM p JOIN c IN p.children",
            "SELECT %s p.name AS ParentName, c.name ChildName FROM p JOIN c IN p.children",

            // distinct queries in subqueries
            "SELECT %s r.age, s FROM r JOIN (SELECT DISTINCT VALUE c FROM (SELECT 1 a) c) s WHERE r.age > 25",
            "SELECT %s p.name, p.age FROM (SELECT DISTINCT * FROM r) p WHERE p.age > 25",

            // distinct queries in scalar subqeries
            "SELECT %s p.name, (SELECT DISTINCT VALUE p.age) AS Age FROM p",
            "SELECT %s p.name, p.age FROM p WHERE (SELECT DISTINCT VALUE LEFT(p.name, 1)) > 'A' AND (SELECT " +
                "DISTINCT VALUE p.age) > 21",
            "SELECT %s p.name, (SELECT DISTINCT VALUE p.age) AS Age FROM p WHERE (SELECT DISTINCT VALUE p.name) >" +
                " 'A' OR (SELECT DISTINCT VALUE p.age) > 21",

            //   select *
            "SELECT %s * FROM c"
        );

        for (String query : queries) {
            FeedOptions options = new FeedOptions();
            options.setMaxItemCount(5);
            options.setMaxDegreeOfParallelism(2);

            List<CosmosItemProperties> documentsFromWithDistinct = new ArrayList<>();
            List<CosmosItemProperties> documentsFromWithoutDistinct = new ArrayList<>();

            final String queryWithDistinct = String.format(query, "DISTINCT");
            final String queryWithoutDistinct = String.format(query, "");

            CosmosPagedFlux<CosmosItemProperties> queryObservable = createdCollection.queryItems(queryWithDistinct, options,
                                                                                                 CosmosItemProperties.class);


            Iterator<FeedResponse<CosmosItemProperties>> iterator = queryObservable.byPage().toIterable().iterator();
            Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
            UnorderedDistinctMap distinctMap = new UnorderedDistinctMap();

            while (iterator.hasNext()) {
                FeedResponse<CosmosItemProperties> next = iterator.next();
                for (CosmosItemProperties document : next.getResults()) {
                    if (distinctMap.add(document, outHash)) {
                        documentsFromWithoutDistinct.add(document);
                    }
                }
            }


            CosmosPagedFlux<CosmosItemProperties> queryObservableWithDistinct = createdCollection
                                                                                    .queryItems(queryWithoutDistinct, options,
                                                                                                CosmosItemProperties.class);


            iterator = queryObservableWithDistinct.byPage().toIterable().iterator();

            while (iterator.hasNext()) {
                FeedResponse<CosmosItemProperties> next = iterator.next();
                documentsFromWithDistinct.addAll(next.getResults());
            }

            assertThat(documentsFromWithDistinct.size()).isEqualTo(documentsFromWithoutDistinct.size());
        }

    }

    public void bulkInsert() {
        generateTestData();
        voidBulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData() {

        Random rand = new Random();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < 40; i++) {
            Person person = GetRandomPerson(rand);
            try {
                docs.add(new CosmosItemProperties(mapper.writeValueAsString(person)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private Pet GetRandomPet(Random rand) {
        String name = GetRandomName(rand);
        int age = GetRandomAge(rand);
        return new Pet(name, age);
    }

    public Person GetRandomPerson(Random rand) {
        String name = GetRandomName(rand);
        City city = GetRandomCity(rand);
        double income = GetRandomIncome(rand);
        List<Person> people = new ArrayList<Person>();
        if (rand.nextInt(10) % 10 == 0) {
            for (int i = 0; i < rand.nextInt(5); i++) {
                people.add(GetRandomPerson(rand));
            }
        }

        int age = GetRandomAge(rand);
        Pet pet = GetRandomPet(rand);
        UUID guid = UUID.randomUUID();
        Person p = new Person(name, city, income, people, age, pet, guid);
        return p;
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = {"simple"}, timeOut = 3 * SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert();

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    public enum City {
        NewYork,
        LosAngeles,
        Seattle
    }

    public final class Pet extends JsonSerializable {
        @JsonProperty("name")
        public String name;

        @JsonProperty("age")
        public int age;

        public Pet(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public final class Person extends JsonSerializable {
        @JsonProperty("name")
        public String Name;

        @JsonProperty("id")
        public String id;

        @JsonProperty("city")
        public City City;

        @JsonProperty("income")
        public double Income;

        @JsonProperty("children")
        public List<Person> Children;

        @JsonProperty("age")
        public int Age;

        @JsonProperty("pet")
        public Pet Pet;

        @JsonProperty("guid")
        public UUID Guid;

        public Person(String name, City city, double income, List<Person> children, int age, Pet pet, UUID guid) {
            this.Name = name;
            this.City = city;
            this.Income = income;
            this.Children = children;
            this.Age = age;
            this.Pet = pet;
            this.Guid = guid;
            this.id = UUID.randomUUID().toString();
        }
    }
}
