/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.CommaStringBuilder;
import com.microsoft.windowsazure.services.core.utils.DateFactory;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.models.BinaryFilterExpression;
import com.microsoft.windowsazure.services.table.models.ConstantFilterExpression;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.FilterExpression;
import com.microsoft.windowsazure.services.table.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.ListTablesOptions;
import com.microsoft.windowsazure.services.table.models.LitteralFilterExpression;
import com.microsoft.windowsazure.services.table.models.QueryBuilder;
import com.microsoft.windowsazure.services.table.models.QueryTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableServiceOptions;
import com.microsoft.windowsazure.services.table.models.UnaryFilterExpression;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class TableRestProxy implements TableContract {
    private static final String API_VERSION = "2011-08-18";
    private final HttpURLConnectionClient channel;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ISO8601DateConverter iso8601DateConverter;
    private final DateFactory dateFactory;
    private final ServiceFilter[] filters;
    private final SharedKeyFilter filter;
    private final AtomReaderWriter atomReaderWriter;

    @Inject
    public TableRestProxy(HttpURLConnectionClient channel, @Named(TableConfiguration.URI) String url,
            SharedKeyFilter filter, DateFactory dateFactory, ISO8601DateConverter iso8601DateConverter,
            AtomReaderWriter atomReaderWriter) {

        this.channel = channel;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.iso8601DateConverter = iso8601DateConverter;
        this.filters = new ServiceFilter[0];
        this.dateFactory = dateFactory;
        this.atomReaderWriter = atomReaderWriter;
        channel.addFilter(filter);
    }

    public TableRestProxy(HttpURLConnectionClient channel, ServiceFilter[] filters, String url, SharedKeyFilter filter,
            DateFactory dateFactory, AtomReaderWriter atomReaderWriter, RFC1123DateConverter dateMapper,
            ISO8601DateConverter iso8601DateConverter) {

        this.channel = channel;
        this.filters = filters;
        this.url = url;
        this.filter = filter;
        this.dateFactory = dateFactory;
        this.atomReaderWriter = atomReaderWriter;
        this.dateMapper = dateMapper;
        this.iso8601DateConverter = iso8601DateConverter;
    }

    @Override
    public TableContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new TableRestProxy(this.channel, newFilters, this.url, this.filter, this.dateFactory,
                this.atomReaderWriter, this.dateMapper, this.iso8601DateConverter);
    }

    private void ThrowIfError(ClientResponse r) {
        PipelineHelpers.ThrowIfError(r);
    }

    private String encodeODataURIValue(String value) {
        //TODO: Unclear if OData value in URI's need to be encoded or not
        return value;
    }

    private List<String> encodeODataURIValues(List<String> values) {
        List<String> list = new ArrayList<String>();
        for (String value : values) {
            list.add(encodeODataURIValue(value));
        }
        return list;
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private WebResource addOptionalQuery(WebResource webResource, QueryBuilder query) {
        if (query == null)
            return webResource;

        if (query.getFields() != null && query.getFields().size() > 0) {
            webResource = addOptionalQueryParam(webResource, "$select",
                    CommaStringBuilder.join(encodeODataURIValues(query.getFields())));
        }

        if (query.getTop() != null) {
            webResource = addOptionalQueryParam(webResource, "$top", encodeODataURIValue(query.getTop().toString()));
        }

        if (query.getFilter() != null) {
            webResource = addOptionalQueryParam(webResource, "$filter", buildFilterExpression(query.getFilter()));
        }

        if (query.getOrderBy() != null) {
            webResource = addOptionalQueryParam(webResource, "$orderby",
                    CommaStringBuilder.join(encodeODataURIValues(query.getOrderBy())));
        }

        if (query.getNextPartitionKey() != null) {
            webResource = addOptionalQueryParam(webResource, "NextPartitionKey",
                    encodeODataURIValue(query.getNextPartitionKey()));
        }

        if (query.getNextRowKey() != null) {
            webResource = addOptionalQueryParam(webResource, "NextRowKey", encodeODataURIValue(query.getNextRowKey()));
        }

        return webResource;
    }

    private String buildFilterExpression(FilterExpression filter) {
        StringBuilder sb = new StringBuilder();
        buildFilterExpression(filter, sb);
        return sb.toString();
    }

    private void buildFilterExpression(FilterExpression filter, StringBuilder sb) {
        if (filter == null)
            return;

        if (filter instanceof LitteralFilterExpression) {
            sb.append(((LitteralFilterExpression) filter).getLitteral());
        }
        else if (filter instanceof ConstantFilterExpression) {
            sb.append("'");
            sb.append(((ConstantFilterExpression) filter).getValue());
            sb.append("'");
        }
        else if (filter instanceof UnaryFilterExpression) {
            sb.append(((UnaryFilterExpression) filter).getOperator());
            sb.append("(");
            buildFilterExpression(((UnaryFilterExpression) filter).getOperand(), sb);
            sb.append(")");
        }
        else if (filter instanceof BinaryFilterExpression) {
            sb.append("(");
            buildFilterExpression(((BinaryFilterExpression) filter).getLeft(), sb);
            sb.append(" ");
            sb.append(((BinaryFilterExpression) filter).getOperator());
            sb.append(" ");
            buildFilterExpression(((BinaryFilterExpression) filter).getRight(), sb);
            sb.append(")");
        }
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value) {
        return PipelineHelpers.addOptionalHeader(builder, name, value);
    }

    private WebResource.Builder addTableRequestHeaders(WebResource.Builder builder) {
        builder = addOptionalHeader(builder, "x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "DataServiceVersion", "1.0;NetFx");
        builder = addOptionalHeader(builder, "MaxDataServiceVersion", "2.0;NetFx");
        builder = addOptionalHeader(builder, "Accept", "application/atom+xml,application/xml");
        builder = addOptionalHeader(builder, "Accept-Charset", "UTF-8");
        return builder;
    }

    private WebResource getResource(TableServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout", options.getTimeout());
        for (ServiceFilter filter : filters) {
            webResource.addFilter(new ClientFilterAdapter(filter));
        }

        return webResource;
    }

    @Override
    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return getServiceProperties(new TableServiceOptions());
    }

    @Override
    public GetServicePropertiesResult getServiceProperties(TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        GetServicePropertiesResult result = new GetServicePropertiesResult();
        result.setValue(builder.get(ServiceProperties.class));
        return result;
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        setServiceProperties(serviceProperties, new TableServiceOptions());
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties, TableServiceOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.put(serviceProperties);
    }

    @Override
    public GetTableResult getTable(String table) throws ServiceException {
        return getTable(table, new TableServiceOptions());
    }

    @Override
    public GetTableResult getTable(String table, TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("Tables" + "('" + table + "')");

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetTableResult result = new GetTableResult();
        result.setTableEntry(atomReaderWriter.parseTableEntry(response.getEntityInputStream()));
        return result;
    }

    @Override
    public QueryTablesResult listTables() throws ServiceException {
        return listTables(new ListTablesOptions());
    }

    @Override
    public QueryTablesResult listTables(ListTablesOptions options) throws ServiceException {
        // Append Max char to end '{' is 1 + 'z' in AsciiTable ==> uppperBound is prefix + '{'
        FilterExpression filter = FilterExpression.and(
                FilterExpression.ge(FilterExpression.litteral("TableName"),
                        FilterExpression.constant(options.getPrefix())),
                FilterExpression.le(FilterExpression.litteral("TableName"),
                        FilterExpression.constant(options.getPrefix() + "{")));

        QueryTablesOptions queryTableOptions = new QueryTablesOptions();
        queryTableOptions.setTimeout(options.getTimeout());
        queryTableOptions.setQuery(new QueryBuilder().setFilter(filter));
        return queryTables(queryTableOptions);
    }

    @Override
    public QueryTablesResult queryTables() throws ServiceException {
        return queryTables(new QueryTablesOptions());
    }

    @Override
    public QueryTablesResult queryTables(QueryTablesOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("Tables");
        webResource = addOptionalQuery(webResource, options.getQuery());

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        QueryTablesResult result = new QueryTablesResult();
        result.setContinuationToken(response.getHeaders().getFirst("x-ms-continuation-NextTableName"));
        result.setTables(atomReaderWriter.parseTableEntries(response.getEntityInputStream()));

        return result;
    }

    @Override
    public void createTable(String table) throws ServiceException {
        createTable(table, new TableServiceOptions());

    }

    @Override
    public void createTable(String table, TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("Tables");

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        builder.entity(atomReaderWriter.generateTableEntry(table), "application/atom+xml");

        ClientResponse response = builder.post(ClientResponse.class);
        ThrowIfError(response);
    }

    @Override
    public void deleteTable(String table) throws ServiceException {
        deleteTable(table, new TableServiceOptions());
    }

    @Override
    public void deleteTable(String table, TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("Tables" + "('" + table + "')");

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);
        builder = addOptionalHeader(builder, "Content-Type", "application/atom+xml");

        ClientResponse response = builder.delete(ClientResponse.class);
        ThrowIfError(response);
    }

    @Override
    public InsertEntityResult insertEntity(String table, Entity entity) throws ServiceException {
        return insertEntity(table, entity, new TableServiceOptions());
    }

    @Override
    public InsertEntityResult insertEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(table);

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        builder = builder.entity(atomReaderWriter.generateEntityEntry(entity), "application/atom+xml");

        ClientResponse response = builder.post(ClientResponse.class);
        ThrowIfError(response);

        InsertEntityResult result = new InsertEntityResult();
        result.setEntity(atomReaderWriter.parseEntityEntry(response.getEntityInputStream()));

        return result;
    }
}
