#!/bin/bash
# Kafka topic provisioning script.
# Run inside the kafka container after broker is healthy:
#   docker exec crm-kafka /opt/kafka/init-topics.sh

KAFKA_BIN=/usr/bin
BOOTSTRAP=localhost:9092

create_topic() {
  local topic=$1
  local partitions=${2:-3}
  local replication=${3:-1}
  $KAFKA_BIN/kafka-topics --bootstrap-server $BOOTSTRAP \
    --create --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$replication"
  echo "Topic created: $topic"
}

# Auth events
create_topic auth.user.registered
create_topic auth.user.login

# Lead events
create_topic lead.created
create_topic lead.updated
create_topic lead.assigned
create_topic lead.status.changed
create_topic lead.deleted

# Deal events
create_topic deal.created
create_topic deal.updated
create_topic deal.stage.changed
create_topic deal.assigned
create_topic deal.deleted

# Email events
create_topic email.sent
create_topic email.campaign.dispatched
create_topic email.reminder.triggered

# Notification events
create_topic notification.send

echo "All Kafka topics provisioned."
