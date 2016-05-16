`runTrain` is a convenience script to compile `train` and `eval` and then run training. Arguments to `gen_sse.rb` may supplied, creating a new ann in `ann.c`. Without arguments, `runTrain` will run `train` with learning rates decreasing by factors of 10 up to 10^-8.

`gen_data` is a bash script which takes in a training set and splits it into 2 files, one of which contains 80% of the randomized, and the other containing the other 20%. These default to test-set.csv and train-set.csv, and a sample is included in the repo. 

`gen_sse.rb` is a ruby script that creates the file ann.c given a network topology. For example, `./gen_sse.rb 3 16t 16t 4l` creates a network with 3 inputs, 2 16 neuron hidden layers using tanh as their activation function, and a 4 neuron output layer using linear transformation. Note that this topology can be seen at the top of ann.c.

`train` is the training program which by default uses train-set.csv and test-set.csv for its training. Learning rate is accepted as an argument.

`eval` is a program which uses sockets to test inputs to a neural net, one line at a time. To test, run `./eval`, and then in another terminal, `telnet localhost 8888` and then upon connecting, type `1 2 3 4` (assuming 3 inputs and then last a target speed) and then enter. 


LSANN Original README
-----------------------
LightSpeedANN (LSANN) is a Ruby program that generates optimized C code that 
implements an artificial neural network (ANN) whose topology you specify.  
Forward and backward propagation are both supported.  If you want to support
more than one ANN topology, you can run LSANN multiple times with different
layer specifications.  The output of the Ruby program is C code that makes
heavy use of SSE vector intrinsics and unrolled loops.  Although there is
further improvement that can be done, LSANN output code is efficient because
there is a relative lack of conditional instructions, and the memory layout
is compact and friendly to cache prefetchers.  

Although LSANN itself is under GPLv2, its output is not under any kind of 
licensing restrictions.

Usage:

    ruby gen_sse.rb {list of layers} > mynet.c

A layer is specified by a number and an activation function suffix.  The
number is how many nodes are in the layer.  Activation function suffixes
are:

    l - linear
    t - tanh
    s - logistic
    r - ReLU
    
With ReLU, you must also specify if the function is hard (h) or soft (s).  
This is specified separately for forward and backward propagation.  Thus,
"rhh" specifies hard ReLU for both forward and back, while "rhs" specifies
hard for forward and soft for backward.

Following the activation spec, you can specify quantization in the form of
"q#.#", where '#' represents numbers of integer and fractional bits.  
This feature needs some work and is currently optimized for tanh activation, 
where specifying "q0.8" would quantize a layer to a signed 8-bit value, 
for instance.  If you really care about quantization, you can look at the
code for more detail.

The input layer has no activation function, and ReLU is currently not
supported on the output layer.

This is an example of generating an ANN using LSANN:
    ruby gen_sse.rb 16 32t 64rhh 3l
    
This would give you 16 input nodes (layer zero), 32 nodes in layer 1 with
tanh activation, 64 nodes in layer 2 with hard ReLU activation (forward and
backward), and 3 nodes in the output layer with no applied nonlinearity.
