package org.gmplib.test;

import android.util.Log;

import org.gmplib.gmpjni.GMP;
import org.gmplib.gmpjni.GMP.mpz_t;
import org.gmplib.gmpjni.GMP.randstate_t;
import org.gmplib.gmpjni.GMP.GMPException;

public class Jacobi_Task extends TaskBase implements Runnable
{
    private static final String TAG = "Jacobi_Task";
    
    public Jacobi_Task(UI ui)
    {
        super(ui, TAG);
    }

    private long mpz_mod4 (mpz_t z)
        throws GMPException
    {
        mpz_t m;
        long  ret;

        m = new mpz_t();
        GMP.mpz_fdiv_r_2exp (m, z, 2);
        ret = GMP.mpz_get_ui (m);
        return ret;
    }

    private boolean mpz_fits_ulimb_p (mpz_t z)
        throws GMPException
    {
        return (GMP.mpz_internal_SIZ(z) == 1 || GMP.mpz_internal_SIZ(z) == 0);
    }

    private void try_base (long a, long b, int answer)
        throws Exception
    {
        int  got;

        if ((b & 1) == 0 || b == 1 || a > b) return;

        got = TestUtil.jacobi (a, b);
        if (got != answer) {
            /***
            printf (LL("mpn_jacobi_base (%lu, %lu) is %d should be %d\n",
                "mpn_jacobi_base (%llu, %llu) is %d should be %d\n"),
                a, b, got, answer);
            abort ();
            ***/
            throw new Exception("jacobi_base (" + a + ", " + b + ") is " + got + " should be " + answer);
        }
    }

    private void try_zi_ui (mpz_t a, long b, int answer)
        throws Exception
    {
        int  got;

        got = GMP.mpz_kronecker_ui (a, b);
        if (got != answer) {
            /***
            printf ("mpz_kronecker_ui (");
            mpz_out_str (stdout, 10, a);
            printf (", %lu) is %d should be %d\n", b, got, answer);
            abort ();
            ***/
            dump_abort ("mpz_kronecker_ui", a, b, got, answer);
        }
    }


    private void try_zi_si (mpz_t a, int b, int answer)
        throws Exception
    {
        int  got;

        got = GMP.mpz_kronecker_si (a, b);
        if (got != answer) {
            /***
            printf ("mpz_kronecker_si (");
            mpz_out_str (stdout, 10, a);
            printf (", %ld) is %d should be %d\n", b, got, answer);
            abort ();
            ***/
            dump_abort ("mpz_kronecker_si", a, (long)b, got, answer);
        }
    }


    private void try_ui_zi (long a, mpz_t b, int answer)
        throws Exception
    {
        int  got;

        got = GMP.mpz_ui_kronecker (a, b);
        if (got != answer) {
            /***
            printf ("mpz_ui_kronecker (%lu, ", a);
            mpz_out_str (stdout, 10, b);
            printf (") is %d should be %d\n", got, answer);
            abort ();
            ***/
            dump_abort2 ("mpz_ui_kronecker", a, b, got, answer);
        }
    }


    private void try_si_zi (int a, mpz_t b, int answer)
        throws Exception
    {
        int  got;

        got = GMP.mpz_si_kronecker (a, b);
        if (got != answer) {
            /***
            printf ("mpz_si_kronecker (%ld, ", a);
            mpz_out_str (stdout, 10, b);
            printf (") is %d should be %d\n", got, answer);
            abort ();
            ***/
            dump_abort2 ("mpz_si_kronecker", (long)a, b, got, answer);
        }
    }

    /* Don't bother checking mpz_jacobi, since it only differs for b even, and
       we don't have an actual expected answer for it.  tests/devel/try.c does
       some checks though.  */
    private void try_zi_zi (mpz_t a, mpz_t b, int answer)
        throws Exception
    {
        int  got;

        got = GMP.mpz_kronecker (a, b);
        if (got != answer) {
            /***
            printf ("mpz_kronecker (");
            mpz_out_str (stdout, 10, a);
            printf (", ");
            mpz_out_str (stdout, 10, b);
            printf (") is %d should be %d\n", got, answer);
            abort ();
            ***/
            dump_abort3 ("mpz_kronecker", a, b, got, answer);
        }
    }

