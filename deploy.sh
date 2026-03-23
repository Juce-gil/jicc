cd /www/wwwroot/jicc
cat > deploy.sh <<'EOF'
#!/usr/bin/env bash
set -Eeuo pipefail

# One-click deploy script for this project.
# Usage:
#   chmod +x deploy.sh
#   SERVER_IP=39.104.93.85 MYSQL_ROOT_PASSWORD='123456' bash deploy.sh
# Optional vars:
#   DB_NAME=campus
#   DB_USER=root
#   DB_PASSWORD=123456
#   SERVICE_NAME=campus-product
#   SPRING_PROFILE=prod
#   BACKEND_PORT=21090
#   INIT_DB_MODE=if_missing   # never / if_missing / always
#   INSTALL_DEPS=true         # true / false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="${APP_ROOT:-$SCRIPT_DIR}"
SERVER_IP="${SERVER_IP:-39.104.93.85}"
DB_NAME="${DB_NAME:-campus}"
DB_USER="${DB_USER:-root}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
DB_PASSWORD="${DB_PASSWORD:-$MYSQL_ROOT_PASSWORD}"
SERVICE_NAME="${SERVICE_NAME:-campus-product}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
BACKEND_PORT="${BACKEND_PORT:-21090}"
CONTEXT_PATH="${CONTEXT_PATH:-/api/campus-product-sys/v1.0}"
INIT_DB_MODE="${INIT_DB_MODE:-if_missing}"
INSTALL_DEPS="${INSTALL_DEPS:-true}"
UPLOAD_DIR="${UPLOAD_DIR:-$APP_ROOT/upload}"
LOG_DIR="${LOG_DIR:-$APP_ROOT/logs}"
NGINX_CONF="/etc/nginx/conf.d/${SERVICE_NAME}.conf"
SYSTEMD_SERVICE="/etc/systemd/system/${SERVICE_NAME}.service"
WEB_USER_DIR="/usr/share/nginx/html/user"
WEB_ADMIN_DIR="/usr/share/nginx/html/admin"
BACKEND_DIR="$APP_ROOT/campus-product-sys"
USER_FRONTEND_DIR="$APP_ROOT/frontend-user"
ADMIN_FRONTEND_DIR="$APP_ROOT/frontend-admin"
INIT_SQL="$APP_ROOT/db/init.sql"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
err() { echo -e "${RED}[ERROR]${NC} $*"; }
ok() { echo -e "${GREEN}[OK]${NC} $*"; }

trap 'err "Script failed near line ${LINENO}."' ERR

require_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    err "Please run as root. Example: sudo SERVER_IP=${SERVER_IP} MYSQL_ROOT_PASSWORD=123456 bash deploy.sh"
    exit 1
  fi
}

check_project_layout() {
  [[ -d "$BACKEND_DIR" ]] || { err "Missing backend dir: $BACKEND_DIR"; exit 1; }
  [[ -d "$USER_FRONTEND_DIR" ]] || { err "Missing user frontend dir: $USER_FRONTEND_DIR"; exit 1; }
  [[ -d "$ADMIN_FRONTEND_DIR" ]] || { err "Missing admin frontend dir: $ADMIN_FRONTEND_DIR"; exit 1; }
  [[ -f "$INIT_SQL" ]] || warn "Missing init.sql: $INIT_SQL . DB init may be skipped."
}

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

get_pkg_manager() {
  if command_exists apt-get; then
    echo apt
  elif command_exists yum; then
    echo yum
  else
    echo unknown
  fi
}

ensure_node18() {
  local pm="$1"
  local major="0"

  if command_exists node; then
    major="$(node -v | sed 's/^v//' | cut -d. -f1)"
  fi

  if command_exists node && [[ "$major" -ge 18 ]]; then
    ok "Node.js version is OK: $(node -v)"
    return 0
  fi

  warn "Node.js is missing or version is below 18. Installing Node.js 18..."
  if [[ "$pm" == "apt" ]]; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
  elif [[ "$pm" == "yum" ]]; then
    curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
    yum install -y nodejs
  else
    err "Unknown package manager. Cannot install Node.js 18 automatically."
    exit 1
  fi
}

install_dependencies() {
  if [[ "$INSTALL_DEPS" != "true" ]]; then
    warn "Skipping dependency installation because INSTALL_DEPS=false"
    return 0
  fi

  local pm
  pm="$(get_pkg_manager)"

  case "$pm" in
    apt)
      export DEBIAN_FRONTEND=noninteractive
      log "Installing dependencies with apt..."
      apt-get update
      apt-get install -y software-properties-common curl ca-certificates gnupg lsb-release git nginx maven python3 mysql-server
      if ! command_exists java; then
        if apt-cache show openjdk-17-jdk >/dev/null 2>&1; then
          apt-get install -y openjdk-17-jdk
        else
          apt-get install -y openjdk-8-jdk
        fi
      fi
      ensure_node18 apt
      systemctl enable nginx || true
      systemctl start nginx || true
      systemctl enable mysql || true
      systemctl start mysql || true
      ;;
    yum)
      log "Installing dependencies with yum..."
      yum install -y epel-release || true
      yum install -y curl ca-certificates git nginx maven python3 mariadb-server
      if ! command_exists java; then
        yum install -y java-17-openjdk-devel || yum install -y java-1.8.0-openjdk-devel
      fi
      ensure_node18 yum
      systemctl enable nginx || true
      systemctl start nginx || true
      systemctl enable mariadb || true
      systemctl start mariadb || true
      systemctl enable mysqld || true
      systemctl start mysqld || true
      ;;
    *)
      err "Unsupported system. Neither apt-get nor yum was found."
      exit 1
      ;;
  esac

  ok "Dependencies are ready"
}

