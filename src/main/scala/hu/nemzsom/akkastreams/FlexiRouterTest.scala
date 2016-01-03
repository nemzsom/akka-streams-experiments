package hu.nemzsom.akkastreams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Attributes, FanOutShape}
import akka.stream.FanOutShape.{Name, Init}
import akka.stream.scaladsl.{FlowGraph, FlexiRoute, Sink, Source}
import akka.stream.scaladsl.FlowGraph.Implicits._

object FlexiRouterTest extends App {

  implicit val system = ActorSystem("FlexiRouter-test")
  implicit val materializer = ActorMaterializer()

  val graph = FlowGraph.closed() { implicit b =>
    val source = Source(List((0, 1),(2, 3), (4, 5)))

    val sink1 = Sink.foreach[Int](x => println("1: " + x))
    val sink2 = Sink.foreach[String](x => println("2: " + x))

    val disJoin = b.add(new DisJoin)

    source ~> disJoin.in
    disJoin.out1.mapConcat(identity) ~> sink1
    disJoin.out2.mapConcat(identity) ~> sink2
  }

  graph.run()
}

class DisJoinShape(_init: Init[(Int, Int)] = Name[(Int, Int)]("DisJoin")) extends FanOutShape[(Int, Int)](_init) {

  val out1 = newOutlet[List[Int]]("numbers")
  val out2 = newOutlet[List[String]]("strings")
  override protected def construct(i: Init[(Int, Int)]) = new DisJoinShape(i)
}

class DisJoin extends FlexiRoute[(Int, Int), DisJoinShape](
  new DisJoinShape, Attributes.name("DisJoin")) {
  import FlexiRoute._

  override def createRouteLogic(p: PortT): RouteLogic[(Int, Int)] = new RouteLogic[(Int, Int)] {
    override def initialState: State[_] =
      State(DemandFromAll(p.out1, p.out2)) {
        (ctx, _, element) =>
          val (out1, out2) = element
          ctx.emit(p.out1)((1 to out1).map(_ => out1).toList)
          ctx.emit(p.out2)((1 to out2).map(_ => s"'${out2.toString}'").toList)
          SameState
      }
  }
}

