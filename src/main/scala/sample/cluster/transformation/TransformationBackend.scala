package sample.cluster.transformation

import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.protobuf.msg.ClusterMessages.MemberStatus
import akka.cluster.{Cluster, Member}
import com.typesafe.config.ConfigFactory

/**
  * Created by yanglei on 2016/12/5.
  */
class TransformationBackend extends Actor {
  val cluster = Cluster(context.system)


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) =>
      register(m)
  }


  def register(member: Member) = {
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! BackendRegistration
  }
}

object TransformationBackend{
  def main(args: Array[String]): Unit = {

    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationBackend], name = "backend")
  }
}
