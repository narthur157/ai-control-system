// Layer 0: 3 nodes
// Layer 1: 64 nodes, tanh activation
// Layer 2: 64 nodes, tanh activation
// Layer 3: 1 nodes, linear activation

#ifndef ANN_HEADER

#include <pmmintrin.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define IN_TMP (mem+0)
#define L1_VAL (mem+4)
#define L1_DEL (mem+68)
#define L1_N0_WEIGHTS (mem+132)
#define L1_N1_WEIGHTS (mem+136)
#define L1_N2_WEIGHTS (mem+140)
#define L1_N3_WEIGHTS (mem+144)
#define L1_N4_WEIGHTS (mem+148)
#define L1_N5_WEIGHTS (mem+152)
#define L1_N6_WEIGHTS (mem+156)
#define L1_N7_WEIGHTS (mem+160)
#define L1_N8_WEIGHTS (mem+164)
#define L1_N9_WEIGHTS (mem+168)
#define L1_N10_WEIGHTS (mem+172)
#define L1_N11_WEIGHTS (mem+176)
#define L1_N12_WEIGHTS (mem+180)
#define L1_N13_WEIGHTS (mem+184)
#define L1_N14_WEIGHTS (mem+188)
#define L1_N15_WEIGHTS (mem+192)
#define L1_N16_WEIGHTS (mem+196)
#define L1_N17_WEIGHTS (mem+200)
#define L1_N18_WEIGHTS (mem+204)
#define L1_N19_WEIGHTS (mem+208)
#define L1_N20_WEIGHTS (mem+212)
#define L1_N21_WEIGHTS (mem+216)
#define L1_N22_WEIGHTS (mem+220)
#define L1_N23_WEIGHTS (mem+224)
#define L1_N24_WEIGHTS (mem+228)
#define L1_N25_WEIGHTS (mem+232)
#define L1_N26_WEIGHTS (mem+236)
#define L1_N27_WEIGHTS (mem+240)
#define L1_N28_WEIGHTS (mem+244)
#define L1_N29_WEIGHTS (mem+248)
#define L1_N30_WEIGHTS (mem+252)
#define L1_N31_WEIGHTS (mem+256)
#define L1_N32_WEIGHTS (mem+260)
#define L1_N33_WEIGHTS (mem+264)
#define L1_N34_WEIGHTS (mem+268)
#define L1_N35_WEIGHTS (mem+272)
#define L1_N36_WEIGHTS (mem+276)
#define L1_N37_WEIGHTS (mem+280)
#define L1_N38_WEIGHTS (mem+284)
#define L1_N39_WEIGHTS (mem+288)
#define L1_N40_WEIGHTS (mem+292)
#define L1_N41_WEIGHTS (mem+296)
#define L1_N42_WEIGHTS (mem+300)
#define L1_N43_WEIGHTS (mem+304)
#define L1_N44_WEIGHTS (mem+308)
#define L1_N45_WEIGHTS (mem+312)
#define L1_N46_WEIGHTS (mem+316)
#define L1_N47_WEIGHTS (mem+320)
#define L1_N48_WEIGHTS (mem+324)
#define L1_N49_WEIGHTS (mem+328)
#define L1_N50_WEIGHTS (mem+332)
#define L1_N51_WEIGHTS (mem+336)
#define L1_N52_WEIGHTS (mem+340)
#define L1_N53_WEIGHTS (mem+344)
#define L1_N54_WEIGHTS (mem+348)
#define L1_N55_WEIGHTS (mem+352)
#define L1_N56_WEIGHTS (mem+356)
#define L1_N57_WEIGHTS (mem+360)
#define L1_N58_WEIGHTS (mem+364)
#define L1_N59_WEIGHTS (mem+368)
#define L1_N60_WEIGHTS (mem+372)
#define L1_N61_WEIGHTS (mem+376)
#define L1_N62_WEIGHTS (mem+380)
#define L1_N63_WEIGHTS (mem+384)
#define L2_VAL (mem+388)
#define L2_DEL (mem+452)
#define L2_N0_WEIGHTS (mem+516)
#define L2_N1_WEIGHTS (mem+584)
#define L2_N2_WEIGHTS (mem+652)
#define L2_N3_WEIGHTS (mem+720)
#define L2_N4_WEIGHTS (mem+788)
#define L2_N5_WEIGHTS (mem+856)
#define L2_N6_WEIGHTS (mem+924)
#define L2_N7_WEIGHTS (mem+992)
#define L2_N8_WEIGHTS (mem+1060)
#define L2_N9_WEIGHTS (mem+1128)
#define L2_N10_WEIGHTS (mem+1196)
#define L2_N11_WEIGHTS (mem+1264)
#define L2_N12_WEIGHTS (mem+1332)
#define L2_N13_WEIGHTS (mem+1400)
#define L2_N14_WEIGHTS (mem+1468)
#define L2_N15_WEIGHTS (mem+1536)
#define L2_N16_WEIGHTS (mem+1604)
#define L2_N17_WEIGHTS (mem+1672)
#define L2_N18_WEIGHTS (mem+1740)
#define L2_N19_WEIGHTS (mem+1808)
#define L2_N20_WEIGHTS (mem+1876)
#define L2_N21_WEIGHTS (mem+1944)
#define L2_N22_WEIGHTS (mem+2012)
#define L2_N23_WEIGHTS (mem+2080)
#define L2_N24_WEIGHTS (mem+2148)
#define L2_N25_WEIGHTS (mem+2216)
#define L2_N26_WEIGHTS (mem+2284)
#define L2_N27_WEIGHTS (mem+2352)
#define L2_N28_WEIGHTS (mem+2420)
#define L2_N29_WEIGHTS (mem+2488)
#define L2_N30_WEIGHTS (mem+2556)
#define L2_N31_WEIGHTS (mem+2624)
#define L2_N32_WEIGHTS (mem+2692)
#define L2_N33_WEIGHTS (mem+2760)
#define L2_N34_WEIGHTS (mem+2828)
#define L2_N35_WEIGHTS (mem+2896)
#define L2_N36_WEIGHTS (mem+2964)
#define L2_N37_WEIGHTS (mem+3032)
#define L2_N38_WEIGHTS (mem+3100)
#define L2_N39_WEIGHTS (mem+3168)
#define L2_N40_WEIGHTS (mem+3236)
#define L2_N41_WEIGHTS (mem+3304)
#define L2_N42_WEIGHTS (mem+3372)
#define L2_N43_WEIGHTS (mem+3440)
#define L2_N44_WEIGHTS (mem+3508)
#define L2_N45_WEIGHTS (mem+3576)
#define L2_N46_WEIGHTS (mem+3644)
#define L2_N47_WEIGHTS (mem+3712)
#define L2_N48_WEIGHTS (mem+3780)
#define L2_N49_WEIGHTS (mem+3848)
#define L2_N50_WEIGHTS (mem+3916)
#define L2_N51_WEIGHTS (mem+3984)
#define L2_N52_WEIGHTS (mem+4052)
#define L2_N53_WEIGHTS (mem+4120)
#define L2_N54_WEIGHTS (mem+4188)
#define L2_N55_WEIGHTS (mem+4256)
#define L2_N56_WEIGHTS (mem+4324)
#define L2_N57_WEIGHTS (mem+4392)
#define L2_N58_WEIGHTS (mem+4460)
#define L2_N59_WEIGHTS (mem+4528)
#define L2_N60_WEIGHTS (mem+4596)
#define L2_N61_WEIGHTS (mem+4664)
#define L2_N62_WEIGHTS (mem+4732)
#define L2_N63_WEIGHTS (mem+4800)
#define L3_VAL (mem+4868)
#define L3_DEL (mem+4872)
#define L3_N0_WEIGHTS (mem+4876)
#define OUT_TMP (mem+4944)

