#!/usr/bin/env bash
kafka-console-consumer --bootstrap-server 127.0.0.1:29092 --topic filtered_tweets --from-beginning
