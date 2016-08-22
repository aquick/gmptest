package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class PerfPow_Task extends TaskBase implements Runnable
{
    private static final String TAG = "PerfPow_Task";
    
    public PerfPow_Task(UI ui)
    {
        super(ui, TAG);
    }
    
    private static class CheckData
    {
        public CheckData(String num, int want)
        {
            this.num_as_str = num;
            this.want = want;
        }
        public String num_as_str;
        public int want;
    }

    private static final CheckData[] check_data = new CheckData[]
    {
    new CheckData("0", 1),
    new CheckData("1", 1),
    new CheckData("-1", 1),
    new CheckData("2", 0),
    new CheckData("-2", 0),
    new CheckData("3", 0),
    new CheckData("-3", 0),
    new CheckData("4", 1),
    new CheckData("-4", 0),
    new CheckData("64", 1),
    new CheckData("-64", 1),
    new CheckData("128", 1),
    new CheckData("-128", 1),
    new CheckData("256", 1),
    new CheckData("-256", 0),
    new CheckData("512", 1),
    new CheckData("-512", 1),
    new CheckData("0x4000000", 1),
    new CheckData("-0x4000000", 1),
    new CheckData("0x3cab640", 1),
    new CheckData("-0x3cab640", 0),
    new CheckData("0x3e23840", 1),
    new CheckData("-0x3e23840", 0),
    new CheckData("0x3d3a7ed1", 1),
    new CheckData("-0x3d3a7ed1", 1),
    new CheckData("0x30a7a6000", 1),
    new CheckData("-0x30a7a6000", 1),
    new CheckData("0xf33e5a5a59", 1),
    new CheckData("-0xf33e5a5a59", 0),
    new CheckData("0xed1b1182118135d", 1),
    new CheckData("-0xed1b1182118135d", 1),
    new CheckData("0xe71f6eb7689cc276b2f1", 1),
    new CheckData("-0xe71f6eb7689cc276b2f1", 0),
    new CheckData("0x12644507fe78cf563a4b342c92e7da9fe5e99cb75a01", 1),
    new CheckData("-0x12644507fe78cf563a4b342c92e7da9fe5e99cb75a01", 0),
    new CheckData("0x1ff2e7c581bb0951df644885bd33f50e472b0b73a204e13cbe98fdb424d66561e4000000", 1),
    new CheckData("-0x1ff2e7c581bb0951df644885bd33f50e472b0b73a204e13cbe98fdb424d66561e4000000", 1),
    new CheckData("0x2b9b44db2d91a6f8165c8c7339ef73633228ea29e388592e80354e4380004aad84000000", 1),
    new CheckData("-0x2b9b44db2d91a6f8165c8c7339ef73633228ea29e388592e80354e4380004aad84000000", 1),
    new CheckData("0x28d5a2b8f330910a9d3cda06036ae0546442e5b1a83b26a436efea5b727bf1bcbe7e12b47d81", 1),
    new CheckData("-0x28d5a2b8f330910a9d3cda06036ae0546442e5b1a83b26a436efea5b727bf1bcbe7e12b47d81", 1)
    };

    private void check_tests ()
        throws Exception
    {
        mpz_t x;
        int i;
        int got;
 
        x = new mpz_t();

        for (i = 0; i < check_data.length; i++) {
            GMP.mpz_set_str (x, check_data[i].num_as_str, 0);
            got = GMP.mpz_perfect_power_p (x);
            if (got != check_data[i].want) {
                dump_abort("mpz_perfect_power_p returns " + got + " when " + check_data[i].want +
                               " was expected, fault operand: " + check_data[i].num_as_str);
                /***
                fprintf (stderr, "mpz_perfect_power_p returns %d when %d was expected\n", got, want);
                fprintf (stderr, "fault operand: %s\n", tests[i].num_as_str);
                abort ();
                ***/
            }
        }
    }

    /* Greatest common divisor of a and b.  Assumes a, b non-negative */
    private static long gcd(long a, long b)
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

    private static final int NRP = 15;

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t n;
        mpz_t np;
        mpz_t temp;
        mpz_t[] primes = new mpz_t[NRP];
        int i;
        int j;
        int k;
        int unique;
        int destroy = 0;
        int res;
        int nrprimes;
        long primebits;
        long g;
        long [] exp = new long[NRP];
        long e;

        n = new mpz_t();
        np = new mpz_t();
        temp = new mpz_t();

        for (i = 0; i < NRP; i++) {
            primes[i] = new mpz_t();
        }

        for (i = 0; i < reps; i++) {
            GMP.mpz_urandomb (np, rands, 32);
            nrprimes = (int)(GMP.mpz_get_ui (np) % NRP + 1); /* 1-NRP unique primes */

            GMP.mpz_urandomb (np, rands, 32);
            g = GMP.mpz_get_ui (np) % 32 + 2; /* gcd 2-33 */

            for (j = 0; j < nrprimes;) {
                GMP.mpz_urandomb (np, rands, 32);
                primebits = GMP.mpz_get_ui (np) % 100 + 3; /* 3-102 bit primes */
                GMP.mpz_urandomb (primes[j], rands, primebits);
                GMP.mpz_nextprime (primes[j], primes[j]);
                unique = 1;
                for (k = 0; k < j; k++) {
                    if (GMP.mpz_cmp (primes[j], primes[k]) == 0) {
                        unique = 0;
                        break;
                    }
                }
                if (unique != 0) {
                    GMP.mpz_urandomb (np, rands, 32);
                    e = 371 / (10 * primebits) + GMP.mpz_get_ui (np) % 11 + 1; /* Magic constants */
                    exp[j++] = g * e;
                }
            }

            if (nrprimes > 1) {
                /* Destroy d exponents, d in [1, nrprimes - 1] */
                if (nrprimes == 2) {
                    destroy = 1;
                } else {
                    GMP.mpz_urandomb (np, rands, 32);
                    destroy = (int)(GMP.mpz_get_ui (np) % (nrprimes - 2));
                }

                g = exp[destroy];
                for (k = destroy + 1; k < nrprimes; k++) {
                    g = gcd (g, exp[k]);
                }

                for (j = 0; j < destroy; j++) {
                    GMP.mpz_urandomb (np, rands, 32);
                    e = GMP.mpz_get_ui (np) % 50 + 1;
                    while (gcd (g, e) > 1) {
                        e++;
                    }

                    exp[j] = e;
                }
            }

            /* Compute n */
            GMP.mpz_pow_ui (n, primes[0], exp[0]);
            for (j = 1; j < nrprimes; j++) {
                GMP.mpz_pow_ui (temp, primes[j], exp[j]);
                GMP.mpz_mul (n, n, temp);
            }

            res = GMP.mpz_perfect_power_p (n);

            if (nrprimes == 1) {
                if (res == 0 && exp[0] > 1) {
                    dump_abort2("n is a perfect power, perfpow_p disagrees" +
                        " primes[0]=" + primes[0] + " exp[0]=" + exp[0],
                        n);
                    /***
                    printf("n is a perfect power, perfpow_p disagrees\n");
                    gmp_printf("n = %Zu\nprimes[0] = %Zu\nexp[0] = %lu\n", n, primes[0], exp[0]);
                    abort ();
                    ***/
                } else if (res == 1 && exp[0] == 1) {
                    dump_abort2("n is now a prime number, but perfpow_p still believes n is a perfect power", n);
                    /***
                    gmp_printf("n = %Zu\n", n);
                    printf("n is now a prime number, but perfpow_p still believes n is a perfect power\n");
                    abort ();
                    ***/
                }
            } else {
                if (res == 1 && destroy != 0) {
                    dump_abort2("n was destroyed, but perfpow_p still believes n is a perfect power", n);
                    /***
                    gmp_printf("n = %Zu\nn was destroyed, but perfpow_p still believes n is a perfect power\n", n);
                    abort ();
                    ***/
                } else if (res == 0 && destroy == 0) {
                    dump_abort2("n is a perfect power, perfpow_p disagrees", n);
                }
            }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 10 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }

    }

    public void run()
    {
        int reps = 50; // 500;
        randstate_t rands;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            seed = uinterface.getSeed();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            String s = "seed=" + seed;
            Log.d(TAG, s);
            uinterface.display(s);
            rands = new randstate_t(seed);

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            check_tests();
            check_random(reps, rands);
        }
        catch (GMPException e) {
            failmsg = "GMPException [" + e.getCode() + "] " + e.getMessage();
            ret = -1;
        }
        catch (Exception e) {
            failmsg = e.getMessage();
            ret = -1;
        }
        onPostExecute(Integer.valueOf(ret));
    }

    private void dump_abort(String msg)
        throws Exception
    {
        throw new Exception(msg);
    }

    private void dump_abort2(String msg, mpz_t x)
        throws Exception
    {
        String x_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " n=" + x_str;
        throw new Exception(emsg);
    }
}
