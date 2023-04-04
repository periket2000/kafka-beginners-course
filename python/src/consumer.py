from kafka import KafkaConsumer

consumer = KafkaConsumer('mi_tema', bootstrap_servers=['localhost:49092'])
for mensaje in consumer:
	print(mensaje.value.decode('utf-8'))

consumer.close()
