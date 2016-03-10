// Layer 0: 3 nodes
// Layer 1: 16 nodes, tanh activation
// Layer 2: 16 nodes, tanh activation
// Layer 3: 4 nodes, linear activation

#ifndef ANN_HEADER

#include <pmmintrin.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define IN_TMP (mem+0)
#define L1_VAL (mem+4)
#define L1_DEL (mem+20)
#define L1_N0_WEIGHTS (mem+36)
#define L1_N1_WEIGHTS (mem+40)
#define L1_N2_WEIGHTS (mem+44)
#define L1_N3_WEIGHTS (mem+48)
#define L1_N4_WEIGHTS (mem+52)
#define L1_N5_WEIGHTS (mem+56)
#define L1_N6_WEIGHTS (mem+60)
#define L1_N7_WEIGHTS (mem+64)
#define L1_N8_WEIGHTS (mem+68)
#define L1_N9_WEIGHTS (mem+72)
#define L1_N10_WEIGHTS (mem+76)
#define L1_N11_WEIGHTS (mem+80)
#define L1_N12_WEIGHTS (mem+84)
#define L1_N13_WEIGHTS (mem+88)
#define L1_N14_WEIGHTS (mem+92)
#define L1_N15_WEIGHTS (mem+96)
#define L2_VAL (mem+100)
#define L2_DEL (mem+116)
#define L2_N0_WEIGHTS (mem+132)
#define L2_N1_WEIGHTS (mem+152)
#define L2_N2_WEIGHTS (mem+172)
#define L2_N3_WEIGHTS (mem+192)
#define L2_N4_WEIGHTS (mem+212)
#define L2_N5_WEIGHTS (mem+232)
#define L2_N6_WEIGHTS (mem+252)
#define L2_N7_WEIGHTS (mem+272)
#define L2_N8_WEIGHTS (mem+292)
#define L2_N9_WEIGHTS (mem+312)
#define L2_N10_WEIGHTS (mem+332)
#define L2_N11_WEIGHTS (mem+352)
#define L2_N12_WEIGHTS (mem+372)
#define L2_N13_WEIGHTS (mem+392)
#define L2_N14_WEIGHTS (mem+412)
#define L2_N15_WEIGHTS (mem+432)
#define L3_VAL (mem+452)
#define L3_DEL (mem+456)
#define L3_N0_WEIGHTS (mem+460)
#define L3_N1_WEIGHTS (mem+480)
#define L3_N2_WEIGHTS (mem+500)
#define L3_N3_WEIGHTS (mem+520)
#define OUT_TMP (mem+540)

float *allocate_ann() {
    return (float *)_mm_malloc(544 * sizeof(float), 16);
}

void free_ann(float *mem) {
    _mm_free(mem);
}

static void randomize_3(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)3, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=3; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) static void randomize_16(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)16, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=16; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) void randomize_ann(float *mem) {
    int i, j;
    j = 0;
    for (i=0; i<16; i++) {
        randomize_3(L1_N0_WEIGHTS + j);
        j += 4;
    }
    j = 0;
    for (i=0; i<16; i++) {
        randomize_16(L2_N0_WEIGHTS + j);
        j += 20;
    }
    j = 0;
    for (i=0; i<4; i++) {
        randomize_16(L3_N0_WEIGHTS + j);
        j += 20;
    }
}

__attribute__((noinline)) static void tanh_16(float *in) {
    const float Dscale = 16777216.0 / log(2.0);
    __m128 Fscale = _mm_set1_ps(Dscale);
    __m128 Foffset = _mm_set1_ps(1065353216.0);
    __m128 Fone = _mm_set1_ps(1.0);
    __m128 Fhalf = _mm_set1_ps(0.5);
    __m128 x, u, v, a, b;
    int i;
    for (i=0; i<4; i++) {
        x = _mm_load_ps(in);
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

static float dotprod_3(float *weights, float *values) {
    float sum0, sum1, sum2, sum3;
    sum0 = values[0] * weights[0];
    sum1 = values[1] * weights[1];
    sum2 = values[2] * weights[2];
    return (sum0 + sum1) + sum2;
}

__attribute__((noinline)) static float dotprod_16(float *weights, float *values) {
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
    total0 = _mm_add_ps(total0, total1);
    total2 = _mm_add_ps(total2, total3);
    total0 = _mm_add_ps(total0, total2);
    total0 = _mm_hadd_ps(total0, total0);
    total0 = _mm_hadd_ps(total0, total0);
    _mm_store_ss(&sum0, total0);
    return sum0;
}

__attribute__((noinline)) static void sum_scaled_16(float *in, float *out, float scale) {
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
}

static void sum_scaled_3(float *in, float *out, float scale) {
    out[0] += scale * in[0];
    out[1] += scale * in[1];
    out[2] += scale * in[2];
}

__attribute__((noinline)) static void mul_tanh_prime_16(float *in, float *out) {
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
}

static void subtract_4(float *a, float *b, float *c) {
    __m128 ina, inb;
    ina = _mm_load_ps(a + 0);
    inb = _mm_load_ps(b + 0);
    ina = _mm_sub_ps(ina, inb);
    _mm_store_ps(c + 0, ina);
}

static void memory_copy_3(float *dst, float *src) {
    dst[0] = src[0];
    dst[1] = src[1];
    dst[2] = src[2];
}

static void memory_copy_4(float *dst, float *src) {
    dst[0] = src[0];
    dst[1] = src[1];
    dst[2] = src[2];
    dst[3] = src[3];
}

__attribute__((noinline)) static void memory_clear_16(float *dst) {
    __m128 zero;
    zero = _mm_setzero_ps();
    _mm_store_ps((float *)dst + 0, zero);
    _mm_store_ps((float *)dst + 4, zero);
    _mm_store_ps((float *)dst + 8, zero);
    _mm_store_ps((float *)dst + 12, zero);
}

__attribute__((noinline)) float *forward_L1_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<16; i++) {
        *(L1_VAL+0+i) = dotprod_3(L1_N0_WEIGHTS + k, IN_TMP) + *(L1_N0_WEIGHTS+k+3);
        k += 4;
    }
    tanh_16(L1_VAL);
    return L1_VAL;
}