MYSQL_BIN=""
MYSQL_AUTH=()

setup_mysql_cli() {
  if command_exists mysql; then
    MYSQL_BIN="$(command -v mysql)"
  elif command_exists mariadb; then
    MYSQL_BIN="$(command -v mariadb)"
  else
    err "MySQL/MariaDB client not found"
    exit 1
  fi

  if [[ -n "$MYSQL_ROOT_PASSWORD" ]]; then
    if "$MYSQL_BIN" -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_AUTH=(-uroot -p"$MYSQL_ROOT_PASSWORD")
      ok "Connected to MySQL using provided root password"
      return 0
    else
      warn "Provided MYSQL_ROOT_PASSWORD did not work. Trying root without password..."
    fi
  fi

  if "$MYSQL_BIN" -uroot -e "SELECT 1;" >/dev/null 2>&1; then
    MYSQL_AUTH=(-uroot)
    ok "Connected to MySQL as root without password"
    return 0
  fi

  err "Cannot connect to MySQL. Make sure MySQL is running and MYSQL_ROOT_PASSWORD is correct."
  exit 1
}

mysql_exec() {
  "$MYSQL_BIN" "${MYSQL_AUTH[@]}" "$@"
}

patch_pom() {
  local pom="$BACKEND_DIR/pom.xml"
  [[ -f "$pom" ]] || { err "Missing pom.xml: $pom"; exit 1; }

  log "Patching pom.xml ..."
  python3 - <<PY
from pathlib import Path
pom = Path(r'''$pom''')
text = pom.read_text(encoding='utf-8')
original = text
text = text.replace('<news.build.sourceEncoding>UTF-8</news.build.sourceEncoding>如何修改', '<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>')
text = text.replace('<news.build.sourceEncoding>UTF-8</news.build.sourceEncoding>', '<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>')
if '<project.build.sourceEncoding>' not in text and '<properties>' in text:
    text = text.replace('<properties>', '<properties>\n        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>', 1)
if 'spring-boot-maven-plugin' not in text:
    plugin = '''
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>'''
    text = text.replace('</plugins>', plugin + '\n        </plugins>', 1)
if text != original:
    pom.write_text(text, encoding='utf-8')
PY
  ok "pom.xml patched"
}

patch_file_controller() {
  local file="$BACKEND_DIR/src/main/java/cn/kmbeast/controller/FileController.java"
  if [[ ! -f "$file" ]]; then
    warn "FileController.java not found: $file"
    return 0
  fi

  log "Patching FileController.java ..."
  python3 - <<PY
from pathlib import Path
import re
file = Path(r'''$file''')
text = file.read_text(encoding='utf-8')
new = re.sub(r'private\s+final\s+static\s+String\s+URL\s*=\s*"[^"]*"\s*;', 'private final static String URL = "http://$SERVER_IP";', text)
if new != text:
    file.write_text(new, encoding='utf-8')
PY
  ok "FileController.java patched"
}

