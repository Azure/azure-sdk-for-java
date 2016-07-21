#!/bin/bash
set -e # exit with nonzero exit code if anything fails

git config user.name "Travis CI"
git config user.email "azuresdk@outlook.com"

git subtree pull --squash --prefix runtimes git@github.com:Azure/autorest-clientruntime-for-java.git master --no-edit
git subtree push --prefix runtimes https://${GH_TOKEN}@github.com:Azure/autorest-clientruntime-for-java.git sdk_${TRAVIS_PULL_REQUEST}

curl -i -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/Azure/autorest-clientruntime-for-java/pulls --data "{\"title\":\"SDK changes from pull request #${TRAVIS_PULL_REQUEST}\",\"head\":\"sdk_${TRAVIS_PULL_REQUEST}\",\"base\":\"master\",\"body\":\"#${TRAVIS_PULL_REQUEST}\"}"
