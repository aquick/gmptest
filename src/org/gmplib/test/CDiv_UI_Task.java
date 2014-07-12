package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.MutableInteger;
import org.gmplib.gmpjni.GMP.randstate_t;
//import java.io.IOException;

public class CDiv_UI_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "CDiv_UI_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public CDiv_UI_Task(UI ui, RandomNumberFile rng)
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
        long divisor;
        int i;
        long r_rq;
        long r_q;
        long r_r;
        long r;
        mpz_t bs;
        long bsi;
        long size_range;
        int reps = 1000; // 10000;
        randstate_t rands;
        long seed;
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();
            
            seed = rng.nextInt();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            Log.d(TAG, "seed=" + seed);
            rands = new randstate_t(seed);

            bs = new mpz_t();
            dividend = new mpz_t();
            quotient = new mpz_t();
            remainder = new mpz_t();
            quotient2 = new mpz_t();
            remainder2 = new mpz_t();
            temp = new mpz_t();

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 10 + 2; /* 0..2047 bit operands */

                do {
                    GMP.mpz_rrandomb (bs, rands, 64);
                    divisor = GMP.mpz_get_ui (bs);
                } while (divisor == 0);

                GMP.mpz_urandomb (bs, rands, size_range);
                dividend_size = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (dividend, rands, dividend_size);

                GMP.mpz_urandomb (bs, rands, 2);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 1) != 0) GMP.mpz_neg (dividend, dividend);

                /* printf ("%ld\n", SIZ (dividend)); */

                r_rq = GMP.mpz_cdiv_qr_ui (quotient, remainder, dividend, divisor);
                r_q = GMP.mpz_cdiv_q_ui (quotient2, dividend, divisor);
                r_r = GMP.mpz_cdiv_r_ui (remainder2, dividend, divisor);
                r = GMP.mpz_cdiv_ui (dividend, divisor);

                /* First determine that the quotients and remainders computed
                   with different functions are equal.  */
                if (GMP.mpz_cmp (quotient, quotient2) != 0) {
                    dump_abort ("quotients from mpz_cdiv_qr_ui and mpz_cdiv_q_ui differ",
                                dividend, divisor);
                }
                if (GMP.mpz_cmp (remainder, remainder2) != 0) {
                    dump_abort ("remainders from mpz_cdiv_qr_ui and mpz_cdiv_r_ui differ",
                                dividend, divisor);
                }

                /* Check if the sign of the quotient is correct.  */
                if (GMP.mpz_cmp_ui (quotient, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (quotient, 0) < 0) != (GMP.mpz_cmp_ui (dividend, 0) < 0)) {
                        dump_abort ("quotient sign wrong", dividend, divisor);
                    }
                }

                /* Check if the remainder has the opposite sign as the (positive) divisor
                   (quotient rounded towards minus infinity).  */
                if (GMP.mpz_cmp_ui (remainder, 0) != 0) {
                    if (GMP.mpz_cmp_ui (remainder, 0) > 0) {
                        dump_abort ("remainder sign wrong", dividend, divisor);
                    }
                }

                GMP.mpz_mul_ui (temp, quotient, divisor);
                GMP.mpz_add (temp, temp, remainder);
                if (GMP.mpz_cmp (temp, dividend) != 0) {
                    dump_abort ("n mod d != n - [n/d]*d", dividend, divisor);
                }

                GMP.mpz_abs (remainder, remainder);
                if (GMP.mpz_cmp_ui (remainder, divisor) >= 0) {
                    dump_abort ("remainder greater than divisor", dividend, divisor);
                }

                if (GMP.mpz_cmp_ui (remainder, r_rq) != 0) {
                    dump_abort ("remainder returned from mpz_cdiv_qr_ui is wrong",
                                dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r_q) != 0) {
                    dump_abort ("remainder returned from mpz_cdiv_q_ui is wrong",
                                dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r_r) != 0) {
                    dump_abort ("remainder returned from mpz_cdiv_r_ui is wrong",
                                dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r) != 0) {
                    dump_abort ("remainder returned from mpz_cdiv_ui is wrong",
                                dividend, divisor);
                }
                if (isCancelled()) break;
                if (i % 100 == 0) {
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

    private void dump_abort(String msg, mpz_t dividend, long divisor)
        throws Exception
    {
        String a_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(dividend, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " dividend=" + a_str + " divisor=" + divisor;
        throw new Exception(emsg);
    }

}
