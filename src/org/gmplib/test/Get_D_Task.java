package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class Get_D_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "Get_D_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public Get_D_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
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

    protected Integer doInBackground(Integer... params)
    {
        int limit = 512;
        int ret = 0;

        try {
            GMP.init();
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
