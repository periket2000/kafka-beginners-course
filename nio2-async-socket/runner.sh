#!/bin/sh

NUMPROC=$1

if [ -z "$NUMPROC" ]; then
  NUMPROC=1
fi

for i in $(seq 1 $NUMPROC); do 
    nohup java -cp target/nio2-async-socket-1.0.jar com.github.periket2000.NioSocketClient & 
done

