
#define ANN_HEADER
#include "ann.c"
#include "data_set.hpp"
#include <unistd.h>
#include <cmath>
#include <math.h>
#include <signal.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h> 
#include <ctype.h>
#include <sstream>
#include <fstream>
#include <iomanip>      // std::setprecision

using namespace std;

typedef float ValueType;

void bail(const char *s)
{
    cerr << "Error on: " << s << endl;
    exit(0);
}

int listen_socket;
int sock;
struct sockaddr_in my_addr;

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

float denormalizePower(int power) {
	return denormalize((float) power, controlPwrMean, controlPwrStdDev);
}

void setup_socket(int port)
{
    int er;
    listen_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_socket < 0) bail("socket");
    
    int on=1;
    er = setsockopt(listen_socket, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));
    if (er < 0) bail("setsockopt");
        
    memset(&my_addr, 0, sizeof(my_addr));
    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(port);
    my_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    
    er = ::bind(listen_socket, (struct  sockaddr  *)&my_addr,
        (socklen_t)sizeof(struct sockaddr_in));
    if (er < 0) bail("bind");
    
    er = listen(listen_socket, 1);
    if (er < 0) bail("listen");
}

int listen_wait()
{
    socklen_t len = sizeof(struct sockaddr_in);
    int s = accept(listen_socket, (struct  sockaddr  *)&my_addr, &len);
    if (s < 0) return -1;
    return s;
}

int read_line(int sock, string& line)
{
    int i, try_again;
    char c;
    line = "";
    do {
        i = read(sock, &c, 1);
        try_again = (errno==EAGAIN);
		if ((i<1) && (!try_again)) {
			shutdown(sock, 2);
			close(sock);
			return -1;
		}
        if (i>0) {
            if (c == 10) return 0;
            line.push_back(c);
        }
    } while (1);
    return 0;
}

int parse_floats(const char *line, float *f)
{
    int ix = 0;
    const char *p;
    
    if (!isdigit(line[0])) return 0;
    
    do {
        f[ix++] = atof(line);
        p = strchr(line, ' ');
        if (!p) return ix;
        line = p+1;
    } while (1);
}

int power_search(float * inputs, ValueType *mem) {
	//inputs[0] = normalizeSpeed(inputs[0]);
	//inputs[1] = normalizeAngle(inputs[1]);
	//inputs[2] = normalizePower(inputs[2]);
	//inputs[1] = normalizePower(inputs[1]);

	int bestPower = 0;
	float minErr = 666;
	float targetSpeed = inputs[3];
	float testIns[10];
		
	for (int i = 0; i <= 100; i++) {
		//inputs[2] = normalizePower(i);
		testIns[0] = inputs[0];
		testIns[1] = inputs[1];
		testIns[2] = normalizePower(i);

		float predictedSpeed1 = forward_ann(testIns, mem)[0];
		float predictedSpeed2 = forward_ann(testIns, mem)[1];

		float err1 = std::abs(predictedSpeed1 > targetSpeed ? 
						(predictedSpeed1 - targetSpeed) : 
						(targetSpeed - predictedSpeed1));

		float err2 = std::abs(predictedSpeed2 > targetSpeed ? 
						(predictedSpeed2 - targetSpeed) : 
						(targetSpeed - predictedSpeed2));
		
		float err = err1 + (err2 / 2);

		if (err < minErr) {
//			cout << "New best power " << i << " predicts speed of " << denormalizeSpeed(predictedSpeed);
//			cout << " compared to target speed of " << denormalizeSpeed(targetSpeed) << " with err " << err << std::endl;
			minErr = err;
			bestPower = i;
		}
	}
	
	//cout << "Best Power " << bestPower << " found with min err " << minErr << std::endl;
	return bestPower;
}

void eval_socket(ValueType *mem) {
    float inputs[10], *outputs;
    setup_socket(8888);
    
    do {
        cout << "Listening\n";
		cout << "Format: CurSpd Angle CtrlPwr TargetSpd" << std::endl;
        
		int s = listen_wait();

        do {
            //cout << "Reading\n";
            string line;
            int r = read_line(s, line);

            if (r < 0) break;
			

            int n = parse_floats(line.c_str(), inputs);
			
			inputs[0] = normalizeSpeed(inputs[0]);
			inputs[1] = normalizeAngle(inputs[1]);
			inputs[2] = normalizePower(inputs[2]);
			inputs[3] = normalizeSpeed(inputs[3]);
			inputs[4] = 0;
            
			stringstream ss;
            ss << std::setprecision(40);

            ss << power_search(inputs, mem);
//			ss << denormalizeSpeed(forward_ann(inputs, mem)[0]);
//			ss << "\tPredicted power for speed: " << power_search(inputs, mem);
            ss << "\n";

            const string& st(ss.str());
            size_t bytes_written = write(s, st.c_str(), st.size());
            
//            cout << line << endl;
        } while (1);
    } while (1);
}

/*
void make_tables(ValueType *mem) {
	// make a csv file full of various inputs/outputs to the ann
	// for simplicity, fix all numbers at their means and change only 1 at once
	
	float inputs[10];
	
		
	stringstream evalTableStr;

	// 18*18*15 = 4860 loops
	//for(float targetSpeed = 0; targetSpeed <= 900; targetSpeed+=50) {
	for(int testPower = 0; testPower <= 100; testPower++) {
		inputs[0] = normalizePower(testPower);
		inputs[1] = 0;
		inputs[2] = 0;
		
	//	int result = power_search(inputs, mem);
		float result = forward_ann(inputs, mem)[0];

		evalTableStr << testPower << "," << denormalizeSpeed(result) << std::endl;
	}
	
	ofstream evalTable;
	evalTable.open ("evalTable.csv");
	// column headers
	evalTable << "TestPower,PredictedSpeed" << std::endl;
	evalTable << evalTableStr.rdbuf();
	evalTable.close();
}
*/

int main(int argc, char* argv[])
{
    ValueType *mem = allocate_ann();
    
    // READ WEIGHTS
    FILE *out = fopen("weights.net", "rb");
    
	if (!out) {
		out = fopen("LightSpeedANN/weights.net", "rb");
	}
   
	size_t bytes_read = fread(mem, 1, MEM_SIZE_ann, out);
    
	fclose(out);

	setNormalizationData();

	if (argc == 1) {
		eval_socket(mem);    
	}
	
	/*
	if (argc == 2 && argv[1]) {
		make_tables(mem);	
	}
	*/

	return 0;
}
