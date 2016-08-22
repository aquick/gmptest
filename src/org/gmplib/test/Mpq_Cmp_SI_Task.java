package org.gmplib.test;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;

import android.util.Log;

public class Mpq_Cmp_SI_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Cmp_SI_Task";
    
    public Mpq_Cmp_SI_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final class CheckData
    {
        public String q;
        public int n;
        public long d;
        public int want;

        public CheckData(String q, int n, long d, int want)
        {
            this.q = q;
            this.n = n;
            this.d = d;
            this.want = want;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	    new CheckData( "0", 0, 1, 0 ),
	    new CheckData( "0", 0, 123, 0 ),
	    new CheckData( "0", 0, GMP.ULONG_MAX, 0 ),
	    new CheckData( "1", 0, 1, 1 ),
	    new CheckData( "1", 0, 123, 1 ),
	    new CheckData( "1", 0, GMP.ULONG_MAX, 1 ),
	    new CheckData( "-1", 0, 1, -1 ),
	    new CheckData( "-1", 0, 123, -1 ),
	    new CheckData( "-1", 0, GMP.ULONG_MAX, -1 ),

	    new CheckData( "123", 123, 1, 0 ),
	    new CheckData( "124", 123, 1, 1 ),
	    new CheckData( "122", 123, 1, -1 ),

	    new CheckData( "-123", 123, 1, -1 ),
	    new CheckData( "-124", 123, 1, -1 ),
	    new CheckData( "-122", 123, 1, -1 ),

	    new CheckData( "123", -123, 1, 1 ),
	    new CheckData( "124", -123, 1, 1 ),
	    new CheckData( "122", -123, 1, 1 ),

	    new CheckData( "-123", -123, 1, 0 ),
	    new CheckData( "-124", -123, 1, -1 ),
	    new CheckData( "-122", -123, 1, 1 ),

	    new CheckData( "5/7", 3,4, -1 ),
	    new CheckData( "5/7", -3,4, 1 ),
	    new CheckData( "-5/7", 3,4, -1 ),
	    new CheckData( "-5/7", -3,4, 1 )
	
    };

    private void check_data()
        throws Exception
    {	
        mpq_t  q;
        int    i;
        int    got;

        q = new mpq_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpq_set_str (q, data[i].q, 0);
            GMP.mpq_internal_CHECK_FORMAT (q);

            got = GMP.mpq_cmp_si (q, data[i].n, data[i].d);
            if (TestUtil.SGN(got) != data[i].want) {
                dump_abort ("mpq_cmp_si wrong", q, data[i].n, data[i].d, got, data[i].want);
                /***
                    printf ("mpq_cmp_si wrong\n");
                  error:
                    mpq_trace ("  q", q);
                    printf ("  n=%ld\n", data[i].n);
                    printf ("  d=%lu\n", data[i].d);
                    printf ("  got=%d\n", got);
                    printf ("  want=%d\n", data[i].want);
                    abort ();
                ***/
            }

            if (data[i].n == 0) {
                got = GMP.mpq_cmp_si (q, 0, data[i].d);
                if (TestUtil.SGN(got) != data[i].want) {
                    dump_abort ("mpq_cmp_si wrong", q, data[i].n, data[i].d, got, data[i].want);
                    /***
                        printf ("mpq_cmp_si wrong\n");
                        goto error;
                    ***/
                }
            }
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

            check_data();
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

    private void dump_abort(String msg, mpq_t x, int n, long d, int got, int want)
        throws Exception
    {
        String x_str = "";
        String emsg;
        try {
            x_str = GMP.mpq_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " q=" + x_str + " n=" + n + " d=" + d + " got= " + got + " want=" + want;
        throw new Exception(emsg);
    }

}
