
trait HList {

}

trait HNil extends HList {

}

class HCon[T, H <: HList]() {
  def ::() = {

  }
}