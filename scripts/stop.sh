#!/usr/bin/env bash

# 프로젝트 경로
PROJECT_ROOT="/home/ec2-user/Deploy-Test"
JAR_FILE="$PROJECT_ROOT/spring-webapp.jar"

# 다중 로그 경로
DEPLOY_LOG="$PROJECT_ROOT/deploy.app"

TIME_NOW=$(date + %c)

# 현재 실행중인 애플리케이션의 PID 확인
CURRENT_PID=$(pgrep -f $JAR_FILE)

# 프로세스가 켜져있으면 종료
if [ -z $CURRENT_PID ]; then # PID가 없으면
  echo "$TIME_NOW > 현재 실행중인 애플리케이션이 없습니다." >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행중인 애플리케이션을 종료합니다. PID = $CURRENT_PID" >> $DEPLOY_LOG
  kill -15 $CURRENT_PID
fi
