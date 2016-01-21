package com.fiser.akka.cluster.backend

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._


class SimpleClusterListener extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive = {
    case MemberUp(member) =>
      log.info("=====> Member is p: {}", member.address)
      printClusterState
    case UnreachableMember(member) =>
      log.info("=====> Member detected as unreachable: {}", member)
      printClusterState
    case MemberRemoved(member, previousStatus) =>
      log.info("=====> Member is Removed: {} after {}", member.address, previousStatus)
      printClusterState
    case UnreachableMember(member) =>
      log.info("=====> Member {} is unreachable", member.address)
      printClusterState
    case MemberExited(member) =>
      log.info("=====> Member {} exited", member.address)
      printClusterState
    case MemberJoined(member) =>
      log.info("=====> Member {} joined", member.address)
      printClusterState

  }

  private def printClusterState: Unit = {
    cluster.state.members.foreach(member => log.info(s"*** $member"))
  }
}

