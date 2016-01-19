package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Get_D_Task extends TaskBase implements Runnable {

    private static final String TAG = "Get_D_Task";
    
    public Get_D_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_onebit (int limit)
        throws Exception
    {
        int     i;
        mpz_t   z;
        double  got;
        double  want;

        z = new mpz_t();

        GMP.mpz_set_ui (z, 1L);
        want = 1.0;

        for (i = 0; i < limit; i++) {
            got = GMP.mpz_get_d (z);

            if (got != want) {
                dump_abort2 ("mpz_get_d wrong on 2**" + i, z, want, got);
                /***
                printf    ("mpz_get_d wrong on 2**%d\n", i);
                mpz_trace ("   z    ", z);
                printf    ("   want  %.20g\n", want);
                printf    ("   got   %.20g\n", got);
                abort();
                ***/
            }

            GMP.mpz_mul_2exp (z, z, 1L);
            want *= 2.0;
        }
    }

    public void run()
    {
        int limit = 512;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            Log.d(TAG, "no randomness");
            
            check_onebit (limit);
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

    private void dump_abort2(String msg, mpz_t z, double want, double got)
        throws Exception
    {
        String z_str = "";
        String emsg;
        try {
            z_str = GMP.mpz_get_str(z, 10);
        }
        catch (GMPException e) {
            z_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " z=" + z_str + " want=" + want + " got=" + got;
        throw new Exception(emsg);
    }
}
