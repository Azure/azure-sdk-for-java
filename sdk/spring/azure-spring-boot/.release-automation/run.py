"""This module releases local built jars to staging repo."""

import json
import utilities

def main():
    """
    Release jars to staging repo
    """
    with open('config.json') as config_file:
        configs = json.load(config_file)

    jar_list = utilities.upload_jars(configs)
    utilities.sign_jars(configs)

    artifact_folder = utilities.prepare_artifacts(configs, jar_list)

    repo_id = utilities.create_staging_repo(configs)
    utilities.deploy_to_staging_repo(configs, artifact_folder, repo_id)
    utilities.close_staging_repo(configs, repo_id)

if __name__ == "__main__":
    main()
