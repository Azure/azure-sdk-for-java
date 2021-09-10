// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.BlobIndexerDataToExtract;
import com.azure.search.documents.indexes.models.BlobIndexerImageAction;
import com.azure.search.documents.indexes.models.BlobIndexerParsingMode;
import com.azure.search.documents.indexes.models.BlobIndexerPdfTextRotationAlgorithm;
import com.azure.search.documents.indexes.models.CreateOrUpdateDataSourceConnectionOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateIndexerOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateSkillsetOptions;
import com.azure.search.documents.indexes.models.IndexerExecutionEnvironment;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingParametersConfiguration;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionsBagsTests {
    @ParameterizedTest
    @MethodSource("nullRequiredValuesThrowNullPointerExceptionsSupplier")
    public void nullRequiredValuesThrowNullPointerExceptions(Executable constructorCallWithNullParameter) {
        assertThrows(NullPointerException.class, constructorCallWithNullParameter);
    }

    private static Stream<Executable> nullRequiredValuesThrowNullPointerExceptionsSupplier() {
        return Stream.of(
            () -> new CreateOrUpdateDataSourceConnectionOptions(null),
            () -> new CreateOrUpdateIndexerOptions(null),
            () -> new CreateOrUpdateSkillsetOptions(null)
        );
    }

    @ParameterizedTest
    @MethodSource("getIndexingParametersConfigurationSupplier")
    public void getIndexingParametersConfiguration(Map<String, Object> configuration,
        Function<IndexingParametersConfiguration, Object> parameterGetter, Object expected) {
        IndexingParametersConfiguration indexingParametersConfiguration = new IndexingParameters()
            .setConfiguration(configuration)
            .getIndexingParametersConfiguration();

        assertEquals(expected, parameterGetter.apply(indexingParametersConfiguration));
    }

    private static Stream<Arguments> getIndexingParametersConfigurationSupplier() {
        return Stream.of(
            createArguments("parsingMode", BlobIndexerParsingMode.DEFAULT,
                IndexingParametersConfiguration::getParsingMode),

            createArguments("excludedFileNameExtensions", "parquet",
                IndexingParametersConfiguration::getExcludedFileNameExtensions),

            createArguments("indexedFileNameExtensions", "json",
                IndexingParametersConfiguration::getIndexedFileNameExtensions),

            createArguments("failOnUnsupportedContentType", true,
                IndexingParametersConfiguration::isFailOnUnsupportedContentType),

            createArguments("failOnUnprocessableDocument", true,
                IndexingParametersConfiguration::isFailOnUnprocessableDocument),

            createArguments("indexStorageMetadataOnlyForOversizedDocuments", true,
                IndexingParametersConfiguration::isIndexStorageMetadataOnlyForOversizedDocuments),

            createArguments("delimitedTextHeaders", "headers",
                IndexingParametersConfiguration::getDelimitedTextHeaders),

            createArguments("delimitedTextDelimiter", ",", IndexingParametersConfiguration::getDelimitedTextDelimiter),

            createArguments("firstLineContainsHeaders", true,
                IndexingParametersConfiguration::isFirstLineContainsHeaders),

            createArguments("documentRoot", "/", IndexingParametersConfiguration::getDocumentRoot),

            createArguments("dataToExtract", BlobIndexerDataToExtract.CONTENT_AND_METADATA,
                IndexingParametersConfiguration::getDataToExtract),

            createArguments("imageAction", BlobIndexerImageAction.GENERATE_NORMALIZED_IMAGE_PER_PAGE,
                IndexingParametersConfiguration::getImageAction),

            createArguments("allowSkillsetToReadFileData", true,
                IndexingParametersConfiguration::isAllowSkillsetToReadFileData),

            createArguments("pdfTextRotationAlgorithm", BlobIndexerPdfTextRotationAlgorithm.DETECT_ANGLES,
                IndexingParametersConfiguration::getPdfTextRotationAlgorithm),

            createArguments("executionEnvironment", IndexerExecutionEnvironment.STANDARD,
                IndexingParametersConfiguration::getExecutionEnvironment),

            createArguments("queryTimeout", "1:00:00", IndexingParametersConfiguration::getQueryTimeout)
        );
    }

    private static Arguments createArguments(String key, Object expected,
        Function<IndexingParametersConfiguration, Object> getter) {
        return Arguments.of(Collections.singletonMap(key, expected), getter, expected);
    }
}
