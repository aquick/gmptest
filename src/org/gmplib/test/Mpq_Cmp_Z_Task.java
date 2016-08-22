package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_Cmp_Z_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Cmp_Z_Task";
    
    public Mpq_Cmp_Z_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static int ref_mpq_cmp_z (mpq_t a, mpz_t b)
	throws GMPException
    {
        mpz_t bi;
        int cc;
	mpz_t anum = new mpz_t();
	mpz_t aden = new mpz_t();

        bi = new mpz_t();

	GMP.mpq_get_num(anum, a);
	GMP.mpq_get_den(aden, a);
        GMP.mpz_mul (bi, b, aden);
        cc = GMP.mpz_cmp (anum, bi);
        return cc;
    }

    private static final long SIZE = 8;  /* increasing this lowers the probability of finding an error */
    private static final int  MAXN = 5;  /* increasing this impacts on total timing */

    private void sizes_test (int m)
        throws Exception
    {
        mpq_t a;
        mpz_t b;
        int i, j, k, s;
        int cc, ccref;
	mpz_t anum = new mpz_t();
	mpz_t aden = new mpz_t();

        a = new mpq_t();
        b = new mpz_t();

        for (i = 0; i <= MAXN ; ++i) {
            GMP.mpz_setbit (aden, i*m); /* \sum_0^i 2^(i*m) */
            for (j = 0; j <= MAXN; ++j) {
                GMP.mpz_set_ui (anum, 0);
                GMP.mpz_setbit (anum, j*m); /* 2^(j*m) */
                GMP.mpq_set_num(a, anum);
                GMP.mpq_set_den(a, aden);
                for (k = 0; k <= MAXN; ++k) {
                    GMP.mpz_set_ui (b, 0);
                    GMP.mpz_setbit (b, k*m); /* 2^(k*m) */
                    if (i == 0) { /* Denominator is 1, compare the two exponents */
                        ccref = (j>k ? 1 : 0) - (j<k ? 1 : 0);
                    } else {
                        ccref = j-i > k ? 1 : -1;
                    }
                    for (s = 1; s >= -1; s -= 2) {
                        cc = GMP.mpq_cmp_z (a, b);

                        if (ccref != TestUtil.SGN (cc)) {
                            /***
                            fprintf (stderr, "i=%i, j=%i, k=%i, m=%i, s=%i\n; ccref= %i, cc= %i\n", i, j, k, m, s, ccref, cc);
                            abort ();
                            ***/
                            dump_abort("", i, j, k, m, s, ccref, cc);
                        }

                        GMP.mpq_neg (a, a);
                        GMP.mpz_neg (b, b);
                        ccref = - ccref;
                    }
                }
            }
        }
    }
    
    public void run()
    {
        mpq_t a;
        mpz_t b;
	mpz_t anum;
	mpz_t aden;
        long size;
        int reps = 1000; // 10000;
        int i;
        int cc;
        int ccref;
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

    	    anum = new mpz_t();
    	    aden = new mpz_t();
            a = new mpq_t();
            b = new mpz_t();

            for (i = 0; i < reps; i++) {
        	if (i % 8192 == 0) {
        	    sizes_test ((int)(TestUtil.urandom (rands) % (i + 1) + 1));
        	}
        	size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
        	TestUtil.mpz_rrandomb_signed(anum, rands, size);
                do {
                    size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
            	    TestUtil.mpz_rrandomb_signed(aden, rands, size);
                }
                while (GMP.mpz_cmp_ui (aden, 0) == 0);


                GMP.mpq_set_num(a, anum);
                GMP.mpq_set_den(a, aden);
                GMP.mpq_canonicalize (a);

        	size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
        	TestUtil.mpz_rrandomb_signed(b, rands, size);
        	
        	ccref = ref_mpq_cmp_z (a, b);
        	cc = GMP.mpq_cmp_z (a, b);

        	if (TestUtil.SGN (ccref) != TestUtil.SGN (cc)) {
        	    dump_abort ("compare failure", ccref, cc);
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

    private void dump_abort(String msg, int i, int j, int k, int m, int s, int ccref, int cc)
        throws Exception
    {
        String emsg;
        emsg = "ERROR: " + msg + " i=" + i + " j=" + j + " k=" + k + " m=" + m + " s=" + s + " ccref=" + ccref + " cc=" + cc;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, int ccref, int cc)
        throws Exception
    {
        String emsg;
        emsg = "ERROR: " + msg + " ccref=" + ccref + " cc=" + cc;
        throw new Exception(emsg);
    }
}
