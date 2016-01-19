package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Powm_UI_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Powm_UI_Task";
    
    public Powm_UI_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
        mpz_t base;
        mpz_t exp;
        mpz_t mod;
        mpz_t r1;
        mpz_t r2;
        mpz_t base2;
        long base_size;
        long exp_size;
        long mod_size;
        long exp2;
        int i;
        int reps = 100; // 100;
        randstate_t rands;
        mpz_t bs;
        long bsi;
        long size_range;
        long seed;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            base = new mpz_t();
            exp = new mpz_t();
            mod = new mpz_t();
            r1 = new mpz_t();
            r2 = new mpz_t();
            base2 = new mpz_t();
            bs = new mpz_t();
            //tests_start ();
            
            seed = uinterface.getSeed();
            if (seed < 0) {
                seed = 0x100000000L + seed;
            }
            String s = "seed=" + seed;
            Log.d(TAG, s);
            uinterface.display(s);
            rands = new randstate_t(seed);

            if (params.length > 0) {
                reps = params[0].intValue();
            }

            for (i = 0; i < reps; i++) {
                GMP.mpz_urandomb (bs, rands, 32);
                size_range = GMP.mpz_get_ui (bs) % 18 + 2;

                do  /* Loop until mathematically well-defined.  */ {
                    GMP.mpz_urandomb (bs, rands, size_range);
                    base_size = GMP.mpz_get_ui (bs);
                    GMP.mpz_rrandomb (base, rands, base_size);

                    GMP.mpz_urandomb (bs, rands, 6L);
                    exp_size = GMP.mpz_get_ui (bs);
                    GMP.mpz_rrandomb (exp, rands, exp_size);
                    exp2 = GMP.mpz_getlimbn (exp, 0);
                } while (GMP.mpz_cmp_ui (base, 0) == 0 && exp2 == 0);

                do {
                    GMP.mpz_urandomb (bs, rands, size_range);
                    mod_size = GMP.mpz_get_ui (bs);
                    GMP.mpz_rrandomb (mod, rands, mod_size);
                } while (GMP.mpz_cmp_ui (mod, 0) == 0);

                GMP.mpz_urandomb (bs, rands, 2);
                bsi = GMP.mpz_get_ui (bs);
                if ((bsi & 1) != 0) {
                    GMP.mpz_neg (base, base);
                }

                GMP.mpz_powm_ui (r1, base, exp2, mod);
                GMP.mpz_internal_CHECK_FORMAT (r1);

                GMP.mpz_set_ui (r2, 1);
                GMP.mpz_set (base2, base);

                GMP.mpz_mod (r2, r2, mod);	/* needed when exp==0 and mod==1 */
                while (exp2 != 0) {
                    if (exp2 % 2 != 0) {
                        GMP.mpz_mul (r2, r2, base2);
                        GMP.mpz_mod (r2, r2, mod);
                    }
                    GMP.mpz_mul (base2, base2, base2);
                    GMP.mpz_mod (base2, base2, mod);
                    exp2 = exp2 / 2;
                }

                if (GMP.mpz_cmp (r1, r2) != 0) {
                    dump_abort("test " + i + ": Incorrect results for operands:",
                               base, exp, mod, r1, r2);
                    /***
                    fprintf (stderr, "\ntest %d: Incorrect results for operands:\n", i);
                    debug_mp (base, -16);
                    debug_mp (exp, -16);
                    debug_mp (mod, -16);
                    fprintf (stderr, "mpz_powm_ui result:\n");
                    debug_mp (r1, -16);
                    fprintf (stderr, "reference result:\n");
                    debug_mp (r2, -16);
                    abort ();
                    ***/
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (i % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
                }
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

    private void dump_abort(String msg, mpz_t base, mpz_t exp, mpz_t mod, mpz_t r1, mpz_t r2)
        throws Exception
    {
        String base_str = "";
        String exp_str = "";
        String mod_str = "";
        String r1_str = "";
        String r2_str = "";
        String emsg;
        try {
            base_str = GMP.mpz_get_str(base, 10);
        }
        catch (GMPException e) {
            base_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            exp_str = GMP.mpz_get_str(exp, 10);
        }
        catch (GMPException e) {
            exp_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            mod_str = GMP.mpz_get_str(mod, 10);
        }
        catch (GMPException e) {
            mod_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            r1_str = GMP.mpz_get_str(r1, 10);
        }
        catch (GMPException e) {
            r1_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            r2_str = GMP.mpz_get_str(r2, 10);
        }
        catch (GMPException e) {
            r2_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " base=" + base_str + " exp=" + exp_str + " mod=" + mod_str +
                " mpz_powm_ui result=" + r1_str + " reference result=" + r2_str;
        throw new Exception(emsg);
    }
}
