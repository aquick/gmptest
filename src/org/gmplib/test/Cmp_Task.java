package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Cmp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Cmp_Task";
    
    public Cmp_Task(UI ui)
    {
        super(ui, TAG);
    }


    /* Nothing sophisticated here, just exercise some combinations of sizes and
       signs.  */


    private void check_one (mpz_t x, mpz_t y, int want_cmp, int want_cmpabs)
        throws Exception
    {
        int  got;

        got = GMP.mpz_cmp (x, y);
        if ((   got <  0) != (want_cmp <  0)
            || (got == 0) != (want_cmp == 0)
            || (got >  0) != (want_cmp >  0)) {
            dump_abort("mpz_cmp", x, y, want_cmp, got);
            /***
            printf ("mpz_cmp got %d want %d\n", got, want_cmp);
            mpz_trace ("x", x);
            mpz_trace ("y", y);
            abort ();
            ***/
        }

        got = GMP.mpz_cmpabs (x, y);
        if ((   got <  0) != (want_cmpabs <  0)
            || (got == 0) != (want_cmpabs == 0)
            || (got >  0) != (want_cmpabs >  0)) {
            dump_abort("mpz_cmpabs", x, y, want_cmpabs, got);
            /***
            printf ("mpz_cmpabs got %d want %d\n", got, want_cmpabs);
            mpz_trace ("x", x);
            mpz_trace ("y", y);
            abort ();
            ***/
        }
    }


    private void check_all (mpz_t x, mpz_t y, int want_cmp, int want_cmpabs)
        throws Exception
    {
        check_one (x, y,  want_cmp,  want_cmpabs);
        check_one (y, x, -want_cmp, -want_cmpabs);

        GMP.mpz_neg (x, x);
        GMP.mpz_neg (y, y);
        want_cmp = -want_cmp;

        check_one (x, y,  want_cmp,  want_cmpabs);
        check_one (y, x, -want_cmp, -want_cmpabs);
    }

    private static void SET1(mpz_t z, int size, long n)
        throws GMPException
    {
        GMP.mpz_internal_SETSIZ(z, size);
        GMP.mpz_internal_set_ulimb(z, 0, n);
    }

    private static void SET2(mpz_t z, int size, long n1, long n0)
        throws GMPException
    {
        GMP.mpz_internal_SETSIZ(z, size);
        GMP.mpz_internal_set_ulimb(z, 0, n0);
        GMP.mpz_internal_set_ulimb(z, 1, n1);
    }

    private static void SET4(mpz_t z, int size, long n3, long n2, long n1, long n0)
        throws GMPException
    {
        GMP.mpz_internal_SETSIZ(z, size);
        GMP.mpz_internal_set_ulimb(z, 0, n0);
        GMP.mpz_internal_set_ulimb(z, 1, n1);
        GMP.mpz_internal_set_ulimb(z, 2, n2);
        GMP.mpz_internal_set_ulimb(z, 3, n3);
    }

    private void check_various ()
        throws Exception
    {
        mpz_t  x;
        mpz_t  y;

        x = new mpz_t();
        y = new mpz_t();

        GMP.mpz_internal_REALLOC (x, 20);
        GMP.mpz_internal_REALLOC (y, 20);

        /* 0 cmp 0, junk in low limbs */
        SET1 (x,0, 123);
        SET1 (y,0, 456);
        check_all (x, y, 0, 0);


        /* 123 cmp 0 */
        SET1 (x,1, 123);
        SET1 (y,0, 456);
        check_all (x, y, 1, 1);

        /* 123:456 cmp 0 */
        SET2 (x,2, 456,123);
        SET1 (y,0, 9999);
        check_all (x, y, 1, 1);


        /* 123 cmp 123 */
        SET1(x,1, 123);
        SET1(y,1, 123);
        check_all (x, y, 0, 0);

        /* -123 cmp 123 */
        SET1(x,-1, 123);
        SET1(y,1,  123);
        check_all (x, y, -1, 0);


        /* 123 cmp 456 */
        SET1(x,1, 123);
        SET1(y,1, 456);
        check_all (x, y, -1, -1);

        /* -123 cmp 456 */
        SET1(x,-1, 123);
        SET1(y,1,  456);
        check_all (x, y, -1, -1);

        /* 123 cmp -456 */
        SET1(x,1,  123);
        SET1(y,-1, 456);
        check_all (x, y, 1, -1);


        /* 1:0 cmp 1:0 */
        SET2 (x,2, 1,0);
        SET2 (y,2, 1,0);
        check_all (x, y, 0, 0);

        /* -1:0 cmp 1:0 */
        SET2 (x,-2, 1,0);
        SET2 (y,2,  1,0);
        check_all (x, y, -1, 0);


        /* 2:0 cmp 1:0 */
        SET2 (x,2, 2,0);
        SET2 (y,2, 1,0);
        check_all (x, y, 1, 1);


        /* 4:3:2:1 cmp 2:1 */
        SET4 (x,4, 4,3,2,1);
        SET2 (y,2, 2,1);
        check_all (x, y, 1, 1);

        /* -4:3:2:1 cmp 2:1 */
        SET4 (x,-4, 4,3,2,1);
        SET2 (y,2,  2,1);
        check_all (x, y, -1, 1);

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

            check_various ();
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

    private void dump_abort(String msg, mpz_t x, mpz_t y, int want, int got)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " got " + got + " want " + want + " x=" + x_str + " y=" + y_str;
        throw new Exception(emsg);
    }

}
