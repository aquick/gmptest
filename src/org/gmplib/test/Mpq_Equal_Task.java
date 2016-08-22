package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_Equal_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Equal_Task";
    
    public Mpq_Equal_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static void SET4Z(mpz_t z, int size, long l3, long l2, long l1, long l0)
        throws GMPException
    {
	GMP.mpz_internal_SETSIZ(z, size);
	GMP.mpz_internal_set_ulimb(z, 3, l3);
	GMP.mpz_internal_set_ulimb(z, 2, l2);
	GMP.mpz_internal_set_ulimb(z, 1, l1);
	GMP.mpz_internal_set_ulimb(z, 0, l0);
    }

    private static void SET4(mpq_t q, mpz_t qnum, mpz_t qden, int nsize, long n3, long n2, long n1, long n0, int dsize, long d3, long d2, long d1, long d0)
        throws GMPException
    {
	SET4Z (qnum, nsize,n3,n2,n1,n0);
	SET4Z (qden, dsize,d3,d2,d1,d0);
        GMP.mpq_set_num(q, qnum);
        GMP.mpq_set_den(q, qden);
    }

    private void check_one (mpq_t x, mpq_t y, int want)
        throws Exception
    {
        int  got;

        GMP.mpq_internal_CHECK_FORMAT(x);
        GMP.mpq_internal_CHECK_FORMAT(y);

        got = GMP.mpq_equal (x, y);
        if ((got != 0) != (want != 0)) {
            /***
            printf ("mpq_equal got %d want %d\n", got, want);
            mpq_trace ("x", x);
            mpq_trace ("y", y);
            abort ();
            ***/
            dump_abort("mpq_equal", got, want, x, y);
        }
    }

    private void check_all (mpq_t x, mpq_t y, int want)
        throws Exception
    {
        check_one (x, y, want);
        check_one (y, x, want);

        GMP.mpq_neg (x, x);
        GMP.mpq_neg (y, y);

        check_one (x, y, want);
        check_one (y, x, want);
    }

    /* Exercise various combinations of same and slightly different values. */

    private void check_various ()
        throws Exception
    {
        mpq_t  x;
        mpq_t  y;
        mpz_t  xnum;
        mpz_t  xden;
        mpz_t  ynum;
        mpz_t  yden;

        x = new mpq_t();
        y = new mpq_t();
        xnum = new mpz_t();
        xden = new mpz_t();
        ynum = new mpz_t();
        yden = new mpz_t();

        GMP.mpz_internal_REALLOC(xnum, 20);
        GMP.mpz_internal_REALLOC(xden, 20);
        GMP.mpz_internal_REALLOC(ynum, 20);
        GMP.mpz_internal_REALLOC(yden, 20);

        /* 0 == 0 */
        SET4 (x, xnum, xden, 0,13,12,11,10, 1,23,22,21,1);
        SET4 (y, ynum, yden, 0,33,32,31,30, 1,43,42,41,1);
        check_all (x, y, 1);

        /* 83/99 == 83/99 */
        SET4 (x, xnum, xden, 1,13,12,11,83, 1,23,22,21,99);
        SET4 (y, ynum, yden, 1,33,32,31,83, 1,43,42,41,99);
        check_all (x, y, 1);

        /* 1:2:3:4/5:6:7 == 1:2:3:4/5:6:7 */
        SET4 (x, xnum, xden, 4,1,2,3,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 1);

        /* various individual changes making != */
        SET4 (x, xnum, xden, 4,1,2,3,667, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 4,1,2,666,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 4,1,666,3,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        if (GMP.GMP_NUMB_BITS() != 62) {
            SET4 (x, xnum, xden, 4,667,2,3,4, 3,88,5,6,7);
            SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
            check_all (x, y, 0);
        }
        SET4 (x, xnum, xden, 4,1,2,3,4, 3,88,5,6,667);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 4,1,2,3,4, 3,88,5,667,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 4,1,2,3,4, 3,88,666,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, -4,1,2,3,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 1,1,2,3,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 3,99,5,6,7);
        check_all (x, y, 0);
        SET4 (x, xnum, xden, 4,1,2,3,4, 3,88,5,6,7);
        SET4 (y, ynum, yden, 4,1,2,3,4, 2,99,5,6,7);
        check_all (x, y, 0);
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

    private void dump_abort(String msg, int got, int want, mpq_t x, mpq_t y)
	throws Exception
    {
	String x_str = "";
	String y_str = "";
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
        emsg = "ERROR: " + msg + " got=" + got + " want=" + want + " x=" + x_str + " y=" + y_str;
        throw new Exception(emsg);
    }
}
