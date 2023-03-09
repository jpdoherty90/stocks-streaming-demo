import json
import os
import time
import subprocess
from confluent_kafka import Consumer

def read_ccloud_config(config_file):
    conf = {}
    with open(config_file) as fh:
        for line in fh:
            line = line.strip()
            if len(line) != 0 and line[0] != "#":
                parameter, value = line.strip().split('=', 1)
                conf[parameter] = value.strip()
    return conf

props = read_ccloud_config("client.properties")
props["group.id"] = "python-group-23"
props["auto.offset.reset"] = "earliest"

consumer = Consumer(props)
consumer.subscribe(["largest_movers"])
try:
    start_time = time.time()
    os.system("chmod +x socket_1")
    while True:
        msg = consumer.poll(1.0)
        if msg is not None and msg.error() is None:
            myjson = json.loads(msg.value())
            if time.time() - start_time > 10.0:
                print("10 seconds elapsed, opening up new socket for stock: " + msg.key().decode('utf-8'))
                os.system("killall -9 socket_1")
                subprocess.Popen(['./socket_1', msg.key().decode('utf-8')])
                start_time = time.time()
except KeyboardInterrupt:
    pass
finally:
    consumer.close()