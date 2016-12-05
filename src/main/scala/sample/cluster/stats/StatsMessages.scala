package sample.cluster.stats

/**
  * Created by yanglei on 2016/12/5.
  */
final case class StatsJob(text: String)
final case class StatsResult(meanWordLength: Double)
final case class JobFailed(reason: String)