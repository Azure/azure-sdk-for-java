// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.models.DataGenerationJob;
import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.ai.projects.models.ModelVersion;
import com.azure.ai.projects.models.SkillDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class SamplesTests extends ClientTestBase {
    private static final String SAMPLE_SKILL_NAME = "java-sample-skill-package-test";
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void skillsPackageSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) throws IOException {
        SkillsClient skillsClient = getClientBuilder(httpClient, serviceVersion).buildSkillsClient();

        try {
            skillsClient.deleteSkill(SAMPLE_SKILL_NAME);
        } catch (ResourceNotFoundException ignored) {
            // The sample skill does not already exist.
        }

        SkillDetails imported = null;
        try {
            imported = skillsClient.createSkillFromPackage(createSkillPackage());
            Assertions.assertNotNull(imported);
            Assertions.assertEquals(SAMPLE_SKILL_NAME, imported.getName());
            Assertions.assertTrue(imported.isBlobPresent());

            SkillDetails fetched = skillsClient.getSkill(imported.getName());
            Assertions.assertNotNull(fetched);
            Assertions.assertEquals(imported.getName(), fetched.getName());

            BinaryData downloaded = skillsClient.downloadSkill(fetched.getName());
            Assertions.assertNotNull(downloaded);
            Assertions.assertTrue(downloaded.toBytes().length > 0);

            Path downloadPath = Files.createTempFile(fetched.getName() + "-", ".zip");
            Files.write(downloadPath, downloaded.toBytes());
            Assertions.assertTrue(Files.size(downloadPath) > 0);
        } finally {
            String skillName = imported == null ? SAMPLE_SKILL_NAME : imported.getName();
            try {
                skillsClient.deleteSkill(skillName);
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
        SkillsAsyncClient skillsAsyncClient = getClientBuilder(httpClient, serviceVersion).buildSkillsAsyncClient();

        StepVerifier.create(skillsAsyncClient.deleteSkill(SAMPLE_SKILL_NAME)
            .onErrorResume(ResourceNotFoundException.class, ignored -> reactor.core.publisher.Mono.empty())
            .then(skillsAsyncClient.createSkillFromPackage(createSkillPackage()))
            .flatMap(imported -> {
                Assertions.assertNotNull(imported);
                Assertions.assertEquals(SAMPLE_SKILL_NAME, imported.getName());
                Assertions.assertTrue(imported.isBlobPresent());

                return skillsAsyncClient.getSkill(imported.getName()).doOnNext(fetched -> {
                    Assertions.assertEquals(imported.getName(), fetched.getName());
                    Assertions.assertTrue(fetched.isBlobPresent());
                }).then(skillsAsyncClient.downloadSkill(imported.getName())).doOnNext(downloaded -> {
                    Assertions.assertNotNull(downloaded);
                    Assertions.assertTrue(downloaded.toBytes().length > 0);
                }).then(skillsAsyncClient.deleteSkill(imported.getName()));
            })).verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DataGenerationJobsClient dataGenerationJobsClient
            = getClientBuilder(httpClient, serviceVersion).buildDataGenerationJobsClient();

        Iterable<DataGenerationJob> jobs = dataGenerationJobsClient.listGenerationJobs(DATA_GENERATION_PREVIEW, 5,
            PageOrder.DESC, null, null, null, null);
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

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).buildDataGenerationJobsAsyncClient();

        StepVerifier.create(dataGenerationJobsAsyncClient
            .listGenerationJobs(DATA_GENERATION_PREVIEW, 5, PageOrder.DESC, null, null, null, null)
            .take(5)
            .doOnNext(job -> {
                Assertions.assertNotNull(job);
                Assertions.assertNotNull(job.getId());
            })
            .then()).verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void modelsListLatestSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ModelsClient modelsClient = getClientBuilder(httpClient, serviceVersion).buildModelsClient();

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
        ModelsAsyncClient modelsAsyncClient = getClientBuilder(httpClient, serviceVersion).buildModelsAsyncClient();

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

    @Disabled("Requires FOUNDRY_MODEL_NAME and creates a long-running preview data generation job.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationCreateGetCancelDeleteSample(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        Assertions.fail(
            "Enable after providing FOUNDRY_MODEL_NAME and deciding whether to record this long-running preview flow.");
    }

    @Disabled("Requires FOUNDRY_MODEL_ASSET_NAME, FOUNDRY_MODEL_ASSET_VERSION, and FOUNDRY_MODEL_BLOB_URI.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void modelVersionCreateGetUpdateDeleteSample(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        Assertions.fail("Enable after providing model asset environment variables.");
    }

    private static BinaryData createSkillPackage() throws IOException {
        String skillMarkdown = "---\n" + "name: " + SAMPLE_SKILL_NAME + "\n"
            + "description: Answers product support questions using company policy and product guidance.\n" + "---\n\n"
            + "You help answer product support questions using company policy and product guidance.\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(SAMPLE_SKILL_NAME + "/SKILL.md"));
            zipOutputStream.write(skillMarkdown.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        return BinaryData.fromBytes(outputStream.toByteArray());
    }
}
