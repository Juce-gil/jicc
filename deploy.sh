#!/usr/bin/env bash
set -Eeuo pipefail

########################################
# 鏍″洯浜屾墜浜ゆ槗椤圭洰涓€閿儴缃茶剼鏈?# 浣跨敤鏂瑰紡锛?#   chmod +x deploy.sh
#   SERVER_IP=39.104.93.85 MYSQL_ROOT_PASSWORD='浣犵殑瀵嗙爜' bash deploy.sh
#
# 鍙€夌幆澧冨彉閲忥細
#   SERVER_IP=39.104.93.85
#   MYSQL_ROOT_PASSWORD=浣犵殑root瀵嗙爜
#   DB_NAME=campus
#   DB_USER=root
#   DB_PASSWORD=浣犵殑搴旂敤鏁版嵁搴撳瘑鐮侊紙榛樿璺?MYSQL_ROOT_PASSWORD 涓€鑷达級
#   APP_ROOT=/www/wwwroot/jicc
#   SERVICE_NAME=campus-product
#   INIT_DB_MODE=if_missing   # never / if_missing / always
#   INSTALL_DEPS=true         # true / false
########################################

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

trap 'err "鑴氭湰鎵ц澶辫触锛屽嚭閿欎綅缃細绗?${LINENO} 琛屻€?' ERR

require_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    err "璇蜂娇鐢?root 鎵ц璇ヨ剼鏈紝渚嬪锛歴udo SERVER_IP=${SERVER_IP} MYSQL_ROOT_PASSWORD=浣犵殑瀵嗙爜 bash deploy.sh"
    exit 1
  fi
}

check_project_layout() {
  [[ -d "$BACKEND_DIR" ]] || { err "鏈壘鍒板悗绔洰褰曪細$BACKEND_DIR"; exit 1; }
  [[ -d "$USER_FRONTEND_DIR" ]] || { err "鏈壘鍒扮敤鎴峰墠绔洰褰曪細$USER_FRONTEND_DIR"; exit 1; }
  [[ -d "$ADMIN_FRONTEND_DIR" ]] || { err "鏈壘鍒扮鐞嗗墠绔洰褰曪細$ADMIN_FRONTEND_DIR"; exit 1; }
  [[ -f "$INIT_SQL" ]] || warn "鏈壘鍒版暟鎹簱鍒濆鍖栬剼鏈細$INIT_SQL锛屽悗缁皢璺宠繃瀵煎簱銆?
}

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

get_pkg_manager() {
  if command_exists apt-get; then
    echo "apt"
  elif command_exists yum; then
    echo "yum"
  else
    echo "unknown"
  fi
}

ensure_node18() {
  local pm="$1"
  local major="0"
  if command_exists node; then
    major="$(node -v | sed 's/^v//' | cut -d. -f1)"
  fi

  if command_exists node && [[ "$major" -ge 18 ]]; then
    ok "Node.js 鐗堟湰婊¤冻瑕佹眰锛?(node -v)"
    return 0
  fi

  warn "褰撳墠 Node.js 涓嶅瓨鍦ㄦ垨鐗堟湰浣庝簬 18锛屽紑濮嬪畨瑁?Node.js 18..."
  if [[ "$pm" == "apt" ]]; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
  elif [[ "$pm" == "yum" ]]; then
    curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
    yum install -y nodejs
  else
    err "鏃犳硶璇嗗埆鍖呯鐞嗗櫒锛屾棤娉曡嚜鍔ㄥ畨瑁?Node.js 18"
    exit 1
  fi
}

install_dependencies() {
  [[ "$INSTALL_DEPS" == "true" ]] || { warn "宸茶烦杩囦緷璧栧畨瑁咃紙INSTALL_DEPS=false锛?; return 0; }

  local pm
  pm="$(get_pkg_manager)"
  case "$pm" in
    apt)
      export DEBIAN_FRONTEND=noninteractive
      log "浣跨敤 apt 瀹夎渚濊禆..."
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
      log "浣跨敤 yum 瀹夎渚濊禆..."
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
      err "涓嶆敮鎸佺殑绯荤粺锛屾湭璇嗗埆鍒?apt-get 鎴?yum"
      exit 1
      ;;
  esac

  ok "鍩虹渚濊禆瀹夎瀹屾垚"
}

MYSQL_BIN=""
MYSQL_AUTH=()

