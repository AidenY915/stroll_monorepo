# Stroll 서비스 배포 및 실행을 위한 Justfile
# stroll_chatbot은 제외한 나머지 서비스들을 관리합니다.

# 변수 설정
api_server_dir := "./services/stroll_api_server"
airflow_dir := "./services/stroll_airflow"
api_compose := "compose.api_server.yml"
airflow_compose := "compose.airflow.yml"
network_name := "stroll-network"
api_image := "stroll-api"
airflow_image := "stroll-airflow-scheduler"
jar_name := "stroll-api-0.0.1-SNAPSHOT.jar"

# 기본 명령어 (help 표시)
default:
	@just --list

# ============================================================================
# 네트워크 관리
# ============================================================================

# Docker 네트워크 생성 (이미 존재하면 무시)
network-create:
	@echo "stroll-network 생성 중..."
	@docker network inspect {{network_name}} > /dev/null 2>&1 || docker network create {{network_name}}

# Docker 네트워크 삭제
network-remove:
	@echo "stroll-network 삭제 중..."
	@docker network rm {{network_name}} 2>/dev/null || echo "네트워크가 존재하지 않습니다."

# ============================================================================
# API 서버 빌드 및 배포
# ============================================================================

# API 서버 Gradle 빌드 (JAR 파일 생성)
api-build:
	@echo "API 서버 JAR 빌드 중..."; \
	cd {{api_server_dir}}; \
	if [ -f "./gradlew" ]; then \
		./gradlew clean bootJar; \
	elif [ -f "./gradlew.bat" ]; then \
		./gradlew.bat clean bootJar; \
	else \
		echo "gradlew를 찾을 수 없습니다."; \
		exit 1; \
	fi;
	@echo "✅ API 서버 JAR 빌드 완료";

# API 서버 Docker 이미지 빌드
api-image: api-build
	@echo "API 서버 Docker 이미지 빌드 중..."
	@docker compose -f {{api_compose}} build
	@echo "✅ API 서버 Docker 이미지 빌드 완료"

# API 서버 실행
api-up: network-create
	@echo "API 서버 실행 중..."
	@docker compose -f {{api_compose}} up -d
	@echo "✅ API 서버 실행 완료 (http://localhost:8080)"

# API 서버 중지
api-down:
	@echo "🛑 API 서버 중지 중..."
	@docker compose -f {{api_compose}} down
	@echo "✅ API 서버 중지 완료"

# API 서버 재시작
api-restart: api-down api-up
	@echo "✅ API 서버 재시작 완료"

# API 서버 로그 확인
api-logs:
	@docker compose -f {{api_compose}} logs -f

# API 서버 상태 확인
api-status:
	@docker compose -f {{api_compose}} ps

# ============================================================================
# Airflow 빌드 및 배포
# ============================================================================

# Airflow Docker 이미지 빌드 (scheduler와 webserver 동일 이미지 사용)
airflow-image:
	@echo "Airflow Docker 이미지 빌드 중..."
	@docker build -t {{airflow_image}} -f {{airflow_dir}}/stroll_airflow_scheduler_dockerfile {{airflow_dir}}
	@echo "✅ Airflow Docker 이미지 빌드 완료"

# Airflow 초기화 (DB 초기화 및 관리자 계정 생성)
airflow-init: network-create airflow-image
	@echo "Airflow 초기화 중..."
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} run --rm airflow-init
	@echo "✅ Airflow 초기화 완료"

# Airflow 실행
airflow-up: network-create airflow-image
	@echo "Airflow 실행 중..."
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} up -d scheduler webserver
	@echo "✅ Airflow 실행 완료 (http://localhost:5000)"

# Airflow 중지
airflow-down:
	@echo "🛑 Airflow 중지 중..."
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} down
	@echo "✅ Airflow 중지 완료"

# Airflow 재시작
airflow-restart: airflow-down airflow-up
	@echo "✅ Airflow 재시작 완료"

# Airflow 로그 확인
airflow-logs:
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} logs -f

# Airflow 상태 확인
airflow-status:
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} ps

# Airflow Postgres 데이터 초기화 (주의: 데이터 삭제됨)
airflow-clean-db:
	@echo "※ Airflow Postgres 데이터 삭제 중..."
	@echo "정말로 데이터를 삭제하시겠습니까? (y/N)"
	#!/usr/bin/env sh
	read confirm
	if [ "$$confirm" = "y" ] || [ "$$confirm" = "Y" ]; then \
		cd {{airflow_dir}}  \
		rm -rf postgres_data/*  \
		echo "✅ 데이터 삭제 완료"  \
	else	\
		echo "취소되었습니다."  \
	fi

# ============================================================================
# 전체 서비스 관리
# ============================================================================

# 모든 서비스 빌드
build-all: api-image airflow-image
	@echo "✅ 모든 서비스 빌드 완료"

# 모든 서비스 실행
up-all: network-create api-up airflow-up
	@echo "✅ 모든 서비스 실행 완료"
	@echo ""
	@echo "서비스 접속 정보:"
	@echo "  - API 서버: http://localhost:8080"
	@echo "  - Airflow: http://localhost:5000"

# 모든 서비스 중지
down-all: api-down airflow-down
	@echo "✅ 모든 서비스 중지 완료"

# 모든 서비스 재시작
restart-all: down-all up-all
	@echo "✅ 모든 서비스 재시작 완료"

# 전체 배포 (빌드 + 실행)
deploy: build-all up-all
	@echo "전체 배포 완료!"

# 전체 배포 (초기화 포함, Airflow는 초기화 필수)
deploy-init: network-create api-image airflow-image airflow-init
	@echo "전체 서비스 실행 중..."
	@docker compose -f {{api_compose}} up -d
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} up -d scheduler webserver
	@echo "전체 배포 완료!"
	@echo ""
	@echo "서비스 접속 정보:"
	@echo "  - API 서버: http://localhost:8080"
	@echo "  - Airflow: http://localhost:5000"

# 모든 서비스 상태 확인
status-all:
	@echo "API 서버 상태:"
	@docker compose -f {{api_compose}} ps
	@echo ""
	@echo "Airflow 상태:"
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} ps

# 모든 서비스 로그 확인 (API 서버)
logs-api:
	@docker compose -f {{api_compose}} logs -f

# 모든 서비스 로그 확인 (Airflow)
logs-airflow:
	#!/usr/bin/env sh
	cd {{airflow_dir}}
	docker compose -f ../../{{airflow_compose}} logs -f

# ============================================================================
# 유틸리티
# ============================================================================

# 모든 Docker 이미지 삭제 (주의: 모든 stroll 관련 이미지 삭제)
clean-images:
	@echo "Docker 이미지 삭제 중..."
	@docker rmi {{api_image}} {{airflow_image}} 2>/dev/null || echo "이미지가 존재하지 않습니다."
	@echo "✅ 이미지 삭제 완료"

# 모든 컨테이너 및 이미지 정리
clean-all: down-all clean-images
	@echo "전체 정리 완료"

# 네트워크 확인
network-status:
	@docker network inspect {{network_name}} || echo "네트워크가 존재하지 않습니다."