float *allocate_ann() {
    return (float *)_mm_malloc(4948 * sizeof(float), 16);
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

__attribute__((noinline)) static void randomize_64(float *mem) {
    const double RMI = 1.0 / RAND_MAX;
    const double b2 = pow((double)64, -0.5) * sqrt(12.0);
    const double b = b2*0.5;
    const double b3 = b2*RMI;
    int i;
    for (i=0; i<=64; i++) {
        do {
            mem[i] = random() * b3 - b;
        } while (mem[i] == 0);
    }
}

__attribute__((noinline)) void randomize_ann(float *mem) {
    int i, j;
    j = 0;
    for (i=0; i<64; i++) {
        randomize_3(L1_N0_WEIGHTS + j);
        j += 4;
    }
    j = 0;
    for (i=0; i<64; i++) {
        randomize_64(L2_N0_WEIGHTS + j);
        j += 68;
    }
    randomize_64(L3_N0_WEIGHTS);
}

__attribute__((noinline)) static void tanh_64(float *in) {
    const float Dscale = 16777216.0 / log(2.0);
    __m128 Fscale = _mm_set1_ps(Dscale);
    __m128 Foffset = _mm_set1_ps(1065353216.0);
    __m128 Fone = _mm_set1_ps(1.0);
    __m128 Fhalf = _mm_set1_ps(0.5);
    __m128 x, u, v, a, b;
    int i;
    for (i=0; i<16; i++) {
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

__attribute__((noinline)) static float dotprod_64(float *weights, float *values) {
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
    inp   = _mm_load_ps(values+32);
    wei   = _mm_load_ps(weights+32);
    prod  = _mm_mul_ps(inp, wei);
    total0 = _mm_add_ps(prod, total0);
    inp   = _mm_load_ps(values+36);
    wei   = _mm_load_ps(weights+36);
    prod  = _mm_mul_ps(inp, wei);
    total1 = _mm_add_ps(prod, total1);
    inp   = _mm_load_ps(values+40);
    wei   = _mm_load_ps(weights+40);
    prod  = _mm_mul_ps(inp, wei);
    total2 = _mm_add_ps(prod, total2);
    inp   = _mm_load_ps(values+44);
    wei   = _mm_load_ps(weights+44);
    prod  = _mm_mul_ps(inp, wei);
    total3 = _mm_add_ps(prod, total3);
    inp   = _mm_load_ps(values+48);
    wei   = _mm_load_ps(weights+48);
    prod  = _mm_mul_ps(inp, wei);
    total0 = _mm_add_ps(prod, total0);
    inp   = _mm_load_ps(values+52);
    wei   = _mm_load_ps(weights+52);
    prod  = _mm_mul_ps(inp, wei);
    total1 = _mm_add_ps(prod, total1);
    inp   = _mm_load_ps(values+56);
    wei   = _mm_load_ps(weights+56);
    prod  = _mm_mul_ps(inp, wei);
    total2 = _mm_add_ps(prod, total2);
    inp   = _mm_load_ps(values+60);
    wei   = _mm_load_ps(weights+60);
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

__attribute__((noinline)) static void sum_scaled_64(float *in, float *out, float scale) {
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
    inp = _mm_load_ps(in+32);
    tgt = _mm_load_ps(out+32);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+32, tgt);
    inp = _mm_load_ps(in+36);
    tgt = _mm_load_ps(out+36);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+36, tgt);
    inp = _mm_load_ps(in+40);
    tgt = _mm_load_ps(out+40);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+40, tgt);
    inp = _mm_load_ps(in+44);
    tgt = _mm_load_ps(out+44);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+44, tgt);
    inp = _mm_load_ps(in+48);
    tgt = _mm_load_ps(out+48);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+48, tgt);
    inp = _mm_load_ps(in+52);
    tgt = _mm_load_ps(out+52);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+52, tgt);
    inp = _mm_load_ps(in+56);
    tgt = _mm_load_ps(out+56);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+56, tgt);
    inp = _mm_load_ps(in+60);
    tgt = _mm_load_ps(out+60);
    inp = _mm_mul_ps(sca, inp);
    tgt = _mm_add_ps(inp, tgt);
    _mm_store_ps(out+60, tgt);
}

