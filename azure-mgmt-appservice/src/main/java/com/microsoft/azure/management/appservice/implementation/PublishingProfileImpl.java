/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.PublishingProfile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A credential for publishing to a web app.
 */
@Fluent
class PublishingProfileImpl implements PublishingProfile {

    private String ftpUrl;
    private String gitUrl;
    private String ftpUsername;
    private String gitUsername;
    private String ftpPassword;
    private String gitPassword;

    private static final Pattern GIT_REGEX = Pattern.compile("publishMethod=\"MSDeploy\" publishUrl=\"([^\"]+)\".+userName=\"(\\$[^\"]+)\".+userPWD=\"([^\"]+)\"");
    private static final Pattern FTP_REGEX = Pattern.compile("publishMethod=\"FTP\" publishUrl=\"ftp://([^\"]+).+userName=\"([^\"]+\\\\\\$[^\"]+)\".+userPWD=\"([^\"]+)\"");

    PublishingProfileImpl(String publishingProfileXml) {
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
        return gitUrl;
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
