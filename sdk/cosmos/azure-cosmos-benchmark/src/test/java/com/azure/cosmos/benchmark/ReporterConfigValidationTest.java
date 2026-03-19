// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReporterConfigValidationTest {

    // ---- CsvReporterConfig ----

    @Test(groups = {"unit"})
    public void csvReporterConfig_validDirectory() {
        CsvReporterConfig config = new CsvReporterConfig("/tmp/output");
        assertThat(config.getReportingDirectory()).isEqualTo("/tmp/output");
    }

    @Test(groups = {"unit"})
    public void csvReporterConfig_nullDirectory_throws() {
        assertThatThrownBy(() -> new CsvReporterConfig(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reportingDirectory");
    }

    @Test(groups = {"unit"})
    public void csvReporterConfig_emptyDirectory_throws() {
        assertThatThrownBy(() -> new CsvReporterConfig(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reportingDirectory");
    }

    // ---- CosmosReporterConfig ----

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_validFields() {
        CosmosReporterConfig config = new CosmosReporterConfig(
            "https://acct.documents.azure.com:443/", "key",
            "db", "container", "variation", "main", "abc123");
        assertThat(config.getServiceEndpoint()).isEqualTo("https://acct.documents.azure.com:443/");
        assertThat(config.getDatabase()).isEqualTo("db");
        assertThat(config.getContainer()).isEqualTo("container");
    }

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_optionalFieldsCanBeNull() {
        CosmosReporterConfig config = new CosmosReporterConfig(
            "https://acct.documents.azure.com:443/", "key",
            "db", "container", null, null, null);
        assertThat(config.getTestVariationName()).isEmpty();
        assertThat(config.getBranchName()).isEmpty();
        assertThat(config.getCommitId()).isEmpty();
    }

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_nullEndpoint_throws() {
        assertThatThrownBy(() -> new CosmosReporterConfig(
            null, "key", "db", "container", null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("serviceEndpoint");
    }

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_nullMasterKey_throws() {
        assertThatThrownBy(() -> new CosmosReporterConfig(
            "https://acct.documents.azure.com:443/", null, "db", "container", null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("masterKey");
    }

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_nullDatabase_throws() {
        assertThatThrownBy(() -> new CosmosReporterConfig(
            "https://acct.documents.azure.com:443/", "key", null, "container", null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("database");
    }

    @Test(groups = {"unit"})
    public void cosmosReporterConfig_nullContainer_throws() {
        assertThatThrownBy(() -> new CosmosReporterConfig(
            "https://acct.documents.azure.com:443/", "key", "db", null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("container");
    }
}
