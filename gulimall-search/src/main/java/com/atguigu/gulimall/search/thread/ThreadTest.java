package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("main...start...");

//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, service);
        /**
         * 方法完成后的感知
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).whenComplete((res, exception) -> {
//            // 虽然能得到异常信息，但是没法修改返回数据
//            // exceptionally可以感知异常，同时返回默认值
//            System.out.println("异步任务成功完成了...结果是：" + res + "；异常是：" + exception);
//        }).exceptionally(throwable -> {
//            return 10;
//        });

        /**
         * 方法完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });

        /**
         * 线程串行化
         * 1 thenRun 不能获取到上一步的执行结果 无返回值
         * thenRunAsync(() -> {
         *             System.out.println("任务2启动了...");
         *         }, service)
         * 2 thenAccept 能接受上一步结果 无返回值
         * thenAcceptAsync(res -> {
         *             System.out.println("任务2启动了..." + res);
         *         }, service)
         * 3 thenApply 能接受上一步结果 有返回值
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        });
        /**
         * 两个任务都完成
         */
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结束：");
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程：" + Thread.currentThread().getId());
//
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务2结束：");
//            return "Hello";
//        }, service);

//        future01.runAfterBothAsync(future02, () -> {
//            System.out.println("任务3开始...");
//        }, service);

//        future01.thenAcceptBothAsync(future02, (f1, f2)->{
//            System.out.println("任务3开始...之前的结果：" + f1 + "-->" + f2);
//        }, service);

//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + "：" + f2 + " ->" + "Hello";
//        }, service);

//        Integer integer = future.get();

        /**
         * 两个任务只要有一个完成，就执行任务3
         * runAfterEitherAsync 不感知结果 自己也无返回值
         * acceptEitherAsync 感知结果 没返回值 前提两个线程返回结果要一样
         */
//        future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("任务3开始...");
//        }, service);
//        future01.acceptEitherAsync(future02, res -> {
//            System.out.println("任务3开始..." + res);
//        }, service);
//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, res -> {
//            return res.toString() + "->哈哈";
//        }, service);

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        });

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片属性");
            return "黑色+256G";
        });

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片介绍");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "华为";
        });

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        CompletableFuture<Object> object = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);

//        System.out.println("main...end..." + futureImg.get() + "；" + futureAttr.get() + "；" + futureDesc.get());
        System.out.println("main...end..." + object.get());
    }

    // 当前系统中池只有一两个，每个异步任务直接提交给线程池让他自己去执行
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    /**
     * 1 继承Thread
     * Thread01 thread01 = new Thread01();
     * thread01.start();
     *
     * 2 实现Runnable接口
     * Runnable01 runnable01 = new Runnable01();
     * new Thread(runnable01).start();
     *
     * 3 实现Callable接口 + FutureTask 可以拿到返回结果 可以处理异常
     * FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
     * new Thread(futureTask).start();
     * // 阻塞等待整个线程执行完成，获取返回结果
     * Integer integer = futureTask.get();
     *
     * 4 线程池[ExecutorService]
     *  给线程池提交任务
     *  以上三种启动线程方式在业务代码里都不用，将所有多线程异步任务都交给线程池执行
     *  1) 创建
     *      1 Executors
     *      2 new ThreadPoolExecutor
     *
     * Future可以获取异步结构
     *
     * 区别
     *  1 2不能得到返回值，3可以获取返回值
     *  1 2 3 都不能控制资源
     *  4 可以控制资源 性能稳定
     */
    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start...");

//        service.execute(new Runnable01());
        /**
         * 七大参数
         * 1 corePoolSize 核心线程数【一直存在 除非设置超时】 线程池 创建好以后就准备就绪的线程数量 等待来接受异步任务去执行
         * 2 maximumPoolSize 最大线程数量 控制资源
         * 3 keepAliveTime 存活时间 如果当前的线程数量大于核心数量
         *  释放空闲线程（maximumPoolSize-corePoolSize） 只要线程空闲大于指定的keepAliveTime
         * 4 unit 时间单位
         * 5 BlockingQueue<Runnable> workQueue 阻塞队列 如果任务有很多，就会将目前多的任务放在队列里面
         *  只要有线程空闲，就会去队列里面取出新的任务继续执行
         * 6 threadFactory 线程的创建工厂
         * 7 RejectedExecutionHandler handler 如果队列满了 按照我们指定的拒绝策略拒绝执行任务
         *
         * 工作顺序：
         * 1 线程池创建，准备好core数量的核心线程，准备接受任务
         * 1.1 core满了，就将再进来的任务放入阻塞队列中，空闲的core就会自己去阻塞队列获取任务执行
         * 1.2 阻塞队列满了 就直接开新线程执行，最大只能开到max指定的数量
         * 1.3 max满了就会RejectedExecutionHandler拒绝任务
         * 1.4 max都执行完成，有很多空闲，在指定的时间keepAliveTime以后，释放max-core这些线程
         *  new LinkedBlockingDeque<>() 默认是Integer最大值 内存不够可能
         *
         * 面试 一个线程池 core：7 max：20 queue：50 100并发进来怎么分配
         * 7个会立即执行 50个会进入队列 再开13个进行执行 剩下的30个就使用拒绝策略
         *
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

//        Executors.newCachedThreadPool()   core是0 所有都可回收
//        Executors.newFixedThreadPool()    固定大小 core=max 都不可回收
//        Executors.newScheduledThreadPool()    定时任务的线程池
//        Executors.newSingleThreadExecutor()   单线程的线程池 后台从队列里面获取任务，挨个执行

        System.out.println("main...end...");
    }

    public static class Thread01 extends Thread {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
