package testing;

import java.io.IOException;
import java.util.Random;


import communication.BrickComm;
import communication.Command;


public class DataGeneration extends Test {
    private Random rand = new Random();
    private int count = 0;
    private int stablePower = 0;
    private int powerOffset = 0;
     
    public DataGeneration() throws IOException {
        super();
    }
 
    @Override
    public void test() {
        if (count == 0) {
            changeFlag = 0;
        	int disturbPower = generateNextPower(stablePower);
        	stablePower = generateNextPower(disturbPower);
        	
            BrickComm.sendCommand(Command.DISTURB_WHEEL, disturbPower); 
            BrickComm.sendCommand(Command.CONTROL_WHEEL, stablePower);
            
            powerOffset = stablePower - disturbPower;
            
        } else {        
            changeFlag = 1;
        	// this simulates changing power in reaction
        	// both wheels must change the same way the motor controller works
        	int reactionPower = generateNextPower(stablePower);
        	
            BrickComm.sendCommand(Command.CONTROL_WHEEL, reactionPower);
            BrickComm.sendCommand(Command.DISTURB_WHEEL, reactionPower - powerOffset);
        }
         
        count = (count+1) % 2;
    }
     
    private int generateNextPower(int prevPower) {
        int randPower = prevPower+rand.nextInt(81)-40; 
         
        if (randPower < 10) {
            randPower = rand.nextInt(40);
        }
        if (randPower > 100) {
            randPower = 100-rand.nextInt(40);
        }
        return randPower;
    }
    
    /**
     * Decorator pattern, this string is logged to file
     * @see Test.collectData
     */
    protected String collectData() {
    	System.out.println("Just making sure: " + super.collectData() + "\t" + stablePower);
		return super.collectData() + "\t" + stablePower + "\t" + changeFlag;
	}
}
