package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class GCD_UI_Task extends TaskBase implements Runnable {

    private static final String TAG = "GCD_UI_Task";
    
    public GCD_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    /* Check mpz_gcd_ui doesn't try to return a value out of range.
       This was wrong in gmp 4.1.2 with a long long limb.  */
    private void check_ui_range ()
        throws Exception
    {
        long  got;
        mpz_t  x = new mpz_t();
        mpz_t  z = new mpz_t();
        int  i;

        GMP.mpz_set_ui (x, GMP.ULONG_MAX);

        for (i = 0; i < 20; i++) {
            GMP.mpz_mul_2exp (x, x, 1L);
            got = GMP.mpz_gcd_ui (z, x, 0L);
            if (got != 0) {
                throw new Exception ("mpz_gcd_ui (ULONG_MAX*2^" + i + ", 0) returned " + got + " should be 0");
                /***
                printf ("mpz_gcd_ui (ULONG_MAX*2^%d, 0)\n", i);
                printf ("   return %#lx\n", got);
                printf ("   should be 0\n");
                abort ();
                ***/
            }
        }
    }

    public void run()
    {
        //int limit = 512;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            Log.d(TAG, "no randomness");
            check_ui_range ();
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

}
