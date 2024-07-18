
SPRING_BOOT_MAJOR_2_VERSION_TAG_PREFIX = ''
SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX = 'springboot3_'
SPRING_BOOT_MAJOR_2_VERSION_NAME = 'v2'
SPRING_BOOT_MAJOR_3_VERSION_NAME = 'v3'
SPRING_BOOT_MAJOR_VERSION_PREFIX_MAP = {
    'v2': SPRING_BOOT_MAJOR_2_VERSION_TAG_PREFIX,
    'v3': SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX
}


def get_spring_boot_major_version_tag_prefix(spring_boot_version):
    if spring_boot_version.startswith('3.'):
        return SPRING_BOOT_MAJOR_VERSION_PREFIX_MAP['v3']
    elif spring_boot_version.startswith('2.'):
        return SPRING_BOOT_MAJOR_VERSION_PREFIX_MAP['v2']
    else:
        return ''
