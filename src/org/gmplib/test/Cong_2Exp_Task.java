package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class Cong_2Exp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Cong_2Exp_Task";
    
    public Cong_2Exp_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_one (mpz_t a, mpz_t c, long d, int want)
        throws Exception
    {
        mpz_t  diff;
        mpz_t  d2exp;
        mpz_t  temp;
        int    got;
        int    swap;

        for (swap = 0; swap <= 1; swap++) {
            got = (GMP.mpz_congruent_2exp_p (a, c, d) != 0 ? 1 : 0);
            if (want != got) {
                diff = new mpz_t();
                d2exp = new mpz_t();

                GMP.mpz_sub (diff, a, c);
                GMP.mpz_set_ui (d2exp, 1L);
                GMP.mpz_mul_2exp (d2exp, d2exp, d);

                dump_abort ("mpz_congruent_2exp_p wrong", a, c, diff, d2exp, d);
                /***
                printf ("mpz_congruent_2exp_p wrong\n");
                printf ("   expected %d got %d\n", want, got);
                mpz_trace ("   a", a);
                mpz_trace ("   c", c);
                mpz_trace (" a-c", diff);
                mpz_trace (" 2^d", d2exp);
                printf    ("   d=%lu\n", d);

                mp_trace_base = -16;
                mpz_trace ("   a", a);
                mpz_trace ("   c", c);
                mpz_trace (" a-c", diff);
                mpz_trace (" 2^d", d2exp);
                printf    ("   d=0x%lX\n", d);
                abort ();
                ***/
            }

            //MPZ_SRCPTR_SWAP (a, c);
            temp = a;
            a = c;
            c = temp;
        }
    }

    private static class CheckData
    {
        public String a;
        public String c;
        public long   d;
        public int    want;

        public CheckData(String a, String c, long d, int want)
        {
            this.a = a;
            this.c = c;
            this.d = d;
            this.want = want;
        }
    }

    private static final CheckData[] data = {

    /* anything is congruent mod 1 */
    new CheckData( "0", "0", 0, 1 ),
    new CheckData( "1", "0", 0, 1 ),
    new CheckData( "0", "1", 0, 1 ),
    new CheckData( "123", "-456", 0, 1 ),
    new CheckData( "0x123456789123456789", "0x987654321987654321", 0, 1 ),
    new CheckData( "0xfffffffffffffffffffffffffffffff7", "-0x9", 129, 0 ),
    new CheckData( "0xfffffffffffffffffffffffffffffff6", "-0xa", 128, 1 )
    };

    private void check_data()
        throws Exception
    {
        mpz_t   a;
        mpz_t   c;
        int     i;

        a = new mpz_t();
        c = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (a, data[i].a, 0);
            GMP.mpz_set_str (c, data[i].c, 0);
            check_one (a, c, data[i].d, data[i].want);
        }
    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        long  d;
        mpz_t  a;
        mpz_t  c;
        mpz_t  ra;
        mpz_t  rc;
        int    i;

        a = new mpz_t();
        c = new mpz_t();
        ra = new mpz_t();
        rc = new mpz_t();

        for (i = 0; i < reps; i++) {
            TestUtil.mpz_errandomb (a, rands, 8*GMP.GMP_LIMB_BITS());
            TestUtil.mpz_errandomb (c, rands, 8*GMP.GMP_LIMB_BITS());
            d = TestUtil.urandom(rands) % (8*GMP.GMP_LIMB_BITS());

            GMP.mpz_mul_2exp (a, a, TestUtil.urandom(rands) % (2*GMP.GMP_LIMB_BITS()));
            GMP.mpz_mul_2exp (c, c, TestUtil.urandom(rands) % (2*GMP.GMP_LIMB_BITS()));

            TestUtil.mpz_negrandom (a, rands);
            TestUtil.mpz_negrandom (c, rands);

            GMP.mpz_fdiv_r_2exp (ra, a, d);
            GMP.mpz_fdiv_r_2exp (rc, c, d);

            GMP.mpz_sub (ra, ra, rc);
            if (GMP.mpz_cmp_ui (ra, 0) != 0) {
                check_one (a, c, d, 0);
                GMP.mpz_sub (a, a, ra);
            }
            check_one (a, c, d, 1);
            if (d != 0) {
                GMP.mpz_combit (a, TestUtil.urandom(rands) % d);
                check_one (a, c, d, 0);
	    }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 100 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*50.0/(float)reps)));
            }
        }

    }

    private void check_random_bits (int reps, randstate_t rands)
        throws Exception
    {
        long ea;
        long ec;
        long en;
        long d;
        long m = 10 * GMP.GMP_LIMB_BITS();
        mpz_t  a;
        mpz_t  c;
        int    i;

        a = new mpz_t();
        c = new mpz_t();
        GMP.mpz_set_ui(a, m + 1);
        GMP.mpz_set_ui(c, m);

        for (i = 0; i < reps; i++) {
            d  = TestUtil.urandom(rands) % m;
            ea = TestUtil.urandom(rands) % m;
            ec = TestUtil.urandom(rands) % m;
            en = TestUtil.urandom(rands) % m;

            GMP.mpz_set_ui (c, 0);
            GMP.mpz_setbit (c, en);

            GMP.mpz_set_ui (a, 0);
            GMP.mpz_setbit (a, ec);
            GMP.mpz_sub (c , a, c);

            GMP.mpz_set_ui (a, 0);
            GMP.mpz_setbit (a, ea);
            GMP.mpz_add (a , a, c);

            check_one (a, c, d, (ea >= d ? 1 : 0));
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 100 == 0) {
                onProgressUpdate(Integer.valueOf((int)(50.0 + (float)(i+1)*50.0/(float)reps)));
            }
        }
    }

    public void run()
    {
        int reps = 5000;
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

            check_data ();
            check_random (reps, rands);
            check_random_bits (reps, rands);
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

    private void dump_abort(String msg, mpz_t a, mpz_t c, mpz_t diff, mpz_t d2exp, long d)
        throws Exception
    {
        String a_str = "";
        String c_str = "";
        String diff_str = "";
        String d2exp_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            c_str = GMP.mpz_get_str(c, 10);
        }
        catch (GMPException e) {
            c_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            diff_str = GMP.mpz_get_str(diff, 10);
        }
        catch (GMPException e) {
            diff_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            d2exp_str = GMP.mpz_get_str(d2exp, 10);
        }
        catch (GMPException e) {
            d2exp_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " a=" + a_str + " c=" + c_str +
                   " a-c=" + diff_str + " 2^d=" + d2exp_str + " d=" + d;
        throw new Exception(emsg);
    }

}
