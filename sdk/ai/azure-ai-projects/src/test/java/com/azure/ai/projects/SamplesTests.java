// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.models.CreateSkillVersionFromFilesBody;
import com.azure.ai.projects.models.DataGenerationJob;
import com.azure.ai.projects.models.SkillFileDetails;
import com.azure.ai.projects.models.ModelVersion;
import com.azure.ai.projects.models.SkillDetails;
import com.azure.ai.projects.models.SkillVersion;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class SamplesTests extends ClientTestBase {
    private static final String SAMPLE_SKILL_NAME = "java-sample-skill-package-test";
    private static final String SAMPLE_SKILL_ASYNC_NAME = "java-sample-skill-package-async-test";

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void skillsPackageSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) throws IOException {
        BetaSkillsClient skillsClient = getClientBuilder(httpClient, serviceVersion).beta().buildBetaSkillsClient();
        String skillName = SAMPLE_SKILL_NAME;

        try {
            skillsClient.deleteSkill(skillName);
        } catch (ResourceNotFoundException ignored) {
            // The sample skill does not already exist.
        }

        SkillVersion imported = null;
        try {
            imported = skillsClient.createSkillVersionFromFiles(skillName, createSkillPackageBody(skillName));
            Assertions.assertNotNull(imported);
            Assertions.assertEquals(skillName, imported.getName());

            SkillDetails fetched = skillsClient.getSkill(imported.getName());
            Assertions.assertNotNull(fetched);
            Assertions.assertEquals(imported.getName(), fetched.getName());

            BinaryData downloaded = skillsClient.getSkillContent(fetched.getName());
            Assertions.assertNotNull(downloaded);
            Assertions.assertTrue(downloaded.toBytes().length > 0);
        } finally {
            String importedSkillName = imported == null ? skillName : imported.getName();
            try {
                skillsClient.deleteSkill(importedSkillName);
            } catch (ResourceNotFoundException ignored) {
                // The skill may not have been created.
            }
        }
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void skillsPackageAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws IOException {
        BetaSkillsAsyncClient skillsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).beta().buildBetaSkillsAsyncClient();
        String skillName = SAMPLE_SKILL_ASYNC_NAME;

        StepVerifier.create(skillsAsyncClient.deleteSkill(skillName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> reactor.core.publisher.Mono.empty())
            .then(skillsAsyncClient.createSkillVersionFromFiles(skillName, createSkillPackageBody(skillName)))
            .flatMap(imported -> {
                Assertions.assertNotNull(imported);
                Assertions.assertEquals(skillName, imported.getName());

                return skillsAsyncClient.getSkill(imported.getName()).doOnNext(fetched -> {
                    Assertions.assertEquals(imported.getName(), fetched.getName());
                }).then(skillsAsyncClient.getSkillContent(imported.getName())).doOnNext(downloaded -> {
                    Assertions.assertNotNull(downloaded);
                    Assertions.assertTrue(downloaded.toBytes().length > 0);
                }).then(skillsAsyncClient.deleteSkill(imported.getName()));
            })).verifyComplete();
    }

    @Disabled("The live service returns 400: API operation not supported for token authentication.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        BetaDatasetsClient dataGenerationJobsClient
            = getClientBuilder(httpClient, serviceVersion).beta().buildBetaDatasetsClient();

        Iterable<DataGenerationJob> jobs = dataGenerationJobsClient.listGenerationJobs(5, PageOrder.DESC, null, null);
        Assertions.assertNotNull(jobs);

        int count = 0;
        for (DataGenerationJob job : jobs) {
            Assertions.assertNotNull(job);
            Assertions.assertNotNull(job.getId());
            count++;
            if (count >= 5) {
                break;
            }
        }
    }

    @Disabled("The live service returns 400: API operation not supported for token authentication.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        BetaDatasetsAsyncClient dataGenerationJobsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).beta().buildBetaDatasetsAsyncClient();

        StepVerifier.create(
            dataGenerationJobsAsyncClient.listGenerationJobs(5, PageOrder.DESC, null, null).take(5).doOnNext(job -> {
                Assertions.assertNotNull(job);
                Assertions.assertNotNull(job.getId());
            }).then()).verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void modelsListLatestSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        BetaModelsClient modelsClient = getClientBuilder(httpClient, serviceVersion).beta().buildBetaModelsClient();

        Iterable<ModelVersion> modelVersions = modelsClient.listLatestModelVersions();
        Assertions.assertNotNull(modelVersions);

        int count = 0;
        for (ModelVersion modelVersion : modelVersions) {
            Assertions.assertNotNull(modelVersion);
            Assertions.assertNotNull(modelVersion.getName());
            Assertions.assertNotNull(modelVersion.getVersion());
            count++;
            if (count >= 5) {
                break;
            }
        }
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void modelsListLatestAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        BetaModelsAsyncClient modelsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).beta().buildBetaModelsAsyncClient();

        AtomicBoolean sawModel = new AtomicBoolean(false);
        StepVerifier.create(modelsAsyncClient.listLatestModelVersions().take(5).doOnNext(modelVersion -> {
            sawModel.set(true);
            Assertions.assertNotNull(modelVersion);
            Assertions.assertNotNull(modelVersion.getName());
            Assertions.assertNotNull(modelVersion.getVersion());
        }).then()).verifyComplete();
        // Empty model lists are valid for projects that have no model assets.
        Assertions.assertNotNull(sawModel);
    }

    @Disabled("Data generation live validation is blocked by 400: API operation not supported for token "
        + "authentication; the create flow is also a long-running preview operation.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationCreateGetCancelDeleteSample(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        Assertions.fail("Enable after token-auth support is available for data generation jobs.");
    }

    @Disabled("Requires FOUNDRY_MODEL_ASSET_NAME, FOUNDRY_MODEL_ASSET_VERSION, and FOUNDRY_MODEL_BLOB_URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void modelVersionCreateGetUpdateDeleteSample(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing model asset environment variables.");
    }

    private static CreateSkillVersionFromFilesBody createSkillPackageBody(String skillName) throws IOException {
        String skillMarkdown = "---\n" + "name: " + skillName + "\n"
            + "description: Answers product support questions using company policy and product guidance.\n" + "---\n\n"
            + "You help answer product support questions using company policy and product guidance.\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            ZipEntry skillEntry = new ZipEntry(skillName + "/SKILL.md");
            skillEntry.setTime(0);
            zipOutputStream.putNextEntry(skillEntry);
            zipOutputStream.write(skillMarkdown.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        SkillFileDetails fileDetails = new SkillFileDetails(BinaryData.fromBytes(outputStream.toByteArray()))
            .setFilename(SAMPLE_SKILL_NAME + ".zip");
        return new CreateSkillVersionFromFilesBody(Arrays.asList(fileDetails));
    }
}
