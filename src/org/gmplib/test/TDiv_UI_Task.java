package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class TDiv_UI_Task extends TaskBase implements Runnable
{
    private static final String TAG = "TDiv_UI_Task";
    
    public TDiv_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
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
        int reps = 20000; // 200000;
        randstate_t rands;
        mpz_t bs;
        long bsi;
        long size_range;
        long r_rq;
        long r_q;
        long r_r;
        long r;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            dividend = new mpz_t();
            quotient = new mpz_t();
            remainder = new mpz_t();
            quotient2 = new mpz_t();
            remainder2 = new mpz_t();
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
                if ((bsi & 1) != 0) {
                    GMP.mpz_neg (dividend, dividend);
                }

                /* printf ("%ld\n", SIZ (dividend)); */

                r_rq = GMP.mpz_tdiv_qr_ui (quotient, remainder, dividend, divisor);
                r_q = GMP.mpz_tdiv_q_ui (quotient2, dividend, divisor);
                r_r = GMP.mpz_tdiv_r_ui (remainder2, dividend, divisor);
                r = GMP.mpz_tdiv_ui (dividend, divisor);

                /* First determine that the quotients and remainders computed
                 * with different functions are equal.  */
                if (GMP.mpz_cmp (quotient, quotient2) != 0) {
                    dump_abort ("quotients from mpz_tdiv_qr_ui and mpz_tdiv_q_ui differ",
                            dividend, divisor);
                }
                if (GMP.mpz_cmp (remainder, remainder2) != 0) {
                    dump_abort ("remainders from mpz_tdiv_qr_ui and mpz_tdiv_r_ui differ",
                            dividend, divisor);
                }

                /* Check if the sign of the quotient is correct.  */
                if (GMP.mpz_cmp_ui (quotient, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (quotient, 0) < 0)
                            != (GMP.mpz_cmp_ui (dividend, 0) < 0)) {
                        dump_abort ("quotient sign wrong", dividend, divisor);
                    }
                }

                /* Check if the remainder has the same sign as the dividend
                 * (quotient rounded towards 0).  */
                if (GMP.mpz_cmp_ui (remainder, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (remainder, 0) < 0) != (GMP.mpz_cmp_ui (dividend, 0) < 0)) {
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
                    dump_abort ("remainder returned from mpz_tdiv_qr_ui is wrong",
                            dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r_q) != 0) {
                    dump_abort ("remainder returned from mpz_tdiv_q_ui is wrong",
                            dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r_r) != 0) {
                    dump_abort ("remainder returned from mpz_tdiv_r_ui is wrong",
                            dividend, divisor);
                }
                if (GMP.mpz_cmp_ui (remainder, r) != 0) {
                    dump_abort ("remainder returned from mpz_tdiv_ui is wrong",
                            dividend, divisor);
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (i % 1000 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
                }
            }
        }
        catch (GMPException e) {
            this.failmsg = "GMPException [" + e.getCode() + "] " + e.getMessage();
            ret = -1;
        }
        catch (Exception e) {
            this.failmsg = e.getMessage();
            ret = -1;
        }
        onPostExecute(Integer.valueOf(ret));
    }

    private void dump_abort(String msg, mpz_t dividend, long divisor)
        throws Exception
    {
        String dividend_str = "";
        String emsg;
        try {
            dividend_str = GMP.mpz_get_str(dividend, 10);
        }
        catch (GMPException e) {
            dividend_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " dividend=" + dividend_str + " divisor=" + divisor;
        throw new Exception(emsg);
    }
}
