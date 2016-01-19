package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Invert_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Invert_Task";
    
    public Invert_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
        mpz_t a;
        mpz_t m;
        mpz_t ainv;
        mpz_t t;
        int test;
        int r;
        int reps = 100; // 1000;
        randstate_t rands;
        mpz_t bs;
        long bsi;
        long size_range;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            a = new mpz_t();
            m = new mpz_t();
            ainv = new mpz_t();
            t = new mpz_t();
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

            for (test = 0; test < reps; test++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 16 + 2;

                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (a, rands, GMP.mpz_get_ui (bs));
                do {
                    GMP.mpz_urandomb (bs, rands, size_range);
                    GMP.mpz_rrandomb (m, rands, GMP.mpz_get_ui (bs));
                } while (GMP.mpz_sgn (m) == 0);

                GMP.mpz_urandomb (bs, rands, 8);
                bsi = GMP.mpz_get_ui (bs);

                if ((bsi & 1) != 0) GMP.mpz_neg (a, a);
                if ((bsi & 2) != 0) GMP.mpz_neg (m, m);

                r = GMP.mpz_invert (ainv, a, m);
                if (r != 0) {
                    GMP.mpz_internal_CHECK_FORMAT (ainv);

                    if (GMP.mpz_cmp_ui (ainv, 0) < 0 || GMP.mpz_cmpabs (ainv, m) >= 0) {
                        dump_abort2 ("ERROR in test " + test + ": Inverse out of range.", a, ainv, m);
                        /***
                        fprintf (stderr, "ERROR in test %d\n", test);
                        gmp_fprintf (stderr, "Inverse out of range.\n");
                        gmp_fprintf (stderr, "a = %Zx\n", a);
                        gmp_fprintf (stderr, "m = %Zx\n", m);
                        abort ();
                        ***/
                    }

                    GMP.mpz_mul (t, ainv, a);
                    GMP.mpz_mod (t, t, m);

                    if (GMP.mpz_cmp_ui (t, (GMP.mpz_cmpabs_ui(m, 1L) != 0 ? 1 : 0)) != 0) {
                        dump_abort ("ERROR in test " + test + ": a^(-1)*a != 1 (mod m)", a, m);
                        /***
                        fprintf (stderr, "ERROR in test %d\n", test);
                        gmp_fprintf (stderr, "a^(-1)*a != 1 (mod m)\n");
                        gmp_fprintf (stderr, "a = %Zx\n", a);
                        gmp_fprintf (stderr, "m = %Zx\n", m);
                        abort ();
                        ***/
                    }
                } else /* Inverse does not exist */ {
                    if (GMP.mpz_cmpabs_ui (m, 1) <= 0) continue; /* OK */

                    GMP.mpz_gcd (t, a, m);
                    if (GMP.mpz_cmp_ui (t, 1) == 0) {
                        dump_abort ("ERROR in test " + test + ": Inverse exists, but was not found.", a, m);
                        /***
                        fprintf (stderr, "ERROR in test %d\n", test);
                        gmp_fprintf (stderr, "Inverse exists, but was not found.\n");
                        gmp_fprintf (stderr, "a = %Zx\n", a);
                        gmp_fprintf (stderr, "m = %Zx\n", m);
                        abort ();
                        ***/
                    }
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (test % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(test+1)*100.0/(float)reps)));
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

    private void dump_abort(String msg, mpz_t a, mpz_t m)
        throws Exception
    {
        String a_str = "";
        String m_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            m_str = GMP.mpz_get_str(m, 10);
        }
        catch (GMPException e) {
            m_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " a=" + a_str + " m=" + m_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t a, mpz_t ainv, mpz_t m)
        throws Exception
    {
        String a_str = "";
        String ainv_str = "";
        String m_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            ainv_str = GMP.mpz_get_str(ainv, 10);
        }
        catch (GMPException e) {
            ainv_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            m_str = GMP.mpz_get_str(m, 10);
        }
        catch (GMPException e) {
            m_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " a=" + a_str + " 1/a=" + ainv_str + " m=" + m_str;
        throw new Exception(emsg);
    }
}
