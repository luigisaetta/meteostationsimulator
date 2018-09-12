package oracle.it.wiot;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

public class SimpleDCDClient
{
	private static final long sleepTime = 1000;

	private static final String tasFilePath = "/Users/lsaetta/eclipse-workspace/WorkshopIoT2018/config/dcdmeteo0";
	private static final String tasFilePwd = "Amsterdam1";

	private DirectlyConnectedDevice dcd = null;
	private DeviceModel deviceModel = null;
	private VirtualDevice virtualDevice = null;

	private static final String AIRCARE_URN_MSG = "urn:com:oracle:aircare";

	public SimpleDCDClient()
	{
		try
		{
			System.out.println("Trusted assett store path: " + tasFilePath);
			System.out.println("Trusted assett pwd: " + tasFilePwd);

			// DCD API 1. Create a DCD Java object
			dcd = new DirectlyConnectedDevice(tasFilePath, tasFilePwd);

			if (!dcd.isActivated())
			{
				// DCD API 2. Activate, if not yet
				dcd.activate(AIRCARE_URN_MSG);

				System.out.println("Device activated !");
			} else
			{
				System.out.println("Device already activated !");
			}

			// DCD API 3. Get Device Model
			deviceModel = dcd.getDeviceModel(AIRCARE_URN_MSG);

			// DCD API 4. Create Virtual Device
			virtualDevice = dcd.createVirtualDevice(dcd.getEndpointId(), deviceModel);

		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
			;
		}
	}
	
	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Starting SimpleDCDClient...");

		SimpleDCDClient iotClient = new SimpleDCDClient();

		try
		{
			for (int i = 0; i < 10; i++)
			{
				// simulate reading some data
				AircareMessage msg = iotClient.readData();
				
				// send data to the cloud
				iotClient.sendData(msg);
				
				Thread.sleep(sleepTime);
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Stopping SimpleDCDClient...");
	}

	public void sendData(AircareMessage msg)
	{
		try
		{
			// DCD API 5. change the state of the virtual device (Send msg to IoT)
			virtualDevice.update().set("temp", msg.getTemp()).set("hum", msg.getHum()).set("pm25", msg.getPm25())
					.set("pm10", msg.getPm10()).finish();

			// you have to bracket the set of calls between update() and finish() to avoid
			// fragmentation of msgs

			System.out.println("Message sent to IoT...");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// simulate reading some data
	private AircareMessage readData()
	{
		AircareMessage aMsg = new AircareMessage();

		aMsg.setTemp(33.8d);
		aMsg.setHum(66.6d);
		aMsg.setPm25(10d);
		aMsg.setPm10(5d);

		return aMsg;
	}
}
