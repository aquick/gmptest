package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class SqrtRem_Task extends TaskBase implements Runnable
{
    private static final String TAG = "SqrtRem_Task";
    
    public SqrtRem_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
        mpz_t x2;
        mpz_t x;
        mpz_t rem;
        mpz_t temp;
        mpz_t temp2;
        long x2_size;
        int i;
        int reps = 100; // 1000;
        randstate_t rands;
        mpz_t bs;
        long size_range;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            x2 = new mpz_t();
            x = new mpz_t();
            rem = new mpz_t();
            temp2 = new mpz_t();
            temp = new mpz_t();
            bs = new mpz_t();
            //tests_start ();
            
            seed = uinterface.getSeed();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            String s = "seed=" + seed;
            Log.d(TAG, s);
            uinterface.display(s);
            rands = new randstate_t(seed);

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 17 + 2; /* 0..262144 bit operands */

                GMP.mpz_urandomb (bs, rands, size_range);
                x2_size = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (x2, rands, x2_size);

                /* printf ("%ld\n", SIZ (x2)); */

                GMP.mpz_sqrt(temp, x2);
                GMP.mpz_internal_CHECK_FORMAT (temp);                
                
                GMP.mpz_sqrtrem (x, rem, x2);
                GMP.mpz_internal_CHECK_FORMAT (x);
                GMP.mpz_internal_CHECK_FORMAT (rem);
                
                /* Are results different? */
                if (GMP.mpz_cmp(temp,  x) != 0) {
                    dump_abort (x2, x, rem);
                }

                GMP.mpz_mul (temp, x, x);

                /* Is square of result > argument?  */
                if (GMP.mpz_cmp (temp, x2) > 0) {
                    dump_abort (x2, x, rem);
                }

                GMP.mpz_add_ui (temp2, x, 1);
                GMP.mpz_mul (temp2, temp2, temp2);

                /* Is square of (result + 1) <= argument?  */
                if (GMP.mpz_cmp (temp2, x2) <= 0) {
                    dump_abort (x2, x, rem);
                }

                GMP.mpz_add (temp2, temp, rem);

                /* Is the remainder wrong?  */
                if (GMP.mpz_cmp (x2, temp2) != 0) {
                    dump_abort (x2, x, rem);
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (i % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
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

    private void dump_abort(mpz_t x2, mpz_t x, mpz_t rem)
        throws Exception
    {
        String x2_str = "";
        String x_str = "";
        String rem_str = "";
        String emsg;
        try {
            x2_str = GMP.mpz_get_str(x2, 10);
        }
        catch (GMPException e) {
            x2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            rem_str = GMP.mpz_get_str(rem, 10);
        }
        catch (GMPException e) {
            rem_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: x2=" + x2_str + " x=" + x_str + " remainder=" + rem_str;
        throw new Exception(emsg);
    }
}
