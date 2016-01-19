package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class PerfSqr_Task extends TaskBase implements Runnable
{
    private static final String TAG = "PerfSqr_Task";
    
    public PerfSqr_Task(UI ui)
    {
        super(ui, TAG);
    }

    /* check_modulo() exercises mpz_perfect_square_p on squares which cover each
       possible quadratic residue to each divisor used within
       mpn_perfect_square_p, ensuring those residues aren't incorrectly claimed
       to be non-residues.

       Each divisor is taken separately.  It's arranged that n is congruent to 0
       modulo the other divisors, 0 of course being a quadratic residue to any
       modulus.

       The values "(j*others)^2" cover all quadratic residues mod divisor[i],
       but in no particular order.  j is run from 1<=j<=divisor[i] so that zero
       is excluded.  A literal n==0 doesn't reach the residue tests.  */

    private static final long[]  divisor = new long[] { 256, 45, 17, 13, 7 };

    private void check_modulo ()
        throws Exception
    {
        int i;
        int j;

        mpz_t  alldiv;
        mpz_t  others;
        mpz_t  n;

        alldiv = new mpz_t();
        others = new mpz_t();
        n = new mpz_t();

        /* product of all divisors */
        GMP.mpz_set_ui (alldiv, 1L);
        for (i = 0; i < divisor.length; i++) {
            GMP.mpz_mul_ui (alldiv, alldiv, divisor[i]);
        }

        for (i = 0; i < divisor.length; i++) {
            /* product of all divisors except i */
            GMP.mpz_set_ui (others, 1L);
            for (j = 0; j < divisor.length; j++) {
                if (i != j) {
                    GMP.mpz_mul_ui (others, others, divisor[j]);
                }
            }

            for (j = 1; j <= divisor[i]; j++) {
                /* square */
                GMP.mpz_mul_ui (n, others, j);
                GMP.mpz_mul (n, n, n);
                if (GMP.mpz_perfect_square_p (n) == 0) {
                    dump_abort ("mpz_perfect_square_p got 0, want 1");
                    /***
                    printf ("mpz_perfect_square_p got 0, want 1\n");
                    mpz_trace ("  n", n);
                    abort ();
                    ***/
                }
            }
        }

    }


    /* Exercise mpz_perfect_square_p compared to what mpz_sqrt says. */
    private void check_sqrt (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t x2;
        mpz_t x2t;
        mpz_t x;
        long x2n;
        int res;
        int i;
        /* int cnt = 0; */
        mpz_t bs;

        bs = new mpz_t();

        x2 = new mpz_t();
        x = new mpz_t();
        x2t = new mpz_t();

        for (i = 0; i < reps; i++) {
            GMP.mpz_urandomb (bs, rands, 9);
            x2n = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (x2, rands, x2n);
            /* mpz_out_str (stdout, -16, x2); puts (""); */

            res = GMP.mpz_perfect_square_p (x2);
            GMP.mpz_sqrt (x, x2);
            GMP.mpz_mul (x2t, x, x);

            if (res != (GMP.mpz_cmp (x2, x2t) == 0 ? 1 : 0)) {
                dump_abort2("mpz_perfect_square_p and mpz_sqrt differ",
                    x, x2, x2t, res, (GMP.mpz_cmp(x2, x2t) == 0 ? 1 : 0));
                /***
                printf    ("mpz_perfect_square_p and mpz_sqrt differ\n");
                mpz_trace ("   x  ", x);
                mpz_trace ("   x2 ", x2);
                mpz_trace ("   x2t", x2t);
                printf    ("   mpz_perfect_square_p %d\n", res);
                printf    ("   mpz_sqrt             %d\n", mpz_cmp (x2, x2t) == 0);
                abort ();
                ***/
            }

            /* cnt += res != 0; */
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 1000 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }
        /* printf ("%d/%d perfect squares\n", cnt, reps); */

    }

    public void run()
    {
        int reps = 20000; // 200000;
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

            check_modulo();
            check_sqrt(reps, rands);
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

    private void dump_abort2(String msg, mpz_t x, mpz_t x2, mpz_t x2t, int r1, int r2)
        throws Exception
    {
        String x_str = "";
        String x2_str = "";
        String x2t_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x2_str = GMP.mpz_get_str(x2, 10);
        }
        catch (GMPException e) {
            x2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x2t_str = GMP.mpz_get_str(x2t, 10);
        }
        catch (GMPException e) {
            x2t_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x=" + x_str + " x2=" + x2_str + " x2t=" + x2t_str +
                " mpz_perfect_square_p=" + r1 + " mpz_sqrt=" + r2;
        throw new Exception(emsg);
    }
}
