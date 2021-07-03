package pl.pozadr.grpc.greeting.server;

import io.grpc.stub.StreamObserver;
import pl.pozadr.greet.*;
import pl.pozadr.greet.GreetServiceGrpc.GreetServiceImplBase;

public class GreetServiceImpl extends GreetServiceImplBase {

  @Override
  public void greet(
      GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    var greeting = request.getGreeting();
    var firstName = greeting.getFirstName();

    String result = "Hello " + firstName;
    GreetResponse response = GreetResponse.newBuilder()
        .setResult(result)
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void greetManyTimes(
      GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
    var greeting = request.getGreeting();
    var firstName = greeting.getFirstName();

    try {
      for (int i = 0; i < 10; i++) {
        String result = "Hello " + firstName + ", response number: " + i;
        var response = GreetManyTimesResponse.newBuilder()
            .setResult(result)
            .build();
        responseObserver.onNext(response);
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<LongGreetRequest> longGreet(
      StreamObserver<LongGreetResponse> responseObserver) {
    var requestObserver = new StreamObserver<LongGreetRequest>() {

      String result = "";

      @Override
      public void onNext(LongGreetRequest value) {
        // client sends a message
        result += "Hello " + value.getGreeting().getFirstName() + "! ";
      }

      @Override
      public void onError(Throwable t) {
        // client sends an error
      }

      @Override
      public void onCompleted() {
        // client sends is done
        // this is when we want to return a response (reponseObserver)
        responseObserver.onNext(
            LongGreetResponse.newBuilder()
                .setResult(result)
                .build()
        );
        responseObserver.onCompleted();
      }
    };
    return requestObserver;
  }

  @Override
  public StreamObserver<GreetEveryoneRequest> greetEveryone(
      StreamObserver<GreetEveryoneResponse> responseObserver) {

    StreamObserver<GreetEveryoneRequest> requestObserver = new StreamObserver<>() {
      @Override
      public void onNext(GreetEveryoneRequest value) {
        String result = "Hello from server " + value.getGreeting().getFirstName();
        var response = GreetEveryoneResponse.newBuilder()
            .setResult(result)
            .build();
        responseObserver.onNext(response);
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("Server onCompleted");
        responseObserver.onCompleted();
      }
    };
    return requestObserver;
  }
}
