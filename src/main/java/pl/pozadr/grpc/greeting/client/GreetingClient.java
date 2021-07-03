package pl.pozadr.grpc.greeting.client;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pl.pozadr.greet.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

  ManagedChannel channel;

  public static void main(String[] args) {
    GreetingClient main = new GreetingClient();
    main.run();
  }

  private void run() {
    channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext() // disable SSL
        .build();

    doUnaryCall(channel);
    doServerStreamingCall(channel);
    doClientStreamingCall(channel);
    doBiDirectionalStreamingCall(channel);

    System.out.println("\nShutting down channel");
    channel.shutdown();
  }

  private void doUnaryCall(ManagedChannel channel) {
    System.out.println("gRPC Greeting client: Unary");
    System.out.println("Creating stub");
    var greetClient = GreetServiceGrpc.newBlockingStub(channel);

    var greeting = Greeting.newBuilder()
        .setFirstName("Adrian")
        .setLastName("Blabla")
        .build();

    // Unary
    var request = GreetRequest.newBuilder()
        .setGreeting(greeting)
        .build();

    var response = greetClient.greet(request);
    System.out.println(response);

  }

  private void doServerStreamingCall(ManagedChannel channel) {
    System.out.println("gRPC Greeting client: Server Streaming");
    System.out.println("Creating stub");
    var greetClient = GreetServiceGrpc.newBlockingStub(channel);

    // Server Streaming
    var greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
        .setGreeting(Greeting.newBuilder().setFirstName("Adrian"))
        .build();

    greetClient.greetManyTimes(greetManyTimesRequest)
        .forEachRemaining(greetManyTimesResponse -> {
          System.out.println(greetManyTimesResponse.getResult());
        });
  }

  private void doClientStreamingCall(ManagedChannel channel) {
    System.out.println("\ngRPC Greeting client: client Streaming");
    System.out.println("Creating asynchronous stub");
    // create asynchronous client
    var asyncClient = GreetServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    var requestObserver =
        asyncClient.longGreet(new StreamObserver<>() {
          @Override
          public void onNext(LongGreetResponse value) {
            // we get a response from the server
            // onNext will be called only once
            System.out.println("Received a response from the server");
            System.out.println(value.getResult());
          }

          @Override
          public void onError(Throwable t) {
            // we get an error from the server
          }

          @Override
          public void onCompleted() {
            // the server is done sending us data
            // onComplited will be called right after onNext()
            System.out.println("Server has completed sending us something");
            latch.countDown();
          }
        });

    List.of("Adrian", "Marco", "Stephan", "Andrew").forEach(
        name -> requestObserver.onNext(LongGreetRequest.newBuilder()
            .setGreeting(Greeting.newBuilder()
                .setFirstName(name)
                .build())
            .build())
    );

    // we tell server that the client is done sending data
    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doBiDirectionalStreamingCall(ManagedChannel channel) {
    System.out.println("\ngRPC Greeting client: bi directional Streaming");
    System.out.println("Creating asynchronous stub");
    // create asynchronous client
    var asyncClient = GreetServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    var requestObserver =
        asyncClient.greetEveryone(new StreamObserver<>() {
          @Override
          public void onNext(GreetEveryoneResponse value) {
            System.out.println("Response from server: " + value.getResult());
          }

          @Override
          public void onError(Throwable t) {

          }

          @Override
          public void onCompleted() {
            // the server is done sending us data
            // onComplited will be called right after onNext()
            System.out.println("Server has completed sending us something");
            latch.countDown();
          }
        });

    List.of("Stephane", "John", "Marc", "Adrian", "Adam").forEach(
        name -> {
          System.out.println("Sending: " + name);
          requestObserver.onNext(GreetEveryoneRequest.newBuilder()
              .setGreeting(Greeting.newBuilder()
                  .setFirstName(name)
                  .build())
              .build());
        }
    );

    requestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
