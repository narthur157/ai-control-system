// Layer 0: 4 nodes
// Layer 1: 32 nodes, tanh activation
// Layer 2: 32 nodes, tanh activation
// Layer 3: 2 nodes, linear activation

#ifndef ANN_HEADER

#include <pmmintrin.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define IN_TMP (mem+0)
#define L1_VAL (mem+4)
#define L1_DEL (mem+36)
#define L1_N0_WEIGHTS (mem+68)
#define L1_N1_WEIGHTS (mem+76)
#define L1_N2_WEIGHTS (mem+84)
#define L1_N3_WEIGHTS (mem+92)
#define L1_N4_WEIGHTS (mem+100)
#define L1_N5_WEIGHTS (mem+108)
#define L1_N6_WEIGHTS (mem+116)
#define L1_N7_WEIGHTS (mem+124)
#define L1_N8_WEIGHTS (mem+132)
#define L1_N9_WEIGHTS (mem+140)
#define L1_N10_WEIGHTS (mem+148)
#define L1_N11_WEIGHTS (mem+156)
#define L1_N12_WEIGHTS (mem+164)
#define L1_N13_WEIGHTS (mem+172)
#define L1_N14_WEIGHTS (mem+180)
#define L1_N15_WEIGHTS (mem+188)
#define L1_N16_WEIGHTS (mem+196)
#define L1_N17_WEIGHTS (mem+204)
#define L1_N18_WEIGHTS (mem+212)
#define L1_N19_WEIGHTS (mem+220)
#define L1_N20_WEIGHTS (mem+228)
#define L1_N21_WEIGHTS (mem+236)
#define L1_N22_WEIGHTS (mem+244)
#define L1_N23_WEIGHTS (mem+252)
#define L1_N24_WEIGHTS (mem+260)
#define L1_N25_WEIGHTS (mem+268)
#define L1_N26_WEIGHTS (mem+276)
#define L1_N27_WEIGHTS (mem+284)
#define L1_N28_WEIGHTS (mem+292)
#define L1_N29_WEIGHTS (mem+300)
#define L1_N30_WEIGHTS (mem+308)
#define L1_N31_WEIGHTS (mem+316)
#define L2_VAL (mem+324)
#define L2_DEL (mem+356)
#define L2_N0_WEIGHTS (mem+388)
#define L2_N1_WEIGHTS (mem+424)
#define L2_N2_WEIGHTS (mem+460)
#define L2_N3_WEIGHTS (mem+496)
#define L2_N4_WEIGHTS (mem+532)
#define L2_N5_WEIGHTS (mem+568)
#define L2_N6_WEIGHTS (mem+604)
#define L2_N7_WEIGHTS (mem+640)
#define L2_N8_WEIGHTS (mem+676)
#define L2_N9_WEIGHTS (mem+712)
#define L2_N10_WEIGHTS (mem+748)
#define L2_N11_WEIGHTS (mem+784)
#define L2_N12_WEIGHTS (mem+820)
#define L2_N13_WEIGHTS (mem+856)
#define L2_N14_WEIGHTS (mem+892)
#define L2_N15_WEIGHTS (mem+928)
#define L2_N16_WEIGHTS (mem+964)
#define L2_N17_WEIGHTS (mem+1000)
#define L2_N18_WEIGHTS (mem+1036)
#define L2_N19_WEIGHTS (mem+1072)
#define L2_N20_WEIGHTS (mem+1108)
#define L2_N21_WEIGHTS (mem+1144)
#define L2_N22_WEIGHTS (mem+1180)
#define L2_N23_WEIGHTS (mem+1216)
#define L2_N24_WEIGHTS (mem+1252)
#define L2_N25_WEIGHTS (mem+1288)
#define L2_N26_WEIGHTS (mem+1324)
#define L2_N27_WEIGHTS (mem+1360)
#define L2_N28_WEIGHTS (mem+1396)
#define L2_N29_WEIGHTS (mem+1432)
#define L2_N30_WEIGHTS (mem+1468)
#define L2_N31_WEIGHTS (mem+1504)
#define L3_VAL (mem+1540)
#define L3_DEL (mem+1544)
#define L3_N0_WEIGHTS (mem+1548)
#define L3_N1_WEIGHTS (mem+1584)
#define OUT_TMP (mem+1620)

