package sample.cluster.stats

import akka.actor.Actor

/**
  * Created by yanglei on 2016/12/5.
  */
class StatsWorker extends Actor {
  var cache = Map.empty[String, Int]

  override def receive: Receive = {
    case word: String => {
      val length = cache.get(word) match {
        case Some(x) => x
        case None =>
          val x = word.length
          cache += (word -> x)
          x
      }
      sender() ! length
    }
  }
}