    private void try_each (mpz_t a, mpz_t b, int answer)
        throws Exception
    {
        if (mpz_fits_ulimb_p (a) && mpz_fits_ulimb_p (b)) {
            try_base (GMP.mpz_internal_get_ulimb (a, 0), GMP.mpz_internal_get_ulimb (b, 0), answer);
        }

        if (GMP.mpz_fits_ulong_p (b) != 0) {
            try_zi_ui (a, GMP.mpz_get_ui (b), answer);
        }

        if (GMP.mpz_fits_slong_p (b) != 0) {
            try_zi_si (a, GMP.mpz_get_si (b), answer);
        }

        if (GMP.mpz_fits_ulong_p (a) != 0) {
            try_ui_zi (GMP.mpz_get_ui (a), b, answer);
        }

        if (GMP.mpz_fits_sint_p (a) != 0) {
            try_si_zi (GMP.mpz_get_si (a), b, answer);
        }

        try_zi_zi (a, b, answer);
    }

    /* Try (a/b) and (a/-b). */
    private void try_pn (mpz_t a, mpz_t b_orig, int answer)
        throws Exception
    {
        mpz_t  b = new mpz_t();

        GMP.mpz_set (b, b_orig);
        try_each (a, b, answer);

        GMP.mpz_neg (b, b);
        if (GMP.mpz_sgn (a) < 0) {
            answer = -answer;
        }

        try_each (a, b, answer);
    }


    /* Try (a+k*p/b) for various k, using the fact (a/b) is periodic in a with
       period p.  For b>0, p=b if b!=2mod4 or p=4*b if b==2mod4. */

    private void try_periodic_num (mpz_t a_orig, mpz_t b, int answer)
        throws Exception
    {
        mpz_t  a = new mpz_t();
        mpz_t  a_period = new mpz_t();
        int    i;

        if (GMP.mpz_sgn (b) <= 0) return;

        GMP.mpz_set (a, a_orig);
        GMP.mpz_set (a_period, b);
        if (mpz_mod4 (b) == 2) {
            GMP.mpz_mul_ui (a_period, a_period, 4L);
        }

        /* don't bother with these tests if they're only going to produce
           even/even */
        if (GMP.mpz_even_p (a) != 0 && GMP.mpz_even_p (b) != 0 && GMP.mpz_even_p (a_period) != 0) return;

        for (i = 0; i < 6; i++) {
            GMP.mpz_add (a, a, a_period);
            try_pn (a, b, answer);
        }

        GMP.mpz_set (a, a_orig);
        for (i = 0; i < 6; i++) {
            GMP.mpz_sub (a, a, a_period);
            try_pn (a, b, answer);
        }

    }


    /* Try (a/b+k*p) for various k, using the fact (a/b) is periodic in b of
       period p.

 		                              	period p
        	   a==0,1mod4             a
        	   a==2mod4              4*a
        	   a==3mod4 and b odd    4*a
	           a==3mod4 and b even   8*a

       In Henri Cohen's book the period is given as 4*a for all a==2,3mod4, but
       a counterexample would seem to be (3/2)=-1 which with (3/14)=+1 doesn't
       have period 4*a (but rather 8*a with (3/26)=-1).  Maybe the plain 4*a is
       to be read as applying to a plain Jacobi symbol with b odd, rather than
       the Kronecker extension to b even. */

    private void try_periodic_den (mpz_t a, mpz_t b_orig, int answer)
        throws Exception
    {
        mpz_t  b = new mpz_t();
        mpz_t  b_period = new mpz_t();
        int    i;

        if (GMP.mpz_sgn (a) == 0 || GMP.mpz_sgn (b_orig) == 0) return;

        GMP.mpz_set (b, b_orig);

        GMP.mpz_set (b_period, a);
        if (mpz_mod4 (a) == 3 && GMP.mpz_even_p (b) != 0) {
            GMP.mpz_mul_ui (b_period, b_period, 8L);
        } else if (mpz_mod4 (a) >= 2) {
            GMP.mpz_mul_ui (b_period, b_period, 4L);
        }

        /* don't bother with these tests if they're only going to produce
           even/even */
        if (GMP.mpz_even_p (a) != 0 && GMP.mpz_even_p (b) != 0 && GMP.mpz_even_p (b_period) != 0) return;

        for (i = 0; i < 6; i++) {
            GMP.mpz_add (b, b, b_period);
            try_pn (a, b, answer);
        }

        GMP.mpz_set (b, b_orig);
        for (i = 0; i < 6; i++) {
            GMP.mpz_sub (b, b, b_period);
            try_pn (a, b, answer);
        }

    }

