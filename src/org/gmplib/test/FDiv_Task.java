package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class FDiv_Task extends TaskBase implements Runnable {

    private static final String TAG = "FDiv_Task";
    
    public FDiv_Task(UI ui)
    {
        super(ui, TAG);
    }


    public void run()
    {
        mpz_t dividend;
        mpz_t divisor;
        mpz_t quotient;
        mpz_t remainder;
        mpz_t quotient2;
        mpz_t remainder2;
        mpz_t temp;
        long dividend_size;
        long divisor_size;
        int i;
        mpz_t bs;
        long bsi;
        long size_range;
        int reps = 100; // 1000;
        randstate_t rands;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            seed = uinterface.getSeed();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            String s = "seed=" + seed;
            Log.d(TAG, s);
            uinterface.display(s);
            rands = new randstate_t(seed);

            bs = new mpz_t();
            dividend = new mpz_t();
            divisor = new mpz_t();
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
                size_range = GMP.mpz_get_ui (bs) % 16 + 2; /* 0..131071 bit operands */

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
                if ((bsi & 1) != 0) GMP.mpz_neg (dividend, dividend);
                if ((bsi & 2) != 0) GMP.mpz_neg (divisor, divisor);

                /* printf ("%ld %ld\n", SIZ (dividend), SIZ (divisor)); */

                GMP.mpz_fdiv_qr (quotient, remainder, dividend, divisor);
                GMP.mpz_fdiv_q (quotient2, dividend, divisor);
                GMP.mpz_fdiv_r (remainder2, dividend, divisor);

                /* First determine that the quotients and remainders computed
                   with different functions are equal.  */
                if (GMP.mpz_cmp (quotient, quotient2) != 0) {
                    dump_abort ("quotients from mpz_fdiv_qr and mpz_fdiv_q differ", dividend, divisor);
                }
                if (GMP.mpz_cmp (remainder, remainder2) != 0) {
                    dump_abort ("remainders from mpz_fdiv_qr and mpz_fdiv_r differ", dividend, divisor);
                }

                /* Check if the sign of the quotient is correct.  */
                if (GMP.mpz_cmp_ui (quotient, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (quotient, 0) < 0)
                         != ((GMP.mpz_cmp_ui (dividend, 0) ^ GMP.mpz_cmp_ui (divisor, 0)) < 0)) {
                        dump_abort ("quotient sign wrong", dividend, divisor);
                    }
                }

                /* Check if the remainder has the same sign as the divisor
                   (quotient rounded towards minus infinity).  */
                if (GMP.mpz_cmp_ui (remainder, 0) != 0) {
                    if ((GMP.mpz_cmp_ui (remainder, 0) < 0) != (GMP.mpz_cmp_ui (divisor, 0) < 0)) {
                        dump_abort ("remainder sign wrong", dividend, divisor);
                    }
                }

                GMP.mpz_mul (temp, quotient, divisor);
                GMP.mpz_add (temp, temp, remainder);
                if (GMP.mpz_cmp (temp, dividend) != 0) {
                    dump_abort ("n mod d != n - [n/d]*d", dividend, divisor);
                }

                GMP.mpz_abs (temp, divisor);
                GMP.mpz_abs (remainder, remainder);
                if (GMP.mpz_cmp (remainder, temp) >= 0) {
                    dump_abort ("remainder greater than divisor", dividend, divisor);
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

    private void dump_abort(String msg, mpz_t dividend, mpz_t divisor)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(dividend, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(divisor, 10);
        }
        catch (GMPException e) {
            b_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " dividend=" + a_str + " divisor=" + b_str;
        throw new Exception(emsg);
    }

}
