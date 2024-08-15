package com.shaoteemo.service;

import com.shaoteemo.HelloWorldProto;
import com.shaoteemo.StreamServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Create Info: SpringBoot实现的一个Grpc服务
 * <br>Change Info:
 * <br>Create On 6/30/2024 21:15
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@GrpcService
public class StreamServiceImpl extends StreamServiceGrpc.StreamServiceImplBase {

    @Override
    public void serverStream(HelloWorldProto.Request request, StreamObserver<HelloWorldProto.Response> responseObserver) {
        String name = request.getName();
        System.out.println("业务处理：" + name);

        // 数据多此处理传送
        for (int i = 0; i < 9; i++) {
            HelloWorldProto.Response.Builder builder = HelloWorldProto.Response.newBuilder();
            builder.setResult("第" + (i + 1) + "此处理结果。");
            responseObserver.onNext(builder.build());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloWorldProto.Request> clientStream(StreamObserver<HelloWorldProto.Response> responseObserver) {
        // 监听客户端请求
        return new StreamObserver<HelloWorldProto.Request>() {
            @Override
            public void onNext(HelloWorldProto.Request value) {
                // 来自客户端的消息
                System.out.println("Received message from client: " + value.getName());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Client stream completed");

                // 等待客户端数据完毕后
                responseObserver.onNext(HelloWorldProto.Response.newBuilder().setResult("服务端接收到了所有请求。").build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<HelloWorldProto.Request> bidirectionalStream(StreamObserver<HelloWorldProto.Response> responseObserver) {
        return new StreamObserver<HelloWorldProto.Request>() {
            @Override
            public void onNext(HelloWorldProto.Request value) {
                System.out.println("Received message from client: " + value.getName());

                // 这个发送也可以放在Completed中
                responseObserver.onNext(
                        HelloWorldProto.Response.newBuilder()
                                .setResult("接收到客户端的消息：" + value.getName())
                                .build()
                );
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Client stream completed");
                responseObserver.onCompleted();
            }
        };
    }
}
