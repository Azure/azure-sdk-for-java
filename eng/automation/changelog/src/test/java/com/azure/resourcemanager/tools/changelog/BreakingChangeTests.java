package com.azure.resourcemanager.tools.changelog;

import com.azure.resourcemanager.tools.changelog.utils.BreakingChange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BreakingChangeTests {
    @Test
    public void testBreakingChange() {
        BreakingChange breakingChange = BreakingChange.fromClass("MyClass");
        breakingChange.setClassLevelChangeType(BreakingChange.Type.REMOVED);
        Assertions.assertEquals("#### `MyClass` was removed\n", breakingChange.getForChangelog());
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
    }
}
