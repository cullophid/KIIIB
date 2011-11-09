package utils
class BitArray(i: Int) {
  def hasFlag(flag: Int): Boolean = ((i & flag) == flag)
}
object BitArray {
  implicit def int2bitArray(i: Int) = new BitArray(i)
}

// vim: set et sw=2 ts=2:

