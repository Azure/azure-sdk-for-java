# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

URL = 'https://repo.maven.apache.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.pom'


class Pom:
    def __init__(self, group_id, artifact_id, version, depth):
        self.group_id = group_id
        self.artifact_id = artifact_id
        self.version = version
        self.depth = depth

    def to_url(self):
        return URL.format(
            group = self.group_id.replace('.', '/'),
            artifact = self.artifact_id,
            version = self.version)

    def __str__(self):
        return '{}:{}:{}'.format(self.group_id, self.artifact_id, self.version)