__attribute__((noinline)) float *forward_L2_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<16; i++) {
        *(L2_VAL+0+i) = dotprod_16(L2_N0_WEIGHTS + k, L1_VAL) + *(L2_N0_WEIGHTS+k+16);
        k += 20;
    }
    tanh_16(L2_VAL);
    return L2_VAL;
}

__attribute__((noinline)) float *forward_L3_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<4; i++) {
        *(L3_VAL+0+i) = dotprod_16(L3_N0_WEIGHTS + k, L2_VAL) + *(L3_N0_WEIGHTS+k+16);
        k += 20;
    }
    return L3_VAL;
}

__attribute__((noinline)) float *forward_ann(float *in, float *mem) {
    memory_copy_3(IN_TMP, in);
    forward_L1_ann(mem);
    forward_L2_ann(mem);
    return forward_L3_ann(mem);
}

__attribute__((noinline)) void backward_ann(float *desired, float *mem, float lr) {
    float odel;
    int i, j;
    memory_copy_4(OUT_TMP, desired);

    /* Compute output deltas */
    subtract_4(OUT_TMP, L3_VAL, L3_DEL);

    /* Layer deltas 3 */
    memory_clear_16(L2_DEL);
    j = 0;
    for (i=0; i<4; i++) {
        sum_scaled_16(L3_N0_WEIGHTS + j, L2_DEL, *(L3_DEL+0+i));
        j += 20;
    }
    mul_tanh_prime_16(L2_VAL, L2_DEL);

    /* Layer deltas 2 */
    memory_clear_16(L1_DEL);
    j = 0;
    for (i=0; i<16; i++) {
        sum_scaled_16(L2_N0_WEIGHTS + j, L1_DEL, *(L2_DEL+0+i));
        j += 20;
    }
    mul_tanh_prime_16(L1_VAL, L1_DEL);

    /* Adjust weights */
    j = 0;
    for (i=0; i<16; i++) {
        odel = *(L1_DEL+0+i) * lr;    *(L1_N0_WEIGHTS+j+3) += odel;    sum_scaled_3(IN_TMP, L1_N0_WEIGHTS+j, odel);
        j += 4;
    }

    /* Adjust weights */
    j = 0;
    for (i=0; i<16; i++) {
        odel = *(L2_DEL+0+i) * lr;    *(L2_N0_WEIGHTS+j+16) += odel;    sum_scaled_16(L1_VAL, L2_N0_WEIGHTS+j, odel);
        j += 20;
    }

    /* Adjust weights */
    j = 0;
    for (i=0; i<4; i++) {
        odel = *(L3_DEL+0+i) * lr;    *(L3_N0_WEIGHTS+j+16) += odel;    sum_scaled_16(L2_VAL, L3_N0_WEIGHTS+j, odel);
        j += 20;
    }
}

int layer_values_ann[4] = {0, 4, 100, 452};

#else /* HEADER FOLLOWS */

float *allocate_ann();
void free_ann(float *mem);
#define MEM_SIZE_ann ( 544 * sizeof(float) )
float *forward_L1_ann(float *mem);
float *forward_L2_ann(float *mem);
float *forward_L3_ann(float *mem);
float *forward_ann(float *in, float *mem);
void backward_ann(float *desired_in, float *mem, float lr);
void randomize_ann(float *mem);
extern int layer_values_ann[4];

#endif
