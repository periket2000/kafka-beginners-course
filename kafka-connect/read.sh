#!/usr/bin/env bash
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:29092 --topic twitter_status_connect --from-beginning
