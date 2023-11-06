// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JDBC self-diagnostics features.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DataSource.class})
public class JdbcSelfDiagConfig {
    private static final class JdbcSelfDiagnostics implements CommandLineRunner {

        private static final String OPEN_TELEMETRY_DATA_SOURCE_CLASS_NAME = "io.opentelemetry.instrumentation.jdbc.datasource.OpenTelemetryDataSource";
        private final ObjectProvider<List<DataSource>> dataSources;
        private final Logger selfDiagnosticsLogger;

        private JdbcSelfDiagnostics(ObjectProvider<List<DataSource>> dataSources, Logger selfDiagnosticsLogger) {
            this.dataSources = dataSources;
            this.selfDiagnosticsLogger = selfDiagnosticsLogger;
        }


        /**
         * To execute the JDBC self-diagnostics.
         * @param args Incoming main method arguments
         */
        @Override
        public void run(String... args) {
            try {
                applyJdbcSelfDiagnostics();
            } catch (Exception e) {
                selfDiagnosticsLogger.warn("An unexpected issue has happened during JDBC self-diagnostics.", e);
            }
        }

        private void applyJdbcSelfDiagnostics() {

            if (selfDiagnosticsLogger.isDebugEnabled()) {

                try {
                    Class.forName(OPEN_TELEMETRY_DATA_SOURCE_CLASS_NAME);
                } catch (ClassNotFoundException e) {
                    selfDiagnosticsLogger.debug("You need the io.opentelemetry.instrumentation:opentelemetry-jdbc dependency for JDBC instrumentation.");
                    return;
                }

                Predicate<DataSource> otelDatasourcePredicate = dataSource -> !dataSource.getClass().getName().equals(OPEN_TELEMETRY_DATA_SOURCE_CLASS_NAME);
                List<DataSource> notOtelDatasources = dataSources.getIfAvailable(Collections::emptyList).stream().filter(otelDatasourcePredicate).collect(Collectors.toList());
                if (!notOtelDatasources.isEmpty()) {
                    selfDiagnosticsLogger.debug("Data source configuration type - Not OpenTelemetry data sources: " + notOtelDatasources);
                }

                Enumeration<Driver> drivers = DriverManager.getDrivers();
                Collection<String> driverClassNames = findDriverClassNames(drivers);
                String driverClassNamesAsString = String.join(", ", driverClassNames);
                selfDiagnosticsLogger.debug("JDBC driver configuration type - Available JDBC drivers: " + driverClassNamesAsString);
            }
        }

    }

    private static Collection<String> findDriverClassNames(Enumeration<Driver> drivers) {
        List<String> driverClassNames = new ArrayList<>();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            driverClassNames.add(driver.getClass().getName());
        }
        return driverClassNames;
    }

    /**
     * A bean execute the JDBC self-diagnostics
     *
     * @param dataSources Potential SQL datasources
     * @param selfDiagnosticsLogger The self-diagnostics logger
     * @return A CommandLineRunner bean to execute the JDBC self-diagnostics
     */
    @Bean
    public CommandLineRunner jdbcSelfDiagnostics(ObjectProvider<List<DataSource>> dataSources, Logger selfDiagnosticsLogger) {
        return new JdbcSelfDiagnostics(dataSources, selfDiagnosticsLogger);
    }
}
