package communication;

/**
 * Ensure tht this class matches the one in client-brick 
 * @author Nicholas Arthur
 *
 */
public class Command {
	public static final byte DISTURB_WHEEL = (byte) 0,
							 TORQUE_ARM = (byte) 1,
							 CONTROL_WHEEL = (byte) 2,
							 STOP = (byte) 3;
	// bytes[0] = which wheel to change
	// bytes[1] = power setting for this wheel, or stop command
	
	public byte[] bytes = new byte[2];
	
	// server can also tell the brick to shut down
	public Command(int motor, int power) {
		bytes[0] = (byte) motor;
		bytes[1] = (byte) power;
	}
	
	// toString does not need to match
	public String toString() {
		String s = "Command: Set ";
		if(bytes[0] == DISTURB_WHEEL) {
			s += "disturb wheel";
		}
		else if (bytes[0] == TORQUE_ARM) {
			s += "torque arm";
		}
		else if (bytes[0] == CONTROL_WHEEL) {
			s += "control wheel";
		}
		else if (bytes[0] == STOP) {
			s += "STOP";
		}
		return s + " to power " + bytes[1];
	}
}
