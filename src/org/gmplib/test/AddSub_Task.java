package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.MutableInteger;
import org.gmplib.gmpjni.GMP.randstate_t;
//import java.io.IOException;

public class AddSub_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "AddSub_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public AddSub_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }


    protected Integer doInBackground(Integer... params)
    {
        mpz_t op1;
        mpz_t op2;
        mpz_t r1;
        mpz_t r2;
        long op1n;
        long op2n;
        int i;
        long op2long;
        mpz_t bs;
        long bsi;
        long size_range;
        int reps = 10000; // 100000;
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
            op1 = new mpz_t();
            op2 = new mpz_t();
            r1 = new mpz_t();
            r2 = new mpz_t();

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 10 + 2; /* 0..2047 bit operands */

                GMP.mpz_urandomb (bs, rands, size_range);
                op1n = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (op1, rands, op1n);

                GMP.mpz_urandomb (bs, rands, size_range);
                op2n = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (op2, rands, op2n);

                GMP.mpz_urandomb (bs, rands, 2);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 1) != 0) GMP.mpz_neg (op1, op1);
                if ((bsi & 2) != 0) GMP.mpz_neg (op2, op2);

                /* printf ("%ld %ld\n", SIZ (multiplier), SIZ (multiplicand)); */

                GMP.mpz_add (r1, op1, op2);
                GMP.mpz_sub (r2, r1, op2);
                if (GMP.mpz_cmp (r2, op1) != 0) {
                    dump_abort (i, "mpz_add or mpz_sub incorrect", op1, op2);
                }

                if (GMP.mpz_fits_ulong_p (op2) != 0) {
                    op2long = GMP.mpz_get_ui (op2);
                    GMP.mpz_add_ui (r1, op1, op2long);
                    GMP.mpz_sub_ui (r2, r1, op2long);
                    if (GMP.mpz_cmp (r2, op1) != 0) {
                        dump_abort (i, "mpz_add_ui or mpz_sub_ui incorrect", op1, op2);
                    }

                    GMP.mpz_ui_sub (r1, op2long, op1);
                    GMP.mpz_sub_ui (r2, op1, op2long);
                    GMP.mpz_neg (r2, r2);
                    if (GMP.mpz_cmp (r1, r2) != 0) {
                        dump_abort (i, "mpz_add_ui or mpz_ui_sub incorrect", op1, op2);
                    }
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

    private void dump_abort(int i, String msg, mpz_t op1, mpz_t op2)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(op1, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(op2, 10);
        }
        catch (GMPException e) {
            b_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " in test " + i + " op1=" + a_str + " op2=" + b_str;
        throw new Exception(emsg);
    }

}
