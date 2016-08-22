package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Fib_UI_Task extends TaskBase implements Runnable {

    private static final String TAG = "Fib_UI_Task";
    
    public Fib_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static long MPZ_FIB_SIZE_FLOAT(int n)
    {
        return (long) ((float)n * 0.6942419 / GMP.GMP_NUMB_BITS() + 1);
    }

    private static long MPN_FIB2_SIZE(int n)
    {
        return (long) (n/32 * 23 / GMP.GMP_NUMB_BITS()) + 4;
    }


    private static void check_fib_table ()
        throws Exception
    {
        int        i;
        long       want;

        if (TestUtil.fib_table(-1) != 1) {
            throw new Exception("fib_table(-1) incorrect");
        }
        if (TestUtil.fib_table(0) != 0) {
            throw new Exception("fib_table(0) incorrect");
        }

        for (i = 1; i <= TestUtil.FIB_TABLE_LIMIT; i++) {
            want = TestUtil.fib_table(i-1) + TestUtil.fib_table(i-2);
            if (TestUtil.fib_table(i) != want) {
                throw new Exception("fib_table(" + i + ") incorrect");
                /***
                printf ("FIB_TABLE(%d) wrong\n", i);
                gmp_printf ("  got  %#Nx\n", &FIB_TABLE(i), 1);
                gmp_printf ("  want %#Nx\n", &want, 1);
                abort ();
                ***/
            }
        }
    }

    public void run()
    {
        int   n;
        int   limit = 100 * GMP.GMP_LIMB_BITS();
        int   ret = 0;
        mpz_t          want_fn;
        mpz_t          want_fn1;
        mpz_t          got_fn;
        mpz_t          got_fn1;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            Log.d(TAG, "no randomness");

            want_fn = new mpz_t();
            want_fn1 = new mpz_t();
            got_fn = new mpz_t();
            got_fn1 = new mpz_t();

            if (params.length > 0) {
                limit = params[0].intValue();
            }
            check_fib_table ();

            /* start at n==0 */
            GMP.mpz_set_ui (want_fn1, 1L);  /* F[-1] */
            GMP.mpz_set_ui (want_fn,  0);  /* F[0]   */

            for (n = 0; n < limit; n++) {
                /* check our float formula seems right */
                if (MPZ_FIB_SIZE_FLOAT (n) < GMP.mpz_internal_SIZ(want_fn)) {
                    throw new Exception("MPZ_FIB_SIZE_FLOAT wrong at n=" + n);
                    /***
                    printf ("MPZ_FIB_SIZE_FLOAT wrong at n=%lu\n", n);
                    printf ("  MPZ_FIB_SIZE_FLOAT  %ld\n", MPZ_FIB_SIZE_FLOAT (n));
                    printf ("  SIZ(want_fn)        %d\n", SIZ(want_fn));
                    abort ();
                    ***/
                }

                /* check MPN_FIB2_SIZE seems right, compared to actual size and
                   compared to our float formula */
                if (MPN_FIB2_SIZE (n) < MPZ_FIB_SIZE_FLOAT (n)) {
                    throw new Exception("MPZ_FIB2_SIZE wrong at n=" + n);
                    /***
                    printf ("MPN_FIB2_SIZE wrong at n=%lu\n", n);
                    printf ("  MPN_FIB2_SIZE       %ld\n", MPN_FIB2_SIZE (n));
                    printf ("  MPZ_FIB_SIZE_FLOAT  %ld\n", MPZ_FIB_SIZE_FLOAT (n));
                    abort ();
                    ***/
                }
                if (MPN_FIB2_SIZE (n) < GMP.mpz_internal_SIZ(want_fn)) {
                    throw new Exception("MPZ_FIB2_SIZE wrong at n=" + n);
                    /***
                    printf ("MPN_FIB2_SIZE wrong at n=%lu\n", n);
                    printf ("  MPN_FIB2_SIZE  %ld\n", MPN_FIB2_SIZE (n));
                    printf ("  SIZ(want_fn)   %d\n", SIZ(want_fn));
                    abort ();
                    ***/
                }

                GMP.mpz_fib2_ui (got_fn, got_fn1, n);
                GMP.mpz_internal_CHECK_FORMAT (got_fn);
                GMP.mpz_internal_CHECK_FORMAT (got_fn1);
                if (GMP.mpz_cmp (got_fn, want_fn) != 0 || GMP.mpz_cmp (got_fn1, want_fn1) != 0) {
                    dump_abort ("mpz_fib2_ui(" + n + ") wrong", want_fn, got_fn, want_fn1, got_fn1);
                    /***
                    printf ("mpz_fib2_ui(%lu) wrong\n", n);
                    mpz_trace ("want fn ", want_fn);
                    mpz_trace ("got  fn ",  got_fn);
                    mpz_trace ("want fn1", want_fn1);
                    mpz_trace ("got  fn1",  got_fn1);
                    abort ();
                    ***/
                }

                GMP.mpz_fib_ui (got_fn, n);
                GMP.mpz_internal_CHECK_FORMAT (got_fn);
                if (GMP.mpz_cmp (got_fn, want_fn) != 0) {
                    dump_abort2 ("mpz_fib_ui(" + n + ") wrong", want_fn, got_fn);
                    /***
                    printf ("mpz_fib_ui(%lu) wrong\n", n);
                    mpz_trace ("want fn", want_fn);
                    mpz_trace ("got  fn", got_fn);
                    abort ();
                    ***/
                }

                GMP.mpz_add (want_fn1, want_fn1, want_fn);  /* F[n+1] = F[n] + F[n-1] */
                GMP.mpz_swap (want_fn1, want_fn);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (n % 100 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(n+1)*100.0/(float)limit)));
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

    private void dump_abort(String msg, mpz_t want, mpz_t got, mpz_t want1, mpz_t got1)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String want1_str = "";
        String got1_str = "";
        String emsg;
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want1_str = GMP.mpz_get_str(want1, 10);
        }
        catch (GMPException e) {
            want1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got1_str = GMP.mpz_get_str(got1, 10);
        }
        catch (GMPException e) {
            got1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " want fn=" + want_str + " got fn=" + got_str + " want fn1=" + want1_str + " got fn1=" + got1_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t want, mpz_t got)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " want fn=" + want_str + " got fn=" + got_str;
        throw new Exception(emsg);
    }
}
