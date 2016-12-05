import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by yanglei on 2016/12/2.
  */
object SimpleClusterApp {

  def startup(ports: Seq[String]) = {
    ports.foreach(port => {
      //重写配置文件中的端口
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.load())

      //创建Akka系统
      val system = ActorSystem("ClusterSystem", config)

      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")
    })
  }

  def main(args: Array[String]) {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
    else
      startup(args)
  }
}


