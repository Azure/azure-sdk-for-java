package com.azure.resourcemanager.tools.changelog.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Breaking change information for class.
 */
public class BreakingChange {

    private final String className;
    private Type type;
    private final Set<String> methodChanges = new LinkedHashSet<>();
    private final Set<String> fieldChanges = new LinkedHashSet<>();
    private final Set<String> stageChanges = new LinkedHashSet<>();

    private BreakingChange(String className) {
        this.className = className;
        setClassLevelChangeType(Type.NOT_CHANGED);
    }

    public static BreakingChange onJavaClass(String className) {
        return new BreakingChange(className);
    }

    public void setClassLevelChangeType(Type changeType) {
        Objects.requireNonNull(changeType);
        this.type = changeType;
    }

    public void addMethodLevelChange(String content) {
        setClassLevelChangeType(Type.MODIFIED);
        methodChanges.add(content);
    }

    public void addFieldLevelChange(String content) {
        setClassLevelChangeType(Type.MODIFIED);
        fieldChanges.add(content);
    }

    public void addStageLevelChange(String content) {
        setClassLevelChangeType(Type.MODIFIED);
        stageChanges.add(content);
    }

    public String getForChangelog() {
        if (type == Type.NOT_CHANGED) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("#### `%s` was %s\n\n", className, type.getDisplayName()));
        int count = 0;
        List<String> innerChanges = Stream.concat(Stream.concat(stageChanges.stream(), fieldChanges.stream()), methodChanges.stream()).collect(Collectors.toList());
        for (String methodChange : innerChanges) {
            builder
                .append("* ")
                .append(methodChange)
                .append("\n");
            count++;
            if (count == innerChanges.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Collection<String> getItems() {
        if (type == Type.NOT_CHANGED) {
            return Collections.emptyList();
        } else if (methodChanges.isEmpty() && fieldChanges.isEmpty() && stageChanges.isEmpty()) {
            return Collections.singleton(String.format("Class `%s` was %s.", className, type.getDisplayName()));
        } else {
            return Stream.concat(
                Stream.concat(
                    stageChanges.stream().map(stageChange -> String.format("%s in class `%s`.", stageChange, className)),
                    fieldChanges.stream().map(fieldChange -> String.format("Field %s in class `%s`.", fieldChange, className))
                ),
                methodChanges.stream().map(methodChange -> String.format("Method %s in class `%s`.", methodChange, className))
            ).collect(Collectors.toList());
        }
    }

    public enum Type {
        NOT_CHANGED("not changed"),
        MODIFIED("modified"),
        REMOVED("removed"),
        ;

        private final String displayName;
        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
