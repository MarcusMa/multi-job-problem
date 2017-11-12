package com.marcusma.multijob;

import com.marcusma.multijob.data.JobEdge;
import com.marcusma.multijob.data.MyJob;

import java.util.*;

public class Test {

    public static List<MyJob> jobs = new ArrayList<MyJob>();
    public static Set<JobEdge> edges = new HashSet<JobEdge>();
    public static boolean[][] arrivedMap = null;
    public static List<MyJob> finishJobs = new LinkedList<MyJob>();
    // @Deprecated
    public static Queue<MyJob> unfinishJobs = new LinkedList<MyJob>();
    // public static Queue<MyJob> undoJobs = new LinkedList<MyJob>();

    public static void main(String[] args) {
        // init data;
        initData();

        boolean ret = checkAdditionOperation(new JobEdge(jobs.get(3),jobs.get(0))); // should return false;
        System.out.println(ret?"Can add the new edge.":"Can NOT add the new edge");
        ret = checkAdditionOperation(new JobEdge(jobs.get(4),jobs.get(0))); // should return false;
        System.out.println(ret?"Can add the new edge.":"Can NOT add the new edge");

        MyJobExecThread thread1 = new MyJobExecThread("线程A");
        MyJobExecThread thread2 = new MyJobExecThread("线程B");
        MyJobExecThread thread3 = new MyJobExecThread("线程C");

        thread1.start();
        thread2.start();
        thread3.start();

        // MyJobExecThread thread3 = new MyJobExecThread("3");


    }


    public static void initData(){
        MyJob job0 = new MyJob(0);
        MyJob job1 = new MyJob(1);
        MyJob job2 = new MyJob( 2);
        MyJob job3 = new MyJob(3);
        MyJob job4 = new MyJob(4);
        MyJob job5 = new MyJob( 5);

        jobs.add(job0);
        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);
        jobs.add(job4);
        jobs.add(job5);

        unfinishJobs.add(job0);
        unfinishJobs.add(job1);
        unfinishJobs.add(job2);
        unfinishJobs.add(job3);
        unfinishJobs.add(job4);
        unfinishJobs.add(job5);


        JobEdge relation1 = new JobEdge(job0,job1);
        JobEdge relation2 = new JobEdge(job0,job2);
        JobEdge relation3 = new JobEdge(job2,job3);
        JobEdge relation4 = new JobEdge(job5,job3);
        JobEdge relation5 = new JobEdge(job5,job4);

        edges.add(relation1);
        edges.add(relation2);
        edges.add(relation3);
        edges.add(relation4);
        edges.add(relation5);

        arrivedMap = new boolean[jobs.size()][jobs.size()];

        Iterator<JobEdge> it = edges.iterator();
        while (it.hasNext()) {
            JobEdge edge  = it.next();
            MyJob fromJob = edge.getFrom();
            MyJob toJob = edge.getTo();
            arrivedMap[fromJob.getIndex()][toJob.getIndex()] = true;
            for(int x=0;x < jobs.size();x++){
                if(arrivedMap[x][fromJob.getIndex()]){
                    arrivedMap[x][toJob.getIndex()] = true;
                }
            }
        }
    }


    public static boolean checkAdditionOperation(JobEdge newEdge){
        // 检查是否是已经有的边，有边则直接返回
        if(arrivedMap[newEdge.getFrom().getIndex()][newEdge.getTo().getIndex()]){
            return false;
        }
        // 检查是否反向可达
        return !arrivedMap[newEdge.getTo().getIndex()][newEdge.getFrom().getIndex()];
    }

    public static class MyJobExecThread extends Thread{

        public MyJobExecThread(String name){
            this.setName(name);
        }

        @Override
        public void run() {
            boolean isAllDone = false;
            while (!isAllDone){
                boolean isGetJob = false;
                while (!isGetJob){
                    // 取出一个可运行的job
                    MyJob selectJob;
                    synchronized (unfinishJobs){
                        if(unfinishJobs.size() == 0){
                            System.out.println(getName() + ": all job have done");
                            isAllDone = true;
                            break;
                        }
                        selectJob = unfinishJobs.poll();
                        isGetJob = true;
                        for(int i=0;i<jobs.size();i++){
                            if(arrivedMap[i][selectJob.getIndex()]&&!finishJobs.contains(jobs.get(i))){
                                System.out.println(getName() + ": job " + selectJob.getIndex() + " 不满足条件");
                                isGetJob = false;
                                break;
                            }
                        }
                        if(isGetJob){
                            unfinishJobs.remove(selectJob);
                        }else{
                            unfinishJobs.offer(selectJob);
                        }
                    }

                    if(isGetJob){
                        System.out.println(getName() + ": job " + selectJob.getIndex() + " is working....");
                        selectJob.work();
                        System.out.println(getName() + ": job " + selectJob.getIndex() + " is done.");
                    }

                    synchronized (finishJobs){
                        finishJobs.add(selectJob);
                    }
                }

            }

        }
    }
}
