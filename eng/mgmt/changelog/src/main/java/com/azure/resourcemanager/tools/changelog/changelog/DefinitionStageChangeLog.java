// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.changelog;

import com.azure.resourcemanager.tools.changelog.utils.AllMethods;
import com.azure.resourcemanager.tools.changelog.utils.ClassName;
import com.azure.resourcemanager.tools.changelog.utils.MethodName;
import japicmp.model.JApiMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefinitionStageChangeLog extends ChangeLog {
    private String parentClass;
    private List<Set<JApiMethod>> oldMethodStages;
    private List<Set<JApiMethod>> newMethodStages;

    DefinitionStageChangeLog(Map<String, AllMethods> allStages, String parentClass) {
        this.parentClass = parentClass;
        oldMethodStages = new ArrayList<>();
        newMethodStages = new ArrayList<>();
        AllMethods blankStage = allStages.entrySet().stream().filter(x -> ClassName.name(x.getKey()).equals("Blank")).findAny().get().getValue();
        calcMethodStages(blankStage, allStages, oldMethodStages, method -> method.getReturnType().getOldReturnType());
        calcMethodStages(blankStage, allStages, newMethodStages, method -> method.getReturnType().getNewReturnType());
        calcChangeLog();
    }

    /**
     * Use BFS to search all definition stages function graph. Start from BlankStage and end to FinalStage.
     *
     * @param blankStage The start point for BFS, which is the first stage.
     * @param allStages Map contains all stages. Map from Stage name to all methods it has.
     * @param results The result contains list of methods. The index represents which stage the method locates.
     * @param getReturnType The function to map a JApiMethod to its according return type.
     */
    private void calcMethodStages(AllMethods blankStage, Map<String, AllMethods> allStages, List<Set<JApiMethod>> results, Function<JApiMethod, String> getReturnType) {
        results.add(new HashSet<>(blankStage.getMethods()));
        Set<JApiMethod> used = new HashSet<>(blankStage.getMethods());
        for (int i = 0; i < results.size(); ++i) {
            Set<JApiMethod> nextStage = new HashSet<>();
            results.get(i).forEach(method -> {
                String returnType = getReturnType.apply(method);
                if (allStages.containsKey(returnType)) {
                    allStages.get(returnType).getMethods().forEach(nextMethod -> {
                        if (!used.contains(nextMethod)) {
                            nextStage.add(nextMethod);
                            used.add(nextMethod);
                        }
                    });
                }
            });
            if (!nextStage.isEmpty()) {
                results.add(nextStage);
            }
        }
    }

    @Override
    public boolean isClassLevelChanged() {
        return false;
    }

    @Override
    protected void calcChangeLog() {
        int oldSize = oldMethodStages.size() - 1;
        int newSize = newMethodStages.size() - 1; // don't need check the last stage
        for (int i = 0; i < Math.min(oldSize, newSize); ++i) {
            for (JApiMethod method : oldMethodStages.get(i)) {
                if (newMethodStages.get(i).contains(method)) {
                    switch (method.getChangeStatus()) {
                        case REMOVED:
                            addClassTitle(breakingChange);
                            breakingChange.add(String.format("* `%s` was removed in stage %d", MethodName.name(method.getOldMethod().get()), i + 1));
                            break;
                        case MODIFIED:
                            if (!method.getOldMethod().get().getLongName().equals(method.getNewMethod().get().getLongName())) {
                                addClassTitle(breakingChange);
                                breakingChange.add(String.format("* `%s` -> `%s` in stage %d", MethodName.name(method.getOldMethod().get()), MethodName.name(method.getNewMethod().get()), i + 1));
                            }
                            break;
                    }
                } else if (method.getOldMethod().isPresent()) {
                    addClassTitle(breakingChange);
                    breakingChange.add(String.format("* `%s` was removed in stage %d", MethodName.name(method.getOldMethod().get()), i + 1));
                }
            }
        }
        if (newSize > oldSize) {
            List<String> newStages = IntStream.range(oldSize + 1, newSize + 1).boxed().map(Object::toString).collect(Collectors.toList());
            addClassTitle(breakingChange);
            breakingChange.add(String.format("* Stage %s was added", String.join(", ", newStages)));
        }
    }

    @Override
    protected void addClassTitle(List<String> list) {
        if (list.isEmpty()) {
            list.add(String.format("#### `%s` was modified", parentClass));
            list.add("");
        }
    }
}
