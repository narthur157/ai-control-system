Each of these packages intends to separate logic from the others into manageable chunks.

`communication` - Part of the framework for communication between the PC and NXT. Defines the protocols for communication and gives the BrickComm class.

`datacollection` - A set of 'tests' for data collection, also contains `RebalanceData` which formats data for training.

`framework` - A framework for remote NXT control, intended to potentially be useful for the LeJOS community and not just this specific usage.

`neural` - A set of tests for a ANN motor controller implementation, which is also in the package.

`pid` - A currently broken set of tests for a PID controller and its implementation.

`server` - Stores the main class for this project.
