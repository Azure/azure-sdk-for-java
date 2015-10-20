#!/usr/bin/python
import sys
import os
import fnmatch
import shutil
import re


def main(argv):
    # validation
    if len(argv) < 4:
        print "Usage: " + get_usage()
        exit(1)
    # figure out source dir
    folder = os.path.dirname(os.path.realpath(__file__))
    if len(argv) == 5:
        folder = argv[4]
        if not os.path.isdir(folder):
            print "Cannot find directory " + folder
            exit(1)
    folder = folder.strip('/')
    # make working dir
    version = argv[1]
    working = folder + "/" + version
    if os.path.exists(working):
        print "Cowardly exiting because " + working + " already exists"
        exit(1)
    os.mkdir(working)
    # copy over all jars
    for i in get_jars(folder):
        shutil.copy(i, working)
    # copy over all poms
    pkgs = []
    for i in get_poms(folder):
        assert isinstance(i, str)
        parts = i.rsplit("\\")
        pkg_name = parts[len(parts) - 2]
        if len(parts) == len(folder.rsplit("\\")) + 1:
            # root folder
            shutil.copyfile(i, "%s/%s-%s.pom" % (working, "azure-bom", version))
            pkg_name = "azure-bom"
        elif pkg_name == "azure":
            # parent folder
            shutil.copyfile(i, "%s/%s-%s.pom" % (working, "azure-parent", version))
            pkg_name = "azure-parent"
        else:
            shutil.copyfile(i, "%s/%s-%s.pom" % (working, pkg_name, version))
        pkgs.append(pkg_name)
    # filter out packages
    print "Publishing the following: "
    to_pub = []
    for pkg in pkgs:
        if re.match(argv[3], pkg) is not None:
            to_pub.append(os.path.join(working, pkg))
            print pkg
    if len(to_pub) == 0:
        print "No compiled package matches the regex. Exiting."
        exit(1)
    for pkg in to_pub:
        cmd = "mvn gpg:sign-and-deploy-file"
        cmd += " -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        cmd += " -DrepositoryId=sonatype-nexus-staging -DpomFile=%s-%s.pom" % (pkg, version)
        assert isinstance(pkg, str)
        if pkg.endswith("azure-parent") or pkg.endswith("azure-bom"):
            cmd += " -Dfile=%s-%s.pom -Dgpg.passphrase=%s" % (pkg, version, argv[2])
            os.system(cmd)
        else:
            os.system(cmd + " -Dfile=%s-%s.jar -Dgpg.passphrase=%s" % (pkg, version, argv[2]))
            os.system(cmd + " -Dfile=%s-%s-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=%s" % (pkg, version, argv[2]))
            os.system(cmd + " -Dfile=%s-%s-sources.jar -Dclassifier=sources -Dgpg.passphrase=%s" % (pkg, version, argv[2]))
    print "Finished."


def mvn_package():
    cmd = "mvn package source:jar javadoc:jar"
    print "Shell: " + cmd
    os.system(cmd)


def get_poms(folder):
    matches = []
    for root, dirnames, filenames in os.walk(folder):
        for filename in fnmatch.filter(filenames, 'pom.xml'):
            matches.append(os.path.join(root, filename))
    return matches


def get_jars(folder):
    matches = []
    for root, dirnames, filenames in os.walk(folder):
        for filename in fnmatch.filter(filenames, '*.jar'):
            matches.append(os.path.join(root, filename))
    return matches


def get_usage():
    return "publish.py version gpg_passphrase package_grep_string [root_directory]"


main(sys.argv)
