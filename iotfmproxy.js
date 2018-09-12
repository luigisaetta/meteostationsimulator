/**
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 *
 * This software is dual-licensed to you under the MIT License (MIT) and
 * the Universal Permissive License (UPL). See the LICENSE file in the root
 * directory for license terms. You may choose either license, or both.
 *
 */

/*
 * Author: L. Saetta (luigi.saetta@gmail.com)
 * 
 * It uses the virtual device API to update attributes, raise alerts and
 * handle attribute updates  from the server.
 *
 *
 * The client is a directly connected device using the virtual device API.
 */

var async = require('async')
var express = require('express');
var bodyParser = require('body-parser');
const isReachable = require('is-reachable');
var package = require('./package')
var log = require('npmlog-ts')

// Oracle IoT client library require
dcl = require("./modules/device-library.node");

// Switch to enable debug (on|off) in Oracle IoT SDK
// here if you want to debug (set to true)
dcl = dcl({debug: true});

var storeFile = (process.argv[2]);
var storePassword = (process.argv[3]);

//
// globals
//
const PROCESS = 'IoT CS Proxy';

// Device model URN
SENSOR_URN_MSG = 'urn:com:oracle:iot:device:obd2'
ALERT_START = 'urn:com:oracle:iot:device:obd2:vehicle_started'
ALERT_STOP = 'urn:com:oracle:iot:device:obd2:vehicle_stopped'

// THIS proxy HTTP PATH
PROXY_PATH = '/iotproxy'

// IoT CS UI URL (only to check if reachable)
IOT_UI_URL = 'https://iotserver:10443/ui'

var app = express();

app.use(bodyParser.json());

// My device shadow
// my device local representation
var deviceShadow = {};
deviceShadow.activated = false;
deviceShadow.connected = false;

// sensor OBD2 model (initialization)
deviceShadow.data = 
{
    ora_obd2_engine_rpm: 0,
    ora_obd2_vehicle_speed: 0,
    ora_obd2_engine_coolant_temperature: 0,
    ora_obd2_runtime_since_engine_start: 0,
    ora_obd2_throttle_position: 0,
    ora_obd2_mass_air_flow: 0,
    ora_obd2_number_of_dtcs: 0,
    
    ora_latitude: 0,
    ora_longitude: 0,
    ora_altitude : 0
};

//
// we will be using Device Virtualization API
//


var deviceModel;
var virtualDev;

//
// createVirtualDevice
//
function createVirtualDevice(device){
    device.getDeviceModel(SENSOR_URN_MSG, function (response, error) {
        if (error) {
            console.log('-------------ERROR ON GET DEVICE MODEL-------------');
            console.log(error.message);
            console.log('------------------------------------------------------------');
            return;
        }
        deviceModel = response;
        
        virtualDev = device.createVirtualDevice(device.getEndpointId(), deviceModel);
    });
}


//
// Per ogni POST la funzione fa una send ad IoT CS
//
// POST 
//
count = 0;

app.post(PROXY_PATH, function (req, res) 
{
    // console.log(req.body);

    // build the message for IoT CS
    deviceShadow.data.ora_obd2_engine_rpm = req.body.RPM;
    deviceShadow.data.ora_obd2_vehicle_speed = req.body.SPEED;
    // if you need to visualize in Celsius (BUG !!!)
    deviceShadow.data.ora_obd2_engine_coolant_temperature = Math.round((req.body.COOLANT_TEMP - 32)/1.8);
    // deviceShadow.data.ora_obd2_engine_coolant_temperature = req.body.COOLANT_TEMP;
    deviceShadow.data.ora_obd2_runtime_since_engine_start = req.body.RUN_TIME;
    deviceShadow.data.ora_obd2_throttle_position = req.body.THROTTLE_POS;
    deviceShadow.data.ora_obd2_mass_air_flow = req.body.MAF;
    deviceShadow.data.ora_obd2_number_of_dtcs = 0;

    deviceShadow.data.ora_latitude = req.body.LAT;
    deviceShadow.data.ora_longitude = req.body.LON;
    deviceShadow.ora_altitude = req.body.ALT;

    //
    // send the msg to Oracle IoT
    // async no problem here !!!!
    //
    console.log('Have a msg to send.......');

    if (virtualDev != null)
        console.log('Sent....');
        virtualDev.update(deviceShadow.data);

    count ++;

    log.info(PROCESS, " processed msg n.", count);

    if (count == 2)
    {
        // send the ALERT: vehicle START
        console.log('Sending vehicle start Alert .......');
        var alert = virtualDev.createAlert(ALERT_START);
        alert.fields.ora_obd2_vehicle_speed = 10;

        alert.raise();
    }

    res.status(200).end()})
;

//
// Proxy server setup
//
// app.listen
//
app.listen(3000, function () {
    console.log();
    console.log("************************************");
    console.log();
    log.info(PROCESS, package.name, "starting up");
    log.info(PROCESS, "version", package.version);
    log.info(PROCESS);

    // verify if ToT server is reachable
    isReachable(IOT_UI_URL).then(reachable => {
        log.info("IoT server is reachable: ", reachable);

        if (!reachable)
        {
            log.error(PROCESS, "exiting...");
            process.exit(1);
        }
            
        //=> true
        log.info("IoT CS Proxy running !" );
        log.info('IoT CS Proxy listening on port 3000!');

        //
        // here we use file with device credentials
        var dcd = new dcl.device.DirectlyConnectedDevice(storeFile, storePassword);

        // Device initialization
        if (dcd.isActivated()) {
            deviceShadow.activated = true;

            // this way I handle the first time
            if (!deviceShadow.connected)
            {
                createVirtualDevice(dcd);
                deviceShadow.connected = true;
            }
        } else
        {
            dcd.activate([SENSOR_URN_MSG], function (device, error) {
                if (error) {
                    log.error(PROCESS, '-----------------ERROR ON ACTIVATION------------------------');
                    log.error(PROCESS, error.message);
                    log.error(PROCESS, '------------------------------------------------------------');
                    process.exit(1);
                }
                dcd = device;
                log.info(PROCESS, dcd.isActivated());
                
                if (dcd.isActivated()) {
                    createVirtualDevice(dcd);
                    deviceShadow.connected = true;
                }
            });
        }
    });

    
});