package com.fiser.akka.cluster.frontend

import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.fiser.akka.cluster.{BackendRegistration, JobFailed, TransformationJob}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

class TransformationFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef]

  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later", job)
    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

    case BackendRegistration if !backends.contains(sender()) =>
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) => backends.filterNot(_ == a)
  }
}

object TransformationFrontend {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val address = java.net.InetAddress.getLocalHost.getHostAddress
    println(s"---> address = $address")
    val config = ConfigFactory.load()

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")
    system.actorOf(Props[SimpleClusterListener], name = "clusterListener2")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {
      implicit val timeout = Timeout(5.seconds)
      (frontend ? TransformationJob("hello-" + counter.incrementAndGet())) onSuccess {
        case result => println(result)
      }
    }

  }
}