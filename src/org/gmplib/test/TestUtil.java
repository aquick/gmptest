package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class TestUtil
{

    public static int[] primes = null;
    public static int   numprimes = 0;

    public static int initprimes(int b)
    {
        boolean[] pr = new boolean[b + 1];
        int i;
        int j;

        primes = new int[b + 1];
        /* compute primes up to b */
        for (i = 2; i <= b; i++) pr[i] = true;
        j = 2;
        do {
            for (i = j*j; i <= b; i += j) pr[i] = false;
            while (!pr[++j]);
        } while (j*j <= b);
        j = 0;
        for (i = 2; i <= b; i++) {
            if (pr[i]) {
                primes[++j] = i;
            }
        }
        numprimes = j;
        return j;
    }

    /* Compute Jacobi(a, m).  Assumes a and m are unsigned int's */
    public static int jacobi(long a, long m)
        throws Exception
    {
        int result = 1;
        boolean factor_found;
        long q = 0;
        long r;
        int i;

        //Log.d("TestUtil.jacobi", "a=" + a + " m=" + m);
        if (numprimes < 2) {
            throw new Exception("jacobi: primes not initialized");
        }
        if (a < 0 || a > GMP.ULONG_MAX) {
            throw new Exception("jacobi: argument out of range: a");
        }
        if (m < 3 || m > GMP.ULONG_MAX) {
            throw new Exception("jacobi: argument out of range: m");
        }
        for (;;) {
            factor_found = false;
            for (i = 1; i <= numprimes; i++) {
                q = m/primes[i];
                r = m - q*primes[i];
                if (r == 0) {
                    factor_found = true;
                    break;
                }
            }
            if (factor_found) {
                result = result*legendre_ui(a, (long)primes[i]);
                m = q;
                if (m == 1) break;
            } else { // m prime
                result = result*legendre_ui(a, m);
                break;
            }
        }
        return result;
    }

    /* Legendre symbol via powm. p must be an odd prime. */
    public static int legendre_ui (long al, long pl)
        throws Exception
    {
        mpz_t a;
        mpz_t p;

        //Log.d("TestUtil.legendre_ui", "a=" + al + " p=" + pl);
        if (al < 0 || al > GMP.ULONG_MAX) {
            throw new Exception("legendre_ui: argument out of range: a");
        }
        if (pl < 0 || pl > GMP.ULONG_MAX) {
            throw new Exception("legendre_ui: argument out of range: p");
        }
        if ((pl & 1) == 0) {
            throw new Exception("legendre_ui: invalid argument: p");
        }
        a = new mpz_t();
        p = new mpz_t();
        GMP.mpz_set_ui(a, al);
        GMP.mpz_set_ui(p, pl);
        return legendre(a, p);
    }

    /* Legendre symbol via powm. p must be an odd prime. */
    public static int legendre (mpz_t a, mpz_t p)
        throws Exception
    {
        int res;

        mpz_t r;
        mpz_t e;

        if (GMP.mpz_sgn(p) <= 0) {
            throw new Exception("legendre: invalid argument: p");
        }
        if (GMP.mpz_odd_p(p) == 0) {
            throw new Exception("legendre: invalid argument: p");
        }
        r = new mpz_t();
        e = new mpz_t();

        GMP.mpz_fdiv_r (r, a, p);

        GMP.mpz_set (e, p);
        GMP.mpz_sub_ui (e, e, 1);
        GMP.mpz_fdiv_q_2exp (e, e, 1);
        GMP.mpz_powm (r, r, e, p);

        /* Normalize to a more or less symmetric range around zero */
        if (GMP.mpz_cmp (r, e) > 0) {
            GMP.mpz_sub (r, r, p);
        }
        if (GMP.mpz_cmpabs_ui (r, 1) > 0) {
            throw new Exception("legendre: bad state: r > 1");
        }
        res = GMP.mpz_sgn (r);
        return res;
    }

    public static int jacobi (mpz_t a, int nprime, mpz_t[] prime, int[] exp)
        throws Exception
    {
        int i;
        int res = 1;

        for (i = 0; i < nprime; i++) {
            if (exp[i] != 0) {
                int legendre = legendre (a, prime[i]);
                if (legendre == 0) {
                    return 0;
                }
                if ((exp[i] & 1) != 0) {
                    res *= legendre;
                }
            }
        }
        return res;
    }

    /* Greatest common divisor of a and b.  Assumes a, b non-negative */
    public static long gcd(long a, long b)
    {
        long x;
        long y;
        long r;

        if (a == 0) return b;
        if (b == 0) return a;
        if (a <= b) {
            x = a;
            y = b;
        } else {
            x = b;
            y = a;
        }
        while (x > 0) {
            r = y - x*(y/x);
            y = x;
            x = r;
        }
        return y;
    }

    /* Exponentially distributed between 0 and 2^nbits-1, meaning the number of
    bits in the result is uniformly distributed between 0 and nbits-1.

    FIXME: This is not a proper exponential distribution, since the
    probability function will have a stepped shape due to using a uniform
    distribution after choosing how many bits.  */

    public static void mpz_erandomb (mpz_t rop, randstate_t rstate, long nbits)
        throws GMPException
    {
        GMP.mpz_urandomb (rop, rstate, GMP.gmp_urandomm_ui (rstate, nbits));
    }

    public static void mpz_erandomb_nonzero (mpz_t rop, randstate_t rstate, long nbits)
        throws GMPException
    {
        mpz_erandomb (rop, rstate, nbits);
        if (GMP.mpz_sgn (rop) == 0) GMP.mpz_set_ui (rop, 1L);
    }

    public static void mpz_errandomb (mpz_t rop, randstate_t rstate, long nbits)
        throws GMPException
    {
        GMP.mpz_rrandomb (rop, rstate, GMP.gmp_urandomm_ui (rstate, nbits));
    }

    public static void mpz_errandomb_nonzero (mpz_t rop, randstate_t rstate, long nbits)
        throws GMPException
    {
        mpz_errandomb (rop, rstate, nbits);
        if (GMP.mpz_sgn (rop) == 0) GMP.mpz_set_ui (rop, 1L);
    }

    public static void mpz_negrandom (mpz_t rop, randstate_t rstate)
        throws GMPException
    {
        mpz_t n = new mpz_t();
        GMP.mpz_urandomb (n, rstate, 1L);
        if (GMP.mpz_cmp_si(n, 0) != 0) GMP.mpz_neg (rop, rop);
    }
    
    public static void mpz_rrandomb_signed (mpz_t rop, randstate_t rstate, long size)
        throws GMPException
    {
	if (size >= 0) {
	    GMP.mpz_rrandomb(rop, rstate, size);
	} else {
	    GMP.mpz_rrandomb(rop, rstate, -size);	    
	    GMP.mpz_neg (rop, rop);
	}
    }
	    
    public static long urandom(randstate_t rstate)
        throws GMPException
    {
        mpz_t r = new mpz_t();
        GMP.mpz_urandomb(r,  rstate, GMP.GMP_LIMB_BITS());
        return GMP.mpz_get_ui(r);
    }
    
    public static void mpz_set_n (mpz_t z, long[] p, int size)
        throws GMPException
    {
         if (size < 0) return;
         while (size > 0) {
             if (p[size-1] != 0) break;
             size--;
        }
        GMP.mpz_internal_REALLOC (z, (int)size);
        for (int i = 0; i < (int)size; i++) {
            GMP.mpz_internal_set_ulimb(z, i, p[i]);
        }
        GMP.mpz_internal_SETSIZ(z, (int)size);
    }

    public static int log2c(int n)
    {
        int v = 0;
        if (n >=    0x1) v++; else return v;
        if (n >=    0x2) v++; else return v;
        if (n >=    0x4) v++; else return v;
        if (n >=    0x8) v++; else return v;
        if (n >=   0x10) v++; else return v;
        if (n >=   0x20) v++; else return v;
        if (n >=   0x40) v++; else return v;
        if (n >=   0x80) v++; else return v;
        if (n >=  0x100) v++; else return v;
        if (n >=  0x200) v++; else return v;
        if (n >=  0x400) v++; else return v;
        if (n >=  0x800) v++; else return v;
        if (n >= 0x1000) v++; else return v;
        if (n >= 0x2000) v++; else return v;
        if (n >= 0x4000) v++; else return v;
        if (n >= 0x8000) v++;
        return v;
    }
    
    public static int pow2_p(long n)
    {
        if (n <= 0) return 0;
        if ((n & (n - 1)) == 0) {
            return 1;
        }
        return 0;
    }

    /* Whether the absolute value of z is a power of 2. */
    public static int mpz_pow2abs_p (mpz_t z)
        throws GMPException
    {
        int  size;
        int  i;

        size = GMP.mpz_internal_SIZ (z);
        if (size == 0) return 0;  /* zero is not a power of 2 */
        if (size < 0) size = -size;

        for (i = 0; i < size-1; i++) {
            if (GMP.mpz_internal_get_ulimb(z, i) != 0) {
                return 0;  /* non-zero low limb means not a power of 2 */
            }
        }

        return pow2_p (GMP.mpz_internal_get_ulimb(z, i));  /* high limb power of 2 */
    }

    public static final int FIB_TABLE_LIMIT         = 47;
    public static final int FIB_TABLE_LUCNUM_LIMIT  = 46;
    
    private static final long _fib_table[] = new long[]
    {
            0x1L,  /* -1 */
            0x0L,  /* 0 */
            0x1L,  /* 1 */
            0x1L,  /* 2 */
            0x2L,  /* 3 */
            0x3L,  /* 4 */
            0x5L,  /* 5 */
            0x8L,  /* 6 */
            0xdL,  /* 7 */
            0x15L,  /* 8 */
            0x22L,  /* 9 */
            0x37L,  /* 10 */
            0x59L,  /* 11 */
            0x90L,  /* 12 */
            0xe9L,  /* 13 */
            0x179L,  /* 14 */
            0x262L,  /* 15 */
            0x3dbL,  /* 16 */
            0x63dL,  /* 17 */
            0xa18L,  /* 18 */
            0x1055L,  /* 19 */
            0x1a6dL,  /* 20 */
            0x2ac2L,  /* 21 */
            0x452fL,  /* 22 */
            0x6ff1L,  /* 23 */
            0xb520L,  /* 24 */
            0x12511L,  /* 25 */
            0x1da31L,  /* 26 */
            0x2ff42L,  /* 27 */
            0x4d973L,  /* 28 */
            0x7d8b5L,  /* 29 */
            0xcb228L,  /* 30 */
            0x148addL,  /* 31 */
            0x213d05L,  /* 32 */
            0x35c7e2L,  /* 33 */
            0x5704e7L,  /* 34 */
            0x8cccc9L,  /* 35 */
            0xe3d1b0L,  /* 36 */
            0x1709e79L,  /* 37 */
            0x2547029L,  /* 38 */
            0x3c50ea2L,  /* 39 */
            0x6197ecbL,  /* 40 */
            0x9de8d6dL,  /* 41 */
            0xff80c38L,  /* 42 */
            0x19d699a5L,  /* 43 */
            0x29cea5ddL,  /* 44 */
            0x43a53f82L,  /* 45 */
            0x6d73e55fL,  /* 46 */
            0xb11924e1L  /* 47 */
    };
    
    public static final long fib_table(int i)
    {
        return _fib_table[i+1];
    }

    public static int SGN(int x)
    {
	return ((x) < 0 ? -1 : (x) > 0 ? 1 : 0);
    }
}
