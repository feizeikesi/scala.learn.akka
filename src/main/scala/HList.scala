

/**
  * Created by Lei on 2017-8-21.
  */
sealed trait HList {
  type ViewAt[Inx <: Nat] <: IndexedView
}

sealed trait Nat {
  type Expand[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] <: Up
}


object Nat {

  sealed trait _0 extends Nat {
    type Expand[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = IfZero
  }

  sealed trait Succ[Prev <: Nat] extends Nat {
    type Expand[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = NonZero[Prev]
  }

  type _1 = Succ[_0]
  type _2 = Succ[_1]
}

final case class HCons[H, T <: HList](head: H, tail: T) extends HList {
  type FullType = HCons[H, T]

  def ::[V](v: V) = HCons(v, this)

  def viewAt[Idx <: Nat](implicit in: FullType => FullType#ViewAt[Idx]) = in(this.asInstanceOf[FullType])

  override type ViewAt[N <: Nat] = N#Expand[({type Z[P <: Nat] = HListViewN[H, T#ViewAt[P]]})#Z, HListView0[H, T], IndexedView]


  override def toString: String = head + " :: " + tail
}

final class HNil extends HList {
  def ::[T](v: T) = HCons(v, this)

  override def toString: String = "Nil"
}

object HList {
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons
  val HNil = new HNil
}

sealed trait IndexedView {
  type Before <: HList
  type After <: HList
  type At

  def fold[R](f: (Before, At, After) => R): R

  def get: At = fold((_, value, _) => value)
}


import HList._
object IndexedView {
  implicit def index0[H, T <: HList](list : H :: T) : HListView0[H,T] =
    new HListView0[H,T](list)
  implicit def indexN[H, T <: HList, Prev <: IndexedView](
                                                           list : (H :: T))(implicit indexTail : T => Prev) : HListViewN[H,Prev] =
    new HListViewN[H, Prev](list.head, indexTail(list.tail))
}
class HListView0[H, T <: HList](val list: H :: T) extends IndexedView {
  override type Before = HNil
  override type After = T
  override type At = H

  override def fold[R](f: (Before, At, After) => R): R = f(HList.HNil, list.head, list.tail)

}

class HListViewN[H, NextIndexedView <: IndexedView](h: H, next: NextIndexedView) extends IndexedView {
  override type Before = H :: NextIndexedView#Before
  override type After = NextIndexedView#After
  override type At = NextIndexedView#At

  override def fold[R](f: (Before, At, After) => R): R = next.fold((before, at, after) => f(HCons(h, before), at, after))
}

object Test {
  def main(args: Array[String]): Unit = {
    import HList._

    val x: (String :: Int :: Boolean :: HNil) = "Hello" :: 5 :: false :: HNil
    val first :: second :: rest = x



    println(x)
    println(x.viewAt[Nat._0].get)
  }
}