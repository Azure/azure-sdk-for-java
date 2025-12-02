#!/usr/bin/env python3

# changeable parameters
# use update logic in generate.py#update_parameters
# set to None first to guarantee it would be updated
SUFFIX = None

NAMESPACE_SUFFIX = None
ARTIFACT_SUFFIX = None
NAMESPACE_FORMAT = None
ARTIFACT_FORMAT = None
OUTPUT_FOLDER_FORMAT = None

# Constant parameters
MAVEN_HOST = "https://repo1.maven.org/maven2"
MAVEN_URL = MAVEN_HOST + "/{group_id}/{artifact_id}/{version}/{artifact_id}-{version}.jar"

SDK_ROOT = "../../"  # related to file dir
AUTOREST_CORE_VERSION = "3.9.7"
AUTOREST_JAVA = "@autorest/java@4.1.60"
DEFAULT_VERSION = "1.0.0-beta.1"
GROUP_ID = "com.azure.resourcemanager"
API_SPECS_FILE = "api-specs.yaml"

CI_FILE_FORMAT = "sdk/{0}/ci.yml"
POM_FILE_FORMAT = "sdk/{0}/pom.xml"
README_FORMAT = "specification/{0}/resource-manager/readme.md"
JAR_FORMAT = "sdk/{service}/{artifact_id}/target/{artifact_id}-{version}.jar"
CHANGELOG_FORMAT = "sdk/{service}/{artifact_id}/CHANGELOG.md"

MODELERFOUR_ARGUMENTS = "--modelerfour.additional-checks=false --modelerfour.lenient-model-deduplication=true"
FLUENTLITE_ARGUMENTS = "{0} --azure-arm --verbose --sdk-integration --generate-samples --generate-tests --fluent=lite --java.fluent=lite --java.license-header=MICROSOFT_MIT_SMALL".format(
    MODELERFOUR_ARGUMENTS
)
FLUENTPREMIUM_ARGUMENTS = (
    "--verbose --generate-samples --fluent --java.fluent --java.license-header=MICROSOFT_MIT_SMALL"
)

FLUENT_PREMIUM_PACKAGES = (
    "azure-resourcemanager-appplatform",
    "azure-resourcemanager-appservice",
    "azure-resourcemanager-authorization",
    "azure-resourcemanager-cdn",
    "azure-resourcemanager-compute",
    "azure-resourcemanager-containerinstance",
    "azure-resourcemanager-containerregistry",
    "azure-resourcemanager-containerservice",
    "azure-resourcemanager-cosmos",
    "azure-resourcemanager-dns",
    "azure-resourcemanager-eventhubs",
    "azure-resourcemanager-keyvault",
    "azure-resourcemanager-monitor",
    "azure-resourcemanager-msi",
    "azure-resourcemanager-network",
    "azure-resourcemanager-privatedns",
    "azure-resourcemanager-redis",
    "azure-resourcemanager-resources",
    "azure-resourcemanager-search",
    "azure-resourcemanager-servicebus",
    "azure-resourcemanager-sql",
    "azure-resourcemanager-storage",
    "azure-resourcemanager-trafficmanager",
)

CI_HEADER = """\
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

"""

CI_FORMAT = """\
trigger:
  branches:
    include:
      - main
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/{0}/ci.yml
      - sdk/{0}/{1}/
    exclude:
      - sdk/{0}/pom.xml
      - sdk/{0}/{1}/pom.xml

pr:
  branches:
    include:
      - main
      - feature/*
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/{0}/ci.yml
      - sdk/{0}/{1}/
    exclude:
      - sdk/{0}/pom.xml
      - sdk/{0}/{1}/pom.xml

parameters: []

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: {0}
    Artifacts: []
"""

POM_FORMAT = """\
<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.azure</groupId>
  <artifactId>azure-{service}-service</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version><!-- Need not change for every release-->

  <modules>
    <module>{artifact_id}</module>
  </modules>
</project>
"""

POM_MODULE_FORMAT = "<module>{0}</module>\n"

CHANGELOG_INITIAL_SECTION_FORMAT = """\
### Features Added

- Initial release for the {artifact_id} Java SDK.
"""
