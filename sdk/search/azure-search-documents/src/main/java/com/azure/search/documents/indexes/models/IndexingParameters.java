// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents parameters for indexer execution.
 */
@Fluent
public final class IndexingParameters {
    /*
     * The number of items that are read from the data source and indexed as a
     * single batch in order to improve performance. The default depends on the
     * data source type.
     */
    @JsonProperty(value = "batchSize")
    private Integer batchSize;

    /*
     * The maximum number of items that can fail indexing for indexer execution
     * to still be considered successful. -1 means no limit. Default is 0.
     */
    @JsonProperty(value = "maxFailedItems")
    private Integer maxFailedItems;

    /*
     * The maximum number of items in a single batch that can fail indexing for
     * the batch to still be considered successful. -1 means no limit. Default
     * is 0.
     */
    @JsonProperty(value = "maxFailedItemsPerBatch")
    private Integer maxFailedItemsPerBatch;

    /*
     * A dictionary of indexer-specific configuration properties. Each name is
     * the name of a specific property. Each value must be of a primitive type.
     */
    @JsonProperty(value = "configuration")
    private Map<String, Object> configuration;

    /**
     * Get the batchSize property: The number of items that are read from the data source and indexed as a single batch
     * in order to improve performance. The default depends on the data source type.
     *
     * @return the batchSize value.
     */
    public Integer getBatchSize() {
        return this.batchSize;
    }

    /**
     * Set the batchSize property: The number of items that are read from the data source and indexed as a single batch
     * in order to improve performance. The default depends on the data source type.
     *
     * @param batchSize the batchSize value to set.
     * @return the IndexingParameters object itself.
     */
    public IndexingParameters setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Get the maxFailedItems property: The maximum number of items that can fail indexing for indexer execution to
     * still be considered successful. -1 means no limit. Default is 0.
     *
     * @return the maxFailedItems value.
     */
    public Integer getMaxFailedItems() {
        return this.maxFailedItems;
    }

    /**
     * Set the maxFailedItems property: The maximum number of items that can fail indexing for indexer execution to
     * still be considered successful. -1 means no limit. Default is 0.
     *
     * @param maxFailedItems the maxFailedItems value to set.
     * @return the IndexingParameters object itself.
     */
    public IndexingParameters setMaxFailedItems(Integer maxFailedItems) {
        this.maxFailedItems = maxFailedItems;
        return this;
    }

    /**
     * Get the maxFailedItemsPerBatch property: The maximum number of items in a single batch that can fail indexing for
     * the batch to still be considered successful. -1 means no limit. Default is 0.
     *
     * @return the maxFailedItemsPerBatch value.
     */
    public Integer getMaxFailedItemsPerBatch() {
        return this.maxFailedItemsPerBatch;
    }

    /**
     * Set the maxFailedItemsPerBatch property: The maximum number of items in a single batch that can fail indexing for
     * the batch to still be considered successful. -1 means no limit. Default is 0.
     *
     * @param maxFailedItemsPerBatch the maxFailedItemsPerBatch value to set.
     * @return the IndexingParameters object itself.
     */
    public IndexingParameters setMaxFailedItemsPerBatch(Integer maxFailedItemsPerBatch) {
        this.maxFailedItemsPerBatch = maxFailedItemsPerBatch;
        return this;
    }

    /**
     * Get the configuration property: A dictionary of indexer-specific configuration properties. Each name is the name
     * of a specific property. Each value must be of a primitive type.
     *
     * @return the configuration value.
     */
    public Map<String, Object> getConfiguration() {
        return this.configuration;
    }

