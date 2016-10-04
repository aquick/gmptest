package org.gmplib.test;

import android.content.Context;
import android.os.Bundle;
//import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//import java.lang.reflect.Array;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

import org.gmplib.gmpjni.GMP;

public class MainActivity extends Activity implements UI {

    private MyHandler mHandler;
    private ThreadPoolExecutor mThreadPool;
    private ArrayBlockingQueue<Runnable> mThreadWorkQueue;

    private TextView[] mView;
    private Button mTestMpz;
    private Button mTestMpq;
    private Button mCancel;
    private Button mClear;
    private ProgressBar[] mProgress;
    private PowerManager.WakeLock mWakeLock;
    private RandomNumberFile rng;
    private TaskBase[] tasks;
    private int numTasks;
    private String randfname = "2010-03-02.hex.txt";
    private int base = 16;
    private BufferedWriter logf = null;
    private static final String TAG = "MainActivity";
    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES;
    private static final int MAX_NUM_TASKS = 100;

    private class MyHandler extends Handler {
	
	private static final int MAX_NUM_VIEWS = 2;
	public static final int DISPLAY_INFO = 0;
	public static final int DISPLAY_PROGRESS = 1;
	public static final int RESET = 3;
	
        long[] tids;
        
        private void reset()
        {
            for (int i = 0; i < MAX_NUM_VIEWS; i++) {
        	tids[i] = 0;
            }
        }
        
        private int getIndex(long tid)
        {
            int ret = -1;
            if (tid <= 1) return ret;
            for (int i = 0; i < MAX_NUM_VIEWS; i++) {
        	if (tids[i] == 0) {
        	    tids[i] = tid;
        	    ret = i;
        	    break;
        	}
        	if (tid == tids[i]) {
        	    ret = i;
        	    break;
        	}
            }
            return ret;
        }
	
	public MyHandler(Looper looper)
	{
	    super(looper);
	    tids = new long[MAX_NUM_VIEWS];
	}

	@Override
        public void handleMessage(Message inputMessage)
        {
            int code = inputMessage.what;
            int i = inputMessage.arg1;
            int j = inputMessage.arg2;
            long tid = (long)(i << 32) | (long)j;
            int index = getIndex(tid);
            if (index < 0) {
        	index = 0;
            }
            switch (code) {
            case DISPLAY_INFO:
                StringBuffer sb = new StringBuffer();
                String msg = (String)inputMessage.obj;
                sb.append("[");
                if (i != 0) {
            	    sb.append(Integer.toString(i, 16));
                }
                if (j != 0) {
            	    sb.append(Integer.toString(j, 16));
                }
                sb.append("] ");
                sb.append(msg);
                sb.append("\n");
                MainActivity.this.mView[index].append(sb.toString());
                MainActivity.this.log(sb.toString());
        	break;
            case DISPLAY_PROGRESS:
        	int progress = ((Integer)inputMessage.obj).intValue();
        	MainActivity.this.mProgress[index].setProgress(progress);
        	break;
            case RESET:
        	reset();
        	try {
        	    MainActivity.this.finiLog();
        	}
        	catch (IOException e) {
                    Log.d(TAG + ".MyHandler", "EXCEPTION: " + e.toString());
        	}
        	break;
            default:
        	break;
            }
        }

    }
    
    private class MonitorThread extends Thread {
	
	public void run()
	{
	    Log.d(TAG + ".MonitorThread", "starting");
	    try {
	        for (;;) {
		    Thread.sleep(10000);
		    if (!MainActivity.this.isActive()) {
		        MainActivity.this.cleanup();
		        break;
		    }
	        }
	    }
	    catch (Exception e) {
	        Log.d(TAG + ".MonitorThread", "EXCEPTION: " + e.toString());	    
	    }
	    Log.d(TAG + ".MonitorThread", "terminating");
	}
    }
    
    protected void startMonitorThread()
    {
	(this.new MonitorThread()).start();
    }
    
