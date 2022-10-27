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
MAVEN_URL = 'https://repo1.maven.org/maven2/{group_id}/{artifact_id}/{version}/{artifact_id}-{version}.jar'

SDK_ROOT = '../../../'  # related to file dir
AUTOREST_CORE_VERSION = '3.8.4'
AUTOREST_JAVA = '@autorest/java@4.1.6'
DEFAULT_VERSION = '1.0.0-beta.1'
GROUP_ID = 'com.azure.resourcemanager'
API_SPECS_FILE = 'api-specs.yaml'

CI_FILE_FORMAT = 'sdk/{0}/ci.yml'
POM_FILE_FORMAT = 'sdk/{0}/pom.xml'
README_FORMAT = 'specification/{0}/resource-manager/readme.md'
JAR_FORMAT = 'sdk/{service}/{artifact_id}/target/{artifact_id}-{version}.jar'
CHANGELOG_FORMAT = 'sdk/{service}/{artifact_id}/CHANGELOG.md'

MODELERFOUR_ARGUMENTS = '--modelerfour.additional-checks=false --modelerfour.lenient-model-deduplication=true'
FLUENTLITE_ARGUMENTS = '{0} --azure-arm --verbose --sdk-integration --generate-samples --fluent=lite --java.fluent=lite --java.license-header=MICROSOFT_MIT_SMALL'.format(
    MODELERFOUR_ARGUMENTS)

CI_HEADER = '''\
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

'''

CI_FORMAT = '''\
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
    EnableBatchRelease: true
    Artifacts: []
'''

POM_FORMAT = '''\
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
'''

POM_MODULE_FORMAT = '<module>{0}</module>\n'
