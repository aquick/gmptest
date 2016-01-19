package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Pow_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Pow_Task";
    
    public Pow_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void refmpz_pow_ui (mpz_t w, mpz_t b, long e)
        throws GMPException
    {
        mpz_t  s;
        mpz_t  t;
        long  i;

        s = new mpz_t();
        t = new mpz_t();

        GMP.mpz_set_ui (t, 1L);
        GMP.mpz_set (s, b);

        if ((e & 1) != 0) {
            GMP.mpz_mul (t, t, s);
        }

        for (i = 2; i <= e; i <<= 1) {
            GMP.mpz_mul (s, s, s);
            if ((i & e) != 0) {
	        GMP.mpz_mul (t, t, s);
            }
        }

        GMP.mpz_set (w, t);
    } 

    private void check_one (mpz_t want, mpz_t base, long exp)
        throws Exception
    {
        mpz_t  got;

        got = new mpz_t();

        GMP.mpz_internal_CHECK_FORMAT (want);

        GMP.mpz_pow_ui (got, base, exp);
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort ("mpz_pow_ui wrong", base, exp, got, want);
            /***
            printf ("mpz_pow_ui wrong\n");
            mpz_trace ("  base", base);
            printf    ("  exp = %lu (0x%lX)\n", exp, exp);
            mpz_trace ("  got ", got);
            mpz_trace ("  want", want);
            abort ();
            ***/
        }

        GMP.mpz_set (got, base);
        GMP.mpz_pow_ui (got, got, exp);
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort ("mpz_pow_ui wrong", base, exp, got, want);
            /***
            printf ("mpz_pow_ui wrong\n");
            mpz_trace ("  base", base);
            printf    ("  exp = %lu (0x%lX)\n", exp, exp);
            mpz_trace ("  got ", got);
            mpz_trace ("  want", want);
            abort ();
            ***/
        }

        if (GMP.mpz_fits_ulong_p (base) != 0) {
            long  base_u = GMP.mpz_get_ui (base);
            GMP.mpz_ui_pow_ui (got, base_u, exp);
            if (GMP.mpz_cmp (got, want) != 0) {
                dump_abort2 ("mpz_pow_ui wrong", base_u, exp, got, want);
                /***
                printf    ("mpz_ui_pow_ui wrong\n");
                printf    ("  base=%lu (0x%lX)\n", base_u, base_u);
                printf    ("  exp = %lu (0x%lX)\n", exp, exp);
                mpz_trace ("  got ", got);
                mpz_trace ("  want", want);
                abort ();
                ***/
            }
        }

    }

    private void check_base (mpz_t base)
        throws Exception
    {
        long  exp;
        mpz_t want;

        want = new mpz_t();
        GMP.mpz_set_ui (want, 1L);

        for (exp = 0; exp < 20; exp++) {
            check_one (want, base, exp);
            GMP.mpz_mul (want, want, base);
        }

    }

    private static final String[] data = new String[] {
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "10",
    "15",
    "16",

    "0x1F",
    "0xFF",
    "0x1001",
    "0xFFFF",
    "0x10000001",
    "0x1000000000000001",

    /* actual size closest to estimate */
    "0xFFFFFFFF",
    "0xFFFFFFFFFFFFFFFF",

    /* same after rshift */
    "0xFFFFFFFF0",
    "0xFFFFFFFF00",
    "0xFFFFFFFFFFFFFFFF0",
    "0xFFFFFFFFFFFFFFFF00",

    /* change from 2 limbs to 1 after rshift */
    "0x180000000",
    "0x18000000000000000",

    /* change from 3 limbs to 2 after rshift */
    "0x18000000100000000",
    "0x180000000000000010000000000000000",

    /* handling of absolute value */
    "-0x80000000",
    "-0x8000000000000000",

    /* low zero limb, and size>2, checking argument overlap detection */
    "0x3000000000000000300000000000000030000000000000000"
    };

    private void check_various ()
        throws Exception
    {
        mpz_t  base;
        int    i;

        base = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (base, data[i], 0);
            check_base (base);
        }
    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t base;
        mpz_t want;
        long  base_size;
        int   i;
        long  size_range;
        long  exp;

        base = new mpz_t();
        want = new mpz_t();

        for (i = 0; i < reps; i++) {
            /* exponentially random 0 to 2^13 bits for base */
            GMP.mpz_urandomb (want, rands, 32);
            size_range = GMP.mpz_get_ui (want) % 12 + 2;
            GMP.mpz_urandomb (want, rands, size_range);
            base_size = GMP.mpz_get_ui (want);
            GMP.mpz_rrandomb (base, rands, base_size);

            /* randomly signed base */
            GMP.mpz_urandomb (want, rands, 2);
            if ((GMP.mpz_get_ui (want) & 1) != 0) {
                GMP.mpz_neg (base, base);
            }

            /* random 5 bits for exponent */
            GMP.mpz_urandomb (want, rands, 5L);
            exp = GMP.mpz_get_ui (want);

            refmpz_pow_ui (want, base, exp);
            check_one (want, base, exp);

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
        int reps = 500; // 5000;
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

            check_various ();
            check_random (reps, rands);
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

    private void dump_abort(String msg, mpz_t base, long exp, mpz_t got, mpz_t want)
        throws Exception
    {
        String base_str = "";
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            base_str = GMP.mpz_get_str(base, 10);
        }
        catch (GMPException e) {
            base_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " base=" + base_str + " exp=" + exp + " got=" + got_str +
                " want=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, long base, long exp, mpz_t got, mpz_t want)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " base=" + base + " exp=" + exp + " got=" + got_str +
                " want=" + want_str;
        throw new Exception(emsg);
    }
}
