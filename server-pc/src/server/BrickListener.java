package server;

public interface BrickListener {
	// get an update about BrickState, do something with it
	public void updateBrick(BrickState bs);
}