static void sum_scaled_3(float *in, float *out, float scale) {
    out[0] += scale * in[0];
    out[1] += scale * in[1];
    out[2] += scale * in[2];
}

__attribute__((noinline)) static void mul_tanh_prime_64(float *in, float *out) {
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
    inp = _mm_load_ps(in+32);
    tgt = _mm_load_ps(out+32);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+32, tgt);
    inp = _mm_load_ps(in+36);
    tgt = _mm_load_ps(out+36);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+36, tgt);
    inp = _mm_load_ps(in+40);
    tgt = _mm_load_ps(out+40);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+40, tgt);
    inp = _mm_load_ps(in+44);
    tgt = _mm_load_ps(out+44);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+44, tgt);
    inp = _mm_load_ps(in+48);
    tgt = _mm_load_ps(out+48);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+48, tgt);
    inp = _mm_load_ps(in+52);
    tgt = _mm_load_ps(out+52);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+52, tgt);
    inp = _mm_load_ps(in+56);
    tgt = _mm_load_ps(out+56);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+56, tgt);
    inp = _mm_load_ps(in+60);
    tgt = _mm_load_ps(out+60);
    inp = _mm_mul_ps(inp, inp);
    inp = _mm_sub_ps(one, inp);
    tgt = _mm_mul_ps(inp, tgt);
    _mm_store_ps(out+60, tgt);
}

