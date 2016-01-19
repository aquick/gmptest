package org.gmplib.test;

//import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Set_Str_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Set_Str_Task";
    
    public Set_Str_Task(UI ui)
    {
        super(ui, TAG);
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

    public void run()
    {
        mpz_t z;
        int ret = 0;

        if (!isActive()) {
            return;
        }
        onPreExecute();
        try {
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
        onPostExecute(Integer.valueOf(ret));
    }

    private void dump_abort(String msg)
        throws Exception
    {
        String emsg = "ERROR: " + msg;
        throw new Exception(emsg);
    }
}
