#!/bin/bash
export DOCKER_HOST=ssh://yac-user@46.62.203.236:2222
read -p "Enter image tag: " tag
export BACKEND_TAG="$tag"
export FRONTEND_TAG="$tag"

echo "Starting application on ${DOCKER_HOST} with version ${FRONTEND_TAG}"

docker compose -f compose.yaml -f compose.production.yaml up -d