
SPRING_BOOT_MAJOR_2_VERSION_NAME = '2'
SPRING_BOOT_MAJOR_2_VERSION_TAG_PREFIX = ''
SPRING_BOOT_MAJOR_3_VERSION_NAME = '3'
SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX = 'springboot3_'
SPRING_BOOT_MAJOR_VERSION_PREFIX_DICT = {
    SPRING_BOOT_MAJOR_2_VERSION_NAME: SPRING_BOOT_MAJOR_2_VERSION_TAG_PREFIX,
    SPRING_BOOT_MAJOR_3_VERSION_NAME: SPRING_BOOT_MAJOR_3_VERSION_TAG_PREFIX
}


def get_spring_boot_version_tag_prefix(spring_boot_version):
    if spring_boot_version.startswith('3.') or spring_boot_version == SPRING_BOOT_MAJOR_3_VERSION_NAME:
        return SPRING_BOOT_MAJOR_VERSION_PREFIX_DICT[SPRING_BOOT_MAJOR_3_VERSION_NAME]
    elif spring_boot_version.startswith('2.') or spring_boot_version == SPRING_BOOT_MAJOR_2_VERSION_NAME:
        return SPRING_BOOT_MAJOR_VERSION_PREFIX_DICT[SPRING_BOOT_MAJOR_2_VERSION_NAME]
    else:
        return ''


# Since Spring Cloud Azure uses multiple versions of external dependencies managed by Spring Boot,
# the modules that use Spring Boot 2 to manage dependencies are skipped.
SKIP_ADDING_DEPENDENCY_MANAGEMENT_ARTIFACTS = [
    'spring-cloud-azure-starter-monitor-test',
    'spring-cloud-azure-starter-monitor'
]
SKIP_ADDING_DEPENDENCY_MANAGEMENT_ARTIFACTS_WITH_SPRING_BOOT_VERSION = {
    '3.0': [
        'spring-cloud-azure-testcontainers'
    ]
}


def should_skip_artifacts_when_adding_dependency_management(file_path):
    for artifact in SKIP_ADDING_DEPENDENCY_MANAGEMENT_ARTIFACTS:
        if artifact in file_path:
            return True
    else:
        return False


def should_skip_artifacts_when_adding_dependency_management_with_spring_version(spring_boot_version, file_path):
    version_prefix = spring_boot_version[0:spring_boot_version.rindex('.')]
    if version_prefix in list(SKIP_ADDING_DEPENDENCY_MANAGEMENT_ARTIFACTS_WITH_SPRING_BOOT_VERSION.keys()):
        for artifact in SKIP_ADDING_DEPENDENCY_MANAGEMENT_ARTIFACTS_WITH_SPRING_BOOT_VERSION[version_prefix]:
            if artifact in file_path:
                return True
    return False
