package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Divis_2Exp_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Divis_2Exp_Task";
    
    public Divis_2Exp_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static class CheckData
    {
        public CheckData(String a, long d, int want)
        {
            this.a = a;
            this.d = d;
            this.want = want;
        }
        public String a;
        public long   d;
        public int    want;
    }
    
    private static final CheckData[] data = new CheckData[] {
    new CheckData( "0", 0, 1 ),
    new CheckData( "0", 1, 1 ),
    new CheckData( "0", 2, 1 ),
    new CheckData( "0", 3, 1 ),

    new CheckData( "1", 0, 1 ),
    new CheckData( "1", 1, 0 ),
    new CheckData( "1", 2, 0 ),
    new CheckData( "1", 3, 0 ),
    new CheckData( "1", 10000, 0 ),

    new CheckData( "4", 0, 1 ),
    new CheckData( "4", 1, 1 ),
    new CheckData( "4", 2, 1 ),
    new CheckData( "4", 3, 0 ),
    new CheckData( "4", 4, 0 ),
    new CheckData( "4", 10000, 0 ),

    new CheckData( "0x80000000", 31, 1 ),
    new CheckData( "0x80000000", 32, 0 ),
    new CheckData( "0x80000000", 64, 0 ),

    new CheckData( "0x100000000", 32, 1 ),
    new CheckData( "0x100000000", 33, 0 ),
    new CheckData( "0x100000000", 64, 0 ),

    new CheckData( "0x8000000000000000", 63, 1 ),
    new CheckData( "0x8000000000000000", 64, 0 ),
    new CheckData( "0x8000000000000000", 128, 0 ),

    new CheckData( "0x10000000000000000", 64, 1 ),
    new CheckData( "0x10000000000000000", 65, 0 ),
    new CheckData( "0x10000000000000000", 128, 0 ),
    new CheckData( "0x10000000000000000", 256, 0 ),

    new CheckData( "0x10000000000000000100000000", 32, 1 ),
    new CheckData( "0x10000000000000000100000000", 33, 0 ),
    new CheckData( "0x10000000000000000100000000", 64, 0 ),

    new CheckData( "0x1000000000000000010000000000000000", 64, 1 ),
    new CheckData( "0x1000000000000000010000000000000000", 65, 0 ),
    new CheckData( "0x1000000000000000010000000000000000", 128, 0 ),
    new CheckData( "0x1000000000000000010000000000000000", 256, 0 ),
    new CheckData( "0x1000000000000000010000000000000000", 1024, 0 )
    };

    private void check_one (mpz_t a, long d, int want)
        throws Exception
    {
        int   got;

        got = (GMP.mpz_divisible_2exp_p (a, d) != 0 ? 1 : 0);
        if (want != got) {
            dump_abort("mpz_divisible_2exp_p wrong", want, got, a, d);
            /***
            printf ("mpz_divisible_2exp_p wrong\n");
            printf ("   expected %d got %d\n", want, got);
            mpz_trace ("   a", a);
            printf    ("   d=%lu\n", d);
            mp_trace_base = -16;
            mpz_trace ("   a", a);
            printf    ("   d=0x%lX\n", d);
            abort ();
            ***/
        }
    }

    private void check_data()
        throws Exception
    {
        mpz_t  a = new mpz_t();
        int i;

        for (i = 0; i < data.length; i++) {

            GMP.mpz_set_str (a, data[i].a, 0);

            check_one (a, data[i].d, data[i].want);

            GMP.mpz_neg (a, a);
            check_one (a, data[i].d, data[i].want);
        }

    }

    public void run()
    {
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            check_data();
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

}
