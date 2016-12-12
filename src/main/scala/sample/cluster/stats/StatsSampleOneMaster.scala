package sample.cluster.stats

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.typesafe.config.ConfigFactory

/**
  * Created by yanglei on 2016/12/7.
  */
object StatsSampleOneMaster {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup(Seq("2551", "2552", "0"))
      StatsSampleOneMasterClient.main(Array.empty)
    }else{
      startup(args)
    }
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]"))
        .withFallback(ConfigFactory.load("stats2"))

      val system = ActorSystem("ClusterSystem", config)

      system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[StatsService],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system).withRole("compute")
      ), name = "statsService")


      system.actorOf(ClusterSingletonProxy.props(singletonManagerPath = "/user/statsService",
        settings = ClusterSingletonProxySettings(system).withRole("compute")
      ), name = "statsServiceProxy")
    }
  }
}


object StatsSampleOneMasterClient{
  def main(args: Array[String]): Unit = {
    val system=ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[StatsSampleClient],"/user/statsServiceProxy"))
  }
}