    private static final long[]  ktable = new long[] {
        0, 1, 2, 3, 4, 5, 6, 7,
        GMP.GMP_NUMB_BITS()-1, GMP.GMP_NUMB_BITS(), GMP.GMP_NUMB_BITS()+1,
        2*GMP.GMP_NUMB_BITS()-1, 2*GMP.GMP_NUMB_BITS(), 2*GMP.GMP_NUMB_BITS()+1,
        3*GMP.GMP_NUMB_BITS()-1, 3*GMP.GMP_NUMB_BITS(), 3*GMP.GMP_NUMB_BITS()+1
    };

    /* Try (a/b*2^k) for various k. */
    private void try_2den (mpz_t a, mpz_t b_orig, int answer)
        throws Exception
    {
        mpz_t  b = new mpz_t();
        int    kindex;
        int    answer_a2;
        int    answer_k;
        long k;

        /* don't bother when b==0 */
        if (GMP.mpz_sgn (b_orig) == 0) return;

        GMP.mpz_set (b, b_orig);

        /* (a/2) is 0 if a even, 1 if a==1 or 7 mod 8, -1 if a==3 or 5 mod 8 */
        answer_a2 = (GMP.mpz_even_p (a) != 0 ? 0
	             : (((GMP.mpz_internal_SIZ(a) >= 0 ? GMP.mpz_internal_get_ulimb(a, 0) : -GMP.mpz_internal_get_ulimb(a, 0)) + 2) & 7) < 4 ? 1
	             : -1);

        for (kindex = 0; kindex < ktable.length; kindex++) {
            k = ktable[kindex];

            /* answer_k = answer*(answer_a2^k) */
            answer_k = (answer_a2 == 0 && k != 0 ? 0
		                : (k & 1) == 1 && answer_a2 == -1 ? -answer
		                : answer);

            GMP.mpz_mul_2exp (b, b_orig, k);
            try_pn (a, b, answer_k);
        }

    }


    /* Try (a*2^k/b) for various k.  If it happens mpz_ui_kronecker() gets (2/b)
       wrong it will show up as wrong answers demanded. */
    private void try_2num (mpz_t a_orig, mpz_t b, int answer)
        throws Exception
    {
        mpz_t  a = new mpz_t();
        int    kindex;
        int    answer_2b;
        int    answer_k;
        long   k;

        /* don't bother when a==0 */
        if (GMP.mpz_sgn (a_orig) == 0) return;

        /* (2/b) is 0 if b even, 1 if b==1 or 7 mod 8, -1 if b==3 or 5 mod 8 */
        answer_2b = (GMP.mpz_even_p (b) != 0 ? 0
	             : (((GMP.mpz_internal_SIZ(b) >= 0 ? GMP.mpz_internal_get_ulimb(b, 0) : -GMP.mpz_internal_get_ulimb(b, 0)) + 2) & 7) < 4 ? 1
	             : -1);

        for (kindex = 0; kindex < ktable.length; kindex++) {
            k = ktable[kindex];

            /* answer_k = answer*(answer_2b^k) */
            answer_k = (answer_2b == 0 && k != 0 ? 0
		                : (k & 1) == 1 && answer_2b == -1 ? -answer
		                : answer);

            GMP.mpz_mul_2exp (a, a_orig, k);
            try_pn (a, b, answer_k);
        }
    }


    /* The try_2num() and try_2den() routines don't in turn call
       try_periodic_num() and try_periodic_den() because it hugely increases the
       number of tests performed, without obviously increasing coverage.

       Useful extra derived cases can be added here. */

    private void try_all (mpz_t a, mpz_t b, int answer)
        throws Exception
    {
        try_pn (a, b, answer);
        try_periodic_num (a, b, answer);
        try_periodic_den (a, b, answer);
        try_2num (a, b, answer);
        try_2den (a, b, answer);
    }

    private static final class CheckData
    {
        public String a;
        public String b;
        public int    answer;

        public CheckData(String a, String b, int answer)
        {
            this.a = a;
            this.b = b;
            this.answer = answer;
        }
    }

