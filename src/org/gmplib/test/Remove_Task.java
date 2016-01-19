package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Remove_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Remove_Task";
    
    public Remove_Task(UI ui)
    {
        super(ui, TAG);
    }

    private long mpz_refremove (mpz_t dest, mpz_t src, mpz_t f)
        throws GMPException
    {
        long pwr = 0;

        GMP.mpz_set (dest, src);
        if (GMP.mpz_cmpabs_ui (f, 1) > 0) {
            mpz_t rem;
            mpz_t x;

            x = new mpz_t();
            rem = new mpz_t();

            for (;; pwr++) {
                GMP.mpz_tdiv_qr (x, rem, dest, f);
                if (GMP.mpz_cmp_ui (rem, 0) != 0) break;
                GMP.mpz_swap (dest, x);
            }

        }

        return pwr;
    }

    public void run()
    {
        long exp;
        mpz_t t;
        mpz_t dest;
        mpz_t refdest;
        mpz_t dividend;
        mpz_t divisor;
        long dividend_size;
        long divisor_size;
        int i;
        int reps = 100; // 1000;
        long pwr;
        long refpwr;
        randstate_t rands;
        mpz_t bs;
        long size_range;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            t = new mpz_t();
            dest = new mpz_t();
            refdest = new mpz_t();
            dividend = new mpz_t();
            divisor = new mpz_t();
            bs = new mpz_t();
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

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 18 + 1; /* 1..524288 bit operands */

                do {
                    GMP.mpz_urandomb (bs, rands, size_range);
                    divisor_size = GMP.mpz_get_ui (bs);
                    GMP.mpz_rrandomb (divisor, rands, divisor_size);
                } while (GMP.mpz_sgn (divisor) == 0);

                GMP.mpz_urandomb (bs, rands, size_range);
                dividend_size = GMP.mpz_get_ui (bs) + divisor_size;
                GMP.mpz_rrandomb (dividend, rands, dividend_size);

                GMP.mpz_urandomb (bs, rands, 32);
                exp = GMP.mpz_get_ui (bs) % (5 + 10000 / GMP.mpz_sizeinbase (divisor, 2));
                if ((GMP.mpz_get_ui (bs) & 2) != 0) {
                    GMP.mpz_neg (divisor, divisor);
                }
                GMP.mpz_pow_ui (t, divisor, exp);
                GMP.mpz_mul (dividend, dividend, t);

                refpwr = mpz_refremove (refdest, dividend, divisor);
                pwr = GMP.mpz_remove (dest, dividend, divisor);

                if (refpwr != pwr || GMP.mpz_cmp (refdest, dest) != 0) {
                    dump_abort("ERROR after " + i + " tests",
                        refpwr, pwr, dividend, divisor, refdest, dest);
                    /***
                    fprintf (stderr, "ERROR after %d tests\n", i);
                    fprintf (stderr, "refpower = %lu\n", refpwr);
                    fprintf (stderr, "   power = %lu\n", pwr);
                    fprintf (stderr, "    op1 = "); debug_mp (dividend);
                    fprintf (stderr, "    op2 = "); debug_mp (divisor);
                    fprintf (stderr, "refdest = "); debug_mp (refdest);
                    fprintf (stderr, "   dest = "); debug_mp (dest);
                    abort ();
                    ***/
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (i % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
                }
            }
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

    private void dump_abort(String msg, long refpwr, long pwr,
                            mpz_t dividend, mpz_t divisor, mpz_t refdest, mpz_t dest)
        throws Exception
    {
        String dividend_str = "";
        String divisor_str = "";
        String dest_str = "";
        String refdest_str = "";
        String emsg;
        try {
            dividend_str = GMP.mpz_get_str(dividend, 10);
        }
        catch (GMPException e) {
            dividend_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            divisor_str = GMP.mpz_get_str(divisor, 10);
        }
        catch (GMPException e) {
            divisor_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            refdest_str = GMP.mpz_get_str(refdest, 10);
        }
        catch (GMPException e) {
            refdest_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            dest_str = GMP.mpz_get_str(dest, 10);
        }
        catch (GMPException e) {
            dest_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " refpower=" + refpwr + " power=" + pwr +
               " dividend=" + dividend_str + " divisor=" + divisor_str +
               " refdest=" + refdest_str +
               " dest=" + dest_str;
        throw new Exception(emsg);
    }

}
