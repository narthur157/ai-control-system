// Layer 0: 1 nodes
// Layer 1: 8 nodes, tanh activation
// Layer 2: 8 nodes, tanh activation
// Layer 3: 1 nodes, linear activation

#ifndef ANN_HEADER

#include <pmmintrin.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define IN_TMP (mem+0)
#define L1_VAL (mem+4)
#define L1_DEL (mem+12)
#define L1_N0_WEIGHTS (mem+20)
#define L1_N1_WEIGHTS (mem+24)
#define L1_N2_WEIGHTS (mem+28)
#define L1_N3_WEIGHTS (mem+32)
#define L1_N4_WEIGHTS (mem+36)
#define L1_N5_WEIGHTS (mem+40)
#define L1_N6_WEIGHTS (mem+44)
#define L1_N7_WEIGHTS (mem+48)
#define L2_VAL (mem+52)
#define L2_DEL (mem+60)
#define L2_N0_WEIGHTS (mem+68)
#define L2_N1_WEIGHTS (mem+80)
#define L2_N2_WEIGHTS (mem+92)
#define L2_N3_WEIGHTS (mem+104)
#define L2_N4_WEIGHTS (mem+116)
#define L2_N5_WEIGHTS (mem+128)
#define L2_N6_WEIGHTS (mem+140)
#define L2_N7_WEIGHTS (mem+152)
#define L3_VAL (mem+164)
#define L3_DEL (mem+168)
#define L3_N0_WEIGHTS (mem+172)
#define OUT_TMP (mem+184)

float *allocate_ann() {
    return (float *)_mm_malloc(188 * sizeof(float), 16);
}

void free_ann(float *mem) {
    _mm_free(mem);
}

static void randomize_1(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)1, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=1; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

static void randomize_8(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)8, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=8; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) void randomize_ann(float *mem) {
    int i, j;
    j = 0;
    for (i=0; i<8; i++) {
        randomize_1(L1_N0_WEIGHTS + j);
        j += 4;
    }
    j = 0;
    for (i=0; i<8; i++) {
        randomize_8(L2_N0_WEIGHTS + j);
        j += 12;
    }
    randomize_8(L3_N0_WEIGHTS);
}

__attribute__((noinline)) static void tanh_8(float *in) {
    const float Dscale = 16777216.0 / log(2.0);
    __m128 Fscale = _mm_set1_ps(Dscale);
    __m128 Foffset = _mm_set1_ps(1065353216.0);
    __m128 Fone = _mm_set1_ps(1.0);
    __m128 Fhalf = _mm_set1_ps(0.5);
    __m128 min = _mm_set1_ps(-32.0);
    __m128 max = _mm_set1_ps(32.0);
    __m128 x, u, v, a, b, g;
    int i;
    for (i=0; i<2; i++) {
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

static float dotprod_1(float *weights, float *values) {
    float sum0, sum1, sum2, sum3;
    sum0 = values[0] * weights[0];
    return sum0;
}

static float dotprod_8(float *weights, float *values) {
    float sum0, sum1, sum2, sum3;
    __m128 wei, inp, prod, total0, total1, total2, total3;
    float *v, *w;  int i;
    inp    = _mm_load_ps(values+0);
    wei    = _mm_load_ps(weights+0);
    total0 = _mm_mul_ps(inp, wei);
    inp   = _mm_load_ps(values+4);
    wei   = _mm_load_ps(weights+4);
    total1 = _mm_mul_ps(inp, wei);
    total0 = _mm_add_ps(total0, total1);
    total0 = _mm_hadd_ps(total0, total0);
    total0 = _mm_hadd_ps(total0, total0);
    _mm_store_ss(&sum0, total0);
    return sum0;
}

static void sum_scaled_8(float *in, float *out, float scale) {
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
}

static void sum_scaled_1(float *in, float *out, float scale) {
    out[0] += scale * in[0];
}

static void mul_tanh_prime_8(float *in, float *out) {
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
}

static void subtract_1(float *a, float *b, float *c) {
    c[0] = a[0] - b[0];
}

static void memory_copy_1(float *dst, float *src) {
    dst[0] = src[0];
}

static void memory_clear_8(float *dst) {
    __m128 zero;
    zero = _mm_setzero_ps();
    _mm_store_ps((float *)dst + 0, zero);
    _mm_store_ps((float *)dst + 4, zero);
}

__attribute__((noinline)) float *forward_L1_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<8; i++) {
        *(L1_VAL+0+i) = dotprod_1(L1_N0_WEIGHTS + k, IN_TMP) + *(L1_N0_WEIGHTS+k+1);
        k += 4;
    }
    tanh_8(L1_VAL);
    return L1_VAL;
}

