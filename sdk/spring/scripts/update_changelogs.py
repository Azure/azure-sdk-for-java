import os
import re
from datetime import datetime

# python sdk/spring/scripts/update_changelogs.py

PACKAGES_DIR = "./sdk/spring"
EXCLUDE_PATHS = {"./sdk/spring\spring-cloud-azure-native-reachability", "./sdk/spring\azure-spring-data-cosmos"}
BETA_HEADER_PATTERN = re.compile(r"^## (\d+\.\d+\.\d+)-beta\.\d+ \(Unreleased\)$")


def update_changelog(package_path: str):
    changelog_path = os.path.join(package_path, "CHANGELOG.md")
    if not os.path.exists(changelog_path):
        return
    with open(changelog_path, "r", encoding="utf-8") as f:
        lines = f.readlines()
        if not lines:
            return

        new_lines = []
        i = 0
        updated = False

        while i < len(lines):
            line = lines[i]
            match = BETA_HEADER_PATTERN.match(line)

            if match and not updated:
                base_version = match.group(1)
                date_str = datetime.now().strftime("%Y-%m-%d")
                new_header = f"## {base_version} ({date_str})\n"
                new_log = (
                    f"Please refer to [spring/CHANGELOG.md]"
                    f"(https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CHANGELOG.md"
                    f"#{base_version.replace(".", "")}-{date_str}) for more details.\n"
                )
                new_lines.append(new_header)
                i += 1

                while i < len(lines) and not lines[i].startswith("## "):
                    i += 1

                # Insert the new one-liner
                new_lines.append("\n" + new_log + "\n")
                updated = True

            else:
                new_lines.append(line)
                i += 1

    with open (changelog_path, "w", encoding="utf-8") as f:
        f.writelines(new_lines)

    print(f"✅ Updated CHANGELOG for: {os.path.basename(package_path)}")

def main():
    for entry in os.listdir(PACKAGES_DIR):
        package_path = os.path.join(PACKAGES_DIR, entry)
        if os.path.isdir(package_path) and package_path not in EXCLUDE_PATHS:
            update_changelog(package_path)

if __name__ == "__main__":
    main()
