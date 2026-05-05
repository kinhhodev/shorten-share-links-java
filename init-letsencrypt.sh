#!/usr/bin/env bash
# Obtain initial SSL certificate from Let's Encrypt for shortlinks.kinhho.id.vn.
# Run once on a fresh server:  chmod +x init-letsencrypt.sh && ./init-letsencrypt.sh
set -euo pipefail

DOMAIN="shortlinks.kinhho.id.vn"
EMAIL="${CERTBOT_EMAIL:-}"                       # set env or pass as: CERTBOT_EMAIL=you@example.com ./init-letsencrypt.sh
STAGING="${CERTBOT_STAGING:-0}"                   # set to 1 for Let's Encrypt staging (rate-limit safe)

COMPOSE="docker compose"
DATA_PATH="certbot-certs"                        # docker volume name

# --- 1. Create a temporary self-signed cert so nginx can start on 443 ---
echo "### Creating temporary self-signed certificate ..."
$COMPOSE run --rm --entrypoint "\
  mkdir -p /etc/letsencrypt/live/$DOMAIN" certbot

$COMPOSE run --rm --entrypoint "\
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem \
    -out    /etc/letsencrypt/live/$DOMAIN/fullchain.pem \
    -subj '/CN=localhost'" certbot

# --- 2. Start nginx (it will use the dummy cert) ---
echo "### Starting nginx ..."
$COMPOSE up -d frontend

# --- 3. Remove the dummy cert ---
echo "### Removing temporary certificate ..."
$COMPOSE run --rm --entrypoint "\
  rm -rf /etc/letsencrypt/live/$DOMAIN && \
  rm -rf /etc/letsencrypt/archive/$DOMAIN && \
  rm -rf /etc/letsencrypt/renewal/$DOMAIN.conf" certbot

# --- 4. Request real certificate ---
STAGING_ARG=""
if [ "$STAGING" = "1" ]; then
  STAGING_ARG="--staging"
fi

EMAIL_ARG=""
if [ -n "$EMAIL" ]; then
  EMAIL_ARG="--email $EMAIL"
else
  EMAIL_ARG="--register-unsafely-without-email"
fi

echo "### Requesting Let's Encrypt certificate for $DOMAIN ..."
$COMPOSE run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    $STAGING_ARG \
    $EMAIL_ARG \
    -d $DOMAIN \
    --agree-tos \
    --no-eff-email \
    --force-renewal" certbot

# --- 5. Reload nginx to pick up the real cert ---
echo "### Reloading nginx ..."
$COMPOSE exec frontend nginx -s reload

echo "### Done! SSL certificate installed for $DOMAIN"
