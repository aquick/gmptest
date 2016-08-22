package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Fits_Task extends TaskBase implements Runnable {

    private static final String TAG = "Fits_Task";
    
    public Fits_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
        mpz_t z;
        int   got;
        int   want;
        String expr;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            
            Log.d(TAG, "no randomness");
            z = new mpz_t();

            GMP.mpz_set_ui (z, 0L);
            expr = "0";
            want = 1;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, 1L);
            expr = "1";
            want = 1;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, -1);
            expr = "-1";
            want = 0;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, 1L);
            GMP.mpz_mul_2exp (z, z, 5*GMP.GMP_LIMB_BITS());
            expr = "2^(5*BPML)";
            want = 0;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, (long)0xFFFF);
            expr = "USHORT_MAX";
            want = 1;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, (long)0xFFFF);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "USHORT_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_ushort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ushort_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_ui (z, GMP.ULONG_MAX);
            expr = "UINT_MAX";
            want = 1;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, GMP.ULONG_MAX);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "UINT_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_uint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_uint_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_ui (z, GMP.ULONG_MAX);
            expr = "ULONG_MAX";
            want = 1;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_ui (z, GMP.ULONG_MAX);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "ULONG_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_ulong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_ulong_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, (int)Short.MAX_VALUE);
            expr = "SHORT_MAX";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, (int)Short.MAX_VALUE);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "SHORT_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, (int)Integer.MAX_VALUE);
            expr = "INT_MAX";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, (int)Integer.MAX_VALUE);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "INT_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, Integer.MAX_VALUE);
            expr = "LONG_MAX";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, Integer.MAX_VALUE);
            GMP.mpz_add_ui (z, z, 1L);
            expr = "LONG_MAX + 1";
            want = 0;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, (int)Short.MIN_VALUE);
            expr = "SHORT_MIN";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, (int)Short.MIN_VALUE);
            GMP.mpz_sub_ui (z, z, 1L);
            expr = "SHORT_MIN + 1";
            want = 0;
            got = GMP.mpz_fits_sshort_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sshort_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, (int)Integer.MIN_VALUE);
            expr = "INT_MIN";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, (int)Integer.MIN_VALUE);
            GMP.mpz_sub_ui (z, z, 1L);
            expr = "INT_MIN + 1";
            want = 0;
            got = GMP.mpz_fits_sint_p(z);
            if (got != want) {
                dump_abort("mpz_fits_sint_p(" + expr + ")", z, got, want);
            }


            GMP.mpz_set_si (z, Integer.MIN_VALUE);
            expr = "LONG_MIN";
            want = 1;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }

            GMP.mpz_set_si (z, Integer.MIN_VALUE);
            GMP.mpz_sub_ui (z, z, 1L);
            expr = "LONG_MIN + 1";
            want = 0;
            got = GMP.mpz_fits_slong_p(z);
            if (got != want) {
                dump_abort("mpz_fits_slong_p(" + expr + ")", z, got, want);
            }
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

    private void dump_abort(String msg, mpz_t z, int got, int want)
        throws Exception
    {
        String z_str = "";
        String emsg;
        try {
            z_str = GMP.mpz_get_str(z, 10);
        }
        catch (GMPException e) {
            z_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " z=" + z_str + " want=" + want + " got=" + got;
        throw new Exception(emsg);
    }
}
