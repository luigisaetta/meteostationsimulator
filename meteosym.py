# Simulate a Meteo station

import csv 
import json
import os
import sys
import datetime
import time

# send a msg every 1 sec.
sleepTime = 1
#MQTT topic
TOPIC_NAME = 'cardata'


#
# Main 
#

#
# reading file name from command line args
#
fName = sys.argv[1]


print "*******************"
print "Starting simulation...."
print ""
print "File name: ", fName


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
                print 'Sending:', msgJson

            time.sleep(sleepTime)
            lin_num = lin_num + 1

except IOError:
    print("Errore: file not found: ", fName)
    print("Interrupted...")
    sys.exit(-1)



