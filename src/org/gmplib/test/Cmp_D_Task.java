package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Cmp_D_Task extends TaskBase implements Runnable {

    private static final String TAG = "Cmp_D_Task";
    
    public Cmp_D_Task(UI ui)
    {
        super(ui, TAG);
    }


    private static class CheckData
    {
        public String x;
        public double y;
        public int    cmp;
        public int    cmpabs;

        public CheckData(String x, double y, int cmp, int cmpabs)
        {
            this.x = x;
            this.y = y;
            this.cmp = cmp;
            this.cmpabs = cmpabs;
        }
    }

    private static int SGN(int x) {      return((x) < 0 ? -1 : (x) == 0 ? 0 : 1); }

    private void check_one (String name, mpz_t x, double y, int cmp, int cmpabs)
        throws Exception
    {
        int   got;

        got = GMP.mpz_cmp_d (x, y);
        if (SGN(got) != cmp) {
            dump_abort    ("mpz_cmp_d wrong (from " + name + ")", x, y, cmp, got);
            /***
            int i;
            printf    ("mpz_cmp_d wrong (from %s)\n", name);
            printf    ("  got  %d\n", got);
            printf    ("  want %d\n", cmp);
            mpz_trace ("  x", x);
            printf    ("  y %g\n", y);
            mp_trace_base=-16;
            mpz_trace ("  x", x);
            printf    ("  y %g\n", y);
            printf    ("  y");
            for (i = 0; i < sizeof(y); i++)
                printf (" %02X", (unsigned) ((unsigned char *) &y)[i]);
            printf ("\n");
            abort ();
            ***/
        }

        got = GMP.mpz_cmpabs_d (x, y);
        if (SGN(got) != cmpabs) {
            dump_abort    ("mpz_cmpabs_d wrong (from " + name + ")", x, y, cmpabs, got);
            /***
            printf    ("mpz_cmpabs_d wrong\n");
            printf    ("  got  %d\n", got);
            printf    ("  want %d\n", cmpabs);
            mpz_trace ("  x", x);
            printf    ("  y %g\n", y);
            mp_trace_base=-16;
            mpz_trace ("  x", x);
            printf    ("  y %g\n", y);
            printf    ("  y");
            for (i = 0; i < sizeof(y); i++)
                printf (" %02X", (unsigned) ((unsigned char *) &y)[i]);
            printf ("\n");
            abort ();
            ***/
        }
    }

    private static final CheckData[] data = {

    new CheckData(  "0",  0.0,  0,  0 ),

    new CheckData(  "1",  0.0,  1,  1 ),
    new CheckData( "-1",  0.0, -1,  1 ),

    new CheckData(  "1",  0.5,  1,  1 ),
    new CheckData( "-1", -0.5, -1,  1 ),

    new CheckData(  "0",  1.0, -1, -1 ),
    new CheckData(  "0", -1.0,  1, -1 ),

    new CheckData(  "0x1000000000000000000000000000000000000000000000000", 1.0,  1, 1 ),
    new CheckData( "-0x1000000000000000000000000000000000000000000000000", 1.0, -1, 1 ),

    new CheckData(  "0",  1e100, -1, -1 ),
    new CheckData(  "0", -1e100,  1, -1 ),

    new CheckData(  "2",  1.5,   1,  1 ),
    new CheckData(  "2", -1.5,   1,  1 ),
    new CheckData( "-2",  1.5,  -1,  1 ),
    new CheckData( "-2", -1.5,  -1,  1 )
    };

    private void check_data()
        throws Exception
    {
        mpz_t   x;
        int     i;

        x = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (x, data[i].x, 0);
            check_one ("check_data", x, data[i].y, data[i].cmp, data[i].cmpabs);
        }
    }

    /* Equality of integers with up to 53 bits */
    private void check_onebits ()
        throws Exception
    {
        mpz_t   x;
        mpz_t   x2;
        double  y;
        int     i;

        x = new mpz_t();
        x2 = new mpz_t();
        GMP.mpz_set_ui (x, 0L);

        for (i = 0; i < 512; i++) {
            GMP.mpz_mul_2exp (x, x, 1);
            GMP.mpz_add_ui (x, x, 1L);

            y = GMP.mpz_get_d (x);
            GMP.mpz_set_d (x2, y);

            /* stop if any truncation is occurring */
            if (GMP.mpz_cmp (x, x2) != 0) break;

            check_one ("check_onebits", x, y, 0, 0);
            check_one ("check_onebits", x, -y, 1, 0);
            GMP.mpz_neg (x, x);
            check_one ("check_onebits", x, y, -1, 0);
            check_one ("check_onebits", x, -y, 0, 0);
            GMP.mpz_neg (x, x);
        }

    }

    /* With the mpz differing by 1, in a limb position possibly below the double */
    private void check_low_z_one ()
        throws Exception
    {
        mpz_t          x;
        double         y;
        int  i;

        x = new mpz_t();

        for (i = 1; i < 512; i++) {
            GMP.mpz_set_ui (x, 1L);
            GMP.mpz_mul_2exp (x, x, i);
            y = GMP.mpz_get_d (x);

            check_one ("check_low_z_one", x, y,   0, 0);
            check_one ("check_low_z_one", x, -y,  1, 0);
            GMP.mpz_neg (x, x);
            check_one ("check_low_z_one", x, y,  -1, 0);
            check_one ("check_low_z_one", x, -y,  0, 0);
            GMP.mpz_neg (x, x);

            GMP.mpz_sub_ui (x, x, 1L);

            check_one ("check_low_z_one", x, y,  -1, -1);
            check_one ("check_low_z_one", x, -y,  1, -1);
            GMP.mpz_neg (x, x);
            check_one ("check_low_z_one", x, y,  -1, -1);
            check_one ("check_low_z_one", x, -y,  1, -1);
            GMP.mpz_neg (x, x);

            GMP.mpz_add_ui (x, x, 2L);

            check_one ("check_low_z_one", x, y,   1, 1);
            check_one ("check_low_z_one", x, -y,  1, 1);
            GMP.mpz_neg (x, x);
            check_one ("check_low_z_one", x, y,  -1, 1);
            check_one ("check_low_z_one", x, -y, -1, 1);
            GMP.mpz_neg (x, x);
        }

    }

    /* Comparing 1 and 1+2^-n.  "y" is volatile to make gcc store and fetch it,
       which forces it to a 64-bit double, whereas on x86 it would otherwise
       remain on the float stack as an 80-bit long double.  */
    private void check_one_2exp ()
        throws Exception
    {
        double           e;
        mpz_t            x;
        double           y;
        //volatile double  y;
        int              i;

        x = new mpz_t();

        e = 1.0;
        for (i = 0; i < 128; i++) {
            e /= 2.0;
            y = 1.0 + e;
            if (y == 1.0) break;

            GMP.mpz_set_ui (x, 1L);
            check_one ("check_one_2exp", x,  y, -1, -1);
            check_one ("check_one_2exp", x, -y,  1, -1);

            GMP.mpz_set_si (x, -1);
            check_one ("check_one_2exp", x,  y, -1, -1);
            check_one ("check_one_2exp", x, -y,  1, -1);
        }

    }

    private void check_infinity ()
        throws Exception
    {
        mpz_t   x;
        double  y = Double.POSITIVE_INFINITY; // tests_infinity_d ();
        if (y == 0.0) return;

        x = new mpz_t();

        /* 0 cmp inf */
        GMP.mpz_set_ui (x, 0L);
        check_one ("check_infinity", x,  y, -1, -1);
        check_one ("check_infinity", x, -y,  1, -1);

        /* 123 cmp inf */
        GMP.mpz_set_ui (x, 123L);
        check_one ("check_infinity", x,  y, -1, -1);
        check_one ("check_infinity", x, -y,  1, -1);

        /* -123 cmp inf */
        GMP.mpz_set_si (x, -123);
        check_one ("check_infinity", x,  y, -1, -1);
        check_one ("check_infinity", x, -y,  1, -1);

        /* 2^5000 cmp inf */
        GMP.mpz_set_ui (x, 1L);
        GMP.mpz_mul_2exp (x, x, 5000L);
        check_one ("check_infinity", x,  y, -1, -1);
        check_one ("check_infinity", x, -y,  1, -1);

        /* -2^5000 cmp inf */
        GMP.mpz_neg (x, x);
        check_one ("check_infinity", x,  y, -1, -1);
        check_one ("check_infinity", x, -y,  1, -1);

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
            check_onebits ();
            check_low_z_one ();
            check_one_2exp ();
            check_infinity ();
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

    private void dump_abort(String msg, mpz_t x, double y, int want, int got)
        throws Exception
    {
        String a_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " x=" + a_str + " y=" + y +
                   " expected " + want + " got " + got;
        throw new Exception(emsg);
    }

}
