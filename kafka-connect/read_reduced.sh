#!/usr/bin/env bash
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic reduced_tweets --from-beginning