float *allocate_ann() {
    return (float *)_mm_malloc(1624 * sizeof(float), 16);
}

void free_ann(float *mem) {
    _mm_free(mem);
}

static void randomize_4(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)4, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=4; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) static void randomize_32(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)32, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=32; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) void randomize_ann(float *mem) {
    int i, j;
    j = 0;
    for (i=0; i<32; i++) {
        randomize_4(L1_N0_WEIGHTS + j);
        j += 8;
    }
    j = 0;
    for (i=0; i<32; i++) {
        randomize_32(L2_N0_WEIGHTS + j);
        j += 36;
    }
    randomize_32(L3_N0_WEIGHTS);
    randomize_32(L3_N1_WEIGHTS);
}

__attribute__((noinline)) static void tanh_32(float *in) {
    const float Dscale = 16777216.0 / log(2.0);
    __m128 Fscale = _mm_set1_ps(Dscale);
    __m128 Foffset = _mm_set1_ps(1065353216.0);
    __m128 Fone = _mm_set1_ps(1.0);
    __m128 Fhalf = _mm_set1_ps(0.5);
    __m128 min = _mm_set1_ps(-32.0);
    __m128 max = _mm_set1_ps(32.0);
    __m128 x, u, v, a, b, g;
    int i;
    for (i=0; i<8; i++) {
        x = _mm_load_ps(in);
        x = _mm_min_ps(x, max);
        x = _mm_max_ps(x, min);
        x = _mm_mul_ps(x, Fscale);
        u = _mm_add_ps(x, Foffset);
        u = (__m128)_mm_cvtps_epi32(u);
        v = _mm_sub_ps(Foffset, x);
        v = (__m128)_mm_cvtps_epi32(v);
        a = _mm_div_ps(_mm_sub_ps(u, Fone), _mm_add_ps(u, Fone));
        b = _mm_div_ps(_mm_sub_ps(v, Fone), _mm_add_ps(v, Fone));
        a = _mm_mul_ps(_mm_sub_ps(a, b), Fhalf);
        _mm_store_ps(in, a);
        in += 4;
    }
}

static float dotprod_4(float *weights, float *values) {
    float sum0, sum1, sum2, sum3;
    __m128 wei, inp, prod, total0, total1, total2, total3;
    float *v, *w;  int i;
    inp    = _mm_load_ps(values+0);
    wei    = _mm_load_ps(weights+0);
    total0 = _mm_mul_ps(inp, wei);
    total0 = _mm_hadd_ps(total0, total0);
    total0 = _mm_hadd_ps(total0, total0);
    _mm_store_ss(&sum0, total0);
    return sum0;
}

__attribute__((noinline)) static float dotprod_32(float *weights, float *values) {
    float sum0, sum1, sum2, sum3;
    __m128 wei, inp, prod, total0, total1, total2, total3;
    float *v, *w;  int i;
    inp    = _mm_load_ps(values+0);
    wei    = _mm_load_ps(weights+0);
    total0 = _mm_mul_ps(inp, wei);
    inp   = _mm_load_ps(values+4);
    wei   = _mm_load_ps(weights+4);
    total1 = _mm_mul_ps(inp, wei);
    inp   = _mm_load_ps(values+8);
    wei   = _mm_load_ps(weights+8);
    total2 = _mm_mul_ps(inp, wei);
    inp   = _mm_load_ps(values+12);
    wei   = _mm_load_ps(weights+12);
    total3 = _mm_mul_ps(inp, wei);
    inp   = _mm_load_ps(values+16);
    wei   = _mm_load_ps(weights+16);
    prod  = _mm_mul_ps(inp, wei);
    total0 = _mm_add_ps(prod, total0);
    inp   = _mm_load_ps(values+20);
    wei   = _mm_load_ps(weights+20);
    prod  = _mm_mul_ps(inp, wei);
    total1 = _mm_add_ps(prod, total1);
    inp   = _mm_load_ps(values+24);
    wei   = _mm_load_ps(weights+24);
    prod  = _mm_mul_ps(inp, wei);
    total2 = _mm_add_ps(prod, total2);
    inp   = _mm_load_ps(values+28);
    wei   = _mm_load_ps(weights+28);
    prod  = _mm_mul_ps(inp, wei);
    total3 = _mm_add_ps(prod, total3);
    total0 = _mm_add_ps(total0, total1);
    total2 = _mm_add_ps(total2, total3);
    total0 = _mm_add_ps(total0, total2);
    total0 = _mm_hadd_ps(total0, total0);
    total0 = _mm_hadd_ps(total0, total0);
    _mm_store_ss(&sum0, total0);
    return sum0;
}

