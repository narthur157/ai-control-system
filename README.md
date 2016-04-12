Research on using neural networks to make motor controllers better.

`robotics-research/run` - This is a script to run tests on the NXT. For data collection, use -d numTests, and optionally -f to format data for training afterwards. Raw data is output to the `testRuns` folder, and a version formatted for neural network training is written to `training-set.csv` if -f is used. Use -n to run tests for the ann motor controller.

`robotics-research/getLastTest` - Open up the latest test in `testRuns` in vim

`robotics-research/dataparser.py` - Script to collect samples out of the raw training data formatted suitable for training

-----------------
`LightspeedANN` - This directory holds the artifical neural network implementation. There is a makefile for the neural net, 2 scripts, and 2 programs. 

`runTrain` is a convenience script to compile `train` and `eval` and then run training. Arguments to `gen_sse.rb` may supplied, creating a new ann in `ann.c`. 

`gen_data` is a bash script which takes in a training set and splits it into 2 files, one of which contains 80% of the randomized, and the other containing the other 20%. These default to test-set.csv and train-set.csv, and a sample is included in the repo. 

`gen_sse.rb` is a ruby script that creates the file ann.c given a network topology. For example, `./gen_sse.rb 3 16t 16t 4l` creates a network with 3 inputs, 2 16 neuron hidden layers using tanh as their activation function, and a 4 neuron output layer using linear transformation. Note that this topology can be seen at the top of ann.c.

`train` is the training program which by default uses train-set.csv and test-set.csv for its training. Learning rate is accepted as an argument.

`eval` is a program which uses sockets to test inputs to a neural net, one line at a time. To test, run `./eval`, and then in another terminal, `telnet localhost 8888` and then upon connecting, type `1 2 3` (assuming 3 inputs) and then enter. 

-----------------

`server-pc` - This directory holds the code for a PC communicating with the NXT. It includes a framework for accessing a raw NXT motor without motor controls over USB, as well as a framework for running tests, and then instances of these tests. There is also a (currently broken) PID implementation over this framework. 

`client-brick` - This directory holds the NXT side of the raw motor communications framework. State updates are sent and received asynchronously. A multithreaded approach to non-blocking tachometer readings is also given.
