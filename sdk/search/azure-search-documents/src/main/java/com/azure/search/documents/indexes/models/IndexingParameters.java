// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

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

        Map<String, Object> additionalProperties = null;
        for (Map.Entry<String, Object> kvp : this.configuration.entrySet()) {
            String key = kvp.getKey();
            if (key == null) {
                continue;
            }

            Object value = kvp.getValue();
            switch (key) {
                case "parsingMode":
                    configuration.setParsingMode(converter(value, BlobIndexerParsingMode::fromString));
                    break;

                case "excludedFileNameExtensions":
                    configuration.setExcludedFileNameExtensions(converter(value, Function.identity()));
                    break;

                case "indexedFileNameExtensions":
                    configuration.setIndexedFileNameExtensions(converter(value, Function.identity()));
                    break;

                case "failOnUnsupportedContentType":
                    configuration.setFailOnUnsupportedContentType(converter(value, Boolean::parseBoolean));
                    break;

                case "failOnUnprocessableDocument":
                    configuration.setFailOnUnprocessableDocument(converter(value, Boolean::parseBoolean));
                    break;

                case "indexStorageMetadataOnlyForOversizedDocuments":
                    configuration.setIndexStorageMetadataOnlyForOversizedDocuments(
                        converter(value, Boolean::parseBoolean));
                    break;

                case "delimitedTextHeaders":
                    configuration.setDelimitedTextHeaders(converter(value, Function.identity()));
                    break;

                case "delimitedTextDelimiter":
                    configuration.setDelimitedTextDelimiter(converter(value, Function.identity()));
                    break;

                case "firstLineContainsHeaders":
                    configuration.setFirstLineContainsHeaders(converter(value, Boolean::parseBoolean));
                    break;

                case "documentRoot":
                    configuration.setDocumentRoot(converter(value, Function.identity()));
                    break;

                case "dataToExtract":
                    configuration.setDataToExtract(converter(value, BlobIndexerDataToExtract::fromString));
                    break;

                case "imageAction":
                    configuration.setImageAction(converter(value, BlobIndexerImageAction::fromString));
                    break;

                case "allowSkillsetToReadFileData":
                    configuration.setAllowSkillsetToReadFileData(converter(value, Boolean::parseBoolean));
                    break;

                case "pdfTextRotationAlgorithm":
                    configuration.setPdfTextRotationAlgorithm(
                        converter(value, BlobIndexerPdfTextRotationAlgorithm::fromString));
                    break;

                case "executionEnvironment":
                    configuration.setExecutionEnvironment(converter(value, IndexerExecutionEnvironment::fromString));
                    break;

                case "queryTimeout":
                    configuration.setQueryTimeout(converter(value, Function.identity()));
                    break;

                default:
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(key, value);
                    break;
            }
        }

        return configuration.setAdditionalProperties(additionalProperties);
    }

    static <T> T converter(Object value, Function<String, T> conv) {
        return value == null ? null : conv.apply(String.valueOf(value));
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
