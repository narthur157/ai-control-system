
#define ANN_HEADER
#include "ann.c"
#include <pmmintrin.h>
#include "data_set.hpp"
#include <math.h>
#include <fstream>
#include <signal.h>

using namespace std;

typedef float ValueType;

float loadSpdMean, loadSpdStdDev, angleMean,
	  angleStdDev, controlPwrMean, controlPwrStdDev;

/*
			loadSpdMean = scan.nextDouble();
			loadSpdStdDev = scan.nextDouble();
			angleMean = scan.nextDouble();
			angleStdDev = scan.nextDouble();
			controlPwrMean = scan.nextDouble();
			controlPwrStdDev = scan.nextDouble();
*/
void setNormalizationData() {
	std::ifstream normalizationFile("../normalizationData");
	std::string line; 

	normalizationFile >> loadSpdMean;
	normalizationFile >> loadSpdStdDev;
	normalizationFile >> angleMean;
	normalizationFile >> angleStdDev;
	normalizationFile >> controlPwrMean;
	normalizationFile >> controlPwrStdDev;

	cout << "Using normalizationData" << std::endl;
	cout << "LdSpdMean " << loadSpdMean << "\tLdSpdStdDev " << loadSpdStdDev << std::endl;
	cout << "AngleMean " << angleMean << "\tAngleStdDEv " << angleStdDev << std::endl;
	cout << "ControlPwrMean " << controlPwrMean << "\tControlPwrStdDev " << controlPwrStdDev << std::endl;
	
	normalizationFile.close();
}

float normalize(float val, float mean, float dev) {
	return (val-mean)/dev;
}

float denormalize(float val, float mean, float dev) {
	return (val * dev) + mean;
}

float normalizeSpeed(float speed) {
	return normalize(speed, loadSpdMean, loadSpdStdDev);
}

float denormalizeSpeed(float speed) {
	return denormalize(speed, loadSpdMean, loadSpdStdDev);
}

float normalizeAngle(float angle) {
	return normalize(angle, angleMean, angleStdDev);
}

float denormalizeAngle(float angle) {
	return denormalize(angle, angleMean, angleStdDev);
}

float normalizePower(int power) {
	return normalize((float) power, controlPwrMean, controlPwrStdDev);
}

float denormalizePower(float power) {
	return denormalize(power, controlPwrMean, controlPwrStdDev);
}


template<typename T>
bool compute_err(
    DataSet<T>& data, 
    T* mem,
    T *(*fw)(T *in, T *mem),
    T& sse,
    T& max)
{
    int i, j;
    T sum = 0, v;
    max = 0;
    
    T *computed_dn, *expected_dn;
//    computed_dn = new T[data.nOuts()];
//    expected_dn = new T[data.nOuts()];
	
	//cout << "numSamples: " << data.numSamples() << " nOuts: " << data.nOuts() << endl;
    for (i=0; i<data.numSamples(); i++) {
        T *s = data.getSample(i);
        
        T *computed = fw(data.getIns(s), mem);
        T *expected = data.getOuts(s);

		computed_dn = computed;
		expected_dn = expected;
		//data.denormalizeOuts(computed, computed_dn);
        //data.denormalizeOuts(expected, expected_dn);
        for (j=0; j<data.nOuts(); j++) {
			//cout << "computed[j]: " << computed[j] << endl;
    
			if (!isfinite(computed[j])) {
				cout << "Not finite" << std::endl;
				max = sum = 1.0/0.0;
				return false;            
			}

            v = computed_dn[j] - expected_dn[j];
            v = fabs(v);

			if (computed_dn[j] > 100) {
				cout << "Large computed: " << computed_dn[j] << std::endl;
			}

            if (v > 3) {
				printf("Wanted %f, got %f\n", denormalizeSpeed(expected_dn[j]), denormalizeSpeed(computed_dn[j]));
				cout << "LdSpd\t" << denormalizeSpeed(data.getIns(s)[0]);
//				cout << "\tAngle\t" << denormalizeAngle(data.getIns(s)[1]);
				cout << "\tCtrlPwr\t" << denormalizePower(data.getIns(s)[1]) << std::endl;
//				cout << "getSample(" << i << ") in " << data.getIns(s)[0] << std::endl;
			}

            if (v > max) max = v;
            sum += v*v;
			//cout << "Sum: " << sum << " max: " << max << endl;
        }
    }
   
	sum /= (data.numSamples() * data.nOuts());
    sse = sqrt(sum);
    
 //   delete [] computed_dn;
  //  delete [] expected_dn;
    
    return true;
}

