package oracle.it.wiot;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

public class JSONMQTTSubscriber implements MqttCallback
{
	private final static String broker = "tcp://localhost:1883";
	private final static String clientId = "meteo0";
	private static final String TOPIC = "device/" + clientId + "/data";
	private static final int MYQOS = 1;
	
	private static MemoryPersistence persistence = new MemoryPersistence();
	private static MqttConnectOptions connOpts = new MqttConnectOptions();
	
	private static Gson gson = new Gson();
	
	private static SimpleDCDClient iotClient = null;
	
	public static void main(String[] args)
	{
		JSONMQTTSubscriber subscriber = new JSONMQTTSubscriber();
		
		System.out.println("Connecting to MQTT broker: " + broker);
		
		// creating the IoT client
		iotClient = new SimpleDCDClient();
		
		try
		{
			MqttClient client = new MqttClient(broker, clientId, persistence);

			connOpts.setCleanSession(true);
			connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

			client.connect(connOpts);

			System.out.println("Connected...");
			
			client.setCallback(subscriber);

			// subscriptions to MQTT topics
			client.subscribe(TOPIC, MYQOS);
			
			// main loop
			// a simple never-ending loop to keep the class up&running while
			// waiting for msgs
			while(true)
			{
				try
				{
					Thread.sleep(5000);
					
					System.out.println("...");
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (MqttSecurityException e)
		{
			e.printStackTrace();
		} catch (MqttException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectionLost(Throwable arg0)
	{
		System.out.println("KO, connection lost !");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0)
	{
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception
	{
		// here we must handle the message arriving
		System.out.println("OK message received on topic: " + topic);
		
		String strMess = new String(msg.getPayload(), StandardCharsets.UTF_8);
		
		System.out.println("Msg: " + strMess);
		
		// here we de-serialize the JSON message
		AircareMessage aMsg = gson.fromJson(strMess, AircareMessage.class);
		
		System.out.println("Deserialized....");
		System.out.println("ts:" + aMsg.getTs());
		System.out.println("temp:" + aMsg.getTemp());
		
		// send data to IoT
		iotClient.sendData(aMsg);
	}

}
