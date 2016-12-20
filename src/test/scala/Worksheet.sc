import com.sun.glass.ui.MenuItem.Callback

import scala.collection.immutable.HashMap

def log[A,B](callback:(A) => B)(a: A):B = {
  println("starting " + a)
  val b = callback.apply(a)
  println("finished " + b)
  b
}

def sqr(a: Int): Int = a*2

log[Int,Int](sqr)(2)


val map1 = HashMap[Int,Int](1->1,2->2)
val map2 = HashMap[Int,Int](1->1,4->4)

val list = List(map1, map2)
list.flatten.toMap


