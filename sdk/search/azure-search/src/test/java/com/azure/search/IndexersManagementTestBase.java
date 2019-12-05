// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.search.models.IndexingSchedule;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.InputFieldMappingEntry;
import com.azure.search.models.OutputFieldMappingEntry;
import com.azure.search.models.Skill;
import com.azure.search.models.IndexingParameters;
import com.azure.search.models.OcrSkill;
import com.azure.search.models.Skillset;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.FieldMapping;
import com.azure.search.models.IndexerExecutionResult;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerLimits;
import com.azure.search.models.IndexerExecutionStatus;
import com.azure.search.test.CustomQueryPipelinePolicy;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class IndexersManagementTestBase extends SearchServiceTestBase {
    static final String TARGET_INDEX_NAME = "indexforindexers";
    static final HttpPipelinePolicy MOCK_STATUS_PIPELINE_POLICY = new CustomQueryPipelinePolicy("mock_status",
        "inProgress");

    @Test
    public abstract void createIndexerReturnsCorrectDefinition();

    @Test
    public abstract void canCreateAndListIndexers();

    @Test
    public abstract void canCreateAndListIndexerNames();

    @Test
    public abstract void createIndexerFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void canResetIndexerAndGetIndexerStatus();

    @Test
    public abstract void canRunIndexer();

    @Test
    public abstract void canUpdateIndexer();

    @Test
    public abstract void canUpdateIndexerFieldMapping();

    @Test
    public abstract void canCreateIndexerWithFieldMapping();

    @Test
    public abstract void canUpdateIndexerDisabled();

    @Test
    public abstract void canUpdateIndexerSchedule();

    @Test
    public abstract void canCreateIndexerWithSchedule();

    @Test
    public abstract void canUpdateIndexerBatchSizeMaxFailedItems();

    @Test
    public abstract void canCreateIndexerWithBatchSizeMaxFailedItems();

    @Test
    public abstract void canUpdateIndexerBlobParams();

    @Test
    public abstract void canCreateIndexerWithBlobParams();

    @Test
    public abstract void canUpdateIndexerSkillset();

    @Test
    public abstract void canCreateIndexerWithSkillset();

    @Test
    public abstract void deleteIndexerIsIdempotent();

    @Test
    public abstract void canCreateAndDeleteIndexer();

    @Test
    public abstract void canCreateAndGetIndexer();

    @Test
    public abstract void getIndexerThrowsOnNotFound();

    @Test
    public abstract void createOrUpdateIndexerIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateIndexerIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void deleteIndexerIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteIndexerIfNotChangedWorksOnlyOnCurrentResource();

    @Test
    public abstract void updateIndexerIfExistsFailsOnNoResource();

    @Test
    public abstract void updateIndexerIfExistsSucceedsOnExistingResource();

    @Test
    public abstract void updateIndexerIfNotChangedFailsWhenResourceChanged();

    @Test
    public abstract void updateIndexerIfNotChangedSucceedsWhenResourceUnchanged();

    @Test
    public abstract void existsReturnsTrueForExistingIndexer();

    @Test
    public abstract void existsReturnsFalseForNonExistingIndexer();

    @Test
    public abstract  void canRunIndexerWithResponse();

    @Test
    public abstract void canRunIndexerAndGetIndexerStatus();

    /**
     * Create a new valid skillset object
     * @return the newly created skillset object
     */
    Skillset createSkillsetObject() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry()
                .setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(true)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext("/document")
                .setInputs(inputs)
                .setOutputs(outputs)
        );
        return new Skillset()
            .setName("ocr-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    void assertIndexersEqual(Indexer expected, Indexer actual) {
        expected.setETag("none");
        actual.setETag("none");

        // we ignore defaults as when properties are not set they are returned from the service with
        // default values
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    Indexer createBaseTestIndexerObject(String indexerName, String targetIndexName) {
        return new Indexer()
            .setName(indexerName)
            .setTargetIndexName(targetIndexName)
            .setSchedule(new IndexingSchedule().setInterval(Duration.ofDays(1)));
    }

    /**
     * This index contains fields that are declared on the live data source
     * we use to test the indexers
     *
     * @return the newly created Index object
     */
    Index createTestIndexForLiveDatasource(String indexName) {
        return new Index()
            .setName(indexName)
            .setFields(Arrays.asList(
                new Field()
                    .setName("county_name")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE),
                new Field()
                    .setName("state")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE),
                new Field()
                    .setName("feature_id")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)));
    }


    /**
     * Create a new indexer and change its description property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentDescription() {
        // create a new indexer object with a modified description
        return createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
            .setDescription("somethingdifferent");
    }

    /**
     * Create a new indexer and change its field mappings property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentFieldMapping() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        // Create field mappings
        List<FieldMapping> fieldMappings = Collections.singletonList(new FieldMapping()
            .setSourceFieldName("state_alpha")
            .setTargetFieldName("state"));

        // modify the indexer
        indexer.setFieldMappings(fieldMappings);

        return indexer;
    }

    /**
     * Create a new indexer and set the Disabled property to true
     *
     * @return the created indexer
     */
    Indexer createDisabledIndexer() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        // modify it
        indexer.setIsDisabled(false);

        return indexer;
    }

    /**
     * Create a new indexer and change its schedule property
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentSchedule() {
        // create a new indexer object
        Indexer indexer = createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        IndexingSchedule is = new IndexingSchedule()
            .setInterval(Duration.ofMinutes(10));

        // modify the indexer
        indexer.setSchedule(is);

        return indexer;
    }

    /**
     * Create a new indexer and change its skillset
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentSkillset(String skillsetName) {
        // create a new indexer object
        return createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME)
            .setSkillsetName(skillsetName);
    }

    /**
     * Create a new indexer and change its indexing parameters
     *
     * @return the created indexer
     */
    Indexer createIndexerWithDifferentIndexingParameters(Indexer indexer) {
        // create a new indexer object
        IndexingParameters ip = new IndexingParameters()
            .setMaxFailedItems(121)
            .setMaxFailedItemsPerBatch(11)
            .setBatchSize(20);

        // modify the indexer
        indexer.setParameters(ip);

        return indexer;
    }

    Indexer createIndexerWithStorageConfig() {
        // create an indexer object
        Indexer updatedExpected =
            createBaseTestIndexerObject("indexer", TARGET_INDEX_NAME);

        // just adding some(valid) config values for blobs
        HashMap<String, Object> config = new HashMap<>();
        config.put("indexedFileNameExtensions", ".pdf,.docx");
        config.put("excludedFileNameExtensions", ".xlsx");
        config.put("dataToExtract", "storageMetadata");
        config.put("failOnUnsupportedContentType", false);

        IndexingParameters ip = new IndexingParameters()
            .setConfiguration(config);

        // modify it
        updatedExpected.setParameters(ip);

        return updatedExpected;
    }

    void setSameStartTime(Indexer expected, Indexer actual) {
        // There ought to be a start time in the response; We just can't know what it is because it would
        // make the test timing-dependent.
        expected.getSchedule().setStartTime(actual.getSchedule().getStartTime());
    }

    void assertAllIndexerFieldsNullExceptName(Indexer indexer) {
        Assert.assertNull(indexer.getParameters());
        Assert.assertNull(indexer.getDataSourceName());
        Assert.assertNull(indexer.getDescription());
        Assert.assertNull(indexer.getETag());
        Assert.assertNull(indexer.getFieldMappings());
        Assert.assertNull(indexer.getOutputFieldMappings());
        Assert.assertNull(indexer.getSchedule());
        Assert.assertNull(indexer.getSkillsetName());
        Assert.assertNull(indexer.getTargetIndexName());
    }

    void assertStartAndEndTimeValid(IndexerExecutionResult result) {
        Assert.assertNotNull(result.getStartTime());
        Assert.assertNotEquals(OffsetDateTime.now(), result.getStartTime());
        Assert.assertNotNull(result.getEndTime());
        Assert.assertNotEquals(OffsetDateTime.now(), result.getEndTime());
    }

    void assertValidIndexerExecutionInfo(IndexerExecutionInfo indexerExecutionInfo) {
        Assert.assertEquals(IndexerExecutionStatus.IN_PROGRESS, indexerExecutionInfo.getLastResult().getStatus());
        Assert.assertEquals(3, indexerExecutionInfo.getExecutionHistory().size());

        IndexerLimits limits = indexerExecutionInfo.getLimits();
        Assert.assertNotNull(limits);
        Assert.assertEquals(100000, limits.getMaxDocumentContentCharactersToExtract(), 0);
        Assert.assertEquals(1000, limits.getMaxDocumentExtractionSize(), 0);

        IndexerExecutionResult newestResult = indexerExecutionInfo.getExecutionHistory().get(0);
        IndexerExecutionResult middleResult = indexerExecutionInfo.getExecutionHistory().get(1);
        IndexerExecutionResult oldestResult = indexerExecutionInfo.getExecutionHistory().get(2);

        Assert.assertEquals(IndexerExecutionStatus.TRANSIENT_FAILURE, newestResult.getStatus());
        Assert.assertEquals("The indexer could not connect to the data source",
            newestResult.getErrorMessage());
        assertStartAndEndTimeValid(newestResult);

        Assert.assertEquals(IndexerExecutionStatus.RESET, middleResult.getStatus());
        assertStartAndEndTimeValid(middleResult);

        Assert.assertEquals(IndexerExecutionStatus.SUCCESS, oldestResult.getStatus());
        Assert.assertEquals(124876, oldestResult.getItemCount());
        Assert.assertEquals(2, oldestResult.getFailedItemCount());
        Assert.assertEquals("100", oldestResult.getInitialTrackingState());
        Assert.assertEquals("200", oldestResult.getFinalTrackingState());
        assertStartAndEndTimeValid(oldestResult);

        Assert.assertEquals(2, oldestResult.getErrors().size());
        Assert.assertEquals("1", oldestResult.getErrors().get(0).getKey());
        Assert.assertEquals("Key field contains unsafe characters",
            oldestResult.getErrors().get(0).getErrorMessage());
        Assert.assertEquals("DocumentExtraction.AzureBlob.MyDataSource",
            oldestResult.getErrors().get(0).getName());
        Assert.assertEquals("The file could not be parsed.", oldestResult.getErrors().get(0).getDetails());
        Assert.assertEquals("https://go.microsoft.com/fwlink/?linkid=2049388",
            oldestResult.getErrors().get(0).getDocumentationLink());

        Assert.assertEquals("121713", oldestResult.getErrors().get(1).getKey());
        Assert.assertEquals("Item is too large", oldestResult.getErrors().get(1).getErrorMessage());
        Assert.assertEquals("DocumentExtraction.AzureBlob.DataReader",
            oldestResult.getErrors().get(1).getName());
        Assert.assertEquals("Blob size cannot exceed 256 MB.", oldestResult.getErrors().get(1).getDetails());
        Assert.assertEquals("https://go.microsoft.com/fwlink/?linkid=2049388",
            oldestResult.getErrors().get(1).getDocumentationLink());


        Assert.assertEquals(1, oldestResult.getWarnings().size());
        Assert.assertEquals("2", oldestResult.getWarnings().get(0).getKey());
        Assert.assertEquals("Document was truncated to 50000 characters.",
            oldestResult.getWarnings().get(0).getMessage());
        Assert.assertEquals("Enrichment.LanguageDetectionSkill.#4",
            oldestResult.getWarnings().get(0).getName());
        Assert.assertEquals("Try to split the input into smaller chunks using Split skill.",
            oldestResult.getWarnings().get(0).getDetails());
        Assert.assertEquals("https://go.microsoft.com/fwlink/?linkid=2099692",
            oldestResult.getWarnings().get(0).getDocumentationLink());
    }
}
