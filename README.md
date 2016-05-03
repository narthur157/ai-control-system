GENERAL PROCESSES
---------

There a few sets of actions you do with this project, they are

- Collect and formatting data
- Train a neural net using data
- Using a neural net, run a controller
- Run a non-neural net controller

Some of these are dependent on each other. 

This project is set up to use (abuse?) git branches to switch between different neural network implementations.
Each of these actions may vary based on which branch is checked out, but the idea is for the process you use
to be the same for each, and that we avoid a bunch of logic in the code this way.

The reasoning for this is that each neural network requires different formatting of the data, its own training sets,
its own weights file, its own motor controller, etc. There should eventually be at least 3 neural networks, going from 1
input to 3 inputs (all 1 output). We will in the end mostly care about only 1 of these networks, and the other branches
can then be safely forgotten or deleted. The purpose of keeping the branches is to have working versions, and is essentially
the same idea as creating releases. Releases are good, but we're doing this instead because I prefer it.

COLLECTING DATA
-------

To collect data, you will usually want to do `run -d num_tests -f`, which will run `num_tests` tests and then format
the output for training. The run script will inform you that you probably should delete your `weights.net` file in `LightSpeedANN`.

If you change some formatting code and don't want to re-run the last test you did, you may simply use `run -f`. Note however that this
will re-use the most recent file from `testRuns` which stores the raw test data.

To inspect raw data, there is a convenience script `getLastTest` which opens the most recent test in `testRuns` in vim. 

Note that each branch has its own way of collecting data, and requires a different number of tests. As a rule of thumb I expect that
the number of tests required grows by powers of 10 for each input in the system. 1 in 1 out requires 101 tests to cover each possible
power setting (101 because 0 and 100 are both included). Thus you might 2 in 1 out to require 1000 tests and 3 in 1 out to require 10000
for full coverage.

TRAINING AN ANN
-------

Go to `LightSpeedANN`. You will need to have the `train-set.csv` and `test-set.csv` files up to date from the previous process.

If new training data, a new ANN, or `train` changed, you will want to delete the `weights.net` file.

Use `runTrain` to do training. This will compile `train` and then run tests with learning rates of `0.1` to `0.000000001` decreasing by factors of `10`, each for `1000` runs. A learning rate may be interrupted early by using Ctrl-c, which will save the weights file before quitting. `train` always saves its best results to `weights.net` as it gets them, and loads them when it starts. 


USING A NEURAL NET
-------

Making sure you have the correct branch checked out (commit changes before switching), use `run -n num_tests`. This will run `LightSpeedANN/eval` in the background and run whatever a `NeuralTest` (see `server-pc/src/framework` for explanation of what tests are in this context) for this branch. 

NON-NEURAL NET CONTROLLER
-------

Not yet implemented

--------

`run` - This is a script to run tests on the NXT. For data collection, use -d numTests, and optionally -f to format data for training afterwards. Raw data is output to the `testRuns` folder, and a version formatted for neural network training is written to `training-set.csv` if -f is used. Use -n to run tests for the ann motor controller. This requires the lejos folder to be in your path, which can be found here: `https://sourceforge.net/projects/lejos/`. This project uses LeJOS 0.9.1-beta-3 (note that some methods vary from the documentation do to this being a beta)

`getLastTest` - Open up the latest test in `testRuns` in vim

`dataparser.py` - This script outputs specific parts of the test data and removes unused columns, outputs to `training-set.csv`

-----------------
`LightspeedANN` - This directory holds the artifical neural network implementation. There is a makefile for the neural net, 2 scripts, and 2 programs. 

`server-pc` - This directory holds the code for a PC communicating with the NXT. It includes a framework for accessing a raw NXT motor without motor controls over USB, as well as a framework for running tests, and then instances of these tests. There is also a (currently broken) PID implementation over this framework. 

`client-brick` - This directory holds the NXT side of the raw motor communications framework. State updates are sent and received asynchronously. A multithreaded approach to non-blocking tachometer readings is also given.

