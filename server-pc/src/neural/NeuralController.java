package neural;

import framework.MotorController;
import neural.AnnClient;

public class NeuralController extends MotorController {
	/**
	 * All the actual work is done by the ANN server
	 */
	@Override
	protected int findPower() throws Exception {
		int result = AnnClient.searchSpeed(targetSpeed, bs);
		
		return result;
	}
}
