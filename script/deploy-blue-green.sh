#!/usr/bin/env bash

set -Eeuo pipefail

APP_NAME="${APP_NAME:-amumal-be}"
DEPLOY_DIR="${DEPLOY_DIR:-/home/ubuntu/deploy}"
ENV_FILE="${ENV_FILE:-${DEPLOY_DIR}/spring.env}"
ROLLBACK_SCRIPT="${ROLLBACK_SCRIPT:-${DEPLOY_DIR}/rollback-blue-green.sh}"
SERVICE_URL_FILE="${SERVICE_URL_FILE:-/etc/nginx/conf.d/service-url.inc}"

BLUE_CONTAINER="${BLUE_CONTAINER:-amumal-be-blue}"
GREEN_CONTAINER="${GREEN_CONTAINER:-amumal-be-green}"
BLUE_PORT="${BLUE_PORT:-8080}"
GREEN_PORT="${GREEN_PORT:-8081}"
BLUE_COMPOSE_FILE="${BLUE_COMPOSE_FILE:-${DEPLOY_DIR}/docker-compose.blue.yaml}"
GREEN_COMPOSE_FILE="${GREEN_COMPOSE_FILE:-${DEPLOY_DIR}/docker-compose.green.yaml}"

# actuator health endpoint를 배포 상태 확인에 사용한다.
HEALTH_PATH="${HEALTH_PATH:-/v1/actuator/health}"
MAX_RETRIES="${MAX_RETRIES:-10}"
HEALTH_INTERVAL_SECONDS="${HEALTH_INTERVAL_SECONDS:-3}"
CURL_MAX_TIME_SECONDS="${CURL_MAX_TIME_SECONDS:-3}"

PRUNE_AFTER_DEPLOY="${PRUNE_AFTER_DEPLOY:-false}"
NOTIFY_SUCCESS_URL="${NOTIFY_SUCCESS_URL:-}"

BEFORE_COMPOSE_COLOR=""
AFTER_COMPOSE_COLOR=""
BEFORE_PORT=""
AFTER_PORT=""
BEFORE_COMPOSE_FILE=""
AFTER_COMPOSE_FILE=""


validate_files() {
  [[ -f "$ENV_FILE" ]] || {
    echo "env file does not exist: ${ENV_FILE}"
    exit 1
  }

  [[ -f "$BLUE_COMPOSE_FILE" ]] || {
    echo "blue compose file does not exist: ${BLUE_COMPOSE_FILE}"
    exit 1
  }

  [[ -f "$GREEN_COMPOSE_FILE" ]] || {
    echo "green compose file does not exist: ${GREEN_COMPOSE_FILE}"
    exit 1
  }

  [[ -x "$ROLLBACK_SCRIPT" ]] || {
    echo "rollback script does not exist or is not executable: ${ROLLBACK_SCRIPT}"
    exit 1
  }
}


# 컨테이너 상태 체크
health_check() {
  local url="$1"
  local label="${2:-service}"
  local retries=0
  local http_code=""

  while [ "$retries" -lt "$MAX_RETRIES" ]; do
    echo "Checking ${label} at ${url}... (attempt: $((retries + 1))/${MAX_RETRIES})"

    http_code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time "$CURL_MAX_TIME_SECONDS" "$url" 2>/dev/null || true)
    echo "status=${http_code}"

    if [[ "$http_code" =~ ^2[0-9][0-9]$ || "$http_code" =~ ^3[0-9][0-9]$ ]]; then
      echo "health check success"
      return 0
    fi

    retries=$((retries + 1))
    sleep "$HEALTH_INTERVAL_SECONDS"
  done

  echo "Failed to check ${label} after ${MAX_RETRIES} attempts."
  return 1
}

down_container() {
  echo "### ${BEFORE_COMPOSE_COLOR} DOWN(port:${BEFORE_PORT}) ###"

  BACKEND_PORT="$BEFORE_PORT" \
    sudo docker-compose -p "${APP_NAME}-dev-${BEFORE_COMPOSE_COLOR}" \
    --env-file "$ENV_FILE" \
    -f "$BEFORE_COMPOSE_FILE" \
    down || true
}

