package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;

import android.util.Log;

public class Mpq_MulDiv_2Exp_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_MulDiv_2Exp_Task";
    
    public Mpq_MulDiv_2Exp_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final class CheckData
    {
	public String      l_num;
	public String      l_den;
	public long        n;
	public String      r_num;
	public String      r_den;

        public CheckData(String l_num, String l_den, long n, String r_num, String r_den)
        {
            this.l_num = l_num;
            this.l_den = l_den;
            this.n = n;
            this.r_num = r_num;
            this.r_den = r_den;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	    new CheckData("0","1", 0, "0","1"),
	    new CheckData("0","1", 1, "0","1"),
	    new CheckData("0","1", 2, "0","1"),

	    new CheckData("1","1", 0, "1","1"),
	    new CheckData("1","1", 1, "2","1"),
	    new CheckData("1","1", 2, "4","1"),
	    new CheckData("1","1", 3, "8","1"),

	    new CheckData("1","1", 31, "0x80000000","1"),
	    new CheckData("1","1", 32, "0x100000000","1"),
	    new CheckData("1","1", 33, "0x200000000","1"),
	    new CheckData("1","1", 63, "0x8000000000000000","1"),
	    new CheckData("1","1", 64, "0x10000000000000000","1"),
	    new CheckData("1","1", 65, "0x20000000000000000","1"),
	    new CheckData("1","1", 95, "0x800000000000000000000000","1"),
	    new CheckData("1","1", 96, "0x1000000000000000000000000","1"),
	    new CheckData("1","1", 97, "0x2000000000000000000000000","1"),
	    new CheckData("1","1", 127, "0x80000000000000000000000000000000","1"),
	    new CheckData("1","1", 128, "0x100000000000000000000000000000000","1"),
	    new CheckData("1","1", 129, "0x200000000000000000000000000000000","1"),

	    new CheckData("1","2", 31, "0x40000000","1"),
	    new CheckData("1","2", 32, "0x80000000","1"),
	    new CheckData("1","2", 33, "0x100000000","1"),
	    new CheckData("1","2", 63, "0x4000000000000000","1"),
	    new CheckData("1","2", 64, "0x8000000000000000","1"),
	    new CheckData("1","2", 65, "0x10000000000000000","1"),
	    new CheckData("1","2", 95, "0x400000000000000000000000","1"),
	    new CheckData("1","2", 96, "0x800000000000000000000000","1"),
	    new CheckData("1","2", 97, "0x1000000000000000000000000","1"),
	    new CheckData("1","2", 127, "0x40000000000000000000000000000000","1"),
	    new CheckData("1","2", 128, "0x80000000000000000000000000000000","1"),
	    new CheckData("1","2", 129, "0x100000000000000000000000000000000","1"),

	    new CheckData("1","0x80000000", 30, "1","2"),
	    new CheckData("1","0x80000000", 31, "1","1"),
	    new CheckData("1","0x80000000", 32, "2","1"),
	    new CheckData("1","0x80000000", 33, "4","1"),
	    new CheckData("1","0x80000000", 62, "0x80000000","1"),
	    new CheckData("1","0x80000000", 63, "0x100000000","1"),
	    new CheckData("1","0x80000000", 64, "0x200000000","1"),
	    new CheckData("1","0x80000000", 94, "0x8000000000000000","1"),
	    new CheckData("1","0x80000000", 95, "0x10000000000000000","1"),
	    new CheckData("1","0x80000000", 96, "0x20000000000000000","1"),
	    new CheckData("1","0x80000000", 126, "0x800000000000000000000000","1"),
	    new CheckData("1","0x80000000", 127, "0x1000000000000000000000000","1"),
	    new CheckData("1","0x80000000", 128, "0x2000000000000000000000000","1"),

	    new CheckData("1","0x100000000", 1, "1","0x80000000"),
	    new CheckData("1","0x100000000", 2, "1","0x40000000"),
	    new CheckData("1","0x100000000", 3, "1","0x20000000"),

	    new CheckData("1","0x10000000000000000", 1, "1","0x8000000000000000"),
	    new CheckData("1","0x10000000000000000", 2, "1","0x4000000000000000"),
	    new CheckData("1","0x10000000000000000", 3, "1","0x2000000000000000")
    };
    
    private void check_random (randstate_t rands, int reps)
        throws Exception
    {
        mpz_t bs;
        mpz_t qnum;
        mpz_t qden;
        long arg_size;
        long size_range;
        mpq_t q;
        mpq_t r;
        int i;
        long shift;

        bs = new mpz_t();
        qnum = new mpz_t();
        qden = new mpz_t();
        q = new mpq_t();
        r = new mpq_t();

        for (i = 0; i < reps; i++) {
            GMP.mpz_urandomb (bs, rands, 32);
            size_range = GMP.mpz_get_ui (bs) % 11 + 2; /* 0..4096 bit operands */

            GMP.mpz_urandomb (bs, rands, size_range);
            arg_size = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (qnum, rands, arg_size);
            do {
                GMP.mpz_urandomb (bs, rands, size_range);
                arg_size = GMP.mpz_get_ui (bs);
                GMP.mpz_rrandomb (qden, rands, arg_size);
            }
            while (GMP.mpz_sgn (qden) == 0);
            GMP.mpq_set_num(q, qnum);
            GMP.mpq_set_den(q, qden);

            /* We now have a random rational in q, albeit an unnormalised one.  The
               lack of normalisation should not matter here, so let's save the time a
               gcd would require.  */

            GMP.mpz_urandomb (bs, rands, 32);
            shift = GMP.mpz_get_ui (bs) % 4096;

            GMP.mpq_mul_2exp (r, q, shift);

            if (GMP.mpq_cmp (r, q) < 0) {
        	/***
                printf ("mpq_mul_2exp wrong on random\n");
                abort ();
                ***/
                dump_abort ("mpq_mul_2exp wrong on random");
            }

            GMP.mpq_div_2exp (r, r, shift);

            if (GMP.mpq_cmp (r, q) != 0) {
        	/***
                printf ("mpq_mul_2exp or mpq_div_2exp wrong on random\n");
                abort ();
                ***/
                dump_abort ("mpq_mul_2exp or mpq_div_2exp wrong on random");
            }
            if (Thread.interrupted()) {
                throw new Exception("Task cancelled");
            }
            if (i % 10 == 0) {
                onProgressUpdate(Integer.valueOf((int)((float)(i+1)*100.0/(float)reps)));
            }
        }
    }

    @Override
    public void run()
    {
        String name;
        String start_num_str;
        String start_den_str;
        String want_num_str;
        String want_den_str;
        mpz_t    want_num;
        mpz_t    want_den;
        mpz_t    q_num;
        mpz_t    q_den;
        mpq_t    sep;
        mpq_t    got;
        mpq_t    want;
        mpq_t    q;
        int      i;
        int      muldiv;
        int      sign;
        int      overlap;
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
            
            want_num = new mpz_t();
            want_den = new mpz_t();
            q_num = new mpz_t();
            q_den = new mpz_t();
            sep = new mpq_t();
            got = new mpq_t();
            want = new mpq_t();
            for (i = 0; i < data.length; i++) {
                for (muldiv = 0; muldiv < 2; muldiv++) {
                    if (muldiv == 0) {
                        name = "mpq_mul_2exp";
                        start_num_str = data[i].l_num;
                        start_den_str = data[i].l_den;
                        want_num_str = data[i].r_num;
                        want_den_str = data[i].r_den;
                    } else {
                        name = "mpq_div_2exp";
                        start_num_str = data[i].r_num;
                        start_den_str = data[i].r_den;
                        want_num_str = data[i].l_num;
                        want_den_str = data[i].l_den;
                    }

                    for (sign = 0; sign <= 1; sign++) {
                        GMP.mpz_set_str (want_num, want_num_str, 0);
                        GMP.mpz_set_str (want_den, want_den_str, 0);
                        GMP.mpq_set_num(want, want_num);
                        GMP.mpq_set_den(want, want_den);
                        if (sign != 0) {
                            GMP.mpq_neg (want, want);
                        }

                        for (overlap = 0; overlap <= 1; overlap++) {
                            q = overlap != 0 ? got : sep;

                            /* initial garbage in "got" */
                            GMP.mpq_set_ui (got, 123L, 456L);

                            GMP.mpz_set_str (q_num, start_num_str, 0);
                            GMP.mpz_set_str (q_den, start_den_str, 0);
                            GMP.mpq_set_num(q, q_num);
                            GMP.mpq_set_den(q, q_den);
                            if (sign != 0) {
                                GMP.mpq_neg (q, q);
                            }

                            if (muldiv == 0) {
                                GMP.mpq_mul_2exp (got, q, data[i].n);
                            } else {
                                GMP.mpq_div_2exp (got, q, data[i].n);
                            }
                            GMP.mpq_internal_CHECK_FORMAT (got);

                            if (GMP.mpq_equal (got, want) == 0) {
                        	/***
                                printf ("%s wrong at data[%d], sign %d, overlap %d\n",
                                      name, i, sign, overlap);
                                printf ("   num \"%s\"\n", p_start->num);
                                printf ("   den \"%s\"\n", p_start->den);
                                printf ("   n   %lu\n", data[i].n);

                                printf ("   got  ");
                                mpq_out_str (stdout, 16, got);
                                printf (" (hex)\n");

                                printf ("   want ");
                                mpq_out_str (stdout, 16, want);
                                printf (" (hex)\n");

                                abort ();
                                ***/
                        	dump_abort(name + " wrong at data[" + i + "], sign " + sign + ", overlap " + overlap,
                        		start_num_str, start_den_str, data[i].n, got, want);
                            }
                        }
                    }
                }
            }

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
        String emsg;
        emsg = "ERROR: " + msg;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, String num, String den, long n, mpq_t got, mpq_t want)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            got_str = GMP.mpq_get_str(got, 16);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            want_str = GMP.mpq_get_str(want, 16);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " num=" + num + " den=" + den + " n=" + n +
        	" want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

}
