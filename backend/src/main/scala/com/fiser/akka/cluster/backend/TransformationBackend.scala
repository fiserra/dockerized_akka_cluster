package com.fiser.akka.cluster.backend

import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import com.fiser.akka.cluster.{BackendRegistration, TransformationResult, TransformationJob}
import com.typesafe.config.ConfigFactory

class TransformationBackend extends Actor {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) =>
      sender() ! TransformationResult(text.toUpperCase)
    case MemberUp(m) => register(m)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
  }

  def register(member: Member): Unit = {
    if(member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" /"frontend") ! BackendRegistration
  }
}

object TransformationBackend {
  def main(args: Array[String]): Unit = {
    args.toList.foreach(println)

    val address = java.net.InetAddress.getLocalHost.getHostAddress
    println(s"---> address = $address")

    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname=$address")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationBackend], name = "backend")

    system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

  }
}