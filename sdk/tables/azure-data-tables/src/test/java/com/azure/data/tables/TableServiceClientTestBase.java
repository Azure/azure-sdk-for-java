// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.test.TestBase;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

public abstract class TableServiceClientTestBase extends TestBase {
    @Test
    public abstract void serviceCreateTable();

    @Test
    public abstract void serviceCreateTableWithResponse();

    @Test
    public abstract void serviceCreateTableFailsIfExists();

    @Test
    public abstract void serviceCreateTableIfNotExists();

    @Test
    public abstract void serviceCreateTableIfNotExistsSucceedsIfExists();

    @Test
    public abstract void serviceCreateTableIfNotExistsWithResponse();

    @Test
    public abstract void serviceCreateTableIfNotExistsWithResponseSucceedsIfExists();

    @Test
    public abstract void serviceDeleteTable();

    @Test
    public abstract void serviceDeleteNonExistingTable();

    @Test
    public abstract void serviceDeleteTableWithResponse();

    @Test
    public abstract void serviceDeleteNonExistingTableWithResponse();

    @Test
    public abstract void serviceListTables();

    @Test
    public abstract void serviceListTablesWithFilter();

    @Test
    public abstract void serviceListTablesWithTop();

    @Test
    public abstract void serviceGetTableClient();

    @Test
    public abstract void generateAccountSasTokenWithMinimumParameters();

    @Test
    public abstract void generateAccountSasTokenWithAllParameters();

    @Test
    public abstract void canUseSasTokenToCreateValidTableClient();

    @Test
    public abstract void setGetProperties();

    @Test
    public abstract void getStatistics() throws URISyntaxException;
}