    private static final CheckData[] data = new CheckData[]
    {

    /* Note that the various derived checks in try_all() reduce the cases
       that need to be given here.  */

    /* some zeros */
    new CheckData(  "0",  "0", 0 ),
    new CheckData(  "0",  "2", 0 ),
    new CheckData(  "0",  "6", 0 ),
    new CheckData(  "5",  "0", 0 ),
    new CheckData( "24", "60", 0 ),

    /* (a/1) = 1, any a
       In particular note (0/1)=1 so that (a/b)=(a mod b/b). */
    new CheckData( "0", "1", 1 ),
    new CheckData( "1", "1", 1 ),
    new CheckData( "2", "1", 1 ),
    new CheckData( "3", "1", 1 ),
    new CheckData( "4", "1", 1 ),
    new CheckData( "5", "1", 1 ),

    /* (0/b) = 0, b != 1 */
    new CheckData( "0",  "3", 0 ),
    new CheckData( "0",  "5", 0 ),
    new CheckData( "0",  "7", 0 ),
    new CheckData( "0",  "9", 0 ),
    new CheckData( "0", "11", 0 ),
    new CheckData( "0", "13", 0 ),
    new CheckData( "0", "15", 0 ),

    /* (1/b) = 1 */
    new CheckData( "1",  "1", 1 ),
    new CheckData( "1",  "3", 1 ),
    new CheckData( "1",  "5", 1 ),
    new CheckData( "1",  "7", 1 ),
    new CheckData( "1",  "9", 1 ),
    new CheckData( "1", "11", 1 ),

    /* (-1/b) = (-1)^((b-1)/2) which is -1 for b==3 mod 4 */
    new CheckData( "-1",  "1",  1 ),
    new CheckData( "-1",  "3", -1 ),
    new CheckData( "-1",  "5",  1 ),
    new CheckData( "-1",  "7", -1 ),
    new CheckData( "-1",  "9",  1 ),
    new CheckData( "-1", "11", -1 ),
    new CheckData( "-1", "13",  1 ),
    new CheckData( "-1", "15", -1 ),
    new CheckData( "-1", "17",  1 ),
    new CheckData( "-1", "19", -1 ),

    /* (2/b) = (-1)^((b^2-1)/8) which is -1 for b==3,5 mod 8.
       try_2num() will exercise multiple powers of 2 in the numerator.  */
    new CheckData( "2",  "1",  1 ),
    new CheckData( "2",  "3", -1 ),
    new CheckData( "2",  "5", -1 ),
    new CheckData( "2",  "7",  1 ),
    new CheckData( "2",  "9",  1 ),
    new CheckData( "2", "11", -1 ),
    new CheckData( "2", "13", -1 ),
    new CheckData( "2", "15",  1 ),
    new CheckData( "2", "17",  1 ),

    /* (-2/b) = (-1)^((b^2-1)/8)*(-1)^((b-1)/2) which is -1 for b==5,7mod8.
       try_2num() will exercise multiple powers of 2 in the numerator, which
       will test that the shift in mpz_si_kronecker() uses unsigned not
       signed.  */
    new CheckData( "-2",  "1",  1 ),
    new CheckData( "-2",  "3",  1 ),
    new CheckData( "-2",  "5", -1 ),
    new CheckData( "-2",  "7", -1 ),
    new CheckData( "-2",  "9",  1 ),
    new CheckData( "-2", "11",  1 ),
    new CheckData( "-2", "13", -1 ),
    new CheckData( "-2", "15", -1 ),
    new CheckData( "-2", "17",  1 ),

    /* (a/2)=(2/a).
       try_2den() will exercise multiple powers of 2 in the denominator. */
    new CheckData(  "3",  "2", -1 ),
    new CheckData(  "5",  "2", -1 ),
    new CheckData(  "7",  "2",  1 ),
    new CheckData(  "9",  "2",  1 ),
    new CheckData(  "11", "2", -1 ),

    /* Harriet Griffin, "Elementary Theory of Numbers", page 155, various
       examples.  */
    new CheckData(   "2", "135",  1 ),
    new CheckData( "135",  "19", -1 ),
    new CheckData(   "2",  "19", -1 ),
    new CheckData(  "19", "135",  1 ),
    new CheckData( "173", "135",  1 ),
    new CheckData(  "38", "135",  1 ),
    new CheckData( "135", "173",  1 ),
    new CheckData( "173",   "5", -1 ),
    new CheckData(   "3",   "5", -1 ),
    new CheckData(   "5", "173", -1 ),
    new CheckData( "173",   "3", -1 ),
    new CheckData(   "2",   "3", -1 ),
    new CheckData(   "3", "173", -1 ),
    new CheckData( "253",  "21",  1 ),
    new CheckData(   "1",  "21",  1 ),
    new CheckData(  "21", "253",  1 ),
    new CheckData(  "21",  "11", -1 ),
    new CheckData(  "-1",  "11", -1 ),

    /* Griffin page 147 */
    new CheckData(  "-1",  "17",  1 ),
    new CheckData(   "2",  "17",  1 ),
    new CheckData(  "-2",  "17",  1 ),
    new CheckData(  "-1",  "89",  1 ),
    new CheckData(   "2",  "89",  1 ),

    /* Griffin page 148 */
    new CheckData(  "89",  "11",  1 ),
    new CheckData(   "1",  "11",  1 ),
    new CheckData(  "89",   "3", -1 ),
    new CheckData(   "2",   "3", -1 ),
    new CheckData(   "3",  "89", -1 ),
    new CheckData(  "11",  "89",  1 ),
    new CheckData(  "33",  "89", -1 ),

    /* H. Davenport, "The Higher Arithmetic", page 65, the quadratic
       residues and non-residues mod 19.  */
    new CheckData(  "1", "19",  1 ),
    new CheckData(  "4", "19",  1 ),
    new CheckData(  "5", "19",  1 ),
    new CheckData(  "6", "19",  1 ),
    new CheckData(  "7", "19",  1 ),
    new CheckData(  "9", "19",  1 ),
    new CheckData( "11", "19",  1 ),
    new CheckData( "16", "19",  1 ),
    new CheckData( "17", "19",  1 ),
    new CheckData(  "2", "19", -1 ),
    new CheckData(  "3", "19", -1 ),
    new CheckData(  "8", "19", -1 ),
    new CheckData( "10", "19", -1 ),
    new CheckData( "12", "19", -1 ),
    new CheckData( "13", "19", -1 ),
    new CheckData( "14", "19", -1 ),
    new CheckData( "15", "19", -1 ),
    new CheckData( "18", "19", -1 ),

    /* Residues and non-residues mod 13 */
    new CheckData(  "0",  "13",  0 ),
    new CheckData(  "1",  "13",  1 ),
    new CheckData(  "2",  "13", -1 ),
    new CheckData(  "3",  "13",  1 ),
    new CheckData(  "4",  "13",  1 ),
    new CheckData(  "5",  "13", -1 ),
    new CheckData(  "6",  "13", -1 ),
    new CheckData(  "7",  "13", -1 ),
    new CheckData(  "8",  "13", -1 ),
    new CheckData(  "9",  "13",  1 ),
    new CheckData( "10",  "13",  1 ),
    new CheckData( "11",  "13", -1 ),
    new CheckData( "12",  "13",  1 ),

    /* various */
    new CheckData(  "5",   "7", -1 ),
    new CheckData( "15",  "17",  1 ),
    new CheckData( "67",  "89",  1 ),

    /* special values inducing a==b==1 at the end of jac_or_kron() */
    new CheckData( "0x10000000000000000000000000000000000000000000000001",
      "0x10000000000000000000000000000000000000000000000003", 1 ),

    /* Test for previous bugs in jacobi_2. */
    new CheckData( "0x43900000000", "0x42400000439", -1 ), /* 32-bit limbs */
    new CheckData( "0x4390000000000000000", "0x4240000000000000439", -1 ), /* 64-bit limbs */

    new CheckData( "198158408161039063", "198158360916398807", -1 ),

    /* Some tests involving large quotients in the continued fraction
       expansion. */
    new CheckData( "37200210845139167613356125645445281805",
      "451716845976689892447895811408978421929", -1 ),
    new CheckData( "67674091930576781943923596701346271058970643542491743605048620644676477275152701774960868941561652032482173612421015",
      "4902678867794567120224500687210807069172039735", 0 ),
    new CheckData( "2666617146103764067061017961903284334497474492754652499788571378062969111250584288683585223600172138551198546085281683283672592", "2666617146103764067061017961903284334497474492754652499788571378062969111250584288683585223600172138551198546085281683290481773", 1 ),

    /* Exersizes the case asize == 1, btwos > 0 in mpz_jacobi. */
    new CheckData( "804609", "421248363205206617296534688032638102314410556521742428832362659824", 1 ) ,
    new CheckData( "4190209", "2239744742177804210557442048984321017460028974602978995388383905961079286530650825925074203175536427000", 1 ),

    /* Exersizes the case asize == 1, btwos = 63 in mpz_jacobi
       (relevant when GMP_LIMB_BITS == 64). */
    new CheckData( "17311973299000934401", "1675975991242824637446753124775689449936871337036614677577044717424700351103148799107651171694863695242089956242888229458836426332300124417011114380886016", 1 ),
    new CheckData( "3220569220116583677", "41859917623035396746", -1 ),

    /* Other test cases that triggered bugs during development. */
    new CheckData( "37200210845139167613356125645445281805", "340116213441272389607827434472642576514", -1 ),
    new CheckData( "74400421690278335226712251290890563610", "451716845976689892447895811408978421929", -1 )
    };

