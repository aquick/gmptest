package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class Set_SI_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "Set_SI_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public Set_SI_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private void check_data ()
        throws Exception
    {
        mpz_t  z = new mpz_t();

        GMP.mpz_set_si (z, 0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_si wrong on data", 0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_si (z, 1);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 1 || GMP.refmpn_cmp_allowzero(z, new long[] {1}, 1) != 0) {
            dump_abort2("mpz_set_si wrong on data", 1, GMP.mpz_internal_SIZ(z), 1L, z, new long[] {1});
        }
        GMP.mpz_set_si (z, -1);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != -1 || GMP.refmpn_cmp_allowzero(z, new long[] {1}, 1) != 0) {
            dump_abort2("mpz_set_si wrong on data", -1, GMP.mpz_internal_SIZ(z), -1L, z, new long[] {1});
        }
        GMP.mpz_set_si (z, GMP.LONG_MAX);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 1 || GMP.refmpn_cmp_allowzero(z, new long[] {GMP.LONG_MAX, 0}, 1) != 0) {
            dump_abort2("mpz_set_si wrong on data", GMP.LONG_MAX, GMP.mpz_internal_SIZ(z), 1L, z, new long[] {GMP.LONG_MAX, 0});
        }
        GMP.mpz_set_si (z, -GMP.LONG_MAX);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != -1 || GMP.refmpn_cmp_allowzero(z, new long[] {GMP.LONG_MAX, 0}, 1) != 0) {
            dump_abort2("mpz_set_si wrong on data", -GMP.LONG_MAX, GMP.mpz_internal_SIZ(z), -1L, z, new long[] {GMP.LONG_MAX, 0});
        }
        GMP.mpz_set_si (z, Integer.MIN_VALUE);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != -1 || GMP.refmpn_cmp_allowzero(z, new long[] {0x80000000L, 0}, 1) != 0) {
            dump_abort2("mpz_set_si wrong on data", Integer.MIN_VALUE, GMP.mpz_internal_SIZ(z), -1L, z, new long[] {0x80000000L, 0});
        }
    }


    protected Integer doInBackground(Integer... params)
    {
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();
            Log.d(TAG, "no randomness");
            
            check_data ();
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

    private void dump_abort2(String msg, int d, long gotsize, long wantsize, mpz_t got, long[] want)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = "";
            for (int i = 0; i < want.length; i++) {
                if (i > 0) want_str += ", ";
                want_str += Long.toString(want[i]);
            }
        }
        catch (Exception e) {
        }
        emsg = "ERROR: " + msg + " d=" + d +
           " gotsize=" + gotsize + " wantsize=" + wantsize +
           " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }
}
