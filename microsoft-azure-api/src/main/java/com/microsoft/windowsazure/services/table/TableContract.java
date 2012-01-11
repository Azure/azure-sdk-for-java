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
package com.microsoft.windowsazure.services.table;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.table.models.DeleteEntityOptions;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.ListTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableServiceOptions;
import com.microsoft.windowsazure.services.table.models.UpdateEntityResult;

public interface TableContract extends FilterableService<TableContract> {
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    GetServicePropertiesResult getServiceProperties(TableServiceOptions options) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties, TableServiceOptions options) throws ServiceException;

    void createTable(String table) throws ServiceException;

    void createTable(String table, TableServiceOptions options) throws ServiceException;

    void deleteTable(String table) throws ServiceException;

    void deleteTable(String table, TableServiceOptions options) throws ServiceException;

    GetTableResult getTable(String table) throws ServiceException;

    GetTableResult getTable(String table, TableServiceOptions options) throws ServiceException;

    QueryTablesResult listTables() throws ServiceException;

    QueryTablesResult listTables(ListTablesOptions options) throws ServiceException;

    QueryTablesResult queryTables() throws ServiceException;

    QueryTablesResult queryTables(QueryTablesOptions options) throws ServiceException;

    InsertEntityResult insertEntity(String table, Entity entity) throws ServiceException;

    InsertEntityResult insertEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    UpdateEntityResult updateEntity(String table, Entity entity) throws ServiceException;

    UpdateEntityResult updateEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    UpdateEntityResult mergeEntity(String table, Entity entity) throws ServiceException;

    UpdateEntityResult mergeEntity(String table, Entity entity, TableServiceOptions options) throws ServiceException;

    UpdateEntityResult insertOrReplaceEntity(String table, Entity entity) throws ServiceException;

    UpdateEntityResult insertOrReplaceEntity(String table, Entity entity, TableServiceOptions options)
            throws ServiceException;

    void deleteEntity(String table, String partitionKey, String rowKey) throws ServiceException;

    void deleteEntity(String table, String partitionKey, String rowKey, DeleteEntityOptions options)
            throws ServiceException;
}
