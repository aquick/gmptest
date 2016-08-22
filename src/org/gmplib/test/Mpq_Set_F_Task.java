package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.mpf_t;
//import org.gmplib.gmpjni.GMP.randstate_t;



import android.util.Log;

public class Mpq_Set_F_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Set_F_Task";
    
    public Mpq_Set_F_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final class CheckData
    {
	public int         f_base;
	public String      f;
	public int         z_base;
	public String      want_num;
	public String      want_den;

        public CheckData(int f_base, String f, int z_base, String want_num, String want_den)
        {
            this.f_base = f_base;
            this.f = f;
            this.z_base = z_base;
            this.want_num = want_num;
            this.want_den = want_den;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	    new CheckData( -2, "0",    16, "0", "1" ),
	    new CheckData( -2, "1",    16, "1", "1" ),
	    new CheckData( -2, "1@1",  16, "2", "1" ),
	    new CheckData( -2, "1@2",  16, "4", "1" ),
	    new CheckData( -2, "1@3",  16, "8", "1" ),

	    new CheckData( -2, "1@30", 16,  "40000000", "1" ),
	    new CheckData( -2, "1@31", 16,  "80000000", "1" ),
	    new CheckData( -2, "1@32", 16, "100000000", "1" ),
	    new CheckData( -2, "1@33", 16, "200000000", "1" ),
	    new CheckData( -2, "1@34", 16, "400000000", "1" ),

	    new CheckData( -2, "1@62", 16,  "4000000000000000", "1" ),
	    new CheckData( -2, "1@63", 16,  "8000000000000000", "1" ),
	    new CheckData( -2, "1@64", 16, "10000000000000000", "1" ),
	    new CheckData( -2, "1@65", 16, "20000000000000000", "1" ),
	    new CheckData( -2, "1@66", 16, "40000000000000000", "1" ),

	    new CheckData( -2, "1@126", 16,  "40000000000000000000000000000000", "1" ),
	    new CheckData( -2, "1@127", 16,  "80000000000000000000000000000000", "1" ),
	    new CheckData( -2, "1@128", 16, "100000000000000000000000000000000", "1" ),
	    new CheckData( -2, "1@129", 16, "200000000000000000000000000000000", "1" ),
	    new CheckData( -2, "1@130", 16, "400000000000000000000000000000000", "1" ),

	    new CheckData( -2, "1@-1",  16, "1", "2" ),
	    new CheckData( -2, "1@-2",  16, "1", "4" ),
	    new CheckData( -2, "1@-3",  16, "1", "8" ),

	    new CheckData( -2, "1@-30", 16, "1",  "40000000" ),
	    new CheckData( -2, "1@-31", 16, "1",  "80000000" ),
	    new CheckData( -2, "1@-32", 16, "1", "100000000" ),
	    new CheckData( -2, "1@-33", 16, "1", "200000000" ),
	    new CheckData( -2, "1@-34", 16, "1", "400000000" ),

	    new CheckData( -2, "1@-62", 16, "1",  "4000000000000000" ),
	    new CheckData( -2, "1@-63", 16, "1",  "8000000000000000" ),
	    new CheckData( -2, "1@-64", 16, "1", "10000000000000000" ),
	    new CheckData( -2, "1@-65", 16, "1", "20000000000000000" ),
	    new CheckData( -2, "1@-66", 16, "1", "40000000000000000" ),

	    new CheckData( -2, "1@-126", 16, "1",  "40000000000000000000000000000000" ),
	    new CheckData( -2, "1@-127", 16, "1",  "80000000000000000000000000000000" ),
	    new CheckData( -2, "1@-128", 16, "1", "100000000000000000000000000000000" ),
	    new CheckData( -2, "1@-129", 16, "1", "200000000000000000000000000000000" ),
	    new CheckData( -2, "1@-130", 16, "1", "400000000000000000000000000000000" ),

	    new CheckData( -2, "1@-30", 16, "1",  "40000000" ),
	    new CheckData( -2, "1@-31", 16, "1",  "80000000" ),
	    new CheckData( -2, "1@-32", 16, "1", "100000000" ),
	    new CheckData( -2, "1@-33", 16, "1", "200000000" ),
	    new CheckData( -2, "1@-34", 16, "1", "400000000" ),

	    new CheckData( -2, "11@-62", 16, "3",  "4000000000000000" ),
	    new CheckData( -2, "11@-63", 16, "3",  "8000000000000000" ),
	    new CheckData( -2, "11@-64", 16, "3", "10000000000000000" ),
	    new CheckData( -2, "11@-65", 16, "3", "20000000000000000" ),
	    new CheckData( -2, "11@-66", 16, "3", "40000000000000000" ),

	    new CheckData( 16, "80000000.00000001", 16, "8000000000000001", "100000000" ),
	    new CheckData( 16, "80000000.00000008", 16, "1000000000000001",  "20000000" ),
	    new CheckData( 16, "80000000.8",        16, "100000001", "2" )
    };
    
    @Override
    public void run()
    {
	mpf_t  f;
	mpq_t  got;
	mpz_t  want_num;
	mpz_t  want_den;
	mpz_t  got_num;
	mpz_t  got_den;
	int    i;
	int    neg;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            Log.d(TAG, "no randomness");
            
            f = new mpf_t(1024L);
            got = new mpq_t();
            want_num = new mpz_t();
            want_den = new mpz_t();
            got_num = new mpz_t();
            got_den = new mpz_t();
            for (i = 0; i < data.length; i++) {
                for (neg = 0; neg <= 1; neg++) {
                    GMP.mpf_set_str (f, data[i].f, data[i].f_base);
                    GMP.mpz_set_str (want_num, data[i].want_num, data[i].z_base);
                    GMP.mpz_set_str (want_den, data[i].want_den, data[i].z_base);

                    if (neg != 0) {
                        GMP.mpf_neg (f, f);
                        GMP.mpz_neg (want_num, want_num);
                    }

                    GMP.mpq_set_f (got, f);
                    GMP.mpq_internal_CHECK_FORMAT (got);

                    GMP.mpq_get_num(got_num, got);
                    GMP.mpq_get_den(got_den, got);
                    if (GMP.mpz_cmp (got_num, want_num) != 0
                        || GMP.mpz_cmp (got_den, want_den) != 0) {
                	/***
                        printf ("wrong at data[%d]\n", i);
                        printf ("   f_base %d, z_base %d\n",
                              data[i].f_base, data[i].z_base);

                        printf ("   f \"%s\" hex ", data[i].f);
                        mpf_out_str (stdout, 16, 0, f);
                        printf ("\n");

                        printf ("   want num 0x");
                        mpz_out_str (stdout, 16, want_num);
                        printf ("\n");
                        printf ("   want den 0x");
                        mpz_out_str (stdout, 16, want_den);
                        printf ("\n");

                        printf ("   got num 0x");
                        mpz_out_str (stdout, 16, mpq_numref(got));
                        printf ("\n");
                        printf ("   got den 0x");
                        mpz_out_str (stdout, 16, mpq_denref(got));
                        printf ("\n");

                        abort ();
                        ***/
                	dump_abort("wrong at data[" + i + "]", data[i].f_base, data[i].z_base, data[i].f, want_num, want_den, got_num, got_den);
                    }
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

    private void dump_abort(String msg, int f_base, int z_base, String f, mpz_t want_num, mpz_t want_den, mpz_t got_num, mpz_t got_den)
        throws Exception
    {
        String want_num_str = "";
        String want_den_str = "";
        String got_num_str = "";
        String got_den_str = "";
        String emsg;
        try {
            want_num_str = GMP.mpz_get_str(want_num, 10);
        }
        catch (GMPException e) {
            want_num_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_den_str = GMP.mpz_get_str(want_den, 10);
        }
        catch (GMPException e) {
            want_den_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_num_str = GMP.mpz_get_str(got_num, 10);
        }
        catch (GMPException e) {
            got_num_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_den_str = GMP.mpz_get_str(got_den, 10);
        }
        catch (GMPException e) {
            got_den_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " f_base=" + f_base + " z_base=" + z_base + " f=" + f +
        	" want num=" + want_num_str + " want den=" + want_den_str +
        	" got num=" + got_num_str + " got den=" + got_den_str;
        throw new Exception(emsg);
    }
}
