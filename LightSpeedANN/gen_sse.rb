#!/usr/bin/ruby


$use_sse = true
$double = false
#$use_ssex = false
$float = $double ? "double" : "float"
$wsize = $double ? 2 : 1
$reroll = 256

def quantize(len, ibits, fbits)
  x = ""
  x += "__attribute__((noinline)) static "
  x += "void quantize_#{len}_#{ibits}_#{fbits}(#{$float} *a, #{$float} *b) {\n"
  x += "    int i, j;\n"
  x += "    #{$float} v;\n"
  x += "    const #{$float} scale = 1.0 / #{(1<<(fbits-1))};\n"
  x += "    for (i=0; i<#{len}; i++) {\n"
  x += "        v = *a++;\n"
  x += "        j = v * #{1<<(fbits-1)} + #{1<<(ibits+fbits-1)};\n"
  x += "        if (j < 0) j = 0;\n"
  x += "        if (j > #{(1<<(ibits+fbits))-1}) j = #{(1<<(ibits+fbits))-1};\n"
  x += "        v = (j - #{1<<(ibits+fbits-1)}) * scale;\n"
  x += "        *b++ = v;\n"
  x += "    }\n}\n\n"
  x
end
  

def memclr(len)
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void memory_clear_#{len}(#{$float} *dst) {\n"
  #x += "   memset(dst, 0, #{len}*sizeof(#{$float}));\n"
  
  i = 0
  
  if $use_sse
    len *= $wsize
    
    x += "    __m128 zero;\n"
    x += "    zero = _mm_setzero_ps();\n"
    
    if (len>=$reroll/2)
      x += "    int i;  float *p;\n"
      reroll = $reroll / 4
      loops = len / reroll
      x += "    p = (float *)dst;\n"
      x += "    for (i=0; i<#{loops}; i++) {\n"
      t = reroll/4
      (0...t).each do |j|
        x += "        _mm_store_ps(p + #{j*4}, zero);\n"
      end
      x += "        p += #{reroll};\n"
      x += "    }\n"
      i += loops * reroll
      len -= loops * reroll
    end
    while (len >= 4)
      x += "    _mm_store_ps((float *)dst + #{i}, zero);\n"
      i += 4
      len -= 4
    end
    if (len >= 2)
      x += "    _mm_store_sd((double *)((float *)dst + #{i}), (__m128d)zero);\n"
      i += 2
      len -= 2
    end
    if (len >= 1)
      x += "    _mm_store_ss((float *)dst + #{i}, zero);\n"
    end
  else
    while (len > 0) 
      x += "    dst[#{i}] = 0;\n"
      i += 1
      len -= 1
    end
  end      
  
  x + "}\n\n"
end


def memcpy(len)
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void memory_copy_#{len}(#{$float} *dst, #{$float} *src) {\n"
  i = 0
    
  if $use_sse
    len *= $wsize
    len2 = len
    
    if (len >= 8)
      x += "    __m128 tmp;\n"
      x += "    int i; float *s, *d;\n" if len>=512
      x += "    if (((long)src) & 0xf) {\n"
      i = 0
      if (len >= $reroll/2)
        reroll = $reroll / 4
        loops = len / reroll
        x += "        s = (float *)src; d = (float *)dst;\n"
        x += "        for (i=0; i<#{loops}; i++) {\n"
        t = reroll / 4
        (0...t).each do |j|
          x += "            tmp = _mm_loadu_ps(s + #{j*4});\n"
          x += "            _mm_store_ps(d + #{j*4}, tmp);\n"
        end
        x += "            s += #{reroll}; d += #{reroll};\n"
        x += "        }\n"
        len -= loops * reroll
        i += loops * reroll
      end          
      while (len >= 4)
        x += "        tmp = _mm_loadu_ps((float *)src + #{i});\n"
        x += "        _mm_store_ps((float *)dst + #{i}, tmp);\n"
        len -= 4
        i += 4
      end
      x += "    } else {\n"
      i = 0
      if (len2 >= $reroll/2)
        reroll = $reroll / 4
        loops = len2 / reroll
        x += "        s = (float *)src; d = (float *)dst;\n"
        x += "        for (i=0; i<#{loops}; i++) {\n"
        t = reroll / 4
        (0...t).each do |j|
          x += "            tmp = _mm_loadu_ps(s + #{j*4});\n"
          x += "            _mm_store_ps(d + #{j*4}, tmp);\n"
        end
        x += "            s += #{reroll}; d += #{reroll};\n"
        x += "        }\n"
        len2 -= loops * reroll
        i += loops * reroll
      end          
      while (len2 >= 4)
        x += "        tmp = _mm_load_ps((float *)src + #{i});\n"
        x += "        _mm_store_ps((float *)dst + #{i}, tmp);\n"
        len2 -= 4
        i += 4
      end
      x += "    }\n"
    end
    
    if ($double)
      while (len > 0)
        x += "    dst[#{i/2}] = src[#{i/2}];\n"
        i += 2
        len -= 2
      end
    else
      while (len > 0)
        x += "    dst[#{i}] = src[#{i}];\n"
        i += 1
        len -= 1
      end
    end
  else
    while (len > 0) 
      x += "    dst[#{i}] = src[#{i}];\n"
      len -= 1
      i += 1
    end
  end
    
  x + "}\n\n"
end
        


def subtract(len)
  # Emit a function that subtracts b from a and writes to c
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void subtract_#{len}(#{$float} *a, #{$float} *b, #{$float} *c) {\n"
  i = 0
  
  if $use_sse
    if $double
      if len >= 4
        x += "    __m128d ina, inb;\n"
        while (len >= 2)
          x += "    ina = _mm_load_pd(a + #{i});\n"
          x += "    inb = _mm_load_pd(b + #{i});\n"
          x += "    ina = _mm_sub_pd(ina, inb);\n"
          x += "    _mm_store_pd(c + #{i}, ina);\n"
          i += 2
          len -= 2
        end
      end
    else
      if len >= 4
        x += "    __m128 ina, inb;\n"
        if (len >= $reroll / 2)
          reroll = $reroll / 4
          x += "    int i; float *ai, *bi, *ci;\n"
          loops = len / reroll
          x += "    ai = a; bi = b; ci = c;\n"
          x += "    for (i=0; i<#{loops}; i++) {\n"
          t = reroll / 4
          (0...t).each do |j|
            x += "        ina = _mm_load_ps(ai + #{j*4});\n"
            x += "        inb = _mm_load_ps(ib + #{j*4});\n"
            x += "        ina = _mm_sub_ps(ina, inb);\n"
            x += "        _mm_store_ps(ci + #{j*4}, ina);\n"
          end
          x += "        ai += #{reroll}; bi += #{reroll}; ci += #{reroll};\n"
          x += "    }"
          i += loops * reroll
          len -= loops * reroll
        end
        while (len >= 4)
          x += "    ina = _mm_load_ps(a + #{i});\n"
          x += "    inb = _mm_load_ps(b + #{i});\n"
          x += "    ina = _mm_sub_ps(ina, inb);\n"
          x += "    _mm_store_ps(c + #{i}, ina);\n"
          i += 4
          len -= 4
        end
      end
    end
  end
      
  while (len > 0) 
    x += "    c[#{i}] = a[#{i}] - b[#{i}];\n"
    i += 1
    len -= 1
  end
    
  x + "}\n\n"
end

def subtract_tanh_prime(len)
  # Emit a function that computes c[i] = (a[i] - b[i]) * (1 - b[i]*b[i])
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void subtract_tanh_prime_#{len}(#{$float} *a, #{$float} *b, #{$float} *c) {\n"
  if $double
    x += "    __m128d ina, inb, ones;\n"
  else
    x += "    __m128 ina, inb, ones;\n"
  end
  x += "    #{$float} bf;\n"
  x += "    #{$float} *ai, *bi, *ci;\n"
  x += "    int i;\n"
  
  i = 0
  
  if $use_sse && len >= 4
    if $double
      x += "    ones = _mm_set1_pd(1.0);\n"
      while (len >= 2) 
        x += "    ina = _mm_load_pd(a + #{i});\n"
        x += "    inb = _mm_load_pd(b + #{i});\n"
        x += "    ina = _mm_sub_pd(ina, inb);\n"
        x += "    inb = _mm_mul_pd(inb, inb);\n"    
        x += "    inb = _mm_sub_pd(ones, inb);\n"
        x += "    ina = _mm_mul_pd(inb, ina);\n"
        x += "    _mm_store_pd(c + #{i}, ina);\n"
        i += 2
        len -= 2
      end
    else
      x += "    ones = _mm_set1_ps(1.0f);\n"
      if (len >= $reroll / 4)
        reroll = $reroll / 8
        loops = len / reroll
        x += "    ai = a; bi = b; ci = c;\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
        t = reroll / 4
        (0...t).each do |j|
          x += "        ina = _mm_load_ps(ai + #{j*4});\n"
          x += "        inb = _mm_load_ps(bi + #{j*4});\n"
          x += "        ina = _mm_sub_ps(ina, inb);\n"
          x += "        inb = _mm_mul_ps(inb, inb);\n"    
          x += "        inb = _mm_sub_ps(ones, inb);\n"
          x += "        ina = _mm_mul_ps(inb, ina);\n"
          x += "        _mm_store_ps(ci + #{j*4}, ina);\n"
        end
        x += "        ai += #{reroll}; bi += #{reroll}; ci += #{reroll};\n"
        x += "    }\n"
        i += reroll * loops
        len -= reroll * loops
      end
      while (len >= 4)
        x += "    ina = _mm_load_ps(a + #{i});\n"
        x += "    inb = _mm_load_ps(b + #{i});\n"
        x += "    ina = _mm_sub_ps(ina, inb);\n"
        x += "    inb = _mm_mul_ps(inb, inb);\n"    
        x += "    inb = _mm_sub_ps(ones, inb);\n"
        x += "    ina = _mm_mul_ps(inb, ina);\n"
        x += "    _mm_store_ps(c + #{i}, ina);\n"
        i += 4
        len -= 4
      end
    end
  end

  while (len > 0) 
    x += "    bf = b[#{i}];\n"
    x += "    c[#{i}] = (a[#{i}] - bf) * (1 - bf*bf);\n"
    i += 1
    len -= 1
  end
    
  x + "}\n\n"
