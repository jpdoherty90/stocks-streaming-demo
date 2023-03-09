import requests
from confluent_kafka import Producer

def read_ccloud_config(config_file):
    conf = {}
    with open(config_file) as fh:
        for line in fh:
            line = line.strip()
            if len(line) != 0 and line[0] != "#":
                parameter, value = line.strip().split('=', 1)
                conf[parameter] = value.strip()
    return conf

api_url = "https://financialmodelingprep.com/api/v3/available-traded/list?apikey="
response = requests.get(api_url)
producer = Producer(read_ccloud_config("client.properties"))
count = 0
for stock in response.json():
    symbol = str(stock["symbol"])
    if len(symbol) < 6 and symbol.isalpha() and symbol.find(".") == -1 and stock["type"] == "stock":
        print(stock)
        producer.produce("tradable_symbols", key=symbol, value=stock["name"])
        print("============")
        count += 1
producer.flush()
print("COUNT: " + str(count))