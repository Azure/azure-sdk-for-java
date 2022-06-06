# Scripts for Spring Cloud Azure Project

## About

Scripts in this folder is used to hold the scripts used in Spring Cloud Azure project(in /sdk/spring folder).

## Upgrade external dependencies' version according to spring-boot-dependencies and spring-cloud-dependencies.

1. Get `SPRING_BOOT_VERSION` from [spring-boot-dependencies tags](https://github.com/spring-projects/spring-boot/tags). Get `SPRING_CLOUD_VERSION` from [spring-cloud-dependencies tags](https://github.com/spring-cloud/spring-cloud-release/tags). Note that spring-cloud version should compatible with spring-boot version. Refs: [Spring Cloud Release train Spring Boot compatibility](https://spring.io/projects/spring-cloud).
2. Update `SPRING_BOOT_VERSION` and `SPRING_CLOUD_VERSION` in `get_spring_boot_managed_external_dependencies.py`.
3. Run command `python .\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py`. Then a file named `spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt` will be created in `.\sdk\spring\scripts`.
4. Update `SPRING_BOOT_VERSION` in `sync_external_dependencies.py`.
5. Run command `python .\sdk\spring\scripts\sync_external_dependencies.py`. Then versions in `\eng\versioning\external_dependencies.txt` will be synchronized with `spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`.
6. Update the comment at the beginning of `\eng\versioning\external_dependencies.txt`: Update the file name of `spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`.
7. Run command `python .\eng\versioning\update_versions.py --ut external_dependency --sr`
8. Run command `.\eng\versioning\pom_file_version_scanner.ps1 -Debug`. If there is error, fix it.
9. Update changelog about compatible Spring Boot versions and Spring Cloud versions in `/sdk/spring/CHANGELOG.md`.
10. Update two spring-boot versions and two spring-cloud versions in`/sdk/spring/ci.yml` for pipeline, then use script `get_spring_boot_managed_external_dependencies.py` generate corresponding file `spring_boot_SPRING_BOOT_VERSION_managed_external_dependencies.txt`, delete the old two files.
