package oracle.it.wiot;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

public class SimpleDCDClient
{
	private static final long sleepTime = 1000;
	
	private static final String tasFilePath = "/Users/lsaetta/eclipse-workspace/WorkshopIoT2018/config/dcdmeteo0";
	private static final String tasFilePwd = "Amsterdam1";
	
	private static DirectlyConnectedDevice dcd = null;
	private static DeviceModel deviceModel = null;
	private static VirtualDevice virtualDevice = null;
	
	private static final String AIRCARE_URN_MSG = "urn:com:oracle:aircare";
	
	public static void main(String[] args)
	{
		System.out.println("Starting SimpleDCDClient...");
		
		System.out.println("Trusted assett store path: " + tasFilePath);
		System.out.println("Trusted assett pwd: " + tasFilePwd);
		
		try
		{
			// 1. Create a DCD
			dcd = new DirectlyConnectedDevice(tasFilePath, tasFilePwd);
			
			if (!dcd.isActivated())
			{
				// 2. Activate, if not yet
				dcd.activate(AIRCARE_URN_MSG);
				
				System.out.println("Device activated !");
			}
			else
			{
				System.out.println("Device already activated !");
			}
			
			// 3. Get Device Model
			deviceModel = dcd.getDeviceModel(AIRCARE_URN_MSG);
			
			// 4. Create Virtual Device
			virtualDevice = dcd.createVirtualDevice(dcd.getEndpointId(), deviceModel);
			
			double myTemp = 33.8d;
			double myHum = 66.6d;
			double myPm25 = 10d;
			double myPm10 = 5d;
			
			for (int i = 0; i < 10; i++)
			{
				// 5. Send msg to IoT
				virtualDevice.update()
				.set("temp", myTemp)
				.set("hum", myHum)
				.set("pm25", myPm25)
				.set("pm10", myPm10)
				.finish();
				
				// you have to bracket set calls between update() and finish() to avoid fragmentation of msgs
				
				
				Thread.sleep(sleepTime);
				
				System.out.println("Message sent to IoT...");
				
				myTemp = myTemp + 0.1d;
				myHum = myHum - 0.1d;
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
