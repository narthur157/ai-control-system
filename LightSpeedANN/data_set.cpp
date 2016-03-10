
#include "data_set.hpp"
#include <iostream>
#include <fstream>
#include <string>
#include <math.h>
#include <iomanip>

using namespace std;

template<typename T>
T* DataSet<T>::add(T* a, T* b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] + b[i];
    }
    return c;
}

template<typename T>
T* DataSet<T>::sub(T* a, T* b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] - b[i];
    }
    return c;
}

template<typename T>
T* DataSet<T>::mul(T* a, T* b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] * b[i];
    }
    return c;
}

template<typename T>
T* DataSet<T>::mul(T* a, T b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] * b;
    }
    return c;
}

template<typename T>
T* DataSet<T>::div(T* a, T* b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] / b[i];
    }
    return c;
}

template<typename T>
T* DataSet<T>::div(T* a, T b, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] / b;
    }
    return c;
}

template<typename T>
T* DataSet<T>::sqrt(T* a, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = ::sqrt(a[i]);
    }
    return c;
}

template<typename T>
T* DataSet<T>::sqr(T* a, T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = a[i] * a[i];
    }
    return c;
}

template<typename T>
T* DataSet<T>::zero(T* c)
{
    int i;
    for (i=0; i<N; i++) {
        c[i] = 0;
    }
    return c;
}

template<typename T>
DataSet<T>::DataSet(int i, int o) : N_INS(i), N_OUTS(o), N(i+o)
{
    avgs = allocSample();
    covars = allocSample();
}

template<typename T>
DataSet<T>::~DataSet() 
{
    for (auto i=samples.begin(); i!=samples.end(); ++i) {
        freeSample(*i);
    }
    samples.clear();
    freeSample(avgs);       avgs = 0;
    freeSample(covars);     covars = 0;
}
    
template<typename T>
void DataSet<T>::normalize()
{
    zero(avgs);
    zero(covars);
    T* tmp = allocSample();
    
    for (auto i=samples.begin(); i != samples.end(); ++i) {
        add(avgs, *i, avgs);
    }
    div(avgs, (T)samples.size(), avgs);
    
    for (auto i=samples.begin(); i != samples.end(); ++i) {
        sub(*i, avgs, tmp);
        sqr(tmp, tmp);
        add(covars, tmp, covars);
    }
    div(covars, (T)samples.size(), covars);
    sqrt(covars, covars);
    
    for (auto i=samples.begin(); i != samples.end(); ++i) {
        sub(*i, avgs, *i);
        div(*i, covars, *i);
    }
    
    freeSample(tmp);
}

template<typename T>
T* DataSet<T>::denormalizeOuts(T* s, T* t)
{
    for (int i=0; i<N_OUTS; i++) {
        t[i] = s[i] * covars[i+N_INS] + avgs[i+N_INS];
    }
    return t;
}

template<typename T>
void DataSet<T>::print_stats(ostream& os)
{
    for (int i=0; i<N; i++) {
        os << setprecision(40) << i << " " << avgs[i] << " " << covars[i] << endl;
    }
}


template<typename T>
void DataSet<T>::loadFile(const string& pattern, istream& is)
{
    int in, out, i, n, linen = 0;

    n = pattern.size();
    in = 0;
    i = 0;
    out = 0;
    T* s = allocSample();
    T t;
    string line, str;
    const char *p, *q;
    
    getline(is, line);
    if (is.bad()) goto end;
    p = line.c_str();
    q = p + line.length();

    for (;;) {
        //is >> t;
        //is >> str;
        //cout << str << endl;
        //t = atof(str.c_str());

        while (isspace(*p) && p<q) p++;
        str = "";
        while (!isspace(*p) && p<q) str += *p++;
        while (isspace(*p) && p<q) p++;
        t = atof(str.c_str());
        
        switch (pattern[i]) {
        case 'x':
            break;
        case 'i':
            getIns(s)[in++] = t;
            break;
        case 'o':
            getOuts(s)[out++] = t;
            break;
        }

        i++;
        if (i >= n) {
            if (p != q) {
                cerr << "Line " << linen << " too long\n";
                exit(0);
            }
            i = 0;
            in = 0;
            out = 0;
            samples.push_back(s);
            s = allocSample();
            getline(is, line);
            if (is.bad()) break;
            p = line.c_str();
            q = p + line.length();
            if (p == q) break;
            str = "";
            linen++;
        } else {
            if (p == q) {
                cerr << "Line " << linen << " too short, i=" << i << " out of " << n << "\n";
                exit(0);
            }
        }
    }
    
end:
    freeSample(s);
}

template<typename T>
int DataSet<T>::loadFile(const string& pattern, const string& fname)
{
    ifstream file(fname);
    if (!file.is_open()) return 0;
    DataSet::loadFile(pattern, file);
    file.close();
    return 1;
}

template<typename T>
void DataSet<T>::randomOrder()
{
    int i, j;
    for (i=0; i<samples.size(); i++) {
        j = random() % samples.size();
        if (i == j) continue;
        swap(samples[i], samples[j]);
    }
}

template<typename T>
void DataSet<T>::dump()
{
    int i, j;
    for (i=0; i<samples.size(); i++) {
        printf("%6d: ", i);
        for (j=0; j<N; j++) {
            printf("%10.7f ", (double)samples[i][j]);
        }
        printf("\n");
    }
}

template class DataSet<float>;
template class DataSet<double>;
template class DataSet<long double>;
