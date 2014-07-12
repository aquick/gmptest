package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class Mul_I_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "Mul_I_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public Mul_I_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private void compare_si (mpz_t got, mpz_t want, mpz_t x, int y)
        throws Exception
    {
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort("mpz_mul_si wrong", x, y, got, want);
            /***
            printf    ("mpz_mul_si wrong\n");
            mpz_trace ("  x", x);
            printf    ("  y=%ld (0x%lX)\n", y, y);
            mpz_trace ("  got ", got);
            mpz_trace ("  want", want);
            abort ();
            ***/
        }
    }

    private void compare_ui (mpz_t got, mpz_t want, mpz_t x, long y)
        throws Exception
    {
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort2("mpz_mul_ui wrong", x, y, got, want);
            /***
            printf    ("mpz_mul_ui wrong\n");
            mpz_trace ("  x", x);
            printf    ("  y=%lu (0x%lX)\n", y, y);
            mpz_trace ("  got ", got);
            mpz_trace ("  want", want);
            abort ();
            ***/
        }
    }

    private void check_samples (mpz_t x, mpz_t got, mpz_t want)
        throws Exception
    {
        {
            int  y;

            GMP.mpz_set_ui (x, 1L);
            y = 0;
            GMP.mpz_mul_si (got, x, y);
            GMP.mpz_set_si (want, y);
            compare_si (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = 1;
            GMP.mpz_mul_si (got, x, y);
            GMP.mpz_set_si (want, y);
            compare_si (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = -1;
            GMP.mpz_mul_si (got, x, y);
            GMP.mpz_set_si (want, y);
            compare_si (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = Integer.MIN_VALUE; // LONG_MIN;
            GMP.mpz_mul_si (got, x, y);
            GMP.mpz_set_si (want, y);
            compare_si (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = Integer.MAX_VALUE; // LONG_MAX;
            GMP.mpz_mul_si (got, x, y);
            GMP.mpz_set_si (want, y);
            compare_si (got, want, x, y);
        }

        {
            long y;

            GMP.mpz_set_ui (x, 1L);
            y = 0;
            GMP.mpz_mul_ui (got, x, y);
            GMP.mpz_set_ui (want, y);
            compare_ui (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = 1;
            GMP.mpz_mul_ui (got, x, y);
            GMP.mpz_set_ui (want, y);
            compare_ui (got, want, x, y);

            GMP.mpz_set_ui (x, 1L);
            y = GMP.ULONG_MAX;
            GMP.mpz_mul_ui (got, x, y);
            GMP.mpz_set_ui (want, y);
            compare_ui (got, want, x, y);
        }
    }

    protected Integer doInBackground(Integer... params)
    {
        mpz_t got;
        mpz_t want;
        mpz_t x;
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();

            x = new mpz_t();
            got = new mpz_t();
            want = new mpz_t();

            check_samples(x, got, want);
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

    private void dump_abort(String msg, mpz_t x, int y, mpz_t got, mpz_t want)
        throws Exception
    {
        String x_str = "";
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
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
        emsg = msg + " x=" + x_str + " y=" + y + " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t x, long y, mpz_t got, mpz_t want)
        throws Exception
    {
        String x_str = "";
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
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
        emsg = msg + " x=" + x_str + " y=" + y + " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

}
