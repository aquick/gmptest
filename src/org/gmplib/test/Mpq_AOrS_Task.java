package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_AOrS_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_AOrS_Task";
    
    public Mpq_AOrS_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_all (mpq_t x, mpq_t y, mpq_t want_add, mpq_t want_sub)
        throws Exception
    {
        mpq_t  got;
        int    neg_x;
        int    neg_y;
        int    swap;

        got = new mpq_t();

        GMP.mpq_internal_CHECK_FORMAT (want_add);
        GMP.mpq_internal_CHECK_FORMAT (want_sub);
        GMP.mpq_internal_CHECK_FORMAT (x);
        GMP.mpq_internal_CHECK_FORMAT (y);

        for (swap = 0; swap <= 1; swap++) {
            for (neg_x = 0; neg_x <= 1; neg_x++) {
                for (neg_y = 0; neg_y <= 1; neg_y++) {
                    GMP.mpq_add (got, x, y);
                    GMP.mpq_internal_CHECK_FORMAT (got);
                    if (GMP.mpq_equal (got, want_add) == 0) {
                	dump_abort("mpq_add wrong", x, y, got, want_add);
                	/***
                        printf ("mpq_add wrong\n");
                        mpq_trace ("  x   ", x);
                        mpq_trace ("  y   ", y);
                        mpq_trace ("  got ", got);
                        mpq_trace ("  want", want_add);
                        abort ();
                        ***/
                    }

                    GMP.mpq_sub (got, x, y);
                    GMP.mpq_internal_CHECK_FORMAT (got);
                    if (GMP.mpq_equal (got, want_sub) == 0) {
                	dump_abort("mpq_sub wrong", x, y, got, want_sub);
                	/***
                        printf ("mpq_sub wrong\n");
                        mpq_trace ("  x   ", x);
                        mpq_trace ("  y   ", y);
                        mpq_trace ("  got ", got);
                        mpq_trace ("  want", want_sub);
                        abort ();
                        ***/
                    }


                    GMP.mpq_neg (y, y);
                    GMP.mpq_swap (want_add, want_sub);
                }

                GMP.mpq_neg (x, x);
                GMP.mpq_swap (want_add, want_sub);
                GMP.mpq_neg (want_add, want_add);
                GMP.mpq_neg (want_sub, want_sub);
            }

            GMP.mpq_swap (x, y);
            GMP.mpq_neg (want_sub, want_sub);
        }

    }

    private static void refmpq_add (mpq_t w, mpq_t x, mpq_t y)
        throws GMPException
    {
	mpz_t wnum = new mpz_t();
	mpz_t wden = new mpz_t();
	mpz_t xnum = new mpz_t();
	mpz_t xden = new mpz_t();
	mpz_t ynum = new mpz_t();
	mpz_t yden = new mpz_t();

	GMP.mpq_get_num(xnum, x);
	GMP.mpq_get_den(xden, x);
	GMP.mpq_get_num(ynum, y);
	GMP.mpq_get_den(yden, y);
	
	GMP.mpz_mul    (wnum, xnum, yden);
        GMP.mpz_addmul (wnum, xden, ynum);
        GMP.mpz_mul    (wden, xden, yden);
        GMP.mpq_set_num(w, wnum);
        GMP.mpq_set_den(w, wden);
        GMP.mpq_canonicalize (w);
    }

    private static void refmpq_sub (mpq_t w, mpq_t x, mpq_t y)
	throws GMPException
    {
	mpz_t wnum = new mpz_t();
	mpz_t wden = new mpz_t();
	mpz_t xnum = new mpz_t();
	mpz_t xden = new mpz_t();
	mpz_t ynum = new mpz_t();
	mpz_t yden = new mpz_t();

	GMP.mpq_get_num(xnum, x);
	GMP.mpq_get_den(xden, x);
	GMP.mpq_get_num(ynum, y);
	GMP.mpq_get_den(yden, y);
	
        GMP.mpz_mul    (wnum, xnum, yden);
        GMP.mpz_submul (wnum, xden, ynum);
        GMP.mpz_mul    (wden, xden, yden);
        GMP.mpq_set_num(w, wnum);
        GMP.mpq_set_den(w, wden);
        GMP.mpq_canonicalize (w);
    }

    private void check_rand (randstate_t rands, int reps)
        throws Exception
    {
	mpz_t z;
        mpq_t x;
        mpq_t y;
        mpq_t want_add;
        mpq_t want_sub;
        int i;

        z = new mpz_t();
        x = new mpq_t();
        y = new mpq_t();
        want_add = new mpq_t();
        want_sub = new mpq_t();

        for (i = 0; i < reps; i++) {
            GMP.mpq_get_num(z,  x);
            TestUtil.mpz_errandomb (z, rands, 512L);
            GMP.mpq_get_den(z,  x);
            TestUtil.mpz_errandomb_nonzero (z, rands, 512L);
            GMP.mpq_canonicalize (x);

            GMP.mpq_get_num(z,  y);
            TestUtil.mpz_errandomb (z, rands, 512L);
            GMP.mpq_get_den(z,  y);
            TestUtil.mpz_errandomb_nonzero (z, rands, 512L);
            GMP.mpq_canonicalize (y);

            refmpq_add (want_add, x, y);
            refmpq_sub (want_sub, x, y);

            check_all (x, y, want_add, want_sub);

            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 10 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }

    }

    private static final class CheckData
    {
        public String x;
        public String y;
        public String want_add;
        public String want_sub;

        public CheckData(String x, String y, String want_add, String want_sub)
        {
            this.x = x;
            this.y = y;
            this.want_add = want_add;
            this.want_sub = want_sub;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	    new CheckData( "0", "0", "0", "0" ),
	    new CheckData( "1", "0", "1", "1" ),
	    new CheckData( "1", "1", "2", "0" ),

	    new CheckData( "1/2", "1/2", "1", "0" ),
	    new CheckData( "5/6", "14/15", "53/30", "-1/10" )
	
    };
    
    private void check_data()
        throws Exception
    {
	mpq_t x;
	mpq_t y;
	mpq_t want_add;
	mpq_t want_sub;
	int i;

	x = new mpq_t();
	y = new mpq_t();
	want_add = new mpq_t();
	want_sub = new mpq_t();

	for (i = 0; i < data.length; i++) {
	    GMP.mpq_set_str (x, data[i].x, 0);
	    GMP.mpq_set_str (y, data[i].y, 0);
	    GMP.mpq_set_str (want_add, data[i].want_add, 0);
	    GMP.mpq_set_str (want_sub, data[i].want_sub, 0);

	    check_all (x, y, want_add, want_sub);
	}	
    }
    
    public void run()
    {
	int reps = 500;
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

    private void dump_abort(String msg, mpq_t x, mpq_t y, mpq_t got, mpq_t want)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            x_str = GMP.mpq_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpq_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpq_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpq_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " x=" + x_str + " y=" + y_str + " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

}
