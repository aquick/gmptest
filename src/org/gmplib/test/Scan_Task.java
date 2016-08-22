package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Scan_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Scan_Task";
    
    public Scan_Task(UI ui)
    {
        super(ui, TAG);
    }

    private long refmpz_scan (mpz_t z, long i, int sought)
        throws GMPException
    {
        long  z_bits = (long) GMP.mpz_internal_ABSIZ(z) * GMP.GMP_NUMB_BITS();

        do {
            if (GMP.mpz_tstbit (z, i) == sought) {
                return i;
            }
            i++;
        } while (i <= z_bits);

        return 0xFFFFFFFFL;
    }

    private long refmpz_scan0 (mpz_t z, long starting_bit)
        throws GMPException
    {
        return refmpz_scan (z, starting_bit, 0);
    }

    private long refmpz_scan1 (mpz_t z, long starting_bit)
        throws GMPException
    {
        return refmpz_scan (z, starting_bit, 1);
    }


    private static final int[] offset = new int [] {
        -2, -1, 0, 1, 2, 3
    };

    private void check_ref (randstate_t rands)
        throws Exception
    {
        mpz_t          z;
        int            test;
        int            neg;
        int            sought;
        int            oindex;
        int            o;
        long           size;
        long           isize;
        long           start;
        long           got;
        long           want;

        z = new mpz_t();
        for (test = 0; test < 5; test++) {
            for (size = 0; size < 5; size++) {
                GMP.mpz_rrandomb (z, rands, size*GMP.GMP_LIMB_BITS());

                for (neg = 0; neg <= 1; neg++) {
                    if (neg != 0) {
                        GMP.mpz_neg (z, z);
                    }

                    for (isize = 0; isize <= size; isize++) {
                        for (oindex = 0; oindex < offset.length; oindex++) {
                            o = offset[oindex];
                            if ((int) isize*GMP.GMP_NUMB_BITS() < -o) {
                                continue;  /* start would be negative */
                            }

                            start = isize*GMP.GMP_NUMB_BITS() + o;

                            for (sought = 0; sought <= 1; sought++) {
                                if (sought == 0) {
                                    got = GMP.mpz_scan0 (z, start);
                                    want = refmpz_scan0 (z, start);
                                } else {
                                    got = GMP.mpz_scan1 (z, start);
                                    want = refmpz_scan1 (z, start);
                                }

                                if (got != want) {
                                    /***
                                    printf ("wrong at test=%d, size=%ld, neg=%d, start=%lu, sought=%d\n",
                                      test, size, neg, start, sought);
                                    printf ("   z 0x");
                                    mpz_out_str (stdout, -16, z);
                                    printf ("\n");
                                    printf ("   got=%lu, want=%lu\n", got, want);
                                    exit (1);
                                    ***/
                                    dump_abort("wrong at test=" + test + ", size=" +
                                        size + ", neg=" + neg + ", start=" + start + ", sought=" + sought,
                                        z, got, want);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void run()
    {
        int ret = 0;
        long seed;
        randstate_t rands;

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
            
            check_ref (rands);
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

    private void dump_abort(String msg, mpz_t z, long got, long want)
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
        emsg = "ERROR: " + msg + " z=" + z_str + " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }
}
