package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class HamDist_Task extends TaskBase implements Runnable
{
    private static final String TAG = "HamDist_Task";
    
    public HamDist_Task(UI ui)
    {
        super(ui, TAG);
    }

    private long refmpz_hamdist(mpz_t x, mpz_t y)
        throws GMPException
    {
        if ((GMP.mpz_sgn(x) < 0 && GMP.mpz_sgn(y) >= 0) ||
            (GMP.mpz_sgn(y) < 0 && GMP.mpz_sgn(x) >= 0)) {
            return GMP.ULONG_MAX;
        }
        mpz_t xc = new mpz_t();
        mpz_t yc = new mpz_t();
        mpz_t z = new mpz_t();
        GMP.mpz_set(xc, x);
        GMP.mpz_set(yc, y);
        if (GMP.mpz_sgn(x) < 0) {
            GMP.mpz_com(xc, x);
            GMP.mpz_com(yc, y);
        }
        GMP.mpz_xor(z, xc, yc);
        return refmpz_popcount(z);
    }

    private long refmpz_popcount (mpz_t arg)
        throws GMPException
    {
        long n;
        int  i;
        long cnt;
        long x;

        n = GMP.mpz_internal_SIZ(arg);
        if (n < 0) return GMP.ULONG_MAX;

        cnt = 0;
        for (i = 0; i < (int)n; i++) {
            x = GMP.mpz_internal_get_ulimb(arg, i);
            while (x != 0) {
                if ((x & 1) != 0) {
                    cnt += 1;
                }
                x >>= 1;
            }
        }
        return cnt;
    }

    private void check_twobits ()
        throws Exception
    {
        long  i;
        long  j;
        long  got;
        long  want;
        mpz_t x = new mpz_t();
        mpz_t y = new mpz_t();

        for (i = 0; i < 5 * GMP.GMP_NUMB_BITS(); i++) {
            for (j = 0; j < 5 * GMP.GMP_NUMB_BITS(); j++) {
                GMP.mpz_set_ui (x, 0L);
                GMP.mpz_setbit (x, i);
                GMP.mpz_set_ui (y, 0L);
                GMP.mpz_setbit (y, j);

                want = 2 * ((i != j) ? 1 : 0);
                got = GMP.mpz_hamdist (x, y);
                if (got != want) {
                    dump_abort("mpz_hamdist wrong on 2 bits pos/pos", x, y, i, j, got, want);
                    /***
                    printf    ("mpz_hamdist wrong on 2 bits pos/pos\n");
                    wrong:
                    printf    ("  i    %lu\n", i);
                    printf    ("  j    %lu\n", j);
                    printf    ("  got  %lu\n", got);
                    printf    ("  want %lu\n", want);
                    mpz_trace ("  x   ", x);
                    mpz_trace ("  y   ", y);
                    abort();
                    ***/
                }

                GMP.mpz_neg (x, x);
                GMP.mpz_neg (y, y);
                want = Math.abs ((long) (i-j));
                got = GMP.mpz_hamdist (x, y);
                if (got != want) {
                    dump_abort("mpz_hamdist wrong on 2 bits neg/neg", x, y, i, j, got, want);
                    /***
                    printf    ("mpz_hamdist wrong on 2 bits neg/neg\n");
                    goto wrong;
                    ***/
                }
            }

        }
    }


    private void check_rand (randstate_t rands, int reps)
        throws Exception
    {
        long  got;
        long  want;
        int   i;
        mpz_t x = new mpz_t();
        mpz_t y = new mpz_t();

        for (i = 0; i < reps; i++) {
            TestUtil.mpz_erandomb (x, rands, 6 * GMP.GMP_NUMB_BITS());
            TestUtil.mpz_negrandom (x, rands);
            GMP.mpz_mul_2exp (x, x, TestUtil.urandom(rands) % (4 * GMP.GMP_NUMB_BITS()));

            TestUtil.mpz_erandomb (y, rands, 6 * GMP.GMP_NUMB_BITS());
            TestUtil.mpz_negrandom (y, rands);
            GMP.mpz_mul_2exp (y, y, TestUtil.urandom(rands) % (4 * GMP.GMP_NUMB_BITS()));

            want = refmpz_hamdist (x, y);
            got = GMP.mpz_hamdist (x, y);
            if (got != want) {
                dump_abort2("mpz_hamdist wrong on random", x, y, got, want);
                /***
                printf    ("mpz_hamdist wrong on random\n");
                printf    ("  got  %lu\n", got);
                printf    ("  want %lu\n", want);
                mpz_trace ("  x   ", x);
                mpz_trace ("  y   ", y);
                abort();
                ***/
            }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 10 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }
    }

    public void run()
    {
        int reps = 200; // 2000;
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

            check_twobits ();
            check_rand (rands, reps);
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

    private void dump_abort(String msg, mpz_t x, mpz_t y, long i, long j, long got, long want)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 16);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 16);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x=" + x_str + " y=" + y_str +
                   " i=" + i + " j=" + j + " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t x, mpz_t y, long got, long want)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 16);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 16);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x=" + x_str + " y=" + y_str + " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }
}
