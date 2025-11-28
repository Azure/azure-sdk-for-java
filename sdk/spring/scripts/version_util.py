# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import unittest
from itertools import takewhile
from packaging.version import parse

SPECIAL_VERSION_LIST = ['.jre', '.Final', '.RELEASE', '.v']


def version_greater_than(source_version, target_version):
    source_version = format_version(source_version, SPECIAL_VERSION_LIST)
    target_version = format_version(target_version, SPECIAL_VERSION_LIST)
    sv = parse(source_version)
    tv = parse(target_version)

    if sv == tv:
        return True
    elif sv < tv:
        # Spring milestone comparison ('3.0.0-M4', '3.0.0-M5')
        if is_invalid_version(source_version) and is_invalid_version(target_version):
            return False

        # ('1.0-RELEASE','1.1') ('1.1-RELEASE','1') ('1.1-RELEASE','1.0')
        if is_invalid_version(source_version) or is_invalid_version(target_version):
            return special_version_greater_than(source_version, target_version)
    else:
        # Spring RC version should be bigger than milestone version ('3.0.0-RC1', '3.0.0-M5')
        if not is_invalid_version(source_version) and sv.is_prerelease and is_invalid_version(target_version):
            return True

        # ('1.1-RELEASE','1.0.1-RELEASE')
        if is_invalid_version(source_version) and is_invalid_version(target_version):
            return True

        # ('2.7.4', '3.0.0-M5')
        if is_invalid_version(source_version) or is_invalid_version(target_version):
            return special_version_greater_than(source_version, target_version)

    if sv.major != tv.major:
        return sv.major > tv.major
    elif sv.major == tv.major and sv.minor != tv.minor:
        return sv.minor > tv.minor
    elif sv.major == tv.major and sv.minor == tv.minor and sv.micro != tv.micro:
        return sv.micro >= tv.micro
    return sv > tv


def format_version(version, str_list):
    for i in str_list:
        if version.find(i):
            version = version.partition(i)[0]
    return version


def is_invalid_version(verify_version):
    version_dict = vars(parse(verify_version))
    return type(version_dict['_version']) == str


def special_version_greater_than(version1, version2):
    v1 = version1.split('.')
    v2 = version2.split('.')
    len_1 = len(v1)
    len_2 = len(v2)

    max_len = max(len_1, len_2)
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
        self.assertEqual(version_greater_than('2.7.4', '3.0.0-M5'), False)
        self.assertEqual(version_greater_than('3.0.0-M5', '2.7.4'), True)
        self.assertEqual(version_greater_than('3.0.0-M4', '3.0.0-M5'), False)
        self.assertEqual(version_greater_than('3.0.0-M5', '3.0.0-M4'), True)
        self.assertEqual(version_greater_than('3.0.0-M5', '3.0.0-RC1'), False)
        self.assertEqual(version_greater_than('3.0.0-RC1', '3.0.0-M5'), True)
        self.assertEqual(version_greater_than('3.0.0-RC1', '3.0.0-RC2'), False)
        self.assertEqual(version_greater_than('3.0.0-RC2', '3.0.0-RC1'), True)
        self.assertEqual(version_greater_than('11.2.3.jre17', '10.2.3.jre8'), True)
        self.assertEqual(version_greater_than('4.1.89.Final', '4.1.87.Final'), True)
        self.assertEqual(version_greater_than('6.2.0.RELEASE', '6.2.2.RELEASE'), False)
        self.assertEqual(version_greater_than('9.4.50.v20221201', '11.0.13'), False)
