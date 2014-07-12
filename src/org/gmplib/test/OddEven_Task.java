package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class OddEven_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "OddEven_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public OddEven_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private static class CheckData
    {
        public CheckData(String n, int odd, int even)
        {
            this.n = n;
            this.odd = odd;
            this.even = even;
        }
        public String n;
        public int odd;
        public int even;
    }
    
    private static final CheckData[] data = new CheckData[] {
    new CheckData(   "0", 0, 1 ),
    new CheckData(   "1", 1, 0 ),
    new CheckData(   "2", 0, 1 ),
    new CheckData(   "3", 1, 0 ),
    new CheckData(   "4", 0, 1 ),

    new CheckData(  "-4", 0, 1 ),
    new CheckData(  "-3", 1, 0 ),
    new CheckData(  "-2", 0, 1 ),
    new CheckData(  "-1", 1, 0 ),

    new CheckData(  "0x1000000000000000000000000000000000000000000000000000", 0, 1 ),
    new CheckData(  "0x1000000000000000000000000000000000000000000000000001", 1, 0 ),
    new CheckData(  "0x1000000000000000000000000000000000000000000000000002", 0, 1 ),
    new CheckData(  "0x1000000000000000000000000000000000000000000000000003", 1, 0 ),

    new CheckData( "-0x1000000000000000000000000000000000000000000000000004", 0, 1 ),
    new CheckData( "-0x1000000000000000000000000000000000000000000000000003", 1, 0 ),
    new CheckData( "-0x1000000000000000000000000000000000000000000000000002", 0, 1 ),
    new CheckData( "-0x1000000000000000000000000000000000000000000000000001", 1, 0 ),
    };

    private void check_data()
        throws Exception
    {
        mpz_t  n = new mpz_t();
        int i;

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (n, data[i].n, 0);

            if (GMP.mpz_odd_p (n) != data[i].odd) {
                dump_abort ("mpz_odd_p wrong on data[" + i + "]");
            }

            if (GMP.mpz_even_p (n) != data[i].even) {
                dump_abort ("mpz_even_p wrong on data[" + i + "]");
	            }
        }

    }

    protected Integer doInBackground(Integer... params)
    {
        int ret = 0;

        try {
            GMP.init();
            //tests_start ();
            
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
        return ret;
    }

    protected void onPreExecute()
    {
        uinterface.display(TAG);
    }

    protected void onProgressUpdate(Integer... progress)
    {
        uinterface.display("progress=" + progress[0]);
    }

    protected void onPostExecute(Integer result)
    {
        uinterface.display("result=" + result);
        if (result == 0) {
            uinterface.display("PASS");
            uinterface.nextTask();
        } else {
            uinterface.display(failmsg);
            uinterface.display("FAIL");
        }
    }

    protected void onCancelled(Integer result)
    {
        uinterface.display("result=" + result);
        uinterface.display(failmsg);
        uinterface.display("FAIL");
    }

    private String failmsg;

    private void dump_abort(String msg)
        throws Exception
    {
        throw new Exception(msg);
    }

}
