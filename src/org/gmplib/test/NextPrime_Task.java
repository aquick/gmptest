package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class NextPrime_Task extends TaskBase implements Runnable
{
    private static final String TAG = "NextPrime_Task";
    
    public NextPrime_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final short[] diff1 = new short[]
    {
  1,2,2,4,2,4,2,4,6,2,6,4,2,4,6,6,
  2,6,4,2,6,4,6,8,4,2,4,2,4,14,4,6,
  2,10,2,6,6,4,6,6,2,10,2,4,2,12,12,4,
  2,4,6,2,10,6,6,6,2,6,4,2,10,14,4,2,
  4,14,6,10,2,4,6,8,6,6,4,6,8,4,8,10,
  2,10,2,6,4,6,8,4,2,4,12,8,4,8,4,6,
  12,2,18,6,10,6,6,2,6,10,6,6,2,6,6,4,
  2,12,10,2,4,6,6,2,12,4,6,8,10,8,10,8,
  6,6,4,8,6,4,8,4,14,10,12,2,10,2,4,2,
  10,14,4,2,4,14,4,2,4,20,4,8,10,8,4,6,
  6,14,4,6,6,8,6,12,4,6,2,10,2,6,10,2,
  10,2,6,18,4,2,4,6,6,8,6,6,22,2,10,8,
  10,6,6,8,12,4,6,6,2,6,12,10,18,2,4,6,
  2,6,4,2,4,12,2,6,34,6,6,8,18,10,14,4,
  2,4,6,8,4,2,6,12,10,2,4,2,4,6,12,12,
  8,12,6,4,6,8,4,8,4,14,4,6,2,4,6,2,
  6,10,20,6,4,2,24,4,2,10,12,2,10,8,6,6,
  6,18,6,4,2,12,10,12,8,16,14,6,4,2,4,2,
  10,12,6,6,18,2,16,2,22,6,8,6,4,2,4,8,
  6,10,2,10,14,10,6,12,2,4,2,10,12,2,16,2,
  6,4,2,10,8,18,24,4,6,8,16,2,4,8,16,2,
  4,8,6,6,4,12,2,22,6,2,6,4,6,14,6,4,
  2,6,4,6,12,6,6,14,4,6,12,8,6,4,26,18,
  10,8,4,6,2,6,22,12,2,16,8,4,12,14,10,2,
  4,8,6,6,4,2,4,6,8,4,2,6,10,2,10,8,
  4,14,10,12,2,6,4,2,16,14,4,6,8,6,4,18,
  8,10,6,6,8,10,12,14,4,6,6,2,28,2,10,8,
  4,14,4,8,12,6,12,4,6,20,10,2,16,26,4,2,
  12,6,4,12,6,8,4,8,22,2,4,2,12,28,2,6,
  6,6,4,6,2,12,4,12,2,10,2,16,2,16,6,20,
  16,8,4,2,4,2,22,8,12,6,10,2,4,6,2,6,
  10,2,12,10,2,10,14,6,4,6,8,6,6,16,12,2,
  4,14,6,4,8,10,8,6,6,22,6,2,10,14,4,6,
  18,2,10,14,4,2,10,14,4,8,18,4,6,2,4,6,
  2,12,4,20,22,12,2,4,6,6,2,6,22,2,6,16,
  6,12,2,6,12,16,2,4,6,14,4,2,18,24,10,6,
  2,10,2,10,2,10,6,2,10,2,10,6,8,30,10,2,
  10,8,6,10,18,6,12,12,2,18,6,4,6,6,18,2,
  10,14,6,4,2,4,24,2,12,6,16,8,6,6,18,16,
  2,4,6,2,6,6,10,6,12,12,18,2,6,4,18,8,
  24,4,2,4,6,2,12,4,14,30,10,6,12,14,6,10,
  12,2,4,6,8,6,10,2,4,14,6,6,4,6,2,10,
  2,16,12,8,18,4,6,12,2,6,6,6,28,6,14,4,
  8,10,8,12,18,4,2,4,24,12,6,2,16,6,6,14,
  10,14,4,30,6,6,6,8,6,4,2,12,6,4,2,6,
  22,6,2,4,18,2,4,12,2,6,4,26,6,6,4,8,
  10,32,16,2,6,4,2,4,2,10,14,6,4,8,10,6,
  20,4,2,6,30,4,8,10,6,6,8,6,12,4,6,2,
  6,4,6,2,10,2,16,6,20,4,12,14,28,6,20,4,
  18,8,6,4,6,14,6,6,10,2,10,12,8,10,2,10,
  8,12,10,24,2,4,8,6,4,8,18,10,6,6,2,6,
  10,12,2,10,6,6,6,8,6,10,6,2,6,6,6,10,
  8,24,6,22,2,18,4,8,10,30,8,18,4,2,10,6,
  2,6,4,18,8,12,18,16,6,2,12,6,10,2,10,2,
  6,10,14,4,24,2,16,2,10,2,10,20,4,2,4,8,
  16,6,6,2,12,16,8,4,6,30,2,10,2,6,4,6,
  6,8,6,4,12,6,8,12,4,14,12,10,24,6,12,6,
  2,22,8,18,10,6,14,4,2,6,10,8,6,4,6,30,
  14,10,2,12,10,2,16,2,18,24,18,6,16,18,6,2,
  18,4,6,2,10,8,10,6,6,8,4,6,2,10,2,12,
  4,6,6,2,12,4,14,18,4,6,20,4,8,6,4,8,
  4,14,6,4,14,12,4,2,30,4,24,6,6,12,12,14,
  6,4,2,4,18,6,12,8
    };

    private static final short[] diff3 = new short[]
    {
  33,32,136,116,24,22,104,114,76,278,238,162,36,44,388,134,
  130,26,312,42,138,28,24,80,138,108,270,12,330,130,98,102,
  162,34,36,170,90,34,14,6,24,66,154,218,70,132,188,88,
  80,82
    };

    private static final short[] diff4 = new short[]
    {
  91,92,64,6,104,24,46,258,68,18,54,100,68,154,26,4,
  38,142,168,42,18,26,286,104,136,116,40,2,28,110,52,78,
  104,24,54,96,4,626,196,24,56,36,52,102,48,156,26,18,
  42,40
    };

    private static final short[] diff5 = new short[]
    {
  268,120,320,184,396,2,94,108,20,318,274,14,64,122,220,108,
  18,174,6,24,348,32,64,116,268,162,20,156,28,110,52,428,
  196,14,262,30,194,120,300,66,268,12,428,370,212,198,192,130,
  30,80
    };

    private void refmpz_nextprime (mpz_t p, mpz_t t)
        throws GMPException
    {
        GMP.mpz_add_ui (p, t, 1L);
        while (GMP.mpz_probab_prime_p (p, 10) == 0) {
            GMP.mpz_add_ui (p, p, 1L);
        }
    }

    private void run (String start, int reps, String end, short[] diffs)
        throws Exception
    {
        mpz_t x;
        mpz_t y;
        int i;

        x = new mpz_t();
        y = new mpz_t();
        GMP.mpz_set_str (x, start, 0);

        for (i = 0; i < reps; i++) {
            GMP.mpz_nextprime (y, x);
            GMP.mpz_sub (x, y, x);
            if (diffs != null && (long)diffs[i] != GMP.mpz_get_ui (x)) {
                dump_abort ("diff list discrepancy");
            }
            GMP.mpz_set (x, y);
        }

        GMP.mpz_set_str (y, end, 0);

        if (GMP.mpz_cmp (x, y) != 0) {
            dump_abort2("at end of diff list", x, y);
            /***
            gmp_printf ("got  %Zx\n", x);
            gmp_printf ("want %Zx\n", y);
            abort ();
            ***/
        }

    }

    public void run()
    {
        int reps = 20;
        int i;
        mpz_t bs;
        mpz_t x;
        mpz_t nxtp;
        mpz_t ref_nxtp;
        long size_range;
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

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            run ("2", 1000, "0x1ef7", diff1);

            run ("3", 1000 - 1, "0x1ef7", null);

            run ("0x8a43866f5776ccd5b02186e90d28946aeb0ed914", 50,
                 "0x8a43866f5776ccd5b02186e90d28946aeb0eeec5", diff3);

            run ("0x10000000000000000000000000000000000000", 50,
                 "0x100000000000000000000000000000000010ab", diff4);

            run ("0x1c2c26be55317530311facb648ea06b359b969715db83292ab8cf898d8b1b", 50,
                 "0x1c2c26be55317530311facb648ea06b359b969715db83292ab8cf898da957", diff5);

            bs = new mpz_t();
            x = new mpz_t();
            nxtp = new mpz_t();
            ref_nxtp = new mpz_t();

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 8 + 2; /* 0..1024 bit operands */

                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (x, rands, GMP.mpz_get_ui (bs));

                /*      gmp_printf ("%ld: %Zd\n", mpz_sizeinbase (x, 2), x); */

                GMP.mpz_nextprime (nxtp, x);
                refmpz_nextprime (ref_nxtp, x);
                if (GMP.mpz_cmp (nxtp, ref_nxtp) != 0) {
                    dump_abort ("mpz_nextprime differs from refmpz_nextprime");
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

    private void dump_abort(String msg)
        throws Exception
    {
        throw new Exception(msg);
    }

    private void dump_abort2(String msg, mpz_t x, mpz_t y)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " got=" + x_str + " want=" + y_str;
        throw new Exception(emsg);
    }
}
