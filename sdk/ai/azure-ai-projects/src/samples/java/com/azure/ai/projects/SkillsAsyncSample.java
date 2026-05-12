// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.SkillDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating CRUD operations on Skills using the asynchronous SkillsAsyncClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsAsyncSample {

    private static SkillsAsyncClient skillsAsyncClient
        = new AIProjectClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSkillsAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
//        createSkill().block();
//        getSkill().block();
//        updateSkill().block();
//        listSkills().blockLast();
//        deleteSkill().block();
    }

    public static Mono<SkillDetails> createSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.createSkill

        Map<String, String> metadata = new HashMap<>();
        metadata.put("domain", "support");

        return skillsAsyncClient.createSkill(
            "product-support-skill",
            "Answers product support questions using company policy.",
            "You help answer product support questions using company policy and product guidance.",
            metadata
        ).doOnNext(skill -> {
            System.out.println("Created skill: " + skill.getName());
            System.out.println("Skill ID: " + skill.getSkillId());
            System.out.println("Blob present: " + skill.isBlobPresent());
        });

        // END:com.azure.ai.projects.SkillsAsyncSample.createSkill
    }

    public static Mono<SkillDetails> getSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.getSkill

        String skillName = "product-support-skill";

        return skillsAsyncClient.getSkill(skillName)
            .doOnNext(skill -> {
                System.out.println("Skill name: " + skill.getName());
                System.out.println("Skill ID: " + skill.getSkillId());
                System.out.println("Description: " + skill.getDescription());
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.getSkill
    }

    public static Mono<SkillDetails> updateSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.updateSkill

        String skillName = "product-support-skill";

        Map<String, String> metadata = new HashMap<>();
        metadata.put("domain", "support");
        metadata.put("status", "updated");

        return skillsAsyncClient.updateSkill(
            skillName,
            "Updated description for the sample skill.",
            null,
            metadata
        ).doOnNext(updated -> {
            System.out.println("Updated skill: " + updated.getName());
            System.out.println("Description: " + updated.getDescription());
            System.out.println("Metadata: " + updated.getMetadata());
        });

        // END:com.azure.ai.projects.SkillsAsyncSample.updateSkill
    }

    public static Flux<SkillDetails> listSkills() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.listSkills

        return skillsAsyncClient.listSkills()
            .doOnNext(skill -> {
                System.out.println("Skill name: " + skill.getName());
                System.out.println("Skill ID: " + skill.getSkillId());
                System.out.println("Blob present: " + skill.isBlobPresent());
                System.out.println("-------------------------------------------------");
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.listSkills
    }

    public static Mono<Void> deleteSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.deleteSkill

        String skillName = "product-support-skill";

        return skillsAsyncClient.deleteSkill(skillName)
            .doOnSuccess(unused -> System.out.println("Deleted skill: " + skillName));

        // END:com.azure.ai.projects.SkillsAsyncSample.deleteSkill
    }
}
