#!/bin/bash

set -x

SONARQUBE_URL=""
ANALYSIS_ID=""
GIT_REF=""
TASKDETAILS=task_details.json

usage()
{
cat <<EOF
    usage: $0 --sq-report <file> --sq-url <url> --git-ref <git reference> [options]

    This script starts the dp-raf flow for Heuristics Releases

    Mandatory:
    --sq-report          path to sonarQube report file (report-task.txt)
    --sq-url             typicaly http://localhost:9000
    --git-ref            Git reference: commit-id, tag

     Options:
    -h/--help                   Show this message.
    -v                          Be verbose.
EOF
    return 0
}

function parseArguments()
{
    OPTS=`getopt -o hv -l help,sq-report:,sq-url:,git-ref: -- "$@"`

    eval set -- "$OPTS"

    while [ $# -gt 0 ]
    do
        case $1 in
            -h | --help)
                usage
                exit 0
                ;;
            --sq-report)
                SONARQUBE_REPORT=$2
                shift; shift
                ;;
            --sq-url)
                SONARQUBE_URL=$2
                shift; shift
                ;;
            --git-ref)
                GIT_REF=$2
                shift; shift
                ;;
            -v)
                BE_VERBOSE='y'
                set -x
                shift;
                ;;
            --)
                shift
                break
                ;;
            (-*)
                echo "$0: unrecognized option $1" 1>&2
                exit 1
                (*)
                break
                ;;
        esac
    done
    return 0
}

function getTaskDetails ()
{

    [[ -f "${TASKDETAILS}" ]] && rm "${TASKDETAILS}"
    wget ${ceTaskUrl} -O "${TASKDETAILS}"
    return $?
}

function getAnalysisID ()
{
    local analysis_id=`python -c "import json; fd=open('"${TASKDETAILS}"'); answer=json.loads(fd.readline()); print answer['task']['analysisId']; fd.close()"`
    [[ -z "$analysis_id" ]] && echo "AnalysisId not found" && cat "${TASKDETAILS}" && return 1;
    echo ${analysis_id}
    return 0
}

#####################
#### MAIN METHOD ####
#####################
parseArguments $@

source ${SONARQUBE_REPORT}
getTaskDetails
ANALYSIS_ID=$(getAnalysisID)

curl \
    -i -X "POST" -u admin:admin \
    ${SONARQUBE_URL}/api/project_analyses/create_event?analysis=${ANALYSIS_ID}\&category=OTHER\&name=${GIT_REF}