    private void check_data ()
        throws Exception
    {
        int    i;
        mpz_t  a = new mpz_t();
        mpz_t  b = new mpz_t();

        for (i = 0; i < data.length; i++) {
            GMP.mpz_set_str (a, data[i].a, 0);
            GMP.mpz_set_str (b, data[i].b, 0);
            try_all (a, b, data[i].answer);
        }

    }

    /* (a^2/b)=1 if gcd(a,b)=1, or (a^2/b)=0 if gcd(a,b)!=1.
       This includes when a=0 or b=0. */
    private void check_squares_zi (randstate_t rands)
        throws Exception
    {
        mpz_t  a = new mpz_t();
        mpz_t  b = new mpz_t();
        mpz_t  g = new mpz_t();
        int    i;
        int    answer;
        long   size_range;
        long   an;
        long   bn;
        mpz_t  bs = new mpz_t();

        for (i = 0; i < 50; i++) {
            GMP.mpz_urandomb (bs, rands, 32);
            size_range = GMP.mpz_get_ui (bs) % 10 + i/8 + 2;

            GMP.mpz_urandomb (bs, rands, size_range);
            an = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (a, rands, an);

            GMP.mpz_urandomb (bs, rands, size_range);
            bn = GMP.mpz_get_ui (bs);
            GMP.mpz_rrandomb (b, rands, bn);

            GMP.mpz_gcd (g, a, b);
            if (GMP.mpz_cmp_ui (g, 1L) == 0) {
                answer = 1;
            } else {
                answer = 0;
            }

            GMP.mpz_mul (a, a, a);

            try_all (a, b, answer);
        }

    }


