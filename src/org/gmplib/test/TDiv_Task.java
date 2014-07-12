package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class TDiv_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "TDiv_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public TDiv_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    protected Integer doInBackground(Integer... params)
    {
        mpz_t dividend;
        mpz_t quotient;
        mpz_t remainder;
        mpz_t quotient2;
        mpz_t remainder2;
        mpz_t temp;
        long dividend_size;
        long divisor_size;
        mpz_t divisor;
        int i;
        int reps = 100; // 1000;
        randstate_t rands;
        mpz_t bs;
        long bsi;
        long size_range;
        long seed;
        int ret = 0;

        try {
            GMP.init();
            dividend = new mpz_t();
            divisor = new mpz_t();
            quotient = new mpz_t();
            remainder = new mpz_t();
            quotient2 = new mpz_t();
            remainder2 = new mpz_t();
            temp = new mpz_t();
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

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 18 + 2; /* 0..524288 bit operands */

                do {
                    GMP.mpz_urandomb (bs, rands, size_range);
                    divisor_size = GMP.mpz_get_ui (bs);
                    GMP.mpz_rrandomb (divisor, rands, divisor_size);
                } while (GMP.mpz_sgn (divisor) == 0);

                GMP.mpz_urandomb (bs, rands, size_range);
                dividend_size = GMP.mpz_get_ui (bs) + divisor_size;
                GMP.mpz_rrandomb (dividend, rands, dividend_size);

                GMP.mpz_urandomb (bs, rands, 2);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 1) != 0) {
                    GMP.mpz_neg (dividend, dividend);
                }
                if ((bsi & 2) != 0) {
                    GMP.mpz_neg (divisor, divisor);
                }

                /* printf ("%ld %ld\n", SIZ (dividend), SIZ (divisor)); */

                GMP.mpz_tdiv_qr (quotient, remainder, dividend, divisor);
                GMP.mpz_tdiv_q (quotient2, dividend, divisor);
                GMP.mpz_tdiv_r (remainder2, dividend, divisor);

                /* First determine that the quotients and remainders computed
                    with different functions are equal.  */
                if (GMP.mpz_cmp (quotient, quotient2) != 0) {
                    dump_abort ("", dividend, divisor);
                }
                if (GMP.mpz_cmp (remainder, remainder2) != 0) {
                    dump_abort ("", dividend, divisor);
                }

                /* Check if the sign of the quotient is correct.  */
                if (GMP.mpz_cmp_ui (quotient, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (quotient, 0) < 0)
                        != ((GMP.mpz_cmp_ui (dividend, 0) ^ GMP.mpz_cmp_ui (divisor, 0)) < 0)) {
                        dump_abort ("", dividend, divisor);
                    }
                }

                /* Check if the remainder has the same sign as the dividend
                    (quotient rounded towards 0).  */
                if (GMP.mpz_cmp_ui (remainder, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (remainder, 0) < 0) != (GMP.mpz_cmp_ui (dividend, 0) < 0)) {
                        dump_abort ("", dividend, divisor);
                    }
                }

                GMP.mpz_mul (temp, quotient, divisor);
                GMP.mpz_add (temp, temp, remainder);
                if (GMP.mpz_cmp (temp, dividend) != 0) {
                    dump_abort ("", dividend, divisor);
                }

                GMP.mpz_abs (temp, divisor);
                GMP.mpz_abs (remainder, remainder);
                if (GMP.mpz_cmp (remainder, temp) >= 0) {
                    dump_abort ("", dividend, divisor);
                }
                if (isCancelled()) {
                    throw new Exception("Task cancelled");
                }
                if (i %10 == 0) {
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

    private void dump_abort(String msg, mpz_t dividend, mpz_t divisor)
        throws Exception
    {
        String dividend_str = "";
        String divisor_str = "";
        String emsg;
        try {
            dividend_str = GMP.mpz_get_str(dividend, 10);
        }
        catch (GMPException e) {
            dividend_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            divisor_str = GMP.mpz_get_str(divisor, 10);
        }
        catch (GMPException e) {
            divisor_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " dividend=" + dividend_str + " divisor=" + divisor_str;
        throw new Exception(emsg);
    }
}
