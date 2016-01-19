package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Cmp_SI_Task extends TaskBase implements Runnable {

    private static final String TAG = "Cmp_SI_Task";
    
    public Cmp_SI_Task(UI ui)
    {
        super(ui, TAG);
    }


    private static class CheckData
    {
        public String a;
        public String b;
        public int    want;

        public CheckData(String a, String b, int want)
        {
            this.a = a;
            this.b = b;
            this.want = want;
        }
    }

    private static int SGN(int x) {      return((x) < 0 ? -1 : (x) == 0 ? 0 : 1); }

    private static final CheckData[] data = {

    new CheckData( "0",  "1", -1 ),
    new CheckData( "0",  "0",  0 ),
    new CheckData( "0", "-1",  1 ),

    new CheckData( "1",  "1", 0 ),
    new CheckData( "1",  "0", 1 ),
    new CheckData( "1", "-1", 1 ),

    new CheckData( "-1",  "1", -1 ),
    new CheckData( "-1",  "0", -1 ),
    new CheckData( "-1", "-1", 0 ),

    new CheckData(           "0", "-0x80000000",  1 ),
    new CheckData(  "0x80000000", "-0x80000000",  1 ),
    new CheckData(  "0x80000001", "-0x80000000",  1 ),
    new CheckData( "-0x80000000", "-0x80000000",  0 ),
    new CheckData( "-0x80000001", "-0x80000000", -1 ),

    new CheckData(                   "0", "-0x8000000000000000",  1 ),
    new CheckData(  "0x8000000000000000", "-0x8000000000000000",  1 ),
    new CheckData(  "0x8000000000000001", "-0x8000000000000000",  1 ),
    new CheckData( "-0x8000000000000000", "-0x8000000000000000",  0 ),
    new CheckData( "-0x8000000000000001", "-0x8000000000000000", -1 )
    };

    private void check_data()
        throws Exception
    {
        mpz_t   a;
        mpz_t   bz;
        int     b;
        int     got;
        int     i;

        a = new mpz_t();
        bz = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (a, data[i].a, 0);
            GMP.mpz_set_str (bz, data[i].b, 0);
            if (GMP.mpz_fits_slong_p (bz) != 0) {
                b = GMP.mpz_get_si (bz);
                got = GMP.mpz_cmp_si (a, b);
                if (SGN (got) != data[i].want) {
                    dump_abort ("mpz_cmp_si wrong on data[" + i + "]", a, b, data[i].want, got);
                    /***
                    printf ("mpz_cmp_si wrong on data[%d]\n", i);
                    printf ("  a="); mpz_out_str (stdout, 10, a); printf ("\n");
                    printf ("  b=%ld\n", b);
                    printf ("  got=%d\n", got);
                    printf ("  want=%d\n", data[i].want);
                    abort();
                    ***/
                }
            }
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
            
            Log.d(TAG, "no randomness");

            check_data ();
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

    private void dump_abort(String msg, mpz_t a, int b, int want, int got)
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
        emsg = "ERROR: " + msg + " a=" + a_str + " b=" + b +
                   " expected " + want + " got " + got;
        throw new Exception(emsg);
    }

}
