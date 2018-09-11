#
# Simulate a Meteo station
# reads data from a file, select an interval (one day for example)
# and send data as JSON msgs to an MQTT broker
#
# can change the speed using sleepTime
#

import csv 
import json
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
TOPIC_NAME = "device/meteo1/data"

# enables MQTT logging
DO_LOG = False

# config for interval chosen (idmi, idmax)
ID1 = 1531
ID2 = 1626

#
# handles MQTT logging
#
def on_log(client, userdata, level, buf):
    if DO_LOG == True:
        print("log: ",buf)

#
# Main 
#

#
# reading data file name from command line args
#
fName = sys.argv[1]


print ("***********************")
print ("Starting simulation....")
print ("")
print ("File name: ", fName)
print ("")
print ("")

# connect to MQTT broker
mqttClient = mqtt.Client(clientID, protocol=mqtt.MQTTv311)
mqttClient.on_log = on_log

mqttClient.connect(HOST, 1883, TIMEOUT)

try:
    with open(fName) as csvfile:
        reader = csv.reader(csvfile, delimiter=',')

        lin_num = 1

        for line in reader:
            
            # line is a List
            # ignore first line (header)
            if (lin_num > 1):

                id = line[0]

                if (int(id) >= ID1 and int(id) <= ID2):
                    # print(line)

                    # Unix timestamp of the read from sensors
                    ts = line[1]
                    temp = line[2]
                    hum = line[3]
                    light = line[4]
                    airq = line[5]
                    pm25 = line[6]
                    pm10 = line[7]

                    # build the JSON msg
                    msg = {}
                    msg['id'] = int(id)
                    msg['ts'] = int(ts)
                    msg['temp'] = float(temp)
                    msg['hum'] = float(hum)
                    msg['light'] = float(light)
                    msg['airq'] = float(airq)
                    msg['pm25'] = float(pm25)
                    msg['pm10'] = float(pm10)

                    msgJson = json.dumps(msg)
                    
                    print ('Sending: ', datetime.utcfromtimestamp(int(ts)).strftime('%Y-%m-%d %H:%M:%S'), msgJson)
                    
                    # send the msg to the MQTT broker
                    (result, mid) = mqttClient.publish(TOPIC_NAME, msgJson)

                    if result != 0:
                        # some problems ? try enabling logging
                        print (result, mid)

                    time.sleep(sleepTime)

            lin_num = lin_num + 1

except Exception:
    print()
    print('\n')
    print('*** Error info: ', sys.exc_info()[0], sys.exc_info()[1])
    sys.exit(-1)



