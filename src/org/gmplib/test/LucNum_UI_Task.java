package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class LucNum_UI_Task extends TaskBase implements Runnable
{
    private static final String TAG = "LucNum_UI_Task";
    
    public LucNum_UI_Task(UI ui)
    {
        super(ui, TAG);
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
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (n % 100 == 0) {
                 onProgressUpdate(Integer.valueOf((int)((float)(n+1)*100.0/(float)limit)));
            }
        }

    }

    public void run()
    {
        long  limit = 100 * GMP.GMP_LIMB_BITS();
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
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
        onPostExecute(Integer.valueOf(ret));
    }

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