end


def subtract_logistic_prime(len)
  # Emit a function that computes c[i] = (a[i] - b[i]) * b[i] * (1 - b[i])
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void subtract_logistic_prime_#{len}(#{$float} *a, #{$float} *b, #{$float} *c) {\n"
  if $double
    x += "    __m128d ina, inb, ones;\n"
  else
    x += "    __m128 ina, inb, ones;\n"
  end
  x += "    #{$float} bf;\n"
  x += "    #{$float} *ai, *bi, *ci;\n"
  x += "    int i;\n"
  
  i = 0
  
  if $use_sse && len >= 4
    if $double
      x += "    ones = _mm_set1_pd(1.0);\n"
      while (len >= 2) 
        x += "    ina = _mm_load_pd(a + #{i});\n"
        x += "    inb = _mm_load_pd(b + #{i});\n"
        x += "    ina = _mm_sub_pd(ina, inb);\n"  # a := a-b
        x += "    ina = _mm_mul_pd(ina, inb);\n"  # a := (a-b)*b
        x += "    inb = _mm_sub_pd(ones, inb);\n" # b := 1-b
        x += "    ina = _mm_mul_pd(ina, inb);\n"  # a := (a-b)*b*(1-b)
        x += "    _mm_store_pd(c + #{i}, ina);\n"
        i += 2
        len -= 2
      end
    else
      x += "    ones = _mm_set1_ps(1.0f);\n"
      if (len >= $reroll / 4)
        reroll = $reroll / 8
        loops = len / reroll
        x += "    ai = a; bi = b; ci = c;\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
        t = reroll / 4
        (0...t).each do |j|
          x += "        ina = _mm_load_ps(ai + #{j*4});\n"
          x += "        inb = _mm_load_ps(bi + #{j*4});\n"
          x += "        ina = _mm_sub_ps(ina, inb);\n"  # a := a-b
          x += "        ina = _mm_mul_ps(ina, inb);\n"  # a := (a-b)*b
          x += "        inb = _mm_sub_ps(ones, inb);\n" # b := 1-b
          x += "        ina = _mm_mul_ps(ina, inb);\n"  # a := (a-b)*b*(1-b)
          x += "        _mm_store_ps(ci + #{j*4}, ina);\n"
        end
        x += "        ai += #{reroll}; bi += #{reroll}; ci += #{reroll};\n"
        x += "    }\n"
        i += reroll * loops
        len -= reroll * loops
      end
      while (len >= 4)
        x += "    ina = _mm_load_ps(a + #{i});\n"
        x += "    inb = _mm_load_ps(b + #{i});\n"
        x += "    ina = _mm_sub_ps(ina, inb);\n"  # a := a-b
        x += "    ina = _mm_mul_ps(ina, inb);\n"  # a := (a-b)*b
        x += "    inb = _mm_sub_ps(ones, inb);\n" # b := 1-b
        x += "    ina = _mm_mul_ps(ina, inb);\n"  # a := (a-b)*b*(1-b)
        x += "    _mm_store_ps(c + #{i}, ina);\n"
        i += 4
        len -= 4
      end
    end
  end

  while (len > 0) 
    x += "    bf = b[#{i}];\n"
    x += "    c[#{i}] = (a[#{i}] - bf) * bf * (1 - bf);\n"
    i += 1
    len -= 1
  end
    
  x + "}\n\n"
end


def dotprod(len)
  io = 0
  wo = 0
  hsum = false
  
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static #{$float} dotprod_#{len}(#{$float} *weights, #{$float} *values) {\n"
  
  if $use_sse && len >= 4
    x += "    #{$float} sum0, sum1, sum2, sum3;\n"
    if $double
      x += "    __m128d wei, inp, prod, total0, total1, total2, total3;\n"

      got = 1
      x += "    inp    = _mm_load_pd(values+#{io});\n"
      x += "    wei    = _mm_load_pd(weights+#{wo});\n"
      x += "    total0 = _mm_mul_pd(inp, wei);\n"
      len -= 2
      io += 2
      wo += 2

      while (len >= 2)
        x += "    inp   = _mm_load_pd(values+#{io});\n"
        x += "    wei   = _mm_load_pd(weights+#{wo});\n"
        
        if (0 != (got & (1<<(3&(io/2)))))
          x += "    prod  = _mm_mul_pd(inp, wei);\n"
          x += "    total#{3&(io/2)} = _mm_add_pd(prod, total#{3&(io/2)});\n"
        else
          x += "    total#{3&(io/2)} = _mm_mul_pd(inp, wei);\n"
          got |= 1<<(3&(io/2))
        end
        
        len -= 2
        io += 2
        wo += 2
      end
      
      case got
      when 3
        x += "    total0 = _mm_add_pd(total0, total1);\n"
      when 7
        x += "    total0 = _mm_add_pd(total0, total1);\n"
        x += "    total0 = _mm_add_pd(total0, total2);\n"
      when 15
        x += "    total0 = _mm_add_pd(total0, total1);\n"
        x += "    total2 = _mm_add_pd(total2, total3);\n"
        x += "    total0 = _mm_add_pd(total0, total2);\n"
      end

      x += "    total0 = _mm_hadd_pd(total0, total0);\n"
      x += "    _mm_store_sd(&sum0, total0);\n"
    else
      x += "    __m128 wei, inp, prod, total0, total1, total2, total3;\n"
      x += "    float *v, *w;  int i;\n"

      got = 1
      x += "    inp    = _mm_load_ps(values+#{io});\n"
      x += "    wei    = _mm_load_ps(weights+#{wo});\n"
      x += "    total0 = _mm_mul_ps(inp, wei);\n"
      len -= 4
      io += 4
      wo += 4

      while (len >= 4 && (got!=15 || ((3&(io/4))!=0)))
        y = 3&(io/4)
        x += "    inp   = _mm_load_ps(values+#{io});\n"
        x += "    wei   = _mm_load_ps(weights+#{wo});\n"
        
        if (0 != (got & (1<<y)))
          x += "    prod  = _mm_mul_ps(inp, wei);\n"
          x += "    total#{y} = _mm_add_ps(prod, total#{y});\n"
        else
          x += "    total#{y} = _mm_mul_ps(inp, wei);\n"
          got |= 1<<y
        end
        
        len -= 4
        io += 4
        wo += 4
      end
      
      if (len >= $reroll/2)
        reroll = $reroll / 4
        loops = len/reroll
        x += "    v = values + #{io}; w = weights + #{wo};\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
        t = reroll / 4
        (0...t).each do |j|
          y = 3&(j)
          x += "        inp   = _mm_load_ps(v+#{j*4});\n"
          x += "        wei   = _mm_load_ps(w+#{j*4});\n"
          x += "        prod  = _mm_mul_ps(inp, wei);\n"
          x += "        total#{y} = _mm_add_ps(prod, total#{y});\n"
        end
        x += "        v += #{reroll};  w += #{reroll};\n"
        x += "    }\n"
        len -= loops * reroll
        io += loops * reroll
        wo += loops * reroll
      end
        
      while (len >= 4)
        y = 3&(io/4)
        x += "    inp   = _mm_load_ps(values+#{io});\n"
        x += "    wei   = _mm_load_ps(weights+#{wo});\n"
        
        if (0 != (got & (1<<y)))
          x += "    prod  = _mm_mul_ps(inp, wei);\n"
          x += "    total#{y} = _mm_add_ps(prod, total#{y});\n"
        else
          x += "    total#{y} = _mm_mul_ps(inp, wei);\n"
          got |= 1<<y
        end
        
        len -= 4
        io += 4
        wo += 4
      end
      
      case got
      when 3
        x += "    total0 = _mm_add_ps(total0, total1);\n"
      when 7
        x += "    total0 = _mm_add_ps(total0, total1);\n"
        x += "    total0 = _mm_add_ps(total0, total2);\n"
      when 15
        x += "    total0 = _mm_add_ps(total0, total1);\n"
        x += "    total2 = _mm_add_ps(total2, total3);\n"
        x += "    total0 = _mm_add_ps(total0, total2);\n"
      end
      
      x += "    total0 = _mm_hadd_ps(total0, total0);\n"
      x += "    total0 = _mm_hadd_ps(total0, total0);\n"
      x += "    _mm_store_ss(&sum0, total0);\n"
    end
    
    case len
    when 0
      x += "    return sum0;\n"
    when 1
      x += "    sum1 = values[#{io}] * weights[#{wo}];\n";
      x += "    return sum0 + sum1;\n"
    when 2
      x += "    sum1 = values[#{io}] * weights[#{wo}];\n";
      x += "    sum2 = values[#{io+1}] * weights[#{wo+1}];\n";
      x += "    return (sum0 + sum1) + sum2;\n"
    when 3
      x += "    sum1 = values[#{io}] * weights[#{wo}];\n";
      x += "    sum2 = values[#{io+1}] * weights[#{wo+1}];\n";
      x += "    sum3 = values[#{io+2}] * weights[#{wo+2}];\n";
      x += "    return (sum0 + sum1) + (sum2 + sum3);\n"
    end
  else
    x += "    #{$float} sum0, sum1, sum2, sum3;\n"
    got = 0
    while (len > 0)
      if ((got & (1<<(io&3))) != 0)
        x += "    sum#{io&3} += values[#{io}] * weights[#{wo}];\n";
      else
        x += "    sum#{io&3} = values[#{io}] * weights[#{wo}];\n";
        got |= 1<<(io&3)
      end
      len -= 1
      io += 1
      wo += 1
    end
    case got
    when 1
      x += "    return sum0;\n"
    when 3
      x += "    return sum0 + sum1;\n"
    when 7
      x += "    return (sum0 + sum1) + sum2;\n"
    when 15
      x += "    return (sum0 + sum1) + (sum2 + sum3);\n"
    end
  end
  
  x + "}\n\n";