    /* Check the handling of asize==0, make sure it isn't affected by the low
       limb. */
    private void check_a_zero ()
        throws Exception
    {
        mpz_t  a = new mpz_t();
        mpz_t  b = new mpz_t();

        GMP.mpz_set_ui (a, 0);

        GMP.mpz_set_ui (b, 1L);
        GMP.mpz_internal_set_ulimb(a, 0, 0);
        try_all (a, b, 1);   /* (0/1)=1 */
        GMP.mpz_internal_set_ulimb(a, 0, 1);
        try_all (a, b, 1);   /* (0/1)=1 */

        GMP.mpz_set_si (b, -1);
        GMP.mpz_internal_set_ulimb(a, 0, 0);
        try_all (a, b, 1);   /* (0/-1)=1 */
        GMP.mpz_internal_set_ulimb(a, 0, 1);
        try_all (a, b, 1);   /* (0/-1)=1 */

        GMP.mpz_set_ui (b, 0);
        GMP.mpz_internal_set_ulimb(a, 0, 0);
        try_all (a, b, 0);   /* (0/0)=0 */
        GMP.mpz_internal_set_ulimb(a, 0, 1);
        try_all (a, b, 0);   /* (0/0)=0 */

        GMP.mpz_set_ui (b, 2);
        GMP.mpz_internal_set_ulimb(a, 0, 0);
        try_all (a, b, 0);   /* (0/2)=0 */
        GMP.mpz_internal_set_ulimb(a, 0, 1);
        try_all (a, b, 0);   /* (0/2)=0 */

    }

