#ruby gen_sse.rb 2 32 32 32 5 > net_2_32_32_32_5_double.c
#ruby gen_sse.rb 2 16 16 1 > net_2_16_16_1_double.c
#ruby gen_sse.rb 2 32 32 5 > net_2_32_32_5_double.c

all: 
	g++ ann.c data_set.cpp train.cpp  -std=c++11 -O3 -msse3 -o train
	g++ ann.c data_set.cpp eval.cpp  -std=c++11 -O3 -msse3 -o eval 
train: 
	./train
eval: 
	./eval
