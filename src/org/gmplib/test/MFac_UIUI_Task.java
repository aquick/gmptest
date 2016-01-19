package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class MFac_UIUI_Task extends TaskBase implements Runnable
{
    private static final String TAG = "MFac_UIUI_Task";
    
    public MFac_UIUI_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final int MULTIFAC_WHEEL = (2*3*11);
    private static final int MULTIFAC_WHEEL2 = (5*13);

    public void run()
    {
        mpz_t[] ref = new mpz_t[MULTIFAC_WHEEL];
        mpz_t[] ref2 = new mpz_t[MULTIFAC_WHEEL2];
        mpz_t res;
        long  n;
        long  j;
        int  m;
        int  m2;
        long  limit = 222; // 2222;
        long  step = 1;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            res = new mpz_t();
            //tests_start ();
            
            if (params.length > 0) {
                limit = (long)params[0].intValue();
            }

            /* for small limb testing */
            if (limit > GMP.ULONG_MAX) {
                limit = GMP.ULONG_MAX;
            }

            for (m = 0; m < MULTIFAC_WHEEL; m++) {
                ref[m] = new mpz_t();
                GMP.mpz_set_ui(ref[m], 1L);
            }
            for (m2 = 0; m2 < MULTIFAC_WHEEL2; m2++) {
                ref2[m2] = new mpz_t();
                GMP.mpz_set_ui(ref2[m2], 1L);
            }

            m = 0;
            m2 = 0;
            for (n = 0; n <= limit;) {
                GMP.mpz_mfac_uiui (res, n, MULTIFAC_WHEEL);
                GMP.mpz_internal_CHECK_FORMAT (res);
                if (GMP.mpz_cmp (ref[m], res) != 0) {
                    dump_abort ("mpz_mfac_uiui(" + n + "," + MULTIFAC_WHEEL + ") wrong",
                                    res, ref[m]);
                    /***
                    printf ("mpz_mfac_uiui(%lu,&i) wrong\n", n, MULTIFAC_WHEEL);
                    printf ("  got  "); mpz_out_str (stdout, 10, res); printf("\n");
                    printf ("  want "); mpz_out_str (stdout, 10, ref[m]); printf("\n");
                    abort ();
                    ***/
                }
                GMP.mpz_mfac_uiui (res, n, MULTIFAC_WHEEL2);
                GMP.mpz_internal_CHECK_FORMAT (res);
                if (GMP.mpz_cmp (ref2[m2], res) != 0) {
                    dump_abort ("mpz_mfac_uiui(" + n + "," + MULTIFAC_WHEEL2 + ") wrong",
                                    res, ref2[m2]);
                    /***
                    printf ("mpz_mfac_uiui(%lu,&i) wrong\n", n, MULTIFAC_WHEEL2);
                    printf ("  got  "); mpz_out_str (stdout, 10, res); printf("\n");
                    printf ("  want "); mpz_out_str (stdout, 10, ref2[m2]); printf("\n");
                    abort ();
                    ***/
                }
                if (n + step <= limit) {
                    for (j = 0; j < step; j++) {
                        n++; m++; m2++;
                        if (m >= MULTIFAC_WHEEL) m -= MULTIFAC_WHEEL;
                        if (m2 >= MULTIFAC_WHEEL2) m2 -= MULTIFAC_WHEEL2;
                        GMP.mpz_mul_ui (ref[m], ref[m], n); /* Compute a reference, with current library */
                        GMP.mpz_mul_ui (ref2[m2], ref2[m2], n); /* Compute a reference, with current library */
                    }
                } else {
                    n += step;
                }
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                if (n % 10 == 0) {
                    onProgressUpdate(Integer.valueOf((int)((float)(n+1)*100.0/(float)limit)));
                }
            }
            GMP.mpz_fac_ui (ref[0], n);
            GMP.mpz_mfac_uiui (res, n, 1);
            GMP.mpz_internal_CHECK_FORMAT (res);
            if (GMP.mpz_cmp (ref[0], res) != 0) {
                dump_abort ("mpz_mfac_uiui(" + n + ",1) wrong", res, ref[0]);
                /***
                printf ("mpz_mfac_uiui(%lu,1) wrong\n", n);
                printf ("  got  "); mpz_out_str (stdout, 10, res); printf("\n");
                printf ("  want "); mpz_out_str (stdout, 10, ref[0]); printf("\n");
                abort ();
                ***/
            }

            GMP.mpz_2fac_ui (ref[0], n);
            GMP.mpz_mfac_uiui (res, n, 2);
            GMP.mpz_internal_CHECK_FORMAT (res);
            if (GMP.mpz_cmp (ref[0], res) != 0) {
                dump_abort ("mpz_mfac_uiui(" + n + ",1) wrong", res, ref[0]);
                /***
                printf ("mpz_mfac_uiui(%lu,1) wrong\n", n);
                printf ("  got  "); mpz_out_str (stdout, 10, res); printf("\n");
                printf ("  want "); mpz_out_str (stdout, 10, ref[0]); printf("\n");
                abort ();
                ***/
            }

            n++;
            GMP.mpz_2fac_ui (ref[0], n);
            GMP.mpz_mfac_uiui (res, n, 2);
            GMP.mpz_internal_CHECK_FORMAT (res);
            if (GMP.mpz_cmp (ref[0], res) != 0) {
                dump_abort ("mpz_mfac_uiui(" + n + ",2) wrong", res, ref[0]);
                /***
                printf ("mpz_mfac_uiui(%lu,2) wrong\n", n);
                printf ("  got  "); mpz_out_str (stdout, 10, res); printf("\n");
                printf ("  want "); mpz_out_str (stdout, 10, ref[0]); printf("\n");
                abort ();
                ***/
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

    private void dump_abort(String msg,
                            mpz_t got, mpz_t want)
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
        emsg = msg +
               " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

}
