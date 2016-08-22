package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.randstate_t;




import android.util.Log;

public class Mpq_Inv_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Inv_Task";
    
    public Mpq_Inv_Task(UI ui)
    {
        super(ui, TAG);
    }

    public void run()
    {
	mpq_t a;
	mpq_t b;
	mpz_t m;
	mpz_t n;
	String s = "-420000000000000000000000";
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            Log.d(TAG, "no randomness");

            a = new mpq_t();
            b = new mpq_t();
            m = new mpz_t();
            n = new mpz_t();
            GMP.mpz_set_ui (m, 13);
            GMP.mpq_set_den (a, m);
            GMP.mpz_set_str (m, s, 0);
            GMP.mpq_set_num (a, m);
            GMP.mpq_internal_CHECK_FORMAT (a);
            GMP.mpq_inv (b, a);
            GMP.mpq_internal_CHECK_FORMAT (b);
            GMP.mpq_get_num (n, b);
            if (GMP.mpz_cmp_si (n, -13) != 0) {
        	dump_abort("mpz_cmp_si(n, -13) != 0");
            }
            GMP.mpq_neg (b, b);
            GMP.mpq_inv (a, b);
            GMP.mpq_internal_CHECK_FORMAT (a);
            GMP.mpq_inv (b, b);
            GMP.mpq_internal_CHECK_FORMAT (b);
            GMP.mpq_get_den (n, b);
            if (GMP.mpz_cmp_ui (n, 13) != 0) {
        	dump_abort("mpz_cmp_ui(n, 13) != 0");
            }
            GMP.mpq_get_num (n, a);
            GMP.mpz_add (n, n, m);
            if (GMP.mpz_sgn (n) != 0) {
        	dump_abort("mpz_sgn(n) != 0");
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

    private void dump_abort(String msg)
	throws Exception
    {
        String emsg;
        emsg = "ERROR: " + msg;
        throw new Exception(emsg);
    }
}
