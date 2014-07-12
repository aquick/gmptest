package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class GCD_UI_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "GCD_UI_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public GCD_UI_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
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

    protected Integer doInBackground(Integer... params)
    {
        int limit = 512;
        int ret = 0;

        try {
            GMP.init();
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
        return ret;
    }

    protected void onPreExecute()
    {
        uinterface.display(TAG);
    }

    protected void onProgressUpdate(Integer... progress)
    {
        uinterface.display("progress=" + progress[0]);
    }

    protected void onPostExecute(Integer result)
    {
        uinterface.display("result=" + result);
        if (result == 0) {
            uinterface.display("PASS");
            uinterface.nextTask();
        } else {
            uinterface.display(failmsg);
            uinterface.display("FAIL");
        }
    }

    protected void onCancelled(Integer result)
    {
        uinterface.display("result=" + result);
        uinterface.display(failmsg);
        uinterface.display("FAIL");
    }

    private String failmsg;

}