end


def sum_scaled(len)
  # Emit a function that does a scaled sum
    
  io = 0
  oo = 0
  
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void sum_scaled_#{len}(#{$float} *in, #{$float} *out, #{$float} scale) {\n"
    
  if $use_sse
    if $double
      if (len >= 4)
        x += "    __m128d tgt, inp, sca;\n"
        x += "    sca = _mm_set1_pd(scale);\n"
      
        while (len >= 2)
          x += "    inp = _mm_load_pd(in+#{io});\n"
          x += "    tgt = _mm_load_pd(out+#{oo});\n"
          x += "    inp = _mm_mul_pd(sca, inp);\n"
          x += "    tgt = _mm_add_pd(inp, tgt);\n"
          x += "    _mm_store_pd(out+#{oo}, tgt);\n"
        
          len -= 2
          io += 2
          oo += 2
        end
      end
    else
      if (len >= 4)
        x += "    __m128 tgt, inp, sca;\n"
        x += "    int i;  float *a, *b;\n" if (len >= 256)
        x += "    sca = _mm_set1_ps(scale);\n"
      
        if (len >= $reroll / 2)
          reroll = $reroll / 4
          loops = len / reroll
          x += "    a = in;  b = out;\n"
          x += "    for (i=0; i<#{loops}; i++) {\n"
          t = reroll / 4
          (0...t).each do |j|
            x += "        inp = _mm_load_ps(a+#{j*4});\n"
            x += "        tgt = _mm_load_ps(b+#{j*4});\n"
            x += "        inp = _mm_mul_ps(sca, inp);\n"
            x += "        tgt = _mm_add_ps(inp, tgt);\n"
            x += "        _mm_store_ps(b+#{j*4}, tgt);\n"
          end
          x += "        a += #{reroll};  b += #{reroll};\n"
          x += "    }\n"
          len -= loops * reroll;
          io += loops * reroll;
          oo += loops * reroll;
        end
      
        while (len >= 4)
          x += "    inp = _mm_load_ps(in+#{io});\n"
          x += "    tgt = _mm_load_ps(out+#{oo});\n"
          x += "    inp = _mm_mul_ps(sca, inp);\n"
          x += "    tgt = _mm_add_ps(inp, tgt);\n"
          x += "    _mm_store_ps(out+#{oo}, tgt);\n"
        
          len -= 4
          io += 4
          oo += 4
        end
      end
    end
  end
      
  while (len > 0) 
    x += "    out[#{oo}] += scale * in[#{io}];\n"
    len -= 1
    io += 1
    oo += 1
  end
    
  x + "}\n\n"
end


def mul_tanh_prime(len)
  # multiplies scaled delta (out) by tanh-prime (from in)
  # If quantized, be sure to pass in unquantized node value
  io = 0
  oo = 0
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void mul_tanh_prime_#{len}(#{$float} *in, "
  x += "#{$float} *out) {\n"
  x += "    #{$float} i;\n"
    
  if $use_sse
    if $double
      if len >= 4
        #x += "    __m128d tgt, inp;\n"
        x += "    __m128d tgt, inp, one;\n"
        x += "    one = _mm_set1_pd(1.0);\n"
        
        while (len >= 2)
          x += "    inp = _mm_load_pd(in+#{io});\n"
          x += "    tgt = _mm_load_pd(out+#{oo});\n"
          x += "    inp = _mm_mul_pd(inp, inp);\n"
          x += "    inp = _mm_sub_pd(one, inp);\n"
          x += "    tgt = _mm_mul_pd(inp, tgt);\n"
          x += "    _mm_store_pd(out+#{oo}, tgt);\n"

          io += 2
          oo += 2
          len -= 2
        end
      end
    else
      if len >= 4
        #x += "    __m128 tgt, inp;\n"
        x += "    __m128 tgt, inp, one;\n"
        x += "    int j;  float *a, *b;\n" if (len >= 256)
        x += "    one = _mm_set1_ps(1.0f);\n"
        
        if (len >= $reroll / 2)
          reroll = $reroll / 4
          loops = len / reroll
          x += "    a = in;  b = out;\n"
          x += "    for (j=0; j<#{loops}; j++) {\n"
          t = reroll / 4
          (0...t).each do |j|
            x += "        inp = _mm_load_ps(a+#{j*4});\n"
            x += "        tgt = _mm_load_ps(b+#{j*4});\n"
            x += "        inp = _mm_mul_ps(inp, inp);\n"
            x += "        inp = _mm_sub_ps(one, inp);\n"
            x += "        tgt = _mm_mul_ps(inp, tgt);\n"
            x += "        _mm_store_ps(b+#{j*4}, tgt);\n"
          end
          x += "        a += #{reroll};  b += #{reroll};\n"
          x += "    }\n"
          len -= loops * reroll;
          io += loops * reroll;
          oo += loops * reroll;
        end

        while (len >= 4)
          x += "    inp = _mm_load_ps(in+#{io});\n"
          x += "    tgt = _mm_load_ps(out+#{oo});\n"
          x += "    inp = _mm_mul_ps(inp, inp);\n"
          x += "    inp = _mm_sub_ps(one, inp);\n"
          x += "    tgt = _mm_mul_ps(inp, tgt);\n"
          x += "    _mm_store_ps(out+#{oo}, tgt);\n"

          io += 4
          oo += 4
          len -= 4
        end
      end
    end
  end
    
  while len > 0
    x += "    i = in[#{io}];\n"
    #x += "    out[#{oo}] *= i;\n"
    if $double
      x += "    out[#{oo}] *= 1.0 - i*i;\n"
    else
      x += "    out[#{oo}] *= 1.0f - i*i;\n"
    end
    io += 1
    oo += 1
    len -= 1
  end
    
  x + "}\n\n"
end


