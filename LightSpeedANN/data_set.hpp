
#include <vector>
#include <string>
#include <string.h>
#include <iostream>

template<typename T>
class DataSet {
public:
    std::vector<T*> samples;
    T *avgs, *covars;
    int N_INS, N_OUTS, N;
    
    T* add(T* a, T* b, T* c);
    T* sub(T* a, T* b, T* c);
    T* mul(T* a, T* b, T* c);
    T* div(T* a, T* b, T* c);
    T* mul(T* a, T b, T* c);
    T* div(T* a, T b, T* c);
    T* sqrt(T* a, T* c);
    T* sqr(T* a, T* c);
    T* zero(T* a);
     
public:
    DataSet(int i, int o);
    ~DataSet();
    
    int nIns() { return N_INS; }
    int nOuts() { return N_OUTS; }
    
    T* allocSample() { return new T[N]; }
    void freeSample(T* s) { delete [] s; }
    
    T* getIns(T* s) { return s; }
    T* getOuts(T* s) { return s + N_INS; }
    T* denormalizeOuts(T* s, T* t);
    void copyIns(T* to, const T* fr) { memcpy(to, fr, sizeof(T) * N_INS); }
    void copyOuts(T* to, const T* fr) { memcpy(to+N_INS, fr, sizeof(T) * N_OUTS); }
    
    void addSample(const T* in, const T* out) {
        T* s = allocSample();
        copyIns(s, in);
        copyOuts(s, out);
        samples.push_back(s);
    }
    
    T* getSample(int ix) {
        return samples[ix];
    }
    int numSamples() { return samples.size(); }
    
    void normalize();
    void randomOrder();
    
    void loadFile(const std::string& pattern, std::istream& is);
    int loadFile(const std::string& pattern, const std::string& fname);
    
    void print_stats(std::ostream& os);
    
    
    void dump();
};
