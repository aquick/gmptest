package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Get_SI_Task extends TaskBase implements Runnable {

    private static final String TAG = "Get_SI_Task";
    
    public Get_SI_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_data ()
        throws Exception
    {
        mpz_t  z = new mpz_t();
        int    got;

        GMP.mpz_set_str (z, "0", 10);
        got = GMP.mpz_get_si(z);
        if (got != 0) {
            dump_abort2("mpz_get_si wrong on data", 0, got);
        }
        GMP.mpz_set_str (z, "1", 10);
        got = GMP.mpz_get_si(z);
        if (got != 1L) {
            dump_abort2("mpz_get_si wrong on data", 1, got);
        }
        GMP.mpz_set_str (z, "-1", 10);
        got = GMP.mpz_get_si(z);
        if (got != -1L) {
            dump_abort2("mpz_get_si wrong on data", -1, got);
        }
        GMP.mpz_set_str (z, "2", 10);
        got = GMP.mpz_get_si(z);
        if (got != 2L) {
            dump_abort2("mpz_get_si wrong on data", 2, got);
        }
        GMP.mpz_set_str (z, "-2", 10);
        got = GMP.mpz_get_si(z);
        if (got != -2L) {
            dump_abort2("mpz_get_si wrong on data", -2, got);
        }
        GMP.mpz_set_str (z, "12345", 10);
        got = GMP.mpz_get_si(z);
        if (got != 12345L) {
            dump_abort2("mpz_get_si wrong on data", 12345, got);
        }
        GMP.mpz_set_str (z, "-12345", 10);
        got = GMP.mpz_get_si(z);
        if (got != -12345L) {
            dump_abort2("mpz_get_si wrong on data", -12345, got);
        }
    }


    private void check_max ()
        throws Exception
    {
        mpz_t  n;
        int    want;
        int    got;

        n = new mpz_t();

        want = Integer.MAX_VALUE;
        GMP.mpz_set_si (n, want);
        got = GMP.mpz_get_si (n);
        if (got != want) {
            dump_abort2 ("mpz_get_si wrong on LONG_MAX", want, got);
        }

        want = Integer.MIN_VALUE;
        GMP.mpz_set_si (n, want);
        got = GMP.mpz_get_si (n);
        if (got != want) {
            dump_abort2 ("mpz_get_si wrong on LONG_MIN", want, got);
        }

        /* The following checks that -0x100000000 gives -0x80000000.  This doesn't
           actually fit in a long and the result from mpz_get_si() is undefined,
           but -0x80000000 is what comes out currently, and it should be that
           value irrespective of the mp_limb_t size (long or long long).  */

        want = Integer.MIN_VALUE;
        GMP.mpz_mul_2exp (n, n, 1);
        if (got != want) {
            dump_abort2 ("mpz_get_si wrong on -0x100...00", want, got);
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
            Log.d(TAG, "no randomness");
            
            check_data ();
            check_max ();
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

    private void dump_abort2(String msg, int want, int got)
        throws Exception
    {
        String emsg = "ERROR: " + msg + " want=" + want + " got=" + got;
        throw new Exception(emsg);
    }
}
