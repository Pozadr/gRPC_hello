package pl.pozadr.grpc.calculator.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pl.pozadr.calc.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

  ManagedChannel channel;

  public static void main(String[] args) {
    CalculatorClient calculatorClient = new CalculatorClient();
    calculatorClient.runCalc();
  }

  private void runCalc() {
    channel = ManagedChannelBuilder.forAddress("localhost", 50052)
        .usePlaintext() // disable SSL
        .build();

    doUnaryCall(channel);
    doServerStreamingCall(channel);
    doClientStreamingCall(channel);
    doBiDirectionalStreamingCall(channel);
    doErrorCall(channel);

    System.out.println("Shutting down the channel.");
    channel.shutdown();
  }

  private void doUnaryCall(ManagedChannel channel) {
    System.out.println("\nSum");
    System.out.println("Creating sync stub.");
    var calcClient = CalcServiceGrpc.newBlockingStub(channel);

    var request = SumRequest.newBuilder()
        .setNumber1(5)
        .setNumber2(5)
        .build();

    var response = calcClient.add(request);

    System.out.println("Response:");
    System.out.println(
        request.getNumber1() + " + " + request.getNumber2() + " = " + response.getSumResult());
    System.out.println("Finished");
  }

  private void doServerStreamingCall(ManagedChannel channel) {
    System.out.println("\nPrimeNumberDecomposition");
    System.out.println("Creating sync stub.");
    var calcClient = CalcServiceGrpc.newBlockingStub(channel);

    var requestNumToDecompose = PrimeNumberDecompositionRequest.newBuilder()
        .setNumberToDecompose(12928)
        .build();

    calcClient.primeNumberDecomposition(requestNumToDecompose)
        .forEachRemaining(
            primeNumberDecompositionResponse -> System.out.println("Decomposed number: "
                + primeNumberDecompositionResponse.getDecompositionNumber() + ", "));
    System.out.println("Finished");
  }

  private void doClientStreamingCall(ManagedChannel channel) {
    System.out.println("\nComputeAverage");
    System.out.println("Creating async stub.");
    var calcClient = CalcServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    var averageRequestObserver =
        calcClient.computeAverage(new StreamObserver<>() {
          @Override
          public void onNext(ComputeAverageResponse value) {
            System.out.println("Received a response from the server");
            var average = value.getAverage();
            System.out.println("Avr = " + average);
          }

          @Override
          public void onError(Throwable t) {

          }

          @Override
          public void onCompleted() {
            System.out.println("Server has completed sending us something");
            latch.countDown();
          }
        });
    List.of(4, 3, 7, 3).forEach(
        num -> averageRequestObserver.onNext(
            ComputeAverageRequest.newBuilder()
                .setNumberToCalculate(4)
                .build())
    );

    // we tell server that the client is done sending data
    averageRequestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doBiDirectionalStreamingCall(ManagedChannel channel) {
    System.out.println("\nFind Maximum");
    System.out.println("Creating async stub.");
    var calcClient = CalcServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    var maximumRequestObserver = calcClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
      @Override
      public void onNext(FindMaximumResponse value) {
        System.out.println("Got new maximum = " + value.getMaximum());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("Server has completed sending us something");
        latch.countDown();
      }
    });

    List.of(1, 100, 2, 101, 99, 999).forEach(
        num -> {
          System.out.println("Sending number = " + num);
          maximumRequestObserver.onNext(FindMaximumRequest.newBuilder()
              .setNumberToCompare(num)
              .build());
        }
    );
    // we tell server that the client is done sending data
    maximumRequestObserver.onCompleted();

    try {
      latch.await(3L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doErrorCall(ManagedChannel channel) {
    System.out.println("\nError call");
    System.out.println("Creating sync stub.");
    var calcClient = CalcServiceGrpc.newBlockingStub(channel);

    long number = -100L;
    try {
      var request = SquareRootRequest.newBuilder()
          .setNumber(number)
          .build();

      var response = calcClient.squareRoot(request);

      System.out.println("Response:");
      System.out.println(
          request.getNumber() + " root = " + response.getNumberRoot());
    } catch (RuntimeException ex) {
      System.out.println("Got an exception for square root!");
      ex.printStackTrace();
    }
    System.out.println("Finished");
  }
}