    /**
     * Set the configuration property: A dictionary of indexer-specific configuration properties. Each name is the name
     * of a specific property. Each value must be of a primitive type.
     *
     * @param configuration the configuration value to set.
     * @return the IndexingParameters object itself.
     */
    public IndexingParameters setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Get the configuration property: A dictionary of indexer-specific configuration properties. Each name is the name
     * of a specific property. Each value must be of a primitive type.
     *
     * @return the configuration value.
     */
    public IndexingParametersConfiguration getIndexingParametersConfiguration() {
        if (CoreUtils.isNullOrEmpty(this.configuration)) {
            return null;
        }

        IndexingParametersConfiguration configuration = new IndexingParametersConfiguration();

        for (Map.Entry<String, Object> kvp : this.configuration.entrySet()) {
            String key = kvp.getKey();
            if (key == null) {
                continue;
            }

            String value = (kvp.getValue() == null) ? null : String.valueOf(kvp.getValue());
            switch (key) {
                case "parsingMode":
                    configuration.setParsingMode(BlobIndexerParsingMode.fromString(value));
                    break;

                case "excludedFileNameExtensions":
                    configuration.setExcludedFileNameExtensions(value);
                    break;

                case "indexedFileNameExtensions":
                    configuration.setIndexedFileNameExtensions(value);
                    break;

                case "failOnUnsupportedContentType":
                    configuration.setFailOnUnsupportedContentType(Boolean.valueOf(value));
                    break;

                case "failOnUnprocessableDocument":
                    configuration.setFailOnUnprocessableDocument(Boolean.valueOf(value));
                    break;

                case "indexStorageMetadataOnlyForOversizedDocuments":
                    configuration.setIndexStorageMetadataOnlyForOversizedDocuments(Boolean.valueOf(value));
                    break;

                case "delimitedTextHeaders":
                    configuration.setDelimitedTextHeaders(value);
                    break;

                case "delimitedTextDelimiter":
                    configuration.setDelimitedTextDelimiter(value);
                    break;

                case "firstLineContainsHeaders":
                    configuration.setFirstLineContainsHeaders(Boolean.valueOf(value));
                    break;

                case "documentRoot":
                    configuration.setDocumentRoot(value);
                    break;

                case "dataToExtract":
                    configuration.setDataToExtract(BlobIndexerDataToExtract.fromString(value));
                    break;

                case "imageAction":
                    configuration.setImageAction(BlobIndexerImageAction.fromString(value));
                    break;

                case "allowSkillsetToReadFileData":
                    configuration.setAllowSkillsetToReadFileData(Boolean.valueOf(value));
                    break;

                case "pdfTextRotationAlgorithm":
                    configuration.setPdfTextRotationAlgorithm(BlobIndexerPdfTextRotationAlgorithm.fromString(value));
                    break;

                case "executionEnvironment":
                    configuration.setExecutionEnvironment(IndexerExecutionEnvironment.fromString(value));
                    break;

                case "queryTimeout":
                    configuration.setQueryTimeout(value);
                    break;

                default:
                    configuration.setAdditionalProperties(key, value);
                    break;
            }
        }

        return configuration;
    }

    /**
     * Set the configuration property: A dictionary of indexer-specific configuration properties. Each name is the name
     * of a specific property. Each value must be of a primitive type.
     *
     * @param configuration the configuration value to set.
     * @return the IndexingParameters object itself.
     */
    public IndexingParameters setIndexingParametersConfiguration(IndexingParametersConfiguration configuration) {
        setConfigurationValue(configuration.getParsingMode(), "parsingMode");
        setConfigurationValue(configuration.getExcludedFileNameExtensions(), "excludedFileNameExtensions");
        setConfigurationValue(configuration.getIndexedFileNameExtensions(), "indexedFileNameExtensions");
        setConfigurationValue(configuration.isFailOnUnsupportedContentType(), "failOnUnsupportedContentType");
        setConfigurationValue(configuration.isFailOnUnprocessableDocument(), "failOnUnprocessableDocument");
        setConfigurationValue(configuration.isIndexStorageMetadataOnlyForOversizedDocuments(),
            "indexStorageMetadataOnlyForOversizedDocuments");
        setConfigurationValue(configuration.getDelimitedTextHeaders(), "delimitedTextHeaders");
        setConfigurationValue(configuration.getDelimitedTextDelimiter(), "delimitedTextDelimiter");
        setConfigurationValue(configuration.isFirstLineContainsHeaders(), "firstLineContainsHeaders");
        setConfigurationValue(configuration.getDocumentRoot(), "documentRoot");
        setConfigurationValue(configuration.getDataToExtract(), "dataToExtract");
        setConfigurationValue(configuration.getImageAction(), "imageAction");
        setConfigurationValue(configuration.isAllowSkillsetToReadFileData(), "allowSkillsetToReadFileData");
        setConfigurationValue(configuration.getPdfTextRotationAlgorithm(), "pdfTextRotationAlgorithm");
        setConfigurationValue(configuration.getExecutionEnvironment(), "executionEnvironment");
        setConfigurationValue(configuration.getQueryTimeout(), "queryTimeout");

        Map<String, Object> additionalProperties = configuration.getAdditionalProperties();
        if (!CoreUtils.isNullOrEmpty(additionalProperties)) {
            this.configuration.putAll(additionalProperties);
        }

        return this;
    }

    private void setConfigurationValue(Object value, String key) {
        if (value == null) {
            return;
        }

        if (configuration == null) {
            configuration = new HashMap<>();
        }

        configuration.put(key, String.valueOf(value));
    }
}
