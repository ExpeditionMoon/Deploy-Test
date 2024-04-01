#!/usr/bin/env bash

# 프로젝트 경로
PROJECT_ROOT="/home/ec2-user/Deploy-Test"
JAR_FILE="$PROJECT_ROOT/spring-webapp.jar"

# 다중 로그 경로
APP_LOG="$PROJECT_ROOT/application.app"
ERROR_LOG="$PROJECT_ROOT/error.app"
DEPLOY_LOG="$PROJECT_ROOT/deploy.app"

TIME_NOW=$(date + %c)

# build 파일을 정해진 이름으로 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사 " >> $DEPLOY_LOG # (생략가능)
cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE

# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행 " >> $DEPLOY_LOG # (생략가능)
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG & # > $APP_LOG 2> $ERROR_LOG : 로그 출력하고 싶을 경우 추가하는 것

# 결과 출력 (생략 가능)
CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행 완료. PID = $CURRENT_PID " >> $DEPLOY_LOG