setup_mysql_cli() {
  if command_exists mysql; then
    MYSQL_BIN="$(command -v mysql)"
  elif command_exists mariadb; then
    MYSQL_BIN="$(command -v mariadb)"
  else
    err "鏈壘鍒?mysql/mariadb 瀹㈡埛绔紝璇峰厛瀹夎鏁版嵁搴撳鎴风"
    exit 1
  fi

  if [[ -n "$MYSQL_ROOT_PASSWORD" ]]; then
    if "$MYSQL_BIN" -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_AUTH=(-uroot -p"$MYSQL_ROOT_PASSWORD")
      ok "浣跨敤 root 瀵嗙爜杩炴帴 MySQL 鎴愬姛"
      return 0
    else
      warn "浣跨敤鎻愪緵鐨?MYSQL_ROOT_PASSWORD 杩炴帴澶辫触锛屽皢灏濊瘯鍏嶅瘑 root 杩炴帴"
    fi
  fi

  if "$MYSQL_BIN" -uroot -e "SELECT 1;" >/dev/null 2>&1; then
    MYSQL_AUTH=(-uroot)
    ok "浣跨敤鍏嶅瘑 root 杩炴帴 MySQL 鎴愬姛"
    return 0
  fi

  err "鏃犳硶杩炴帴 MySQL銆傝纭 MySQL 宸插惎鍔紝骞舵彁渚涙纭殑 MYSQL_ROOT_PASSWORD銆?
  exit 1
}

mysql_exec() {
  "$MYSQL_BIN" "${MYSQL_AUTH[@]}" "$@"
}

patch_pom() {
  local pom="$BACKEND_DIR/pom.xml"
  [[ -f "$pom" ]] || { err "鏈壘鍒?pom.xml锛?pom"; exit 1; }

  log "淇 pom.xml锛堢紪鐮侀厤缃?+ Spring Boot 鎵撳寘鎻掍欢锛?.."
  python3 - <<PY
from pathlib import Path
import re
pom = Path(r'''$pom''')
text = pom.read_text(encoding='utf-8')
original = text
text = text.replace('<news.build.sourceEncoding>UTF-8</news.build.sourceEncoding>', '<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>')
text = text.replace('<news.build.sourceEncoding>UTF-8</news.build.sourceEncoding>濡備綍淇敼', '<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>')
if '<project.build.sourceEncoding>' not in text and '<properties>' in text:
    text = text.replace('<properties>', '<properties>\n        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>', 1)
if 'spring-boot-maven-plugin' not in text:
    plugin = '''
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>'''
    text = text.replace('</plugins>', f'{plugin}\n        </plugins>', 1)
if text != original:
    pom.write_text(text, encoding='utf-8')
PY
  ok "pom.xml 澶勭悊瀹屾垚"
}

patch_file_controller() {
  local file="$BACKEND_DIR/src/main/java/cn/kmbeast/controller/FileController.java"
  [[ -f "$file" ]] || { warn "鏈壘鍒?FileController.java锛?file锛岃烦杩?; return 0; }

  log "淇 FileController.java 涓枃浠惰闂湴鍧€..."
  python3 - <<PY
from pathlib import Path
import re
file = Path(r'''$file''')
text = file.read_text(encoding='utf-8')
new = re.sub(r'private\s+final\s+static\s+String\s+URL\s*=\s*"[^"]*"\s*;', 'private final static String URL = "http://$SERVER_IP";', text)
if new != text:
    file.write_text(new, encoding='utf-8')
PY
  ok "FileController.java 澶勭悊瀹屾垚"
}

write_application_prod() {
  local file="$BACKEND_DIR/src/main/resources/application-${SPRING_PROFILE}.yml"
  log "鐢熸垚 application-${SPRING_PROFILE}.yml ..."
  cat > "$file" <<EOF
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
EOF
  ok "宸茬敓鎴?$file"
}

prepare_dirs() {
  mkdir -p "$UPLOAD_DIR" "$LOG_DIR" "$WEB_USER_DIR" "$WEB_ADMIN_DIR"
  ok "鐩綍鍑嗗瀹屾垚"
}

init_database() {
  [[ -f "$INIT_SQL" ]] || { warn "鏈壘鍒?init.sql锛岃烦杩囨暟鎹簱鍒濆鍖?; return 0; }

  log "鍒涘缓鏁版嵁搴擄紙鑻ヤ笉瀛樺湪锛?.."
  mysql_exec -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

  local table_count="0"
  table_count="$(mysql_exec -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';" | tr -d '[:space:]')"
  table_count="${table_count:-0}"

  case "$INIT_DB_MODE" in
    always)
      warn "INIT_DB_MODE=always锛屽皢閲嶆柊瀵煎叆鏁版嵁搴擄紝杩欏彲鑳借鐩栫幇鏈夋暟鎹?
      mysql_exec "$DB_NAME" < "$INIT_SQL"
      ;;
    if_missing)
      if [[ "$table_count" == "0" ]]; then
        log "妫€娴嬪埌鏁版嵁搴撲负绌猴紝寮€濮嬪鍏ュ垵濮嬪寲鑴氭湰..."
        mysql_exec "$DB_NAME" < "$INIT_SQL"
      else
        warn "鏁版嵁搴?${DB_NAME} 宸插瓨鍦ㄨ〃缁撴瀯锛岃烦杩?init.sql 瀵煎叆銆傚闇€寮哄埗閲嶅锛岃璁剧疆 INIT_DB_MODE=always"
      fi
      ;;
    never)
      warn "INIT_DB_MODE=never锛岃烦杩?init.sql 瀵煎叆"
      ;;
    *)
      err "鏈煡 INIT_DB_MODE锛?INIT_DB_MODE锛屽彲閫?never / if_missing / always"
      exit 1
      ;;
  esac

  log "淇鏁版嵁搴撲腑鐨?localhost 鍥剧墖鍦板潃..."
  mysql_exec -D "$DB_NAME" -e "UPDATE product SET cover_list = REPLACE(cover_list, 'http://localhost:21090', 'http://${SERVER_IP}') WHERE cover_list LIKE '%http://localhost:21090%';" || true
  ok "鏁版嵁搴撳鐞嗗畬鎴?
}

