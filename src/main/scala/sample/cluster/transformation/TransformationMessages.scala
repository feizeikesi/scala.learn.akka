package sample.cluster.transformation

/**
  * Created by yanglei on 2016/12/5.
  */


final  case class TransformationJob(text:String)
final case class TransformationResult(text: String)
final case class JobFailed(reason: String, job: TransformationJob)
case object BackendRegistration
