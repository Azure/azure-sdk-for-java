// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.query.UnorderedDistinctMap;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.rx.pojos.City;
import com.azure.cosmos.rx.pojos.Person;
import com.azure.cosmos.rx.pojos.Pet;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DistinctQueryTests extends TestSuiteBase {
    private final int TIMEOUT_LONG = 240000;
    private final String FIELD = "name";
    private CosmosAsyncContainer createdCollection;

    private ArrayList<Person> docs = new ArrayList<>();
    private ArrayList<InternalObjectNode> propertiesDocs = new ArrayList<>();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public DistinctQueryTests(CosmosClientBuilder clientBuilder) {
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(Boolean qmEnabled) {
        String query = "SELECT DISTINCT c.name from c";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable =
            createdCollection.queryItems(query,
                                         options,
                                         InternalObjectNode.class);
        List<Object> nameList = docs.stream()
                                    .map(d -> d.getName())
                                    .collect(Collectors.toList());
        List<Object> collect = propertiesDocs.stream().map(d -> d.get(FIELD)).collect(Collectors.toList());
        nameList.add(collect);

        List<Object> distinctNameList = nameList.stream().distinct().collect(Collectors.toList());

        FeedResponseListValidator<InternalObjectNode> validator =
            new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(distinctNameList.size())
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                                     .requestChargeGreaterThanOrEqualTo(1.0)
                                     .build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(5), validator, TIMEOUT);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT_LONG)
    public void queryDistinctDocuments() {

        Map<String, Boolean> queries = ImmutableMap.<String, Boolean>builder()
             // basic distinct queries
            .put("SELECT %s VALUE null", true)
            .put("SELECT %s VALUE false", false)
            .put("SELECT %s VALUE true", false)
            .put("SELECT %s VALUE 1", false)
            .put("SELECT %s VALUE 'a'", true)
            .put("SELECT %s VALUE [null, true, false, 1, 'a']", false)
            .put("SELECT %s false AS p", true)
            .put("SELECT %s 1 AS p", false)
            .put("SELECT %s 'a' AS p", false)
            .put("SELECT %s VALUE null FROM c", false)
            .put("SELECT %s VALUE false FROM c", false)
            .put("SELECT %s VALUE 1 FROM c", false)
            .put("SELECT %s VALUE 'a' FROM c", false)
            .put("SELECT %s null AS p FROM c", false)
            .put("SELECT %s false AS p FROM c", false)
            .put("SELECT %s 1 AS p FROM c", false)
            .put("SELECT %s 'a' AS p FROM c", false)

            // number value distinct queries
            .put("SELECT %s VALUE c.income from c", true)
            .put("SELECT %s VALUE c.age from c", false)
            .put("SELECT %s c.income, c.income AS income2 from c",  false)
            .put("SELECT %s c.income, c.age from c", false)

            // string value distinct queries
            .put("SELECT %s  c.name from c", true)
            .put("SELECT %s VALUE c.city from c", false)
            .put("SELECT %s c.name, c.name AS name2 from c", false)
            .put("SELECT %s c.name, c.city from c", false)

            // array distinct queries
            .put("SELECT %s c.children from c", true)
            .put("SELECT %s c.children, c.children AS children2 from c", false)

            // object value distinct queries
            .put("SELECT %s VALUE c.pet from c", true)
            .put("SELECT %s c.pet, c.pet AS pet2 from c", false)

            // scalar expressions distinct query
            .put("SELECT %s VALUE ABS(c.age) FROM c", true)
            .put("SELECT %s VALUE LEFT(c.name, 1) FROM c", false)
            .put("SELECT %s VALUE c.name || ', ' || (c.city ?? '') FROM c", false)
            .put("SELECT %s VALUE ARRAY_LENGTH(c.children) FROM c", false)
            .put("SELECT %s VALUE IS_DEFINED(c.city) FROM c", false)
            .put("SELECT %s VALUE (c.children[0].age ?? 0) + (c.children[1].age ?? 0) FROM c", false)

            // distinct queries with order by
            .put("SELECT %s  c.name FROM c ORDER BY c.name ASC", false)
            .put("SELECT %s  c.age FROM c ORDER BY c.age", false)
            .put("SELECT %s  c.city FROM c ORDER BY c.city", false)
            .put("SELECT %s  c.city FROM c ORDER BY c.age", false)
            .put("SELECT %s  LEFT(c.name, 1) FROM c ORDER BY c.name", false)

            // distinct queries with top and no matching order by
            .put("SELECT %s TOP 2147483647 VALUE c.age FROM c", false)

            // distinct queries with top and  matching order by
            .put("SELECT %s TOP 2147483647  c.age FROM c ORDER BY c.age", false)

            // distinct queries with aggregates
            .put("SELECT %s VALUE MAX(c.age) FROM c", false)

            // distinct queries with joins
            .put("SELECT %s VALUE c.age FROM p JOIN c IN p.children", true)
            .put("SELECT %s p.age AS ParentAge, c.age ChildAge FROM p JOIN c IN p.children", false)
            .put("SELECT %s VALUE c.name FROM p JOIN c IN p.children", false)
            .put("SELECT %s p.name AS ParentName, c.name ChildName FROM p JOIN c IN p.children", false)

            // distinct queries in subqueries
            .put("SELECT %s r.age, s FROM r JOIN (SELECT DISTINCT VALUE c FROM (SELECT 1 a) c) s WHERE r.age > 25", false)
            .put("SELECT %s p.name, p.age FROM (SELECT DISTINCT * FROM r) p WHERE p.age > 25", false)

            // distinct queries in scalar subqeries
            .put("SELECT %s p.name, (SELECT DISTINCT VALUE p.age) AS Age FROM p", true)
            .put("SELECT %s p.name, p.age FROM p WHERE (SELECT DISTINCT VALUE LEFT(p.name, 1)) > 'A' AND (SELECT " +
                "DISTINCT VALUE p.age) > 21", false)
            .put("SELECT %s p.name, (SELECT DISTINCT VALUE p.age) AS Age FROM p WHERE (SELECT DISTINCT VALUE p.name) >" +
                " 'A' OR (SELECT DISTINCT VALUE p.age) > 21", false)

            //   select *
            .put("SELECT %s * FROM c", true)
            .build();
        for (Map.Entry<String, Boolean> entry :  queries.entrySet()) {
            logger.info("Current distinct query: " + entry.getKey());
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setMaxDegreeOfParallelism(2);

            List<JsonNode> documentsFromWithDistinct = new ArrayList<>();
            List<JsonNode> documentsFromWithoutDistinct = new ArrayList<>();

            final String queryWithDistinct = String.format(entry.getKey(), "DISTINCT");
            final String queryWithoutDistinct = String.format(entry.getKey(), "");

            CosmosPagedFlux<JsonNode> queryObservable = createdCollection.queryItems(queryWithoutDistinct,
                                                                                     options,
                                                                                     JsonNode.class);

            Iterator<FeedResponse<JsonNode>> iterator = queryObservable.byPage().toIterable().iterator();
            Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
            UnorderedDistinctMap distinctMap = new UnorderedDistinctMap();

            while (iterator.hasNext()) {
                FeedResponse<JsonNode> next = iterator.next();
                for (JsonNode document : next.getResults()) {
                    if (distinctMap.add(document, outHash)) {
                        documentsFromWithoutDistinct.add(document);
                    }
                }
            }
            CosmosPagedFlux<JsonNode> queryObservableWithDistinct = createdCollection
                                                                        .queryItems(queryWithDistinct, options,
                                                                                    JsonNode.class);

            iterator = queryObservableWithDistinct.byPage(5).toIterable().iterator();

            while (iterator.hasNext()) {
                FeedResponse<JsonNode> next = iterator.next();
                documentsFromWithDistinct.addAll(next.getResults());
            }

            // We want to do Dcount for some queries
            if (entry.getValue()) {
                // Do a dcount query and validate results
                String queryWithDcount = "Select value count(1) from ("
                                             + String.format(entry.getKey(), "DISTINCT")
                                             + ")";
                List<Integer> docsWithDCount = new ArrayList<>();

                CosmosPagedFlux<Integer> dcountQueryObs = createdCollection.queryItems(queryWithDcount,
                                                                                       options,
                                                                                       Integer.class);

                for (FeedResponse<Integer> next : dcountQueryObs.byPage().toIterable()) {
                    docsWithDCount.addAll(next.getResults());
                }
                assertThat(docsWithDCount.size()).isEqualTo(1);
                int dCount = docsWithDCount.get(0);
                assertThat(dCount).isEqualTo(documentsFromWithDistinct.size());
            }

            assertThat(documentsFromWithDistinct.size()).isGreaterThanOrEqualTo(1);
            assertThat(documentsFromWithDistinct.size()).isEqualTo(documentsFromWithoutDistinct.size());
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsForDistinctIntValues(Boolean qmEnabled) {
        String query = "SELECT DISTINCT c.intprop from c";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options,
                                                                                             InternalObjectNode.class);

        Iterator<FeedResponse<InternalObjectNode>> iterator = queryObservable.byPage(5).collectList().single().block()
                                                                             .iterator();
        List<InternalObjectNode> itemPropertiesList = new ArrayList<>();
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> next = iterator.next();
            itemPropertiesList.addAll(next.getResults());
        }

        assertThat(itemPropertiesList.size()).isEqualTo(2);
        List<Object> intpropList = itemPropertiesList
                                       .stream()
                                       .map(internalObjectNode ->
                                                ModelBridgeInternal.getObjectFromJsonSerializable(
                                                    internalObjectNode, "intprop"))
                                   .collect(Collectors.toList());
        // We insert two documents witn intprop as 5.0 and 5. Distinct should consider them as one
        assertThat(intpropList).containsExactlyInAnyOrder(null, 5);

    }

    public void bulkInsert() {
        generateTestData();
        voidBulkInsertBlocking(createdCollection, docs);
        voidBulkInsertBlocking(createdCollection, propertiesDocs);
    }

    public void generateTestData() {

        Random rand = new Random();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < 40; i++) {
            Person person = getRandomPerson(rand, true);
            docs.add(person);
        }
        String resourceJson = String.format("{ " + "\"id\": \"%s\", \"intprop\": %d }", UUID.randomUUID().toString(),
                                            5);
        String resourceJson2 = String.format("{ " + "\"id\": \"%s\", \"intprop\": %f }", UUID.randomUUID().toString(),
                                             5.0f);

        propertiesDocs.add(new InternalObjectNode(resourceJson));
        propertiesDocs.add(new InternalObjectNode(resourceJson2));
    }

    private Pet getRandomPet(Random rand) {
        String name = getRandomName(rand);
        int age = getRandomAge(rand);
        return new Pet(name, age);
    }

    public Person getRandomPerson(Random rand, boolean addChildren) {
        String name = getRandomName(rand);
        City city = getRandomCity(rand);
        double income = getRandomIncome(rand);
        List<Person> people = new ArrayList<Person>();
        if (rand.nextInt(2) == 0 && addChildren) {
            //  Starting from -1, add at least one children
            for (int i = -1; i < rand.nextInt(5); i++) {
                people.add(getRandomPerson(rand, false));
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