    private static final int  PRIME_N = 10;
    private static final long PRIME_MAX_SIZE = 50;
    private static final int  PRIME_MAX_EXP = 4;
    private static final int  PRIME_A_COUNT = 10;
    private static final int  PRIME_B_COUNT = 5;
    private static final long PRIME_MAX_B_SIZE = 2000;

    private void check_jacobi_factored (randstate_t rands)
        throws Exception
    {
        mpz_t[] prime = new mpz_t[PRIME_N];
        int[] exp = new int[PRIME_N];
        mpz_t a = new mpz_t();
        mpz_t b = new mpz_t();
        mpz_t t = new mpz_t();
        mpz_t bs = new mpz_t();
        int i;

        /* Generate primes */
        for (i = 0; i < PRIME_N; i++) {
            long size;
            prime[i] = new mpz_t();
            GMP.mpz_urandomb (bs, rands, 32);
            size = GMP.mpz_get_ui (bs) % PRIME_MAX_SIZE + 2;
            GMP.mpz_rrandomb (prime[i], rands, size);
            if (GMP.mpz_cmp_ui (prime[i], 3) <= 0) {
                GMP.mpz_set_ui (prime[i], 3);
            } else {
                GMP.mpz_nextprime (prime[i], prime[i]);
            }
        }

        for (i = 0; i < PRIME_B_COUNT; i++) {
            int j, k;
            long bsize;

            GMP.mpz_set_ui (b, 1L);
            bsize = 1;

            for (j = 0; j < PRIME_N && bsize < PRIME_MAX_B_SIZE; j++) {
                GMP.mpz_urandomb (bs, rands, 32);
                exp[j] = (int)(GMP.mpz_get_ui (bs) % PRIME_MAX_EXP);
                GMP.mpz_pow_ui (t, prime[j], (long)exp[j]);
                GMP.mpz_mul (b, b, t);
                bsize = GMP.mpz_sizeinbase (b, 2);
            }
            for (k = 0; k < PRIME_A_COUNT; k++) {
                int answer;
                GMP.mpz_rrandomb (a, rands, bsize + 2);
                answer = TestUtil.jacobi (a, j, prime, exp);
                try_all (a, b, answer);
            }
        }
    }

    /* These tests compute (a|n), where the quotient sequence includes
       large quotients, and n has a known factorization. Such inputs are
       generated as follows. First, construct a large n, as a power of a
       prime p of moderate size.

       Next, compute a matrix from factors (q,1;1,0), with q chosen with
       uniformly distributed size. We must stop with matrix elements of
       roughly half the size of n. Denote elements of M as M = (m00, m01;
       m10, m11).

       We now look for solutions to

         n = m00 x + m01 y
         a = m10 x + m11 y

       with x,y > 0. Since n >= m00 * m01, there exists a positive
       solution to the first equation. Find those x, y, and substitute in
       the second equation to get a. Then the quotient sequence for (a|n)
       is precisely the quotients used when constructing M, followed by
       the quotient sequence for (x|y).

       Numbers should also be large enough that we exercise hgcd_jacobi,
       which means that they should be larger than

         max (GCD_DC_THRESHOLD, 3 * HGCD_THRESHOLD)

       With an n of roughly 40000 bits, this should hold on most machines.
    */

    private static final int COUNT = 50;
    private static final int PBITS = 200;
    private static final int PPOWER = 201;
    private static final int MAX_QBITS = 500;

