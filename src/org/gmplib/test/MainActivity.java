package org.gmplib.test;

import android.content.Context;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//import java.lang.reflect.Array;
import java.util.StringTokenizer;

import org.gmplib.gmpjni.GMP;

public class MainActivity extends Activity implements UI {

    private TextView mView;
    private Button mButton;
    private Button mCancel;
    private Button mClear;
    private PowerManager.WakeLock mWakeLock;
    private RandomNumberFile rng;
    private Object[] tasks;
    private int numTasks;
    private int currTask;
    private String randfname = "2010-03-02.hex.txt";
    private int base = 16;
    private static final String TAG = "MainActivity";
    
    protected void initRandom() throws IOException
    {
        int n = 0;
        String root = this.getExternalFilesDir(null).getPath();
        String fname = root + "/.randseed";
        BufferedReader fin = new BufferedReader(new FileReader(fname));
        String line = fin.readLine();
        fin.close();
        if (line.length() > 0) {
            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens()) {
                randfname = st.nextToken();
                if (st.hasMoreTokens()) {
                    base = Integer.parseInt(st.nextToken());
                    if (st.hasMoreTokens()) {                        
                        n = Integer.parseInt(st.nextToken());
                    }
                }
            }
        }
        rng = new RandomNumberFile(root + "/" + randfname, base);
        rng.skip(n);
    }
    
    protected void finiRandom() throws IOException
    {
        if (rng != null) {
            long consumed = rng.consumed();
            rng.close();
            String root = this.getExternalFilesDir(null).getPath();
            String fname = root + "/.randseed";
            BufferedWriter fout = new BufferedWriter(new FileWriter(fname));
            fout.write(randfname + " " + base + " " + consumed);
            fout.close();
            rng = null;
        }
    }

    public void display(String line)
    {
        mView.append(line);
        mView.append("\n");
    }
    
    public void clearDisplay()
    {
        mView.setText("");
    }
    
    public void nextTask()
    {
        currTask++;
        if (currTask < numTasks) {
            AsyncTask<Integer, Integer, Integer> task = (AsyncTask<Integer, Integer, Integer>)tasks[currTask];
            task.execute();
        }
        if (currTask >= numTasks) {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            try {
                finiRandom();
            }
            catch (IOException e) {
                Log.d(TAG, "EXCEPTION: " + e.toString());
            }
        }
    }
    
    protected void initTasks()
    {
        currTask = 0;
        numTasks = 0;
        try {
            GMP.init();
            display(getString(R.string.testing) + " " + GMP.getVersion());
            initRandom();
            /***/
            tasks[numTasks++] = new TDiv_UI_Task(this, this.rng);
            tasks[numTasks++] = new TDiv_Task(this, this.rng);
            tasks[numTasks++] = new SqrtRem_Task(this, this.rng);
            tasks[numTasks++] = new Set_Str_Task(this, this.rng);
            tasks[numTasks++] = new Set_SI_Task(this, this.rng);
            tasks[numTasks++] = new Set_F_Task(this, this.rng);
            tasks[numTasks++] = new Set_D_Task(this, this.rng);
            tasks[numTasks++] = new Scan_Task(this, this.rng);
            tasks[numTasks++] = new Root_Task(this, this.rng);
            tasks[numTasks++] = new Remove_Task(this, this.rng);
            tasks[numTasks++] = new Primorial_UI_Task(this, this.rng);
            tasks[numTasks++] = new PPrime_P_Task(this, this.rng);
            tasks[numTasks++] = new Powm_UI_Task(this, this.rng);
            tasks[numTasks++] = new Powm_Task(this, this.rng);
            tasks[numTasks++] = new Pow_Task(this, this.rng);
            tasks[numTasks++] = new PerfSqr_Task(this, this.rng);
            tasks[numTasks++] = new PerfPow_Task(this, this.rng);
            tasks[numTasks++] = new OddEven_Task(this, this.rng);
            tasks[numTasks++] = new NextPrime_Task(this, this.rng);
            tasks[numTasks++] = new Mul_I_Task(this, this.rng);
            tasks[numTasks++] = new Mul_Task(this, this.rng);
            tasks[numTasks++] = new MFac_UIUI_Task(this, this.rng);
            tasks[numTasks++] = new LucNum_UI_Task(this, this.rng);
            tasks[numTasks++] = new LCM_Task(this, this.rng);
            tasks[numTasks++] = new Jacobi_Task(this, this.rng);
            tasks[numTasks++] = new Invert_Task(this, this.rng);
            tasks[numTasks++] = new PopCount_Task(this, this.rng);
            tasks[numTasks++] = new HamDist_Task(this, this.rng);
            tasks[numTasks++] = new Get_SI_Task(this, this.rng);
            tasks[numTasks++] = new Get_D_Task(this, this.rng);
            tasks[numTasks++] = new Get_D_2Exp_Task(this, this.rng);
            /***/
            tasks[numTasks++] = new GCD_UI_Task(this, this.rng);
            tasks[numTasks++] = new GCD_Task(this, this.rng);
            tasks[numTasks++] = new Fits_Task(this, this.rng);
            tasks[numTasks++] = new Fib_UI_Task(this, this.rng);
            tasks[numTasks++] = new FDiv_UI_Task(this, this.rng);
            tasks[numTasks++] = new FDiv_Task(this, this.rng);
            tasks[numTasks++] = new Fac_UI_Task(this, this.rng);
            tasks[numTasks++] = new Divis_2Exp_Task(this, this.rng);
            tasks[numTasks++] = new Divis_Task(this, this.rng);
            tasks[numTasks++] = new Div_2Exp_Task(this, this.rng);
            tasks[numTasks++] = new Cong_2Exp_Task(this, this.rng);
            tasks[numTasks++] = new Cong_Task(this, this.rng);
            tasks[numTasks++] = new Cmp_SI_Task(this, this.rng);
            tasks[numTasks++] = new Cmp_D_Task(this, this.rng);
            tasks[numTasks++] = new Cmp_Task(this, this.rng);
            tasks[numTasks++] = new CDiv_UI_Task(this, this.rng);
            tasks[numTasks++] = new Bin_Task(this, this.rng);
            tasks[numTasks++] = new AddSub_Task(this, this.rng);
            tasks[numTasks++] = new AddOrSubMul_Task(this, this.rng);
        }
	catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());	    
            display("EXCEPTION: " + e.toString());
	}
    }
    
    protected void executeCurrentTask()
    {
        if (currTask == 0) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }
        }
        if (currTask < numTasks) {
            AsyncTask<Integer, Integer, Integer> task = (AsyncTask<Integer, Integer, Integer>)tasks[currTask];
            task.execute();
        }
    }

    protected void cancelCurrentTask()
    {
        if (currTask < numTasks) {
            AsyncTask<Integer, Integer, Integer> task = (AsyncTask<Integer, Integer, Integer>)tasks[currTask];
            task.cancel(false);
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        try {
            finiRandom();
        }
        catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mView = (TextView) findViewById(R.id.TextView01);
        mButton = (Button) findViewById(R.id.Button01);
        mCancel = (Button) findViewById(R.id.Button02);
        mClear = (Button) findViewById(R.id.Button03);
        tasks = new Object[100];
        try {
            TestUtil.initprimes(65535);
            mButton.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.initTasks();
                            MainActivity.this.executeCurrentTask();
                        }
                    });
            mCancel.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.cancelCurrentTask();
                        }
                    });
            mClear.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.clearDisplay();
                        }
                    });
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        }
        catch (Exception e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());
            display("EXCEPTION: " + e.toString());
        }
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        cancelCurrentTask();
        try {
            finiRandom();
        }
        catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());
        }
        mWakeLock = null;
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause");
        /***
        cancelCurrentTask();
        try {
            finiRandom();
        }
        catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());
        }
        ***/
        super.onPause();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        /***
        try {
            if (rng == null) {
                initRandom();
            }
        }
        catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());
        }
        ***/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