__attribute__((noinline)) float *forward_L2_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<8; i++) {
        *(L2_VAL+0+i) = dotprod_8(L2_N0_WEIGHTS + k, L1_VAL) + *(L2_N0_WEIGHTS+k+8);
        k += 12;
    }
    tanh_8(L2_VAL);
    return L2_VAL;
}

__attribute__((noinline)) float *forward_L3_ann(float *mem) {
    int i, k;
    *(L3_VAL+0) = dotprod_8(L3_N0_WEIGHTS, L2_VAL) + *(L3_N0_WEIGHTS+8);
    return L3_VAL;
}

__attribute__((noinline)) float *forward_ann(float *in, float *mem) {
    memory_copy_1(IN_TMP, in);
    forward_L1_ann(mem);
    forward_L2_ann(mem);
    return forward_L3_ann(mem);
}

__attribute__((noinline)) void backward_ann(float *desired, float *mem, float lr) {
    float odel;
    int i, j;
    memory_copy_1(OUT_TMP, desired);

    /* Compute output deltas */
    subtract_1(OUT_TMP, L3_VAL, L3_DEL);

    /* Layer deltas 3 */
    memory_clear_8(L2_DEL);
    sum_scaled_8(L3_N0_WEIGHTS, L2_DEL, *(L3_DEL+0));
    mul_tanh_prime_8(L2_VAL, L2_DEL);

    /* Layer deltas 2 */
    memory_clear_8(L1_DEL);
    j = 0;
    for (i=0; i<8; i++) {
        sum_scaled_8(L2_N0_WEIGHTS + j, L1_DEL, *(L2_DEL+0+i));
        j += 12;
    }
    mul_tanh_prime_8(L1_VAL, L1_DEL);

    /* Adjust weights */
    j = 0;
    for (i=0; i<8; i++) {
        odel = *(L1_DEL+0+i) * lr;    *(L1_N0_WEIGHTS+j+1) += odel;    sum_scaled_1(IN_TMP, L1_N0_WEIGHTS+j, odel);
        j += 4;
    }

    /* Adjust weights */
    j = 0;
    for (i=0; i<8; i++) {
        odel = *(L2_DEL+0+i) * lr;    *(L2_N0_WEIGHTS+j+8) += odel;    sum_scaled_8(L1_VAL, L2_N0_WEIGHTS+j, odel);
        j += 12;
    }

    /* Adjust weights */
    odel = *(L3_DEL+0) * lr;    *(L3_N0_WEIGHTS+8) += odel;    sum_scaled_8(L2_VAL, L3_N0_WEIGHTS, odel);
}

int layer_values_ann[4] = {0, 4, 52, 164};

#else /* HEADER FOLLOWS */

float *allocate_ann();
void free_ann(float *mem);
#define MEM_SIZE_ann ( 188 * sizeof(float) )
float *forward_L1_ann(float *mem);
float *forward_L2_ann(float *mem);
float *forward_L3_ann(float *mem);
float *forward_ann(float *in, float *mem);
void backward_ann(float *desired_in, float *mem, float lr);
void randomize_ann(float *mem);
extern int layer_values_ann[4];

#endif