write_application_prod() {
  local file="$BACKEND_DIR/src/main/resources/application-${SPRING_PROFILE}.yml"
  log "Writing application-${SPRING_PROFILE}.yml ..."
  cat > "$file" <<EOF2
server:
  port: ${BACKEND_PORT}
  servlet:
    context-path: ${CONTEXT_PATH}

my-server:
  api-context-path: ${CONTEXT_PATH}

spring:
  application:
    name: campus-trade-backend
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 20MB
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/${DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USER}
    password: ${DB_PASSWORD}

mybatis:
  mapper-locations: classpath:mapper/*.xml

jwt:
  secret: campus-trade-demo-secret
  expire-hours: 72

trade:
  upload-dir: ${UPLOAD_DIR}
  frontend-user-base-url: http://${SERVER_IP}
  frontend-admin-base-url: http://${SERVER_IP}/admin
  websocket-path: /ws/chat

minio:
  endpoint: http://127.0.0.1:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: campus-trade
EOF2
  ok "application-${SPRING_PROFILE}.yml created"
}

prepare_dirs() {
  mkdir -p "$UPLOAD_DIR" "$LOG_DIR" "$WEB_USER_DIR" "$WEB_ADMIN_DIR"
  ok "Directories prepared"
}

init_database() {
  if [[ ! -f "$INIT_SQL" ]]; then
    warn "init.sql not found, skipping DB init"
    return 0
  fi

  log "Ensuring database exists..."
  mysql_exec -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

  local table_count
  table_count="$(mysql_exec -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';" | tr -d '[:space:]')"
  table_count="${table_count:-0}"

  case "$INIT_DB_MODE" in
    always)
      warn "INIT_DB_MODE=always, re-importing init.sql"
      mysql_exec "$DB_NAME" < "$INIT_SQL"
      ;;
    if_missing)
      if [[ "$table_count" == "0" ]]; then
        log "Database is empty, importing init.sql ..."
        mysql_exec "$DB_NAME" < "$INIT_SQL"
      else
        warn "Database ${DB_NAME} already has tables, skipping init.sql import"
      fi
      ;;
    never)
      warn "INIT_DB_MODE=never, skipping init.sql import"
      ;;
    *)
      err "Invalid INIT_DB_MODE: $INIT_DB_MODE"
      exit 1
      ;;
  esac

  log "Replacing localhost image URLs in DB..."
  mysql_exec -D "$DB_NAME" -e "UPDATE product SET cover_list = REPLACE(cover_list, 'http://localhost:21090', 'http://${SERVER_IP}') WHERE cover_list LIKE '%http://localhost:21090%';" || true
  ok "Database step completed"
}

build_backend() {
  log "Building backend..."
  cd "$BACKEND_DIR"
  mvn clean package -DskipTests
  [[ -f "$BACKEND_DIR/target/campus-product-api-1.0-SNAPSHOT.jar" ]] || { err "Backend jar not found"; exit 1; }
  ok "Backend build completed"
}

build_frontends() {
  log "Building user frontend..."
  cd "$USER_FRONTEND_DIR"
  npm install
  npm run build
  [[ -d "$USER_FRONTEND_DIR/dist" ]] || { err "User frontend dist not found"; exit 1; }

  log "Building admin frontend..."
  cd "$ADMIN_FRONTEND_DIR"
  npm install
  npm run build
  [[ -d "$ADMIN_FRONTEND_DIR/dist" ]] || { err "Admin frontend dist not found"; exit 1; }

  ok "Frontend builds completed"
}

deploy_frontends() {
  log "Deploying frontend static files..."
  rm -rf "$WEB_USER_DIR"/* "$WEB_ADMIN_DIR"/*
  cp -r "$USER_FRONTEND_DIR/dist/"* "$WEB_USER_DIR/"
  cp -r "$ADMIN_FRONTEND_DIR/dist/"* "$WEB_ADMIN_DIR/"
  ok "Frontend files deployed"
}

write_nginx_conf() {
  log "Writing Nginx config to $NGINX_CONF ..."
  cat > "$NGINX_CONF" <<EOF2
server {
    listen 80;
    server_name ${SERVER_IP};

    client_max_body_size 20m;

    location / {
        root /usr/share/nginx/html/user;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }

    location /admin/ {
        alias /usr/share/nginx/html/admin/;
        index index.html;
        try_files \$uri \$uri/ /admin/index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:${BACKEND_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:${BACKEND_PORT};
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF2

  rm -f /etc/nginx/sites-enabled/default /etc/nginx/conf.d/default.conf || true
  nginx -t
  systemctl restart nginx
  ok "Nginx configured"
}

write_systemd_service() {
  local java_bin
  java_bin="$(command -v java)"
  [[ -n "$java_bin" ]] || { err "java not found"; exit 1; }

  log "Writing systemd service to $SYSTEMD_SERVICE ..."
  cat > "$SYSTEMD_SERVICE" <<EOF2
[Unit]
Description=Campus Product Spring Boot App
After=network.target mysql.service mariadb.service mysqld.service

[Service]
User=root
WorkingDirectory=${BACKEND_DIR}
ExecStart=${java_bin} -jar ${BACKEND_DIR}/target/campus-product-api-1.0-SNAPSHOT.jar --spring.profiles.active=${SPRING_PROFILE}
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=append:${LOG_DIR}/backend.log
StandardError=append:${LOG_DIR}/backend.log

[Install]
WantedBy=multi-user.target
EOF2

  systemctl daemon-reload
  systemctl enable "$SERVICE_NAME"
  systemctl restart "$SERVICE_NAME"
  ok "systemd service started"
}

show_status() {
  echo
  ok "Deployment finished"
  echo "--------------------------------------------------"
  systemctl --no-pager --full status "$SERVICE_NAME" || true
  echo "--------------------------------------------------"
  systemctl --no-pager --full status nginx || true
  echo "--------------------------------------------------"
  echo "User site:   http://${SERVER_IP}/"
  echo "Admin site:  http://${SERVER_IP}/admin/"
  echo "API prefix:  http://${SERVER_IP}${CONTEXT_PATH}"
  echo
  echo "If the site cannot be opened, check:"
  echo "1. Security group allows port 80"
  echo "2. systemctl status ${SERVICE_NAME}"
  echo "3. journalctl -u ${SERVICE_NAME} -f"
  echo "4. nginx -t && systemctl status nginx"
}

main() {
  require_root
  check_project_layout
  install_dependencies
  setup_mysql_cli
  prepare_dirs
  patch_pom
  patch_file_controller
  write_application_prod
  init_database
  build_backend
  build_frontends
  deploy_frontends
  write_nginx_conf
  write_systemd_service
  show_status
}

main "$@"
EOF
