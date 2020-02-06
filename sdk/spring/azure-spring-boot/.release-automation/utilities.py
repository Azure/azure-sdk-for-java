"""This module includes helper functions for releasing local built jars to staging repo."""

import datetime
import json
import hashlib
import glob
import os
import re
import shutil
import subprocess
import time
import requests
from requests.auth import HTTPBasicAuth
import xmltodict
from jenkinsapi.jenkins import Jenkins

def upload_jars(configs):
    """
    Uploading local built jars to sign server
    """
    print("Upload jars to signing server...")
    jar_list = []
    for module_name in configs["moduleNames"]:
        module_folder = get_module_folder(configs, module_name)
        module_jars = get_folder_files(module_folder, ["*.jar"])

        for module_jar in module_jars:
            print("--Uploading " + module_jar)
            jar_list.append(os.path.basename(module_jar))
            shutil.copy2(module_jar, configs["toSignFolder"])

    return jar_list


def sign_jars(configs):
    """
    Use Jenkins job to sign uploaded jars
    """
    print("Using Jenkins job to sign uploaded Jars...")
    jenkins = Jenkins(configs["jenkins"]["url"],
                      configs["jenkins"]["username"], configs["passwords"]["jenkins"])
    sign_jar_job = jenkins["sign-jar"]
    queue = sign_jar_job.invoke(block=True, build_params=configs["jenkins"]["signJar"])

    if queue.get_build().get_status() == "SUCCESS":
        print("--Jars are signed successfully!")
    else:
        raise Exception("Failed at jar signing. For details, please check " +
                        queue.get_build().get_result_url())


def download_and_delete_jars(configs, artifact_folder, jar_list):
    """
    Download signed jars to local shared folder for further releasing
    """
    print("Downloading signed jar to artifact folder...")
    for jar_name in jar_list:
        print("--" + jar_name)
        jar_path = os.path.join(configs["signedFilder"], jar_name)
        shutil.copy2(jar_path, artifact_folder)
        os.remove(jar_path)


def copy_poms(configs, artifact_folder):
    """
    Copy POM files from local maven repo to shared folder
    """
    print("Copying POM files to artifact folder...")
    for module_name in configs["moduleNames"]:
        module_folder = get_module_folder(configs, module_name)
        pom_files = get_folder_files(module_folder, ["*.pom"])

        for pom_file in pom_files:
            print("--" + pom_file)
            shutil.copy2(pom_file, artifact_folder)


def gpg_sign(configs, artifact_folder):
    """
    Sign all files using gpg utility from www.gnupg.org
    """
    print("GPG sign all files in artifact folder...")
    for file_to_sign in os.listdir(artifact_folder):
        gpg_str = 'gpg --batch --passphrase {0} -ab {1}'.format(
            configs["passwords"]["gpg"], os.path.join(artifact_folder, file_to_sign))
        print("--" + gpg_str)
        subprocess.call(gpg_str)


def generate_checksum(artifact_folder):
    """
    Generate md5 and sh1 for all jars and poms
    """
    print("Generating checksum files...")
    files_grabed = get_folder_files(artifact_folder, ["*.jar", "*.pom"])
    for file in files_grabed:
        file_name = os.path.basename(file)

        md5 = file.replace(file_name, file_name + ".md5")
        print("--md5 file " + md5)
        with open(md5, "w") as md5_file:
            md5_file.write(hashlib.md5(open(file, 'rb').read()).hexdigest())

        sha1 = file.replace(file_name, file_name + ".sha1")
        print("--sha1 file " + sha1)
        with open(sha1, "w") as sha1_file:
            sha1_file.write(hashlib.sha1(open(file, 'rb').read()).hexdigest())


