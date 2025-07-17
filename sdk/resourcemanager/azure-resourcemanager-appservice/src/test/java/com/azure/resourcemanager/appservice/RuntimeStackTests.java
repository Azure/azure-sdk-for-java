// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.test.utils.CliRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RuntimeStackTests {

    @Test
    public void verifyDeprecatedNotInGetAll() {
        Collection<RuntimeStack> stacks = RuntimeStack.getAll();
        Assertions.assertTrue(stacks.contains(RuntimeStack.TOMCAT_10_0_JAVA11));
        Assertions.assertFalse(stacks.contains(RuntimeStack.TOMCAT_10_0_JRE11));
    }

    /**
     * This test is for syncing RuntimeStack with latest supported stacks from backend.
     * To run this test, upgrade Azure CLI to latest version by running "az upgrade", and remove "@Disabled".
     */
    @Test
    @Disabled
    public void listNewRuntimeStacks() throws IOException, InterruptedException {
        Set<String> latestStacks = getLatestStacks();
        Set<String> allCurrentStacks = Arrays.stream(RuntimeStack.class.getDeclaredFields())
            .filter(field -> field.getType() == RuntimeStack.class)
            .map(field -> {
                try {
                    return field.get(null).toString();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toSet());
        Set<String> supportedStacks
            = RuntimeStack.getAll().stream().map(RuntimeStack::toString).collect(Collectors.toSet());
        List<String> newStacks = latestStacks.stream()
            .filter(stack -> !allCurrentStacks.contains(stack))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        List<String> newDeprecated = supportedStacks.stream()
            .filter(stack -> !latestStacks.contains(stack))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("New stacks: " + newStacks);
        System.out.println("New deprecated stacks: " + newDeprecated);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getLatestStacks() throws IOException, InterruptedException {
        String cliOutput = CliRunner.run("az webapp list-runtimes --os linux");
        List<String> outputList
            = BinaryData.fromString(cliOutput.substring(cliOutput.indexOf("["))).toObject(List.class);
        return outputList.stream().map(stack -> String.join(" ", stack.split(":"))).collect(Collectors.toSet());
    }
}
