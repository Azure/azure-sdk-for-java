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
AUTOREST_CORE_VERSION = '3.4.5'
AUTOREST_JAVA = '@autorest/java@4.0.35'
DEFAULT_VERSION = '1.0.0-beta.1'
GROUP_ID = 'com.azure.resourcemanager'
API_SPECS_FILE = 'api-specs.yaml'

CI_FILE_FORMAT = 'sdk/{0}/ci.yml'
POM_FILE_FORMAT = 'sdk/{0}/pom.xml'
README_FORMAT = 'specification/{0}/resource-manager/readme.md'
JAR_FORMAT = 'sdk/{service}/{artifact_id}/target/{artifact_id}-{version}.jar'
CHANGELOG_FORMAT = 'sdk/{service}/{artifact_id}/CHANGELOG.md'

MODELERFOUR_ARGUMENTS = '--pipeline.modelerfour.additional-checks=false --pipeline.modelerfour.lenient-model-deduplication=true'
FLUENTLITE_ARGUMENTS = '--java {0} --azure-arm --verbose --sdk-integration --fluent=lite --java.fluent=lite --java.license-header=MICROSOFT_MIT_SMALL'.format(
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
      - sdk/{0}/azure-resourcemanager-{0}/
    exclude:
      - sdk/{0}/pom.xml
      - sdk/{0}/azure-resourcemanager-{0}/pom.xml

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
      - sdk/{0}/azure-resourcemanager-{0}/
    exclude:
      - sdk/{0}/pom.xml
      - sdk/{0}/azure-resourcemanager-{0}/pom.xml

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
  <artifactId>azure-{service}-service</artifactId>
  <packaging>pom</packaging>
  <version>1.0.0</version><!-- Need not change for every release-->

  <profiles>
    <profile>
      <id>coverage</id>
      <modules>
      </modules>

      <dependencies>
      </dependencies>

      <build>
        <plugins>
          <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.5</version> <!-- {{x-version-update;org.jacoco:jacoco-maven-plugin;external_dependency}} -->
            <executions>
              <execution>
                <id>report-aggregate</id>
                <phase>verify</phase>
                <goals>
                  <goal>report-aggregate</goal>
                </goals>
                <configuration>
                  <outputDirectory>${{project.reporting.outputDirectory}}/test-coverage</outputDirectory>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
    <profile>
      <id>default</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>{artifact_id}</module>
      </modules>
    </profile>
  </profiles>
</project>
'''

POM_MODULE_FORMAT = '<module>{0}</module>\n'
