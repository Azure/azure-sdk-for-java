package com.azure.iot.modelsrepository.implementation;

import java.util.regex.Pattern;

public class DtmiConventions {

    private static final Pattern validDtmiPattern = Pattern.compile("^dtmi:[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?(?::[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?)*;[1-9][0-9]{0,8}$");

    public static boolean isValidDtmi(String dtmi) {
        if (dtmi == null || dtmi.isEmpty()) {
            return false;
        }

        return validDtmiPattern.matcher(dtmi).find();
    }

    public static String dtmiToPath(String dtmi) {
        if (!isValidDtmi(dtmi)) {
            return null;
        }

        return dtmi
            .toLowerCase()
            .replaceAll(":", "/")
            .replaceAll(";", "-")
            + ".json";
    }

    public static String dtmiToQualifiedPath(String dtmi, String basePath, boolean fromExpanded) {
        String dtmiPath = dtmiToPath(dtmi);

        // TODO: azabbasi : exception message
        if (dtmiPath == null) {
            throw new IllegalArgumentException();
        }

        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        String fullyQualifiedPath = basePath.concat(dtmiPath);

        if (fromExpanded) {
            fullyQualifiedPath = fullyQualifiedPath.replaceAll(".json", ".expanded.json");
        }

        return fullyQualifiedPath;
    }
}
