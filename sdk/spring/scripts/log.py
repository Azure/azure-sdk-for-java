# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.


import os
from termcolor import colored, cprint


class Log:
    DEBUG = 5
    INFO = 4
    WARN = 3
    ERROR = 2
    NONE = 1

    level = INFO

    def __init__(self):
        self.level = Log.INFO
        os.system('color')

    def set_log_level(self, level):
        self.level = level

    def debug(self, string):
        if self.level >= Log.DEBUG:
            print(colored('[DEBUG] {}'.format(string), 'yellow'))

    def info(self, string):
        if self.level >= Log.INFO:
            print(colored('[INFO ] {}'.format(string), 'grey'))

    def warn(self, string):
        if self.level >= Log.WARN:
            print(colored('[WARN ] {}'.format(string), 'red'))

    def error(self, string):
        if self.level >= Log.ERROR:
            cprint(colored('[ERROR] {}'.format(string), 'red'))

    def log_level_test(self):
        self.debug('This is debug log.')
        self.info('This is info log.')
        self.warn('This is warn log.')
        self.error('This is error log.')


log = Log()


if __name__ == '__main__':
    log.set_log_level(Log.DEBUG)
    log.log_level_test()