def mul_logistic_prime(len)
  # multiplies scaled delta (out) by logistic-prime (from in)
  # If quantized, be sure to pass in unquantized node value
  io = 0
  oo = 0
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void mul_logistic_prime_#{len}(#{$float} *in, "
  x += "#{$float} *out) {\n"
  x += "    #{$float} i;\n"
    
  if $use_sse
    if $double
      if len >= 4
        #x += "    __m128d tgt, inp;\n"
        x += "    __m128d tgt, inp, one;\n"
        x += "    one = _mm_set1_pd(1.0);\n"
        
        while (len >= 2)
          x += "    inp = _mm_load_pd(in+#{io});\n"
          x += "    tgt = _mm_load_pd(out+#{oo});\n"
          x += "    tgt = _mm_mul_pd(inp, tgt);\n"
          x += "    inp = _mm_sub_pd(one, inp);\n"
          x += "    tgt = _mm_mul_pd(inp, tgt);\n"
          x += "    _mm_store_pd(out+#{oo}, tgt);\n"

          io += 2
          oo += 2
          len -= 2
        end
      end
    else
      if len >= 4
        #x += "    __m128 tgt, inp;\n"
        x += "    __m128 tgt, inp, one;\n"
        x += "    int j;  float *a, *b;\n" if (len >= 256)
        x += "    one = _mm_set1_ps(1.0f);\n"
        
        if (len >= $reroll / 2)
          reroll = $reroll / 4
          loops = len / reroll
          x += "    a = in;  b = out;\n"
          x += "    for (j=0; j<#{loops}; j++) {\n"
          t = reroll / 4
          (0...t).each do |j|
            x += "        inp = _mm_load_ps(a+#{j*4});\n"
            x += "        tgt = _mm_load_ps(b+#{j*4});\n"
            x += "        tgt = _mm_mul_ps(inp, tgt);\n"
            x += "        inp = _mm_sub_ps(one, inp);\n"
            x += "        tgt = _mm_mul_ps(inp, tgt);\n"
            x += "        _mm_store_ps(b+#{j*4}, tgt);\n"
          end
          x += "        a += #{reroll};  b += #{reroll};\n"
          x += "    }\n"
          len -= loops * reroll;
          io += loops * reroll;
          oo += loops * reroll;
        end

        while (len >= 4)
          x += "    inp = _mm_load_ps(in+#{io});\n"
          x += "    tgt = _mm_load_ps(out+#{oo});\n"
          x += "    tgt = _mm_mul_ps(inp, tgt);\n"
          x += "    inp = _mm_sub_ps(one, inp);\n"
          x += "    tgt = _mm_mul_ps(inp, tgt);\n"
          x += "    _mm_store_ps(out+#{oo}, tgt);\n"

          io += 4
          oo += 4
          len -= 4
        end
      end
    end
  end
    
  while len > 0
    x += "    i = in[#{io}];\n"
    #x += "    out[#{oo}] *= i;\n"
    if $double
      x += "    out[#{oo}] *= i * (1.0 - i);\n"
    else
      x += "    out[#{oo}] *= i * (1.0f - i);\n"
    end
    io += 1
    oo += 1
    len -= 1
  end
    
  x + "}\n\n"
end


def mul_relu_hard_prime(len)
  # multiplies scaled delta (out) by relu-hard-prime (from in)
  # If quantized, be sure to pass in unquantized node value
  # This also assumes that "in" is the pre-activation node signal
  io = 0
  oo = 0
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void mul_relu_hard_prime_#{len}(#{$float} *in, "
  x += "#{$float} *out) {\n"
  x += "    #{$float} i;\n"
    
  if $use_sse
    if $double
      if len >= 4
        #x += "    __m128d tgt, inp;\n"
        x += "    __m128d tgt, inp, one, zero;\n"
        x += "    one = _mm_set1_pd(1.0);\n"
        x += "    zero = _mm_setzero_pd();\n"
        
        while (len >= 2)
          x += "    inp = _mm_load_pd(in+#{io});\n"
          x += "    tgt = _mm_load_pd(out+#{oo});\n"
          x += "    inp = _mm_cmpgt_pd(inp, zero);\n"
          x += "    inp = _mm_and_pd(inp, one);\n"
          x += "    tgt = _mm_mul_pd(inp, tgt);\n"
          x += "    _mm_store_pd(out+#{oo}, tgt);\n"

          io += 2
          oo += 2
          len -= 2
        end
      end
    else
      if len >= 4
        #x += "    __m128 tgt, inp;\n"
        x += "    __m128 tgt, inp, one, zero;\n"
        x += "    int j;  float *a, *b;\n" if (len >= 256)
        x += "    one = _mm_set1_ps(1.0f);\n"
        x += "    zero = _mm_setzero_ps();\n"

        if (len >= $reroll/2)
          reroll = $reroll / 4
          loops = len / reroll
          x += "    a = in;  b = out;\n"
          x += "    for (j=0; j<#{loops}; j++) {\n"
          t = reroll / 4
          (0...t).each do |j|
            x += "        inp = _mm_load_ps(a+#{j*4});\n"
            x += "        tgt = _mm_load_ps(b+#{j*4});\n"
            x += "        inp = _mm_cmpgt_ps(inp, zero);\n"
            x += "        inp = _mm_and_ps(inp, one);\n"
            x += "        tgt = _mm_mul_ps(inp, tgt);\n"
            x += "        _mm_store_ps(b+#{j*4}, tgt);\n"
          end
          x += "        a += #{reroll};  b += #{reroll};\n"
          x += "    }\n"
          len -= loops * reroll;
          io += loops * reroll;
          oo += loops * reroll;
        end
        
        while (len >= 4)
          x += "    inp = _mm_load_ps(in+#{io});\n"
          x += "    tgt = _mm_load_ps(out+#{oo});\n"
          x += "    inp = _mm_cmpgt_ps(inp, zero);\n"
          x += "    inp = _mm_and_ps(inp, one);\n"
          x += "    tgt = _mm_mul_ps(inp, tgt);\n"
          x += "    _mm_store_ps(out+#{oo}, tgt);\n"

          io += 4
          oo += 4
          len -= 4
        end
      end
    end
  end
    
  while len > 0
    x += "    i = in[#{io}];\n"
    if $double
      x += "    out[#{oo}] *= (i>0.0) ? 1.0 : 0.0;\n"
    else
      x += "    out[#{oo}] *= (i>0.0f) ? 1.0f 0.0f;\n"
    end
    io += 1
    oo += 1
    len -= 1
  end
    
  x + "}\n\n"
end


def mul_relu_soft_prime(len)
  # multiplies scaled delta (out) by relu-soft-prime (from in)
  # If quantized, be sure to pass in unquantized node value
  # This also assumes that "in" is the pre-activation node signal
  io = 0
  oo = 0
    
  x = ""
  if (len > 8)
    x += "__attribute__((noinline)) "
  end
  x += "static void mul_relu_soft_prime_#{len}(#{$float} *in, "
  x += "#{$float} *out) {\n"
  x += "    #{$float} i;\n"
      
  while len > 0
    x += "    i = 1.0 / (1.0 + exp(-in[#{io}]));\n"
    x += "    out[#{oo}] *= i;\n"
    io += 1
    oo += 1
    len -= 1
  end
    
  x + "}\n\n"
end



# NOTE:  For only four, SSE is actually slower

def scalar_tanh_float()
  x =  "static float tanh_approx(float x) {\n"
  x += "    union { unsigned int i; float f; } u, v;\n"
  x += "    float a, b;\n"
  x += "    const float scale = 16777216.0 / log(2.0);\n"
  x += "    const float offset = 1065353216;\n"
  x += "    x *= scale;\n"
  x += "    u.i = lrintf(x + offset);\n"
  x += "    v.i = lrintf(offset - x);\n"
  x += "    a = (u.f-1) / (u.f+1);\n"
  x += "    b = (v.f-1) / (v.f+1);\n"
  x += "    return (a-b)*0.5f;\n"
  x += "}\n\n"
  x
end

def scalar_tanh_double()
  x =  "static double tanh_approx(double x) {\n"
  x += "    union { unsigned long long i; double f; } u, v;\n"
  x += "    double a, b;\n"
  x += "    const double scale = 9007199254740992.0 / log(2.0);\n"
  x += "    const double offset = 4607182418800017408.0;\n"
  x += "    x *= scale;\n"
  x += "    u.i = llrint(x + offset);\n"
  x += "    v.i = llrint(offset - x);\n"
  x += "    a = (u.f-1) / (u.f+1);\n"
  x += "    b = (v.f-1) / (v.f+1);\n"
  x += "    return (a-b)*0.5;\n"
  x += "}\n\n"
  x
end

def scalar_logistic_float()
  x =  "static float logistic_approx(float x) {\n"
  x += "    union { unsigned int i; float f; } u, v;\n"
  x += "    float a, b;\n"
  x += "    const float scale = 8388608.0 / log(2.0);\n"
  x += "    const float offset = 1065353216;\n"
  x += "    x *= scale;\n"
  x += "    u.i = lrintf(x + offset);\n"   # e^x
  x += "    v.i = lrintf(offset - x);\n"   # e^(-x)
  x += "    a = 1.0f / (1.0f + u.f);\n"
  x += "    b = 1.0f / (1.0f + v.f);\n"
  x += "    return (b-a+1.0f)*0.5f;\n"
  x += "}\n\n"
  x
end

