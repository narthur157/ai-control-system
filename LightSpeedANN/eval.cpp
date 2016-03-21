
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
    FILE *out = fopen("weights.net", "rb");
    if (!out) bail("opening weights");
    size_t bytes_read = fread(mem, 1, MEM_SIZE_ann, out);
    fclose(out);
    
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
            
            cout << line << endl;
        } while (1);
    } while (1);
    
	return 0;
}
