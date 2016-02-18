package client;

public class Command {
	public static final byte DISTURB_WHEEL = (byte) 0,
							 TORQUE_ARM = (byte) 1,
							 CONTROL_WHEEL = (byte) 2;
	
	public byte motor, power;
	
	public Command(byte[] bytes) {
		motor = bytes[0];
		power = bytes[1];
	}
}
