// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: stream.proto

package com.shaoteemo;

public final class StreamProto {
  private StreamProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014stream.proto\032\020HelloWorld.proto2\223\001\n\rStr" +
      "eamService\022\'\n\014serverStream\022\010.Request\032\t.R" +
      "esponse\"\0000\001\022\'\n\014clientStream\022\010.Request\032\t." +
      "Response\"\000(\001\0220\n\023bidirectionalStream\022\010.Re" +
      "quest\032\t.Response\"\000(\0010\001B\036\n\rcom.shaoteemoB" +
      "\013StreamProtoP\000b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.shaoteemo.HelloWorldProto.getDescriptor(),
        });
    com.shaoteemo.HelloWorldProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
