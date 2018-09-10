# Simulate a Meteo station

import csv 
import json
import os
import sys
from datetime import datetime
import time
import paho.mqtt.client as mqtt

# send a msg every 1 sec.
sleepTime = 1

# configuration for MQTT
HOST = "wiotubuntu-workshopiot2018-ezxpt5yn.srv.ravcloud.com"

clientID = "meteo1"
TIMEOUT = 10
TOPIC_NAME = 'device/meteo1/data'
DO_LOG = False

# config for interval chosen
ID1 = 1531
ID2 = 1656

def on_log(client, userdata, level, buf):
    if DO_LOG == True:
        print("log: ",buf)

#
# Main 
#

#
# reading file name from command line args
#
fName = sys.argv[1]


print ("***********************")
print ("Starting simulation....")
print ("")
print ("File name: ", fName)

mqttClient = mqtt.Client(clientID, protocol=mqtt.MQTTv311)
mqttClient.on_log = on_log

mqttClient.connect(HOST, 1883, TIMEOUT)

try:
    with open(fName) as csvfile:
        reader = csv.reader(csvfile, delimiter=',')

        lin_num = 1

        for line in reader:
            # line e' una List
            # ignora la prima linea (header)
            if lin_num > 1:
                # print(line)
                id = line[0]
                ts = line[1]
                temp = line[2]
                hum = line[3]
                light = line[4]
                airq = line[5]
                pm25 = line[6]
                pm10 = line[7]

                # build the JSON msg
                msg = {}
                msg['id'] = id
                msg['ts'] = ts
                msg['temp'] = temp
                msg['hum'] = hum
                msg['light'] = light
                msg['airq'] = airq
                msg['pm25'] = pm25
                msg['pm10'] = pm10

                msgJson = json.dumps(msg)
                
                if (int(id) >= ID1 and int(id) <= ID2):
                    print ('Sending: ', datetime.utcfromtimestamp(int(ts)).strftime('%Y-%m-%d %H:%M:%S'), msgJson)

                    (result, mid) = mqttClient.publish(TOPIC_NAME, msgJson)

                    if result != 0:
                        print (result, mid)

                    time.sleep(sleepTime)

            lin_num = lin_num + 1

except Exception:
    print("Errore: file not found: ", fName)
    print("Interrupted...")
    sys.exit(-1)



