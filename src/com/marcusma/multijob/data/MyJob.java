package com.marcusma.multijob.data;

/**
 * Created by marcus on 2017/11/7.
 */
public class MyJob implements IJob{

    int index; // 任务编号

    int duration; // 模拟任务花费的时间

    int inDegreeCount = 0; // 用于计算存储入度，在遍历时有用

    private MyJob() {

    }

    public MyJob(int index){
        this(index,2000);
    }

    public MyJob(int index,int duration){
        this.index = index;
        this.duration = duration;
    }

    public int getInDegreeCount() {
        return inDegreeCount;
    }

    public void setInDegreeCount(int inDegreeCount) {
        this.inDegreeCount = inDegreeCount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void work()  {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
