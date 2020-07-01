// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.performance;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.sync.CosmosSyncClient;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CosmosPageRequest;
import com.microsoft.azure.spring.data.cosmosdb.performance.domain.PerfPerson;
import com.microsoft.azure.spring.data.cosmosdb.performance.repository.PerfPersonRepository;
import com.microsoft.azure.spring.data.cosmosdb.performance.service.SdkService;
import com.microsoft.azure.spring.data.cosmosdb.performance.utils.Constants;
import com.microsoft.azure.spring.data.cosmosdb.performance.utils.DatabaseUtils;
import com.microsoft.azure.spring.data.cosmosdb.performance.utils.PerfDataProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.azure.spring.data.cosmosdb.performance.utils.FunctionUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PerfConfiguration.class)
public class PerformanceCompare {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceCompare.class);

    @Value("${perf.recursive.times:20}")
    private int recurTimes;

    @Value("${perf.batch.size:5}")
    private int batchSize;

    @Value("${perf.acceptance.percentage:15}")
    private int acceptanceDiffPercentage;

    private float acceptanceDiff;

    @Autowired
    private CosmosSyncClient cosmosSyncClient;

    @Autowired
    private CosmosClient asyncClient;

    @Autowired
    private PerfPersonRepository repository;

    private static boolean hasInit = false;
    private static SdkService sdkService;
    private static PerformanceReport report = new PerformanceReport();

    @Before
    public void setUp() throws CosmosClientException {
        if (!hasInit) {
            DatabaseUtils.createDatabase(cosmosSyncClient, Constants.PERF_DATABASE_NAME);
            DatabaseUtils.createContainer(cosmosSyncClient, Constants.PERF_DATABASE_NAME,
                    Constants.SPRING_COLLECTION_NAME);
            DatabaseUtils.createContainer(cosmosSyncClient,
                Constants.PERF_DATABASE_NAME, Constants.SDK_COLLECTION_NAME);

            sdkService = new SdkService(cosmosSyncClient, Constants.PERF_DATABASE_NAME,
                    Constants.SDK_COLLECTION_NAME, asyncClient);
            hasInit = true;
        }

        acceptanceDiff = (float) acceptanceDiffPercentage / 100;
        LOGGER.info("Running performance test for {} time(s), batch size {} and acceptance diff {}.",
                recurTimes, batchSize, acceptanceDiff);
    }

    @After
    public void clear() {
        repository.deleteAll();
        sdkService.deleteAll();
    }

    @AfterClass
    public static void printReport() {
        report.getPerfItems().forEach(System.out::println);
    }

    @Test
    public void saveOneRecordTest() {
        final List<PerfPerson> personList = PerfDataProvider.getPerfData(recurTimes);

        final long springCost = applyInputListFunc(personList, repository::save);
        final long sdkCost = applyInputListFunc(personList, sdkService::save);

        verifyResult(OperationType.SAVE_ONE, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void saveMultipleRecordsTest() {
        final List<Iterable<PerfPerson>> personList = PerfDataProvider.getMultiPerfData(batchSize, recurTimes);

        final long springCost = acceptInputListFunc(personList, repository::saveAll);
        final long sdkCost = acceptInputListFunc(personList, sdkService::saveAll);

        verifyResult(OperationType.SAVE_ALL, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void deleteOneRecordTest() {
        final List<PerfPerson> personList = prepareListData(recurTimes);

        final long springCost = acceptInputListFunc(personList, repository::delete);
        final long sdkCost = acceptInputListFunc(personList, sdkService::delete);

        verifyResult(OperationType.DELETE_ONE, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void deleteAllRecordsTest() {
        final List<Iterable<PerfPerson>> personList = prepareListBatchData(recurTimes, batchSize);

        final long springCost = acceptInputListFunc(personList, repository::deleteAll);
        final long sdkCost = acceptInputListFunc(personList, sdkService::deleteAll);

        verifyResult(OperationType.DELETE_ALL, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findByIdTest() {
        final List<String> idList = prepareListData(recurTimes).stream().map(PerfPerson::getId)
                .collect(Collectors.toList());

        final long springCost = applyInputListFunc(idList, repository::findById);
        final long sdkCost = applyInputListFunc(idList, sdkService::findById);

        verifyResult(OperationType.FIND_BY_ID, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findByMultipleIdsTest() {
        final List<Iterable<String>> idList = listBatchIds(recurTimes, batchSize);

        final long springCost = acceptInputListFunc(idList, repository::findAllById);
        final long sdkCost = acceptInputListFunc(idList, sdkService::findAllById);

        verifyResult(OperationType.FIND_BY_IDS, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findAllTest() {
        prepareListData(recurTimes);

        final long springCost = getSupplier(recurTimes, repository::findAll);
        final long sdkCost = getSupplier(recurTimes, sdkService::findAll);

        verifyResult(OperationType.FIND_ALL, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void deleteAllTest() {
        final long springCost = getSupplier(recurTimes, this::springDeleteAll);
        final long sdkCost = getSupplier(recurTimes, sdkService::deleteAll);

        verifyResult(OperationType.DELETE_ALL, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findBySortingTest() {
        prepareListData(recurTimes);

        final Sort sort = Sort.by(Sort.Direction.ASC, "name");
        final List<Sort> sortList = buildSortList(sort, recurTimes);

        final long springCost = applyInputListFunc(sortList, repository::findAll);
        final long sdkCost = applyInputListFunc(sortList, sdkService::searchDocuments);

        verifyResult(OperationType.FIND_BY_SORT, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findByPagingTest() {
        prepareListData(recurTimes);
        int pageSize = recurTimes / 2;
        pageSize = pageSize >= 1 ? pageSize : 1;

        final long springCost = runConsumerForTimes(recurTimes, pageSize, this::queryTwoPages);
        final long sdkCost = runConsumerForTimes(recurTimes, pageSize, sdkService::queryTwoPages);

        verifyResult(OperationType.FIND_BY_PAGING, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void findByFieldTest() {
        final List<PerfPerson> data = prepareListData(recurTimes);

        final String name = data.get(recurTimes / 2).getName();

        final long springCost = runFunctionForTimes(recurTimes, name, repository::findByName);
        final long sdkCost = runConsumerForTimes(recurTimes, name, sdkService::findByName);

        verifyResult(OperationType.FIND_BY_FIELD, springCost, sdkCost, acceptanceDiff);
    }

    @Test
    public void countTest() {
        prepareListData(recurTimes);

        final long springCost = getSupplier(recurTimes, repository::count);
        final long sdkCost = getSupplier(recurTimes, sdkService::count);

        verifyResult(OperationType.COUNT, springCost, sdkCost, acceptanceDiff);
    }

    /**
     * Check whether two time cost fall into the acceptable range.
     *
     * @param timeCostSpring
     * @param timeCostSdk
     * @param acceptanceDiff The acceptable diff between two time cost.
     */
    private void assertPerf(long timeCostSpring, long timeCostSdk, float acceptanceDiff) {
        final long diff = timeCostSpring - timeCostSdk;
        final float actualDiff = (float) diff / timeCostSdk;

        assertThat(actualDiff).isLessThan(acceptanceDiff);
    }

    private void verifyResult(OperationType type, long timeCostSpring, long timeCostSdk, float acceptanceDiff) {
        report.addItem(new PerfItem(type, timeCostSpring, timeCostSdk, recurTimes));
        assertPerf(timeCostSpring, timeCostSdk, acceptanceDiff);
    }

    private boolean springDeleteAll() {
        repository.deleteAll();
        return true;  // To provide return value for Supplier
    }

    private List<Sort> buildSortList(Sort sort, int times) {
        final List<Sort> sorts = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            sorts.add(sort);
        }

        return sorts;
    }

    private void queryTwoPages(int pageSize) {
        final Pageable pageable = new CosmosPageRequest(0, pageSize, null);
        final Page<PerfPerson> page = this.repository.findAll(pageable);
        this.repository.findAll(page.getPageable());
    }

    private List<PerfPerson> prepareListData(int count) {
        final List<PerfPerson> personList = PerfDataProvider.getPerfData(count);

        applyInputListFunc(personList, repository::save);
        applyInputListFunc(personList, sdkService::save);

        return personList;
    }


    private List<Iterable<PerfPerson>> prepareListBatchData(int times, int batchSize) {
        final List<Iterable<PerfPerson>> personList = PerfDataProvider.getMultiPerfData(batchSize, times);

        applyInputListFunc(personList, repository::saveAll);
        applyInputListFunc(personList, sdkService::saveAll);

        return personList;
    }

    private List<Iterable<String>> listBatchIds(int times, int batchSize) {
        return prepareListBatchData(times, batchSize).stream()
                .map(iterable -> {
                    final List<String> batchIds = new ArrayList<>();
                    iterable.forEach(person -> batchIds.add(person.getId()));
                    return batchIds;
                }).collect(Collectors.toList());
    }
}
