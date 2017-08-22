package sample.cluster.factorial

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory

import scala.annotation.tailrec
import scala.concurrent.Future

/**
  * 执行阶乘计算
  *
  * Created by yanglei on 2016/12/9.
  *
  */
class FactorialBackend extends Actor with ActorLogging {

  import context.dispatcher

  def factorial(n: Int): BigInt = {
    @tailrec
    def factorialAcc(acc: BigInt, n: Int): BigInt = {
      if (n <= 1)
        acc
      else
        factorialAcc(acc * n, n - 1)
    }

    factorialAcc(BigInt(1), n)
  }

  override def receive: Receive = {
    case (n: Int) =>
      Future(factorial(n)) map {
        result => (n, result)
      } pipeTo sender()
  }
}

object FactorialBackend {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load("factorial"))

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[FactorialBackend], name = "factorialBackend")

    system.actorOf(Props[MetricsListener], name = "metricsListener")
  }
}