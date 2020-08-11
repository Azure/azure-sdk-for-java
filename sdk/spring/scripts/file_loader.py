# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


def load_dict(dict_file):
    result_dict = {}
    with open(dict_file) as f:
        for raw_line in f:
            stripped_line = raw_line.strip()
            if not stripped_line or stripped_line.startswith('#'):
                continue
            key_value = stripped_line.split(':')
            key = key_value[0]
            value = key_value[1]
            if key in result_dict:
                raise ValueError('Duplicate entry: {}'.format(key))
            result_dict[key] = value
    return result_dict


def load_list(list_file):
    result_list = []
    with open(list_file) as f:
        for raw_line in f:
            item = raw_line.strip()
            if not item or item.startswith('#'):
                continue
            if item in result_list:
                raise ValueError('Duplicate entry: {}'.format(item))
            result_list.append(item)
    return result_list
