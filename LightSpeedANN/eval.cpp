
#define ANN_HEADER
#include "ann.c"
#include "data_set.hpp"
#include <unistd.h>
#include <math.h>
#include <signal.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h> 
#include <ctype.h>
#include <sstream>
#include <iomanip>      // std::setprecision

using namespace std;

typedef float ValueType;

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
    computed_dn = new T[data.nOuts()];
    expected_dn = new T[data.nOuts()];
    
    for (i=0; i<data.numSamples(); i++) {
        T *s = data.getSample(i);
        
        T *computed = fw(data.getIns(s), mem);
        T *expected = data.getOuts(s);
        data.denormalizeOuts(computed, computed_dn);
        data.denormalizeOuts(expected, expected_dn);
        
        for (j=0; j<data.nOuts(); j++) {
            if (!isfinite(computed[j])) return false;            
            v = computed_dn[j] - expected_dn[j];
            v = fabs(v);
//            if (v > 10) printf("Wanted %f, got %f\n", expected_dn[j], computed_dn[j]);
            if (v > max) max = v;
            sum += v*v;
			cout << "Sum: " << sum << " max: " << max << endl;
        }
    }
    sum /= (data.numSamples() * data.nOuts());
    sse = sqrt(sum);
    
    delete [] computed_dn;
    delete [] expected_dn;
    
    return true;
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


void bail(const char *s)
{
    cerr << "Error on: " << s << endl;
    exit(0);
}

int listen_socket;
int sock;
struct sockaddr_in my_addr;

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

int main()
{
    ValueType *mem = allocate_ann();
    float inputs[10], *outputs;
    
    // READ WEIGHTS
#if 1
    FILE *out = fopen("weights1.net", "rb");
    if (!out) bail("opening weights");
    size_t bytes_read = fread(mem, 1, MEM_SIZE_ann, out);
    fclose(out);
#endif
    
    setup_socket(8888);
    
    do {
        //cout << "Listening\n";
        int s = listen_wait();
        do {
            //cout << "Reading\n";
            string line;
            int r = read_line(s, line);
            if (r < 0) break;
            
            int n = parse_floats(line.c_str(), inputs);
            outputs = forward_ann(inputs, mem);
            stringstream ss;
            ss << std::setprecision(40);
            for (int i=0; i<4; i++) {
                ss << outputs[i];
                if (i != 3) ss << " ";
            }
            ss << "\n";
            const string& st(ss.str());
            size_t bytes_written = write(s, st.c_str(), st.size());
            
            // for (int i=0; i<n; i++) cout << inputs[i] << " ";
            // cout << endl;
            
            cout << line << endl;
        } while (1);
    } while (1);

#if 0    
    for (int i=0; i<1000000 && !global_quit; i++) {
        lr = find_learning_rate(lr, data, mem, forward_ann, backward_ann, MEM_SIZE_ann);    
        for (int j=0; j<1000; j++) {
            //printf("%d    \r", j);
            //fflush(stdout);
            training_epoch(data, mem, forward_ann, backward_ann, lr);
        }
        compute_err(data, mem, forward_ann, sse, max);
        cout << lr << " " << sse << " " << max << endl;
        
        if (sse < best_sse || max < best_max) {
            best_sse = sse;
            best_max = max;
            
        }
    }
    
    data.print_stats(cout);
#endif
    return 0;
}
