package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.MutableInteger;

public class Get_D_2Exp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Get_D_2Exp_Task";
    
    public Get_D_2Exp_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_zero ()
        throws Exception
    {
        mpz_t   z = new mpz_t();
        double  got;
        double  want;
        MutableInteger got_exp = new MutableInteger(0);
        int     want_exp;

        GMP.mpz_set_ui (z, 0);

        want = 0.0;
        want_exp = 0;
        got = GMP.mpz_get_d_2exp (got_exp, z);
        if (got != want || got_exp.value != want_exp) {
            dump_abort2 ("mpz_get_d_2exp wrong on zero", z, want, got, want_exp, got_exp.value);
            /***
            printf    ("mpz_get_d_2exp wrong on zero\n");
            mpz_trace ("   z    ", z);
            d_trace   ("   want ", want);
            d_trace   ("   got  ", got);
            printf    ("   want exp %ld\n", want_exp);
            printf    ("   got exp  %ld\n", got_exp);
            abort();
            ***/
        }

    }

    private void check_onebit ()
        throws Exception
    {
        int[] data = new int[] {
            1, 32, 52, 53, 54, 63, 64, 65, 128, 256, 511, 512, 513
        };
        mpz_t   z;
        double  got;
        double  want;
        MutableInteger got_exp = new MutableInteger(0);
        int     want_exp;
        int     i;

        z = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_ui (z, 1L);
            GMP.mpz_mul_2exp (z, z, data[i]);
            want = 0.5;
            want_exp = data[i] + 1;
            got = GMP.mpz_get_d_2exp (got_exp, z);
            if (got != want || got_exp.value != want_exp) {
                dump_abort2 ("mpz_get_d_2exp wrong on 2**" + data[i], z, want, got, want_exp, got_exp.value);
                /***
                printf    ("mpz_get_d_2exp wrong on 2**%ld\n", data[i]);
                mpz_trace ("   z    ", z);
                d_trace   ("   want ", want);
                d_trace   ("   got  ", got);
                printf    ("   want exp %ld\n", want_exp);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
                ***/
            }

            GMP.mpz_set_si (z, -1);
            GMP.mpz_mul_2exp (z, z, data[i]);
            want = -0.5;
            want_exp = data[i] + 1;
            got = GMP.mpz_get_d_2exp (got_exp, z);
            if (got != want || got_exp.value != want_exp) {
                dump_abort2 ("mpz_get_d_2exp wrong on -2**" + data[i], z, want, got, want_exp, got_exp.value);
                /***
                printf    ("mpz_get_d_2exp wrong on -2**%ld\n", data[i]);
                mpz_trace ("   z    ", z);
                d_trace   ("   want ", want);
                d_trace   ("   got  ", got);
                printf    ("   want exp %ld\n", want_exp);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
                ***/
            }
        }
    }

    /* Check that hardware rounding doesn't make mpz_get_d_2exp return a value
       outside its defined range. */
    private void check_round ()
        throws Exception
    {
        int[] data = new int[] { 1, 32, 53, 54, 64, 128, 256, 512 };
        mpz_t   z;
        double  got;
        MutableInteger got_exp = new MutableInteger(0);
        int     i; // rnd_mode, old_rnd_mode;

        z = new mpz_t();
        //old_rnd_mode = tests_hardware_getround (); // NYI for ARM

        //for (rnd_mode = 0; rnd_mode < 4; rnd_mode++) {
            //tests_hardware_setround (rnd_mode);

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_ui (z, 1L);
            GMP.mpz_mul_2exp (z, z, data[i]);
            GMP.mpz_sub_ui (z, z, 1L);

            got = GMP.mpz_get_d_2exp (got_exp, z);
            if (got < 0.5 || got >= 1.0) {
                dump_abort ("mpz_get_d_2exp wrong on 2**" + data[i] + "-1",
                    "result out of range, expect 0.5 <= got < 1.0",
                    z, got, got_exp.value);
                /***
                printf    ("mpz_get_d_2exp wrong on 2**%lu-1\n", data[i]);
                printf    ("result out of range, expect 0.5 <= got < 1.0\n");
                printf    ("   rnd_mode = %d\n", rnd_mode);
                printf    ("   data[i]  = %lu\n", data[i]);
                mpz_trace ("   z    ", z);
                d_trace   ("   got  ", got);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
                ***/
            }

            GMP.mpz_neg (z, z);
            got = GMP.mpz_get_d_2exp (got_exp, z);
            if (got <= -1.0 || got > -0.5) {
                dump_abort ("mpz_get_d_2exp wrong on -2**" + data[i] + "-1",
                    "result out of range, expect -1.0 < got <= -0.5",
                    z, got, got_exp.value);
                /***
                printf    ("mpz_get_d_2exp wrong on -2**%lu-1\n", data[i]);
                printf    ("result out of range, expect -1.0 < got <= -0.5\n");
                printf    ("   rnd_mode = %d\n", rnd_mode);
                printf    ("   data[i]  = %lu\n", data[i]);
                mpz_trace ("   z    ", z);
                d_trace   ("   got  ", got);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
                ***/
            }
        }
      //}

      //tests_hardware_setround (old_rnd_mode);
    }

    private void check_rand (randstate_t rands, int limit)
        throws Exception
    {
        int     i;
        mpz_t   z;
        double  got;
        MutableInteger got_exp = new MutableInteger(0);
        long    bits;

        z = new mpz_t();

        for (i = 0; i < limit; i++) {
            bits = GMP.gmp_urandomm_ui (rands, 512L);
            GMP.mpz_urandomb (z, rands, bits);

            got = GMP.mpz_get_d_2exp (got_exp, z);
            if (GMP.mpz_sgn (z) == 0) continue;
            bits = GMP.mpz_sizeinbase (z, 2);

            if (got < 0.5 || got >= 1.0) {
                dump_abort ("mpz_get_d_2exp out of range, expect 0.5 <= got < 1.0",
                    "", z, got, got_exp.value);
                /***
                printf    ("mpz_get_d_2exp out of range, expect 0.5 <= got < 1.0\n");
                mpz_trace ("   z    ", z);
                d_trace   ("   got  ", got);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
                ***/
            }

            /* FIXME: If mpz_get_d_2exp rounds upwards we might have got_exp ==
               bits+1, so leave this test disabled until we decide if that's what
               should happen, or not.  */
            /***
            if (got_exp != bits) {
                printf    ("mpz_get_d_2exp wrong exponent\n", i);
                mpz_trace ("   z    ", z);
                d_trace   ("   bits ", bits);
                d_trace   ("   got  ", got);
                printf    ("   got exp  %ld\n", got_exp);
                abort();
            }
            ***/
        }
    }

    public void run()
    {
        int limit = 200;
        randstate_t rands;
        long seed;
        int ret = 0;

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

            check_zero ();
            check_onebit ();
            check_round ();
            check_rand (rands, limit);
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

    private void dump_abort(String msg, String msg2, mpz_t z, double got, int got_exp)
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
        emsg = "ERROR: " + msg + " (" + msg2 + ")" + " z=" + z_str + " got=" + got +
                   " got_exp=" + got_exp;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, mpz_t z, double want, double got, int want_exp, int got_exp)
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
        emsg = "ERROR: " + msg + " z=" + z_str + " want=" + want + " got=" + got +
                   " want_exp=" + want_exp + " got_exp=" + got_exp;
        throw new Exception(emsg);
    }
}