__attribute__((noinline)) static void sum_scaled_32(float *in, float *out, float scale) {
    __m128 tgt, inp, sca;
    sca = _mm_set1_ps(scale);
    inp = _mm_load_ps(in+0);
    tgt = _mm_load_ps(out+0);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+0, tgt);
    inp = _mm_load_ps(in+4);
    tgt = _mm_load_ps(out+4);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+4, tgt);
    inp = _mm_load_ps(in+8);
    tgt = _mm_load_ps(out+8);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+8, tgt);
    inp = _mm_load_ps(in+12);
    tgt = _mm_load_ps(out+12);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+12, tgt);
    inp = _mm_load_ps(in+16);
    tgt = _mm_load_ps(out+16);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+16, tgt);
    inp = _mm_load_ps(in+20);
    tgt = _mm_load_ps(out+20);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+20, tgt);
    inp = _mm_load_ps(in+24);
    tgt = _mm_load_ps(out+24);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+24, tgt);
    inp = _mm_load_ps(in+28);
    tgt = _mm_load_ps(out+28);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+28, tgt);
}

static void sum_scaled_4(float *in, float *out, float scale) {
    __m128 tgt, inp, sca;
    sca = _mm_set1_ps(scale);
    inp = _mm_load_ps(in+0);
    tgt = _mm_load_ps(out+0);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+0, tgt);
}

__attribute__((noinline)) static void mul_tanh_prime_32(float *in, float *out) {
    float i;
    __m128 tgt, inp, one;
    one = _mm_set1_ps(1.0f);
    inp = _mm_load_ps(in+0);
    tgt = _mm_load_ps(out+0);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+0, tgt);
    inp = _mm_load_ps(in+4);
    tgt = _mm_load_ps(out+4);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+4, tgt);
    inp = _mm_load_ps(in+8);
    tgt = _mm_load_ps(out+8);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+8, tgt);
    inp = _mm_load_ps(in+12);
    tgt = _mm_load_ps(out+12);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+12, tgt);
    inp = _mm_load_ps(in+16);
    tgt = _mm_load_ps(out+16);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+16, tgt);
    inp = _mm_load_ps(in+20);
    tgt = _mm_load_ps(out+20);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+20, tgt);
    inp = _mm_load_ps(in+24);
    tgt = _mm_load_ps(out+24);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+24, tgt);
    inp = _mm_load_ps(in+28);
    tgt = _mm_load_ps(out+28);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+28, tgt);
}

static void subtract_2(float *a, float *b, float *c) {
    c[0] = a[0] - b[0];
    c[1] = a[1] - b[1];
}

static void memory_copy_4(float *dst, float *src) {
    dst[0] = src[0];
    dst[1] = src[1];
    dst[2] = src[2];
    dst[3] = src[3];
}

static void memory_copy_2(float *dst, float *src) {
    dst[0] = src[0];
    dst[1] = src[1];
}

__attribute__((noinline)) static void memory_clear_32(float *dst) {
    __m128 zero;
    zero = _mm_setzero_ps();
    _mm_store_ps((float *)dst + 0, zero);
    _mm_store_ps((float *)dst + 4, zero);
    _mm_store_ps((float *)dst + 8, zero);
    _mm_store_ps((float *)dst + 12, zero);
    _mm_store_ps((float *)dst + 16, zero);
    _mm_store_ps((float *)dst + 20, zero);
    _mm_store_ps((float *)dst + 24, zero);
    _mm_store_ps((float *)dst + 28, zero);
}

