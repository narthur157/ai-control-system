package communication;

import framework.BrickState;

/**
 * Classes that use this must also do BrickComm.addListener(this)
 * to actually receives the updates that updateBrick provides
 * @see BrickComm
 * @author Nicholas Arthur
 *
 */
public interface BrickListener {
	// get an update about BrickState, do something with it
	public void updateBrick(BrickState bs);
}
