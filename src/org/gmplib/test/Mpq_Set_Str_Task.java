package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
//import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_Set_Str_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Set_Str_Task";
    
    public Mpq_Set_Str_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_one (mpq_t want, int base, String str)
        throws Exception
    {
        mpq_t   got;

        GMP.mpq_internal_CHECK_FORMAT (want);
        got = new mpq_t();

        GMP.mpq_set_str (got, str, base);
        GMP.mpq_internal_CHECK_FORMAT (got);

        if (GMP.mpq_equal (got, want) == 0) {
            /***
            printf ("mpq_set_str wrong\n");
            printf ("  base %d\n", base);
            printf ("  str  \"%s\"\n", str);
            mpq_trace ("got ", got);
            mpq_trace ("want", want);
            abort ();
            ***/
            dump_abort("mpq_set_str wrong", base, str, got, want);
        }

    }

    private void check_samples ()
        throws Exception
    {
        mpq_t  q;

        q = new mpq_t();

        GMP.mpq_set_ui (q, 0L, 1L);
        check_one (q, 10, "0");
        check_one (q, 10, "0/1");
        check_one (q, 10, "0  / 1");
        check_one (q, 0, "0x0/ 1");
        check_one (q, 0, "0x0/ 0x1");
        check_one (q, 0, "0 / 0x1");

        check_one (q, 10, "-0");
        check_one (q, 10, "-0/1");
        check_one (q, 10, "-0  / 1");
        check_one (q, 0, "-0x0/ 1");
        check_one (q, 0, "-0x0/ 0x1");
        check_one (q, 0, "-0 / 0x1");

        GMP.mpq_set_ui (q, 255L, 256L);
        check_one (q, 10, "255/256");
        check_one (q, 0,  "0xFF/0x100");
        check_one (q, 16, "FF/100");

        GMP.mpq_neg (q, q);
        check_one (q, 10, "-255/256");
        check_one (q, 0,  "-0xFF/0x100");
        check_one (q, 16, "-FF/100");

    }

    @Override
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
            
            check_samples ();
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

    private void dump_abort(String msg, int base, String str, mpq_t x, mpq_t y)
	throws Exception
    {
	String x_str = "";
	String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpq_get_str(x, base);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpq_get_str(y, base);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " base=" + base + " str=" + str + " got=" + x_str + " want=" + y_str;
        throw new Exception(emsg);
    }
}
