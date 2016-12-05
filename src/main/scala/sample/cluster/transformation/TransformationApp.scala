package sample.cluster.transformation

/**
  * Created by yanglei on 2016/12/5.
  */
object TransformationApp {
  def main(args: Array[String]): Unit = {

    TransformationFrontend.main(Seq("2551").toArray)
    TransformationBackend.main(Seq("2552").toArray)
    TransformationBackend.main(Array.empty)
    TransformationBackend.main(Array.empty)
    TransformationFrontend.main(Array.empty)
  }
}
