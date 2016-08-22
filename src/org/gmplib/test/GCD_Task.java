package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class GCD_Task extends TaskBase implements Runnable {

    private static final String TAG = "GCD_Task";
    
    public GCD_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final int HGCD_REDUCE_THRESHOLD = 4284; //from mpn\arm\v7a\cora9\gmp-mparam.h
    private static final int MAX_SCHOENHAGE_THRESHOLD = HGCD_REDUCE_THRESHOLD;
    private static final int MIN_OPERAND_BITSIZE = 1;
    private static final long GMP_NUMB_MAX = GMP.ULONG_MAX;

    private void check_data ()
        throws Exception
    {
        mpz_t  a;
        mpz_t  b;
        mpz_t  got;
        mpz_t  want;

        a = new mpz_t();
        b = new mpz_t();
        got = new mpz_t();
        want = new mpz_t();

        /* This tickled a bug in gmp 4.1.2 mpn/x86/k6/gcd_finda.asm. */
        GMP.mpz_set_str (a, "0x3FFC000007FFFFFFFFFF00000000003F83FFFFFFFFFFFFFFF80000000000000001", 0);
        GMP.mpz_set_str (b, "0x1FFE0007FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC000000000000000000000001", 0);
        GMP.mpz_set_str (want, "5", 0);
        GMP.mpz_gcd (got, a, b);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort("mpz_gcd wrong", a, b, want, got);
            /***
            printf    ("mpz_gcd wrong on data[%d]\n", i);
            printf    (" a  %s\n", data[i].a);
            printf    (" b  %s\n", data[i].b);
            mpz_trace (" a", a);
            mpz_trace (" b", b);
            mpz_trace (" want", want);
            mpz_trace (" got ", got);
            abort ();
            ***/
        }
    }

    private void make_chain_operands (mpz_t ref, mpz_t a, mpz_t b, randstate_t rs, int nb1, int nb2, int chain_len)
        throws GMPException
    {
        mpz_t bs;
        mpz_t temp1;
        mpz_t temp2;
        int j;

        bs = new mpz_t();
        temp1 = new mpz_t();
        temp2 = new mpz_t();

        /* Generate a division chain backwards, allowing otherwise unlikely huge
           quotients.  */

        GMP.mpz_set_ui (a, 0);
        GMP.mpz_urandomb (bs, rs, 32);
        GMP.mpz_urandomb (bs, rs, GMP.mpz_get_ui (bs) % nb1 + 1);
        GMP.mpz_rrandomb (b, rs, GMP.mpz_get_ui (bs));
        GMP.mpz_add_ui (b, b, 1);
        GMP.mpz_set (ref, b);

        for (j = 0; j < chain_len; j++) {
            GMP.mpz_urandomb (bs, rs, 32);
            GMP.mpz_urandomb (bs, rs, GMP.mpz_get_ui (bs) % nb2 + 1);
            GMP.mpz_rrandomb (temp2, rs, GMP.mpz_get_ui (bs) + 1);
            GMP.mpz_add_ui (temp2, temp2, 1);
            GMP.mpz_mul (temp1, b, temp2);
            GMP.mpz_add (a, a, temp1);

            GMP.mpz_urandomb (bs, rs, 32);
            GMP.mpz_urandomb (bs, rs, GMP.mpz_get_ui (bs) % nb2 + 1);
            GMP.mpz_rrandomb (temp2, rs, GMP.mpz_get_ui (bs) + 1);
            GMP.mpz_add_ui (temp2, temp2, 1);
            GMP.mpz_mul (temp1, a, temp2);
            GMP.mpz_add (b, b, temp1);
        }

    }

    private static class CheckData1
    {
        public long seed;
        public int  nb;
        public String want;

        public CheckData1(long seed, int nb, String want)
        {
            this.seed = seed;
            this.nb = nb;
            this.want = want;
        }
    }

    private static final CheckData1[] data1 = new CheckData1[] {
    new CheckData1( 59618, 38208, "5"),
    new CheckData1( 76521, 49024, "3"),
    new CheckData1( 85869, 54976, "1"),
    new CheckData1( 99449, 63680, "1"),
    new CheckData1(112453, 72000, "1")
    };

    /* Test operands from a table of seed data.  This variant creates the operands
       using plain ol' mpz_rrandomb.  This is a hack for better coverage of the gcd
       code, which depends on that the random number generators give the exact
       numbers we expect.  */
    private void check_kolmo1 (mpz_t gcd1, mpz_t gcd2, mpz_t temp1, mpz_t temp2, mpz_t temp3, mpz_t s)
        throws Exception
    {
        randstate_t rs;
        mpz_t  bs;
        mpz_t  a;
        mpz_t  b;
        mpz_t  want;
        int    i;
        long   unb;
        long   vnb;
        int    nb;

        bs = new mpz_t();
        a = new mpz_t();
        b = new mpz_t();
        want = new mpz_t();

        for (i = 0; i < data1.length; i++) {
            nb = data1[i].nb;

            rs = new randstate_t(data1[i].seed);

            GMP.mpz_urandomb (bs, rs, 32);
            unb = GMP.mpz_get_ui (bs) % nb;
            GMP.mpz_urandomb (bs, rs, 32);
            vnb = GMP.mpz_get_ui (bs) % nb;

            GMP.mpz_rrandomb (a, rs, unb);
            GMP.mpz_rrandomb (b, rs, vnb);

            GMP.mpz_set_str (want, data1[i].want, 0);

            one_test (a, b, want, -1, gcd1, gcd2, temp1, temp2, temp3, s);
        }

    }

    private static class CheckData2
    {
        public long seed;
        public int  nb;
        public int  chain_len;

        public CheckData2(long seed, int nb, int chain_len)
        {
            this.seed = seed;
            this.nb = nb;
            this.chain_len = chain_len;
        }
    }

    private static final CheckData2[] data2 = new CheckData2[] {
    new CheckData2(  917, 15, 5 ),
    new CheckData2( 1032, 18, 6 ),
    new CheckData2( 1167, 18, 6 ),
    new CheckData2( 1174, 18, 6 ),
    new CheckData2( 1192, 18, 6 )
    };

    /* Test operands from a table of seed data.  This variant creates the operands
       using a division chain.  This is a hack for better coverage of the gcd
       code, which depends on that the random number generators give the exact
       numbers we expect.  */
    private void check_kolmo2 (mpz_t gcd1, mpz_t gcd2, mpz_t temp1, mpz_t temp2, mpz_t temp3, mpz_t s)
        throws Exception
    {
        randstate_t rs;
        mpz_t  a;
        mpz_t  b;
        mpz_t  want;
        int    i;

        a = new mpz_t();
        b = new mpz_t();
        want = new mpz_t();

        for (i = 0; i < data2.length; i++) {
            rs = new randstate_t(data2[i].seed);
            make_chain_operands (want, a, b, rs, data2[i].nb, data2[i].nb, data2[i].chain_len);
            one_test (a, b, want, -1, gcd1, gcd2, temp1, temp2, temp3, s);
        }

    }

    /* Called when g is supposed to be gcd(a,b), and g = s a + t b, for some t.
       Uses temp1, temp2 and temp3. */
    private int gcdext_valid_p (mpz_t a, mpz_t b, mpz_t g, mpz_t s,
                                mpz_t temp1, mpz_t temp2, mpz_t temp3)
        throws GMPException
    {
        /* It's not clear that gcd(0,0) is well defined, but we allow it and require that
           gcd(0,0) = 0. */
        if (GMP.mpz_sgn (g) < 0) return 0;

        if (GMP.mpz_sgn (a) == 0) {
            /* Must have g == abs (b). Any value for s is in some sense "correct",
               but it makes sense to require that s == 0. */
            return (GMP.mpz_cmpabs (g, b) == 0 && GMP.mpz_sgn (s) == 0 ? 1 : 0);
        } else if (GMP.mpz_sgn (b) == 0) {
            /* Must have g == abs (a), s == sign (a) */
            return (GMP.mpz_cmpabs (g, a) == 0 && GMP.mpz_cmp_si (s, GMP.mpz_sgn (a)) == 0 ? 1 : 0);
        }

        if (GMP.mpz_sgn (g) <= 0) return 0;

        GMP.mpz_tdiv_qr (temp1, temp3, a, g);
        if (GMP.mpz_sgn (temp3) != 0) return 0;

        GMP.mpz_tdiv_qr (temp2, temp3, b, g);
        if (GMP.mpz_sgn (temp3) != 0) return 0;

        /* Require that 2 |s| < |b/g|, or |s| == 1. */
        if (GMP.mpz_cmpabs_ui (s, 1) > 0) {
            GMP.mpz_mul_2exp (temp3, s, 1);
            if (GMP.mpz_cmpabs (temp3, temp2) >= 0) return 0;
        }

        /* Compute the other cofactor. */
        GMP.mpz_mul(temp2, s, a);
        GMP.mpz_sub(temp2, g, temp2);
        GMP.mpz_tdiv_qr(temp2, temp3, temp2, b);

        if (GMP.mpz_sgn (temp3) != 0) return 0;

        /* Require that 2 |t| < |a/g| or |t| == 1*/
        if (GMP.mpz_cmpabs_ui (temp2, 1) > 0) {
            GMP.mpz_mul_2exp (temp2, temp2, 1);
            if (GMP.mpz_cmpabs (temp2, temp1) >= 0) return 0;
        }
        return 1;
    }

    private void one_test (mpz_t op1, mpz_t op2, mpz_t ref, int i,
                           mpz_t gcd1, mpz_t gcd2, mpz_t temp1, mpz_t temp2, mpz_t temp3, mpz_t s)
        throws Exception
    {
        mpz_t temp4 = new mpz_t();

        GMP.mpz_gcdext (gcd1, s, temp4, op1, op2);
        GMP.mpz_internal_CHECK_FORMAT (gcd1);
        GMP.mpz_internal_CHECK_FORMAT (s);

        if (ref != null && GMP.mpz_cmp (ref, gcd1) != 0) {
            dump_abort2("ERROR in test " + i + "mpz_gcdext returned incorrect result",
                op1, op2, ref, gcd1);
            /***
            fprintf (stderr, "ERROR in test %d\n", i);
            fprintf (stderr, "mpz_gcdext returned incorrect result\n");
            fprintf (stderr, "op1=");                 debug_mp (op1, -16);
            fprintf (stderr, "op2=");                 debug_mp (op2, -16);
            fprintf (stderr, "expected result:\n");   debug_mp (ref, -16);
            fprintf (stderr, "mpz_gcdext returns:\n");debug_mp (gcd1, -16);
            abort ();
            ***/
        }

        if (gcdext_valid_p(op1, op2, gcd1, s, temp1, temp2, temp3) == 0) {
            dump_abort3("ERROR in test " + i + "mpz_gcdext returned invalid result",
                op1, op2, gcd1, s);
            /***
            fprintf (stderr, "ERROR in test %d\n", i);
            fprintf (stderr, "mpz_gcdext returned invalid result\n");
            fprintf (stderr, "op1=");                 debug_mp (op1, -16);
            fprintf (stderr, "op2=");                 debug_mp (op2, -16);
            fprintf (stderr, "mpz_gcdext returns:\n");debug_mp (gcd1, -16);
            fprintf (stderr, "s=");                   debug_mp (s, -16);
            abort ();
            ***/
        }

        GMP.mpz_gcd (gcd2, op1, op2);
        GMP.mpz_internal_CHECK_FORMAT (gcd2);

        if (GMP.mpz_cmp (gcd2, gcd1) != 0) {
            dump_abort2("ERROR in test " + i + "mpz_gcd returned incorrect result",
                op1, op2, gcd1, gcd2);
            /***
            fprintf (stderr, "ERROR in test %d\n", i);
            fprintf (stderr, "mpz_gcd returned incorrect result\n");
            fprintf (stderr, "op1=");                 debug_mp (op1, -16);
            fprintf (stderr, "op2=");                 debug_mp (op2, -16);
            fprintf (stderr, "expected result:\n");   debug_mp (gcd1, -16);
            fprintf (stderr, "mpz_gcd returns:\n");   debug_mp (gcd2, -16);
            abort ();
            ***/
        }

        /* This should probably move to t-gcd_ui.c */
        if (GMP.mpz_fits_ulong_p (op1) != 0 || GMP.mpz_fits_ulong_p (op2) != 0) {
            if (GMP.mpz_fits_ulong_p (op1) != 0) {
                GMP.mpz_gcd_ui (gcd2, op2, GMP.mpz_get_ui (op1));
            } else {
                GMP.mpz_gcd_ui (gcd2, op1, GMP.mpz_get_ui (op2));
            }
            if (GMP.mpz_cmp (gcd2, gcd1) != 0) {
                dump_abort2("ERROR in test " + i + "mpz_gcd_ui returned incorrect result",
                    op1, op2, gcd1, gcd2);
                /***
                fprintf (stderr, "ERROR in test %d\n", i);
                fprintf (stderr, "mpz_gcd_ui returned incorrect result\n");
                fprintf (stderr, "op1=");                 debug_mp (op1, -16);
                fprintf (stderr, "op2=");                 debug_mp (op2, -16);
                fprintf (stderr, "expected result:\n");   debug_mp (gcd1, -16);
                fprintf (stderr, "mpz_gcd_ui returns:\n");   debug_mp (gcd2, -16);
                abort ();
                ***/
            }
        }

        GMP.mpz_gcdext (gcd2, temp1, temp2, op1, op2);
        GMP.mpz_internal_CHECK_FORMAT (gcd2);
        GMP.mpz_internal_CHECK_FORMAT (temp1);
        GMP.mpz_internal_CHECK_FORMAT (temp2);

        GMP.mpz_mul (temp1, temp1, op1);
        GMP.mpz_mul (temp2, temp2, op2);
        GMP.mpz_add (temp1, temp1, temp2);

        if (GMP.mpz_cmp (gcd1, gcd2) != 0 || GMP.mpz_cmp (gcd2, temp1) != 0) {
            dump_abort2("ERROR in test " + i + "mpz_gcdext returned incorrect result",
                op1, op2, gcd1, gcd2);
            /***
            fprintf (stderr, "ERROR in test %d\n", i);
            fprintf (stderr, "mpz_gcdext returned incorrect result\n");
            fprintf (stderr, "op1=");                 debug_mp (op1, -16);
            fprintf (stderr, "op2=");                 debug_mp (op2, -16);
            fprintf (stderr, "expected result:\n");   debug_mp (gcd1, -16);
            fprintf (stderr, "mpz_gcdext returns:\n");debug_mp (gcd2, -16);
            abort ();
            ***/
        }
    }

    public void run()
    {
        mpz_t op1;
        mpz_t op2;
        mpz_t ref;
        mpz_t gcd1;
        mpz_t gcd2;
        mpz_t s;
        mpz_t temp1;
        mpz_t temp2;
        mpz_t temp3;

        int i;
        int chain_len;
        mpz_t bs;
        long bsi;
        long size_range;
        int reps = 200;
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
            String str = "seed=" + seed;
            Log.d(TAG, str);
            uinterface.display(str);
            rands = new randstate_t(seed);

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            bs = new mpz_t();
            op1 = new mpz_t();
            op2 = new mpz_t();
            ref = new mpz_t();
            gcd1 = new mpz_t();
            gcd2 = new mpz_t();
            temp1 = new mpz_t();
            temp2 = new mpz_t();
            temp3 = new mpz_t();
            s = new mpz_t();

            check_data ();
            check_kolmo1 (gcd1, gcd2, temp1, temp2, temp3, s);
            check_kolmo2 (gcd1, gcd2, temp1, temp2, temp3, s);

            /* Testcase to exercise the u0 == u1 case in mpn_gcdext_lehmer_n. */
            GMP.mpz_set_ui (op2, GMP_NUMB_MAX); /* FIXME: Huge limb doesn't always fit */
            GMP.mpz_mul_2exp (op1, op2, 100);
            GMP.mpz_add (op1, op1, op2);
            GMP.mpz_mul_ui (op2, op2, 2);
            one_test (op1, op2, null, -1, gcd1, gcd2, temp1, temp2, temp3, s);

            for (i = 0; i < reps; i++) {
                /* Generate plain operands with unknown gcd.  These types of operands
                have proven to trigger certain bugs in development versions of the
                gcd code.  The "hgcd->row[3].rsize > M" ASSERT is not triggered by
                the division chain code below, but that is most likely just a result
                of that other ASSERTs are triggered before it.  */

                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 17 + 2;

                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (op1, rands, GMP.mpz_get_ui (bs) + MIN_OPERAND_BITSIZE);
                GMP.mpz_urandomb (bs, rands, size_range);
                GMP.mpz_rrandomb (op2, rands, GMP.mpz_get_ui (bs) + MIN_OPERAND_BITSIZE);

                GMP.mpz_urandomb (bs, rands, 8);
                bsi = GMP.mpz_get_ui (bs);

                if ((bsi & 0x3c) == 4) {
                    GMP.mpz_mul (op1, op1, op2);    /* make op1 a multiple of op2 */
                } else if ((bsi & 0x3c) == 8) {
                    GMP.mpz_mul (op2, op1, op2);    /* make op2 a multiple of op1 */
                }

                if ((bsi & 1) != 0) GMP.mpz_neg (op1, op1);
                if ((bsi & 2) != 0) GMP.mpz_neg (op2, op2);

                one_test (op1, op2, null, i, gcd1, gcd2, temp1, temp2, temp3, s);

                /* Generate a division chain backwards, allowing otherwise unlikely huge
                   quotients.  */

                GMP.mpz_urandomb (bs, rands, 32);
                chain_len = (int)(GMP.mpz_get_ui (bs) % TestUtil.log2c (GMP.GMP_NUMB_BITS() * MAX_SCHOENHAGE_THRESHOLD));
                GMP.mpz_urandomb (bs, rands, 32);
                chain_len = (int)(GMP.mpz_get_ui (bs) % (1 << chain_len) / 32);

                make_chain_operands (ref, op1, op2, rands, 16, 12, chain_len);

                one_test (op1, op2, ref, i, gcd1, gcd2, temp1, temp2, temp3, s);
                
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

    private void dump_abort(String msg, mpz_t a, mpz_t b, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(b, 10);
        }
        catch (GMPException e) {
            b_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " a=" + a_str + " b=" + b_str +
                   " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t a, mpz_t b, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(b, 10);
        }
        catch (GMPException e) {
            b_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " op1=" + a_str + " op2=" + b_str +
                   " expected=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

    private void dump_abort3(String msg, mpz_t a, mpz_t b, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(b, 10);
        }
        catch (GMPException e) {
            b_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " op1=" + a_str + " op2=" + b_str +
                   " mpz_gcdext=" + want_str + " s=" + got_str;
        throw new Exception(emsg);
    }

}
