// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.changelog;

import com.azure.resourcemanager.tools.changelog.utils.AllMethods;
import com.azure.resourcemanager.tools.changelog.utils.ClassName;
import com.azure.resourcemanager.tools.changelog.utils.MethodName;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeLog {
    private AllMethods allMethods;
    protected List<String> newFeature;
    protected List<String> breakingChange;

    ChangeLog() {
        this.newFeature = new ArrayList<>();
        this.breakingChange = new ArrayList<>();
    }

    ChangeLog(AllMethods allMethods) {
        this.allMethods = allMethods;
        this.newFeature = new ArrayList<>();
        this.breakingChange = new ArrayList<>();
        calcChangeLog();
    }

    public static List<ChangeLog> fromClasses(List<JApiClass> classes) {
        Map<String, JApiClass> classMap = classes.stream().collect(Collectors.toMap(JApiClass::getFullyQualifiedName, x -> x));
        Map<String, AllMethods> allMethods = new HashMap<>();
        AllMethods.fromClasses(classMap, allMethods);
        Map<String, Map<String, AllMethods>> stages = new HashMap<>();
        List<ChangeLog> changeLogForNonStage = allMethods.entrySet().stream().map(entry -> {
            String namespace = ClassName.namespace(entry.getKey());
            String parentClass = ClassName.parentName(entry.getKey());
            String parentName = ClassName.name(parentClass);
            if (parentName.equals("DefinitionStages")) {
                stages.computeIfAbsent(namespace + "." + parentClass, key -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            } else if (!parentName.equals("UpdateStages")) {
                return new ChangeLog(entry.getValue());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());


        Stream<ChangeLog> changeLogForStage = stages.entrySet().stream()
            .filter(entry -> {
                JApiChangeStatus changeStatus = classMap.get(entry.getKey()).getChangeStatus();
                if (changeStatus == JApiChangeStatus.NEW || changeStatus == JApiChangeStatus.REMOVED) {
                    return false; // Filter totally new / empty stages.
                }
                return true;
            })
            .map(entry -> new DefinitionStageChangeLog(entry.getValue(), entry.getKey()));
        return Stream.concat(changeLogForStage, changeLogForNonStage.stream()).collect(Collectors.toList());
    }

    public JApiClass getJApiClass() {
        return this.allMethods.getJApiClass();
    }

    public String getNewFeature() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.newFeature.size(); ++i) {
            builder.append(this.newFeature.get(i)).append("\n");
            if (i + 1 == this.newFeature.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public String getBreakingChange() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.breakingChange.size(); ++i) {
            builder.append(this.breakingChange.get(i)).append("\n");
            if (i + 1 == this.breakingChange.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public boolean isClassLevelChanged() {
        return getJApiClass().getChangeStatus() == JApiChangeStatus.NEW || getJApiClass().getChangeStatus() == JApiChangeStatus.REMOVED;
    }

    protected void calcChangeLog() {
        calcChangeLogForClass();
    }

    private void calcChangeLogForClass() {
        switch (getJApiClass().getChangeStatus()) {
            case NEW: newFeature.add(String.format("* `%s` was added", getJApiClass().getFullyQualifiedName())); break;
            case REMOVED: breakingChange.add(String.format("* `%s` was removed", getJApiClass().getFullyQualifiedName())); break;
            default:
                boolean checkReturnType = !ClassName.name(getJApiClass()).equals("Definition");
                allMethods.getMethods().forEach(method -> this.calcChangelogForMethod(method, checkReturnType));
                break;
        }
    }

    protected void addClassTitle(List<String> list) {
        if (list.isEmpty()) {
            list.add(String.format("#### `%s` was modified", getJApiClass().getFullyQualifiedName()));
            list.add("");
        }
    }

    private void calcChangelogForMethod(JApiMethod method, boolean checkReturnType) {
        switch (method.getChangeStatus()) {
            case NEW:
                addClassTitle(newFeature);
                newFeature.add(String.format("* `%s` was added", MethodName.name(method.getNewMethod().get())));
                break;
            case REMOVED:
                addClassTitle(breakingChange);
                breakingChange.add(String.format("* `%s` was removed", MethodName.name(method.getOldMethod().get())));
                break;
            case MODIFIED:
                if (!checkReturnType){
                    if (!method.getOldMethod().get().getLongName().equals(method.getNewMethod().get().getLongName())) {
                        addClassTitle(breakingChange);
                        breakingChange.add(String.format("* `%s` -> `%s`", MethodName.name(method.getOldMethod().get()), MethodName.name(method.getNewMethod().get())));
                    }
                } else {
                    addClassTitle(breakingChange);
                    breakingChange.add(String.format("* `%s %s` -> `%s %s`", method.getReturnType().getOldReturnType(), MethodName.name(method.getOldMethod().get()), method.getReturnType().getNewReturnType(), MethodName.name(method.getNewMethod().get())));
                }
                break;
        }
    }
}
