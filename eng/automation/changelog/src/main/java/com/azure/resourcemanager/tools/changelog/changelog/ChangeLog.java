// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.changelog;

import com.azure.resourcemanager.tools.changelog.utils.AllMethods;
import com.azure.resourcemanager.tools.changelog.utils.BreakingChange;
import com.azure.resourcemanager.tools.changelog.utils.ClassName;
import com.azure.resourcemanager.tools.changelog.utils.MethodName;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiMethod;
import javassist.bytecode.AccessFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeLog {
    private AllMethods allMethods;
    protected List<String> newFeature;
    protected BreakingChange breakingChange;

    ChangeLog(AllMethods allMethods) {
        this.allMethods = allMethods;
        this.newFeature = new ArrayList<>();
        this.breakingChange = BreakingChange.onJavaClass(getJApiClass().getFullyQualifiedName());
        calcChangeLog();
    }

    protected ChangeLog() {
        this.newFeature = new ArrayList<>();
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
        return this.breakingChange.getForChangelog();
    }

    public boolean isClassLevelChanged() {
        return getJApiClass().getChangeStatus() == JApiChangeStatus.NEW || getJApiClass().getChangeStatus() == JApiChangeStatus.REMOVED;
    }

    protected void calcChangeLog() {
        calcChangeLogForClass();
        // When a base method overridden by child class got changed, two identical change logs will appear for this child class
        deduplicateChangeLog();
    }

    private void deduplicateChangeLog() {
        deduplicate(this.newFeature);
    }

    private void deduplicate(List<String> changeList) {
        Set<String> changeSet = new HashSet<>();
        Iterator<String> iterator = changeList.iterator();
        while (iterator.hasNext()) {
            String change = iterator.next();
            if (changeSet.contains(change)) {
                iterator.remove();
            } else {
                changeSet.add(change);
            }
        }
    }

    private void calcChangeLogForClass() {
        switch (getJApiClass().getChangeStatus()) {
            case NEW: newFeature.add(String.format("* `%s` was added", getJApiClass().getFullyQualifiedName())); break;
            case REMOVED: breakingChange.setClassLevelChangeType(BreakingChange.Type.REMOVED); break;
            default:
                boolean checkReturnType = !ClassName.name(getJApiClass()).equals("Definition");
                allMethods.getConstructors().forEach(constructor -> this.calcChangelogForConstructor(constructor));
                allMethods.getMethods().forEach(method -> this.calcChangelogForMethod(method, checkReturnType));
                break;
        }
    }

    private void addClassTitle(List<String> list) {
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
                breakingChange.addMethodLevelChange(String.format("`%s` was removed", MethodName.name(method.getOldMethod().get())));
                break;
            case MODIFIED:
                if (!checkReturnType){
                    if (!method.getOldMethod().get().getLongName().equals(method.getNewMethod().get().getLongName())) {
                        breakingChange.addMethodLevelChange(String.format("`%s` -> `%s`", MethodName.name(method.getOldMethod().get()), MethodName.name(method.getNewMethod().get())));
                    }
                } else {
                    breakingChange.addMethodLevelChange(String.format("`%s %s` -> `%s %s`", method.getReturnType().getOldReturnType(), MethodName.name(method.getOldMethod().get()), method.getReturnType().getNewReturnType(), MethodName.name(method.getNewMethod().get())));
                }
                break;
        }
    }

    private void calcChangelogForConstructor(JApiConstructor constructor) {
        switch (constructor.getChangeStatus()) {
            case NEW:
                addClassTitle(newFeature);
                newFeature.add(String.format("* `%s` was added", MethodName.name(constructor.getNewConstructor().get())));
                break;
            case REMOVED:
                breakingChange.addMethodLevelChange(String.format("`%s` was removed", MethodName.name(constructor.getOldConstructor().get())));
                break;
            case MODIFIED:
                if ((constructor.getOldConstructor().get().getModifiers() & AccessFlag.PUBLIC) == AccessFlag.PUBLIC
                        && (constructor.getNewConstructor().get().getModifiers() & AccessFlag.PRIVATE) == AccessFlag.PRIVATE) {
                    breakingChange.addMethodLevelChange(String.format("`%s` was changed to private access", MethodName.name(constructor.getOldConstructor().get())));
                }
                // breakingChange.addMethodLevelChange(String.format("`%s` -> `%s`", MethodName.name(constructor.getOldConstructor().get()), MethodName.name(constructor.getNewConstructor().get())));
                break;
        }
    }

    public Collection<String> getBreakingChangeItems() {
        return breakingChange.getItems();
    }
}
