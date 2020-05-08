package com.azure.search.documents;

import com.azure.core.util.Configuration;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.ResourceCounter;
import com.azure.search.documents.models.ServiceCounters;
import com.azure.search.documents.models.ServiceLimits;
import com.azure.search.documents.models.ServiceStatistics;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.TestHelpers.waitForIndexing;

public final class ServiceResourceHelpers {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final String HOTEL_INDEX_NAME = "hotels";

    public static final String BLOB_DATASOURCE_NAME = "azs-java-live-blob";
    public static final String BLOB_DATASOURCE_TEST_NAME = "azs-java-test-blob";
    public static final String SQL_DATASOURCE_NAME = "azs-java-test-sql";

    public static <T> void uploadDocuments(SearchIndexClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc);
        waitForIndexing();
    }

    public static <T> void uploadDocuments(SearchIndexAsyncClient client, List<T> uploadDoc) {
        client.uploadDocuments(uploadDoc).block();
        waitForIndexing();
    }

    public static <T> void uploadDocument(SearchIndexClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc));
        waitForIndexing();
    }

    public static <T> void uploadDocument(SearchIndexAsyncClient client, T uploadDoc) {
        client.uploadDocuments(Collections.singletonList(uploadDoc)).block();
        waitForIndexing();
    }

    public static List<Map<String, Object>> uploadDocumentsJson(SearchIndexClient client, String dataJson) {
        List<Map<String, Object>> documents = readJsonFileToList(dataJson);
        uploadDocuments(client, documents);

        return documents;
    }

    private static List<Map<String, Object>> readJsonFileToList(String filename) {
        Reader reader = new InputStreamReader(Objects.requireNonNull(ServiceResourceHelpers.class.getClassLoader()
            .getResourceAsStream(filename)));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SerializationUtil.configureMapper(objectMapper);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(reader, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DataSource createBlobDataSource() {
        String storageConnectionString = Configuration.getGlobalConfiguration()
            .get("AZURE_SEARCH_STORAGE_CONNECTION_STRING", "connectionString");
        String blobContainerName = Configuration.getGlobalConfiguration()
            .get("AZURE_SEARCH_STORAGE_CONTAINER_NAME", "container");

        // create the new data source object for this storage account and container
        return DataSources.createFromAzureBlobStorage(BLOB_DATASOURCE_NAME, storageConnectionString,
            blobContainerName, "/", "real live blob", new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue"));
    }

    public static ServiceStatistics getExpectedServiceStatistics() {
        ServiceCounters serviceCounters = new ServiceCounters()
            .setDocumentCounter(new ResourceCounter().setUsage(0).setQuota(null))
            .setIndexCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setIndexerCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setDataSourceCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setStorageSizeCounter(new ResourceCounter().setUsage(0).setQuota(52428800L))
            .setSynonymMapCounter(new ResourceCounter().setUsage(0).setQuota(3L));

        ServiceLimits serviceLimits = new ServiceLimits()
            .setMaxFieldsPerIndex(1000)
            .setMaxFieldNestingDepthPerIndex(10)
            .setMaxComplexCollectionFieldsPerIndex(40)
            .setMaxComplexObjectsInCollectionsPerDocument(3000);

        return new ServiceStatistics()
            .setCounters(serviceCounters)
            .setLimits(serviceLimits);
    }
}
