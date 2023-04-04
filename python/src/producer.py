from kafka import KafkaProducer
import random

producer = KafkaProducer(bootstrap_servers=['localhost:49092'])
for i in range(100):
	message = "Mensaje de prueba {}".format(str(random.randint(1,100)))
	producer.send('mi_tema', value=message.encode())
producer.close()
