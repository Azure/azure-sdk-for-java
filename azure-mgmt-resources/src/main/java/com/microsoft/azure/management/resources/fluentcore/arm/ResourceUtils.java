/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceUtils {
    public static String groupFromResourceId(String id) {
        return extractFromResourceId(id, "resourceGroups");
    }

    public static String extractFromResourceId(String id, String identifier) {
        if (id == null || identifier == null) {
            return id;
        }
        Pattern pattern = Pattern.compile(identifier + "/[-\\w\\._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("/")[1];
        } else {
            return null;
        }
    }

    public static String nameFromResourceId(String id) {
        return id.split("/")[8];
    }
}
