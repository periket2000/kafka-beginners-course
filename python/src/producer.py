from kafka import KafkaProducer

producer = KafkaProducer(bootstrap_servers=['localhost:49092'])
producer.send('mi_tema', b'mensaje de prueba')
