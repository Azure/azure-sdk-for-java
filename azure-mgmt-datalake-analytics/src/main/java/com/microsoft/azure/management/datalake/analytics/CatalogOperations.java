/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters;
import com.microsoft.azure.management.datalake.analytics.models.PageImpl;
import com.microsoft.azure.management.datalake.analytics.models.USqlAssembly;
import com.microsoft.azure.management.datalake.analytics.models.USqlAssemblyClr;
import com.microsoft.azure.management.datalake.analytics.models.USqlCredential;
import com.microsoft.azure.management.datalake.analytics.models.USqlDatabase;
import com.microsoft.azure.management.datalake.analytics.models.USqlExternalDataSource;
import com.microsoft.azure.management.datalake.analytics.models.USqlProcedure;
import com.microsoft.azure.management.datalake.analytics.models.USqlSchema;
import com.microsoft.azure.management.datalake.analytics.models.USqlSecret;
import com.microsoft.azure.management.datalake.analytics.models.USqlTable;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableStatistics;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableValuedFunction;
import com.microsoft.azure.management.datalake.analytics.models.USqlType;
import com.microsoft.azure.management.datalake.analytics.models.USqlView;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in CatalogOperations.
 */
public interface CatalogOperations {
    /**
     * Creates the specified secret for use with external data sources in the specified database.
     *
     * @param databaseName The name of the database in which to create the secret.
     * @param secretName The name of the secret.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param parameters The parameters required to create the secret (name and password)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlSecret> createSecret(String databaseName, String secretName, String accountName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Creates the specified secret for use with external data sources in the specified database.
     *
     * @param databaseName The name of the database in which to create the secret.
     * @param secretName The name of the secret.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param parameters The parameters required to create the secret (name and password)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createSecretAsync(String databaseName, String secretName, String accountName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException;

    /**
     * Modifies the specified secret for use with external data sources in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param parameters The parameters required to modify the secret (name and password)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlSecret> updateSecret(String databaseName, String secretName, String accountName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Modifies the specified secret for use with external data sources in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param parameters The parameters required to modify the secret (name and password)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateSecretAsync(String databaseName, String secretName, String accountName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the specified secret in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to get
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlSecret> getSecret(String databaseName, String secretName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets the specified secret in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to get
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getSecretAsync(String databaseName, String secretName, String accountName, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes the specified secret in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to delete
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> deleteSecret(String databaseName, String secretName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Deletes the specified secret in the specified database.
     *
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to delete
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteSecretAsync(String databaseName, String secretName, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified external data source from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data source.
     * @param externalDataSourceName The name of the external data source.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlExternalDataSource object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlExternalDataSource> getExternalDataSource(String databaseName, String externalDataSourceName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified external data source from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data source.
     * @param externalDataSourceName The name of the external data source.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getExternalDataSourceAsync(String databaseName, String externalDataSourceName, String accountName, final ServiceCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data sources.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlExternalDataSource>> listExternalDataSources(final String databaseName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data sources.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listExternalDataSourcesAsync(final String databaseName, final String accountName, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data sources.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlExternalDataSource>> listExternalDataSources(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the external data sources.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listExternalDataSourcesAsync(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified credential from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param credentialName The name of the credential.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlCredential object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlCredential> getCredential(String databaseName, String credentialName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified credential from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param credentialName The name of the credential.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getCredentialAsync(String databaseName, String credentialName, String accountName, final ServiceCallback<USqlCredential> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlCredential>> listCredentials(final String databaseName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listCredentialsAsync(final String databaseName, final String accountName, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlCredential>> listCredentials(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listCredentialsAsync(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified procedure from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedure.
     * @param schemaName The name of the schema containing the procedure.
     * @param procedureName The name of the procedure.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlProcedure object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlProcedure> getProcedure(String databaseName, String schemaName, String procedureName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified procedure from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedure.
     * @param schemaName The name of the schema containing the procedure.
     * @param procedureName The name of the procedure.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getProcedureAsync(String databaseName, String schemaName, String procedureName, String accountName, final ServiceCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlProcedure>> listProcedures(final String databaseName, final String schemaName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listProceduresAsync(final String databaseName, final String schemaName, final String accountName, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlProcedure>> listProcedures(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listProceduresAsync(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table.
     * @param schemaName The name of the schema containing the table.
     * @param tableName The name of the table.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTable object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlTable> getTable(String databaseName, String schemaName, String tableName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table.
     * @param schemaName The name of the schema containing the table.
     * @param tableName The name of the table.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getTableAsync(String databaseName, String schemaName, String tableName, String accountName, final ServiceCallback<USqlTable> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTable>> listTables(final String databaseName, final String schemaName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTablesAsync(final String databaseName, final String schemaName, final String accountName, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTable>> listTables(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTablesAsync(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified view from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the view.
     * @param schemaName The name of the schema containing the view.
     * @param viewName The name of the view.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlView object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlView> getView(String databaseName, String schemaName, String viewName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified view from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the view.
     * @param schemaName The name of the schema containing the view.
     * @param viewName The name of the view.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getViewAsync(String databaseName, String schemaName, String viewName, String accountName, final ServiceCallback<USqlView> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlView>> listViews(final String databaseName, final String schemaName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listViewsAsync(final String databaseName, final String schemaName, final String accountName, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlView>> listViews(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listViewsAsync(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param statisticsName The name of the table statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableStatistics object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlTableStatistics> getTableStatistic(String databaseName, String schemaName, String tableName, String statisticsName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param statisticsName The name of the table statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getTableStatisticAsync(String databaseName, String schemaName, String tableName, String statisticsName, String accountName, final ServiceCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTableStatistics>> listTableStatistics(final String databaseName, final String schemaName, final String tableName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableStatisticsAsync(final String databaseName, final String schemaName, final String tableName, final String accountName, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTableStatistics>> listTableStatistics(final String databaseName, final String schemaName, final String tableName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableStatisticsAsync(final String databaseName, final String schemaName, final String tableName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlType>> listTypes(final String databaseName, final String schemaName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTypesAsync(final String databaseName, final String schemaName, final String accountName, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlType>> listTypes(final String databaseName, final String schemaName, final String accountName, final USqlType filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTypesAsync(final String databaseName, final String schemaName, final String accountName, final USqlType filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified table valued function from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued function.
     * @param schemaName The name of the schema containing the table valued function.
     * @param tableValuedFunctionName The name of the tableValuedFunction.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableValuedFunction object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlTableValuedFunction> getTableValuedFunction(String databaseName, String schemaName, String tableValuedFunctionName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified table valued function from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued function.
     * @param schemaName The name of the schema containing the table valued function.
     * @param tableValuedFunctionName The name of the tableValuedFunction.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getTableValuedFunctionAsync(String databaseName, String schemaName, String tableValuedFunctionName, String accountName, final ServiceCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTableValuedFunction>> listTableValuedFunctions(final String databaseName, final String schemaName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableValuedFunctionsAsync(final String databaseName, final String schemaName, final String accountName, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlTableValuedFunction>> listTableValuedFunctions(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableValuedFunctionsAsync(final String databaseName, final String schemaName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified assembly from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param assemblyName The name of the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlAssembly object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlAssembly> getAssembly(String databaseName, String assemblyName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified assembly from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param assemblyName The name of the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAssemblyAsync(String databaseName, String assemblyName, String accountName, final ServiceCallback<USqlAssembly> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlAssemblyClr>> listAssemblies(final String databaseName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAssembliesAsync(final String databaseName, final String accountName, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlAssemblyClr>> listAssemblies(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the assembly.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAssembliesAsync(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param schemaName The name of the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSchema object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlSchema> getSchema(String databaseName, String schemaName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified schema from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param schemaName The name of the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getSchemaAsync(String databaseName, String schemaName, String accountName, final ServiceCallback<USqlSchema> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlSchema>> listSchemas(final String databaseName, final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listSchemasAsync(final String databaseName, final String accountName, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlSchema>> listSchemas(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database containing the schema.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listSchemasAsync(final String databaseName, final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the specified database from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlDatabase object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<USqlDatabase> getDatabase(String databaseName, String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the specified database from the Data Lake Analytics catalog.
     *
     * @param databaseName The name of the database.
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getDatabaseAsync(String databaseName, String accountName, final ServiceCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlDatabase>> listDatabases(final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listDatabasesAsync(final String accountName, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException;
    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<USqlDatabase>> listDatabases(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listDatabasesAsync(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlExternalDataSource>> listExternalDataSourcesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listExternalDataSourcesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlCredential>> listCredentialsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listCredentialsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlProcedure>> listProceduresNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listProceduresNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlTable>> listTablesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTablesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlView>> listViewsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listViewsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlTableStatistics>> listTableStatisticsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableStatisticsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlType>> listTypesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTypesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlTableValuedFunction>> listTableValuedFunctionsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTableValuedFunctionsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlAssemblyClr>> listAssembliesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAssembliesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlSchema>> listSchemasNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listSchemasNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException;

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<USqlDatabase>> listDatabasesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listDatabasesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException;

}
