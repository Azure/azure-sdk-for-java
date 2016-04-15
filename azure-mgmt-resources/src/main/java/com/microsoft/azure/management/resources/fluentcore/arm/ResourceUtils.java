package com.microsoft.azure.management.resources.fluentcore.arm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceUtils {
    public static String groupFromResourceId(String id) {
        if (id == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("resourcegroups\\/[-\\w\\._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("\\/")[1];
        } else {
            return null;
        }
    }

    public static String nameFromResourceId(String id) {
        return null;
    }
}
