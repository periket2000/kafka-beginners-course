#!/usr/bin/env bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_status_connect --from-beginning
