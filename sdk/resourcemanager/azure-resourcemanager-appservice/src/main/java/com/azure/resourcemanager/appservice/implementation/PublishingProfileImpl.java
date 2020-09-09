// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A credential for publishing to a web app. */
class PublishingProfileImpl implements PublishingProfile {

    private String ftpUrl;
    private String gitUrl;
    private String ftpUsername;
    private String gitUsername;
    private String ftpPassword;
    private String gitPassword;

    private final WebAppBase parent;

    private static final Pattern GIT_REGEX =
        Pattern
            .compile(
                "publishMethod=\"MSDeploy\" publishUrl=\"([^\"]+)\".+userName=\"(\\$[^\"]+)\".+userPWD=\"([^\"]+)\"");
    private static final Pattern FTP_REGEX =
        Pattern
            .compile(
                "publishMethod=\"FTP\""
                    + " publishUrl=\"ftp://([^\"]+).+userName=\"([^\"]+\\\\\\$[^\"]+)\".+userPWD=\"([^\"]+)\"");

    PublishingProfileImpl(String publishingProfileXml, WebAppBase parent) {
        Matcher matcher = GIT_REGEX.matcher(publishingProfileXml);
        if (matcher.find()) {
            gitUrl = matcher.group(1);
            gitUsername = matcher.group(2);
            gitPassword = matcher.group(3);
        }
        matcher = FTP_REGEX.matcher(publishingProfileXml);
        if (matcher.find()) {
            ftpUrl = matcher.group(1);
            ftpUsername = matcher.group(2);
            ftpPassword = matcher.group(3);
        }
        this.parent = parent;
    }

    @Override
    public String ftpUrl() {
        return ftpUrl;
    }

    @Override
    public String ftpUsername() {
        return ftpUsername;
    }

    @Override
    public String gitUrl() {
        String repoName;
        if (parent instanceof DeploymentSlot) {
            repoName = ((DeploymentSlot) parent).parent().name() + ".git";
        } else {
            repoName = parent.name() + ".git";
        }
        if (!gitUrl.startsWith("https://")) {
            gitUrl = "https://" + gitUrl;
        }
        return gitUrl + "/" + repoName;
    }

    @Override
    public String gitUsername() {
        return gitUsername;
    }

    @Override
    public String ftpPassword() {
        return ftpPassword;
    }

    @Override
    public String gitPassword() {
        return gitPassword;
    }
}
