# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


class VersionUpdateItem:
    def __init__(self, id, new_version):
        self.id = id
        self.new_version = new_version

    def __str__(self):
        return '[id: {}; new_version: {}]'.format(self.id, self.new_version)