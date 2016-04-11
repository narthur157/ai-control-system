package client;

/**
 * This class must always match the class in server-pc
 * @author Nicholas Arthur
 *
 */
public class Command {
	public static final byte DISTURB_WHEEL = (byte) 0,
							 TORQUE_ARM = (byte) 1,
							 CONTROL_WHEEL = (byte) 2,
							 STOP = (byte) 3;
	
	public byte motor, power;
	
	public Command(byte[] bytes) {
		motor = bytes[0];
		power = bytes[1];
	}
}
