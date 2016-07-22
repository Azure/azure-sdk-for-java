#!/bin/bash

# A previous commit may have created the branch, use that
branch=master
if [[ `curl https://api.github.com/repos/Azure/autorest-clientruntime-for-java/branches | jq '.[]["name"]'` == *"sdk_${TRAVIS_PULL_REQUEST}"* ]]; then
    branch=sdk_${TRAVIS_PULL_REQUEST}
fi

# Pull and push the subtree
git subtree pull --squash --prefix runtimes https://${GH_TOKEN}@github.com/Azure/autorest-clientruntime-for-java.git $branch
git subtree push --prefix runtimes https://${GH_TOKEN}@github.com/Azure/autorest-clientruntime-for-java.git sdk_${TRAVIS_PULL_REQUEST} > /dev/null 2>&1

# Create a pull request. This only applies when first time creating the branch
if [[ $branch == "master" ]]; then
    open_pull_request=`curl -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/Azure/autorest-clientruntime-for-java/pulls --data "{\"title\":\"[Automatic PR] SDK changes from pull request #${TRAVIS_PULL_REQUEST}\",\"head\":\"sdk_${TRAVIS_PULL_REQUEST}\",\"base\":\"master\",\"body\":\"Azure/azure-sdk-for-java#${TRAVIS_PULL_REQUEST}\"}"`
    echo $open_pull_request
    # Successfully opened a pull request
    if [[ `echo $open_pull_request | jq '.errors | length'` == 0 ]]; then
        curl -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/Azure/azure-sdk-for-java/issues/${TRAVIS_PULL_REQUEST}/comments --data "{\"body\":\"Runtime changes detected. pull request created. CI running: [![Build Status](https://travis-ci.org/Azure/autorest-clientruntime-for-java.svg?branch=sdk_${TRAVIS_PULL_REQUEST})](https://travis-ci.org/Azure/autorest-clientruntime-for-java)\"}"
    else
        error_msg=`echo $open_pull_request | jq '.'`
        curl -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/Azure/azure-sdk-for-java/issues/${TRAVIS_PULL_REQUEST}/comments --data "{\"body\":\"Runtime changes detected but failed to create a pull request. Error: \\n```json\\n$error_msg\\n```\"}"
    fi
fi