    protected void cleanup() throws IOException
    {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
	if (getResult() == 0) {
	    display("ALL PASSED");
	} else {
	    display("AT LEAST ONE TEST FAILED");
	}
        stopLog();
        finiRandom();
        mThreadPool.shutdown();
        mThreadPool = null;
        mThreadWorkQueue.clear();
        mThreadWorkQueue = null;
        tasks = null;
    }
    
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
	long id = Thread.currentThread().getId();
	Message msg = mHandler.obtainMessage(
		MyHandler.DISPLAY_INFO,
		(int)((id >> 32) & 0xFFFFFFFF),
		(int)(id & 0xFFFFFFFF),
		line);
	mHandler.sendMessage(msg);
    }
    
    public void displayProgress(int pct)
    {
	long id = Thread.currentThread().getId();
	Message msg = mHandler.obtainMessage(
		MyHandler.DISPLAY_PROGRESS,
		(int)((id >> 32) & 0xFFFFFFFF),
		(int)(id & 0xFFFFFFFF),
		Integer.valueOf(pct));
	mHandler.sendMessage(msg);	
    }
    
    public void stopLog()
    {
	long id = Thread.currentThread().getId();
	Message msg = mHandler.obtainMessage(
		MyHandler.RESET,
		(int)((id >> 32) & 0xFFFFFFFF),
		(int)(id & 0xFFFFFFFF));
	mHandler.sendMessage(msg);	
    }
    
    public void clearDisplay()
    {
        mView[0].setText("");
        mView[1].setText("");
        mProgress[0].setProgress(0);
        mProgress[1].setProgress(0);
    }
    
    public synchronized long getSeed()
        throws IOException
    {
	return rng.nextInt();
    }
    
    protected void initTasks()
    {
        numTasks = 0;
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        try {
            initLog();
            display(getString(R.string.testing) + " " + GMP.getVersion());
            initRandom();
            mThreadWorkQueue = new ArrayBlockingQueue<Runnable>(MAX_NUM_TASKS);
            NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
            mThreadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    mThreadWorkQueue);
            tasks = new TaskBase[MAX_NUM_TASKS];
            /***/
            tasks[numTasks++] = new TDiv_UI_Task(this);
            tasks[numTasks++] = new TDiv_Task(this);
            tasks[numTasks++] = new SqrtRem_Task(this);
            tasks[numTasks++] = new Set_Str_Task(this);
            tasks[numTasks++] = new Set_SI_Task(this);
            tasks[numTasks++] = new Set_F_Task(this);
            tasks[numTasks++] = new Set_D_Task(this);
            tasks[numTasks++] = new Scan_Task(this);
            tasks[numTasks++] = new Root_Task(this);
            tasks[numTasks++] = new Remove_Task(this);
            tasks[numTasks++] = new Primorial_UI_Task(this);
            tasks[numTasks++] = new PPrime_P_Task(this);
            tasks[numTasks++] = new Powm_UI_Task(this);
            tasks[numTasks++] = new Powm_Task(this);
            tasks[numTasks++] = new Pow_Task(this);
            tasks[numTasks++] = new PerfSqr_Task(this);
            tasks[numTasks++] = new PerfPow_Task(this);
            tasks[numTasks++] = new OddEven_Task(this);
            tasks[numTasks++] = new NextPrime_Task(this);
            tasks[numTasks++] = new Mul_I_Task(this);
            tasks[numTasks++] = new Mul_Task(this);
            tasks[numTasks++] = new MFac_UIUI_Task(this);
            tasks[numTasks++] = new LucNum_UI_Task(this);
            tasks[numTasks++] = new LCM_Task(this);
            tasks[numTasks++] = new Jacobi_Task(this);
            tasks[numTasks++] = new Invert_Task(this);
            tasks[numTasks++] = new PopCount_Task(this);
            tasks[numTasks++] = new HamDist_Task(this);
            tasks[numTasks++] = new Get_SI_Task(this);
            tasks[numTasks++] = new Get_D_Task(this);
            tasks[numTasks++] = new Get_D_2Exp_Task(this);
            tasks[numTasks++] = new GCD_UI_Task(this);
            tasks[numTasks++] = new GCD_Task(this);
            tasks[numTasks++] = new Fits_Task(this);
            tasks[numTasks++] = new Fib_UI_Task(this);
            tasks[numTasks++] = new FDiv_UI_Task(this);
            tasks[numTasks++] = new FDiv_Task(this);
            tasks[numTasks++] = new Fac_UI_Task(this);
            tasks[numTasks++] = new Divis_2Exp_Task(this);
            tasks[numTasks++] = new Divis_Task(this);
            tasks[numTasks++] = new Div_2Exp_Task(this);
            tasks[numTasks++] = new Cong_2Exp_Task(this);
            tasks[numTasks++] = new Cong_Task(this);
            tasks[numTasks++] = new Cmp_SI_Task(this);
            tasks[numTasks++] = new Cmp_D_Task(this);
            tasks[numTasks++] = new Cmp_Task(this);
            tasks[numTasks++] = new CDiv_UI_Task(this);
            tasks[numTasks++] = new Bin_Task(this);
            tasks[numTasks++] = new AddSub_Task(this);
            tasks[numTasks++] = new AddOrSubMul_Task(this);
            /***/
            for (int i = 0; i < numTasks; i++) {
                mThreadPool.execute((Runnable)tasks[i]);
            }
            startMonitorThread();
        }
	catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());	    
            display("EXCEPTION: " + e.toString());
	}
    }
    
    protected void initMpqTasks()
    {
        numTasks = 0;
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        try {
            initLog();
            display(getString(R.string.testing) + " " + GMP.getVersion());
            initRandom();
            mThreadWorkQueue = new ArrayBlockingQueue<Runnable>(MAX_NUM_TASKS);
            NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
            mThreadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    mThreadWorkQueue);
            tasks = new TaskBase[MAX_NUM_TASKS];
            /***/
            tasks[numTasks++] = new Mpq_AOrS_Task(this);
            tasks[numTasks++] = new Mpq_Cmp_SI_Task(this);
            tasks[numTasks++] = new Mpq_Cmp_Task(this);
            tasks[numTasks++] = new Mpq_Cmp_UI_Task(this);
            tasks[numTasks++] = new Mpq_Cmp_Z_Task(this);
            tasks[numTasks++] = new Mpq_Equal_Task(this);
            tasks[numTasks++] = new Mpq_Set_F_Task(this);
            tasks[numTasks++] = new Mpq_Set_Str_Task(this);
            tasks[numTasks++] = new Mpq_MulDiv_2Exp_Task(this);
            tasks[numTasks++] = new Mpq_Inv_Task(this);
            tasks[numTasks++] = new Mpq_Get_D_Task(this);
            /***/
            tasks[numTasks++] = new Mpq_Get_Str_Task(this);
            /***/
            tasks[numTasks++] = new Mpq_Input_Str_Task(this);
            /***/
            for (int i = 0; i < numTasks; i++) {
                mThreadPool.execute((Runnable)tasks[i]);
            }
            startMonitorThread();
        }
	catch (IOException e) {
            Log.d(TAG, "EXCEPTION: " + e.toString());	    
            display("EXCEPTION: " + e.toString());
	}
    }
    
    protected void cancelAllTasks()
    {
	if (tasks == null) return;
	try {
	    TaskBase task;
	    Thread th;
            for (int i = 0; i < numTasks; i++) {
        	task = tasks[i];
        	task.setActive(false);
        	th = task.getExecutingThread();
            	if (th != null) {
                    th.interrupt();
        	}
            }	    
	}
	catch (Exception e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());	    
	    display("EXCEPTION: " + e.toString());
	}
    }
    
    protected boolean isActive()
    {
	if (tasks == null) return false;
	boolean ret = false;
	try {
	    TaskBase task;
	    Thread th;
            for (int i = 0; i < numTasks; i++) {
        	task = tasks[i];
        	th = task.getExecutingThread();
            	if (th != null) {
                    ret = true;
                    break;
        	}
            }	    
	}
	catch (Exception e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());	    
	    display("EXCEPTION: " + e.toString());
	}
	return ret;
    }
    
    protected int getResult()
    {
	if (tasks == null) return -1;
	TaskBase task;
	int ret = 0;
        for (int i = 0; i < numTasks; i++) {
    	    task = tasks[i];
    	    if (task.getReturnCode() != 0) {
    		ret = -1;
    		break;
    	    }
        }
        return ret;
    }
    
    protected void initLog() throws IOException
    {
        String root = this.getExternalFilesDir(null).getPath();
        this.logf = new BufferedWriter(new FileWriter(root + "/gmptestlog.txt", true));
    }
    
    protected void finiLog() throws IOException
    {
	this.logf.close();
	this.logf = null;
    }
    
    protected void log(String msg)
    {
	if (this.logf != null) {
	    try {
	        this.logf.write(msg);
	    }
	    catch (IOException e) {
	        Log.d(TAG, "EXCEPTION: " + e.toString());
	    }
	}
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mView = new TextView[2];
        mView[0] = (TextView) findViewById(R.id.TextView01);
        mView[1] = (TextView) findViewById(R.id.TextView02);
        mTestMpz = (Button) findViewById(R.id.Button01);
        mTestMpq = (Button) findViewById(R.id.Button04);
        mCancel = (Button) findViewById(R.id.Button02);
        mClear = (Button) findViewById(R.id.Button03);
        mProgress = new ProgressBar[2];
        mProgress[0] = (ProgressBar) findViewById(R.id.Progress01);
        mProgress[1] = (ProgressBar) findViewById(R.id.Progress02);
        mHandler = new MyHandler(Looper.getMainLooper());
        try {
            GMP.init();
            TestUtil.initprimes(65535);
            mTestMpz.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.initTasks();
                        }
                    });
            mTestMpq.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.initMpqTasks();
                        }
                    });
            mCancel.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            MainActivity.this.cancelAllTasks();
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
