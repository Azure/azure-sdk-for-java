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
SDK_ROOT = '../../../'  # related to file dir
AUTOREST_CORE_VERSION = '3.0.6327'
AUTOREST_JAVA = '@autorest/java@4.0.5'
DEFAULT_VERSION = '1.0.0-beta.1'
GROUP_ID = 'com.azure.resourcemanager'
API_SPECS_FILE = 'api-specs.yaml'

CI_FILE_FORMAT = 'sdk/{0}/ci.yml'
POM_FILE_FORMAT = 'sdk/{0}/pom.xml'
README_FORMAT = 'specification/{0}/resource-manager/readme.md'

MODELERFOUR_ARGUMENTS = '--pipeline.modelerfour.additional-checks=false --pipeline.modelerfour.lenient-model-deduplication=true --pipeline.modelerfour.flatten-payloads=false'
FLUENTLITE_ARGUMENTS = '--java {0} --azure-arm --verbose --sdk-integration --fluent=lite --java.fluent=lite --java.license-header=MICROSOFT_MIT_SMALL'.format(
    MODELERFOUR_ARGUMENTS)

CI_HEADER = '''\
# NOTE: Please refer to https://aka.ms/azsdk/engsys/ci-yaml before editing this file.

'''

CI_FORMAT = '''\
trigger:
  branches:
    include:
      - master
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/{0}/

pr:
  branches:
    include:
      - master
      - feature/*
      - hotfix/*
      - release/*
  paths:
    include:
      - sdk/{0}/

extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: {0}
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
  <artifactId>azure-{0}-service</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version><!-- Need not change for every release-->
  <modules>
  </modules>
</project>
'''

POM_MODULE_FORMAT = '    <module>{0}</module>\n'
