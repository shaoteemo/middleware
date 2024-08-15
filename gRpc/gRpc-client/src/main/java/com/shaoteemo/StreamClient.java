package com.shaoteemo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Create Info: 流式RPC客户端
 * BlockingStub -> 阻塞 通信方式
 * Stub -> 异步 通过监听处理
 * FutureStub -> 异/同步 NettyFuture
 * <br>Change Info:
 * <br>Create On 6/29/2024 21:56
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class StreamClient {
    public static void main(String[] args) {
        /* 服务端流式RPC */
        serverBlockStream();
//        serverAsyncStream();
        /* 客户端流式RPC */
//        clientAsyncStream();
        /* 双向流式RPC */
//        bidirectionalStream();


    }

    /*
     * 服务端流式RPC:
     * 阻塞式获取流信息
     * */
    private static void serverBlockStream() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build();

        try {
            // 阻塞式获取
            StreamServiceGrpc.StreamServiceBlockingStub streamService = StreamServiceGrpc.newBlockingStub(channel);

            HelloWorldProto.Request.Builder builder = HelloWorldProto.Request.newBuilder();

            builder.setName("Request send!");

            Iterator<HelloWorldProto.Response> response = streamService.serverStream(builder.build());

            /* 服务器流式处理返回接受 */
            while (response.hasNext()) {
                HelloWorldProto.Response next = response.next();
                System.out.println(next.getResult());
            }
        } finally {
            channel.shutdownNow();
        }
    }

    /**
     * 服务端流式RPC:
     * 异步数据监听获取
     * 使用的观察者模式
     */
    private static void serverAsyncStream() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build();

        try {
            // 异步流数据获取
            StreamServiceGrpc.StreamServiceStub service = StreamServiceGrpc.newStub(channel);

            service.serverStream(
                    // 创建Request
                    HelloWorldProto.Request.newBuilder().setName("Request send!").build(),
                    // 监听对象及处理
                    new StreamObserver<HelloWorldProto.Response>() {
                        @Override
                        public void onNext(HelloWorldProto.Response value) {
                            // do something...
                            System.out.println("获取到数据：" + value.getResult());

                            // do something...
                        }

                        @Override
                        public void onError(Throwable t) {
                            // 异常处理
                            System.err.println(t.getMessage());
                        }

                        @Override
                        public void onCompleted() {
                            // 数据传输完毕后处理
                            System.out.println("服务端响应结束。");
                        }
                    }
            );

            // do something... not blocking

            // demo示例阻塞等待一下
            channel.awaitTermination(12, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
    }

    /**
     * 客户端流式RPC：
     */
    private static void clientAsyncStream() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build();
        try {
            StreamServiceGrpc.StreamServiceStub service = StreamServiceGrpc.newStub(channel);

            StreamObserver<HelloWorldProto.Request> requestStream = service.clientStream(
                    // 创建客户端监听
                    new StreamObserver<HelloWorldProto.Response>() {
                        @Override
                        public void onNext(HelloWorldProto.Response value) {
                            System.out.println("Server response: " + value.getResult());
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("服务端处理完毕");
                        }
                    }
            );

            /* 模拟消息多次发送 */
            for (int i = 0; i < 9; i++) {
                requestStream.onNext(HelloWorldProto.Request.newBuilder().setName("来自客户端的第" + (i + 1) + "消息").build());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // 结束消息传递
            requestStream.onCompleted();

            // demo示例阻塞等待一下
            channel.awaitTermination(13, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }
    }

    /**
     * 双向流式RPC：
     */
    private static void bidirectionalStream() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build();

        try {
            StreamServiceGrpc.StreamServiceStub service = StreamServiceGrpc.newStub(channel);

            StreamObserver<HelloWorldProto.Request> requestStream = service.bidirectionalStream(new StreamObserver<HelloWorldProto.Response>() {
                @Override
                public void onNext(HelloWorldProto.Response value) {
                    System.out.println("Server response: " + value.getResult());
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });

            /* 模拟多次推送 */
            for (int i = 0; i < 5; i++) {
                requestStream.onNext(HelloWorldProto.Request.newBuilder().setName("来自客户端的第" + (i + 1) + "消息").build());
            }

            requestStream.onCompleted();

            channel.awaitTermination(3, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            channel.shutdown();
        }

    }

}
