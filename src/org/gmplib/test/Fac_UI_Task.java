package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Fac_UI_Task extends TaskBase implements Runnable {

    private static final String TAG = "Fac_UI_Task";
    
    public Fac_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
        int   n;
        int   m;
        int   limit = 2222;
        mpz_t[] df = new mpz_t[2];
        mpz_t   f;
        mpz_t   r;
        int   ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            Log.d(TAG, "no randomness");

            if (params.length > 0) {
                limit = params[0].intValue();
            }
            df[0] = new mpz_t();
            df[1] = new mpz_t();
            f = new mpz_t();
            r = new mpz_t();
            GMP.mpz_set_ui (df[0], 1L);  /* 0!! = 1 */
            GMP.mpz_set_ui (df[1], 1L);  /* -1!! = 1 */
            GMP.mpz_set_ui (f, 1L);  /* 0! = 1 */

            for (n = 0, m = 0; n < limit; n++) {
                GMP.mpz_fac_ui (r, n);
                GMP.mpz_internal_CHECK_FORMAT (r);

                if (GMP.mpz_cmp (f, r) != 0) {
                    dump_abort2 ("mpz_fac_ui(" + n + ") wrong", f, r);
                    /***
                    printf ("mpz_fac_ui(%lu) wrong\n", n);
                    printf ("  got  "); mpz_out_str (stdout, 10, r); printf("\n");
                    printf ("  want "); mpz_out_str (stdout, 10, f); printf("\n");
                    abort ();
                    ***/
                }

                GMP.mpz_2fac_ui (r, n);
                GMP.mpz_internal_CHECK_FORMAT (r);

                if (GMP.mpz_cmp (df[m], r) != 0) {
                    dump_abort2 ("mpz_2fac_ui(" + n + ") wrong", df[m], r);
                    /***
                    printf ("mpz_2fac_ui(%lu) wrong\n", n);
                    printf ("  got  "); mpz_out_str (stdout, 10, r); printf("\n");
                    printf ("  want "); mpz_out_str (stdout, 10, df[m]); printf("\n");
                    abort ();
                    ***/
                }

                m ^= 1;
                GMP.mpz_mul_ui (df[m], df[m], (long)n+1);  /* (n+1)!! = (n-1)!! * (n+1) */
                GMP.mpz_mul_ui (f, f, (long)n+1);  /* (n+1)! = n! * (n+1) */
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (n % 100 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(n+1)*100.0/(float)limit)));
                }
            }
            n = 2097169; /* a prime = 1 mod 4*/
            GMP.mpz_fac_ui (f, (long)(n / 2)); /* ((n-1)/2)! */
            long mm = GMP.mpz_fdiv_ui (f, (long)n); /* ((n-1)/2)! mod n*/
            GMP.mpz_set_ui (f, mm);
            GMP.mpz_mul_ui (f, f, mm); /* (((n-1)/2)!)^2 */
            mm = GMP.mpz_fdiv_ui (f, (long)n); /* (((n-1)/2)!)^2 mod n*/
            m = (int)mm;
            if (m != n - 1) {
                dump_abort ("mpz_fac_ui(" + Integer.toString(n/2) + ") wrong\n" +
                    "  al-Haytham's theorem not verified: got " + m + ", expected " + Integer.toString(n-1) + ".");
                /***
                printf ("mpz_fac_ui(%lu) wrong\n", n / 2);
                printf (" al-Haytham's theorem not verified: got %lu, expected %lu.\n", m, n - 1);
                abort ();
                ***/
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

    private void dump_abort(String msg)
        throws Exception
    {
        String emsg;
        emsg = "ERROR: " + msg;
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
        emsg = "ERROR: " + msg + " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }
}
