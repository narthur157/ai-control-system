package neural;

import communication.BrickComm;
import framework.MotorController;
import neural.AnnClient;

public class NeuralController extends MotorController {
	public NeuralController() {
		BrickComm.addListener(this);
	}

	@Override
	protected int findPower() throws Exception {
		int result = AnnClient.searchSpeed(targetSpeed, bs);
		
		return result;
	}
}
