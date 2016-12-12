package sample.cluster.factorial

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.StandardMetrics.{Cpu, HeapMemory}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension, NodeMetrics}

/**
  * Created by yanglei on 2016/12/9.
  */
class MetricsListener extends Actor with ActorLogging {

  val selfAddress = Cluster(context.system).selfAddress
  //集群指标扩展,主要用于节点的负载平衡
  val extension = ClusterMetricsExtension(context.system)


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = extension.subscribe(self)

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = extension.unsubscribe(self)

  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) =>
      log.info("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case _ => // No heap info.
  }

  def logCpu(nodeMetrice: NodeMetrics) = nodeMetrice match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.info("Load: {} ({} processors)", systemLoadAverage, processors)
    case _ =>
  }


  override def receive: Receive = {
    case ClusterMetricsChanged(clusterMetrics) => {
      clusterMetrics.filter(_.address == selfAddress) foreach { nodeMetrice =>
        logHeap(nodeMetrice)
        logCpu(nodeMetrice)
      }
    }
    case state: CurrentClusterState =>
  }
}
