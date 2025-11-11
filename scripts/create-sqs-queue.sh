#!/bin/bash

# LocalStack SQS 큐 생성 스크립트
# 사용법: ./scripts/create-sqs-queue.sh

ENDPOINT="http://localhost:4566"
QUEUE_NAME="order-sync-check"
REGION="ap-northeast-2"

echo "Creating SQS queue: $QUEUE_NAME"
echo "Endpoint: $ENDPOINT"

# AWS CLI를 사용하여 큐 생성
aws --endpoint-url=$ENDPOINT sqs create-queue \
  --queue-name $QUEUE_NAME \
  --region $REGION

if [ $? -eq 0 ]; then
  echo "✓ Queue created successfully!"
  echo ""
  echo "Queue URL: $ENDPOINT/000000000000/$QUEUE_NAME"
  echo ""
  echo "You can verify the queue with:"
  echo "  aws --endpoint-url=$ENDPOINT sqs list-queues --region $REGION"
else
  echo "✗ Failed to create queue. Make sure LocalStack is running:"
  echo "  docker-compose up -d localstack"
fi

