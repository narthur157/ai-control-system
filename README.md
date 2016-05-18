For `server-pc` Javadocs, visit narthur157.github.io

Most important folders in this project have a `README`, of increasing technicality. This one is the most general and deals with running the project and less about exactly how that is achieved internally. Currently the `client-brick` lacks a README, however it is much simpler than everything else as it is a slave to what occurs in `server-pc`. Note in particular the README's in `server-pc/src/framework` and `server-pc/src/communications`.

GENERAL PROCESSES
---------

There a few sets of actions you do with this project, they are

- Run a benchmark
- Change parameters
- Collect and formatting data
- Train a neural net using data
- Using a neural net, run a controller
- Run a non-neural net controller
- Update Javadocs

Some of these are dependent on each other. 

This project is set up to use (abuse?) git branches to switch between different neural network implementations.
Each of these actions may vary based on which branch is checked out, but the idea is for the process you use
to be the same for each, and that we avoid a bunch of logic in the code this way.

The reasoning for this is that each neural network requires different formatting of the data, its own training sets,
its own weights file, its own motor controller, etc. There should eventually be at least 3 neural networks, going from 1
input to 3 inputs (all 1 output). We will in the end mostly care about only 1 of these networks, and the other branches
can then be safely forgotten or deleted. The purpose of keeping the branches is to have working versions, and is essentially
the same idea as creating releases. Releases are good, but we're doing this instead because I prefer it.

RUN A BENCHMARK
-------

Benchmarks are run using `run -b num_tests`. See `ComparisonTest.java`. This test compares the `NeuralController` and `PIDController`. 
This test could be easily re-worked to run any other motor controller. Data is output to `benchmarks/` and then plotted via `gnuplot`.

CHANGING PARAMETERS
-------

There are a number of parameters you may wish to change in this project. They are:

- `server-pc/src/pid/PIDController.java` PID constants
- `server-pc/src/frameworkMotorController` control delay/loop frequency
- `client-brick/src/client/WheelTimer` queue size and collection frequency
- `./dataparser.py` `get_future_speeds` time offset and number of speeds. Note that changing the number of speeds is a change in the neural net, which will require running `LightSpeedNN/runTrain` with a neural net structure as input (ie 3 16t 2l for 3 input, 16 tanh hidden node, 2 output linear activation), as well as deleting the `LightSpeedANN/weights.net` file and collecting new training data and then running training on it. Additionally, each entry in the data logging also logs rows of future times. The number of future times may be adjusted.

Some of these could be made configurable from a properties file, however this has not been implemented.

COLLECTING DATA
-------

To collect data, you will usually want to do `run -d num_tests -f`, which will run `num_tests` tests and then format
the output for training. The run script will inform you that you probably should delete your `weights.net` file in `LightSpeedANN`.

If you change some formatting code and don't want to re-run the last test you did, you may simply use `run -f`. Note however that this
will re-use the most recent file from `testRuns` which stores the raw test data.

To inspect raw data, there is a convenience script `getLastTest` which opens the most recent test in `testRuns` in vim. 

`run -f` will only work for data collected from `run -d`. This could be fixed by setting the stable power and change flag appropriately in `collectData`.


TRAINING AN ANN
-------

Go to `LightSpeedANN`. You will need to have the `train-set.csv` and `test-set.csv` files up to date from the previous process.

If new training data, a new ANN, or `train` changed, you will need to delete the `weights.net` file.

Use `runTrain` (no arguments) to do training. This will compile `train` and then run tests with learning rates of `0.1` to `0.000000001` decreasing by factors of `10`, each for `1000` runs. A learning rate may be interrupted early by using Ctrl-c, which will save the weights file before quitting. `train` always saves its best results to `weights.net` as it gets them, and loads them when it starts. 


USING A NEURAL NET
-------

Making sure you have the correct branch checked out (commit changes before switching), use `run -n num_tests`. This will run `LightSpeedANN/eval` in the background and run whatever a `NeuralTest` (see `server-pc/src/framework` for explanation of what tests are in this context) for this branch. 

NON-NEURAL NET CONTROLLER
-------

Not yet implemented

UPDATE JAVADOCS
-------

In Eclipse, use `Project->Generate Javadocs`. Set storage location in the `narthur157.github.io` git repository/directory, then click next twice and set overview file (at the top) to `overview.html` from this directory. Then change directory to `narthur157.github.io` and do `git add . && git commit -m "Updated Javadocs" && git push`.

--------

`run` - This is a script to run tests on the NXT. For data collection, use -d numTests, and optionally -f to format data for training afterwards. Raw data is output to the `testRuns` folder, and a version formatted for neural network training is written to `training-set.csv` if -f is used. Use -n to run tests for the ann motor controller. This requires the lejos folder to be in your path, which can be found here: `https://sourceforge.net/projects/lejos/`. This project uses LeJOS 0.9.1-beta-3 (note that some methods vary from the documentation do to this being a beta)

`getLastTest` - Open up the latest test in `testRuns` in vim

`dataparser.py` - This script outputs specific parts of the test data and removes unused columns, outputs to `training-set.csv`

-----------------
`LightspeedANN` - This directory holds the artifical neural network implementation. There is a makefile for the neural net, 2 scripts, and 2 programs. 

`server-pc` - This directory holds the code for a PC communicating with the NXT. It includes a framework for accessing a raw NXT motor without motor controls over USB, as well as a framework for running tests, and then instances of these tests. There is also a (currently broken) PID implementation over this framework. 

`client-brick` - This directory holds the NXT side of the raw motor communications framework. State updates are sent and received asynchronously. A multithreaded approach to non-blocking tachometer readings is also given.

