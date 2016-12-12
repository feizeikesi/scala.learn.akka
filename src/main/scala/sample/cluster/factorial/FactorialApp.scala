package sample.cluster.factorial

/**
  * Created by yanglei on 2016/12/9.
  */
object FactorialApp {

  def main(args: Array[String]): Unit = {
    FactorialBackend.main(Seq("2551").toArray)
    FactorialBackend.main(Seq("2552").toArray)
    FactorialFrontend.main(Array.empty)
    FactorialBackend.main(Array.empty)
  }
}
