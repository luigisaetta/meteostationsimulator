package oracle.it.wiot;

public class AircareMessage
{
	private int id;
	private long ts;
	private double temp;
	private double hum;
	private double light;
	private double pm25;
	private double pm10;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public long getTs()
	{
		return ts;
	}
	public void setTs(long ts)
	{
		this.ts = ts;
	}
	public double getTemp()
	{
		return temp;
	}
	public void setTemp(double temp)
	{
		this.temp = temp;
	}
	public double getHum()
	{
		return hum;
	}
	public void setHum(double hum)
	{
		this.hum = hum;
	}
	public double getLight()
	{
		return light;
	}
	public void setLight(double light)
	{
		this.light = light;
	}
	public double getPm25()
	{
		return pm25;
	}
	public void setPm25(double pm25)
	{
		this.pm25 = pm25;
	}
	public double getPm10()
	{
		return pm10;
	}
	public void setPm10(double pm10)
	{
		this.pm10 = pm10;
	}
}