    private void check_large_quotients (randstate_t rands)
        throws Exception
    {

        mpz_t p;
        mpz_t n;
        mpz_t q;
        mpz_t g;
        mpz_t s;
        mpz_t t;
        mpz_t x;
        mpz_t y;
        mpz_t bs;
        mpz_t[][] M = new mpz_t[2][2];
        long nsize;
        int i;

        p = new mpz_t();
        n = new mpz_t();
        q = new mpz_t();
        g = new mpz_t();
        s = new mpz_t();
        t = new mpz_t();
        x = new mpz_t();
        y = new mpz_t();
        bs = new mpz_t();
        M[0][0] = new mpz_t();
        M[0][1] = new mpz_t();
        M[1][0] = new mpz_t();
        M[1][1] = new mpz_t();

        /* First generate a number with known factorization, as a random
           smallish prime raised to an odd power. Then (a|n) = (a|p). */
        GMP.mpz_rrandomb (p, rands, PBITS);
        GMP.mpz_nextprime (p, p);
        GMP.mpz_pow_ui (n, p, PPOWER);

        nsize = GMP.mpz_sizeinbase (n, 2);

        for (i = 0; i < COUNT; i++) {
            //int j;
            //int chain_len;
            int answer;
            long msize;

            GMP.mpz_set_ui (M[0][0], 1L);
            GMP.mpz_set_ui (M[0][1], 0);
            GMP.mpz_set_ui (M[1][0], 0);
            GMP.mpz_set_ui (M[1][1], 1L);

            for (msize = 1; 2*(msize + MAX_QBITS) + 1 < nsize ;) {
                int ii;
                GMP.mpz_rrandomb (bs, rands, 32);
                GMP.mpz_rrandomb (q, rands, 1 + GMP.mpz_get_ui (bs) % MAX_QBITS);

                /* Multiply by (q, 1; 1,0) from the right */
                for (ii = 0; ii < 2; ii++) {
                    long size;
                    GMP.mpz_swap (M[ii][0], M[ii][1]);
                    GMP.mpz_addmul (M[ii][0], M[ii][1], q);
                    size = GMP.mpz_sizeinbase (M[ii][0], 2);
                    if (size > msize) msize = size;
                }
            }
            GMP.mpz_gcdext (g, s, t, M[0][0], M[0][1]);
            if (GMP.mpz_cmp_ui (g, 1L) != 0) {
                throw new Exception("Assertion failure: mpz_cmp_ui(g, 1) == 0");
            }

            /* Solve n = M[0][0] * x + M[0][1] * y */
            if (GMP.mpz_sgn (s) > 0) {
                GMP.mpz_mul (x, n, s);
                GMP.mpz_fdiv_qr (q, x, x, M[0][1]);
                GMP.mpz_mul (y, q, M[0][0]);
                GMP.mpz_addmul (y, t, n);
                if (GMP.mpz_sgn (y) <= 0) {
                    throw new Exception("Assertion failure: mpz_sgn(y) > 0");
                }
            } else {
                GMP.mpz_mul (y, n, t);
                GMP.mpz_fdiv_qr (q, y, y, M[0][0]);
                GMP.mpz_mul (x, q, M[0][1]);
                GMP.mpz_addmul (x, s, n);
                if (GMP.mpz_sgn (x) <= 0) {
                    throw new Exception("Assertion failure: mpz_sgn(x) > 0");
                }
            }
            GMP.mpz_mul (x, x, M[1][0]);
            GMP.mpz_addmul (x, y, M[1][1]);

            /* Now (x|n) has the selected large quotients */
            answer = TestUtil.legendre (x, p);
            try_zi_zi (x, n, answer);
        }
    }

    public void run()
    {
        int ret = 0;
        randstate_t rands;
        long seed;

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

            check_data ();
            check_squares_zi (rands);
            check_a_zero ();
            check_jacobi_factored (rands);
            check_large_quotients (rands);

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

    private void dump_abort(String msg,
                            mpz_t x, long y, int got, int answer )
        throws Exception
    {
        String x_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               "(" + x_str + ", " + y + ") is " + got + " should be " + answer;
        throw new Exception(emsg);
    }

    private void dump_abort2(String msg,
                             long y, mpz_t x, int got, int answer )
        throws Exception
    {
        String x_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               "(" + y + ", " + x_str + ") is " + got + " should be " + answer;
        throw new Exception(emsg);
    }

    private void dump_abort3(String msg,
                             mpz_t x, mpz_t y, int got, int answer )
        throws Exception
    {
        String x_str = "";
        String y_str = "";
        String emsg;
        try {
            x_str = GMP.mpz_get_str(x, 10);
        }
        catch (GMPException e) {
            x_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        try {
            y_str = GMP.mpz_get_str(y, 10);
        }
        catch (GMPException e) {
            y_str = "GMPException [" + e.getCode() + "] " + e.getMessage();
        }
        emsg = msg +
               "(" + x_str + ", " + y_str + ") is " + got + " should be " + answer;
        throw new Exception(emsg);
    }

}
