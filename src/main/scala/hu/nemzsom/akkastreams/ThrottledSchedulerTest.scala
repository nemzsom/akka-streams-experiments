package hu.nemzsom.akkastreams

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.stream.{FlowShape, ActorMaterializer}
import akka.stream.scaladsl._
import akka.stream.scaladsl.FlowGraph.Implicits._
import scala.concurrent.Future

import scala.concurrent.duration._
import scala.language.postfixOps

object ThrottledSchedulerTest extends App {

  implicit val system = ActorSystem("ThrottledScheduler-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val counter = new AtomicInteger

  // given a source and a sink
  // it inserts a throttler within the source and the sink
  // and creates a runnableGraph ??? how to detect the end of the flow ???
  // it repeatedly executes the graph and adjusts the throttling speed


  val source = Source[Int](1 to 10)

  val sink = Sink.foreach[Int]{x => Thread.sleep(100); println(s"processing $x")}

  val desiredCycleTime = 6 seconds

  run(source, sink, 20 millis)

  def run(source: Source[Int, _], sink: Sink[Int, Future[_]], rate: FiniteDuration): Unit = {
    println(s"counter: ${counter.incrementAndGet()}")
    val throttler = FlowGraph.partial() { implicit b =>

      val tickSource = Source(initialDelay = rate, interval = rate, Tick)

      val zip = b.add(Zip[Int, Tick.type])
      val inFLow = b.add(Flow[Int])

      inFLow ~> zip.in0
      tickSource ~> zip.in1

      val outlet = zip.out.map(_._1).outlet
      FlowShape(inFLow.inlet, outlet)
    }

    val startTime = System.nanoTime()
    source.via(throttler).runWith(sink).onComplete { _ =>
      val cycleTimeNs = System.nanoTime() - startTime
      println(s"execTime: ${Duration(cycleTimeNs, NANOSECONDS).toMillis} ms")
      val newRateNs = desiredCycleTime.toNanos * 1.0 / cycleTimeNs * rate.toNanos
      println(s"newRate: ${Duration(newRateNs, NANOSECONDS).toNanos / 1000000.0} ms")
      run(source, sink, FiniteDuration(newRateNs.toLong, NANOSECONDS))
    }
  }

}

case object Tick