static void subtract_1(float *a, float *b, float *c) {
    c[0] = a[0] - b[0];
}

static void memory_copy_3(float *dst, float *src) {
    dst[0] = src[0];
    dst[1] = src[1];
    dst[2] = src[2];
}

static void memory_copy_1(float *dst, float *src) {
    dst[0] = src[0];
}

__attribute__((noinline)) static void memory_clear_64(float *dst) {
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
    _mm_store_ps((float *)dst + 32, zero);
    _mm_store_ps((float *)dst + 36, zero);
    _mm_store_ps((float *)dst + 40, zero);
    _mm_store_ps((float *)dst + 44, zero);
    _mm_store_ps((float *)dst + 48, zero);
    _mm_store_ps((float *)dst + 52, zero);
    _mm_store_ps((float *)dst + 56, zero);
    _mm_store_ps((float *)dst + 60, zero);
}

__attribute__((noinline)) float *forward_L1_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<64; i++) {
        *(L1_VAL+0+i) = dotprod_3(L1_N0_WEIGHTS + k, IN_TMP) + *(L1_N0_WEIGHTS+k+3);
        k += 4;
    }
    tanh_64(L1_VAL);
    return L1_VAL;
}

__attribute__((noinline)) float *forward_L2_ann(float *mem) {
    int i, k;
    k = 0;
    for (i=0; i<64; i++) {
        *(L2_VAL+0+i) = dotprod_64(L2_N0_WEIGHTS + k, L1_VAL) + *(L2_N0_WEIGHTS+k+64);
        k += 68;
    }
    tanh_64(L2_VAL);
    return L2_VAL;
}

__attribute__((noinline)) float *forward_L3_ann(float *mem) {
    int i, k;
    *(L3_VAL+0) = dotprod_64(L3_N0_WEIGHTS, L2_VAL) + *(L3_N0_WEIGHTS+64);
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
    memory_copy_1(OUT_TMP, desired);

    /* Compute output deltas */
    subtract_1(OUT_TMP, L3_VAL, L3_DEL);

    /* Layer deltas 3 */
    memory_clear_64(L2_DEL);
    sum_scaled_64(L3_N0_WEIGHTS, L2_DEL, *(L3_DEL+0));
    mul_tanh_prime_64(L2_VAL, L2_DEL);

    /* Layer deltas 2 */
    memory_clear_64(L1_DEL);
    j = 0;
    for (i=0; i<64; i++) {
        sum_scaled_64(L2_N0_WEIGHTS + j, L1_DEL, *(L2_DEL+0+i));
        j += 68;
    }
    mul_tanh_prime_64(L1_VAL, L1_DEL);

    /* Adjust weights */
    j = 0;
    for (i=0; i<64; i++) {
        odel = *(L1_DEL+0+i) * lr;    *(L1_N0_WEIGHTS+j+3) += odel;    sum_scaled_3(IN_TMP, L1_N0_WEIGHTS+j, odel);
        j += 4;
    }

    /* Adjust weights */
    j = 0;
    for (i=0; i<64; i++) {
        odel = *(L2_DEL+0+i) * lr;    *(L2_N0_WEIGHTS+j+64) += odel;    sum_scaled_64(L1_VAL, L2_N0_WEIGHTS+j, odel);
        j += 68;
    }

    /* Adjust weights */
    odel = *(L3_DEL+0) * lr;    *(L3_N0_WEIGHTS+64) += odel;    sum_scaled_64(L2_VAL, L3_N0_WEIGHTS, odel);
}

int layer_values_ann[4] = {0, 4, 388, 4868};

#else /* HEADER FOLLOWS */

float *allocate_ann();
void free_ann(float *mem);
#define MEM_SIZE_ann ( 4948 * sizeof(float) )
float *forward_L1_ann(float *mem);
float *forward_L2_ann(float *mem);
float *forward_L3_ann(float *mem);
float *forward_ann(float *in, float *mem);
void backward_ann(float *desired_in, float *mem, float lr);
void randomize_ann(float *mem);
extern int layer_values_ann[4];

#endif
