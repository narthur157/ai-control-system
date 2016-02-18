package server;

public class Command {
	public static final byte DISTURB_WHEEL = (byte) 0,
							 TORQUE_ARM = (byte) 1,
							 CONTROL_WHEEL = (byte) 2;
	// bytes[0] = which wheel to change
	// bytes[1] = power setting for this wheel
	
	public byte[] bytes = new byte[2];
	
	public Command(int motor, int power) {
		bytes[0] = (byte) motor;
		bytes[1] = (byte) power;
	}
	
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
		return s + " to power " + bytes[1];
	}
}
