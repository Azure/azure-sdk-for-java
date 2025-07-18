// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.synapse.artifacts.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A copy activity source.
 */
@Fluent
public class CopySource implements JsonSerializable<CopySource> {
    /*
     * Copy source type.
     */
    @Generated
    private String type = "CopySource";

    /*
     * Source retry count. Type: integer (or Expression with resultType integer).
     */
    @Generated
    private Object sourceRetryCount;

    /*
     * Source retry wait. Type: string (or Expression with resultType string), pattern:
     * ((\d+)\.)?(\d\d):(60|([0-5][0-9])):(60|([0-5][0-9])).
     */
    @Generated
    private Object sourceRetryWait;

    /*
     * The maximum concurrent connection count for the source data store. Type: integer (or Expression with resultType
     * integer).
     */
    @Generated
    private Object maxConcurrentConnections;

    /*
     * A copy activity source.
     */
    @Generated
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of CopySource class.
     */
    @Generated
    public CopySource() {
    }

    /**
     * Get the type property: Copy source type.
     * 
     * @return the type value.
     */
    @Generated
    public String getType() {
        return this.type;
    }

    /**
     * Get the sourceRetryCount property: Source retry count. Type: integer (or Expression with resultType integer).
     * 
     * @return the sourceRetryCount value.
     */
    @Generated
    public Object getSourceRetryCount() {
        return this.sourceRetryCount;
    }

    /**
     * Set the sourceRetryCount property: Source retry count. Type: integer (or Expression with resultType integer).
     * 
     * @param sourceRetryCount the sourceRetryCount value to set.
     * @return the CopySource object itself.
     */
    @Generated
    public CopySource setSourceRetryCount(Object sourceRetryCount) {
        this.sourceRetryCount = sourceRetryCount;
        return this;
    }

    /**
     * Get the sourceRetryWait property: Source retry wait. Type: string (or Expression with resultType string),
     * pattern: ((\d+)\.)?(\d\d):(60|([0-5][0-9])):(60|([0-5][0-9])).
     * 
     * @return the sourceRetryWait value.
     */
    @Generated
    public Object getSourceRetryWait() {
        return this.sourceRetryWait;
    }

    /**
     * Set the sourceRetryWait property: Source retry wait. Type: string (or Expression with resultType string),
     * pattern: ((\d+)\.)?(\d\d):(60|([0-5][0-9])):(60|([0-5][0-9])).
     * 
     * @param sourceRetryWait the sourceRetryWait value to set.
     * @return the CopySource object itself.
     */
    @Generated
    public CopySource setSourceRetryWait(Object sourceRetryWait) {
        this.sourceRetryWait = sourceRetryWait;
        return this;
    }

    /**
     * Get the maxConcurrentConnections property: The maximum concurrent connection count for the source data store.
     * Type: integer (or Expression with resultType integer).
     * 
     * @return the maxConcurrentConnections value.
     */
    @Generated
    public Object getMaxConcurrentConnections() {
        return this.maxConcurrentConnections;
    }

    /**
     * Set the maxConcurrentConnections property: The maximum concurrent connection count for the source data store.
     * Type: integer (or Expression with resultType integer).
     * 
     * @param maxConcurrentConnections the maxConcurrentConnections value to set.
     * @return the CopySource object itself.
     */
    @Generated
    public CopySource setMaxConcurrentConnections(Object maxConcurrentConnections) {
        this.maxConcurrentConnections = maxConcurrentConnections;
        return this;
    }

