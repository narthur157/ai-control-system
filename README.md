Make motor controllers better with ANN based model predictive controllers or something like that

`run` - This is a script to run tests on the NXT. For data collection, use -d numTests, and optionally -f to format data for training afterwards. Raw data is output to the `testRuns` folder, and a version formatted for neural network training is written to `training-set.csv` if -f is used. Use -n to run tests for the ann motor controller. This requires the lejos folder to be in your path, which can be found here: `https://sourceforge.net/projects/lejos/`. This project uses LeJOS 0.9.1-beta-3 (note that some methods vary from the documentation do to this being a beta)

`getLastTest` - Open up the latest test in `testRuns` in vim

`dataparser.py` - This script outputs specific parts of the test data and removes unused columns, outputs to `training-set.csv`

-----------------
`LightspeedANN` - This directory holds the artifical neural network implementation. There is a makefile for the neural net, 2 scripts, and 2 programs. 

`server-pc` - This directory holds the code for a PC communicating with the NXT. It includes a framework for accessing a raw NXT motor without motor controls over USB, as well as a framework for running tests, and then instances of these tests. There is also a (currently broken) PID implementation over this framework. 

`client-brick` - This directory holds the NXT side of the raw motor communications framework. State updates are sent and received asynchronously. A multithreaded approach to non-blocking tachometer readings is also given.
