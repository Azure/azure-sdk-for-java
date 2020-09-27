// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.models.Architecture;
import com.azure.resourcemanager.containerregistry.models.BaseImageTriggerType;
import com.azure.resourcemanager.containerregistry.models.OS;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryDockerTaskStep;
import com.azure.resourcemanager.containerregistry.models.RegistryEncodedTaskStep;
import com.azure.resourcemanager.containerregistry.models.RegistryFileTaskStep;
import com.azure.resourcemanager.containerregistry.models.RegistryTask;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRun;
import com.azure.resourcemanager.containerregistry.models.RunStatus;
import com.azure.resourcemanager.containerregistry.models.SourceControlType;
import com.azure.resourcemanager.containerregistry.models.SourceTriggerEvent;
import com.azure.resourcemanager.containerregistry.models.SourceUploadDefinition;
import com.azure.resourcemanager.containerregistry.models.TokenType;
import com.azure.resourcemanager.containerregistry.models.TriggerStatus;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RegistryTaskTests extends RegistryTest {

    @Test
    @Disabled("Needs personal tokens to run")
    public void fileTaskTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String taskFilePath = "Path to your task file that is relative to the githubRepoUrl";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);
        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineFileTaskStep()
                .withTaskPath(taskFilePath)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryFileTaskStep registryFileTaskStep = (RegistryFileTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert that the registryTask file path is correct
        Assertions.assertEquals(taskFilePath, registryFileTaskStep.taskFilePath());

        // Assert CPU count is correct
        Assertions.assertEquals(2, registryTask.cpuCount());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void fileTaskUpdateTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String taskFilePath = "Path to your task file that is relative to the githubRepoUrl";
        String taskFileUpdatePath = "Path to your update task file that is relative to the githubRepoUrl";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);
        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineFileTaskStep()
                .withTaskPath(taskFilePath)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        registryTask.update().updateFileTaskStep().withTaskPath(taskFileUpdatePath).parent().apply();

        RegistryFileTaskStep registryFileTaskStep = (RegistryFileTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert CPU count is correct
        Assertions.assertEquals(2, registryTask.cpuCount());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());

        // Checking to see whether file path name is updated correctly
        Assertions.assertEquals(taskFileUpdatePath, registryFileTaskStep.taskFilePath());

        boolean errorRaised = false;
        try {
            registryTask.update().updateEncodedTaskStep().parent().apply();
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }

        // Checking to see whether error is raised if update is called on the incorrect registryTask step type.
        Assertions.assertTrue(errorRaised);
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void encodedTaskTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String encodedTaskContent = "Base64 encoded task content";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);

        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineEncodedTaskStep()
                .withBase64EncodedTaskContent(encodedTaskContent)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryEncodedTaskStep registryEncodedTaskStep = (RegistryEncodedTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert that the registryTask file path is correct
        Assertions.assertEquals(encodedTaskContent, registryEncodedTaskStep.encodedTaskContent());

        // Assert CPU count is correct
        Assertions.assertEquals(2, registryTask.cpuCount());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void encodedTaskUpdateTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String encodedTaskContent = "Base64 encoded task content";
        String encodedTaskContentUpdate = "Base64 encoded task content that we want to update to";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);

        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineEncodedTaskStep()
                .withBase64EncodedTaskContent(encodedTaskContent)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        registryTask
            .update()
            .updateEncodedTaskStep()
            .withBase64EncodedTaskContent(encodedTaskContentUpdate)
            .parent()
            .apply();

        RegistryEncodedTaskStep registryEncodedTaskStep = (RegistryEncodedTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert that the registryTask file path is correct
        Assertions.assertEquals(encodedTaskContentUpdate, registryEncodedTaskStep.encodedTaskContent());

        // Assert CPU count is correct
        Assertions.assertEquals(1, registryTask.cpuCount());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());

        boolean errorRaised = false;
        try {
            registryTask.update().updateDockerTaskStep().parent().apply();
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }

        // Checking to see whether error is raised if update is called on the incorrect registryTask step type.
        Assertions.assertTrue(errorRaised);
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void dockerTaskTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String dockerFilePath = "Replace with your docker file path relative to githubContext, eg: Dockerfile";
        String imageName = "Replace with the name of your image.";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);
        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryDockerTaskStep registryDockerTaskStep = (RegistryDockerTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert that the registryTask file path is correct
        Assertions.assertEquals(dockerFilePath, registryDockerTaskStep.dockerFilePath());

        // Assert that the image name array is correct
        Assertions.assertEquals(imageName, registryDockerTaskStep.imageNames().get(0));

        // Assert that with cache works
        Assertions.assertTrue(!registryDockerTaskStep.noCache());

        // Assert that push is enabled
        Assertions.assertTrue(registryDockerTaskStep.isPushEnabled());

        // Assert CPU count is correct
        Assertions.assertEquals(2, registryTask.cpuCount());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void dsockerTaskUpdateTest() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String dockerFilePath = "Replace with your docker file path relative to githubContext, eg: Dockerfile";
        String dockerFilePathUpdate =
            "Replace this with your docker file path that you updated your registryTask to, if you did update your"
                + " docker file path";
        String imageName = "Replace with the name of your image.";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);
        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        registryTask
            .update()
            .updateDockerTaskStep()
            .withDockerFilePath(dockerFilePathUpdate)
            .withCacheEnabled(false)
            .withPushEnabled(false)
            .parent()
            .apply();

        RegistryDockerTaskStep registryDockerTaskStep = (RegistryDockerTaskStep) registryTask.registryTaskStep();

        // Assert the name of the registryTask is correct
        Assertions.assertEquals(taskName, registryTask.name());

        // Assert the resource group name is correct
        Assertions.assertEquals(rgName, registryTask.resourceGroupName());

        // Assert location is correct
        Assertions.assertEquals(Region.US_WEST_CENTRAL.name(), registryTask.regionName());

        // Assert OS is correct
        Assertions.assertEquals(OS.LINUX, registryTask.platform().os());

        // Assert architecture is correct
        Assertions.assertEquals(Architecture.AMD64, registryTask.platform().architecture());

        // Assert that the registryTask file path is correct
        Assertions.assertEquals(dockerFilePathUpdate, registryDockerTaskStep.dockerFilePath());

        // Assert that the image name array is correct
        Assertions.assertEquals(imageName, registryDockerTaskStep.imageNames().get(0));

        // Assert that with no cache works
        Assertions.assertTrue(registryDockerTaskStep.noCache());

        // Assert that push is disabled
        Assertions.assertTrue(!registryDockerTaskStep.isPushEnabled());

        // Assert the length of the source triggers array list is correct
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());

        boolean errorRaised = false;
        try {
            registryTask.update().updateFileTaskStep().parent().apply();
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }

        // Checking to see whether error is raised if update is called on the incorrect registryTask step type.
        Assertions.assertTrue(errorRaised);
    }

    @Test
    public void fileTaskRunRequestFromRegistry() {
        final String acrName = generateRandomResourceName("acr", 10);
        String sourceLocation = "https://github.com/Azure/acr.git";
        String taskFilePath = "samples/java/task/acb.yaml";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registry
                .scheduleRun()
                .withWindows()
                .withFileTaskRunRequest()
                .defineFileTaskStep()
                .withTaskPath(taskFilePath)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();
        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.WINDOWS, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.WINDOWS, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    public void fileTaskRunRequestFromRuns() {
        final String acrName = generateRandomResourceName("acr", 10);
        String sourceLocation = "https://github.com/Azure/acr.git";
        String taskFilePath = "samples/java/task/acb.yaml";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registryManager
                .registryTaskRuns()
                .scheduleRun()
                .withExistingRegistry(rgName, acrName)
                .withLinux()
                .withFileTaskRunRequest()
                .defineFileTaskStep()
                .withTaskPath(taskFilePath)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();
        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    public void encodedTaskRunRequestFromRegistry() throws Exception {
        final String acrName = generateRandomResourceName("acr", 10);
        String sourceLocation = "https://github.com/Azure/acr.git";
        String encodedTaskContent = Base64.getEncoder().encodeToString(readTaskYaml());

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registry
                .scheduleRun()
                .withLinux()
                .withEncodedTaskRunRequest()
                .defineEncodedTaskStep()
                .withBase64EncodedTaskContent(encodedTaskContent)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();

        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    public void encodedTaskRunRequestFromRuns() throws Exception {
        final String acrName = generateRandomResourceName("acr", 10);
        String sourceLocation = "https://github.com/Azure/acr.git#master:samples/java/task";
        String encodedTaskContent = Base64.getEncoder().encodeToString(readTaskYaml());

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registryManager
                .registryTaskRuns()
                .scheduleRun()
                .withExistingRegistry(rgName, acrName)
                .withLinux()
                .withEncodedTaskRunRequest()
                .defineEncodedTaskStep()
                .withBase64EncodedTaskContent(encodedTaskContent)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();

        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    public void dockerTaskRunRequestFromRegistry() {
        final String acrName = generateRandomResourceName("acr", 10);
        String dockerFilePath = "Dockerfile";
        String imageName = "test";
        String sourceLocation = "https://github.com/Azure/acr.git#master:samples/java/task";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registry
                .scheduleRun()
                .withLinux()
                .withDockerTaskRunRequest()
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();
        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    public void dockerTaskRunRequestFromRuns() {
        final String acrName = generateRandomResourceName("acr", 10);
        String dockerFilePath = "Dockerfile";
        String imageName = "test";
        String sourceLocation = "https://github.com/Azure/acr.git#master:samples/java/task";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registryManager
                .registryTaskRuns()
                .scheduleRun()
                .withExistingRegistry(rgName, acrName)
                .withLinux()
                .withDockerTaskRunRequest()
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        registryTaskRun.refresh();
        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());

        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void taskRunRequestFromRegistry() {
        final String acrName = generateRandomResourceName("acr", 10);
        String imageName = "Replace with the name of your image.";
        String taskName = generateRandomResourceName("ft", 10);
        String dockerFilePath = "Replace with your docker file path relative to githubContext, eg: Dockerfile";
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryTaskRun registryTaskRun =
            registry.scheduleRun().withTaskRunRequest(taskName).withArchiveEnabled(true).execute();

        boolean stillQueued = true;
        while (stillQueued) {
            registryTaskRun.refresh();
            if (registryTaskRun.status() != RunStatus.QUEUED) {
                stillQueued = false;
            }
            if (registryTaskRun.status() == RunStatus.FAILED) {
                System
                    .out
                    .println(registryManager.registryTaskRuns().getLogSasUrl(rgName, acrName, registryTaskRun.runId()));
                stillQueued = false;
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
        }

        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertEquals(taskName, registryTaskRun.taskName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
        Assertions.assertEquals(taskName, registryTaskRunFromList.taskName());
    }

    @Test
    @Disabled("Needs personal tokens to run")
    public void taskRunRequestFromRuns() {
        final String acrName = generateRandomResourceName("acr", 10);
        String imageName = "Replace with the name of your image.";
        String taskName = generateRandomResourceName("ft", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String dockerFilePath = "Replace with your docker file path relative to githubContext, eg: Dockerfile";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryTaskRun registryTaskRun =
            registryManager
                .registryTaskRuns()
                .scheduleRun()
                .withExistingRegistry(rgName, acrName)
                .withTaskRunRequest(taskName)
                .withArchiveEnabled(true)
                .execute();

        boolean stillQueued = true;
        while (stillQueued) {
            registryTaskRun.refresh();
            if (registryTaskRun.status() != RunStatus.QUEUED) {
                stillQueued = false;
            }
            if (registryTaskRun.status() == RunStatus.FAILED) {
                System
                    .out
                    .println(registryManager.registryTaskRuns().getLogSasUrl(rgName, acrName, registryTaskRun.runId()));
                stillQueued = false;
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
        }
        Assertions.assertEquals(registry.resourceGroupName(), registryTaskRun.resourceGroupName());
        Assertions.assertEquals(acrName, registryTaskRun.registryName());
        Assertions.assertEquals(taskName, registryTaskRun.taskName());
        Assertions.assertTrue(registryTaskRun.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRun.platform().os());

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        RegistryTaskRun registryTaskRunFromList = registryTaskRuns.stream().findFirst().get();
        Assertions.assertTrue(registryTaskRunFromList.status() != null);
        Assertions.assertEquals("QuickRun", registryTaskRunFromList.runType().toString());
        Assertions.assertTrue(registryTaskRunFromList.isArchiveEnabled());
        Assertions.assertEquals(OS.LINUX, registryTaskRunFromList.platform().os());
        Assertions.assertEquals("Succeeded", registryTaskRunFromList.provisioningState().toString());
        Assertions.assertEquals(taskName, registryTaskRunFromList.taskName());
    }

    @Test
    public void getBuildSourceUploadUrlFromRegistryAndRegistries() {
        final String acrName = generateRandomResourceName("acr", 10);

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        // Calling getBuildSourceUploadUrl from Registry
        SourceUploadDefinition buildSourceUploadUrlRegistry = registry.getBuildSourceUploadUrl();
        Assertions.assertNotNull(buildSourceUploadUrlRegistry.relativePath());
        Assertions.assertNotNull(buildSourceUploadUrlRegistry.uploadUrl());

        // Calling getBuildSourceUploadUrl from Registries
        SourceUploadDefinition buildSourceUploadUrlRegistries =
            registryManager.containerRegistries().getBuildSourceUploadUrl(rgName, acrName);
        Assertions.assertNotNull(buildSourceUploadUrlRegistries.relativePath());
        Assertions.assertNotNull(buildSourceUploadUrlRegistries.uploadUrl());
    }

    @Test
    @Disabled("Needs personal tokens to run.")
    public void cancelAndDeleteRunsAndTasks() {
        final String acrName = generateRandomResourceName("acr", 10);
        String taskName = generateRandomResourceName("ft", 10);
        String dockerFilePath = "Replace with your docker file path relative to githubContext, eg: Dockerfile";
        String imageName = "Replace with the name of your image.";
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        RegistryTaskRun registryTaskRun =
            registry.scheduleRun().withTaskRunRequest(taskName).withArchiveEnabled(true).execute();

        boolean stillQueued = true;
        while (stillQueued) {
            registryTaskRun.refresh();
            if (registryTaskRun.status() != RunStatus.QUEUED) {
                stillQueued = false;
            }
            if (registryTaskRun.status() == RunStatus.FAILED) {
                System
                    .out
                    .println(registryManager.registryTaskRuns().getLogSasUrl(rgName, acrName, registryTaskRun.runId()));
                Assertions.fail("Registry registryTask run failed");
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
        }

        Assertions.assertTrue(registryManager.registryTaskRuns().listByRegistry(rgName, acrName).stream().count() == 1);

        // cancelling the run we just created
        registryManager.serviceClient().getRuns().cancel(rgName, acrName, registryTaskRun.runId());

        boolean notCanceled = true;
        while (notCanceled) {
            registryTaskRun.refresh();
            if (registryTaskRun.status() == RunStatus.CANCELED) {
                notCanceled = false;
            }
            if (registryTaskRun.status() == RunStatus.FAILED) {
                System
                    .out
                    .println(registryManager.registryTaskRuns().getLogSasUrl(rgName, acrName, registryTaskRun.runId()));
                Assertions.fail("Registry registryTask run failed");
            }
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
        }

        PagedIterable<RegistryTaskRun> registryTaskRuns =
            registryManager.registryTaskRuns().listByRegistry(rgName, acrName);

        for (RegistryTaskRun rtr : registryTaskRuns) {
            Assertions.assertTrue(rtr.status() == RunStatus.CANCELED);
        }

        // deleting the run we just cancelled
        for (RegistryTaskRun rtr : registryTaskRuns) {
            registryManager.containerRegistryTasks().deleteByRegistry(rgName, acrName, taskName);
        }

        registryTaskRuns = registryManager.registryTaskRuns().listByRegistry(rgName, acrName);
        // Test is set to 1 because there is a server side issue that results in task runs not actually being deleted.
        // Test will fail once the server side issue is fixed.
        Assertions
            .assertTrue(registryManager.containerRegistryTasks().listByRegistry(rgName, acrName).stream().count() == 1);
    }

    @Test
    public void getLogSasUrl() {
        final String acrName = generateRandomResourceName("acr", 10);
        String dockerFilePath = "Dockerfile";
        String imageName = "test";
        String sourceLocation = "https://github.com/Azure/acr.git#master:samples/java/task";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        RegistryTaskRun registryTaskRun =
            registryManager
                .registryTaskRuns()
                .scheduleRun()
                .withExistingRegistry(rgName, acrName)
                .withLinux()
                .withDockerTaskRunRequest()
                .defineDockerTaskStep()
                .withDockerFilePath(dockerFilePath)
                .withImageNames(Arrays.asList(imageName))
                .withCacheEnabled(true)
                .withPushEnabled(true)
                .attach()
                .withSourceLocation(sourceLocation)
                .withArchiveEnabled(true)
                .execute();

        String sasUrl = registryManager.registryTaskRuns().getLogSasUrl(rgName, acrName, registryTaskRun.runId());
        Assertions.assertNotNull(sasUrl);
        Assertions.assertNotEquals("", sasUrl);
    }

    @Test
    @Disabled("Needs personal tokens to run.")
    public void updateTriggers() {
        final String acrName = generateRandomResourceName("acr", 10);
        String githubRepoUrl = "Replace with your github repository url, eg: https://github.com/Azure/acr.git";
        String githubBranch = "Replace with your github repositoty branch, eg: master";
        String githubPAT =
            "Replace with your github personal access token which should have the scopes: admin:repo_hook and repo";
        String taskFilePath = "Path to your task file that is relative to the githubRepoUrl";
        String githubRepoUrlUpdate =
            "Replace with your github repository url to update to, eg: https://github.com/Azure/acr.git";

        Registry registry =
            registryManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        String taskName = generateRandomResourceName("ft", 10);
        RegistryTask registryTask =
            registryManager
                .containerRegistryTasks()
                .define(taskName)
                .withExistingRegistry(rgName, acrName)
                .withLocation(Region.US_WEST_CENTRAL.name())
                .withLinux(Architecture.AMD64)
                .defineFileTaskStep()
                .withTaskPath(taskFilePath)
                .attach()
                .defineSourceTrigger("SampleSourceTrigger")
                .withGithubAsSourceControl()
                .withSourceControlRepositoryUrl(githubRepoUrl)
                .withCommitTriggerEvent()
                .withPullTriggerEvent()
                .withRepositoryBranch(githubBranch)
                .withRepositoryAuthentication(TokenType.PAT, githubPAT)
                .withTriggerStatusEnabled()
                .attach()
                .withBaseImageTrigger("SampleBaseImageTrigger", BaseImageTriggerType.RUNTIME)
                .withCpuCount(2)
                .create();

        // Assert there is the correct number of source triggers
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source control is correct
        Assertions
            .assertEquals(
                SourceControlType.GITHUB.toString(),
                registryTask.trigger().sourceTriggers().get(0).sourceRepository().sourceControlType().toString());

        // Assert source control repository url is correct
        Assertions
            .assertEquals(
                githubRepoUrl, registryTask.trigger().sourceTriggers().get(0).sourceRepository().repositoryUrl());

        // Ignore because of server-side error regarding pull request
        // Assert source control source trigger event list is of correct size

        //        Assertions.assertTrue(registryTask.trigger().sourceTriggers().get(0).sourceTriggerEvents().size() ==
        // 2);
        //
        // Temporarily set size to 1 so when pull request functionality is added back, there is a test alert
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().get(0).sourceTriggerEvents().size() == 1);

        // Assert source trigger event list contains commit
        Assertions
            .assertTrue(
                registryTask
                    .trigger()
                    .sourceTriggers()
                    .get(0)
                    .sourceTriggerEvents()
                    .contains(SourceTriggerEvent.COMMIT));
        //
        //        //Assert source trigger event list contains pull request
        //
        // Assertions.assertTrue(registryTask.trigger().sourceTriggers().get(0).sourceTriggerEvents().contains(SourceTriggerEvent.PULLREQUEST));

        // Assert source control repository branch is correct
        Assertions
            .assertEquals(githubBranch, registryTask.trigger().sourceTriggers().get(0).sourceRepository().branch());

        // Assert trigger status is correct
        Assertions
            .assertEquals(
                TriggerStatus.ENABLED.toString(), registryTask.trigger().sourceTriggers().get(0).status().toString());

        // Assert name of the base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTrigger", registryTask.trigger().baseImageTrigger().name());

        // Assert that the base image trigger type is correct
        Assertions
            .assertEquals(
                BaseImageTriggerType.RUNTIME.toString(),
                registryTask.trigger().baseImageTrigger().baseImageTriggerType().toString());

        registryTask
            .update()
            .updateSourceTrigger("SampleSourceTrigger")
            .withGithubAsSourceControl()
            .withSourceControlRepositoryUrl(githubRepoUrlUpdate)
            .withCommitTriggerEvent()
            .withRepositoryAuthentication(TokenType.PAT, githubPAT)
            .withTriggerStatusDisabled()
            .parent()
            .updateBaseImageTrigger("SampleBaseImageTriggerUpdate", BaseImageTriggerType.ALL)
            .apply();

        // Assert source triggers are correct
        Assertions.assertEquals("SampleSourceTrigger", registryTask.trigger().sourceTriggers().get(0).name());

        // Assert source control is correct
        Assertions
            .assertEquals(
                SourceControlType.GITHUB.toString(),
                registryTask.trigger().sourceTriggers().get(0).sourceRepository().sourceControlType().toString());

        // Assert source control repository url is correct
        Assertions
            .assertEquals(
                githubRepoUrlUpdate, registryTask.trigger().sourceTriggers().get(0).sourceRepository().repositoryUrl());

        // Assert source trigger has correct number of trigger events
        Assertions.assertTrue(registryTask.trigger().sourceTriggers().size() == 1);

        // Assert source trigger event list contains commit
        Assertions
            .assertTrue(
                registryTask
                    .trigger()
                    .sourceTriggers()
                    .get(0)
                    .sourceTriggerEvents()
                    .contains(SourceTriggerEvent.COMMIT));

        // Assert trigger status is correct
        Assertions
            .assertEquals(
                TriggerStatus.DISABLED.toString(), registryTask.trigger().sourceTriggers().get(0).status().toString());

        // Assert name of the base image trigger is correct
        Assertions.assertEquals("SampleBaseImageTriggerUpdate", registryTask.trigger().baseImageTrigger().name());

        // Assert that the base image trigger type is correct
        Assertions
            .assertEquals(
                BaseImageTriggerType.ALL.toString(),
                registryTask.trigger().baseImageTrigger().baseImageTriggerType().toString());
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    private byte[] readTaskYaml() throws Exception {
        File taskFile = new File(getClass().getClassLoader().getResource("task.yaml").getFile());
        FileInputStream taskFileInput = new FileInputStream(taskFile);
        byte[] data = new byte[(int) taskFile.length()];
        taskFileInput.read(data);
        return data;
    }
}