    /**
     * Get the additionalProperties property: A copy activity source.
     * 
     * @return the additionalProperties value.
     */
    @Generated
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Set the additionalProperties property: A copy activity source.
     * 
     * @param additionalProperties the additionalProperties value to set.
     * @return the CopySource object itself.
     */
    @Generated
    public CopySource setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        if (this.sourceRetryCount != null) {
            jsonWriter.writeUntypedField("sourceRetryCount", this.sourceRetryCount);
        }
        if (this.sourceRetryWait != null) {
            jsonWriter.writeUntypedField("sourceRetryWait", this.sourceRetryWait);
        }
        if (this.maxConcurrentConnections != null) {
            jsonWriter.writeUntypedField("maxConcurrentConnections", this.maxConcurrentConnections);
        }
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CopySource from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CopySource if the JsonReader was pointing to an instance of it, or null if it was pointing
     * to JSON null.
     * @throws IOException If an error occurs while reading the CopySource.
     */
    @Generated
    public static CopySource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                readerToUse.nextToken(); // Prepare for reading
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("AvroSource".equals(discriminatorValue)) {
                    return AvroSource.fromJson(readerToUse.reset());
                } else if ("ExcelSource".equals(discriminatorValue)) {
                    return ExcelSource.fromJson(readerToUse.reset());
                } else if ("ParquetSource".equals(discriminatorValue)) {
                    return ParquetSource.fromJson(readerToUse.reset());
                } else if ("DelimitedTextSource".equals(discriminatorValue)) {
                    return DelimitedTextSource.fromJson(readerToUse.reset());
                } else if ("JsonSource".equals(discriminatorValue)) {
                    return JsonSource.fromJson(readerToUse.reset());
                } else if ("XmlSource".equals(discriminatorValue)) {
                    return XmlSource.fromJson(readerToUse.reset());
                } else if ("OrcSource".equals(discriminatorValue)) {
                    return OrcSource.fromJson(readerToUse.reset());
                } else if ("BinarySource".equals(discriminatorValue)) {
                    return BinarySource.fromJson(readerToUse.reset());
                } else if ("TabularSource".equals(discriminatorValue)) {
                    return TabularSource.fromJsonKnownDiscriminator(readerToUse.reset());
                } else if ("AzureTableSource".equals(discriminatorValue)) {
                    return AzureTableSource.fromJson(readerToUse.reset());
                } else if ("InformixSource".equals(discriminatorValue)) {
                    return InformixSource.fromJson(readerToUse.reset());
                } else if ("Db2Source".equals(discriminatorValue)) {
                    return Db2Source.fromJson(readerToUse.reset());
                } else if ("OdbcSource".equals(discriminatorValue)) {
                    return OdbcSource.fromJson(readerToUse.reset());
                } else if ("MySqlSource".equals(discriminatorValue)) {
                    return MySqlSource.fromJson(readerToUse.reset());
                } else if ("PostgreSqlSource".equals(discriminatorValue)) {
                    return PostgreSqlSource.fromJson(readerToUse.reset());
                } else if ("PostgreSqlV2Source".equals(discriminatorValue)) {
                    return PostgreSqlV2Source.fromJson(readerToUse.reset());
                } else if ("SybaseSource".equals(discriminatorValue)) {
                    return SybaseSource.fromJson(readerToUse.reset());
                } else if ("SapBwSource".equals(discriminatorValue)) {
                    return SapBwSource.fromJson(readerToUse.reset());
                } else if ("SalesforceSource".equals(discriminatorValue)) {
                    return SalesforceSource.fromJson(readerToUse.reset());
                } else if ("SapCloudForCustomerSource".equals(discriminatorValue)) {
                    return SapCloudForCustomerSource.fromJson(readerToUse.reset());
                } else if ("SapEccSource".equals(discriminatorValue)) {
                    return SapEccSource.fromJson(readerToUse.reset());
                } else if ("SapHanaSource".equals(discriminatorValue)) {
                    return SapHanaSource.fromJson(readerToUse.reset());
                } else if ("SapOpenHubSource".equals(discriminatorValue)) {
                    return SapOpenHubSource.fromJson(readerToUse.reset());
                } else if ("SapOdpSource".equals(discriminatorValue)) {
                    return SapOdpSource.fromJson(readerToUse.reset());
                } else if ("SapTableSource".equals(discriminatorValue)) {
                    return SapTableSource.fromJson(readerToUse.reset());
                } else if ("SqlSource".equals(discriminatorValue)) {
                    return SqlSource.fromJson(readerToUse.reset());
                } else if ("SqlServerSource".equals(discriminatorValue)) {
                    return SqlServerSource.fromJson(readerToUse.reset());
                } else if ("AmazonRdsForSqlServerSource".equals(discriminatorValue)) {
                    return AmazonRdsForSqlServerSource.fromJson(readerToUse.reset());
                } else if ("AzureSqlSource".equals(discriminatorValue)) {
                    return AzureSqlSource.fromJson(readerToUse.reset());
                } else if ("SqlMISource".equals(discriminatorValue)) {
                    return SqlMISource.fromJson(readerToUse.reset());
                } else if ("SqlDWSource".equals(discriminatorValue)) {
                    return SqlDWSource.fromJson(readerToUse.reset());
                } else if ("AzureMySqlSource".equals(discriminatorValue)) {
                    return AzureMySqlSource.fromJson(readerToUse.reset());
                } else if ("TeradataSource".equals(discriminatorValue)) {
                    return TeradataSource.fromJson(readerToUse.reset());
                } else if ("CassandraSource".equals(discriminatorValue)) {
                    return CassandraSource.fromJson(readerToUse.reset());
                } else if ("AmazonMWSSource".equals(discriminatorValue)) {
                    return AmazonMWSSource.fromJson(readerToUse.reset());
                } else if ("AzurePostgreSqlSource".equals(discriminatorValue)) {
                    return AzurePostgreSqlSource.fromJson(readerToUse.reset());
                } else if ("ConcurSource".equals(discriminatorValue)) {
                    return ConcurSource.fromJson(readerToUse.reset());
                } else if ("CouchbaseSource".equals(discriminatorValue)) {
                    return CouchbaseSource.fromJson(readerToUse.reset());
                } else if ("DrillSource".equals(discriminatorValue)) {
                    return DrillSource.fromJson(readerToUse.reset());
                } else if ("EloquaSource".equals(discriminatorValue)) {
                    return EloquaSource.fromJson(readerToUse.reset());
                } else if ("GoogleBigQuerySource".equals(discriminatorValue)) {
                    return GoogleBigQuerySource.fromJson(readerToUse.reset());
                } else if ("GoogleBigQueryV2Source".equals(discriminatorValue)) {
                    return GoogleBigQueryV2Source.fromJson(readerToUse.reset());
                } else if ("GreenplumSource".equals(discriminatorValue)) {
                    return GreenplumSource.fromJson(readerToUse.reset());
                } else if ("HBaseSource".equals(discriminatorValue)) {
                    return HBaseSource.fromJson(readerToUse.reset());
                } else if ("HiveSource".equals(discriminatorValue)) {
                    return HiveSource.fromJson(readerToUse.reset());
                } else if ("HubspotSource".equals(discriminatorValue)) {
                    return HubspotSource.fromJson(readerToUse.reset());
                } else if ("ImpalaSource".equals(discriminatorValue)) {
                    return ImpalaSource.fromJson(readerToUse.reset());
                } else if ("JiraSource".equals(discriminatorValue)) {
                    return JiraSource.fromJson(readerToUse.reset());
                } else if ("MagentoSource".equals(discriminatorValue)) {
                    return MagentoSource.fromJson(readerToUse.reset());
                } else if ("MariaDBSource".equals(discriminatorValue)) {
                    return MariaDBSource.fromJson(readerToUse.reset());
                } else if ("AzureMariaDBSource".equals(discriminatorValue)) {
                    return AzureMariaDBSource.fromJson(readerToUse.reset());
                } else if ("MarketoSource".equals(discriminatorValue)) {
                    return MarketoSource.fromJson(readerToUse.reset());
                } else if ("PaypalSource".equals(discriminatorValue)) {
                    return PaypalSource.fromJson(readerToUse.reset());
                } else if ("PhoenixSource".equals(discriminatorValue)) {
                    return PhoenixSource.fromJson(readerToUse.reset());
                } else if ("PrestoSource".equals(discriminatorValue)) {
                    return PrestoSource.fromJson(readerToUse.reset());
                } else if ("QuickBooksSource".equals(discriminatorValue)) {
                    return QuickBooksSource.fromJson(readerToUse.reset());
                } else if ("ServiceNowSource".equals(discriminatorValue)) {
                    return ServiceNowSource.fromJson(readerToUse.reset());
                } else if ("ShopifySource".equals(discriminatorValue)) {
                    return ShopifySource.fromJson(readerToUse.reset());
                } else if ("SparkSource".equals(discriminatorValue)) {
                    return SparkSource.fromJson(readerToUse.reset());
                } else if ("SquareSource".equals(discriminatorValue)) {
                    return SquareSource.fromJson(readerToUse.reset());
                } else if ("XeroSource".equals(discriminatorValue)) {
                    return XeroSource.fromJson(readerToUse.reset());
                } else if ("ZohoSource".equals(discriminatorValue)) {
                    return ZohoSource.fromJson(readerToUse.reset());
                } else if ("NetezzaSource".equals(discriminatorValue)) {
                    return NetezzaSource.fromJson(readerToUse.reset());
                } else if ("VerticaSource".equals(discriminatorValue)) {
                    return VerticaSource.fromJson(readerToUse.reset());
                } else if ("SalesforceMarketingCloudSource".equals(discriminatorValue)) {
                    return SalesforceMarketingCloudSource.fromJson(readerToUse.reset());
                } else if ("ResponsysSource".equals(discriminatorValue)) {
                    return ResponsysSource.fromJson(readerToUse.reset());
                } else if ("DynamicsAXSource".equals(discriminatorValue)) {
                    return DynamicsAXSource.fromJson(readerToUse.reset());
                } else if ("OracleServiceCloudSource".equals(discriminatorValue)) {
                    return OracleServiceCloudSource.fromJson(readerToUse.reset());
                } else if ("GoogleAdWordsSource".equals(discriminatorValue)) {
                    return GoogleAdWordsSource.fromJson(readerToUse.reset());
                } else if ("AmazonRedshiftSource".equals(discriminatorValue)) {
                    return AmazonRedshiftSource.fromJson(readerToUse.reset());
                } else if ("WarehouseSource".equals(discriminatorValue)) {
                    return WarehouseSource.fromJson(readerToUse.reset());
                } else if ("SalesforceV2Source".equals(discriminatorValue)) {
                    return SalesforceV2Source.fromJson(readerToUse.reset());
                } else if ("ServiceNowV2Source".equals(discriminatorValue)) {
                    return ServiceNowV2Source.fromJson(readerToUse.reset());
                } else if ("BlobSource".equals(discriminatorValue)) {
                    return BlobSource.fromJson(readerToUse.reset());
                } else if ("DocumentDbCollectionSource".equals(discriminatorValue)) {
                    return DocumentDbCollectionSource.fromJson(readerToUse.reset());
                } else if ("CosmosDbSqlApiSource".equals(discriminatorValue)) {
                    return CosmosDbSqlApiSource.fromJson(readerToUse.reset());
                } else if ("DynamicsSource".equals(discriminatorValue)) {
                    return DynamicsSource.fromJson(readerToUse.reset());
                } else if ("DynamicsCrmSource".equals(discriminatorValue)) {
                    return DynamicsCrmSource.fromJson(readerToUse.reset());
                } else if ("CommonDataServiceForAppsSource".equals(discriminatorValue)) {
                    return CommonDataServiceForAppsSource.fromJson(readerToUse.reset());
                } else if ("RelationalSource".equals(discriminatorValue)) {
                    return RelationalSource.fromJson(readerToUse.reset());
                } else if ("MicrosoftAccessSource".equals(discriminatorValue)) {
                    return MicrosoftAccessSource.fromJson(readerToUse.reset());
                } else if ("ODataSource".equals(discriminatorValue)) {
                    return ODataSource.fromJson(readerToUse.reset());
                } else if ("SalesforceServiceCloudSource".equals(discriminatorValue)) {
                    return SalesforceServiceCloudSource.fromJson(readerToUse.reset());
                } else if ("RestSource".equals(discriminatorValue)) {
                    return RestSource.fromJson(readerToUse.reset());
                } else if ("FileSystemSource".equals(discriminatorValue)) {
                    return FileSystemSource.fromJson(readerToUse.reset());
                } else if ("HdfsSource".equals(discriminatorValue)) {
                    return HdfsSource.fromJson(readerToUse.reset());
                } else if ("AzureDataExplorerSource".equals(discriminatorValue)) {
                    return AzureDataExplorerSource.fromJson(readerToUse.reset());
                } else if ("OracleSource".equals(discriminatorValue)) {
                    return OracleSource.fromJson(readerToUse.reset());
                } else if ("AmazonRdsForOracleSource".equals(discriminatorValue)) {
                    return AmazonRdsForOracleSource.fromJson(readerToUse.reset());
                } else if ("WebSource".equals(discriminatorValue)) {
                    return WebSource.fromJson(readerToUse.reset());
                } else if ("MongoDbSource".equals(discriminatorValue)) {
                    return MongoDbSource.fromJson(readerToUse.reset());
                } else if ("MongoDbAtlasSource".equals(discriminatorValue)) {
                    return MongoDbAtlasSource.fromJson(readerToUse.reset());
                } else if ("MongoDbV2Source".equals(discriminatorValue)) {
                    return MongoDbV2Source.fromJson(readerToUse.reset());
                } else if ("CosmosDbMongoDbApiSource".equals(discriminatorValue)) {
                    return CosmosDbMongoDbApiSource.fromJson(readerToUse.reset());
                } else if ("Office365Source".equals(discriminatorValue)) {
                    return Office365Source.fromJson(readerToUse.reset());
                } else if ("AzureDataLakeStoreSource".equals(discriminatorValue)) {
                    return AzureDataLakeStoreSource.fromJson(readerToUse.reset());
                } else if ("AzureBlobFSSource".equals(discriminatorValue)) {
                    return AzureBlobFSSource.fromJson(readerToUse.reset());
                } else if ("HttpSource".equals(discriminatorValue)) {
                    return HttpSource.fromJson(readerToUse.reset());
                } else if ("LakeHouseTableSource".equals(discriminatorValue)) {
                    return LakeHouseTableSource.fromJson(readerToUse.reset());
                } else if ("SnowflakeSource".equals(discriminatorValue)) {
                    return SnowflakeSource.fromJson(readerToUse.reset());
                } else if ("SnowflakeV2Source".equals(discriminatorValue)) {
                    return SnowflakeV2Source.fromJson(readerToUse.reset());
                } else if ("AzureDatabricksDeltaLakeSource".equals(discriminatorValue)) {
                    return AzureDatabricksDeltaLakeSource.fromJson(readerToUse.reset());
                } else if ("SharePointOnlineListSource".equals(discriminatorValue)) {
                    return SharePointOnlineListSource.fromJson(readerToUse.reset());
                } else if ("SalesforceServiceCloudV2Source".equals(discriminatorValue)) {
                    return SalesforceServiceCloudV2Source.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static CopySource fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CopySource deserializedCopySource = new CopySource();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    deserializedCopySource.type = reader.getString();
                } else if ("sourceRetryCount".equals(fieldName)) {
                    deserializedCopySource.sourceRetryCount = reader.readUntyped();
                } else if ("sourceRetryWait".equals(fieldName)) {
                    deserializedCopySource.sourceRetryWait = reader.readUntyped();
                } else if ("maxConcurrentConnections".equals(fieldName)) {
                    deserializedCopySource.maxConcurrentConnections = reader.readUntyped();
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedCopySource.additionalProperties = additionalProperties;

            return deserializedCopySource;
        });
    }
}