def scalar_logistic_double()
  x =  "static double logistic_approx(double x) {\n"
  x += "    union { unsigned long long i; double f; } u, v;\n"
  x += "    double a, b;\n"
  x += "    const double scale = 4503599627370496.0 / log(2.0);\n"
  x += "    const double offset = 4607182418800017408.0;\n"
  x += "    x *= scale;\n"
  x += "    u.i = lrintf(x + offset);\n"   # e^x
  x += "    v.i = lrintf(offset - x);\n"   # e^(-x)
  x += "    a = 1.0 / (1.0 + u.f);\n"
  x += "    b = 1.0 / (1.0 + v.f);\n"
  x += "    return (b-a+1.0)*0.5;\n"
  x += "}\n\n"
  x
end



def emit_tanh(len, net)
  io = 0
  x = "__attribute__((noinline)) static void tanh_#{len}(#{$float} *in) {\n"

  if $double
    net.need_scalar_tanh = true
    x += "    double v;\n"
    if (len > 1)
      x += "    int i;\n"
      x += "    for (i=0; i<#{len}; i++) {\n"
    end
    x += "        v = *in;\n"
    x += "        v = tanh_approx(v);\n"
    x += "        *in = v;\n"
    if (len > 1)
      x += "        in++;\n"
      x += "    }\n"
    end
  else
    if $use_sse && (len>1)
      x += "    const float Dscale = 16777216.0 / log(2.0);\n"
      x += "    __m128 Fscale = _mm_set1_ps(Dscale);\n"
      x += "    __m128 Foffset = _mm_set1_ps(1065353216.0);\n"
      x += "    __m128 Fone = _mm_set1_ps(1.0);\n"
      x += "    __m128 Fhalf = _mm_set1_ps(0.5);\n"
      x += "    __m128 min = _mm_set1_ps(-32.0);\n"
      x += "    __m128 max = _mm_set1_ps(32.0);\n"
      x += "    __m128 x, u, v, a, b, g;\n"
      loops = (len+3)/4
      if (loops > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
      end
      x += "        x = _mm_load_ps(in);\n"
      x += "        x = _mm_min_ps(x, max);\n"
      x += "        x = _mm_max_ps(x, min);\n"
      x += "        x = _mm_mul_ps(x, Fscale);\n"
      x += "        u = _mm_add_ps(x, Foffset);\n"
      x += "        u = (__m128)_mm_cvtps_epi32(u);\n"
      x += "        v = _mm_sub_ps(Foffset, x);\n"
      x += "        v = (__m128)_mm_cvtps_epi32(v);\n"
      x += "        a = _mm_div_ps(_mm_sub_ps(u, Fone), _mm_add_ps(u, Fone));\n"
      x += "        b = _mm_div_ps(_mm_sub_ps(v, Fone), _mm_add_ps(v, Fone));\n"
      x += "        a = _mm_mul_ps(_mm_sub_ps(a, b), Fhalf);\n"
      x += "        _mm_store_ps(in, a);\n"
      if (loops > 1)
        x += "        in += 4;\n"
        x += "    }\n"
      end
    else
      net.need_scalar_tanh = true
      x += "    float v;\n"
      if (len > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{len}; i++) {\n"
      end
      x += "        v = *in;\n"
      x += "        v = tanh_approx(v);\n"
      x += "        *in = v;\n"
      if (len > 1)
        x += "       in++;\n"
        x += "    }\n"
      end
    end
  end
  
  x + "}\n\n"
end


def emit_logistic(len, net)
  io = 0
  x = "__attribute__((noinline)) static void logistic_#{len}(#{$float} *in) {\n"

  if $double
    net.need_scalar_logistic = true
    x += "    double v;\n"
    if (len > 1)
      x += "    int i;\n"
      x += "    for (i=0; i<#{len}; i++) {\n"
    end
    x += "        v = *in;\n"
    x += "        v = logistic_approx(v);\n"
    x += "        *in = v;\n"
    if (len > 1)
      x += "        in++;\n"
      x += "    }\n"
    end
  else
    if $use_sse && (len>1)
      x += "    const float Dscale = 8388608.0 / log(2.0);\n"
      x += "    __m128 Fscale = _mm_set1_ps(Dscale);\n"
      x += "    __m128 Foffset = _mm_set1_ps(1065353216.0);\n"
      x += "    __m128 Fone = _mm_set1_ps(1.0);\n"
      x += "    __m128 Fhalf = _mm_set1_ps(0.5);\n"
      x += "    __m128 x, u, v, a, b;\n"
      loops = (len+3)/4
      if (loops > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
      end
      x += "        x = _mm_load_ps(in);\n"
      x += "        x = _mm_mul_ps(x, Fscale);\n"
      x += "        u = _mm_add_ps(x, Foffset);\n"
      x += "        u = (__m128)_mm_cvtps_epi32(u);\n"    # e^x
      x += "        v = _mm_sub_ps(Foffset, x);\n"
      x += "        v = (__m128)_mm_cvtps_epi32(v);\n"    # e^(-x)
      x += "        a = _mm_rcp_ps(_mm_add_ps(u, Fone));\n"
      x += "        b = _mm_rcp_ps(_mm_add_ps(v, Fone));\n"
      x += "        a = _mm_mul_ps(_mm_add_ps(Fone, _mm_sub_ps(b, a)), Fhalf);\n"
      x += "        _mm_store_ps(in, a);\n"
      if (loops > 1)
        x += "        in += 4;\n"
        x += "    }\n"
      end
    else
      net.need_scalar_logistic = true
      x += "    float v;\n"
      if (len > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{len}; i++) {\n"
      end
      x += "        v = *in;\n"
      x += "        v = logistic_approx(v);\n"
      x += "        *in = v;\n"
      if (len > 1)
        x += "       in++;\n"
        x += "    }\n"
      end
    end
  end
  
  x + "}\n\n"
end



def emit_relu_hard(len, net, separate_out)
  io = 0
  if separate_out
    x = "__attribute__((noinline)) static void relu_hard_#{len}(#{$float} *in, #{$float} *out) {\n"
  else
    x = "__attribute__((noinline)) static void relu_hard_#{len}(#{$float} *in) {\n"
  end

  if $double
    x += "    double v;\n"
    if (len > 1)
      x += "    int i;\n"
      x += "    for (i=0; i<#{len}; i++) {\n"
    end
    x += "        v = *in;\n"
    x += "        v = (v>0) v : 0;\n"
    if (separate_out)
      x += "        *out = v;\n"
    else
      x += "        *in = v;\n"
    end
    if (len > 1)
      x += "        in++;\n"
      x += "        out++;\n" if separate_out
      x += "    }\n"
    end
  else
    if $use_sse && (len>1)
      x += "    __m128 zero = _mm_setzero_ps();\n"
      x += "    __m128 x, y;\n"
      loops = (len+3)/4
      if (loops > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{loops}; i++) {\n"
      end
      x += "        x = _mm_load_ps(in);\n"
      x += "        y = _mm_cmpgt_ps(x,zero);\n"
      x += "        x = _mm_and_ps(x, y);\n"
      if (separate_out)
        x += "        _mm_store_ps(out, x);\n"
      else
        x += "        _mm_store_ps(in, x);\n"
      end
      if (loops > 1)
        x += "        in += 4;\n"
        x += "        out += 4;\n" if separate_out
        x += "    }\n"
      end
    else
      x += "    float v;\n"
      if (len > 1)
        x += "    int i;\n"
        x += "    for (i=0; i<#{len}; i++) {\n"
      end
      x += "        v = *in;\n"
      x += "        v = (v>0) v : 0;\n"
      if (separate_out)
        x += "        *out = v;\n"
      else
        x += "        *in = v;\n"
      end
      if (len > 1)
        x += "        in++;\n"
        x += "        out++;\n" if separate_out
        x += "    }\n"
      end
    end
  end
  
  x + "}\n\n"
end

def emit_relu_soft(len, net, separate_out)
  io = 0
  if separate_out
    x = "__attribute__((noinline)) static void relu_soft_#{len}(#{$float} *in, #{$float} *out) {\n"
  else
    x = "__attribute__((noinline)) static void relu_soft_#{len}(#{$float} *in) {\n"
  end

  x += "    #{$float} v;\n"
  x += "    int i;\n"
  x += "    for (i=0; i<#{len}; i++) {\n"
  x += "        v = *in;\n"
  x += "        v = log(1.0 + exp(v));\n"
  if (separate_out)
    x += "        *out = v;  in++; out++;\n"
  else
    x += "        *in = v;  in++;\n"
  end
  x += "    }\n"
  x + "}\n\n"
end


def allocate_func_h(size, name)
  x  = "#{$float} *allocate_#{name}();\n";
  x += "void free_#{name}(#{$float} *mem);\n"
  x += "#define MEM_SIZE_#{name} ( #{size} * sizeof(#{$float}) )\n"
  x
end

def allocate_func(size, name)
  x = "#{$float} *allocate_#{name}() {\n";
  if $use_sse
    x += "    return (#{$float} *)_mm_malloc(#{size} * sizeof(#{$float}), 16);\n"
  else
    x += "    return (#{$float} *)malloc(#{size} * sizeof(#{$float}));\n"
  end
  x += "}\n\n"
  x += "void free_#{name}(#{$float} *mem) {\n";
  if $use_sse
    x += "    _mm_free(mem);\n"
  else
    x += "    free(mem);\n"
  end
  x += "}\n\n"
  x
end

def randomize_h(size, name)
  "void randomize_#{name}(#{$float} *mem);\n"
end

# def randomize(size, name)
#   x  = "void randomize_#{name}(#{$float} *mem) {\n"
#   x += "    const double RMI = 1.0 / RAND_MAX;\n"
#   x += "    double b2 = pow(#{size}, -0.5L) * sqrt(12.0L);\n"
#   x += "    double b = b2*0.5;\n"
#   x += "    int i;\n"
#   x += "    for (i=0; i<#{size}; i++) {\n"
#   x += "        do {\n"
#   //    x += "            mem[i] = (double)random() / ((double)RAND_MAX);\n"
#   x += "            mem[i] = ((random() * RMI) * b2 - b);\n"
#   x += "        } while (mem[i] == 0);\n"
#   x += "    }\n"
#   x += "}\n\n"
#   x
# end

$all_relu = [:relu_hh, :relu_hs, :relu_ss, :relu_sh]
$to_relu = {"hh" => :relu_hh, "hs" => :relu_hs, "sh" => :relu_sh, "ss" => :relu_ss}
$sig_symbol = {:tanh => "t", :logistic => "s", :linear => "l", :relu_hh => "rhh", :relu_hs => "rhs", :relu_sh => "rsh", :relu_ss => "rss"}


class Layer
  # Inputs
  attr_accessor :n_in, :in_val, :in_del, :in_qval, :in_signal, :in_sig
  # Outputs
  attr_accessor :n_out, :out_val, :out_del, :sig, :val_offset, :out_qval, :out_signal
  # Weights
  attr_accessor :weights
  attr_accessor :mynum
  # Other
  attr_accessor :quantize, :quanti, :quantf
    
  def initialize
    @weights = []
  end

  def is_relu?
    $all_relu.include?(@sig)
  end
end


class LayerSpec
  attr_accessor :size, :quantize, :quanti, :quantf, :sigmoid, :input

  def is_relu?
    $all_relu.include?(@sigmoid)
  end

  def initialize(word, input)
    @input = input
    @quantize = false
    @sigmoid = :tanh
    @quanti = 0
    @quantf = 0

    word = word.downcase.scan(/./)

    size_s = ""
    while word.size > 0 && word[0] =~ /[[:digit:]]/
      size_s << word[0]
      word.shift
    end
    @size = size_s.to_i

    if word.size > 0 && word[0] =~ /[lstr]/
      case word[0]
      when 'r'
        word.shift
        hs = ""
        while word.size > 0 && word[0] =~ /[hs]/
          hs += word[0]
          word.shift
        end
        @sigmoid = $to_relu[hs]
      when 't'
        word.shift
        @sigmoid = :tanh
      when 's'
        word.shift
        @sigmoid = :logistic
      else
        word.shift
        @sigmoid = :linear
      end      
    end
    
    if word.size > 0 && word[0] == 'q'
      word.shift
      @quantize = true
      i_s = ""
      f_s = ""
      while word.size > 0 && word[0] != '.'
        i_s << word[0]
        word.shift
      end
      @quanti = i_s.to_i
      word.shift if word.size > 0
      while word.size > 0
        f_s << word[0]
        word.shift
      end
      @quantf = f_s.to_i
    end
  end

  def comment
    x = "#{@size} nodes"
    if !@input
      case @sigmoid
      when :tanh
        x += ", tanh activation"
      when :logistic
        x += ", logistic activation"
      when :linear
        x += ", linear activation"
      else
        x += ", #{@sigmoid} activation"
      end
    end
    if @quantize
      x += ", quantized to #{@quanti}.#{@quantf}"
    end
    x
  end
  
  def sigmoid_symbol
    $sig_symbol[@sigmoid]
  end

  def to_s
    if @input
      "#{@size}"
    else
      "#{@size}#{sigmoid_symbol}#{@quantize ? "q" : ""}"
    end
  end
end

def mem_offset(str)
  str.split("+")[1].to_i
end

$reverse_address = {}

class Group
  attr_accessor :base, :count, :stride, :offsets
end

class Grouper
  def initialize
    @base = nil
    @current = nil
    @count = 0
    @stride = 0
    @list = nil
    @old_list = []
  end
  
  def <<(x)
    #$stderr.puts "<< #{x}"
    g = Group.new
    g.base = x
    y = []
    x.each do |i|
      p = $reverse_address[i]
      y << (p ? p : i)
    end
    #$stderr.puts ">> #{y}"
    g.offsets = y
    g.count = 1
    g.stride = 0
    @old_list << g
  end
  
  def list_dif(x, y)
    #$stderr.puts "x:#{x}  y:#{y}"
    d = []
    x.each_index do |i|
      d[i] = x[i] - y[i]
    end
    d
  end
  
  def list_eq(x, y)
    x.each_index do |i|
      return false if x[i] != y[i]
    end
    true
  end
  
  def count_conseq
    if @old_list.size < 3
      return @old_list.size, nil
    end
    stride = list_dif(@old_list[1].offsets, @old_list[0].offsets)
    count = 2
    while (count < @old_list.size)
      stride2 = list_dif(@old_list[count].offsets, @old_list[count-1].offsets)
      eq = list_eq(stride2, stride)
      break if !eq
      count += 1
    end
    [count,stride]
  end
  
  def get_list
    return @list if @list
    
    @list = []
    while @old_list.size > 0
      count,stride = count_conseq
      if (count < 3)
        #$stderr.puts "1"
        @list << @old_list.shift
      else
        #$stderr.puts "#{count}"
        h = @old_list[0]
        @old_list.shift(count)
        g = Group.new
        g.base = h.base
        g.stride = stride
        g.offsets = h.offsets
        g.count = count
        @list << g
      end
    end
    
    @list
  end
end


class Network
  # Data
  attr_accessor :layers, :name, :outsig, :in_tmp, :mem_size, :out_tmp

  # Pending needs
  attr_accessor :need_dotprod, :need_sum_scaled
  attr_accessor :need_mul_tanh_prime, :need_mul_logistic_prime
  attr_accessor :need_subtract
  attr_accessor :need_subtract_tanh, :need_subtract_logistic
  attr_accessor :need_tanh, :need_logistic
  attr_accessor :need_scalar_tanh, :need_scalar_logistic
  attr_accessor :need_copy, :need_clear

  # Functions
  attr_accessor :allocator, :funcs, :fwd, :defines, :bkw, :code
    
  def randomize_n(size)
    # Actually randomizes size+1 values, to include the bias
    x = ""
    if (size > 8)
        x += "__attribute__((noinline)) "
    end
    x += "static void randomize_#{size}(#{$float} *mem) {\n"
    x += "    const double RMI = 1.0 / RAND_MAX;\n"
    x += "    const double b2 = pow((double)#{size}, -0.5) * sqrt(12.0);\n"
    x += "    const double b = b2*0.5;\n"
    x += "    const double b3 = b2*RMI;\n"
    x += "    int i;\n"
    x += "    for (i=0; i<=#{size}; i++) {\n"
    x += "        do {\n"
    x += "            mem[i] = random() * b3 - b;\n"
    x += "        } while (mem[i] == 0);\n"
    x += "    }\n"
    x += "}\n\n"
    x
  end
    
  def randomize
    needrandom = []
    @layers.each_index do |ln|
      layer = @layers[ln]
      needrandom << layer.n_in
    end
    needrandom.uniq!

    x = ""
    needrandom.each do |n|
      x += randomize_n(n)
    end
      
    x += "__attribute__((noinline)) "
    x += "void randomize_#{@name}(#{$float} *mem) {\n"
    x += "    int i, j;\n"
    first = nil
    prev = nil
    loopct = 0
    stride = 0
    @layers.each_index do |ln|
      layer = @layers[ln]
      g = Grouper.new
      layer.weights.each do |w|
        g << [w]
      end
      g = g.get_list
      g.each do |h|
        if (h.count == 1)
          x += "    randomize_#{layer.n_in}(#{h.base[0]});\n"
        else
          x += "    j = 0;\n"
          x += "    for (i=0; i<#{h.count}; i++) {\n"
          x += "        randomize_#{layer.n_in}(#{h.base[0]} + j);\n"
          x += "        j += #{h.stride[0]};\n"
          x += "    }\n"
        end
      end
      
      # layer.weights.each do |w|
      #   x += "    randomize_#{layer.n_in}(#{w});\n"
      # end
    end
    x += "}\n\n"
    x
  end
  
  def initialize(layer_list)
    @mem_size = 0
    @need_logistic = []
    @need_tanh = []
    @need_relu_hard = []
    @need_relu_soft = []
    @need_dotprod = []
    @need_sum_scaled = []
    @need_mul_relu_hard_prime = []
    @need_mul_relu_soft_prime = []
    @need_mul_tanh_prime = []
    @need_mul_logistic_prime = []
    @need_subtract = []
    @need_subtract_tanh = []
    @need_subtract_logistic = []
    @need_copy = []
    @need_clear = []
    @need_quantize = []
    @need_scalar_tanh = false
    @need_scalar_logistic = false
        
    @defines = allocate(layer_list)
    @out_tmp = "(mem+#{allocate_block(layer_list[-1].size)})"
    @defines += "#define OUT_TMP #{@out_tmp}\n\n"
    @fwd = forward
    @bkw = backward
        
    @need_logistic.uniq!
    @need_tanh.uniq!
    @need_dotprod.uniq!
    @need_relu_hard.uniq!
    @need_relu_soft.uniq!
    @need_sum_scaled.uniq!
    @need_mul_tanh_prime.uniq!
    @need_mul_logistic_prime.uniq!
    @need_mul_relu_hard_prime.uniq!
    @need_mul_relu_soft_prime.uniq!
    @need_subtract.uniq!
    @need_subtract_tanh.uniq!
    @need_subtract_logistic.uniq!
    @need_copy.uniq!
    @need_clear.uniq!
    @need_quantize.uniq!
        
    @allocator = allocate_func(@mem_size, @name)
    @funcs = @allocator
    @funcs += randomize #(@mem_size, @name)
    @need_quantize.each { |i| @funcs += quantize(i[0], i[1], i[2]); }

    sigfuncs = ""
    @need_tanh.each { |i| sigfuncs += emit_tanh(i, self); }
    @need_logistic.each { |i| sigfuncs += emit_logistic(i, self); }
    @need_relu_hard.each { |i| sigfuncs += emit_relu_hard(i, self, true); }
    @need_relu_soft.each { |i| sigfuncs += emit_relu_soft(i, self, true); }
    if @need_scalar_tanh
      if $double
        @funcs += scalar_tanh_double()
      else
        @funcs += scalar_tanh_float()
      end
    end
    if @need_scalar_logistic
      if $double
        @funcs += scalar_logistic_double()
      else
        @funcs += scalar_logistic_float()
      end
    end
    @funcs += sigfuncs

    @need_dotprod.each { |i| @funcs += dotprod(i); }
    @need_sum_scaled.each { |i| @funcs += sum_scaled(i); }
    @need_mul_tanh_prime.each { |i| @funcs += mul_tanh_prime(i); }
    @need_mul_logistic_prime.each { |i| @funcs += mul_logistic_prime(i); }
    @need_mul_relu_hard_prime.each { |i| @funcs += mul_relu_hard_prime(i); }
    @need_mul_relu_soft_prime.each { |i| @funcs += mul_relu_soft_prime(i); }
    @need_subtract.each { |i| @funcs += subtract(i); }
    @need_subtract_tanh.each { |i| @funcs += subtract_tanh_prime(i); }
    @need_subtract_logistic.each { |i| @funcs += subtract_logistic_prime(i); }
    @need_copy.each { |i| @funcs += memcpy(i); }
    @need_clear.each { |i| @funcs += memclr(i); }
        
    @code = "#ifndef ANN_HEADER\n\n"
    if ($use_sse)
      @code += "#include <pmmintrin.h>\n"
    end
    @code += "#include <math.h>\n#include <stdlib.h>\n#include <stdio.h>\n#include <string.h>\n\n"
    @code += @defines + @funcs + @fwd + @bkw + layer_vals
    @code += "\n#else /* HEADER FOLLOWS */\n\n"
    @code += allocate_func_h(@mem_size, @name)
    @code += forward_h
    @code += backward_h
    @code += randomize_h(@mem_size, @name)
    @code += layer_vals_h
    @code += "\n#endif\n"
  end

  def allocate_block(n)
    start = @mem_size
    @mem_size += n
    x = @mem_size & 3
    if (x != 0)
      @mem_size += 4-x
    end
    start
  end
    
    
  def allocate(layer_list)
    @name = "ann"
        
    # Find homes for all values, weights, and deltas
    @needs_dotprod = []
    @layers = []
    
    # Previous layer
    l = layer_list.clone
    prev_nnodes = l.shift.size
    @in_tmp = "(mem+#{allocate_block(prev_nnodes)})"
    $reverse_address["IN_TMP"] = @in_tmp
    x = "#define IN_TMP #{@in_tmp}\n"
    @in_tmp = "IN_TMP"
    prev_val = "IN_TMP"
    prev_del = nil
    prev_qval = "IN_TMP"
    prev_signal = "IN_TMP"
    prev_sig = :linear
        
    ln = 1
            
    while (l.size > 0)
      layerspec = l.shift
      nnodes = layerspec.size
      keep_signal = layerspec.is_relu?

      # Unquantized values
      values = allocate_block(nnodes)
      $reverse_address["L#{ln}_VAL"] = values
      val_ptr = "(mem+#{values})"
      x += "#define L#{ln}_VAL #{val_ptr}\n"
      val_ptr = "L#{ln}_VAL"

      # Signal values
      if keep_signal
        signal = allocate_block(nnodes)
        $reverse_address["L#{ln}_SIGNAL"] = signal
        signal_ptr = "(mem+#{signal})"
        x += "#define L#{ln}_SIGNAL #{signal_ptr}\n"
        signal_ptr = "L#{ln}_SIGNAL"
      else
        signal = values
        signal_ptr = val_ptr
      end
      
      if (layerspec.quantize)
        # Quantized values
        qval = allocate_block(nnodes);
        $reverse_address["L#{ln}_QVAL"] = qval
        qval_ptr = "(mem+#{qval})"
        x += "#define L#{ln}_QVAL #{qval_ptr}\n"
        qval_ptr = "L#{ln}_QVAL"
      else
        qval = values
        qval_ptr = val_ptr
      end
            
      deltas = allocate_block(nnodes)
      $reverse_address["L#{ln}_DEL"] = deltas
      del_ptr = "(mem+#{deltas})"
      x += "#define L#{ln}_DEL #{del_ptr}\n"
      del_ptr = "L#{ln}_DEL"
            
      layer = Layer.new
      layer.n_in = prev_nnodes
      layer.n_out = nnodes
      layer.in_val = prev_val
      layer.in_qval = prev_qval
      layer.out_val = val_ptr
      layer.out_qval = qval_ptr
      layer.out_signal = signal_ptr
      layer.in_signal = prev_signal
      layer.val_offset = qval   # Assume want to peek at quantized values
      layer.in_del = prev_del
      layer.out_del = del_ptr
      layer.quantize = layerspec.quantize
      layer.quanti = layerspec.quanti
      layer.quantf = layerspec.quantf
      layer.sig = layerspec.sigmoid
      layer.mynum = ln
      layer.in_sig = prev_sig
            
      for i in 0...nnodes do
        weights = allocate_block(prev_nnodes + 1)
        $reverse_address["L#{ln}_N#{i}_WEIGHTS"] = weights
        n = "(mem+#{weights})"
        x += "#define L#{ln}_N#{i}_WEIGHTS #{n}\n"
        layer.weights << "L#{ln}_N#{i}_WEIGHTS"
      end
            
      @layers << layer
            
      prev_nnodes = nnodes
      prev_val = val_ptr
      prev_qval = qval_ptr
      prev_del = del_ptr
      prev_signal = signal_ptr
      prev_sig = layerspec.sigmoid
            
      ln += 1
    end    
    x
  end

  def layer_vals_h
    x = "extern int layer_values_#{@name}[#{layers.size+1}];\n"
  end

  def layer_vals
    x = "int layer_values_#{@name}[#{layers.size+1}] = {0"
    layers.each do |layer|
      x += ", #{layer.val_offset}"
    end
    x += "};\n"
    x
  end

  def forward_h
    x = ""
    layers.each do |layer|
      x += "#{$float} *forward_L#{layer.mynum}_#{@name}(#{$float} *mem);\n"
    end
    x + "#{$float} *forward_#{@name}(#{$float} *in, #{$float} *mem);\n"
  end

  def forward
    @need_copy << layers.first.n_in
    x = ""

    @layers.each do |layer|
      x += "__attribute__((noinline)) "
      x += "#{$float} *forward_L#{layer.mynum}_#{@name}(#{$float} *mem) {\n"
      x += "    int i, k;\n"

      nnodes = layer.n_out
      prev_nnodes = layer.n_in
      signal = layer.out_signal
      ptr = layer.out_val
      qptr = layer.out_qval
      keep_signal = layer.is_relu?
      dprod_ptr = keep_signal ? signal : ptr
            
      @need_dotprod << prev_nnodes
      
      g = Grouper.new
      for i in 0...nnodes do
        weights = layer.weights[i]
        l = [i, weights]
        g << l
      end
      g = g.get_list
      g.each do |h|
        if h.count == 1
          i = h.base[0]
          weights = h.base[1]
          x += "    *(#{dprod_ptr}+#{i}) = "
          x += "dotprod_#{prev_nnodes}(#{weights}, #{layer.in_qval}) "
          x += "+ *(#{weights}+#{layer.n_in});\n";
        else
          i = h.base[0]
          weights = h.base[1]
          x += "    k = 0;\n"
          x += "    for (i=0; i<#{h.count}; i++) {\n"
          x += "        *(#{dprod_ptr}+#{i}+i) = "
          x += "dotprod_#{prev_nnodes}(#{weights} + k, #{layer.in_qval}) "
          x += "+ *(#{weights}+k+#{layer.n_in});\n";
          x += "        k += #{h.stride[1]};\n"
          x += "    }\n"
        end
      end
      
      # for i in 0...nnodes do
      #   weights = layer.weights[i]
      #   x += "    *(#{dprod_ptr}+#{i}) = "
      #   x += "dotprod_#{prev_nnodes}(#{weights}, #{layer.in_qval}) "
      #   x += "+ *(#{weights}+#{layer.n_in});\n";
      # end
            
      case layer.sig
      when :tanh
        @need_tanh << nnodes
        x += "    tanh_#{nnodes}(#{dprod_ptr});\n"
      when :logistic
        @need_logistic << nnodes
        x += "    logistic_#{nnodes}(#{dprod_ptr});\n"
      when :relu_hh, :relu_hs
        @need_relu_hard << nnodes
        x += "    relu_hard_#{nnodes}(#{signal}, #{ptr});\n"
      when :relu_sh, :relu_ss
        @need_relu_soft << nnodes
        x += "    relu_soft_#{nnodes}(#{signal}, #{ptr});\n"
      end

      if (layer.quantize)
        @need_quantize << [nnodes, layer.quanti, layer.quantf]
        x += "    quantize_#{nnodes}_#{layer.quanti}_#{layer.quantf}(#{ptr}, #{qptr});\n"
      end

      x += "    return #{layer.out_qval};\n"
      
      x += "}\n\n"
    end
        
    x += "__attribute__((noinline)) "
    x += "#{$float} *forward_#{@name}(#{$float} *in, #{$float} *mem) {\n"
    x += "    memory_copy_#{layers.first.n_in}(#{@in_tmp}, in);\n"
    #x += "    memcpy(#{@in_tmp}, in, sizeof(float) * #{layers.first.n_in});\n"

    layers.each_index do |i|
      layer = layers[i]
      ret = (i == layers.size-1) ? "return " : "";
      x += "    #{ret}forward_L#{layer.mynum}_#{@name}(mem);\n"
    end
    #x += "    return #{layers.last.out_val};\n"

    x += "}\n\n"
    
    x
  end

  def backward_h
    "void backward_#{@name}(#{$float} *desired_in, #{$float} *mem, #{$float} lr);\n"
  end
    
  def backward
    x = "__attribute__((noinline)) "
    x += "void backward_#{@name}(#{$float} *desired, #{$float} *mem, #{$float} lr) {\n"
    x += "    #{$float} odel;\n"
    x += "    int i, j;\n"
    @need_copy << @layers.last.n_out
    x += "    memory_copy_#{layers.last.n_out}(OUT_TMP, desired);\n\n"
        
    # Compute output deltas from output values and desired
    out_del = layers.last.out_del
    out_val = layers.last.out_val
    n_out = layers.last.n_out
    x += "    /* Compute output deltas */\n"
    case @layers.last.sig
    when :tanh
      @need_subtract_tanh << n_out
      x += "    subtract_tanh_prime_#{n_out}(OUT_TMP, #{out_val}, #{out_del});\n"
    when :logistic
      @need_subtract_logistic << n_out
      x += "    subtract_logistic_prime_#{n_out}(OUT_TMP, #{out_val}, #{out_del});\n"
    else
      @need_subtract << n_out
      x += "    subtract_#{n_out}(OUT_TMP, #{out_val}, #{out_del});\n"
    end
        
    # Loop backward over layers, computing deltas
    ls = @layers.dup
    ls.shift
    while (ls.size > 0)
      l = ls.pop
      x += "\n    /* Layer deltas #{l.mynum} */\n"
      keep_signal = l.is_relu?
      
      @need_sum_scaled << l.n_in
      @need_clear << l.n_in
      #x += "    memset(#{l.in_del}, 0, sizeof(float) * #{l.n_in});\n"
      x += "    memory_clear_#{l.n_in}(#{l.in_del});\n"
      
      g = Grouper.new
      for i in 0...l.n_out
        m = [l.weights[i], i]
        g << m
      end
      g = g.get_list
      g.each do |m|
        i = m.base[1]
        w = m.base[0]
        if (m.count == 1)
          x += "    sum_scaled_#{l.n_in}(#{w}, #{l.in_del}, "
          x += "*(#{l.out_del}+#{i}));\n"
        else
          x += "    j = 0;\n"
          x += "    for (i=0; i<#{m.count}; i++) {\n"
          x += "        sum_scaled_#{l.n_in}(#{w} + j, #{l.in_del}, "
          x += "*(#{l.out_del}+#{i}+i));\n"
          x += "        j += #{m.stride[0]};\n"
          x += "    }\n"
        end
      end
      
      # for i in 0...l.n_out
      #   x += "    sum_scaled_#{l.n_in}(#{l.weights[i]}, #{l.in_del}, "
      #   x += "*(#{l.out_del}+#{i}));\n"
      # end
    
      case l.in_sig
      when :tanh
        @need_mul_tanh_prime << l.n_in
        x += "    mul_tanh_prime_#{l.n_in}(#{l.in_val}, #{l.in_del});\n"
      when :logistic
        @need_mul_logistic_prime << l.n_in
        x += "    mul_logistic_prime_#{l.n_in}(#{l.in_val}, #{l.in_del});\n"
      when :relu_hh, :relu_sh
        @need_mul_relu_hard_prime << l.n_in
        x += "    mul_relu_hard_prime_#{l.n_in}(#{l.in_signal}, #{l.in_del});\n"
      when :relu_hs, :relu_ss
        @need_mul_relu_soft_prime << l.n_in
        x += "    mul_relu_soft_prime_#{l.n_in}(#{l.in_signal}, #{l.in_del});\n"
      end
    end
        
    # Loop over layers, adjusting weights
    ls = @layers.dup
    inputs = @in_tmp
    ls.each do |l|
      x += "\n    /* Adjust weights */\n"
      @need_sum_scaled << l.n_in
      
      g = Grouper.new
      for i in 0...l.n_out
        m = [l.weights[i], i]
        g << m
      end
      g = g.get_list
      g.each do |m|
        w = m.base[0]
        i = m.base[1]
        if (m.count == 1)
          x += "    odel = *(#{l.out_del}+#{i}) * lr;"
          x += "    *(#{w}+#{l.n_in}) += odel;"
          x += "    sum_scaled_#{l.n_in}(#{inputs}, "
          x += "#{w}, odel);\n"
        else          
          x += "    j = 0;\n"
          x += "    for (i=0; i<#{m.count}; i++) {\n"
          x += "        odel = *(#{l.out_del}+#{i}+i) * lr;"
          x += "    *(#{w}+j+#{l.n_in}) += odel;"
          x += "    sum_scaled_#{l.n_in}(#{inputs}, "
          x += "#{w}+j, odel);\n"
          x += "        j += #{m.stride[0]};\n"
          x += "    }\n"
        end
      end
      
      # for i in 0...l.n_out
      #   x += "    odel = *(#{l.out_del}+#{i}) * lr;"
      #   x += "    *(#{l.weights[i]}+#{l.n_in}) += odel;"
      #   x += "    sum_scaled_#{l.n_in}(#{inputs}, "
      #   x += "#{l.weights[i]}, odel);\n"
      # end
      inputs = l.out_val
    end
        
    x + "}\n\n"
  end
end

layers = []

is_input = true
ARGV.each do |arg|
  layers << LayerSpec.new(arg, is_input)
  is_input = false
end

layers.each_index do |i|
  puts "// Layer #{i}: #{layers[i].comment}"
end
puts

net = Network.new(layers)
print net.code

=begin
layers = ARGV.clone

do_quantize = []
if (layers.last.downcase[0] == 'q')
  q = layers.last.scan(/./)
  q.shift
  q.each_index do |i|
    do_quantize[i] = q[i] == '1'
  end
  layers.pop
end

sigout = false
if (layers.last.downcase == "s")
  sigout = true
  layers.pop
end

layers2 = layers.map {|x| x.to_i}
net = Network.new(layers2, sigout, do_quantize)
print net.code
=end

# Load configuration
# Allocate block for values
# Allocate block for weights
# Allocate block for deltas
# Determine all dotprod functions that will be needed
# Determine all sum_scaled functions needed
