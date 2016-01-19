package org.gmplib.test;

public class TaskBase {

    protected UI uinterface;
    protected String failmsg;
    protected int rcode;
    protected Thread myThread;
    protected Integer[] params;
    protected boolean active;
    protected String name;

    public TaskBase(UI ui, String name)
    {
        this.uinterface = ui;
        this.failmsg = null;
        this.rcode = -1;
        this.myThread = null;
        this.params = new Integer[0];
        this.active = true;
        this.name = name;
    }

    public int getReturnCode()
    {
	int ret = 0;
	synchronized (this) {
	    ret = this.rcode;
	}
	return ret;
    }
    
    public Thread getExecutingThread()
    {
	Thread  ret = null;
	synchronized (this) {
	    ret = this.myThread;
	}
	return ret;
    }
    
    public void setExecutingThread(Thread th)
    {
	synchronized (this) {
	    this.myThread = th;
	}	
    }

    public synchronized void setActive(boolean b)
    {
	this.active = b;
    }
    
    public synchronized boolean isActive()
    {
	return this.active;
    }

    public void onPreExecute()
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        setExecutingThread(Thread.currentThread());
        uinterface.display(name);
    }

    public void onProgressUpdate(Integer... progress)
    {
        //uinterface.display("progress=" + progress[0]);
        uinterface.displayProgress(progress[0]);
    }

    public void onPostExecute(Integer result)
    {
        synchronized (this) {
            this.myThread = null;
            this.rcode = result.intValue();
        }
        uinterface.display("result=" + result);
        if (result == 0) {
            uinterface.display("PASS");
        } else {
            uinterface.display(failmsg);
            uinterface.display("FAIL");
        }
    }

}
