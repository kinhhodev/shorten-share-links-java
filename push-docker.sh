# Push backend to Docker Hub
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t hongockinh/shorten-share-links-backend:latest \
  -f backend/Dockerfile \
  --push \
  backend

# Push frontend to Docker Hub
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t hongockinh/shorten-share-links-frontend:latest \
  -f frontend/Dockerfile \
  --push \
  frontend