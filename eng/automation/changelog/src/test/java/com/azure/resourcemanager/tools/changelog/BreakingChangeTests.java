package com.azure.resourcemanager.tools.changelog;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.tools.changelog.utils.BreakingChange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class BreakingChangeTests {
    @Test
    public void testBreakingChange() {
        BreakingChange breakingChange = BreakingChange.onJavaClass("MyClass");
        breakingChange.setClassLevelChangeType(BreakingChange.Type.REMOVED);
        Assertions.assertEquals("#### `MyClass` was removed\n\n", breakingChange.getForChangelog());
        Assertions.assertEquals("Class `MyClass` was removed.", breakingChange.getItems().iterator().next());

        String methodLevelContent = "`sku()` was removed";
        String methodLevelContent2 = "`tier()` was removed";
        breakingChange.addMethodLevelChange(methodLevelContent);
        breakingChange.addMethodLevelChange(methodLevelContent2);
        // test deduplicate
        breakingChange.addMethodLevelChange(methodLevelContent2);

        Assertions.assertEquals(2, breakingChange.getItems().size());
        Assertions.assertTrue(breakingChange.getForChangelog().contains("#### `MyClass` was modified"));
        Assertions.assertEquals("Method `sku()` was removed in class `MyClass`.", breakingChange.getItems().iterator().next());

        String stageLevelContent = "Required stage 3 was removed";
        breakingChange.addStageLevelChange(stageLevelContent);

        Assertions.assertEquals(3, breakingChange.getItems().size());
        Assertions.assertEquals("Required stage 3 was removed in class `MyClass`.", breakingChange.getItems().iterator().next());
    }

    @Test
    public void testCompareJars() {
        URL oldJar = BreakingChangeTests.class.getResource("/old.jar");
        URL newJar = BreakingChangeTests.class.getResource("/new.jar");
        System.setProperty("OLD_JAR", oldJar.getFile());
        System.setProperty("NEW_JAR", newJar.getFile());
        JSONObject jsonObject = Main.getChangelog();

        JSONArray breakingChanges = (JSONArray) jsonObject.get("breakingChanges");
        String changelog = (String) jsonObject.get("changelog");
        Assertions.assertFalse(CoreUtils.isNullOrEmpty(changelog));
        Assertions.assertFalse(breakingChanges.isEmpty());
        Assertions.assertTrue(breakingChanges.toList().contains("Required stage 3 was added in class `com.azure.resourcemanager.quota.models.CurrentQuotaLimitBase$DefinitionStages`."));
        Assertions.assertTrue(breakingChanges.toList().contains("Method `withProperties(com.azure.resourcemanager.quota.models.QuotaProperties)` was removed in stage 2 in class `com.azure.resourcemanager.quota.models.CurrentQuotaLimitBase$DefinitionStages`."));
        Assertions.assertTrue(breakingChanges.toList().contains("Method `withProperties(com.azure.resourcemanager.quota.models.QuotaProperties)` was removed in class `com.azure.resourcemanager.quota.models.CurrentQuotaLimitBase$Definition`."));
    }
}
