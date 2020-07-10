// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.service;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.sync.CosmosSyncClient;
import com.azure.spring.data.cosmos.performance.utils.DatabaseUtils;
import com.google.gson.Gson;
import com.azure.spring.data.cosmos.performance.domain.PerfPerson;
import org.assertj.core.util.Lists;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SdkService {
    private static Gson gson = new Gson();

    private final CosmosSyncClient cosmosSyncClient;
    private final String dbName;
    private final String containerName;

    public SdkService(CosmosSyncClient client, String dbName, String containerName, CosmosClient asyncClient) {
        this.cosmosSyncClient = client;
        this.dbName = dbName;
        this.containerName = containerName;
    }

    public PerfPerson save(PerfPerson person) {
        try {
            final String personJson = gson.toJson(person);
            final CosmosItemProperties personDoc = new CosmosItemProperties(personJson);

            final CosmosItemProperties doc = cosmosSyncClient.getDatabase(dbName)
                                                     .getContainer(containerName)
                                                     .createItem(personDoc)
                                                     .properties();

            return gson.fromJson(doc.toJson(), PerfPerson.class);
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
            final String docLink = DatabaseUtils.getDocumentLink(dbName, containerName, person.getId());
            cosmosSyncClient.getDatabase(dbName)
                    .getContainer(containerName)
                    .getItem(person.getId(), PartitionKey.None)
                    .delete(new CosmosItemRequestOptions());

        } catch (CosmosClientException e) {
            throw new IllegalStateException(e); // Runtime exception to fail directly
        }
    }

    public void deleteAll(Iterable<PerfPerson> personIterable) {
        personIterable.forEach(person -> delete(person));
    }

    public CosmosItemProperties findById(String id) {
        final Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator =
                cosmosSyncClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems("SELECT * FROM "
                                + containerName
                                + " WHERE "
                                + containerName
                                + ".id='"
                                + id
                                + "'", new FeedOptions());
        CosmosItemProperties itemProperties = null;
        if (feedResponseIterator.hasNext()) {
            final List<CosmosItemProperties> results = feedResponseIterator.next().results();
            if (!results.isEmpty()) {
                itemProperties = results.get(0);
            }
        }

        return itemProperties;
    }

    public List<PerfPerson> findAllById(Iterable<String> ids) {
        final String idsInList = String.join(",",
                Arrays.asList(ids).stream().map(id -> "'" + id + "'").collect(Collectors.toList()));
        final String sql = "SELECT * FROM " + containerName + " WHERE " + containerName + ".id IN ("
                + idsInList + ")";

        final FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);

        final List<CosmosItemProperties> docs = new ArrayList<>();

        final Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator = cosmosSyncClient.getDatabase(dbName)
                                                                                    .getContainer(containerName)
                                                                                    .queryItems(sql, feedOptions);
        while (feedResponseIterator.hasNext()) {
            final FeedResponse<CosmosItemProperties> next = feedResponseIterator.next();
            docs.addAll(next.results());
        }


        return fromDocuments(docs);
    }

    public List<PerfPerson> findAll() {
        final String sql = "SELECT * FROM  " + containerName;
        final List<CosmosItemProperties> docs = getCosmosItemPropertiesList(sql);
        return fromDocuments(docs);
    }

    public boolean deleteAll() {
        final String sql = "SELECT * FROM  " + containerName;
        final List<CosmosItemProperties> documents = getCosmosItemPropertiesList(sql);

        documents.forEach(document -> {
            try {
                cosmosSyncClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .getItem(document.id(), PartitionKey.None)
                        .delete(new CosmosItemRequestOptions().partitionKey(PartitionKey.None));
            } catch (CosmosClientException e) {
                throw new IllegalStateException(e);
            }
        });

        return true;
    }

    private List<CosmosItemProperties> getCosmosItemPropertiesList(String sql) {
        final List<CosmosItemProperties> documents = new ArrayList<>();
        final Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator =
                cosmosSyncClient.getDatabase(dbName)
                        .getContainer(containerName)
                        .queryItems(sql, new FeedOptions().enableCrossPartitionQuery(true));
        while (feedResponseIterator.hasNext()) {
            final FeedResponse<CosmosItemProperties> next = feedResponseIterator.next();
            documents.addAll(next.results());
        }
        return documents;
    }

    public List<PerfPerson> searchDocuments(Sort sort) {
        final Sort.Order order = sort.iterator().next(); // Only one Order supported
        final String sql = "SELECT * FROM  " + containerName + " ORDER BY " + containerName + "."
                                   + order.getProperty() + " " + order.getDirection().name();
        final List<CosmosItemProperties> docs = getCosmosItemPropertiesList(sql);

        return fromDocuments(docs);
    }

    public long count() {
        final String sql = "SELECT VALUE COUNT(1) FROM " + containerName;
        final Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator = cosmosSyncClient.getDatabase(dbName)
                                                                                    .getContainer(containerName)
                                                                                    .queryItems(sql, new FeedOptions());
        final Object result =   feedResponseIterator.next().results().get(0).get("_aggregate");

        return result instanceof Integer ? Long.valueOf((Integer) result) : (Long) result;
    }

    public List<PerfPerson> findByName(String name) {
        final String sql = "SELECT * FROM " + containerName + " WHERE " + containerName + ".name='"
                           + name + "'";
        final Iterator<CosmosItemProperties> result = getCosmosItemPropertiesList(sql).iterator();
        return fromDocuments(Lists.newArrayList(result));
    }

    public void queryTwoPages(int pageSize) {
        final FeedOptions options = new FeedOptions();
        options.maxItemCount(pageSize);
        options.requestContinuation(null);

        searchBySize(pageSize, options);
        searchBySize(pageSize, options);
    }

    private List<PerfPerson> searchBySize(int size, FeedOptions options) {
        final String sql = "SELECT * FROM " + containerName;

        final Iterator<CosmosItemProperties> it = getCosmosItemPropertiesList(sql).iterator();
        final List<PerfPerson> entities = new ArrayList<>();
        int i = 0;
        while (it.hasNext()
                && i++ < size) {
            // This convert here is in order to mock data conversion in real use case, in order to compare with
            // Spring Data mapping
            final CosmosItemProperties d = it.next();
            final PerfPerson entity = gson.fromJson(d.toJson(), PerfPerson.class);
            entities.add(entity);
        }

        count(); // Mock same behavior with Spring pageable query, requires total elements count

        return entities;
    }

    private List<PerfPerson> fromDocuments(List<CosmosItemProperties> documents) {
        return documents.stream().map(d -> gson.fromJson(d.toJson(), PerfPerson.class))
                .collect(Collectors.toList());
    }
}
