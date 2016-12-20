import akka.NotUsed

import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import com.sun.glass.ui.MenuItem.Callback

/**
  * Created by Dima.Statz on 12/18/2016.
  */
case class Envelope(data: String)

object Boot{
  import system.dispatcher
  implicit val system = ActorSystem("akka-example")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val routes = routeDefault() ~ routeSomeApi()
    val binding = Http().bindAndHandle(routes, "0.0.0.0", 8080)

    //logger.debug(s"akka streams http server on 8080")
    StdIn.readLine()

    sys addShutdownHook {
      //logger.debug(s"akka streams http server is shutting down")
      binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }
  }

  def defineSomeApiGetFlow(): Flow[HttpRequest, HttpResponse, NotUsed] = {
    val flowStep1 = Flow[HttpRequest].map(i => Envelope("Request parsed"))
    val flowStep2 = Flow[Envelope].map(i => Envelope(i.data + "->Some works is done"))
    val flowStep3 = Flow[Envelope].map(i => HttpResponse(entity = i.data + "->Response"))
    flowStep1 via flowStep2 via flowStep3
  }

  def routeDefault(): Route = get {
    pathEndOrSingleSlash {
      complete("Welcome to akka streams http server")
    }
  }

  def routeSomeApi(): Route = {
    pathPrefix("someapi") {
      path(Segment / Segment) {
        (parameter1, parameter2) => {
          get {
            extract(_.request){ req =>
              val futureResponse = Source.single(req).via(defineSomeApiGetFlow()).runWith(Sink.head)
              complete(futureResponse)
            }
          } ~ post {
            entity(as[String]) { data =>
              complete(s"post $parameter1 $parameter1")
            }
          }
        }
      }
    }
  }
}
