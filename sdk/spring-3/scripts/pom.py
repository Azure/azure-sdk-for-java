# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

CENTRAL_REPO_URL = 'https://repo.maven.apache.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.pom'
SPRING_REPO_URL = 'https://repo.spring.io/milestone/{group}/{artifact}/{version}/{artifact}-{version}.pom'


class Pom:
    def __init__(self, group_id, artifact_id, version, depth):
        self.group_id = group_id
        self.artifact_id = artifact_id
        self.version = version
        self.depth = depth

    def to_url(self):
        return self.get_url().format(
            group = self.group_id.replace('.', '/'),
            artifact = self.artifact_id,
            version = self.version)

    def __str__(self):
        return '{}:{}:{}'.format(self.group_id, self.artifact_id, self.version)

    def is_milestone_release(self):
        return (('org.springframework' in self.group_id) or ('io.micrometer' == self.group_id)) and \
               (('-M' in self.version) or ('-RC' in self.version))

    def get_url(self):
        if self.is_milestone_release():
            return SPRING_REPO_URL
        else:
            return CENTRAL_REPO_URL
