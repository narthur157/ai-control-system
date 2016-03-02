package communication;

public enum MOTOR {
	CONTROL ((byte) 0), 
	DISTURB ((byte) 1),
	TORQUE ((byte) 2), 
	STOP ((byte) 3);
	
	private final byte b;
	
	private MOTOR(byte b) {
		this.b = b;
	}
}
