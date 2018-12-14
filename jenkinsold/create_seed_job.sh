#!/bin/bash

jenkins_url=$1
jenkins_user=$2
jenkins_apikey=$3

if [ -z "${jenkins_url}" -o \
     -z "${jenkins_user}" -o \
     -z "${jenkins_apikey}" ]; then
     echo "Usage: create_seed_job.sh <jenkins url> <jenkins_user> <jenkins_apikey>"
     exit 1
fi

git_remote=`git status -s -b | head -1 | sed 's/.*\.//' | cut -d/ -f1`
git_branch=`git status -s -b | head -1 | sed 's/.*\.//' | cut -d/ -f2`
git_url=`git remote get-url ${git_remote}`

cat seed-job.xml | \
    sed \
    -e 's|__MY_GIT_URL__|'${git_url}'|' \
    -e 's|__MY_GIT_BRANCH__|'${git_branch}'|' \
    > /tmp/seed-job.xml

curl -i -s -u ${jenkins_user}:${jenkins_apikey} -X POST ${jenkins_url}/createItem?name=customer-seed-job --data-binary @/tmp/seed-job.xml -H "Content-Type: text/xml"
