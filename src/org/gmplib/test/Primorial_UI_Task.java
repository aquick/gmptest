package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Primorial_UI_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Primorial_UI_Task";
    
    public Primorial_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static boolean isprime (long t)
    {
        long q;
        long r;
        long d;

        if (t < 3 || (t & 1) == 0) {
          return t == 2;
        }

        for (d = 3, r = 1; r != 0; d += 2) {
            q = t / d;
            r = t - q * d;
            if (q < d) {
	        return true;
            }
        }
        return false;
    }

    public void run()
    {
        long  n;
        long  limit = 222; // 2222;
        mpz_t f;
        mpz_t r;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            f = new mpz_t();
            r = new mpz_t();
            //tests_start ();
            
            if (params.length > 0) {
                limit = (long)params[0].intValue();
            }

            GMP.mpz_set_ui (f, 1);  /* 0# = 1 */

            for (n = 0; n < limit; n++) {
                GMP.mpz_primorial_ui (r, n);
                GMP.mpz_internal_CHECK_FORMAT (r);

                if (GMP.mpz_cmp (f, r) != 0) {
                    /***
                    printf ("mpz_primorial_ui(%lu) wrong\n", n);
                    printf ("  got  "); mpz_out_str (stdout, 10, r); printf("\n");
                    printf ("  want "); mpz_out_str (stdout, 10, f); printf("\n");
                    abort ();
                    ***/
                    dump_abort("mpz_primorial_ui(" + n + ") wrong", r, f);
                }

                if (isprime (n+1)) {
                    GMP.mpz_mul_ui (f, f, n+1);  /* p# = (p-1)# * (p) */
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (n % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(n+1)*100.0/(float)limit)));
                }
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

    private void dump_abort(String msg,
                            mpz_t got, mpz_t want)
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

}
