package org.gmplib.test;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;
//import java.io.IOException;

public class Set_Str_Task extends AsyncTask<Integer, Integer, Integer>
{
    private static final String TAG = "Set_Str_Task";
    
    private UI uinterface;
    private RandomNumberFile rng;
    
    public Set_Str_Task(UI ui, RandomNumberFile rng)
    {
        super();
        this.uinterface = ui;
        this.rng = rng;
        failmsg = null;
    }

    private void check_one (mpz_t want, int fail, int base, String str)
        throws Exception
    {
        mpz_t got = new mpz_t();
        try {
            GMP.mpz_set_str (got, str, base);
            if (fail != 0) {
                dump_abort("mpz_set_str unexpectedly failed " +
                           "  base " + base +
                           "  str  \"" + str + "\"");
            }
        }
        catch (GMPException e) {
            if (fail == 0) {
                dump_abort("mpz_set_str unexpectedly failed " +
                           "  base " + base +
                           "  str  \"" + str + "\"");
            }
        }

        if (fail == 0 && GMP.mpz_cmp (got, want) != 0) {
            dump_abort("mpz_set_str wrong " +
                           "  base " + base +
                           "  str  \"" + str + "\"" +
                           "  got " + GMP.mpz_get_str(got, base) +
                           "  want " + GMP.mpz_get_str(want, base));
        }
    }

    protected Integer doInBackground(Integer... params)
    {
        mpz_t z;
        int ret = 0;

        try {
            GMP.init();

            z = new mpz_t();
            GMP.mpz_set_ui (z, 0L);
            check_one (z, 0, 0, "0 ");
            check_one (z, 0, 0, " 0 0 0 ");
            check_one (z, 0, 0, " -0B 0 ");
            check_one (z, 0, 0, "  0X 0 ");
            check_one (z, 0, 10, "0 ");
            check_one (z, 0, 10, "-0   ");
            check_one (z, 0, 10, " 0 000 000    ");

            GMP.mpz_set_ui (z, 123L);
            check_one (z, 0, 0, "123 ");
            check_one (z, 0, 0, "123    ");
            check_one (z, 0, 0, "0173   ");
            check_one (z, 0, 0, " 0b 1 11 10 11  ");
            check_one (z, 0, 0, " 0x 7b ");
            check_one (z, 0, 0, "0x7B");
            check_one (z, 0, 10, "123 ");
            check_one (z, 0, 10, "123    ");
            check_one (z, 0, 0, " 123 ");
            check_one (z, 0, 0, "  123    ");
            check_one (z, 0, 10, "  0000123 ");
            check_one (z, 0, 10, "  123    ");
            check_one (z,-1, 10, "1%");
            check_one (z,-1, 0, "3!");
            check_one (z,-1, 0, "0123456789");
            check_one (z,-1, 0, "13579BDF");
            check_one (z,-1, 0, "0b0102");
            check_one (z,-1, 0, "0x010G");
            check_one (z,-1, 37,"0x010G");
            check_one (z,-1, 99,"0x010G");
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
        String emsg = "ERROR: " + msg;
        throw new Exception(emsg);
    }
}
