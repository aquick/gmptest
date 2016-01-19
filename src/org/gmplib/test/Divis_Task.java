package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Divis_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Divis_Task";
    
    public Divis_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static class CheckData
    {
        public CheckData(String a, String d, int want)
        {
            this.a = a;
            this.d = d;
            this.want = want;
        }
        public String a;
        public String d;
        public int    want;
    }
    
    private static final CheckData[] data = new CheckData[] {
    new CheckData( "0",    "0", 1 ),
    new CheckData( "17",   "0", 0 ),
    new CheckData( "0",    "1", 1 ),
    new CheckData( "123",  "1", 1 ),
    new CheckData( "-123", "1", 1 ),

    new CheckData( "0",  "2", 1 ),
    new CheckData( "1",  "2", 0 ),
    new CheckData( "2",  "2", 1 ),
    new CheckData( "-2", "2", 1 ),
    new CheckData( "0x100000000000000000000000000000000", "2", 1 ),
    new CheckData( "0x100000000000000000000000000000001", "2", 0 ),

    new CheckData( "0x3333333333333333", "3", 1 ),
    new CheckData( "0x3333333333333332", "3", 0 ),
    new CheckData( "0x33333333333333333333333333333333", "3", 1 ),
    new CheckData( "0x33333333333333333333333333333332", "3", 0 ),

    /* divisor changes from 2 to 1 limb after stripping 2s */
    new CheckData(          "0x3333333300000000",         "0x180000000",         1 ),
    new CheckData(  "0x33333333333333330000000000000000", "0x18000000000000000", 1 ),
    new CheckData( "0x133333333333333330000000000000000", "0x18000000000000000", 0 )
    };

    private void check_one (mpz_t a, mpz_t d, int want)
        throws Exception
    {
        int   got;

        if (GMP.mpz_fits_ulong_p (d) != 0) {
            long  u = GMP.mpz_get_ui (d);
            got = (GMP.mpz_divisible_ui_p (a, u) != 0 ? 1 : 0);
            if (want != got) {
                dump_abort ("mpz_divisible_ui_p wrong", want, got, a, u);
                /***
                printf ("mpz_divisible_ui_p wrong\n");
                printf ("   expected %d got %d\n", want, got);
                mpz_trace ("   a", a);
                printf ("   d=%lu\n", u);
                mp_trace_base = -16;
                mpz_trace ("   a", a);
                printf ("   d=0x%lX\n", u);
                abort ();
                ***/
            }
        }

        got = (GMP.mpz_divisible_p (a, d) != 0 ? 1 : 0);
        if (want != got) {
            dump_abort2 ("mpz_divisible_p wrong", want, got, a, d);
            /***
            printf ("mpz_divisible_p wrong\n");
            printf ("   expected %d got %d\n", want, got);
            mpz_trace ("   a", a);
            mpz_trace ("   d", d);
            mp_trace_base = -16;
            mpz_trace ("   a", a);
            mpz_trace ("   d", d);
            abort ();
            ***/
        }
    }

    private void check_data()
        throws Exception
    {
        mpz_t  a = new mpz_t();
        mpz_t  d = new mpz_t();
        int i;

        for (i = 0; i < data.length; i++) {

            GMP.mpz_set_str (a, data[i].a, 0);
            GMP.mpz_set_str (d, data[i].d, 0);

            check_one (a, d, data[i].want);
        }

    }

    private void check_random (int reps, randstate_t rands)
        throws Exception
    {
        mpz_t   a;
        mpz_t   d;
        mpz_t   r;
        int     i;
        int     want;
        
        a = new mpz_t();
        d = new mpz_t();
        r = new mpz_t();

        for (i = 0; i < reps; i++) {
            TestUtil.mpz_erandomb (a, rands, 1 << 19);
            TestUtil.mpz_erandomb_nonzero (d, rands, 1 << 18);

            GMP.mpz_fdiv_r (r, a, d);

            want = (GMP.mpz_sgn (r) == 0 ? 1 : 0);
            check_one (a, d, want);

            GMP.mpz_sub (a, a, r);
            check_one (a, d, 1);

            if (GMP.mpz_cmpabs_ui (d, 1L) == 0) continue;

            GMP.mpz_add_ui (a, a, 1L);
            check_one (a, d, 0);

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
        int ret = 0;
        int reps = 100;
        randstate_t rands;
        long seed;

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

            check_data();
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

    private void dump_abort(String msg, int want, int got, mpz_t a, long d)
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
        emsg = "ERROR: " + msg + " expected " + want + " got " + got + " a=" + a_str + " d=" + d;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, int want, int got, mpz_t a, mpz_t d)
        throws Exception
    {
        String a_str = "";
        String d_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            d_str = GMP.mpz_get_str(d, 10);
        }
        catch (GMPException e) {
            d_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " expected " + want + " got " + got + " a=" + a_str + " d=" + d_str;
        throw new Exception(emsg);
    }

}
