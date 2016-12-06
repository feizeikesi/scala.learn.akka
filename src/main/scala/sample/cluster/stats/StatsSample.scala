package sample.cluster.stats

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorSystem, Address, Props, RelativeActorPath, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.protobuf.msg.ClusterMessages.MemberStatus
import com.typesafe.config.ConfigFactory

/**
  * Created by yanglei on 2016/12/6.
  */
object StatsSample {

  def startup(ports: Seq[String]): Unit = {
    ports.foreach { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]"))
        .withFallback(ConfigFactory.load("stats1"))

      val system = ActorSystem("ClusterSystem", config)

      system.actorOf(Props[StatsWorker], name = "statsWorker")
      system.actorOf(Props[StatsService], name = "statsService")
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup(Seq("2551", "2552", "0"))
      StatsSampleClient.main(Array.empty)
    } else {
      startup(args)
    }
  }
}

object StatsSampleClient {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[StatsSampleClient], "/user/statsService"), "client")
  }
}

import scala.concurrent.duration._

class StatsSampleClient(servicePath: String) extends Actor {
  val cluster = Cluster(context.system)

  val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements
    case _ => throw new IllegalArgumentException("servicePath [%s] 不是有效的Actor的相对路径")
  }

  import context.dispatcher

  val tickTask = context.system.scheduler.schedule(2.seconds, 2.seconds, self, "tick")

  var nodes = Set.empty[Address]


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[ReachabilityEvent])
  }


  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    tickTask.cancel()
  }

  override def receive: Receive = {
    case "tick" if nodes.nonEmpty =>
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current().nextInt(nodes.size))
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      service ! StatsJob("这是文本分析")
    case result: StatsResult => println(result)
    case failed: JobFailed => println(failed)
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case m if m.hasRole("compute") && m.status == MemberStatus.Up => m.address
      }
    case MemberUp(m) if m.hasRole("compute") => nodes += m.address
    case other: MemberEvent => nodes -= other.member.address
    case UnreachableMember(m) => nodes -= m.address
    case ReachableMember(m) if m.hasRole("compute") => nodes += m.address
  }
}