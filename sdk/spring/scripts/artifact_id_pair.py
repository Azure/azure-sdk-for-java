# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


class ArtifactIdPair:
    def __init__(self, old_artifact_id, new_artifact_id):
        self.old_artifact_id = old_artifact_id
        self.new_artifact_id = new_artifact_id

    def __str__(self):
        return '[old_artifact_id: {}; new_artifact_id: {}]'.format(self.old_artifact_id, self.new_artifact_id)
