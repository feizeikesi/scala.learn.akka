package sample.cluster.stats

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.FromConfig

import scala.concurrent.duration._

/**
  * Created by yanglei on 2016/12/5.
  */
class StatsService extends Actor {
  val workerRouter = context.actorOf(FromConfig.props(Props[StatsWorker]), name = "workerRouter")

  override def receive: Receive = {
    case StatsJob(text) if !text.isEmpty =>
      val words = text.split(" ")
      val replyTo = sender()

      val aggregate = context.actorOf(Props(classOf[StateAggregator], words.size, replyTo))

      words.foreach({ word =>
        workerRouter.tell(ConsistentHashableEnvelope(word, word), aggregate)
      })
  }
}

class StateAggregator(expectedResults: Int, replyTo: ActorRef) extends Actor {

  var results = IndexedSeq.empty[Int]
  context.setReceiveTimeout(3.seconds)

  override def receive: Receive = {
    case wordCount: Int =>
      results = results :+ wordCount
      if (results.size == expectedResults) {
        val meanWordLength = results.sum.toDouble / results.size
        replyTo ! StatsResult(meanWordLength)
        context.stop(self)
      }
    case ReceiveTimeout=>
      replyTo! JobFailed("服务不可用,稍后再试")
      context.stop(self)
  }
}
