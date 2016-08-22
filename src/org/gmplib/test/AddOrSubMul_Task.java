package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class AddOrSubMul_Task extends TaskBase implements Runnable {

    private static final String TAG = "AddOrSubMul_Task";
    
    public AddOrSubMul_Task(UI ui)
    {
        super(ui, TAG);
    }


    private static final long M = 0xFFFFFFFFL; // GMP_NUMB_MAX


    private void check_one_inplace (mpz_t w, mpz_t y)
        throws Exception
    {
        mpz_t  want;
        mpz_t  got;

        want = new mpz_t();
        got = new mpz_t();

        GMP.mpz_mul (want, w, y);
        GMP.mpz_add (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_addmul (got, got, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort ("mpz_addmul inplace fail", w, y, want, got);
            /***
            printf ("mpz_addmul inplace fail\n");
            mpz_trace ("w", w);
            mpz_trace ("y", y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

        GMP.mpz_mul (want, w, y);
        GMP.mpz_sub (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_submul (got, got, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort ("mpz_submul inplace fail", w, y, want, got);
            /***
            printf ("mpz_submul inplace fail\n");
            mpz_trace ("w", w);
            mpz_trace ("y", y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

    }

    private void check_one_ui_inplace (mpz_t w, long y)
        throws Exception
    {
        mpz_t  want;
        mpz_t  got;

        want = new mpz_t();
        got = new mpz_t();

        GMP.mpz_mul_ui (want, w, y);
        GMP.mpz_add (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_addmul_ui (got, got, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort2 ("mpz_addmul_ui fail", w, y, want, got);
            /***
            printf ("mpz_addmul_ui fail\n");
            mpz_trace ("w", w);
            printf    ("y=0x%lX   %lu\n", y, y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

        GMP.mpz_mul_ui (want, w, y);
        GMP.mpz_sub (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_submul_ui (got, got, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort2 ("mpz_submul_ui fail", w, y, want, got);
            /***
            printf ("mpz_submul_ui fail\n");
            mpz_trace ("w", w);
            printf    ("y=0x%lX   %lu\n", y, y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

    }

    private void check_all_inplace (mpz_t w, mpz_t y)
        throws Exception
    {
        int  wneg;
        int  yneg;

        GMP.mpz_internal_CHECK_FORMAT (w);
        GMP.mpz_internal_CHECK_FORMAT (y);

        for (wneg = 0; wneg < 2; wneg++) {
            for (yneg = 0; yneg < 2; yneg++) {
                check_one_inplace (w, y);

                if (GMP.mpz_fits_ulong_p (y) != 0) {
                    check_one_ui_inplace (w, GMP.mpz_get_ui (y));
                }

                GMP.mpz_neg (y, y);
            }
            GMP.mpz_neg (w, w);
        }
    }

    private void check_one (mpz_t w, mpz_t x, mpz_t y)
        throws Exception
    {
        mpz_t  want;
        mpz_t  got;

        want = new mpz_t();
        got = new mpz_t();

        GMP.mpz_mul (want, x, y);
        GMP.mpz_add (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_addmul (got, x, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort3 ("mpz_addmul fail", w, x, y, want, got);
            /***
            printf ("mpz_addmul fail\n");
            mpz_trace ("w", w);
            mpz_trace ("x", x);
            mpz_trace ("y", y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

        GMP.mpz_mul (want, x, y);
        GMP.mpz_sub (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_submul (got, x, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort3 ("mpz_submul fail", w, x, y, want, got);
            /***
            printf ("mpz_submul fail\n");
            mpz_trace ("w", w);
            mpz_trace ("x", x);
            mpz_trace ("y", y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

    }

    private void check_one_ui (mpz_t w, mpz_t x, long y)
        throws Exception
    {
        mpz_t  want;
        mpz_t  got;

        want = new mpz_t();
        got = new mpz_t();

        GMP.mpz_mul_ui (want, x, y);
        GMP.mpz_add (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_addmul_ui (got, x, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort4 ("mpz_addmul_ui fail", w, x, y, want, got);
            /***
            printf ("mpz_addmul_ui fail\n");
            mpz_trace ("w", w);
            mpz_trace ("x", x);
            printf    ("y=0x%lX   %lu\n", y, y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

        GMP.mpz_mul_ui (want, x, y);
        GMP.mpz_sub (want, w, want);
        GMP.mpz_set (got, w);
        GMP.mpz_submul_ui (got, x, y);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (want, got) != 0) {
            dump_abort4 ("mpz_submul_ui fail", w, x, y, want, got);
            /***
            printf ("mpz_submul_ui fail\n");
            mpz_trace ("w", w);
            mpz_trace ("x", x);
            printf    ("y=0x%lX   %lu\n", y, y);
            mpz_trace ("want", want);
            mpz_trace ("got ", got);
            abort ();
            ***/
        }

    }

    private void check_all (mpz_t w, mpz_t x, mpz_t y)
        throws Exception
    {
        int    swap;
        int    wneg;
        int    xneg;
        int    yneg;

        GMP.mpz_internal_CHECK_FORMAT (w);
        GMP.mpz_internal_CHECK_FORMAT (x);
        GMP.mpz_internal_CHECK_FORMAT (y);

        for (swap = 0; swap < 2; swap++) {
            for (wneg = 0; wneg < 2; wneg++) {
                for (xneg = 0; xneg < 2; xneg++) {
                    for (yneg = 0; yneg < 2; yneg++) {
                        check_one (w, x, y);

                        if (GMP.mpz_fits_ulong_p (y) != 0) {
                            check_one_ui (w, x, GMP.mpz_get_ui (y));
                        }

                        GMP.mpz_neg (y, y);
                    }
                    GMP.mpz_neg (x, x);
                }
                GMP.mpz_neg (w, w);
            }
            GMP.mpz_swap (x, y);
        }
    }

    private static class CheckData
    {
        public long[]      w;
        public long        y;

        public CheckData(long[] w, long y)
        {
            this.w = w;
            this.y = y;
        }
    }

    private static final CheckData[] data = new CheckData[] {

    new CheckData( new long[] { 0L }, 0L ),
    new CheckData( new long[] { 0L }, 1L ),
    new CheckData( new long[] { 1L }, 1L ),
    new CheckData( new long[] { 2L }, 1L ),

    new CheckData( new long[] { 123L }, 1L ),
    new CheckData( new long[] { 123L }, GMP.ULONG_MAX ),
    new CheckData( new long[] { M }, 1L ),
    new CheckData( new long[] { M }, GMP.ULONG_MAX ),

    new CheckData( new long[] { 123L, 456L }, 1L ),
    new CheckData( new long[] { M, M }, 1L ),
    new CheckData( new long[] { 123L, 456L }, GMP.ULONG_MAX ),
    new CheckData( new long[] { M, M }, GMP.ULONG_MAX ),

    new CheckData( new long[] { 123L, 456L, 789L }, 1L ),
    new CheckData( new long[] { M, M, M }, 1L ),
    new CheckData( new long[] { 123L, 456L, 789L }, GMP.ULONG_MAX ),
    new CheckData( new long[] { M, M, M }, GMP.ULONG_MAX )

    };

    private void check_data_inplace_ui ()
        throws Exception
    {
        mpz_t  w;
        mpz_t  y;
        int    i;

        w = new mpz_t();
        y = new mpz_t();

        for (i = 0; i < data.length; i++) {
            TestUtil.mpz_set_n (w, data[i].w, data[i].w.length);
            GMP.mpz_set_ui (y, data[i].y);
            check_all_inplace (w, y);
        }
    }

    private static class CheckData2
    {
        public long[]      w;
        public long[]      x;
        public long[]      y;

        public CheckData2(long[] w, long[] x, long[] y)
        {
            this.w = w;
            this.x = x;
            this.y = y;
        }
    }

    private static final CheckData2[] data2 = new CheckData2[] {

    /* reducing to zero */
    new CheckData2( new long[] { 1L }, new long[] { 1L }, new long[] { 1L } ),
    new CheckData2( new long[] { 2L }, new long[] { 1L }, new long[] { 2L } ),
    new CheckData2( new long[] { 0L,1L }, new long[] { 0L,1L }, new long[] { 1L } ),

    /* reducing to 1 */
    new CheckData2( new long[] { 0L,1L },       new long[] { M },       new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,1L },     new long[] { M,M },     new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,0L,1L },   new long[] { M,M,M },   new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,0L,0L,1L }, new long[] { M,M,M,M }, new long[] { 1L } ),

    /* reducing to -1 */
    new CheckData2( new long[] { M },       new long[] { 0L,1L },       new long[] { 1L } ),
    new CheckData2( new long[] { M,M },     new long[] { 0L,0L,1L },     new long[] { 1L } ),
    new CheckData2( new long[] { M,M,M },   new long[] { 0L,0L,0L,1L },   new long[] { 1L } ),
    new CheckData2( new long[] { M,M,M,M }, new long[] { 0L,0L,0L,0L,1L }, new long[] { 1L } ),

    /* carry out of addmul */
    new CheckData2( new long[] { M },     new long[] { 1L }, new long[] { 1L } ),
    new CheckData2( new long[] { M,M },   new long[] { 1L }, new long[] { 1L } ),
    new CheckData2( new long[] { M,M,M }, new long[] { 1L }, new long[] { 1L } ),

    /* borrow from submul */
    new CheckData2( new long[] { 0L,1L },     new long[] { 1L }, new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,1L },   new long[] { 1L }, new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,0L,1L }, new long[] { 1L }, new long[] { 1L } ),

    /* borrow from submul */
    new CheckData2( new long[] { 0L,0L,1L },     new long[] { 0L,1L }, new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,0L,1L },   new long[] { 0L,1L }, new long[] { 1L } ),
    new CheckData2( new long[] { 0L,0L,0L,0L,1L }, new long[] { 0L,1L }, new long[] { 1L } ),

    /* more borrow from submul */
    new CheckData2( new long[] { M }, new long[] { 0L,1L },       new long[] { 1L } ),
    new CheckData2( new long[] { M }, new long[] { 0L,0L,1L },     new long[] { 1L } ),
    new CheckData2( new long[] { M }, new long[] { 0L,0L,0L,1L },   new long[] { 1L } ),
    new CheckData2( new long[] { M }, new long[] { 0L,0L,0L,0L,1L }, new long[] { 1L } ),

    /* big borrow from submul */
    new CheckData2( new long[] { 0L,0L,1L },     new long[] { M,M }, new long[] { M } ),
    new CheckData2( new long[] { 0L,0L,0L,1L },   new long[] { M,M }, new long[] { M } ),
    new CheckData2( new long[] { 0L,0L,0L,0L,1L }, new long[] { M,M }, new long[] { M } ),

    /* small w */
    new CheckData2( new long[] { 0L,1L }, new long[] { M,M },       new long[] { M } ),
    new CheckData2( new long[] { 0L,1L }, new long[] { M,M,M },     new long[] { M } ),
    new CheckData2( new long[] { 0L,1L }, new long[] { M,M,M,M },   new long[] { M } ),
    new CheckData2( new long[] { 0L,1L }, new long[] { M,M,M,M,M }, new long[] { M } ),
    };

    private void check_data ()
        throws Exception
    {
        mpz_t  w;
        mpz_t  x;
        mpz_t  y;
        int    i;

        w = new mpz_t();
        x = new mpz_t();
        y = new mpz_t();

        for (i = 0; i < data2.length; i++) {
            TestUtil.mpz_set_n (w, data2[i].w, data2[i].w.length);
            TestUtil.mpz_set_n (x, data2[i].x, data2[i].x.length);
            TestUtil.mpz_set_n (y, data2[i].y, data2[i].y.length);
            check_all (w, x, y);
        }
    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t  w;
        mpz_t  x;
        mpz_t  y;
        int    i;

        w = new mpz_t();
        x = new mpz_t();
        y = new mpz_t();

        for (i = 0; i < reps; i++) {
            TestUtil.mpz_errandomb (w, rands, 5*GMP.GMP_LIMB_BITS());
            TestUtil.mpz_errandomb (x, rands, 5*GMP.GMP_LIMB_BITS());
            TestUtil.mpz_errandomb (y, rands, 5*GMP.GMP_LIMB_BITS());
            check_all (w, x, y);
            check_all_inplace (w, y);

            TestUtil.mpz_errandomb (w, rands, 5*GMP.GMP_LIMB_BITS());
            TestUtil.mpz_errandomb (x, rands, 5*GMP.GMP_LIMB_BITS());
            TestUtil.mpz_errandomb (y, rands, 32 /*BITS_PER_ULONG*/);
            check_all (w, x, y);
            check_all_inplace (w, y);
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 100 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }

    }

    public void run()
    {
        int reps = 2000;
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
            check_data_inplace_ui ();
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

    private void dump_abort(String msg, mpz_t w, mpz_t y, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(w, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(y, 10);
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
        emsg = "ERROR: " + msg + " w=" + a_str + " y=" + b_str +
                   " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t w, long y, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(w, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        b_str = Long.toString(y, 10);
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
        emsg = "ERROR: " + msg + " w=" + a_str + " y=" + b_str +
                   " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

    private void dump_abort3(String msg, mpz_t w, mpz_t x, mpz_t y, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String x_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(w, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            b_str = GMP.mpz_get_str(y, 10);
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
        emsg = "ERROR: " + msg + " w=" + a_str + " x=" + x_str + " y=" + b_str +
                   " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

    private void dump_abort4(String msg, mpz_t w, mpz_t x, long y, mpz_t want, mpz_t got)
        throws Exception
    {
        String a_str = "";
        String x_str = "";
        String b_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(w, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        b_str = Long.toString(y, 10);
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
        emsg = "ERROR: " + msg + " w=" + a_str + " x=" + x_str + " y=" + b_str +
                   " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

}
