from kafka import KafkaConsumer
from concurrent.futures import ThreadPoolExecutor
import threading

def consume():
	consumer = KafkaConsumer('mi_tema', bootstrap_servers=['localhost:29092'])
	for message in consumer:
		print(f"{threading.current_thread().name} Valor: {message.value.decode('utf-8')}")

with ThreadPoolExecutor(max_workers=5) as executor:
	for _ in range(5):
		executor.submit(consume)

