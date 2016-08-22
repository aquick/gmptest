package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class Cong_Task extends TaskBase implements Runnable {

    private static final String TAG = "Cong_Task";
    
    public Cong_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_one (mpz_t a, mpz_t c, mpz_t d, int want)
        throws Exception
    {
        mpz_t temp;
        int   got;
        int   swap;

        for (swap = 0; swap <= 1; swap++) {
            got = (GMP.mpz_congruent_p (a, c, d) != 0 ? 1 : 0);
            if (want != got) {
                dump_abort ("mpz_congruent_p wrong", a, c, d, want, got);
                /***
                printf ("mpz_congruent_p wrong\n");
                printf ("   expected %d got %d\n", want, got);
                mpz_trace ("	 a", a);
                mpz_trace ("	 c", c);
                mpz_trace ("	 d", d);
                mp_trace_base = -16;
                mpz_trace ("	 a", a);
                mpz_trace ("	 c", c);
                mpz_trace ("	 d", d);
                abort ();
                ***/
            }

            if (GMP.mpz_fits_ulong_p (c) != 0 && GMP.mpz_fits_ulong_p (d) != 0) {
                long uc = GMP.mpz_get_ui (c);
                long ud = GMP.mpz_get_ui (d);
                got = (GMP.mpz_congruent_ui_p (a, uc, ud) != 0 ? 1 : 0);
                if (want != got) {
                    dump_abort2 ("mpz_congruent_ui_p wrong", a, uc, ud, want, got);
                    /***
                    printf	("mpz_congruent_ui_p wrong\n");
                    printf	("   expected %d got %d\n", want, got);
                    mpz_trace ("   a", a);
                    printf	("   c=%lu\n", uc);
                    printf	("   d=%lu\n", ud);
                    mp_trace_base = -16;
                    mpz_trace ("   a", a);
                    printf	("   c=0x%lX\n", uc);
                    printf	("   d=0x%lX\n", ud);
                    abort ();
                    ***/
                }
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
        public String d;
        public int    want;

        public CheckData(String a, String c, String d, int want)
        {
            this.a = a;
            this.c = c;
            this.d = d;
            this.want = want;
        }
    }

    private static final CheckData[] data = {

    /* strict equality mod 0 */
    new CheckData( "0", "0", "0", 1 ),
    new CheckData( "11", "11", "0", 1 ),
    new CheckData( "3", "11", "0", 0 ),

    /* anything congruent mod 1 */
    new CheckData( "0", "0", "1", 1 ),
    new CheckData( "1", "0", "1", 1 ),
    new CheckData( "0", "1", "1", 1 ),
    new CheckData( "123", "456", "1", 1 ),
    new CheckData( "0x123456789123456789", "0x987654321987654321", "1", 1 ),

    /* csize==1, dsize==2 changing to 1 after stripping 2s */
    new CheckData( "0x3333333333333333",  "0x33333333",
      "0x180000000", 1 ),
    new CheckData( "0x33333333333333333333333333333333", "0x3333333333333333",
      "0x18000000000000000", 1 ),

    /* another dsize==2 becoming 1, with opposite signs this time */
    new CheckData(  "0x444444441",
      "-0x22222221F",
       "0x333333330", 1 ),
    new CheckData(  "0x44444444444444441",
      "-0x2222222222222221F",
       "0x33333333333333330", 1 )
    };

    private void check_data()
        throws Exception
    {
        mpz_t   a;
        mpz_t   c;
        mpz_t   d;
        int     i;

        a = new mpz_t();
        c = new mpz_t();
        d = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (a, data[i].a, 0);
            GMP.mpz_set_str (c, data[i].c, 0);
            GMP.mpz_set_str (d, data[i].d, 0);
            check_one (a, c, d, data[i].want);
        }
    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t   a;
        mpz_t   c;
        mpz_t   d;
        mpz_t   ra;
        mpz_t   rc;
        int     i;
        int     want;
        mpz_t bs;
        long  size_range;
        long  size;

        bs = new mpz_t();

        a = new mpz_t();
        c = new mpz_t();
        d = new mpz_t();
        ra = new mpz_t();
        rc = new mpz_t();

        for (i = 0; i < reps; i++) {
            GMP.mpz_urandomb (bs, rands, 32);
            size_range = GMP.mpz_get_ui (bs) % 16 + 1; /* 0..65536 bit operands */

            GMP.mpz_urandomb (bs, rands, size_range);
            size = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (a, rands, size);

            GMP.mpz_urandomb (bs, rands, 32);
            size_range = GMP.mpz_get_ui (bs) % 16 + 1; /* 0..65536 bit operands */

            GMP.mpz_urandomb (bs, rands, size_range);
            size = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (c, rands, size);

            do {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 16 + 1; /* 0..65536 bit operands */

                GMP.mpz_urandomb (bs, rands, size_range);
                size = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (d, rands, size);
            } while (GMP.mpz_internal_SIZ(d) == 0);

            TestUtil.mpz_negrandom (a, rands);
            GMP.mpz_internal_CHECK_FORMAT (a);
            TestUtil.mpz_negrandom (c, rands);
            GMP.mpz_internal_CHECK_FORMAT (c);
            TestUtil.mpz_negrandom (d, rands);

            GMP.mpz_fdiv_r (ra, a, d);
            GMP.mpz_fdiv_r (rc, c, d);

            want = (GMP.mpz_cmp (ra, rc) == 0 ? 1 : 0);
            check_one (a, c, d, want);

            GMP.mpz_sub (ra, ra, rc);
            GMP.mpz_sub (a, a, ra);
            GMP.mpz_internal_CHECK_FORMAT (a);
            check_one (a, c, d, 1);

            if (TestUtil.mpz_pow2abs_p (d) == 0) {
                GMP.mpz_combit (a, TestUtil.urandom(rands) % (8*GMP.GMP_LIMB_BITS())); // TODO
                check_one (a, c, d, 0);
            }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 1000 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }

    }

    public void run()
    {
        int reps = 10000;
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

    private void dump_abort(String msg, mpz_t a, mpz_t c, mpz_t d, int want, int got)
        throws Exception
    {
        String a_str = "";
        String c_str = "";
        String d_str = "";
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
            d_str = GMP.mpz_get_str(d, 10);
        }
        catch (GMPException e) {
            d_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " a=" + a_str + " c=" + c_str +
                   " d=" + d_str + " expected " + want + " got " + got;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t a, long c, long d, int want, int got)
        throws Exception
    {
        String a_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " a=" + a_str + " c=" + c +
                   " d=" + d + " expected " + want + " got " + got;
        throw new Exception(emsg);
    }

}
