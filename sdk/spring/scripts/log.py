# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import os
from termcolor import colored


class Log:
    DEBUG = 5
    INFO = 4
    WARN = 3
    ERROR = 2
    NONE = 1

    level = INFO
    color = False

    def __init__(self):
        self.level = Log.INFO
        os.system('color')

    def set_log_level(self, level):
        self.level = level

    def set_color(self, color):
        self.color = color

    def debug(self, string):
        if self.level >= Log.DEBUG:
            if self.color:
                print(colored('[DEBUG] {}'.format(string), 'yellow'))
            else:
                print('[DEBUG] {}'.format(string))

    def info(self, string):
        if self.level >= Log.INFO:
            if self.color:
                print(colored('[INFO ] {}'.format(string), 'grey'))
            else:
                print('[INFO ] {}'.format(string))

    def warn(self, string):
        if self.level >= Log.WARN:
            if self.color:
                print(colored('[WARN ] {}'.format(string), 'red'))
            else:
                print('[WARN ] {}'.format(string))

    def error(self, string):
        if self.level >= Log.ERROR:
            if self.color:
                print(colored('[ERROR] {}'.format(string), 'red'))
            else:
                print('[ERROR] {}'.format(string))

    def log_level_test(self):
        self.debug('This is debug log.')
        self.info('This is info log.')
        self.warn('This is warn log.')
        self.error('This is error log.')


log = Log()


if __name__ == '__main__':
    log.set_log_level(Log.DEBUG)
    log.log_level_test()
