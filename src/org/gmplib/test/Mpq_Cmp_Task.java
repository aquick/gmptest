package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_Cmp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Cmp_Task";
    
    public Mpq_Cmp_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static int ref_mpq_cmp (mpq_t a, mpq_t b)
        throws GMPException
    {
        mpz_t ai;
        mpz_t bi;
        int cc;
	mpz_t anum = new mpz_t();
	mpz_t aden = new mpz_t();
	mpz_t bnum = new mpz_t();
	mpz_t bden = new mpz_t();

        ai = new mpz_t();
        bi = new mpz_t();

	GMP.mpq_get_num(anum, a);
	GMP.mpq_get_den(aden, a);
	GMP.mpq_get_num(bnum, b);
	GMP.mpq_get_den(bden, b);
        GMP.mpz_mul (ai, anum, bden);
        GMP.mpz_mul (bi, bnum, aden);
        cc = GMP.mpz_cmp (ai, bi);
        return cc;
    }

    private static final long SIZE = 8;  /* increasing this lowers the probability of finding an error */

    public void run()
    {
        mpq_t a;
        mpq_t b;
	mpz_t anum;
	mpz_t aden;
	mpz_t bnum;
	mpz_t bden;
        long size;
        int reps = 1000; // 10000;
        int i;
        int cc, ccref;
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
    	    bnum = new mpz_t();
    	    bden = new mpz_t();
            a = new mpq_t();
            b = new mpq_t();

            for (i = 0; i < reps; i++) {
        	size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
        	TestUtil.mpz_rrandomb_signed(anum, rands, size);
                do {
                    size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
            	    TestUtil.mpz_rrandomb_signed(aden, rands, size);
                }
                while (GMP.mpz_cmp_ui (aden, 0) == 0);

                size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
        	TestUtil.mpz_rrandomb_signed(bnum, rands, size);
                do {
                    size = (TestUtil.urandom(rands) % SIZE - SIZE/2)*GMP.GMP_LIMB_BITS();
            	    TestUtil.mpz_rrandomb_signed(bden, rands, size);
                }
                while (GMP.mpz_cmp_ui (bden, 0) == 0);

                GMP.mpq_set_num(a, anum);
                GMP.mpq_set_den(a, aden);
                GMP.mpq_set_num(b, bnum);
                GMP.mpq_set_den(b, bden);
                GMP.mpq_canonicalize (a);
                GMP.mpq_canonicalize (b);

                ccref = ref_mpq_cmp (a, b);
                cc = GMP.mpq_cmp (a, b);

                if (TestUtil.SGN (ccref) != TestUtil.SGN (cc)) {
                    dump_abort ("compare failure", a, b);
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

    private void dump_abort(String msg, mpq_t x, mpq_t y)
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpq_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpq_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " x=" + x_str + " y=" + y_str;
        throw new Exception(emsg);
    }

}
