package sample.cluster.stats

import akka.actor.{Actor, ActorRef, ActorSystem, Props, UntypedActor}

class BasicActor extends UntypedActor{

  override def preStart(): Unit = super.preStart()

  override def postStop(): Unit = super.postStop()

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = super.preRestart(reason, message)

  override def postRestart(reason: Throwable): Unit = super.postRestart(reason)

  override def onReceive(message: Any): Unit = {

  }

}




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


