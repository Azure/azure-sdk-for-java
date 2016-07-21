#!/bin/bash
pull_subtree=`git subtree pull --squash --prefix runtimes git@github.com:Azure/autorest-clientruntime-for-java.git master 2>&1`

echo $pull_subtree

if [[ $pull_subtree == *"Subtree is already at commit"* ]]; then
    echo "No changes";
    exit 0
fi

git subtree push --prefix runtimes https://${GH_TOKEN}@github.com/Azure/autorest-clientruntime-for-java.git sdk_${TRAVIS_PULL_REQUEST} > /dev/null 2>&1
curl -i -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/Azure/autorest-clientruntime-for-java/pulls --data "{\"title\":\"SDK changes from pull request #${TRAVIS_PULL_REQUEST}\",\"head\":\"sdk_${TRAVIS_PULL_REQUEST}\",\"base\":\"master\",\"body\":\"Azure/azure-sdk-for-java#${TRAVIS_PULL_REQUEST}\"}" > /dev/null 2>&1
