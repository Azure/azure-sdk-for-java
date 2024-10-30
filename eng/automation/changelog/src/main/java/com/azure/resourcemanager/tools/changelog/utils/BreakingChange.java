package com.azure.resourcemanager.tools.changelog.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Breaking change information for class.
 */
public class BreakingChange {

    private final String className;
    private Type type;
    private final Set<String> methodChanges = new LinkedHashSet<>();

    private BreakingChange(String className) {
        this.className = className;
        setClassLevelChangeType(Type.NOT_CHANGED);
    }

    public static BreakingChange fromClass(String className) {
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

    public String getForChangelog() {
        if (type == Type.NOT_CHANGED) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("#### `%s` was %s", className, type.getDisplayName()));
        for (String methodChange : methodChanges) {
            builder
                .append("* ")
                .append(methodChange)
                .append("\n");
        }
        return builder.append("\n").toString();
    }

    public Collection<String> getItems() {
        if (type == Type.NOT_CHANGED) {
            return Collections.emptyList();
        } else if (methodChanges.isEmpty()) {
            return Collections.singleton(String.format("Class `%s` was %s.", className, type.getDisplayName()));
        } else {
            return methodChanges.stream().map(methodChange -> String.format("Method %s in class `%s`.", methodChange, className)).collect(Collectors.toList());
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
