package com.marcusma.multijob.data;

/**
 * Created by marcus on 2017/11/7.
 */
public class JobEdge {

    private MyJob from;
    private MyJob to;

    public JobEdge(MyJob form, MyJob to){
        this.from = form;
        this.to = to;

    }

    public MyJob getFrom() {
        return from;
    }

    public void setFrom(MyJob from) {
        this.from = from;
    }

    public MyJob getTo() {
        return to;
    }

    public void setTo(MyJob to) {
        this.to = to;
    }
}
