package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class LucNum_UI_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "LucNum_UI_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public LucNum_UI_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private void check_sequence (long limit)
        throws Exception
    {
        long  n;
        mpz_t want_ln;
        mpz_t want_ln1;
        mpz_t got_ln;
        mpz_t got_ln1;

        /* start at n==0 */
        want_ln = new mpz_t();
        want_ln1 = new mpz_t();
        got_ln = new mpz_t();
        got_ln1 = new mpz_t();

        GMP.mpz_set_si (want_ln1, -1); /* L[-1] */
        GMP.mpz_set_ui (want_ln,  2);  /* L[0]   */

        for (n = 0; n < limit; n++) {
            GMP.mpz_lucnum2_ui (got_ln, got_ln1, n);
            GMP.mpz_internal_CHECK_FORMAT (got_ln);
            GMP.mpz_internal_CHECK_FORMAT (got_ln1);
            if (GMP.mpz_cmp (got_ln, want_ln) != 0 || GMP.mpz_cmp (got_ln1, want_ln1) != 0) {
                dump_abort2 ("mpz_lucnum2_ui(" + n + ") wrong",
                             want_ln, got_ln, want_ln1, got_ln1);
                /***
                printf ("mpz_lucnum2_ui(%lu) wrong\n", n);
                mpz_trace ("want ln ", want_ln);
                mpz_trace ("got  ln ",  got_ln);
                mpz_trace ("want ln1", want_ln1);
                mpz_trace ("got  ln1",  got_ln1);
                abort ();
                ***/
            }

            GMP.mpz_lucnum_ui (got_ln, n);
            GMP.mpz_internal_CHECK_FORMAT (got_ln);
            if (GMP.mpz_cmp (got_ln, want_ln) != 0) {
                dump_abort ("mpz_lucnum_ui(" + n + ") wrong",
                             want_ln, got_ln);
                /***
                printf ("mpz_lucnum_ui(%lu) wrong\n", n);
                mpz_trace ("want ln", want_ln);
                mpz_trace ("got  ln", got_ln);
                abort ();
                ***/
            }

            GMP.mpz_add (want_ln1, want_ln1, want_ln);  /* L[n+1] = L[n] + L[n-1] */
            GMP.mpz_swap (want_ln1, want_ln);
            if (isCancelled()) {
                throw new Exception("Task cancelled");
            }
            if (n % 100 == 0) {
                 publishProgress(new Integer((int)((float)(n+1)*100.0/(float)limit)));
            }
        }

    }

    protected Integer doInBackground(Integer... params)
    {
        long  limit = 100 * 32; // * GMP_LIMB_BITS;
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();
            
            if (params.length > 0) {
                limit = (long)params[0].intValue();
            }

            /* for small limb testing */
            if (limit > GMP.ULONG_MAX) {
                limit = GMP.ULONG_MAX;
            }
            check_sequence(limit);

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

    private void dump_abort(String msg,
                            mpz_t want, mpz_t got)
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
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg,
                            mpz_t want, mpz_t got, mpz_t want1, mpz_t got1)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String got1_str = "";
        String want1_str = "";
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
            got1_str = GMP.mpz_get_str(got1, 10);
        }
        catch (GMPException e) {
            got1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want1_str = GMP.mpz_get_str(want1, 10);
        }
        catch (GMPException e) {
            want1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               " got=" + got_str + " want=" + want_str +
               " got1=" + got1_str + " want1=" + want1_str;
        throw new Exception(emsg);
    }

}
