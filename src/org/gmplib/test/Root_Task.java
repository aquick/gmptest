package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class Root_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "Root_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public Root_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private void check_one (mpz_t root1, mpz_t x2, long nth, int i)
        throws Exception
    {
        mpz_t temp = new mpz_t();
        mpz_t temp2 = new mpz_t();
        mpz_t root2 = new mpz_t();
        mpz_t rem2 = new mpz_t();

        GMP.mpz_internal_CHECK_FORMAT (root1);

        GMP.mpz_rootrem (root2, rem2, x2, nth);
        GMP.mpz_internal_CHECK_FORMAT (root2);
        GMP.mpz_internal_CHECK_FORMAT (rem2);

        GMP.mpz_pow_ui (temp, root1, nth);
        GMP.mpz_internal_CHECK_FORMAT (temp);

        GMP.mpz_add (temp2, temp, rem2);

        /* Is power of result > argument?  */
        if (GMP.mpz_cmp (root1, root2) != 0 ||
            GMP.mpz_cmp (x2, temp2) != 0 ||
            GMP.mpz_cmpabs (temp, x2) > 0) {
            dump_abort("ERROR after test " + i, x2, root1, root2, nth);
            /***
            fprintf (stderr, "ERROR after test %d\n", i);
            debug_mp (x2, 10);
            debug_mp (root1, 10);
            debug_mp (root2, 10);
            fprintf (stderr, "nth: %lu\n", nth);
            abort ();
            ***/
        }

        if (nth > 1 && GMP.mpz_cmp_ui (temp, 1L) > 0 && GMP.mpz_perfect_power_p (temp) == 0) {
            dump_abort2("ERROR in mpz_perfect_power_p after test " + i, temp, root1, nth);
            /***
            fprintf (stderr, "ERROR in mpz_perfect_power_p after test %d\n", i);
            debug_mp (temp, 10);
            debug_mp (root1, 10);
            fprintf (stderr, "nth: %lu\n", nth);
            abort ();
            ***/
        }

        if (nth <= 10000 && GMP.mpz_sgn(x2) > 0) { /* skip too expensive test */
            GMP.mpz_add_ui (temp2, root1, 1L);
            GMP.mpz_pow_ui (temp2, temp2, nth);
            GMP.mpz_internal_CHECK_FORMAT (temp2);

            /* Is square of (result + 1) <= argument?  */
            if (GMP.mpz_cmp (temp2, x2) <= 0) {
                dump_abort3("ERROR after test " + i, x2, root1, nth);
                /***
                fprintf (stderr, "ERROR after test %d\n", i);
                debug_mp (x2, 10);
	                debug_mp (root1, 10);
                fprintf (stderr, "nth: %lu\n", nth);
	                abort ();
                ***/
            }
        }

    }

    protected Integer doInBackground(Integer... params)
    {
        mpz_t x2;
        mpz_t root1;
        long x2_size;
        int i;
        int reps = 50; // 500;
        randstate_t rands;
        mpz_t bs;
        long size_range;
        long nth;
        long bsi;
        long seed;
        int ret = 0;

        try {
            GMP.init();
            x2 = new mpz_t();
            root1 = new mpz_t();
            bs = new mpz_t();
            //tests_start ();
            
            seed = rng.nextInt();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            Log.d(TAG, "seed=" + seed);
            rands = new randstate_t(seed);

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            /* This triggers a gcc 4.3.2 bug */
            GMP.mpz_set_str (x2, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000000000000000000000000000000000000000000000000000000000000002", 16);
            GMP.mpz_root (root1, x2, 2);
            check_one (root1, x2, 2, -1);

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 17 + 2;

                GMP.mpz_urandomb (bs, rands, size_range);
                x2_size = GMP.mpz_get_ui (bs) + 10;
                GMP.mpz_rrandomb (x2, rands, x2_size);

                GMP.mpz_urandomb (bs, rands, 15);
                nth = GMP.mpz_getlimbn (bs, 0) % GMP.mpz_sizeinbase (x2, 2) + 2;

                GMP.mpz_root (root1, x2, nth);

                GMP.mpz_urandomb (bs, rands, 4);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 1) != 0) {
                    /* With 50% probability, set x2 near a perfect power.  */
                    GMP.mpz_pow_ui (x2, root1, nth);
                    if ((bsi & 2) != 0) {
                        GMP.mpz_sub_ui (x2, x2, bsi >> 2);
                        GMP.mpz_abs (x2, x2);
                    } else {
                        GMP.mpz_add_ui (x2, x2, bsi >> 2);
                    }
                    GMP.mpz_root (root1, x2, nth);
                }

                check_one (root1, x2, nth, i);

                if (((nth & 1) != 0) && ((bsi & 2) != 0)) {
                    GMP.mpz_neg (x2, x2);
                    GMP.mpz_neg (root1, root1);
                    check_one (root1, x2, nth, i);
                }
                if (isCancelled()) {
                    throw new Exception("Task cancelled");
                }
                if (i % 10 == 0) {
                    publishProgress(new Integer((int)((float)(i+1)*100.0/(float)reps)));
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

    private void dump_abort(String msg, mpz_t x2, mpz_t root1, mpz_t root2, long nth)
        throws Exception
    {
        String x2_str = "";
        String root1_str = "";
        String root2_str = "";
        String emsg;
        try {
            x2_str = GMP.mpz_get_str(x2, 10);
        }
        catch (GMPException e) {
            x2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            root1_str = GMP.mpz_get_str(root1, 10);
        }
        catch (GMPException e) {
            root1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            root2_str = GMP.mpz_get_str(root2, 10);
        }
        catch (GMPException e) {
            root2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x2=" + x2_str + " root1=" + root1_str + " root2=" + root2_str +
               " nth=" + nth;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t x2, mpz_t root1, long nth)
        throws Exception
    {
        String x2_str = "";
        String root1_str = "";
        String emsg;
        try {
            x2_str = GMP.mpz_get_str(x2, 10);
        }
        catch (GMPException e) {
            x2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            root1_str = GMP.mpz_get_str(root1, 10);
        }
        catch (GMPException e) {
            root1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " temp=" + x2_str + " root1=" + root1_str +
               " nth=" + nth;
        throw new Exception(emsg);
    }

    private void dump_abort3(String msg, mpz_t x2, mpz_t root1, long nth)
        throws Exception
    {
        String x2_str = "";
        String root1_str = "";
        String emsg;
        try {
            x2_str = GMP.mpz_get_str(x2, 10);
        }
        catch (GMPException e) {
            x2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            root1_str = GMP.mpz_get_str(root1, 10);
        }
        catch (GMPException e) {
            root1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x2=" + x2_str + " root1=" + root1_str +
               " nth=" + nth;
        throw new Exception(emsg);
    }
}
