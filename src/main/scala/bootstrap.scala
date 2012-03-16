package example

import akka.config.Supervision._
import akka.actor.{Supervisor, Actor}, Actor._
import cc.spray.can.HttpServer
import cc.spray.{SprayCanRootService,HttpService}
import org.slf4j.LoggerFactory

object Boot extends App {

  LoggerFactory.getLogger(getClass) // initialize SLF4J early

  val sse = new EventSourceService { }

  val httpService    = actorOf(new HttpService(sse.service))
  val rootService    = actorOf(new SprayCanRootService(httpService))
  val sprayCanServer = actorOf(new HttpServer())

  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(httpService, Permanent),
        Supervise(rootService, Permanent),
        Supervise(sprayCanServer, Permanent)
      )
    )
  )
}