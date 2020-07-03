SHELL := /bin/bash

PROJECT_NAME ?= spring-kafka-example
SERVICE ?= kafka

start:
	docker-compose --project-name=${PROJECT_NAME}  up -d --build
	docker-compose --project-name=${PROJECT_NAME} logs -f

stop:
	docker-compose --project-name=${PROJECT_NAME} down --remove-orphans -v

ssh:
	docker-compose --project-name=${PROJECT_NAME} exec ${SERVICE} bash;

start-multiple-broker:
	docker-compose --project-name=${PROJECT_NAME} -f docker-compose-multiple.yml up -d --scale kafka=3
	docker-compose --project-name=${PROJECT_NAME} -f docker-compose-multiple.yml logs -f

stop-multiple-broker:
	docker-compose --project-name=${PROJECT_NAME} -f docker-compose-multiple.yml down -v

ssh-multiple:
	docker-compose --project-name=${PROJECT_NAME}  -f docker-compose-multiple.yml exec ${SERVICE} bash

clean-project: 
	-docker kill $$(docker ps -qa -f "name=${PROJECT_NAME}*") 2>/dev/null || true
	-docker rm $$(docker ps -qa -f "name=${PROJECT_NAME}*") 2>/dev/null || true
	-docker rmi -f $$(docker images "${PROJECT_NAME}*" -qa) 2>/dev/null || true
	-docker rmi -f $$(docker images -f 'dangling=true' -qa) 2>/dev/null || true
	-docker network rm $$(docker network ls -q -f "name=${PROJECT_NAME}*") 2>/dev/null || true
	-yes | docker volume prune 2>/dev/null || true
