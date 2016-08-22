package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.mpf_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.MutableInteger;

public class Set_F_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Set_F_Task";
    
    public Set_F_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final int[] shift = new int[] {
        0, 1, GMP.GMP_LIMB_BITS(), 2*GMP.GMP_LIMB_BITS(), 5*GMP.GMP_LIMB_BITS()
    };

    private void check_one (mpz_t z)
        throws Exception
    {
        int    sh;
        int    shneg;
        int    neg;
        mpf_t  f = new mpf_t(GMP.mpz_sizeinbase(z, 2));
        mpz_t  got = new mpz_t();
        mpz_t  want = new mpz_t();

        for (sh = 0; sh < shift.length; sh++) {
            for (shneg = 0; shneg <= 1; shneg++) {
                for (neg = 0; neg <= 1; neg++) {
                    GMP.mpf_set_z (f, z);
                    GMP.mpz_set (want, z);

                    if (neg != 0) {
                        GMP.mpf_neg (f, f);
                        GMP.mpz_neg (want, want);
                    }

                    if (shneg != 0) {
                        GMP.mpz_tdiv_q_2exp (want, want, shift[sh]);
                        GMP.mpf_div_2exp (f, f, shift[sh]);
                    } else {
                        GMP.mpz_mul_2exp (want, want, shift[sh]);
                        GMP.mpf_mul_2exp (f, f, shift[sh]);
                    }

                    GMP.mpz_set_f (got, f);
                    GMP.mpz_internal_CHECK_FORMAT (got);

                    if (GMP.mpz_cmp (got, want) != 0) {
                        dump_abort("wrong result",
                                   shneg != 0 ? -shift[sh] : shift[sh],
                                   neg,
                                   f, got, want);
                        //printf ("wrong result\n");
                        //printf ("  shift  %d\n", shneg ? -shift[sh] : shift[sh]);
                        //printf ("  neg    %d\n", neg);
                        //mpf_trace ("     f", f);
                        //mpz_trace ("   got", got);
                        //mpz_trace ("  want", want);
                        //abort ();
                    }
                }
            }
        }

    }

    public void run()
    {
        mpz_t z;
        randstate_t rands;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            z = new mpz_t();
            //tests_start ();
            
            seed = uinterface.getSeed();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            String s = "seed=" + seed;
            Log.d(TAG, s);
            uinterface.display(s);
            rands = new randstate_t(seed);

            GMP.mpz_set_ui (z, 0L);
            check_one (z);

            GMP.mpz_set_si (z, 123);
            check_one (z);

            GMP.mpz_rrandomb (z, rands, 2*GMP.GMP_LIMB_BITS());
            check_one (z);

            GMP.mpz_rrandomb (z, rands, 5*GMP.GMP_LIMB_BITS());
            check_one (z);
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

    private void dump_abort(String msg, int shift, int neg, mpf_t f, mpz_t got, mpz_t want)
        throws Exception
    {
        String f_str = "";
        String got_str = "";
        String want_str = "";
        String emsg;
        MutableInteger exp = new MutableInteger(0);
        try {
            f_str = GMP.mpf_get_str(exp, 10, 0, f);
            f_str += "E";
            f_str += exp.value;
        }
        catch (GMPException e) {
            f_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
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
        emsg = "ERROR: " + msg + " shift=" + shift + " neg=" + neg +
               " f=" + f_str + " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }
}
