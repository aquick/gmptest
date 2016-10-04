package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.mpf_t;
//import org.gmplib.gmpjni.GMP.randstate_t;



import android.util.Log;

public class Mpq_Get_Str_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Get_Str_Task";
    
    public Mpq_Get_Str_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final class CheckData
    {
	public int         base;
	public String      num;
	public String      den;
	public String      want;

        public CheckData(int base, String num, String den, String want)
        {
            this.base = base;
            this.num = num;
            this.den = den;
            this.want = want;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	    new CheckData( 10, "0", "1", "0" ),
	    new CheckData( 10, "1", "1", "1" ),

	    new CheckData( 16, "ffffffff", "1", "ffffffff" ),
	    new CheckData( 16, "ffffffffffffffff", "1", "ffffffffffffffff" ),

	    new CheckData( 16, "1", "ffffffff", "1/ffffffff" ),
	    new CheckData( 16, "1", "ffffffffffffffff", "1/ffffffffffffffff" ),
	    new CheckData( 16, "1", "10000000000000003", "1/10000000000000003" ),

	    new CheckData( 10, "12345678901234567890", "9876543210987654323",
	      "12345678901234567890/9876543210987654323" )
    };
    
    private void check_one (mpq_t q, int base, String want)
        throws Exception
    {
        String str;
        //String ret;
        int  str_alloc;
        mpz_t qnum;
        mpz_t qden;

        qnum = new mpz_t();
        qden = new mpz_t();
        GMP.mpq_internal_CHECK_FORMAT (q);

        GMP.mpq_get_num(qnum, q);
        GMP.mpq_get_den(qden, q);
        str_alloc =
            (int)GMP.mpz_sizeinbase (qnum, Math.abs(base)) +
            (int)GMP.mpz_sizeinbase (qden, Math.abs(base)) + 3;

        str = GMP.mpq_get_str (q, base);
        if (str.length()+1 > str_alloc) {
            /***
            printf ("mpq_get_str size bigger than should be (passing NULL)\n");
            printf ("  base %d\n", base);
            printf ("  got  size %lu \"%s\"\n", (unsigned long)  strlen(str)+1, str);
            printf ("  want size %lu\n", (unsigned long) str_alloc);
            abort ();
            ***/
            dump_abort("mpq_get_str size bigger than should be, base=" + base + " got size " +
        	    Integer.toString(str.length() + 1) + " want size " + str_alloc);
        }
        if (!str.equals(want)) {
            /***
            printf ("mpq_get_str wrong (passing NULL)\n");
            printf ("  base %d\n", base);
            printf ("  got  \"%s\"\n", str);
            printf ("  want \"%s\"\n", want);
            mpq_trace ("  q", q);
            abort ();
            ***/
            dump_abort("mpq_get_str wrong", base, str, want, q);
        }
        /***
        (*__gmp_free_func) (str, strlen (str) + 1);

        str = (char *) (*__gmp_allocate_func) (str_alloc);

        ret = mpq_get_str (str, base, q);
        if (str != ret)
        {
          printf ("mpq_get_str wrong return value (passing non-NULL)\n");
          printf ("  base %d\n", base);
          printf ("  got  %p\n", ret);
          printf ("  want %p\n", want);
          abort ();
        }
        if (strcmp (str, want) != 0)
        {
          printf ("mpq_get_str wrong (passing non-NULL)\n");
          printf ("  base %d\n", base);
          printf ("  got  \"%s\"\n", str);
          printf ("  want \"%s\"\n", want);
          abort ();
        }
        (*__gmp_free_func) (str, str_alloc);
        ***/
    }

    private void check_all (mpq_t q, int base, String want)
        throws Exception
    {
        check_one (q, base, want);
        check_one (q, -base, want.toUpperCase());
    }

    private void check_data ()
        throws Exception
    {
        mpq_t  q;
        int    i;
        mpz_t qnum;
        mpz_t qden;

        qnum = new mpz_t();
        qden = new mpz_t();
        q = new mpq_t();
        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (qnum, data[i].num, data[i].base);
            GMP.mpz_set_str (qden, data[i].den, data[i].base);
            GMP.mpq_set_num(q,  qnum);
            GMP.mpq_set_den(q,  qden);
            check_all (q, data[i].base, data[i].want);
        }
    }
    
    public void run()
    {
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
            //tests_start ();
            Log.d(TAG, "no randomness");
            
            check_data ();
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
        String emsg = "ERROR: " + msg;
        throw new Exception(emsg);
    }

    private void dump_abort(String msg, int base, String got_str, String want_str, mpq_t q)
        throws Exception
    {
        String q_str = "";
        String emsg;
        try {
            q_str = GMP.mpq_get_str(q, base);
        }
        catch (GMPException e) {
            q_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " base=" + base + " want=" + want_str + " got=" + got_str + " q=" + q_str;
        throw new Exception(emsg);
    }

}
