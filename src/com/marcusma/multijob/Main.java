package com.marcusma.multijob;

import com.marcusma.multijob.data.MyJob;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

/**
 * Created by marcus on 2017/11/9.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("Using Jgrapht libs");

        /**
         * init data
         * SimpleDirectedGraph 类 既不允许两个点之间有多个边，同时不允许有自己到自己的边
         */
        DirectedGraph<MyJob, DefaultEdge> mGraph = new SimpleDirectedGraph<MyJob, DefaultEdge>(DefaultEdge.class);
        MyJob job0 = new MyJob(0);
        MyJob job1 = new MyJob(1);
        MyJob job2 = new MyJob(2);
        MyJob job3 = new MyJob(3);
        MyJob job4 = new MyJob(4);
        MyJob job5 = new MyJob(5);

        // 基本操作：添加顶点
        mGraph.addVertex(job0);
        mGraph.addVertex(job1);
        mGraph.addVertex(job2);
        mGraph.addVertex(job3);
        mGraph.addVertex(job4);
        mGraph.addVertex(job5);

        // 基本操作：添加边
        mGraph.addEdge(job0, job1);
        mGraph.addEdge(job0, job2);
        mGraph.addEdge(job2, job3);
        // mGraph.addEdge(job3,job0); // 该步将形成环
        // mGraph.addEdge(job0,job0); // 该步将形成自己到自己的边
        mGraph.addEdge(job5, job3);
        mGraph.addEdge(job5, job4);

        // 基本操作：顶点的出度和入度
        System.out.println(mGraph.inDegreeOf(job0));
        System.out.println(mGraph.outDegreeOf(job0));

        // 检查环
        System.out.println("Cycles ?\t :" + checkCycles(mGraph));

        // 检查添加边的操作
        System.out.println("Add 3 -> 0 ?\t :" + checkAdditionOperation(mGraph, job3, job0));
        System.out.println("Add 4 -> 2 ?\t :" + checkAdditionOperation(mGraph, job4, job2));

        // 遍历执行
        traverseAndExecuteJobs(mGraph, 5);
    }

    /**
     * 判断有向图是否有环
     *
     * @param graph
     * @return
     */
    public static boolean checkCycles(DirectedGraph graph) {
        if (null == graph)
            return false;
        HawickJamesSimpleCycles finder = new HawickJamesSimpleCycles(graph);
        List<Object> cycles = finder.findSimpleCycles();
        return cycles == null || cycles.size() == 0 ? true : false;
    }

    /**
     * 判断一个边<from, to> 是否可以添加到有向图中，且不形成环。
     * 原理：使用 Dijkstra 算法判断是否存在to到from的反向路径，若存在则添加边<from, to>将会形成环，若不存在，则不会形成环；
     * 注意：Jgrapht包提供了 ConnectivityInspector类, 该类的pathExists方法会忽略边的方向, 故不用此类进行判断
     *
     * @param graph 有向图
     * @param from  边的起始点
     * @param to    边的结束点
     * @return
     */
    public static boolean checkAdditionOperation(DirectedGraph graph, Object from, Object to) {
        if (null == graph) {
            return false;
        }
        DijkstraShortestPath pathFinder = new DijkstraShortestPath(graph);
        return pathFinder.findPathBetween(graph, to, from) == null;
    }

    /**
     * 开启 numOfThread 线程遍历有向图
     * 原理：按照有向图的拓扑排序进行遍历即可,遍历的过程中会用到邻接矩阵进行可运行性判断
     *
     * @param graph       有向图
     * @param numOfThread 线程数量
     */
    public static void traverseAndExecuteJobs(DirectedGraph graph, int numOfThread) {
        System.out.println("Start traverseAndExecuteJobs");
        Queue<MyJob> queue = new LinkedList<MyJob>();
        DirectedNeighborIndex<MyJob,DefaultEdge> neighborIndex = new DirectedNeighborIndex<MyJob, DefaultEdge>(graph);

        TopologicalOrderIterator iterator = new TopologicalOrderIterator(graph);

        while (iterator.hasNext()) {
            MyJob job = (MyJob) iterator.next();
            System.out.print(" -> " + job.getIndex());
            job.setInDegreeCount(graph.inDegreeOf(job));
            queue.add(job);
        }

        for(int i=0;i<numOfThread;i++){
            MyJobExecutor executor = new MyJobExecutor(queue,neighborIndex);
            executor.setName(String.valueOf(i));
            executor.start();
        }

        System.out.println(queue);
        System.out.println("End traverseAndExecuteJobs");
    }


    private static class MyJobExecutor extends Thread {
        private Queue<MyJob> queue;
        private DirectedNeighborIndex neighborIndex;

        public MyJobExecutor(Queue q, DirectedNeighborIndex d) {
            this.queue = q;
            this.neighborIndex = d;
        }

        @Override
        public synchronized void start() {
            super.start();
            System.out.println("Thread " + this.getName() + " started");
        }

        @Override
        public void run() {
            MyJob selectJob = null;
            boolean isAllDone = false;
            while (!isAllDone){
                synchronized (queue) {
                    try {
                        while (queue.size() != 0){
                            for (MyJob job : queue) {
                                if (checkJobRunnable(job)) {
                                    selectJob = job;
                                    queue.remove(job);
                                    break;
                                }
                            }
                            if (null == selectJob) {
                                System.out.println("Thread " + this.getName() + " : Waiting...");
                                queue.wait();
                            }
                            else{
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // synchronized of queue

                if(null == selectJob){
                    isAllDone = true;
                    System.out.println("Thread " + this.getName() + " : All jobs are running or done. Just quit.");
                }
                else{
                    // do job
                    System.out.println("Thread " + this.getName() + " Get Job : " + selectJob.getIndex()  );
                    System.out.println("Thread " + this.getName() + " Running Job : " + selectJob.getIndex()  );
                    selectJob.work();
                    System.out.println("Thread " + this.getName() + " Done Job : " + selectJob.getIndex()  );
                    // 获得所有的继承者
                    List<MyJob> successors =  neighborIndex.successorListOf(selectJob);
                    for (MyJob successor:successors) {
                        synchronized (successor){
                            System.out.println("Thread " + this.getName() + " Modify Job : " + successor.getIndex()  + "'s incoming count ");
                            successor.setInDegreeCount(successor.getInDegreeCount() - 1);
                        }
                    }
                    // 通知其他线程
                    synchronized (queue){
                        selectJob = null;
                        System.out.println("Thread " + this.getName() + " notifyAll other thread.");
                        queue.notifyAll();
                    }
                }
            }
        }

        private boolean checkJobRunnable(MyJob job){
            boolean ret;
            synchronized (job){
                ret = job.getInDegreeCount() <= 0? true:false;
            }
            return ret;
        }
    }
}
