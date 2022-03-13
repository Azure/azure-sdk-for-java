// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog;

import com.azure.resourcemanager.tools.changelog.changelog.ChangeLog;
import com.azure.resourcemanager.tools.changelog.utils.ClassName;
import com.azure.resourcemanager.tools.changelog.utils.Namespaces;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.JApiClass;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private final static String BREAKING_CHANGE_TITLE = "### Breaking Changes\n\n";
    private final static String NEW_FEATURE_TITLE = "### Features Added\n\n";

    public static void main(String[] args) throws Exception {
        String oldJar = System.getProperty("OLD_JAR");
        if (oldJar == null || oldJar.isEmpty()) {
            System.err.println("Cannot found OLD_JAR property");
            System.exit(1);
        }
        String newJar = System.getProperty("NEW_JAR");
        if (newJar == null || newJar.isEmpty()) {
            System.err.println("Cannot found NEW_JAR property");
            System.exit(1);
        }

        JApiCmpArchive oldArchive = new JApiCmpArchive(new File(oldJar), "1.0.0");
        JApiCmpArchive newArchive = new JApiCmpArchive(new File(newJar), "1.0.1");

        Options options = Options.newDefault();
        options.setIgnoreMissingClasses(true);

        JarArchiveComparator jarArchiveComparator = new JarArchiveComparator(JarArchiveComparatorOptions.of(options));
        List<JApiClass> classes = jarArchiveComparator.compare(oldArchive, newArchive);

        Namespaces namespaces = new Namespaces(classes);

        String filter = System.getProperty("PACKAGE_IGNORE");
        if (filter != null && !filter.isEmpty()) {
            List<Pattern> filters = Stream.of(filter.split(";")).map(Pattern::compile).collect(Collectors.toList());
            classes = classes.stream().filter(x -> {
                for (Pattern pattern : filters) {
                    if (pattern.matcher(ClassName.namespace(x)).matches()) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        List<ChangeLog> changeLogs = ChangeLog.fromClasses(classes);
        StringBuilder breakingChange = new StringBuilder();
        StringBuilder newFeature = new StringBuilder();
        changeLogs.forEach(x -> {
            if (x.isClassLevelChanged()) {
                breakingChange.append(x.getBreakingChange());
            }
        });
        changeLogs.forEach(x -> {
            if (!x.isClassLevelChanged()) {
                breakingChange.append(x.getBreakingChange());
            }
        });
        changeLogs.forEach(x -> {
            if (x.isClassLevelChanged()) {
                newFeature.append(x.getNewFeature());
            }
        });
        changeLogs.forEach(x -> {
            if (!x.isClassLevelChanged()) {
                newFeature.append(x.getNewFeature());
            }
        });

        String changelog = (breakingChange.length() > 0 ? BREAKING_CHANGE_TITLE + breakingChange.toString().replace(namespaces.getBase() + ".", "") : "") +
            (newFeature.length() > 0 ? NEW_FEATURE_TITLE + newFeature.toString().replace(namespaces.getBase() + ".", "") : "");

        JSONObject json = new JSONObject();
        json.put("breaking", breakingChange.length() > 0);
        json.put("changelog", changelog);

        System.out.println(json.toString());
    }
}
