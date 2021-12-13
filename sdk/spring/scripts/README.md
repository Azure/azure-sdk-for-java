# Scripts for Spring Cloud Azure Project

## About

Scripts in this folder is used to hold the scripts used in Spring Cloud Azure project(in /sdk/spring folder).

## How to upgrade spring boot versions.

1. Update `SPRING_BOOT_VERSION` and `SPRING_CLOUD_VERSION` in `get_spring_boot_managed_external_dependencies.py`.
2. Run command `python .\sdk\spring\scripts\get_spring_boot_managed_external_dependencies.py`.
3. Update `SPRING_BOOT_VERSION` in `sync_external_dependencies.py`.
4. Run command `python .\sdk\spring\scripts\sync_external_dependencies.py`.
5. Run command `python .\eng\versioning\update_versions.py --ut external_dependency --sr`
6. Run command `.\eng\versioning\pom_file_version_scanner.ps1 -Debug`. If there is error, fix it.
7. Update changelog about compatible Spring Boot versions and Spring Cloud versions in `/sdk/spring/CHANGELOG.md`.
