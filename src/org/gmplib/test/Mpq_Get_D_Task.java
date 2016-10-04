package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.mpf_t;
import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_Get_D_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Get_D_Task";
    
    public Mpq_Get_D_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final long SIZE = 8;
    private static final long EPSIZE = 8;

    private void check_monotonic (randstate_t rands, int reps)
        throws Exception
    {
        mpq_t a;
        mpz_t anum;
        mpz_t aden;
        long size;
        int i;
        int j;
        double last_d;
        double new_d;
        mpq_t qlast_d;
        mpq_t qnew_d;
        mpq_t eps;
        mpz_t epsnum;
        mpz_t epsden;

        /* The idea here is to test the monotonousness of mpq_get_d by adding
           numbers to the numerator and denominator.  */

        a = new mpq_t();
        eps = new mpq_t();
        qlast_d = new mpq_t();
        qnew_d = new mpq_t();
        anum = new mpz_t();
        aden = new mpz_t();
        epsnum = new mpz_t();
        epsden = new mpz_t();

        for (i = 0; i < reps; i++) {
    	    size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
    	    TestUtil.mpz_rrandomb_signed(anum, rands, size);
            do {
        	size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
        	TestUtil.mpz_rrandomb_signed(aden, rands, size);
            }
            while (GMP.mpz_cmp_ui (aden, 0) == 0);

            GMP.mpq_set_num(a, anum);
            GMP.mpq_set_den(a, aden);
            GMP.mpq_canonicalize (a);

            last_d = GMP.mpq_get_d (a);
            GMP.mpq_set_d (qlast_d, last_d);
            for (j = 0; j < 10; j++) {
        	size = (TestUtil.urandom(rands) % EPSIZE + 1)*GMP.GMP_LIMB_BITS();
            	TestUtil.mpz_rrandomb_signed(epsnum, rands, size);
        	size = (TestUtil.urandom(rands) % EPSIZE + 1)*GMP.GMP_LIMB_BITS();
            	TestUtil.mpz_rrandomb_signed(epsden, rands, size);
                GMP.mpq_set_num(eps, epsnum);
                GMP.mpq_set_den(eps, epsden);
                GMP.mpq_canonicalize (eps);

                GMP.mpq_add (a, a, eps);
                GMP.mpq_canonicalize (a);
                new_d = GMP.mpq_get_d (a);
                if (last_d > new_d) {
                    /***
                    printf ("\nERROR (test %d/%d): bad mpq_get_d results\n", i, j);
                    printf ("last: %.16g\n", last_d);
                    printf (" new: %.16g\n", new_d); dump (a);
                    abort ();
                    ***/
                    dump_abort("ERROR (test " + i + "/" + j + "): bad mpq_get_d results", last_d, new_d, a);
                }
                GMP.mpq_set_d (qnew_d, new_d);
                GMP.mpq_internal_CHECK_FORMAT (qnew_d);
                if (GMP.mpq_cmp (qlast_d, qnew_d) > 0) {
                    /***
                    printf ("ERROR (test %d/%d): bad mpq_set_d results\n", i, j);
                    printf ("last: %.16g\n", last_d); dump (qlast_d);
                    printf (" new: %.16g\n", new_d); dump (qnew_d);
                    abort ();
                    ***/
                    dump_abort("ERROR (test " + i + "/" + j + "): bad mpq_get_d results", last_d, new_d, qlast_d, qnew_d);
                }
                last_d = new_d;
                GMP.mpq_set (qlast_d, qnew_d);
            }
        }

    }

    private static final int MAXEXP = 500;

    private static double my_ldexp (double d, int e)
    {
        for (;;) {
            if (e > 0) {
                if (e >= 16) {
                    d *= 65536.0;
                    e -= 16;
                } else {
                    d *= 2.0;
                    e -= 1;
                }
            } else if (e < 0) {
                if (e <= -16) {
                    d /= 65536.0;
                    e += 16;
                } else {
                    d /= 2.0;
                    e += 1;
                }
            } else {
                return d;
            }
        }
    }

    private void check_random (randstate_t rands, int reps)
        throws Exception
    {
        double d;
        mpq_t q;
        mpz_t a;
        mpz_t t;
        mpz_t qnum;
        mpz_t qden;
        int exp;

        int test;

        q = new mpq_t();
        a = new mpz_t();
        t = new mpz_t();
        qnum = new mpz_t();
        qden = new mpz_t();

        for (test = 0; test < reps; test++) {
            GMP.mpz_rrandomb (a, rands, 53);
            GMP.mpz_urandomb (t, rands, 32);
            exp = (int)(GMP.mpz_get_ui (t) % (2*MAXEXP)) - MAXEXP;

            d = my_ldexp (GMP.mpz_get_d (a), exp);
            GMP.mpq_set_d (q, d);
            /* Check that n/d = a * 2^exp, or
               d*a 2^{exp} = n */
            GMP.mpq_get_den(qden, q);
            GMP.mpz_mul (t, a, qden);
            if (exp > 0) {
                GMP.mpz_mul_2exp (t, t, exp);
            } else {
                if (GMP.mpz_divisible_2exp_p (t, -exp) == 0) {
                    dump_abort("(check_random test " + test + "): bad mpq_set_d results", d, q);
                }
                GMP.mpz_fdiv_q_2exp (t, t, -exp);
            }
            GMP.mpq_get_num(qnum, q);
            if (GMP.mpz_cmp (t, qnum) != 0) {
                /***
                printf ("ERROR (check_random test %d): bad mpq_set_d results\n", test);
                printf ("%.16g\n", d);
                gmp_printf ("%Qd\n", q);
                abort ();
                ***/
                dump_abort("(check_random test " + test + "): bad mpq_set_d results", d, q);
            }
        }
    }

    private static long[] data = new long[] {
	    -3*GMP.GMP_NUMB_BITS()-1, -3*GMP.GMP_NUMB_BITS(), -3*GMP.GMP_NUMB_BITS()+1,
	    -2*GMP.GMP_NUMB_BITS()-1, -2*GMP.GMP_NUMB_BITS(), -2*GMP.GMP_NUMB_BITS()+1,
	    -GMP.GMP_NUMB_BITS()-1, -GMP.GMP_NUMB_BITS(), -GMP.GMP_NUMB_BITS()+1,
	    -5, -2, -1, 0, 1, 2, 5,
	    GMP.GMP_NUMB_BITS()-1, GMP.GMP_NUMB_BITS(), GMP.GMP_NUMB_BITS()+1,
	    2*GMP.GMP_NUMB_BITS()-1, 2*GMP.GMP_NUMB_BITS(), 2*GMP.GMP_NUMB_BITS()+1,
	    3*GMP.GMP_NUMB_BITS()-1, 3*GMP.GMP_NUMB_BITS(), 3*GMP.GMP_NUMB_BITS()+1
    };

    /* Check various values 2^n and 1/2^n. */
    private void check_onebit ()
        throws Exception
    {
        int     i;
        int     neg;
        long    exp;
        long    l;
        mpq_t   q;
        double  got;
        double  want;

        q = new mpq_t();

        for (i = 0; i < data.length; i++) {
            exp = data[i];

            GMP.mpq_set_ui (q, 1L, 1L);
            if (exp >= 0) {
                GMP.mpq_mul_2exp (q, q, exp);
            } else {
                GMP.mpq_div_2exp (q, q, -exp);
            }

            want = 1.0;
            for (l = 0; l < exp; l++) {
                want *= 2.0;
            }
            for (l = 0; l > exp; l--) {
                want /= 2.0;
            }

            for (neg = 0; neg <= 1; neg++) {
                if (neg != 0) {
                    GMP.mpq_neg (q, q);
                    want = -want;
                }

                got = GMP.mpq_get_d (q);

                if (got != want) {
                    /***
                    printf    ("mpq_get_d wrong on %s2**%ld\n", neg ? "-" : "", exp);
                    mpq_trace ("   q    ", q);
                    d_trace   ("   want ", want);
                    d_trace   ("   got  ", got);
                    abort();
                    ***/
                    dump_abort("mpq_get_d wrong on 2**" + (neg != 0 ? "-" : "") + exp, q, got, want);
                }
            }
        }
    }

    public void run()
    {
        int reps = 10000; // 100000;
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
            check_onebit ();
            check_monotonic (rands, 100);
            check_random (rands, reps);
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

    private void dump_abort(String msg, double d, mpq_t want)
        throws Exception
    {
        String want_str = "";
        String emsg;
        try {
            want_str = GMP.mpq_get_str(want, 16);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " d=" + d + " q=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, mpq_t q, double got, double want)
        throws Exception
    {
        String want_str = "";
        String emsg;
        try {
            want_str = GMP.mpq_get_str(q, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " q=" + want_str + " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, double last_d, double new_d, mpq_t want)
        throws Exception
    {
        String want_str = "";
        String emsg;
        try {
            want_str = GMP.mpq_get_str(want, 16);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " last=" + last_d + " new=" + new_d + " q=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, double last_d, double new_d, mpq_t qlast_d, mpq_t qnew_d)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            want_str = GMP.mpq_get_str(qlast_d, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpq_get_str(qnew_d, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " last=" + last_d + " new=" + new_d + " qlast=" + want_str + " qnew=" + got_str;
        throw new Exception(emsg);
    }

}
