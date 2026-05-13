// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.SkillDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Sample demonstrating uploading and downloading a skill package using the asynchronous SkillsAsyncClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsPackageAsyncSample {
    private static final String SKILL_NAME = "java-sample-skill-package";

    private static final SkillsAsyncClient SKILLS_ASYNC_CLIENT = new AIProjectClientBuilder()
        .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildSkillsAsyncClient();

    public static void main(String[] args) throws IOException {
        AtomicReference<String> skillNameRef = new AtomicReference<>(SKILL_NAME);

        Mono<Void> workflow = SKILLS_ASYNC_CLIENT.deleteSkill(SKILL_NAME)
            .doOnSuccess(unused -> System.out.println("Deleted existing skill: " + SKILL_NAME))
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(SKILLS_ASYNC_CLIENT.createSkillFromPackage(createSkillPackage()))
            .doOnNext(imported -> {
                skillNameRef.set(imported.getName());
                System.out.println("Imported skill from package: " + imported.getName()
                    + " (" + imported.getSkillId() + ") blobPresent=" + imported.isBlobPresent());
            })
            .flatMap(imported -> SKILLS_ASYNC_CLIENT.getSkill(imported.getName()))
            .doOnNext(fetched -> System.out.println("Fetched imported skill: " + fetched.getName()
                + " (" + fetched.getSkillId() + ") blobPresent=" + fetched.isBlobPresent()))
            .flatMap(SkillsPackageAsyncSample::downloadSkillPackage)
            .flatMap(fetched -> SKILLS_ASYNC_CLIENT.deleteSkill(fetched.getName())
                .doOnSuccess(unused -> System.out.println("Deleted imported skill: " + fetched.getName())));

        workflow
            .onErrorResume(error -> SKILLS_ASYNC_CLIENT.deleteSkill(skillNameRef.get())
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .then(Mono.error(error)))
            .timeout(Duration.ofMinutes(2))
            .block();
    }

    private static Mono<SkillDetails> downloadSkillPackage(SkillDetails fetched) {
        return SKILLS_ASYNC_CLIENT.downloadSkill(fetched.getName())
            .map(downloaded -> {
                try {
                    Path downloadPath = Files.createTempFile(fetched.getName() + "-", ".zip");
                    Files.write(downloadPath, downloaded.toBytes());
                    System.out.println("Downloaded skill package path: " + downloadPath);
                    return fetched;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private static BinaryData createSkillPackage() throws IOException {
        String skillMarkdown = "---\n"
            + "name: " + SKILL_NAME + "\n"
            + "description: Answers product support questions using company policy and product guidance.\n"
            + "---\n\n"
            + "You help answer product support questions using company policy and product guidance.\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(SKILL_NAME + "/SKILL.md"));
            zipOutputStream.write(skillMarkdown.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        return BinaryData.fromBytes(outputStream.toByteArray());
    }
}
