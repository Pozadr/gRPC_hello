package pl.pozadr.grpc.calculator.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pl.pozadr.calc.*;
import pl.pozadr.calc.CalcServiceGrpc.CalcServiceImplBase;


public class CalculatorServiceImpl extends CalcServiceImplBase {

  @Override
  public void add(
      SumRequest request, StreamObserver<SumResponse> responseObserver) {
    var number1 = request.getNumber1();
    var number2 = request.getNumber2();
    var sum = number1 + number2;

    SumResponse response = SumResponse.newBuilder()
        .setSumResult(sum)
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void primeNumberDecomposition(
      PrimeNumberDecompositionRequest request,
      StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
    var numberToDecompose = request.getNumberToDecompose();

    try {
      int divisor = 2;
      while (numberToDecompose > 1) {
        if (numberToDecompose % divisor == 0) {
          responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder()
              .setDecompositionNumber(divisor)
              .build());

          numberToDecompose = numberToDecompose / divisor;
        } else {
          divisor = divisor + 1;
        }
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<ComputeAverageRequest> computeAverage(
      StreamObserver<ComputeAverageResponse> responseObserver) {
    var requestObserver = new StreamObserver<ComputeAverageRequest>() {
      int sum = 0;
      int count = 0;

      @Override
      public void onNext(ComputeAverageRequest value) {
        var num = value.getNumberToCalculate();
        sum += num;
        count++;
        System.out.println("num = " + num);
        System.out.println("sum = " + sum);
        System.out.println("count = " + count);
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        double avr = (double) sum / count;
        responseObserver.onNext(ComputeAverageResponse.newBuilder()
            .setAverage(avr)
            .build());
        responseObserver.onCompleted();
      }
    };
    return requestObserver;
  }

  @Override
  public StreamObserver<FindMaximumRequest> findMaximum(
      StreamObserver<FindMaximumResponse> responseObserver) {
    var requestObserver = new StreamObserver<FindMaximumRequest>() {
      long max = 0L;

      @Override
      public void onNext(FindMaximumRequest value) {
        long numToCompare = value.getNumberToCompare();
        if (max < numToCompare) {
          max = numToCompare;
          responseObserver.onNext(FindMaximumResponse.newBuilder()
              .setMaximum(max)
              .build());
        }
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
    return requestObserver;
  }

  @Override
  public void squareRoot(
      SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
    long number = request.getNumber();

    if (number > 0) {
      double numberRoot = Math.sqrt(number);
      responseObserver.onNext(
          SquareRootResponse.newBuilder()
              .setNumberRoot(numberRoot)
              .build()
      );
    } else {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("The number being sent is not positive")
              .augmentDescription("Number sent: " + number)
              .asRuntimeException()
      );
    }
    responseObserver.onCompleted();
  }
}
