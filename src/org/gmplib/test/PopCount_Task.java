package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class PopCount_Task extends TaskBase implements Runnable
{
    private static final String TAG = "PopCount_Task";
    
    public PopCount_Task(UI ui)
    {
        super(ui, TAG);
    }

    private void check_onebit ()
        throws Exception
    {
        mpz_t n;
        long  i;
        long  got;

        n = new mpz_t();
        for (i = 0; i < 5 * GMP.GMP_LIMB_BITS(); i++) {
            GMP.mpz_setbit (n, i);
            got = GMP.mpz_popcount (n);
            if (got != 1) {
                dump_abort ("mpz_popcount wrong on single bit at " + i);
                /***
                printf ("mpz_popcount wrong on single bit at %lu\n", i);
                printf ("   got %lu, want 1\n", got);
                abort();
                ***/
            }
            GMP.mpz_clrbit (n, i);
        }
    }

    private static class CheckData
    {
        public CheckData(String n, long want) { this.n = n; this.want = want; }
        public String n;
        public long  want;
    };

    private static CheckData[] data = new CheckData[] {
        new CheckData( "-1", GMP.ULONG_MAX ),
        new CheckData( "-12345678", GMP.ULONG_MAX ),
        new CheckData( "0", 0 ),
        new CheckData( "1", 1L ),
        new CheckData( "3", 2L ),
        new CheckData( "5", 2L ),
        new CheckData( "0xFFFF", 16L ),
        new CheckData( "0xFFFFFFFF", 32L ),
        new CheckData( "0xFFFFFFFFFFFFFFFF", 64L ),
        new CheckData( "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 128L )
    };

    private void check_data ()
        throws Exception
    {
        long   got;
        int    i;
        mpz_t  n;

        n = new mpz_t();
        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (n, data[i].n, 0);
            got = GMP.mpz_popcount (n);
            if (got != data[i].want) {
                dump_abort2 ("mpz_popcount wrong at data[" + i + "]",
                             n, got, data[i].want);
                /***
                printf ("mpz_popcount wrong at data[%d]\n", i);
                printf ("   n     \"%s\"\n", data[i].n);
                printf ("         ");   mpz_out_str (stdout, 10, n); printf ("\n");
                printf ("         0x"); mpz_out_str (stdout, 16, n); printf ("\n");
                printf ("   got   %lu\n", got);
                printf ("   want  %lu\n", data[i].want);
                abort ();
                ***/
            }
        }
    }

    private long refmpz_popcount (mpz_t arg)
        throws Exception
    {
        long n;
        int i;
        long cnt;
        long x;

        n = GMP.mpz_internal_SIZ(arg);
        if (n > Integer.MAX_VALUE) throw new Exception("arg out of range");
        if (n < 0) return GMP.ULONG_MAX;

        cnt = 0;
        for (i = 0; i < (int)n; i++) {
            x = GMP.mpz_internal_get_ulimb(arg, i);
            while (x != 0) {
                if ((x & 1) != 0) {
                    cnt += 1;
                }
                x >>= 1;
            }
        }
        return cnt;
    }

    private void check_random (randstate_t rands, int reps)
        throws Exception
    {
        mpz_t bs;
        mpz_t arg;
        long  arg_size;
        long  size_range;
        long  got;
        long  ref;
        int i;

        bs = new mpz_t();
        arg = new mpz_t();

        for (i = 0; i < reps; i++) {
            GMP.mpz_urandomb (bs, rands, 32);
            size_range = GMP.mpz_get_ui (bs) % 11 + 2; /* 0..4096 bit operands */

            GMP.mpz_urandomb (bs, rands, size_range);
            arg_size = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (arg, rands, arg_size);

            got = GMP.mpz_popcount (arg);
            ref = refmpz_popcount (arg);
            if (got != ref) {
                dump_abort2 ("mpz_popcount wrong on random",
                             arg, got, ref);
                /***
                printf ("mpz_popcount wrong on random\n");
                printf ("         ");   mpz_out_str (stdout, 10, arg); printf ("\n");
                printf ("         0x"); mpz_out_str (stdout, 16, arg); printf ("\n");
                printf ("   got   %lu\n", got);
                printf ("   want  %lu\n", ref);
                abort ();
                ***/
            }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 100 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }
    }

    public void run()
    {
        int reps = 1000; // 10000;
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
                reps = params[0].intValue();
            }

            check_onebit ();
            check_data ();
            check_random (rands, reps);
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

    private void dump_abort(String msg)
        throws Exception
    {
        throw new Exception(msg);
    }

    private void dump_abort2(String msg, mpz_t a, long got, long want)
        throws Exception
    {
        String a_str = "";
        String emsg;
        try {
            a_str = GMP.mpz_get_str(a, 16);
        }
        catch (GMPException e) {
            a_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg + " x=" + a_str + " got=" + got + " want=" + want;
        throw new Exception(emsg);
    }
}
