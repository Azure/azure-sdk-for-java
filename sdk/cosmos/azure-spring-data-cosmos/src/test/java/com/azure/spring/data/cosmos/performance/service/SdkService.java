// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.service;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.performance.domain.PerfPerson;
import com.azure.spring.data.cosmos.performance.utils.DatabaseUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.util.Lists;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SdkService {

    private final CosmosClient cosmosClient;
    private final String dbName;
    private final String containerName;

    public SdkService(CosmosClient client, String dbName, String containerName,
                      CosmosAsyncClient cosmosAsyncClient) {
        this.cosmosClient = client;
        this.dbName = dbName;
        this.containerName = containerName;
    }

    public PerfPerson save(PerfPerson person) {
        try {

            return cosmosClient.getDatabase(dbName)
                               .getContainer(containerName)
                               .createItem(person)
                               .getItem();
        } catch (Exception e) {
            throw new IllegalStateException(e); // Runtime exception to fail directly
        }
    }

    public List<PerfPerson> saveAll(Iterable<PerfPerson> personIterable) {
        final List<PerfPerson> result = new ArrayList<>();
        personIterable.forEach(person -> result.add(save(person)));

        return result;
    }

    public void delete(PerfPerson person) {
        try {
            final String docLink = DatabaseUtils.getDocumentLink(dbName, containerName,
                person.getId());
            cosmosClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .deleteItem(person.getId(), PartitionKey.NONE,
                            new CosmosItemRequestOptions());

        } catch (CosmosException e) {
            throw new IllegalStateException(e); // Runtime exception to fail directly
        }
    }

    public void deleteAll(Iterable<PerfPerson> personIterable) {
        personIterable.forEach(this::delete);
    }

    public PerfPerson findById(String id) {
        final Iterator<FeedResponse<PerfPerson>> feedResponseIterator =
            cosmosClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems("SELECT * FROM "
                            + containerName
                            + " WHERE "
                            + containerName
                            + ".id='"
                            + id
                            + "'", new CosmosQueryRequestOptions(), PerfPerson.class)
                        .iterableByPage()
                        .iterator();
        PerfPerson perfPerson = null;
        if (feedResponseIterator.hasNext()) {
            final List<PerfPerson> results = feedResponseIterator.next().getResults();
            if (!results.isEmpty()) {
                perfPerson = results.get(0);
            }
        }

        return perfPerson;
    }

    public List<PerfPerson> findAllById(Iterable<String> ids) {
        final String idsInList = String.join(",",
            Arrays.asList(ids).stream().map(id -> "'" + id + "'").collect(Collectors.toList()));
        final String sql = "SELECT * FROM " + containerName + " WHERE " + containerName + ".id IN ("
            + idsInList + ")";

        final List<PerfPerson> docs = new ArrayList<>();

        final Iterator<FeedResponse<PerfPerson>> feedResponseIterator =
            cosmosClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems(sql, new CosmosQueryRequestOptions(), PerfPerson.class)
                        .iterableByPage()
                        .iterator();
        while (feedResponseIterator.hasNext()) {
            final FeedResponse<PerfPerson> next = feedResponseIterator.next();
            docs.addAll(next.getResults());
        }


        return docs;
    }

    public List<PerfPerson> findAll() {
        final String sql = "SELECT * FROM  " + containerName;
        return getPerfPersonList(sql);
    }

    public boolean deleteAll() {
        final String sql = "SELECT * FROM  " + containerName;
        final List<PerfPerson> documents = getPerfPersonList(sql);

        documents.forEach(document -> {
            try {
                cosmosClient.getDatabase(dbName)
                            .getContainer(containerName)
                            .deleteItem(document.getId(), PartitionKey.NONE,
                                new CosmosItemRequestOptions());
            } catch (CosmosException e) {
                throw new IllegalStateException(e);
            }
        });

        return true;
    }

    private List<PerfPerson> getPerfPersonList(String sql, int size) {
        final List<PerfPerson> documents = new ArrayList<>();
        final Iterator<FeedResponse<PerfPerson>> feedResponseIterator =
            cosmosClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems(sql, new CosmosQueryRequestOptions(), PerfPerson.class)
                        .iterableByPage(size)
                        .iterator();
        while (feedResponseIterator.hasNext()) {
            final FeedResponse<PerfPerson> next = feedResponseIterator.next();
            documents.addAll(next.getResults());
        }
        return documents;
    }

    private List<PerfPerson> getPerfPersonList(String sql) {
        //  Pass default page size, which is 100
        return getPerfPersonList(sql, 100);
    }

    public List<PerfPerson> searchDocuments(Sort sort) {
        final Sort.Order order = sort.iterator().next(); // Only one Order supported
        final String sql = "SELECT * FROM  " + containerName + " ORDER BY " + containerName + "."
            + order.getProperty() + " " + order.getDirection().name();
        return getPerfPersonList(sql);
    }

    public long count() {
        final String sql = "SELECT VALUE COUNT(1) FROM " + containerName;
        final Iterator<FeedResponse<JsonNode>> feedResponseIterator =
            cosmosClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems(sql, new CosmosQueryRequestOptions(), JsonNode.class)
                        .iterableByPage()
                        .iterator();

        return feedResponseIterator.next().getResults().get(0).get("_aggregate").asLong();
    }

    public List<PerfPerson> findByName(String name) {
        final String sql = "SELECT * FROM " + containerName + " WHERE " + containerName + ".name='"
            + name + "'";
        final Iterator<PerfPerson> result = getPerfPersonList(sql).iterator();
        return Lists.newArrayList(result);
    }

    public void queryTwoPages(int pageSize) {
        searchBySize(pageSize);
        searchBySize(pageSize);
    }

    private List<PerfPerson> searchBySize(int size) {
        final String sql = "SELECT * FROM " + containerName;

        final Iterator<PerfPerson> it = getPerfPersonList(sql, size).iterator();
        final List<PerfPerson> entities = new ArrayList<>();
        int i = 0;
        while (it.hasNext() && i++ < size) {
            final PerfPerson perfPerson = it.next();
            entities.add(perfPerson);
        }

        count(); // Mock same behavior with Spring pageable query, requires total elements count

        return entities;
    }
}
