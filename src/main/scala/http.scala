package example

import cc.spray.{Directives,RequestContext}
import cc.spray.typeconversion.ChunkSender
import cc.spray.utils.{Logging,ActorHelpers}
import cc.spray.http._
import MediaTypes._

trait EventSourceService extends Directives with Logging {
  import EventSourceService._

  val service = 
    get {
      getFromResourceDirectory("www", pathRewriter = rewritePath) ~
      path("streaming"){
        respondAsEventStream {
          _.startChunkedResponse {
            HttpContent("data: start\n")
          } foreach sendNext(10)
        }
      }
    }

  import HttpHeaders.{`Cache-Control`, `Connection`}
  import CacheDirectives.`no-cache`
  // compose a couple of directives into one neat 
  // directive for event streams
  def respondAsEventStream = 
    respondWithHeader(`Cache-Control`(`no-cache`)) &
    respondWithHeader(`Connection`("Keep-Alive")) &
    respondWithMediaType(`text/event-stream`)
  
  import akka.util.Duration
  import akka.util.duration._
  import akka.actor.Scheduler
  import java.util.concurrent.TimeUnit

  def in[U](duration: Duration)(body: => U){
    Scheduler.scheduleOnce(() => body, duration.toMillis, TimeUnit.MILLISECONDS)
  }

  import StatusCodes.OK
  
  def sendNext(remaining: Int)(chunkSender: ChunkSender){
    println("~~~~~~~~~~~")
    println(remaining)

    in(500.millis){
      chunkSender
      .sendChunk(MessageChunk("data: " + (10 - remaining)*10 + "\n\n"))
      .onComplete {
        // we use the successful sending of a chunk as trigger for scheduling the next chunk
        _.value.get match {
          case Right(_) if remaining > 0 => sendNext(remaining - 1)(chunkSender)
          case Right(_) =>
            chunkSender.sendChunk(MessageChunk("\ndata: Finished."))
            chunkSender.close()
          case Left(e) => log.warn("Stopping response streaming due to " + e)
        }
      }
    }
  }

  // serve "/" as "index.html"
  private def rewritePath(path: String) = path match {
    case "" => "index.html"
    case x if x.indexOf('.') > 0 => x
    case x => x + ".html"
  }
}

object EventSourceService {
  val `text/event-stream` = CustomMediaType("text/event-stream")
  MediaTypes.register(`text/event-stream`)
}
