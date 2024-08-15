package com.shaoteemo;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.52.0)",
    comments = "Source: stream.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class StreamServiceGrpc {

  private StreamServiceGrpc() {}

  public static final String SERVICE_NAME = "StreamService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getServerStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "serverStream",
      requestType = com.shaoteemo.HelloWorldProto.Request.class,
      responseType = com.shaoteemo.HelloWorldProto.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getServerStreamMethod() {
    io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response> getServerStreamMethod;
    if ((getServerStreamMethod = StreamServiceGrpc.getServerStreamMethod) == null) {
      synchronized (StreamServiceGrpc.class) {
        if ((getServerStreamMethod = StreamServiceGrpc.getServerStreamMethod) == null) {
          StreamServiceGrpc.getServerStreamMethod = getServerStreamMethod =
              io.grpc.MethodDescriptor.<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "serverStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Response.getDefaultInstance()))
              .setSchemaDescriptor(new StreamServiceMethodDescriptorSupplier("serverStream"))
              .build();
        }
      }
    }
    return getServerStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getClientStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "clientStream",
      requestType = com.shaoteemo.HelloWorldProto.Request.class,
      responseType = com.shaoteemo.HelloWorldProto.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getClientStreamMethod() {
    io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response> getClientStreamMethod;
    if ((getClientStreamMethod = StreamServiceGrpc.getClientStreamMethod) == null) {
      synchronized (StreamServiceGrpc.class) {
        if ((getClientStreamMethod = StreamServiceGrpc.getClientStreamMethod) == null) {
          StreamServiceGrpc.getClientStreamMethod = getClientStreamMethod =
              io.grpc.MethodDescriptor.<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "clientStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Response.getDefaultInstance()))
              .setSchemaDescriptor(new StreamServiceMethodDescriptorSupplier("clientStream"))
              .build();
        }
      }
    }
    return getClientStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getBidirectionalStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "bidirectionalStream",
      requestType = com.shaoteemo.HelloWorldProto.Request.class,
      responseType = com.shaoteemo.HelloWorldProto.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request,
      com.shaoteemo.HelloWorldProto.Response> getBidirectionalStreamMethod() {
    io.grpc.MethodDescriptor<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response> getBidirectionalStreamMethod;
    if ((getBidirectionalStreamMethod = StreamServiceGrpc.getBidirectionalStreamMethod) == null) {
      synchronized (StreamServiceGrpc.class) {
        if ((getBidirectionalStreamMethod = StreamServiceGrpc.getBidirectionalStreamMethod) == null) {
          StreamServiceGrpc.getBidirectionalStreamMethod = getBidirectionalStreamMethod =
              io.grpc.MethodDescriptor.<com.shaoteemo.HelloWorldProto.Request, com.shaoteemo.HelloWorldProto.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "bidirectionalStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shaoteemo.HelloWorldProto.Response.getDefaultInstance()))
              .setSchemaDescriptor(new StreamServiceMethodDescriptorSupplier("bidirectionalStream"))
              .build();
        }
      }
    }
    return getBidirectionalStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StreamServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StreamServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StreamServiceStub>() {
        @java.lang.Override
        public StreamServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StreamServiceStub(channel, callOptions);
        }
      };
    return StreamServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StreamServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StreamServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StreamServiceBlockingStub>() {
        @java.lang.Override
        public StreamServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StreamServiceBlockingStub(channel, callOptions);
        }
      };
    return StreamServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StreamServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StreamServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StreamServiceFutureStub>() {
        @java.lang.Override
        public StreamServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StreamServiceFutureStub(channel, callOptions);
        }
      };
    return StreamServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class StreamServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 服务端流式RPC 
     * </pre>
     */
    public void serverStream(com.shaoteemo.HelloWorldProto.Request request,
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getServerStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     * 客户端流式RPC 
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Request> clientStream(
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getClientStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     * 双向流式RPC 
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Request> bidirectionalStream(
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getBidirectionalStreamMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getServerStreamMethod(),
            io.grpc.stub.ServerCalls.asyncServerStreamingCall(
              new MethodHandlers<
                com.shaoteemo.HelloWorldProto.Request,
                com.shaoteemo.HelloWorldProto.Response>(
                  this, METHODID_SERVER_STREAM)))
          .addMethod(
            getClientStreamMethod(),
            io.grpc.stub.ServerCalls.asyncClientStreamingCall(
              new MethodHandlers<
                com.shaoteemo.HelloWorldProto.Request,
                com.shaoteemo.HelloWorldProto.Response>(
                  this, METHODID_CLIENT_STREAM)))
          .addMethod(
            getBidirectionalStreamMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                com.shaoteemo.HelloWorldProto.Request,
                com.shaoteemo.HelloWorldProto.Response>(
                  this, METHODID_BIDIRECTIONAL_STREAM)))
          .build();
    }
  }

  /**
   */
  public static final class StreamServiceStub extends io.grpc.stub.AbstractAsyncStub<StreamServiceStub> {
    private StreamServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StreamServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StreamServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 服务端流式RPC 
     * </pre>
     */
    public void serverStream(com.shaoteemo.HelloWorldProto.Request request,
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getServerStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 客户端流式RPC 
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Request> clientStream(
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getClientStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * 双向流式RPC 
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Request> bidirectionalStream(
        io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getBidirectionalStreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class StreamServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<StreamServiceBlockingStub> {
    private StreamServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StreamServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StreamServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 服务端流式RPC 
     * </pre>
     */
    public java.util.Iterator<com.shaoteemo.HelloWorldProto.Response> serverStream(
        com.shaoteemo.HelloWorldProto.Request request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getServerStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class StreamServiceFutureStub extends io.grpc.stub.AbstractFutureStub<StreamServiceFutureStub> {
    private StreamServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StreamServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StreamServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SERVER_STREAM = 0;
  private static final int METHODID_CLIENT_STREAM = 1;
  private static final int METHODID_BIDIRECTIONAL_STREAM = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StreamServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(StreamServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SERVER_STREAM:
          serviceImpl.serverStream((com.shaoteemo.HelloWorldProto.Request) request,
              (io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CLIENT_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.clientStream(
              (io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response>) responseObserver);
        case METHODID_BIDIRECTIONAL_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.bidirectionalStream(
              (io.grpc.stub.StreamObserver<com.shaoteemo.HelloWorldProto.Response>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class StreamServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StreamServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shaoteemo.StreamProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StreamService");
    }
  }

  private static final class StreamServiceFileDescriptorSupplier
      extends StreamServiceBaseDescriptorSupplier {
    StreamServiceFileDescriptorSupplier() {}
  }

  private static final class StreamServiceMethodDescriptorSupplier
      extends StreamServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    StreamServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (StreamServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StreamServiceFileDescriptorSupplier())
              .addMethod(getServerStreamMethod())
              .addMethod(getClientStreamMethod())
              .addMethod(getBidirectionalStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
