// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql;

import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServers;
import com.azure.resourcemanager.sql.models.SqlSyncFullSchemaProperty;
import com.azure.resourcemanager.sql.models.SqlSyncGroup;
import com.azure.resourcemanager.sql.models.SqlSyncGroupLogProperty;
import com.azure.resourcemanager.sql.models.SqlSyncGroupOperations;
import com.azure.resourcemanager.sql.models.SqlSyncMember;
import com.azure.resourcemanager.sql.models.SqlSyncMemberOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlDataSyncDeprecationTests {
    @Test
    public void handwrittenConvenienceApisAreDeprecated() throws NoSuchMethodException {
        assertDeprecated(SqlSyncFullSchemaProperty.class);
        assertDeprecated(SqlSyncGroup.class);
        assertDeprecated(SqlSyncGroupLogProperty.class);
        assertDeprecated(SqlSyncGroupOperations.class);
        assertDeprecated(SqlSyncGroupOperations.SqlSyncGroupActionsDefinition.class);
        assertDeprecated(SqlSyncMember.class);
        assertDeprecated(SqlSyncMemberOperations.class);
        assertDeprecated(SqlSyncMemberOperations.SqlSyncMemberActionsDefinition.class);
        assertDeprecated(SqlServers.class.getMethod("syncGroups"));
        assertDeprecated(SqlServers.class.getMethod("syncMembers"));
        assertDeprecated(SqlDatabase.class.getMethod("syncGroups"));
        assertDeprecated(SqlSyncGroup.class.getMethod("syncMembers"));
    }

    private static void assertDeprecated(java.lang.reflect.AnnotatedElement element) {
        Assertions.assertTrue(element.isAnnotationPresent(Deprecated.class));
    }
}
