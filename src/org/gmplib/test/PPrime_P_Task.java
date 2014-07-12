package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class PPrime_P_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "PPrime_P_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public PPrime_P_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    /* return 1 if prime, 0 if composite */
    private static int isprime (int n)
    {
        long  i;

        n = Math.abs(n);

        if (n < 2) return 0;
        if (n == 2) return 1;
        if ((n & 1) == 0) return 0;

        for (i = 3; i < n; i++) {
            if ((n % i) == 0) return 0;
        }
        return 1;
    }

    private void check_one (mpz_t n, int want)
        throws Exception
    {
        int  got;

        got = GMP.mpz_probab_prime_p (n, 25);

        /* "definitely prime" is fine if we only wanted "probably prime" */
        if (got == 2 && want == 1) {
            want = 2;
	}

        if (got != want) {
            /***
            printf ("mpz_probab_prime_p\n");
            mpz_trace ("  n    ", n);
            printf    ("  got =%d", got);
            printf    ("  want=%d", want);
            abort ();
            ***/
            dump_abort("mpz_probab_prime", n, got, want);
        }
    }

    private void check_pn (mpz_t n, int want)
        throws Exception
    {
        check_one (n, want);
        GMP.mpz_neg (n, n);
        check_one (n, want);
    }

    /* expect certainty for small n */
    private void check_small ()
        throws Exception
    {
        mpz_t  n;
        int    i;

        n = new mpz_t();

        for (i = 0; i < 300; i++) {
            GMP.mpz_set_si (n, i);
            check_pn (n, isprime (i));
        }
    }

    protected Integer doInBackground(Integer... params)
    {
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();
            
            check_small ();
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

    private void dump_abort(String msg, mpz_t n,
                            int got, int want)
        throws Exception
    {
        String n_str = "";
        String emsg;
        try {
            n_str = GMP.mpz_get_str(n, 10);
        }
        catch (GMPException e) {
            n_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " n=" + n_str +
               " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }

}
