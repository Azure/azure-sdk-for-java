// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import java.util.Optional;

public final class DevopsPipeline {

    private static final DevopsPipeline INSTANCE = readFromEnvironment();

    private final String jobName;
    private final String buildId;
    private final String definitionName;
    private final String buildReason;
    private final String teamProject;
    private final boolean setsDevVersion;

    private DevopsPipeline(String jobName, String buildId, String definitionName, String buildReason,
                          String teamProject, boolean setsDevVersion) {
        this.jobName = jobName;
        this.buildId = buildId;
        this.definitionName = definitionName;
        this.buildReason = buildReason;
        this.teamProject = teamProject;
        this.setsDevVersion = setsDevVersion;
    }

    public static Optional<DevopsPipeline> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    private static DevopsPipeline readFromEnvironment() {
        boolean isRunningOnAgent = Boolean.parseBoolean(
            System.getenv().getOrDefault("TF_BUILD", "false"));
        if (isRunningOnAgent) {
            String jobName = System.getenv("AGENT_JOBNAME");
            String buildId = System.getenv("BUILD_BUILDID");
            String definitionName = System.getenv("BUILD_DEFINITIONNAME");
            String buildReason = System.getenv("BUILD_REASON");
            String teamProject = System.getenv("SYSTEM_TEAMPROJECT");
            String setDevVersionString = System.getenv("SETDEVVERSION");
            boolean setDevVersion = Boolean.parseBoolean(setDevVersionString);
            return new DevopsPipeline(jobName, buildId, definitionName, buildReason, teamProject, setDevVersion);
        } else {
            return null;
        }
    }

    public String getJobName() {
        return jobName;
    }

    public String getBuildId() {
        return buildId;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public String getBuildReason() {
        return buildReason;
    }

    public String getTeamProject() {
        return teamProject;
    }

    public boolean getSetsDevVersion() {
        return setsDevVersion;
    }

    public boolean releasesToMavenCentral() {
        return definitionName.equals("java - storage")
            && buildReason.equalsIgnoreCase("manual")
            && !setsDevVersion;
    }
}
