package org.gmplib.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.GMPException;
import org.gmplib.gmpjni.GMP.mpq_t;
//import org.gmplib.gmpjni.GMP.mpz_t;
//import org.gmplib.gmpjni.GMP.mpf_t;
//import org.gmplib.gmpjni.GMP.randstate_t;

import android.os.Environment;
import android.util.Log;

public class Mpq_Input_Str_Task extends TaskBase implements Runnable {

    private static final String TAG = "Mpq_Input_Str_Task";
    
    private static final String FILENAME = "t-inp_str.txt";
    
    public Mpq_Input_Str_Task(UI ui)
    {
        super(ui, TAG);
    }

    private static final class CheckData
    {
	public int         base;
	public int         want_nread;
	public String      inp;
	public String      want;

        public CheckData(String inp, int base, String want, int want_nread)
        {
            this.base = base;
            this.want_nread = want_nread;
            this.inp = inp;
            this.want = want;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {
	new CheckData( "0",   10, "0", 1 ),
	new CheckData( "0/1", 10, "0", 3 ),

	new CheckData( "0/",   10, "0", 0 ),
	new CheckData( "/123", 10, "0", 0 ),
	new CheckData( "blah", 10, "0", 0 ),
	new CheckData( "123/blah", 10, "0", 0 ),
	new CheckData( "5 /8", 10, "5", 1 ),
	new CheckData( "5/ 8", 10, "0", 0 ),

	new CheckData( "ff", 16,  "255", 2 ),
	new CheckData( "-ff", 16, "-255", 3 ),
	new CheckData( "FF", 16,  "255", 2 ),
	new CheckData( "-FF", 16, "-255", 3 ),

	new CheckData( "z", 36, "35", 1 ),
	new CheckData( "Z", 36, "35", 1 ),

	new CheckData( "0x0",    0,   "0", 3 ),
	new CheckData( "0x10",   0,  "16", 4 ),
	new CheckData( "-0x0",    0,   "0", 4 ),
	new CheckData( "-0x10",   0, "-16", 5 ),
	new CheckData( "-0x10/5", 0, "-16/5", 7 ),

	new CheckData( "00",   0,  "0", 2 ),
	new CheckData( "010",  0,  "8", 3 ),
	new CheckData( "-00",   0,  "0", 3 ),
	new CheckData( "-010",  0, "-8", 4 )
    };
    
    private void check_data ()
        throws Exception
    {
        mpq_t  got;
        mpq_t  want;
        //long   ftell_nread;
        int    i;
        int    post;
        int    j;
        long   got_nread;
        BufferedWriter fp;
        File   f;
        String root;
        String fname;
        
        got = new mpq_t();
        want = new mpq_t();
        
        for (i = 0; i < data.length; i++) {
            for (post = 0; post <= 2; post++) {
                GMP.mpq_set_str (want, data[i].want, 0);
                GMP.mpq_internal_CHECK_FORMAT (want);

                root = Environment.getExternalStorageDirectory().getPath() + "/Android/data/org.gmplib.test/files/";
                fname = root + FILENAME;
                fp = new BufferedWriter(new FileWriter(fname, false));
                fp.write (data[i].inp);
                for (j = 0; j < post; j++) {
                    fp.write (' ');
                }
                fp.flush();

                fp.close();
                got_nread = GMP.mpq_inp_str (got, fname, data[i].base);

                f = new File(fname);
                /***
                // ???
                // mpq_inp_str is not returning a length that includes the spaces at the end
                // of the input.  If it did, the check below for got_nread == want_nread would fail.
                if (got_nread != 0) {
                    ftell_nread = f.length();
                    if (got_nread != ftell_nread) {
                        printf ("mpq_inp_str nread wrong\n");
                        printf ("  inp          \"%s\"\n", data[i].inp);
                        printf ("  base         %d\n", data[i].base);
                        printf ("  got_nread    %d\n", got_nread);
                        printf ("  ftell_nread  %ld\n", ftell_nread);
                        abort ();
                	dump_abort("mpq_inp_str nread wrong" + " inp=" + data[i].inp + " base=" + data[i].base +
                		" got_nread=" + got_nread + " ftell_nread=" + ftell_nread);
                    }
                }

                if (post == 0 && data[i].want_nread == strlen(data[i].inp)) {
                    int  c = getc(fp);
                    if (c != EOF) {
                        printf ("mpq_inp_str didn't read to EOF\n");
                        printf ("  inp         \"%s\"\n", data[i].inp);
                        printf ("  base        %d\n", data[i].base);
                        printf ("  c '%c' %#x\n", c, c);
                        abort ();
                    }
                }
                ***/

                if (got_nread != data[i].want_nread) {
                    /***
                    printf ("mpq_inp_str nread wrong\n");
                    printf ("  inp         \"%s\"\n", data[i].inp);
                    printf ("  base        %d\n", data[i].base);
                    printf ("  got_nread   %d\n", got_nread);
                    printf ("  want_nread  %d\n", data[i].want_nread);
                    abort ();
                    ***/
            	    dump_abort("mpq_inp_str nread wrong" + " inp=" + data[i].inp + " base=" + data[i].base +
        		" got_nread=" + got_nread + " want_nread=" + data[i].want_nread);
                }

                GMP.mpq_internal_CHECK_FORMAT (got);

                if (GMP.mpq_equal (got, want) == 0) {
                    /***
                    printf ("mpq_inp_str wrong result\n");
                    printf ("  inp   \"%s\"\n", data[i].inp);
                    printf ("  base  %d\n", data[i].base);
                    mpq_trace ("  got ",  got);
                    mpq_trace ("  want", want);
                    abort ();
                    ***/
            	    dump_abort("mpq_inp_str wrong result" + " inp=" + data[i].inp + " base=" + data[i].base,
            	    	data[i].base, want, got);
                }
                f.delete();

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

    private void dump_abort(String msg, int base, mpq_t want, mpq_t got)
        throws Exception
    {
        String want_str = "";
        String got_str = "";
        String emsg;
        try {
            want_str = GMP.mpq_get_str(want, base);
        }
        catch (GMPException e) {
            want_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            got_str = GMP.mpq_get_str(got, base);
        }
        catch (GMPException e) {
            got_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = "ERROR: " + msg + " want=" + want_str + " got=" + got_str;
        throw new Exception(emsg);
    }

}