__attribute__((noinline)) float *forward_L1_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<32; i++) {
        *(L1_VAL+0+i) = dotprod_4(L1_N0_WEIGHTS + k, IN_TMP) + *(L1_N0_WEIGHTS+k+4);
        k += 8;
    }
    tanh_32(L1_VAL);
    return L1_VAL;
}

__attribute__((noinline)) float *forward_L2_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<32; i++) {
        *(L2_VAL+0+i) = dotprod_32(L2_N0_WEIGHTS + k, L1_VAL) + *(L2_N0_WEIGHTS+k+32);
        k += 36;
    }
    tanh_32(L2_VAL);
    return L2_VAL;
}

__attribute__((noinline)) float *forward_L3_ann(float *mem) {
    int i, k;
    *(L3_VAL+0) = dotprod_32(L3_N0_WEIGHTS, L2_VAL) + *(L3_N0_WEIGHTS+32);
    *(L3_VAL+1) = dotprod_32(L3_N1_WEIGHTS, L2_VAL) + *(L3_N1_WEIGHTS+32);
    return L3_VAL;
}

__attribute__((noinline)) float *forward_ann(float *in, float *mem) {
    memory_copy_4(IN_TMP, in);
    forward_L1_ann(mem);
    forward_L2_ann(mem);
    return forward_L3_ann(mem);
}

__attribute__((noinline)) void backward_ann(float *desired, float *mem, float lr) {
    float odel;
    int i, j;
    memory_copy_2(OUT_TMP, desired);

    /* Compute output deltas */
    subtract_2(OUT_TMP, L3_VAL, L3_DEL);

    /* Layer deltas 3 */
    memory_clear_32(L2_DEL);
    sum_scaled_32(L3_N0_WEIGHTS, L2_DEL, *(L3_DEL+0));
    sum_scaled_32(L3_N1_WEIGHTS, L2_DEL, *(L3_DEL+1));
    mul_tanh_prime_32(L2_VAL, L2_DEL);

    /* Layer deltas 2 */
    memory_clear_32(L1_DEL);
    j = 0;
    for (i=0; i<32; i++) {
        sum_scaled_32(L2_N0_WEIGHTS + j, L1_DEL, *(L2_DEL+0+i));
        j += 36;
    }
    mul_tanh_prime_32(L1_VAL, L1_DEL);

    /* Adjust weights */
    j = 0;
    for (i=0; i<32; i++) {
        odel = *(L1_DEL+0+i) * lr;    *(L1_N0_WEIGHTS+j+4) += odel;    sum_scaled_4(IN_TMP, L1_N0_WEIGHTS+j, odel);
        j += 8;
    }

    /* Adjust weights */
    j = 0;
    for (i=0; i<32; i++) {
        odel = *(L2_DEL+0+i) * lr;    *(L2_N0_WEIGHTS+j+32) += odel;    sum_scaled_32(L1_VAL, L2_N0_WEIGHTS+j, odel);
        j += 36;
    }

    /* Adjust weights */
    odel = *(L3_DEL+0) * lr;    *(L3_N0_WEIGHTS+32) += odel;    sum_scaled_32(L2_VAL, L3_N0_WEIGHTS, odel);
    odel = *(L3_DEL+1) * lr;    *(L3_N1_WEIGHTS+32) += odel;    sum_scaled_32(L2_VAL, L3_N1_WEIGHTS, odel);
}

int layer_values_ann[4] = {0, 4, 324, 1540};

#else /* HEADER FOLLOWS */

float *allocate_ann();
void free_ann(float *mem);
#define MEM_SIZE_ann ( 1624 * sizeof(float) )
float *forward_L1_ann(float *mem);
float *forward_L2_ann(float *mem);
float *forward_L3_ann(float *mem);
float *forward_ann(float *in, float *mem);
void backward_ann(float *desired_in, float *mem, float lr);
void randomize_ann(float *mem);
extern int layer_values_ann[4];

#endif
