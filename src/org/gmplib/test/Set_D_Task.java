package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Set_D_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Set_D_Task";
    
    public Set_D_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_data ()
        throws Exception
    {
        mpz_t  z = new mpz_t();

        GMP.mpz_set_d (z, 0.0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, 1.0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 1 || GMP.refmpn_cmp_allowzero(z, new long[] {1}, 1) != 0) {
            dump_abort2("mpz_set_d wrong on data", 1.0, GMP.mpz_internal_SIZ(z), 1L, z, new long[] {1});
        }
        GMP.mpz_set_d (z, -1.0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != -1 || GMP.refmpn_cmp_allowzero(z, new long[] {1}, 1) != 0) {
            dump_abort2("mpz_set_d wrong on data", -1.0, GMP.mpz_internal_SIZ(z), -1L, z, new long[] {1});
        }
        GMP.mpz_set_d (z, 123.0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 1 || GMP.refmpn_cmp_allowzero(z, new long[] {123}, 1) != 0) {
            dump_abort2("mpz_set_d wrong on data", 1.0, GMP.mpz_internal_SIZ(z), 1L, z, new long[] {123});
        }
        GMP.mpz_set_d (z, -123.0);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != -1 || GMP.refmpn_cmp_allowzero(z, new long[] {123}, 1) != 0) {
            dump_abort2("mpz_set_d wrong on data", -123.0, GMP.mpz_internal_SIZ(z), -1L, z, new long[] {123});
        }
        GMP.mpz_set_d (z, 1e-1);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, -1e-1);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, 2.328306436538696e-10);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, -2.328306436538696e-10);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, 5.421010862427522e-20);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, -5.421010862427522e-20);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, 2.938735877055719e-39);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
        GMP.mpz_set_d (z, -2.938735877055719e-39);
        GMP.mpz_internal_CHECK_FORMAT (z);
        if (GMP.mpz_internal_SIZ(z) != 0) {
            dump_abort2("mpz_set_d wrong on data", 0.0, GMP.mpz_internal_SIZ(z), 0, z, new long[] {0});
        }
    }

    /* Try mpz_set_d on values 2^i+1, while such a value fits a double. */
    private void check_2n_plus_1 ()
        throws Exception
    {
        double  p;
        double d;
        double diff;
        mpz_t want;
        mpz_t got;
        int    i;

        want = new mpz_t();
        got = new mpz_t();

        p = 1.0;
        GMP.mpz_set_ui (want, 2L);  /* gives 3 on first step */

        for (i = 1; i < 500; i++) {
            GMP.mpz_mul_2exp (want, want, 1L);
            GMP.mpz_sub_ui (want, want, 1L);   /* want = 2^i+1 */

            p *= 2.0;  /* p = 2^i */
            d = p + 1.0;
            diff = d - p;
            if (diff != 1.0) {
                break;   /* rounding occurred, stop now */
            }

            GMP.mpz_set_d (got, d);
            GMP.mpz_internal_CHECK_FORMAT (got);
            if (GMP.mpz_cmp (got, want) != 0) {
                //printf ("mpz_set_d wrong on 2^%d+1\n", i);
                //d_trace   ("  d ", d);
                //mpz_trace ("  got  ", got);
                //mpz_trace ("  want ", want);
                //abort ();
                dump_abort("mpz_set_d wrong on 2^" + i + "+1", d, got, want);
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
            
            check_data ();
            check_2n_plus_1 ();
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

    private void dump_abort(String msg, double d, mpz_t got, mpz_t want)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " d=" + d +
               " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }
    
    private void dump_abort2(String msg, double d, long gotsize, long wantsize, mpz_t got, long[] want)
        throws Exception
    {
        String got_str = "";
        String want_str = "";
        String emsg;
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = "";
            for (int i = 0; i < want.length; i++) {
                if (i > 0) want_str += ", ";
                want_str += Long.toString(want[i]);
            }
        }
        catch (Exception e) {
        }
        emsg = "ERROR: " + msg + " d=" + d +
           " gotsize=" + gotsize + " wantsize=" + wantsize +
           " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }
}
