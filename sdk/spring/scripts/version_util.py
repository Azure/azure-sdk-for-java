# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import unittest
from itertools import takewhile


def version_greater_than(version1, version2):
    v1 = version1.split('.')
    v2 = version2.split('.')
    len_1 = len(v1)
    len_2 = len(v2)
    max_len = max(len_1, len_1)
    for i in range(max_len):
        if i < len_1 and i < len_2:
            int_1 = int('0' + ''.join(takewhile(str.isdigit, v1[i])))
            int_2 = int('0' + ''.join(takewhile(str.isdigit, v2[i])))
            if int_1 != int_2:
                return int_1 > int_2
        elif i < len_1:
            return True
        else:
            return False
    return False


class Tests(unittest.TestCase):
    def test_version_greater_than(self):
        self.assertEqual(version_greater_than('1', '2'), False)
        self.assertEqual(version_greater_than('2', '1'), True)
        self.assertEqual(version_greater_than('1.0', '2'), False)
        self.assertEqual(version_greater_than('2.0', '1'), True)
        self.assertEqual(version_greater_than('1.1', '1'), True)
        self.assertEqual(version_greater_than('1', '1.1'), False)
        self.assertEqual(version_greater_than('1.0-RELEASE', '1.1'), False)
        self.assertEqual(version_greater_than('1.1-RELEASE', '1'), True)
        self.assertEqual(version_greater_than('1.1-RELEASE', '1.0'), True)
        self.assertEqual(version_greater_than('1.1-RELEASE', '1.0.1'), True)
        self.assertEqual(version_greater_than('1.1-RELEASE', '1.0.1-RELEASE'), True)
        self.assertEqual(version_greater_than('1.1-RELEASE', '1.1.1-RELEASE'), False)
