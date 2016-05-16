package framework;

import communication.BrickComm;
import communication.BrickListener;
import communication.Command;

/**
 * MotorController is an abstract base class for various control
 * system implementations. Extending MotorController gives access
 * to periodic updates from the brick, and enables implementation
 * of a control system by implementing findPower, which is called
 * periodically.
 * 
 * @see pid.PIDController
 * @author Nicholas Arthur
 *
 */
public abstract class MotorController implements BrickListener {
	private int disturbance = 0;
	private boolean setting = false;
	private long startTime = -1;
	
	/**
	 * Max output allowed by brick
	 */
	protected final double 	MAX_OUTPUT = 100.0;
	/**
	 * Min output allowed by brick
	 */
	protected final double MIN_OUTPUT = MAX_OUTPUT * -1;
	/**
	 * Amount of deviation from target speed to be treated as
	 * being at the target speed
	 */
	protected final double ERR_TOLERANCE = 20.0;
	
	/**
	 * Most recent BrickState, used to calculate response
	 */
	protected BrickState bs;
	/**
	 * Speed of rotation trying to achieved
	 */
	protected int targetSpeed = 0;
	/**
	 * Time delay between control loops to allow physics to occur
	 */
	protected int CONTROL_DELAY = 100;
	
	/**
	 * Subscribes the motor controller to brick updates
	 */
	public MotorController() {
		BrickComm.addListener(this);
	}
	
	/**
	 * This method represents the implementation of a control loop
	 * 
	 * @return int - The power this controller will set to achieve targetSpeed
	 * @throws Exception
	 */
	abstract protected int findPower() throws Exception;
	
	/**
	 * BrickListener interface method, used to receive
	 * periodic updates from the client. Should not be called
	 * except by BrickUpdater
	 * 
	 * @see BrickListener
	 * @param bs The BrickState representing the brick's state
	 */
	public final void updateBrick(BrickState bs) {
		this.bs = bs;
		
		boolean speedSet = ERR_TOLERANCE - Math.abs(targetSpeed - bs.disturbSpeed) > 0;
		
		boolean delaying = (System.currentTimeMillis() - startTime) < CONTROL_DELAY;
	
		if (!setting && !speedSet && !delaying) {
			// avoid trying to change before a previous loop finishes
			synchronized(this) {
				setting = true;
				startTime = System.currentTimeMillis();
				
				try {
					int power = findPower();
					// these are blocking calls, it may be better to do this in a thread
					// which wakes up every CONTROL_DELAY ms, as other objects may be waiting
					// for their updateBrick to be called
					BrickComm.sendCommand(Command.CONTROL_WHEEL, power);
					BrickComm.sendCommand(Command.DISTURB_WHEEL, power - disturbance);
					
					float err = (Math.abs((float) bs.disturbSpeed - (float) targetSpeed) / (float) targetSpeed)*100.0f;

					System.out.println("desiredSpeed\t" + targetSpeed + " currentSpeed\t" + 
										bs.disturbSpeed + " power\t" + power + " disturbance\t" + 
										disturbance + " err\t" + (int)err + "%");
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				setting = false;
			}
		}
	}

	/**
	 * Returns immediately, speed may not be achieved for some time
	 * @param targetSpeed
	 */
	public final void setSpeed(int targetSpeed) {
		this.targetSpeed = targetSpeed;
	}
	
	/**
	 * 
	 * @param d Power offset from the control wheel
	 */
	public final void setDisturbance(int d) {
		disturbance = d;
	}
}
