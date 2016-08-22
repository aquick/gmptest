package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class Div_2Exp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Div_2Exp_Task";
    
    public Div_2Exp_Task(UI ui)
    {
        super(ui, TAG);
    }


    /* If the remainder is in the correct range and q*d+r is correct, then q
       must have rounded correctly.  */

    private void check_one (mpz_t a, long d)
        throws Exception
    {
        mpz_t  q;
        mpz_t  r;
        mpz_t  p;
        mpz_t  d2exp;
        int    inplace;

        d2exp = new mpz_t();
        q = new mpz_t();
        r = new mpz_t();
        p = new mpz_t();

        GMP.mpz_set_ui (d2exp, 1L);
        GMP.mpz_mul_2exp (d2exp, d2exp, d);

        for (inplace = 0; inplace <= 1; inplace++) {

            //INPLACE (mpz_fdiv_q_2exp, q, a, d);
            //INPLACE (mpz_fdiv_r_2exp, r, a, d);
            if (inplace != 0) {
                GMP.mpz_set (q, a);
                GMP.mpz_fdiv_q_2exp (q, q, d);
            } else {
                GMP.mpz_fdiv_q_2exp (q, a, d);
            }
            if (inplace != 0) {
                GMP.mpz_set (r, a);
                GMP.mpz_fdiv_r_2exp (r, r, d);
            } else {
                GMP.mpz_fdiv_r_2exp (r, a, d);
            }

            GMP.mpz_mul_2exp (p, q, d);
            GMP.mpz_add (p, p, r);
            if (GMP.mpz_sgn (r) < 0 || GMP.mpz_cmp (r, d2exp) >= 0) {
                dump_abort("mpz_fdiv_r_2exp result out of range", a, d, q, r, p);
                /***
                printf ("mpz_fdiv_r_2exp result out of range\n");
                goto error;
                ***/
            }
            if (GMP.mpz_cmp (p, a) != 0) {
                dump_abort("mpz_fdiv_[qr]_2exp doesn't multiply back", a, d, q, r, p);
                /***
                printf ("mpz_fdiv_[qr]_2exp doesn't multiply back\n");
                goto error;
                ***/
            }

            //INPLACE (mpz_cdiv_q_2exp, q, a, d);
            //INPLACE (mpz_cdiv_r_2exp, r, a, d);
            if (inplace != 0) {
                GMP.mpz_set (q, a);
                GMP.mpz_cdiv_q_2exp (q, q, d);
            } else {
                GMP.mpz_cdiv_q_2exp (q, a, d);
            }
            if (inplace != 0) {
                GMP.mpz_set (r, a);
                GMP.mpz_cdiv_r_2exp (r, r, d);
            } else {
                GMP.mpz_cdiv_r_2exp (r, a, d);
            }

            GMP.mpz_mul_2exp (p, q, d);
            GMP.mpz_add (p, p, r);
            if (GMP.mpz_sgn (r) > 0 || GMP.mpz_cmpabs (r, d2exp) >= 0) {
                dump_abort("mpz_cdiv_r_2exp result out of range", a, d, q, r, p);
                /***
                printf ("mpz_cdiv_r_2exp result out of range\n");
                goto error;
                ***/
            }
            if (GMP.mpz_cmp (p, a) != 0) {
                dump_abort("mpz_cdiv_[qr]_2exp doesn't multiply back", a, d, q, r, p);
                /***
                printf ("mpz_cdiv_[qr]_2exp doesn't multiply back\n");
                goto error;
                ***/
            }

            //INPLACE (mpz_tdiv_q_2exp, q, a, d);
            //INPLACE (mpz_tdiv_r_2exp, r, a, d);
            if (inplace != 0) {
                GMP.mpz_set (q, a);
                GMP.mpz_tdiv_q_2exp (q, q, d);
            } else {
                GMP.mpz_tdiv_q_2exp (q, a, d);
            }
            if (inplace != 0) {
                GMP.mpz_set (r, a);
                GMP.mpz_tdiv_r_2exp (r, r, d);
            } else {
                GMP.mpz_tdiv_r_2exp (r, a, d);
            }

            GMP.mpz_mul_2exp (p, q, d);
            GMP.mpz_add (p, p, r);
            if (GMP.mpz_sgn (r) != 0 && GMP.mpz_sgn (r) != GMP.mpz_sgn (a)) {
                dump_abort("mpz_tdiv_r_2exp result wrong sign", a, d, q, r, p);
                /***
                printf ("mpz_tdiv_r_2exp result wrong sign\n");
                goto error;
                ***/
            }
            if (GMP.mpz_cmpabs (r, d2exp) >= 0) {
                dump_abort("mpz_tdiv_r_2exp result out of range", a, d, q, r, p);
                /***
                printf ("mpz_tdiv_r_2exp result out of range\n");
                goto error;
                ***/
            }
            if (GMP.mpz_cmp (p, a) != 0) {
                dump_abort("mpz_tdiv_[qr]_2exp doesn't multiply back", a, d, q, r, p);
                /***
                printf ("mpz_tdiv_[qr]_2exp doesn't multiply back\n");
                goto error;
                ***/
            }
        }
    }

    private void check_all (mpz_t a, long d)
        throws Exception
    {
        check_one (a, d);
        GMP.mpz_neg (a, a);
        check_one (a, d);
    }

    private static final long[]  table = new long[] {
        0, 1, 2, 3, 4, 5,
        GMP.GMP_NUMB_BITS()-1, GMP.GMP_NUMB_BITS(), GMP.GMP_NUMB_BITS()+1,
        2*GMP.GMP_NUMB_BITS()-1, 2*GMP.GMP_NUMB_BITS(), 2*GMP.GMP_NUMB_BITS()+1,
        3*GMP.GMP_NUMB_BITS()-1, 3*GMP.GMP_NUMB_BITS(), 3*GMP.GMP_NUMB_BITS()+1,
        4*GMP.GMP_NUMB_BITS()-1, 4*GMP.GMP_NUMB_BITS(), 4*GMP.GMP_NUMB_BITS()+1
    };

    private void check_various ()
        throws Exception
    {
        int   i;
        int   j;
        long  n;
        long  d;
        mpz_t a;

        a = new mpz_t();

        /* a==0, and various d */
        GMP.mpz_set_ui (a, 0L);
        for (i = 0; i < table.length; i++) {
            check_one (a, table[i]);
        }

        /* a==2^n, and various d */
        for (i = 0; i < table.length; i++) {
            n = table[i];
            GMP.mpz_set_ui (a, 1L);
            GMP.mpz_mul_2exp (a, a, n);

            for (j = 0; j < table.length; j++) {
                d = table[j];
                check_all (a, d);
            }
        }
    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t          a;
        long           d;
        int            i;

        a = new mpz_t();

        for (i = 0; i < reps; i++) {
            /* exponentially within 2 to 257 bits */
            TestUtil.mpz_erandomb (a, rands, TestUtil.urandom (rands) % 8 + 2);

            d = TestUtil.urandom (rands) % 256;

            check_all (a, d);
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
        int reps = 100;
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

    private void dump_abort(String msg, mpz_t a, long d, mpz_t q, mpz_t r, mpz_t p)
        throws Exception
    {
        String a_str = "";
        String q_str = "";
        String r_str = "";
        String p_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            q_str = GMP.mpz_get_str(q, 10);
        }
        catch (GMPException e) {
            q_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            r_str = GMP.mpz_get_str(r, 10);
        }
        catch (GMPException e) {
            r_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            p_str = GMP.mpz_get_str(p, 10);
        }
        catch (GMPException e) {
            p_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " a=" + a_str + " d=" + d + " q=" + q_str + " r=" + r_str + " p=" + p_str;
        throw new Exception(emsg);
    }

}
