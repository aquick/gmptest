package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.randstate_t;

public class Bin_Task extends TaskBase implements Runnable {

    private static final String TAG = "Bin_Task";
    
    public Bin_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void try_mpz_bin_ui (mpz_t want, mpz_t n, long k)
        throws Exception
    {
        mpz_t  got;

        got = new mpz_t();
        GMP.mpz_bin_ui (got, n, k);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort("mpz_bin_ui wrong", n, k, want, got);
            /***
            printf ("mpz_bin_ui wrong\n");
            printf ("  n="); mpz_out_str (stdout, 10, n); printf ("\n");
            printf ("  k=%lu\n", k);
            printf ("  got="); mpz_out_str (stdout, 10, got); printf ("\n");
            printf ("  want="); mpz_out_str (stdout, 10, want); printf ("\n");
            abort();
            ***/
        }
    }


    private void try_mpz_bin_uiui (mpz_t want, long n, long k)
        throws Exception
    {
        mpz_t  got;

        got = new mpz_t();
        GMP.mpz_bin_uiui (got, n, k);
        GMP.mpz_internal_CHECK_FORMAT (got);
        if (GMP.mpz_cmp (got, want) != 0) {
            dump_abort2("mpz_bin_uiui wrong", n, k, want, got);
            /***
            printf ("mpz_bin_uiui wrong\n");
            printf ("  n=%lu\n", n);
            printf ("  k=%lu\n", k);
            printf ("  got="); mpz_out_str (stdout, 10, got); printf ("\n");
            printf ("  want="); mpz_out_str (stdout, 10, want); printf ("\n");
            abort();
            ***/
        }
    }

    private static class CheckData
    {
        public String n;
        public long   k;
        public String want;

        public CheckData(String n, long k, String want)
        {
            this.n = n;
            this.k = k;
            this.want = want;
        }
    }

    private static CheckData[] data = new CheckData[] {
    new CheckData(   "0", 123456, "0" ),
    new CheckData(   "1", 543210, "0" ),
    new CheckData(   "2", 123321, "0" ),
    new CheckData(   "3", 234567, "0" ),
    new CheckData(   "10", 23456, "0" ),

    /* negatives, using bin(-n,k)=bin(n+k-1,k) */
    new CheckData(   "-1",  0,  "1"  ),
    new CheckData(   "-1",  1, "-1"  ),
    new CheckData(   "-1",  2,  "1"  ),
    new CheckData(   "-1",  3, "-1"  ),
    new CheckData(   "-1",  4,  "1"  ),

    new CheckData(   "-2",  0,  "1"  ),
    new CheckData(   "-2",  1, "-2"  ),
    new CheckData(   "-2",  2,  "3"  ),
    new CheckData(   "-2",  3, "-4"  ),
    new CheckData(   "-2",  4,  "5"  ),
    new CheckData(   "-2",  5, "-6"  ),
    new CheckData(   "-2",  6,  "7"  ),

    new CheckData(   "-3",  0,   "1"  ),
    new CheckData(   "-3",  1,  "-3"  ),
    new CheckData(   "-3",  2,   "6"  ),
    new CheckData(   "-3",  3, "-10"  ),
    new CheckData(   "-3",  4,  "15"  ),
    new CheckData(   "-3",  5, "-21"  ),
    new CheckData(   "-3",  6,  "28"  ),

    /* A few random values */
    new CheckData(   "41", 20,  "269128937220" ),
    new CheckData(   "62", 37,  "147405545359541742" ),
    new CheckData(   "50", 18,  "18053528883775" ),
    new CheckData(  "149", 21,  "19332950844468483467894649" )
    };

    private void samples ()
        throws Exception
    {
        mpz_t  n;
        mpz_t  want;
        int    i;

        n = new mpz_t();
        want = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (n, data[i].n, 0);
            GMP.mpz_set_str (want, data[i].want, 0);

            try_mpz_bin_ui (want, n, data[i].k);

            if (GMP.mpz_fits_ulong_p (n) != 0) {
                try_mpz_bin_uiui (want, GMP.mpz_get_ui (n), data[i].k);
            }
        }

    }

    /* Test some bin(2k,k) cases.  This produces some biggish numbers to
       exercise the limb accumulating code.  */
    private void twos (int count)
        throws Exception
    {
        mpz_t n;
        mpz_t want;
        long  k;

        n = new mpz_t();
        want = new mpz_t();

        GMP.mpz_set_ui (want, 2L);
        for (k = 1; k < (long)count; k++) {
            GMP.mpz_set_ui (n, 2*k);
            try_mpz_bin_ui (want, n, k);

            try_mpz_bin_uiui (want, 2*k, k);

            GMP.mpz_mul_ui (want, want, 2*(2*k+1));
            GMP.mpz_fdiv_q_ui (want, want, k+1);
        }

    }

    /* Test some random bin(n,k) cases.  This produces some biggish
       numbers to exercise the limb accumulating code.  */
    private void randomwalk (int count, randstate_t rands)
        throws Exception
    {
        mpz_t  n_z;
        mpz_t  want;
        long   n;
        long   k;
        long   i;
        long   r;
        int    tests;

        n_z = new mpz_t();
        want = new mpz_t();

        k = 3;
        n = 12;
        GMP.mpz_set_ui (want, 220L); /* binomial(12,3) = 220 */

        for (tests = 1; tests < count; tests++) {
            r = GMP.gmp_urandomm_ui (rands, 62) + 1;
            for (i = r & 7; i > 0; i--) {
                n++; k++;
                GMP.mpz_mul_ui (want, want, n);
                GMP.mpz_fdiv_q_ui (want, want, k);
            }
            for (i = r >> 3; i > 0; i--) {
                n++;
                GMP.mpz_mul_ui (want, want, n);
                GMP.mpz_fdiv_q_ui (want, want, n - k);
            }

            GMP.mpz_set_ui (n_z, n);
            try_mpz_bin_ui (want, n_z, k);

            try_mpz_bin_uiui (want, n, k);
        }
    }

    /* Test all bin(n,k) cases, with 0 <= k <= n + 1 <= count.  */
    private void smallexaustive (int count)
        throws Exception
    {
        mpz_t   n_z;
        mpz_t   want;
        long    n;
        long    k;

        n_z = new mpz_t();
        want = new mpz_t();

        for (n = 0; n < (long)count; n++) {
            GMP.mpz_set_ui (want, 1L);
            GMP.mpz_set_ui (n_z, n);
            for (k = 0; k <= n; k++) {
                try_mpz_bin_ui (want, n_z, k);
                try_mpz_bin_uiui (want, n, k);
                GMP.mpz_mul_ui (want, want, n - k);
                GMP.mpz_fdiv_q_ui (want, want, k + 1);
            }
            try_mpz_bin_ui (want, n_z, k);
            try_mpz_bin_uiui (want, n, k);
        }
    }

    public void run()
    {
        int count = 700;
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

            if (params.length > 0) {
                count = params[0].intValue();
            }
            for (;;) {
                samples ();
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(25);

                smallexaustive (count >> 4);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(50);

                twos (count >> 1);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(75);

                randomwalk (count - (count >> 1), rands);
                if (Thread.interrupted()) {
                    throw new Exception("Task cancelled");
                }
                onProgressUpdate(100);
                break;
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

    private void dump_abort(String msg, mpz_t n, long k, mpz_t want, mpz_t got)
        throws Exception
    {
        String n_str = "";
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            n_str = GMP.mpz_get_str(n, 10);
        }
        catch (GMPException e) {
            n_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " n=" + n_str + " k=" + k +
            " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg, long n, long k, mpz_t want, mpz_t got)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            want_str = GMP.mpz_get_str(want, 10);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpz_get_str(got, 10);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " n=" + n + " k=" + k +
            " got=" + got_str + " want=" + want_str;
        throw new Exception(emsg);
    }
}