validate_files

# 1. blue 컨테이너 실행 여부 확인 후 inactive 색상 컨테이너 실행
IS_BLUE=$(sudo docker ps --format '{{.Names}}' | grep -x "$BLUE_CONTAINER" || true)

if [ -z "$IS_BLUE" ]; then
  echo "### GREEN => BLUE ###"

  BEFORE_COMPOSE_COLOR="green"
  AFTER_COMPOSE_COLOR="blue"
  BEFORE_PORT="$GREEN_PORT"
  AFTER_PORT="$BLUE_PORT"
  BEFORE_COMPOSE_FILE="$GREEN_COMPOSE_FILE"
  AFTER_COMPOSE_FILE="$BLUE_COMPOSE_FILE"
else
  echo "### BLUE => GREEN ###"

  BEFORE_COMPOSE_COLOR="blue"
  AFTER_COMPOSE_COLOR="green"
  BEFORE_PORT="$BLUE_PORT"
  AFTER_PORT="$GREEN_PORT"
  BEFORE_COMPOSE_FILE="$BLUE_COMPOSE_FILE"
  AFTER_COMPOSE_FILE="$GREEN_COMPOSE_FILE"
fi

if [[ -n "${BACKEND_IMAGE:-}" ]]; then
  echo "pull backend image: ${BACKEND_IMAGE}"
  sudo docker pull "$BACKEND_IMAGE"
fi

BACKEND_PORT="$AFTER_PORT" \
  sudo docker-compose -p "${APP_NAME}-dev-${AFTER_COMPOSE_COLOR}" \
  --env-file "$ENV_FILE" \
  -f "$AFTER_COMPOSE_FILE" \
  up -d

echo "### ${AFTER_COMPOSE_COLOR} UP(port:${AFTER_PORT}) ###"

# 2. 새 컨테이너 직접 health check
if ! health_check "http://127.0.0.1:${AFTER_PORT}${HEALTH_PATH}" "${AFTER_COMPOSE_COLOR} direct"; then
  echo "### DEPLOY FAILED BEFORE NGINX SWITCH ###"
  exit 1
fi

# 3. nginx service-url.inc 전환 후 reload
echo "### NGINX SERVICE URL SWITCH: ${BEFORE_COMPOSE_COLOR}(port:${BEFORE_PORT}) => ${AFTER_COMPOSE_COLOR}(port:${AFTER_PORT}) ###"

TMP_SERVICE_URL_FILE=$(mktemp)
printf 'set $service_url http://127.0.0.1:%s; # active=%s\n' "$AFTER_PORT" "$AFTER_COMPOSE_COLOR" > "$TMP_SERVICE_URL_FILE"

sudo mkdir -p "$(dirname "$SERVICE_URL_FILE")"
sudo install -m 0644 "$TMP_SERVICE_URL_FILE" "$SERVICE_URL_FILE"
rm -f "$TMP_SERVICE_URL_FILE"

if ! sudo nginx -t; then
  echo "### NGINX CONFIG TEST FAILED ###"
  exit 1
fi

if ! sudo systemctl reload nginx; then
  echo "### NGINX RELOAD FAILED ###"
  exit 1
fi

# 4. nginx 경유 health check
if ! health_check "http://127.0.0.1${HEALTH_PATH}" "${AFTER_COMPOSE_COLOR} nginx"; then
  echo "### DEPLOY FAILED AFTER NGINX SWITCH ###"
  exit 1
fi

# 5. nginx 경유 안정성 검증 성공 후 기존 컨테이너 down
down_container

# 안 쓰는 도커 이미지 정리
sudo docker image prune -f || true
echo "### DEPLOY COMPLETED: ${AFTER_COMPOSE_COLOR}(port:${AFTER_PORT}) ###"
