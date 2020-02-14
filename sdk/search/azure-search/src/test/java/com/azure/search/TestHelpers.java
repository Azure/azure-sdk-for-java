package com.azure.search;

import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;

import java.util.Objects;

/**
 * This class contains helper methods for running Azure Search tests.
 */
public final class TestHelpers {
    public static boolean areDataSourcesEqual(DataSource actual, DataSource expected) {
        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getType(), expected.getType())
            && Objects.equals(actual.getCredentials().getConnectionString(),
                expected.getCredentials().getConnectionString())
            && areDataContainersEqual(actual.getContainer(), expected.getContainer())
            && Objects.equals(actual.getDescription(), expected.getDescription())
            && areChangeDetectionPoliciesEqual(actual.getDataChangeDetectionPolicy(),
                expected.getDataChangeDetectionPolicy())
            && areDeletionDetectionPoliciesEqual(actual.getDataDeletionDetectionPolicy(),
                expected.getDataDeletionDetectionPolicy());
    }

    private static boolean areDataContainersEqual(DataContainer actual, DataContainer expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getQuery(), expected.getQuery());
    }

    private static boolean areChangeDetectionPoliciesEqual(DataChangeDetectionPolicy actual,
        DataChangeDetectionPolicy expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getClass(), expected.getClass());
    }

    private static boolean areDeletionDetectionPoliciesEqual(DataDeletionDetectionPolicy actual,
        DataDeletionDetectionPolicy expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getClass(), expected.getClass());
    }
}