build_backend() {
  log "寮€濮嬫墦鍖呭悗绔?.."
  cd "$BACKEND_DIR"
  mvn clean package -DskipTests
  [[ -f "$BACKEND_DIR/target/campus-product-api-1.0-SNAPSHOT.jar" ]] || { err "鏈壘鍒板悗绔?jar 鍖?; exit 1; }
  ok "鍚庣鎵撳寘瀹屾垚"
}

build_frontends() {
  log "寮€濮嬫墦鍖呯敤鎴风鍓嶇..."
  cd "$USER_FRONTEND_DIR"
  npm install
  npm run build
  [[ -d "$USER_FRONTEND_DIR/dist" ]] || { err "鐢ㄦ埛绔?dist 鐩綍涓嶅瓨鍦?; exit 1; }

  log "寮€濮嬫墦鍖呯鐞嗙鍓嶇..."
  cd "$ADMIN_FRONTEND_DIR"
  npm install
  npm run build
  [[ -d "$ADMIN_FRONTEND_DIR/dist" ]] || { err "绠＄悊绔?dist 鐩綍涓嶅瓨鍦?; exit 1; }

  ok "鍓嶇鎵撳寘瀹屾垚"
}

deploy_frontends() {
  log "閮ㄧ讲鍓嶇闈欐€佽祫婧愬埌 Nginx 鐩綍..."
  rm -rf "$WEB_USER_DIR"/* "$WEB_ADMIN_DIR"/*
  cp -r "$USER_FRONTEND_DIR/dist/"* "$WEB_USER_DIR/"
  cp -r "$ADMIN_FRONTEND_DIR/dist/"* "$WEB_ADMIN_DIR/"
  ok "鍓嶇璧勬簮閮ㄧ讲瀹屾垚"
}

write_nginx_conf() {
  log "鍐欏叆 Nginx 閰嶇疆锛?NGINX_CONF"
  cat > "$NGINX_CONF" <<EOF
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
EOF

  rm -f /etc/nginx/sites-enabled/default /etc/nginx/conf.d/default.conf || true
  nginx -t
  systemctl restart nginx
  ok "Nginx 閰嶇疆瀹屾垚"
}

write_systemd_service() {
  local java_bin
  java_bin="$(command -v java)"
  [[ -n "$java_bin" ]] || { err "鏈壘鍒?java 鍛戒护"; exit 1; }

  log "鍐欏叆 systemd 鏈嶅姟锛?SYSTEMD_SERVICE"
  cat > "$SYSTEMD_SERVICE" <<EOF
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
EOF

  systemctl daemon-reload
  systemctl enable "$SERVICE_NAME"
  systemctl restart "$SERVICE_NAME"
  ok "systemd 鏈嶅姟宸插惎鍔?
}

show_status() {
  echo
  ok "閮ㄧ讲瀹屾垚锛屽綋鍓嶇姸鎬佸涓嬶細"
  echo "--------------------------------------------------"
  systemctl --no-pager --full status "$SERVICE_NAME" || true
  echo "--------------------------------------------------"
  systemctl --no-pager --full status nginx || true
  echo "--------------------------------------------------"
  echo "鐢ㄦ埛绔細  http://${SERVER_IP}/"
  echo "绠＄悊绔細  http://${SERVER_IP}/admin/"
  echo "鎺ュ彛鍓嶇紑锛歨ttp://${SERVER_IP}${CONTEXT_PATH}"
  echo
  echo "鑻ユ墦涓嶅紑锛岃妫€鏌ワ細"
  echo "1. 闃块噷浜戝畨鍏ㄧ粍鏄惁鏀捐 80 绔彛"
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