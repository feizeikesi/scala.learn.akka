package sample.cluster.factorial

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by yanglei on 2016/12/9.
  */
class FactorialFrontend(upToN: Int, repeat: Boolean) extends Actor with ActorLogging {
  //创建路由
  val backend = context.actorOf(FromConfig.props(), name = "factorialBackendRouter")


  def sendJobs() = {
    log.info("开始批量阶乘 [{}]", upToN)
    1 to upToN foreach {
      backend ! _
    }
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    sendJobs()
    if (repeat) {
      context.setReceiveTimeout(10.seconds)
    }
  }

  override def receive: Receive = {
    case (n: Int, factorial: BigInt) =>
      if (n == upToN) {
        log.debug("{}!={}", n, factorial)

        if (repeat)
          sendJobs()
        else
          context.stop(self)
      }
  }
}


object  FactorialFrontend{
  def main(args: Array[String]): Unit = {
    val upToN=200
    val config = ConfigFactory.parseString("akka.cluster.roles = [frontend]").
      withFallback(ConfigFactory.load("factorial"))
    val system = ActorSystem("ClusterSystem", config)
    system.log.info("集群将开启2个backend进行阶乘")

    //registerOnUp
    Cluster(system).registerOnMemberUp{
      system.actorOf(Props(classOf[FactorialFrontend],upToN,true),name="factorialFrontend")
    }

    Cluster(system).registerOnMemberRemoved{
      //ActorSystem 终止时退出JVM
      system.registerOnTermination(System.exit(0))
      //关闭ActorSystem
      system.terminate()

      //防止ActorSystem退出超时,导致jvm假死
      new Thread{
        override def run(): Unit = {
          if (Try(Await.ready(system.whenTerminated,10.second)).isFailure)
            System.exit(-1)
        }
      }.start()
    }
  }
}