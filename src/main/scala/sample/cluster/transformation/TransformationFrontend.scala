package sample.cluster.transformation

import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by yanglei on 2016/12/5.
  */
class TransformationFrontend extends Actor {

  //后台角色池
  var backends = IndexedSeq.empty[ActorRef]
  //当前作业处理数量,用来均衡分发给后台角色
  var jobCounter = 0

  override def receive: Receive = {
    //后台角色池为空时,无法处理作业
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("服务不可用,稍后再试", job)

    //处理作业
    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

      //新后台角色注册
    case BackendRegistration if !backends.contains(sender()) =>
      //当前角色观察监控发送者,当发送者挂掉,会触发Terminated消息
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

object TransformationFrontend {
  def main(args: Array[String]) {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {
      implicit val timeout = Timeout(5 seconds)
      (frontend ? TransformationJob("hello-" + counter.incrementAndGet())) onSuccess {
        case result => println(result)
      }
    }
  }
}