template<typename T>
void weight_decay(T *mem, T lr)
{
    int i;
    __m128 sca, x;
    sca = _mm_set1_ps(1.0f - lr*0.1f);
    //sca = _mm_set1_ps(1.0f - lr);
    for (i=0; i<MEM_SIZE_ann/4; i+=4) {
        x = _mm_load_ps(mem + i);
        x = _mm_mul_ps(x, sca);
        _mm_store_ps(mem + i, x);
    }
}

template<typename T>
void training_epoch(
    DataSet<T>& data,
    T* mem,
    T *(*fw)(T *in, T *mem),
    void (*bk)(T *des, T *mem, T lr),
    T lr)
{
    int i;
	weight_decay(mem, lr);
    for (i=0; i<data.numSamples(); i++) {
        T *s = data.getSample(i);
        T *o = fw(data.getIns(s), mem);
        // printf("s%d ", i);
        // for (int j=0; j<data.nOuts(); j++) {
        //     printf("%g ", o[j]);
        // }
        // printf("\n");
        bk(data.getOuts(s), mem, lr);
    }
}

template<typename T>
T find_learning_rate(T old_lr, DataSet<T>& data, 
    T* mem,
    T *(*fw)(T *in, T *mem),
    void (*bk)(T *des, T *mem, T lr),
    long memsize)
{
    T *backup = (T*)malloc(memsize);
    bool up = false, did_up = false, did_down = false;
    T last_sse, sse, max, last_max;
    T r_down = 0.99;
    T r_up = 1.0 / r_down;
    bool good;
    
    compute_err(data, mem, fw, last_sse, last_max);

    int i = 0;
    while ((!did_up || !did_down) && i<10) {
        memcpy(backup, mem, memsize);
        sse = 1000000;
        max = 1000000;
        for (int j=0; j<10; j++) {
            training_epoch(data, mem, fw, bk, old_lr * 2.0f);
            T ssei, maxi;
            good = compute_err(data, mem, fw, ssei, maxi);
            //sse += ssei;
            //max += maxi;
            //if (ssei < sse) sse = ssei;
            //if (maxi < max) max = maxi;
            sse = ssei;
            max = maxi;
        }
        //sse /= 2;
        //max /= 2;
        memcpy(mem, backup, memsize);
        
        if (sse > last_sse && max > last_max) good = false;
        if (!isfinite(sse) || !isfinite(max) || fabs(sse) > 1000000000.0 || fabs(max) > 1000000000.0) {
            good = false;
            i = 0;
        }
        
        if (good) {
            old_lr *= r_up;
            did_up = true;
        } else {
            old_lr *= r_down;
            did_down = true;
        }        
        
        i++;
        //cout << old_lr << " " << last_sse << " " << sse << endl;
    }
    
    free(backup);
    return old_lr;
}


int global_quit = 0;
static void catch_sigint(int signo) {
    global_quit = 1;
}

int main(int argc, char* argv[])
{
	// default file names for testing and training
	string testFile = "test-set.csv";
	string trainFile = "train-set.csv";

	setNormalizationData();

	ValueType lr = 0.10;

	if (argc == 2) {
		cout << "Using given learning rate " << argv[1] << endl;
		lr = stof(argv[1]);
	}
	
    DataSet<ValueType> trainData(2, 1), testData(2, 1);
    trainData.loadFile("iiioo", trainFile);
    testData.loadFile("iioo", testFile);
	
    
    ValueType sse, max;
    ValueType best_sse = 1000000, best_max = 1000000;
	ValueType *mem = allocate_ann();

    FILE *out = fopen("weights.net", "rb");

    if (!out) {
		cout << "No weights file found, using random weights" << endl;
		randomize_ann(mem);
	}
	else {
		size_t bytes_read = fread(mem, 1, MEM_SIZE_ann, out);
		compute_err(testData, mem, forward_ann, sse, max);
        
        best_sse = sse;
        best_max = max;
	}

    signal(SIGINT, catch_sigint);
    
    compute_err(testData, mem, forward_ann, sse, max);
    cout << lr << " " << sse << " " << max << endl;

	for (int i = 0; i < 10000 && !global_quit; i++) {
		if (argc == 1) {
			lr = find_learning_rate(lr, trainData, mem, forward_ann, backward_ann, MEM_SIZE_ann);    
		}
		for (int j=0; j<100; j++) {
            //printf("%d    \r", j);
            //fflush(stdout);
            training_epoch(trainData, mem, forward_ann, backward_ann, lr);
        }

        compute_err(testData, mem, forward_ann, sse, max);
        cout << lr << " " << sse << " " << max << endl;
        
        if (sse < best_sse) {
            best_sse = sse;
            best_max = max;
            
            FILE *out = fopen("weights.net", "wb");
            fwrite(mem, 1, MEM_SIZE_ann, out);
            fclose(out);
        }
    }
    
    trainData.print_stats(cout);
    return 0;
}

