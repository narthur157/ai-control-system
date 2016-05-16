`BrickComm` is used throughout the project to communicate with the NXT. The most relevant methods are:

`sendCommand` - Send a command to the NXT (see `Command.java`)

`addListener` - Subscribe caller (which must implement `BrickListener`) to updates from the `BrickUpdater`, which is managed by `BrickComm` for simplicity. This is an observer pattern.


