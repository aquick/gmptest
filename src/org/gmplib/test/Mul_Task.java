package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

import java.math.BigInteger;

public class Mul_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Mul_Task";
    
    public Mul_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final long FFT_MIN_BITSIZE = 100000;

    private static void refmpz_mul (mpz_t product, mpz_t multiplier, mpz_t multiplicand)
        throws Exception
    {
        String mr_str = GMP.mpz_get_str(multiplier, 16);
        BigInteger mr = new BigInteger(mr_str, 16);
        String md_str = GMP.mpz_get_str(multiplicand, 16);
        BigInteger md = new BigInteger(md_str, 16);
        BigInteger p = md.multiply(mr);
        GMP.mpz_set_str(product, p.toString(16), 16);
    }

    private void one (int i, mpz_t multiplicand, mpz_t multiplier)
        throws Exception
    {
        mpz_t product;
        mpz_t ref_product;

        product = new mpz_t();
        ref_product = new mpz_t();

        /* Test plain multiplication comparing results against reference code.  */
        GMP.mpz_mul (product, multiplier, multiplicand);
        refmpz_mul (ref_product, multiplier, multiplicand);
        if (GMP.mpz_cmp (product, ref_product) != 0) {
            dump_abort (i, "incorrect plain product",
                        multiplier, multiplicand, product, ref_product);
        }

        /* Test squaring, comparing results against plain multiplication  */
        GMP.mpz_mul (product, multiplier, multiplier);
        GMP.mpz_set (multiplicand, multiplier);
        GMP.mpz_mul (ref_product, multiplier, multiplicand);
        if (GMP.mpz_cmp (product, ref_product) != 0) {
            dump_abort (i, "incorrect square product",
                        multiplier, multiplier, product, ref_product);
        }

    }

    public void run()
    {
        int reps = 20;
        int i;
        mpz_t bs;
        mpz_t op1;
        mpz_t op2;
        long size_range;
        long fsize_range;
        long bsi;
        randstate_t rands;
        int fft_max_2exp = 0;
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

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            fft_max_2exp = 22; /* default limit, good for any machine */

            bs = new mpz_t();
            op1 = new mpz_t();
            op2 = new mpz_t();

            fsize_range = 4 << 8; /* a fraction 1/256 of size_range */
            for (i = 0;; i++) {
                size_range = fsize_range >> 8;
                fsize_range = fsize_range * 33 / 32;
                //Log.d(TAG, "i=" + i);
                //Log.d(TAG, "fsize_range=" + fsize_range);
                //Log.d(TAG, "size_range=" + size_range);

                if (size_range > fft_max_2exp) break;

                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (op1, rands, GMP.mpz_get_ui (bs));
                if ((i & 1) != 0) {
                    GMP.mpz_urandomb (bs, rands, size_range);
                }
                GMP.mpz_rrandomb (op2, rands, GMP.mpz_get_ui (bs));

                GMP.mpz_urandomb (bs, rands, 4);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 0x3) == 0) {
                    GMP.mpz_neg (op1, op1);
                }
                if ((bsi & 0xC) == 0) {
                    GMP.mpz_neg (op2, op2);
                }

                /* printf ("%d %d\n", SIZ (op1), SIZ (op2)); */
                one (i, op2, op1);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(Integer.valueOf((int)((float)(size_range)*50.0/(float)fft_max_2exp)));
            }

            for (i = -50; i < 0; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = (long)(GMP.mpz_get_ui (bs) % fft_max_2exp);
                //Log.d(TAG, "----> i=" + i);

                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (op1, rands, GMP.mpz_get_ui (bs) + FFT_MIN_BITSIZE);
                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (op2, rands, GMP.mpz_get_ui (bs) + FFT_MIN_BITSIZE);

                /* printf ("%d: %d %d\n", i, SIZ (op1), SIZ (op2)); */
                one (i, op2, op1);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(101 + i);
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
        Log.d(TAG, "done");
        onPostExecute(Integer.valueOf(ret));
    }

    private void dump_abort (int i, String msg,
            mpz_t op1, mpz_t op2, mpz_t product, mpz_t ref_product)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String p_str = "";
        String rp_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(op1, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(op2, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            p_str = GMP.mpz_get_str(product, 10);
        }
        catch (GMPException e) {
            p_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            rp_str = GMP.mpz_get_str(ref_product, 10);
        }
        catch (GMPException e) {
            rp_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " in test " + i + " op1=" + x_str + " op2=" + y_str +
                   " product=" + p_str + " ref_product=" + rp_str;
        throw new Exception(emsg);
    }
}
