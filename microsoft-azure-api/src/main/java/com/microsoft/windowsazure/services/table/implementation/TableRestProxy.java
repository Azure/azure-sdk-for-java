/**
 * Copyright 2012 Microsoft Corporation
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Header;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMultipart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.CommaStringBuilder;
import com.microsoft.windowsazure.services.core.utils.DateFactory;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.implementation.HttpReaderWriter.StatusLine;
import com.microsoft.windowsazure.services.table.models.BatchOperations;
import com.microsoft.windowsazure.services.table.models.BatchOperations.DeleteEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.InsertEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.InsertOrMergeEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.InsertOrReplaceEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.MergeEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.Operation;
import com.microsoft.windowsazure.services.table.models.BatchOperations.UpdateEntityOperation;
import com.microsoft.windowsazure.services.table.models.BatchResult;
import com.microsoft.windowsazure.services.table.models.BatchResult.DeleteEntity;
import com.microsoft.windowsazure.services.table.models.BatchResult.Entry;
import com.microsoft.windowsazure.services.table.models.BatchResult.Error;
import com.microsoft.windowsazure.services.table.models.BatchResult.InsertEntity;
import com.microsoft.windowsazure.services.table.models.BatchResult.UpdateEntity;
import com.microsoft.windowsazure.services.table.models.BinaryFilter;
import com.microsoft.windowsazure.services.table.models.ConstantFilter;
import com.microsoft.windowsazure.services.table.models.DeleteEntityOptions;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.Filter;
import com.microsoft.windowsazure.services.table.models.GetEntityResult;
import com.microsoft.windowsazure.services.table.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.PropertyNameFilter;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesOptions;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesResult;
import com.microsoft.windowsazure.services.table.models.QueryStringFilter;
import com.microsoft.windowsazure.services.table.models.QueryTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableServiceOptions;
import com.microsoft.windowsazure.services.table.models.UnaryFilter;
import com.microsoft.windowsazure.services.table.models.UpdateEntityResult;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.ReaderWriter;

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
    private final MimeReaderWriter mimeReaderWriter;
    private final HttpReaderWriter httpReaderWriter;

    @Inject
    public TableRestProxy(HttpURLConnectionClient channel, @Named(TableConfiguration.URI) String url,
            SharedKeyFilter filter, DateFactory dateFactory, ISO8601DateConverter iso8601DateConverter,
            AtomReaderWriter atomReaderWriter, MimeReaderWriter mimeReaderWriter, HttpReaderWriter httpReaderWriter) {

        this.channel = channel;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.iso8601DateConverter = iso8601DateConverter;
        this.filters = new ServiceFilter[0];
        this.dateFactory = dateFactory;
        this.atomReaderWriter = atomReaderWriter;
        this.mimeReaderWriter = mimeReaderWriter;
        this.httpReaderWriter = httpReaderWriter;
        channel.addFilter(filter);
    }

    public TableRestProxy(HttpURLConnectionClient channel, ServiceFilter[] filters, String url, SharedKeyFilter filter,
            DateFactory dateFactory, AtomReaderWriter atomReaderWriter, MimeReaderWriter mimeReaderWriter,
            HttpReaderWriter httpReaderWriter, RFC1123DateConverter dateMapper,
            ISO8601DateConverter iso8601DateConverter) {

        this.channel = channel;
        this.filters = filters;
        this.url = url;
        this.filter = filter;
        this.dateFactory = dateFactory;
        this.atomReaderWriter = atomReaderWriter;
        this.mimeReaderWriter = mimeReaderWriter;
        this.httpReaderWriter = httpReaderWriter;
        this.dateMapper = dateMapper;
        this.iso8601DateConverter = iso8601DateConverter;
    }

    @Override
    public TableContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new TableRestProxy(this.channel, newFilters, this.url, this.filter, this.dateFactory,
                this.atomReaderWriter, this.mimeReaderWriter, this.httpReaderWriter, this.dateMapper,
                this.iso8601DateConverter);
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

    private String getEntityPath(String table, String partitionKey, String rowKey) {
        return table + "(" + "PartitionKey='" + safeEncode(partitionKey) + "',RowKey='" + safeEncode(rowKey) + "')";
    }

    private String safeEncode(String input) {
        String fixSingleQuotes = input.replace("'", "''");
        try {
            return URLEncoder.encode(fixSingleQuotes, "UTF-8").replace("+", "%20");
        }
        catch (UnsupportedEncodingException e) {
            return fixSingleQuotes;
        }
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private WebResource addOptionalQueryEntitiesOptions(WebResource webResource,
            QueryEntitiesOptions queryEntitiesOptions) {
        if (queryEntitiesOptions == null)
            return webResource;

        if (queryEntitiesOptions.getSelectFields() != null && queryEntitiesOptions.getSelectFields().size() > 0) {
            webResource = addOptionalQueryParam(webResource, "$select",
                    CommaStringBuilder.join(encodeODataURIValues(queryEntitiesOptions.getSelectFields())));
        }

        if (queryEntitiesOptions.getTop() != null) {
            webResource = addOptionalQueryParam(webResource, "$top", encodeODataURIValue(queryEntitiesOptions.getTop()
                    .toString()));
        }

        if (queryEntitiesOptions.getFilter() != null) {
            webResource = addOptionalQueryParam(webResource, "$filter",
                    buildFilterExpression(queryEntitiesOptions.getFilter()));
        }

        if (queryEntitiesOptions.getOrderByFields() != null) {
            webResource = addOptionalQueryParam(webResource, "$orderby",
                    CommaStringBuilder.join(encodeODataURIValues(queryEntitiesOptions.getOrderByFields())));
        }

        return webResource;
    }

    private String buildFilterExpression(Filter filter) {
        StringBuilder sb = new StringBuilder();
        buildFilterExpression(filter, sb);
        return sb.toString();
    }

    private void buildFilterExpression(Filter filter, StringBuilder sb) {
        if (filter == null)
            return;

        if (filter instanceof PropertyNameFilter) {
            sb.append(((PropertyNameFilter) filter).getPropertyName());
        }
        else if (filter instanceof ConstantFilter) {
            Object value = ((ConstantFilter) filter).getValue();
            if (value == null) {
                sb.append("null");
            }
            else if (value.getClass() == Long.class) {
                sb.append(value);
                sb.append("L");
            }
            else if (value.getClass() == Date.class) {
                ISO8601DateConverter dateConverter = new ISO8601DateConverter();
                sb.append("datetime'");
                sb.append(dateConverter.format((Date) value));
                sb.append("'");
            }
            else if (value.getClass() == UUID.class) {
                sb.append("(guid'");
                sb.append(value);
                sb.append("')");
            }
            else if (value.getClass() == String.class) {
                sb.append("'");
                sb.append(((String) value).replace("'", "''"));
                sb.append("'");
            }
            else if (value.getClass() == byte[].class) {
                sb.append("X'");
                byte[] byteArray = (byte[]) value;
                Formatter formatter = new Formatter(sb);
                for (byte b : byteArray) {
                    formatter.format("%02x", b);
                }
                sb.append("'");
            }
            else if (value.getClass() == Byte[].class) {
                sb.append("X'");
                Byte[] byteArray = (Byte[]) value;
                Formatter formatter = new Formatter(sb);
                for (Byte b : byteArray) {
                    formatter.format("%02x", b);
                }
                sb.append("'");
            }
            else {
                sb.append(value);
            }
        }
        else if (filter instanceof UnaryFilter) {
            sb.append(((UnaryFilter) filter).getOperator());
            sb.append("(");
            buildFilterExpression(((UnaryFilter) filter).getOperand(), sb);
            sb.append(")");
        }
        else if (filter instanceof BinaryFilter) {
            sb.append("(");
            buildFilterExpression(((BinaryFilter) filter).getLeft(), sb);
            sb.append(" ");
            sb.append(((BinaryFilter) filter).getOperator());
            sb.append(" ");
            buildFilterExpression(((BinaryFilter) filter).getRight(), sb);
            sb.append(")");
        }
        else if (filter instanceof QueryStringFilter) {
            sb.append(((QueryStringFilter) filter).getQueryString());
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

    private WebResource.Builder addIfMatchHeader(WebResource.Builder builder, String eTag) {
        builder = addOptionalHeader(builder, "If-Match", eTag == null ? "*" : eTag);
        return builder;
    }

    private WebResource getResource(TableServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
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
        if (serviceProperties == null)
            throw new NullPointerException();

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
        if (table == null)
            throw new NullPointerException();

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
    public QueryTablesResult queryTables() throws ServiceException {
        return queryTables(new QueryTablesOptions());
    }

    @Override
    public QueryTablesResult queryTables(QueryTablesOptions options) throws ServiceException {
        Filter queryFilter = options.getFilter();
        String nextTableName = options.getNextTableName();
        String prefix = options.getPrefix();

        if (prefix != null) {
            // Append Max char to end '{' is 1 + 'z' in AsciiTable ==> upperBound is prefix + '{'
            Filter prefixFilter = Filter.and(Filter.ge(Filter.propertyName("TableName"), Filter.constant(prefix)),
                    Filter.le(Filter.propertyName("TableName"), Filter.constant(prefix + "{")));

            if (queryFilter == null) {
                queryFilter = prefixFilter;
            }
            else {
                queryFilter = Filter.and(queryFilter, prefixFilter);
            }
        }

        WebResource webResource = getResource(options).path("Tables");
        webResource = addOptionalQueryParam(webResource, "$filter", buildFilterExpression(queryFilter));
        webResource = addOptionalQueryParam(webResource, "NextTableName", nextTableName);

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        QueryTablesResult result = new QueryTablesResult();
        result.setNextTableName(response.getHeaders().getFirst("x-ms-continuation-NextTableName"));
        result.setTables(atomReaderWriter.parseTableEntries(response.getEntityInputStream()));

        return result;
    }

    @Override
    public void createTable(String table) throws ServiceException {
        createTable(table, new TableServiceOptions());

    }

    @Override
    public void createTable(String table, TableServiceOptions options) throws ServiceException {
        if (table == null)
            throw new NullPointerException();

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
        if (table == null)
            throw new NullPointerException();

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
        if (table == null)
            throw new NullPointerException();

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

    @Override
    public UpdateEntityResult updateEntity(String table, Entity entity) throws ServiceException {
        return updateEntity(table, entity, new TableServiceOptions());
    }

    @Override
    public UpdateEntityResult updateEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException {
        return putOrMergeEntityCore(table, entity, "PUT", true/*includeEtag*/, options);
    }

    @Override
    public UpdateEntityResult mergeEntity(String table, Entity entity) throws ServiceException {
        return mergeEntity(table, entity, new TableServiceOptions());
    }

    @Override
    public UpdateEntityResult mergeEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException {
        return putOrMergeEntityCore(table, entity, "MERGE", true/*includeEtag*/, options);
    }

    @Override
    public UpdateEntityResult insertOrReplaceEntity(String table, Entity entity) throws ServiceException {
        return insertOrReplaceEntity(table, entity, new TableServiceOptions());
    }

    @Override
    public UpdateEntityResult insertOrReplaceEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException {
        return putOrMergeEntityCore(table, entity, "PUT", false/*includeEtag*/, options);
    }

    @Override
    public UpdateEntityResult insertOrMergeEntity(String table, Entity entity) throws ServiceException {
        return insertOrMergeEntity(table, entity, new TableServiceOptions());
    }

    @Override
    public UpdateEntityResult insertOrMergeEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException {
        return putOrMergeEntityCore(table, entity, "MERGE", false/*includeEtag*/, options);
    }

    private UpdateEntityResult putOrMergeEntityCore(String table, Entity entity, String verb, boolean includeEtag,
            TableServiceOptions options) throws ServiceException {
        if (table == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(
                getEntityPath(table, entity.getPartitionKey(), entity.getRowKey()));

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);
        if (includeEtag) {
            builder = addIfMatchHeader(builder, entity.getEtag());
        }
        if (verb == "MERGE") {
            builder = builder.header("X-HTTP-Method", "MERGE");
            verb = "POST";
        }

        builder = builder.entity(atomReaderWriter.generateEntityEntry(entity), "application/atom+xml");

        ClientResponse response = builder.method(verb, ClientResponse.class);
        ThrowIfError(response);

        UpdateEntityResult result = new UpdateEntityResult();
        result.setEtag(response.getHeaders().getFirst("ETag"));

        return result;
    }

    @Override
    public void deleteEntity(String table, String partitionKey, String rowKey) throws ServiceException {
        deleteEntity(table, partitionKey, rowKey, new DeleteEntityOptions());
    }

    @Override
    public void deleteEntity(String table, String partitionKey, String rowKey, DeleteEntityOptions options)
            throws ServiceException {
        if (table == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(getEntityPath(table, partitionKey, rowKey));

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);
        builder = addIfMatchHeader(builder, options.getEtag());

        ClientResponse response = builder.delete(ClientResponse.class);
        ThrowIfError(response);
    }

    @Override
    public GetEntityResult getEntity(String table, String partitionKey, String rowKey) throws ServiceException {
        return getEntity(table, partitionKey, rowKey, new TableServiceOptions());
    }

    @Override
    public GetEntityResult getEntity(String table, String partitionKey, String rowKey, TableServiceOptions options)
            throws ServiceException {
        if (table == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(getEntityPath(table, partitionKey, rowKey));

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetEntityResult result = new GetEntityResult();
        result.setEntity(atomReaderWriter.parseEntityEntry(response.getEntityInputStream()));

        return result;
    }

    @Override
    public QueryEntitiesResult queryEntities(String table) throws ServiceException {
        return queryEntities(table, new QueryEntitiesOptions());
    }

    @Override
    public QueryEntitiesResult queryEntities(String table, QueryEntitiesOptions options) throws ServiceException {
        if (table == null)
            throw new NullPointerException();

        if (options == null)
            options = new QueryEntitiesOptions();

        WebResource webResource = getResource(options).path(table);
        webResource = addOptionalQueryEntitiesOptions(webResource, options);
        webResource = addOptionalQueryParam(webResource, "NextPartitionKey",
                encodeODataURIValue(options.getNextPartitionKey()));
        webResource = addOptionalQueryParam(webResource, "NextRowKey", encodeODataURIValue(options.getNextRowKey()));

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        QueryEntitiesResult result = new QueryEntitiesResult();
        result.setNextPartitionKey(response.getHeaders().getFirst("x-ms-continuation-NextPartitionKey"));
        result.setNextRowKey(response.getHeaders().getFirst("x-ms-continuation-NextRowKey"));
        result.setEntities(atomReaderWriter.parseEntityEntries(response.getEntityInputStream()));

        return result;
    }

    @Override
    public BatchResult batch(BatchOperations operations) throws ServiceException {
        return batch(operations, new TableServiceOptions());
    }

    @Override
    public BatchResult batch(BatchOperations operations, TableServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("$batch");

        WebResource.Builder builder = webResource.getRequestBuilder();
        builder = addTableRequestHeaders(builder);

        MimeMultipart entity = createBatchRequestBody(operations);
        builder = builder.type(entity.getContentType());

        ClientResponse response = builder.post(ClientResponse.class, entity);
        ThrowIfError(response);

        BatchResult result = new BatchResult();

        try {
            result.setEntries(parseBatchResponse(response, operations));
        }
        catch (IOException e) {
            throw new ServiceException(e);
        }

        return result;
    }

    private MimeMultipart createBatchRequestBody(BatchOperations operations) {
        List<DataSource> bodyPartContents = new ArrayList<DataSource>();
        int contentId = 1;
        for (Operation operation : operations.getOperations()) {

            DataSource bodyPartContent = null;
            if (operation instanceof InsertEntityOperation) {
                InsertEntityOperation op = (InsertEntityOperation) operation;
                bodyPartContent = createBatchInsertOrUpdateEntityPart(op.getTable(), op.getEntity(), "POST",
                        false/*includeEtag*/, contentId);
                contentId++;
            }
            else if (operation instanceof UpdateEntityOperation) {
                UpdateEntityOperation op = (UpdateEntityOperation) operation;
                bodyPartContent = createBatchInsertOrUpdateEntityPart(op.getTable(), op.getEntity(), "PUT",
                        true/*includeEtag*/, contentId);
                contentId++;
            }
            else if (operation instanceof MergeEntityOperation) {
                MergeEntityOperation op = (MergeEntityOperation) operation;
                bodyPartContent = createBatchInsertOrUpdateEntityPart(op.getTable(), op.getEntity(), "MERGE",
                        true/*includeEtag*/, contentId);
                contentId++;
            }
            else if (operation instanceof InsertOrReplaceEntityOperation) {
                InsertOrReplaceEntityOperation op = (InsertOrReplaceEntityOperation) operation;
                bodyPartContent = createBatchInsertOrUpdateEntityPart(op.getTable(), op.getEntity(), "PUT",
                        false/*includeEtag*/, contentId);
                contentId++;
            }
            else if (operation instanceof InsertOrMergeEntityOperation) {
                InsertOrMergeEntityOperation op = (InsertOrMergeEntityOperation) operation;
                bodyPartContent = createBatchInsertOrUpdateEntityPart(op.getTable(), op.getEntity(), "MERGE",
                        false/*includeEtag*/, contentId);
                contentId++;
            }
            else if (operation instanceof DeleteEntityOperation) {
                DeleteEntityOperation op = (DeleteEntityOperation) operation;
                bodyPartContent = createBatchDeleteEntityPart(op.getTable(), op.getPartitionKey(), op.getRowKey(),
                        op.getEtag(), contentId);
                contentId++;
            }

            if (bodyPartContent != null) {
                bodyPartContents.add(bodyPartContent);
            }
        }

        return mimeReaderWriter.getMimeMultipart(bodyPartContents);
    }

    private DataSource createBatchInsertOrUpdateEntityPart(String table, Entity entity, String verb,
            boolean includeEtag, int contentId) {

        URI path;
        if ("POST".equals(verb)) {
            path = channel.resource(url).path(table).getURI();
        }
        else {
            path = channel.resource(url).path(getEntityPath(table, entity.getPartitionKey(), entity.getRowKey()))
                    .getURI();
        }

        //
        // Stream content into byte[] so that we have the length
        //
        InputStream stream = atomReaderWriter.generateEntityEntry(entity);
        byte[] bytes = inputStreamToByteArray(stream);

        //
        // Create body of MIME part as the HTTP request
        //
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-ID", Integer.toString(contentId));
        headers.addHeader("Content-Type", "application/atom+xml;type=entry");
        headers.addHeader("Content-Length", Integer.toString(bytes.length));
        if (includeEtag) {
            headers.addHeader("If-Match", entity.getEtag());
        }

        //TODO: Review code to make sure encoding is correct 
        ByteArrayOutputStream httpRequest = new ByteArrayOutputStream();
        httpReaderWriter.appendMethod(httpRequest, verb, path);
        httpReaderWriter.appendHeaders(httpRequest, headers);
        httpReaderWriter.appendEntity(httpRequest, new ByteArrayInputStream(bytes));

        DataSource bodyPartContent = new InputStreamDataSource(new ByteArrayInputStream(httpRequest.toByteArray()),
                "application/http");
        return bodyPartContent;
    }

    private DataSource createBatchDeleteEntityPart(String table, String partitionKey, String rowKey, String etag,
            int contentId) {

        URI path = channel.resource(url).path(getEntityPath(table, partitionKey, rowKey)).getURI();

        //
        // Create body of MIME part as the HTTP request
        //
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-ID", Integer.toString(contentId));
        headers.addHeader("If-Match", etag == null ? "*" : etag);

        //TODO: Review code to make sure encoding is correct 
        ByteArrayOutputStream httpRequest = new ByteArrayOutputStream();
        httpReaderWriter.appendMethod(httpRequest, "DELETE", path);
        httpReaderWriter.appendHeaders(httpRequest, headers);
        httpReaderWriter.appendEntity(httpRequest, new ByteArrayInputStream(new byte[0]));

        DataSource bodyPartContent = new InputStreamDataSource(new ByteArrayInputStream(httpRequest.toByteArray()),
                "application/http");
        return bodyPartContent;
    }

    private List<Entry> parseBatchResponse(ClientResponse response, BatchOperations operations) throws IOException {
        // Default stream cannot be reset, but it is needed by multiple parts of this method.
        // Replace the default response stream with one that can be read multiple times.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getEntityInputStream();
        ReaderWriter.writeTo(inputStream, byteArrayOutputStream);
        response.setEntityInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        List<DataSource> parts = mimeReaderWriter.parseParts(response.getEntityInputStream(), response.getHeaders()
                .getFirst("Content-Type"));

        if (parts.size() == 0 || parts.size() > operations.getOperations().size()) {
            throw new UniformInterfaceException(String.format(
                    "Batch response from server does not contain the correct amount "
                            + "of parts (expecting %d, received %d instead)", parts.size(), operations.getOperations()
                            .size()), response);
        }

        Entry[] entries = new Entry[operations.getOperations().size()];
        for (int i = 0; i < parts.size(); i++) {
            DataSource ds = parts.get(i);
            Operation operation = operations.getOperations().get(i);

            StatusLine status = httpReaderWriter.parseStatusLine(ds);
            InternetHeaders headers = httpReaderWriter.parseHeaders(ds);
            InputStream content = httpReaderWriter.parseEntity(ds);
            ByteArrayOutputStream contentByteArrayOutputStream = new ByteArrayOutputStream();
            ReaderWriter.writeTo(content, contentByteArrayOutputStream);
            content = new ByteArrayInputStream(contentByteArrayOutputStream.toByteArray());

            if (status.getStatus() >= 400) {
                // Create dummy client response with status, headers and content
                InBoundHeaders inBoundHeaders = new InBoundHeaders();

                @SuppressWarnings("unchecked")
                Enumeration<Header> e = headers.getAllHeaders();
                while (e.hasMoreElements()) {
                    Header header = e.nextElement();
                    inBoundHeaders.putSingle(header.getName(), header.getValue());
                }

                ClientResponse dummyResponse = new ClientResponse(status.getStatus(), inBoundHeaders, content, null);

                // Wrap into a ServiceException
                UniformInterfaceException exception = new UniformInterfaceException(dummyResponse);
                ServiceException serviceException = new ServiceException(exception);
                serviceException = ServiceExceptionFactory.process("table", serviceException);
                Error error = new Error().setError(serviceException);

                // Parse the message to find which operation caused this error.
                try {
                    XMLInputFactory xmlStreamFactory = XMLInputFactory.newFactory();
                    content.reset();
                    XMLStreamReader xmlStreamReader = xmlStreamFactory.createXMLStreamReader(content);

                    while (xmlStreamReader.hasNext()) {
                        xmlStreamReader.next();
                        if (xmlStreamReader.isStartElement() && "message".equals(xmlStreamReader.getLocalName())) {
                            xmlStreamReader.next();
                            // Process "message" elements only
                            String message = xmlStreamReader.getText();
                            int colonIndex = message.indexOf(':');
                            String errorOpId = message.substring(0, colonIndex);
                            int opId = Integer.parseInt(errorOpId);
                            entries[opId] = error;
                            break;
                        }
                    }
                    xmlStreamReader.close();
                }
                catch (XMLStreamException e1) {
                    throw new UniformInterfaceException(
                            "Batch response from server does not contain XML in the expected format", response);
                }
            }
            else if (operation instanceof InsertEntityOperation) {
                InsertEntity opResult = new InsertEntity().setEntity(atomReaderWriter.parseEntityEntry(content));
                entries[i] = opResult;
            }
            else if ((operation instanceof UpdateEntityOperation) || (operation instanceof MergeEntityOperation)
                    || (operation instanceof InsertOrReplaceEntityOperation)
                    || (operation instanceof InsertOrMergeEntityOperation)) {
                UpdateEntity opResult = new UpdateEntity().setEtag(headers.getHeader("ETag", null));
                entries[i] = opResult;
            }
            else if (operation instanceof DeleteEntityOperation) {
                DeleteEntity opResult = new DeleteEntity();
                entries[i] = opResult;
            }
        }

        List<Entry> result = new ArrayList<Entry>();
        for (int i = 0; i < entries.length; i++) {
            result.add(entries[i]);
        }

        return result;
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            try {
                while (true) {
                    int n = inputStream.read(buffer);
                    if (n == -1)
                        break;
                    outputStream.write(buffer, 0, n);
                }
            }
            finally {
                inputStream.close();
            }
            return outputStream.toByteArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
