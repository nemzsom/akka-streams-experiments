package hu.nemzsom.akkastreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{SyncDirective, Context, PushStage}

object PushStateTest extends App {

  implicit val system = ActorSystem("FlexiRouter-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val longList: List[Activity] = Begin.asInstanceOf[Activity] +: (0 to 100).map(Element).toList :+ End.asInstanceOf[Activity]

  val mat = Source(List[Activity](Begin, Element(0), End, Begin, Element(0), Element(1), Element(2), End)
                   ::: longList
                   ::: List[Activity](Begin, End)
                   ::: List[Activity](Begin, Element(0), End))
    .transform(() => new Collector())
    .runWith(Sink.foreach[List[Element]](l => println("received list: " + l)))

  mat.onComplete (_ =>
    system.terminate().onComplete(_ => println("terminated"))
  )

}

trait Activity

case object Begin extends Activity
case object End extends Activity

case class Element(index: Int) extends Activity

class Collector extends PushStage[Activity, List[Element]] {

  val builder = List.newBuilder[Element]

  override def onPush(elem: Activity, ctx: Context[List[Element]]): SyncDirective = {
    elem match {
      case Begin =>
        println("new round begins")
        ctx.pull()
      case e : Element =>
        println(s"received element ${e.index}")
        builder += e
        ctx.pull()
      case End =>
        println("round end")
        val result = builder.result()
        builder.clear()
        ctx.push(result)
    }
  }
}