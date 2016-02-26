package communication;

import framework.BrickState;


public interface BrickListener {
	// get an update about BrickState, do something with it
	public void updateBrick(BrickState bs);
}
