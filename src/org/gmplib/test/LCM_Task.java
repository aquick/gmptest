package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class LCM_Task extends TaskBase implements Runnable
{
    private static final String TAG = "LCM_Task";
    
    public LCM_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_all (mpz_t want, mpz_t x_orig, mpz_t y_orig)
        throws Exception
    {
        mpz_t  got;
        mpz_t  x;
        mpz_t  y;
        int    negx;
        int    negy;
        int    swap;
        int    inplace;

        got = new mpz_t();
        x = new mpz_t();
        y = new mpz_t();
        GMP.mpz_set(x, x_orig);
        GMP.mpz_set(y, y_orig);

        for (swap = 0; swap < 2; swap++) {
            GMP.mpz_swap (x, y);

            for (negx = 0; negx < 2; negx++) {
                GMP.mpz_neg (x, x);

                for (negy = 0; negy < 2; negy++) {
                    GMP.mpz_neg (y, y);

                    for (inplace = 0; inplace <= 1; inplace++) {
                        if (inplace != 0) {
                            GMP.mpz_set (got, x);
                            GMP.mpz_lcm (got, got, y);
                        } else {
                            GMP.mpz_lcm (got, x, y);
                        }
                        GMP.mpz_internal_CHECK_FORMAT (got);

                        if (GMP.mpz_cmp (got, want) != 0) {
                            dump_abort ("mpz_lcm wrong, inplace=" + inplace, x, y, got, want);
                            /***
                            printf ("mpz_lcm wrong, inplace=%d\n", inplace);
                            mpz_trace ("x", x);
                            mpz_trace ("y", y);
                            mpz_trace ("got", got);
                            mpz_trace ("want", want);
                            abort ();
                            ***/
                        }

                        if (GMP.mpz_fits_ulong_p (y) != 0) {
                            long  yu = GMP.mpz_get_ui (y);
                            if (inplace != 0) {
                                GMP.mpz_set (got, x);
                                GMP.mpz_lcm_ui (got, got, yu);
                            } else {
                                GMP.mpz_lcm_ui (got, x, yu);
                            }

                            if (GMP.mpz_cmp (got, want) != 0) {
                                dump_abort ("mpz_lcm_ui wrong, inplace=" + inplace + " yu=" + yu, x, y, got, want);
                                /***
                                printf ("mpz_lcm_ui wrong, inplace=%d\n", inplace);
                                printf    ("yu=%lu\n", yu);
                                ***/
                            }
                        }
                    }
                }
            }
        }

    }


    private static final long[]  prime = new long[] {
    2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,
    101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,
    191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,
    281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,
    389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487
    };

    private void check_primes ()
        throws Exception
    {
        mpz_t  want;
        mpz_t  x;
        mpz_t  y;
        int    i;

        want = new mpz_t();
        x = new mpz_t();
        y = new mpz_t();

        /* Check zeros. */
        GMP.mpz_set_ui (want, 0);
        GMP.mpz_set_ui (x, 1L);
        check_all (want, want, want);
        check_all (want, want, x);
        check_all (want, x, want);

        /* New prime each time. */
        GMP.mpz_set_ui (want, 1L);
        for (i = 0; i < prime.length; i++) {
            GMP.mpz_set (x, want);
            GMP.mpz_set_ui (y, prime[i]);
            GMP.mpz_mul_ui (want, want, prime[i]);
            check_all (want, x, y);
        }

        /* Old prime each time. */
        GMP.mpz_set (x, want);
        for (i = 0; i < prime.length; i++) {
            GMP.mpz_set_ui (y, prime[i]);
            check_all (want, x, y);
        }

        /* One old, one new each time. */
        GMP.mpz_set_ui (want, prime[0]);
        for (i = 1; i < prime.length; i++) {
            GMP.mpz_set (x, want);
            GMP.mpz_set_ui (y, prime[i] * prime[i-1]);
            GMP.mpz_mul_ui (want, want, prime[i]);
            check_all (want, x, y);
        }

        /* Triplets with A,B in x and B,C in y. */
        GMP.mpz_set_ui (want, 1L);
        GMP.mpz_set_ui (x, 1L);
        GMP.mpz_set_ui (y, 1L);
        for (i = 0; i+2 < prime.length; i += 3) {
            GMP.mpz_mul_ui (want, want, prime[i]);
            GMP.mpz_mul_ui (want, want, prime[i+1]);
            GMP.mpz_mul_ui (want, want, prime[i+2]);

            GMP.mpz_mul_ui (x, x, prime[i]);
            GMP.mpz_mul_ui (x, x, prime[i+1]);

            GMP.mpz_mul_ui (y, y, prime[i+1]);
            GMP.mpz_mul_ui (y, y, prime[i+2]);

            check_all (want, x, y);
        }
    }

    public void run()
    {
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            check_primes();

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

    private void dump_abort(String msg,
                            mpz_t x, mpz_t y, mpz_t got, mpz_t want)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               " x=" + x_str + " y=" + y_str +
               " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

}