def prepare_artifacts(configs, jar_list):
    """
    Make all artifacts readly in a temp folder under specificed target folder
    """
    artifact_folder = os.path.join(configs["targetFolder"],
                                   datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S'))
    print("Creating artifact folder {0}...".format(artifact_folder))
    os.makedirs(artifact_folder)

    download_and_delete_jars(configs, artifact_folder, jar_list)
    copy_poms(configs, artifact_folder)
    gpg_sign(configs, artifact_folder)
    generate_checksum(artifact_folder)

    return artifact_folder


def create_staging_repo(configs):
    """
    Create a Nexus staging repo for deploying artifacts
    """
    print("Creating staging repo...")

    url = configs["nexus"]["createRepoURL"]
    content = configs["nexus"]["stagingRepoDescription"]
    header = {"Content-Type": "application/xml"}
    basic_auth = HTTPBasicAuth(configs["nexus"]["username"], configs["passwords"]["nexus"])
    response = requests.post(url, data=content, headers=header, auth=basic_auth)

    if response.status_code == 201:
        json_response = json.loads(json.dumps(xmltodict.parse(response.text)))
        staging_repo = json_response["promoteResponse"]["data"]["stagedRepositoryId"]
        print("--Staging repo created: " + staging_repo)
        return staging_repo
    else:
        raise Exception("Failed at creating staging repo. Status code: " +
                        str(response.status_code) + " Response content: " + response.text)


def deploy_to_staging_repo(configs, artifact_folder, repo_name):
    """
    Upload articfacts to created staging repo
    """
    print("Deploying artifacts to staging repo...")
    for module_name in configs["moduleNames"]:
        for file_path in glob.glob(os.path.join(artifact_folder, module_name + "*")):
            print("--" + file_path)
            file_name = os.path.basename(file_path)
            url_join_items = [configs["nexus"]["deployRepoURL"],
                              repo_name, configs["groupId"].replace(".", "/"),
                              module_name, configs["releaseVersion"], file_name]
            upload_url = ("/".join(url_join_items))
            files = {'file': open(file_path, 'rb')}
            basic_auth = HTTPBasicAuth(configs["nexus"]["username"], configs["passwords"]["nexus"])
            response = requests.post(upload_url, files=files, auth=basic_auth)

            if response.status_code == 201:
                print("--Succeeded")
            else:
                raise Exception("Failed at creating staging repo. Status code: " +
                                str(response.status_code) + " Response content: " + response.text)


def close_staging_repo(configs, repo_id):
    """
    Close staging repo to verify uploaded artifacts
    """
    print("Closing staging repo...")

    url = configs["nexus"]["closeRepoURL"]
    content = "<promoteRequest><data><stagedRepositoryId>{0}"\
    "</stagedRepositoryId></data></promoteRequest>".format(repo_id)
    header = {"Content-Type": "application/xml"}
    basic_auth = HTTPBasicAuth(configs["nexus"]["username"], configs["passwords"]["nexus"])
    response = requests.post(url, data=content, headers=header, auth=basic_auth)

    if response.status_code == 201:
        times = 1
        status = get_repo_status(configs, repo_id)
        while status == "open" and times < 15:
            times += 1
            time.sleep(10)
            status = get_repo_status(configs, repo_id)

        if status == "closed":
            print("--Staging repo is closed and ready for testing: " + repo_id)
        else:
            print("--Failed to close staging repo. Please check repo activity for root cause.")
            print(get_repo_activity(configs, repo_id))
    else:
        raise Exception("--Failed at closing staging repo. Status code: " +
                        str(response.status_code) + " Response content: " + response.text)


def get_repo_status(configs, repo_id):
    """
    Utility to get repo status
    """
    print("--Getting staging repo status...")
    url = "/".join([configs["nexus"]["repoBaseURL"], repo_id])
    basic_auth = HTTPBasicAuth(configs["nexus"]["username"], configs["passwords"]["nexus"])
    response = requests.get(url, auth=basic_auth)

    if response.status_code == 200:
        json_response = json.loads(json.dumps(xmltodict.parse(response.text)))
        status = json_response["stagingProfileRepository"]["type"]
        print("----" + status)
        return status
    else:
        raise Exception("----Failed at getting repo status. Status code: " +
                        str(response.status_code) + " Response content: " + response.text)


def get_repo_activity(configs, repo_id):
    """
    Utility to get repo activity
    """
    print("--Getting staging repo activity...")
    url = "/".join([configs["nexus"]["repoBaseURL"], repo_id, "activity"])
    basic_auth = HTTPBasicAuth(configs["nexus"]["username"], configs["passwords"]["nexus"])
    response = requests.get(url, auth=basic_auth)

    if response.status_code == 200:
        return response.text
    else:
        raise Exception("----Failed at getting repo activity. Status code: " +
                        str(response.status_code) + " Response content: " + response.text)

def get_local_repository_path():
    """
    Get maven's local repository
    """
    result = subprocess.run("cmd /c mvn help:evaluate -Dexpression=settings.localRepository",
                            stdout=subprocess.PIPE)

    regex = re.compile('.*[INFO].*')
    path = regex.sub("", result.stdout.decode("utf-8")).rstrip().lstrip()
    return path

def get_module_folder(configs, module_name):
    """
    Get the full path of local module folder
    """
    repo_path = get_local_repository_path()
    return os.path.join(repo_path, configs["groupId"].replace(".", "/"),
                        module_name, configs["releaseVersion"])

def get_folder_files(folder, types):
    """
    Get file list with given folder and types
    """
    files_grabed = []
    for file_type in types:
        files_grabed.extend(glob.glob(os.path.join(folder, file_type)))
    return files_grabed
