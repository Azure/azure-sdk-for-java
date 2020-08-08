// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.rx.pojos.City;
import com.azure.cosmos.rx.pojos.Person;
import com.azure.cosmos.rx.pojos.Pet;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Triple;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupByQueryTests extends TestSuiteBase {
    private final static int INSERT_DOCUMENTS_CNT = 40;
    List<Person> personList;
    private CosmosAsyncContainer createdCollection;
    private ArrayList<InternalObjectNode> docs = new ArrayList<>();
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public GroupByQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    private static String getRandomName(Random rand) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name_" + rand.nextInt(100));

        return stringBuilder.toString();
    }

    private static City getRandomCity(Random rand) {
        int index = rand.nextInt(3);
        switch (index) {
            case 0:
                return City.LOS_ANGELES;
            case 1:
                return City.NEW_YORK;
            case 2:
                return City.SEATTLE;
        }

        return City.LOS_ANGELES;
    }

    private static double getRandomIncome(Random rand) {
        return rand.nextDouble() * Double.MAX_VALUE;
    }

    private static int getRandomAge(Random rand) {
        return rand.nextInt(100);
    }

    @DataProvider
    public static Object[] groupByConfigProvider() {
        // left: groupBy property
        // right: maxItemCount
        return new Object[]{
            Triple.of(
                "city",
                new Function<Person, City>() {
                    @Override
                    public City apply(Person person) {
                        return person.getCity();
                    }
                }, 35),
            Triple.of(
                "guid",
                new Function<Person, UUID>() {
                @Override
                public UUID apply(Person person) {
                    return person.getGuid();
                }
            }, INSERT_DOCUMENTS_CNT/2) // this is to make sure we are testing paging scenario
        };
    }

    @Test(groups = {"simple"}, dataProvider = "groupByConfigProvider", timeOut = TIMEOUT)
    public void queryDocuments(Triple<String, Function<Person, Object>, Integer> groupByConfig) {
        boolean qmEnabled = true;

        String query =
            String.format("SELECT sum(c.age) as sum_age, c.%s FROM c group by c.%s", groupByConfig.getLeft(), groupByConfig.getLeft());
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, groupByConfig.getRight());
        options.setQueryMetricsEnabled(qmEnabled);
        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<JsonNode> queryObservable = createdCollection.queryItems(query,
                                                                                 options,
                                                                                 JsonNode.class);
        Map<Object, Integer> resultMap = personList.stream()
                                           .collect(Collectors.groupingBy(groupByConfig.getMiddle(),
                                                                          Collectors.summingInt(Person::getAge)));

        List<Document> expectedDocumentsList = new ArrayList<>();
        resultMap.forEach((groupByObj, sum) ->
                          {
                              Document d = new Document();
                              d.set("sum_age", sum);
                              d.set(groupByConfig.getLeft(), groupByObj);
                              expectedDocumentsList.add(d);
                          });


        List<FeedResponse<JsonNode>> queryResultPages = queryObservable.byPage().collectList().block();

        List<JsonNode> queryResults = new ArrayList<>();

        queryResultPages
            .forEach(feedResponse -> queryResults.addAll(feedResponse.getResults()));

        assertThat(expectedDocumentsList.size()).isEqualTo(queryResults.size());

        for (int i = 0; i < expectedDocumentsList.size(); i++) {
            assertThat(expectedDocumentsList.get(i).toString().equals(queryResults.get(i).toString()));
        }

        double totalRequestCharge =  queryResultPages.stream().collect(Collectors.summingDouble(FeedResponse::getRequestCharge));
        assertThat(totalRequestCharge).isGreaterThan(0);
    }

    public void bulkInsert() {
        generateTestData(INSERT_DOCUMENTS_CNT);
        voidBulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData(int documentCnt) {
        personList = new ArrayList<>();
        Random rand = new Random();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < documentCnt; i++) {
            Person person = getRandomPerson(rand);
            try {
                docs.add(new InternalObjectNode(mapper.writeValueAsString(person)));
                personList.add(person);
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private Pet getRandomPet(Random rand) {
        String name = getRandomName(rand);
        int age = getRandomAge(rand);
        return new Pet(name, age);
    }

    public Person getRandomPerson(Random rand) {
        String name = getRandomName(rand);
        City city = getRandomCity(rand);
        double income = getRandomIncome(rand);
        List<Person> people = new ArrayList<Person>();
        if (rand.nextInt(10) % 10 == 0) {
            for (int i = 0; i < rand.nextInt(5); i++) {
                people.add(getRandomPerson(rand));
            }
        }

        int age = getRandomAge(rand);
        Pet pet = getRandomPet(rand);
        UUID guid = UUID.randomUUID();
        return new Person(name, city, income, people, age, pet, guid);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = {"simple"}, timeOut = 3 * SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
        bulkInsert();
        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());
    }

}
