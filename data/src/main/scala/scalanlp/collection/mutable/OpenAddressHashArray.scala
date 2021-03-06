package scalanlp.collection.mutable

import collection.mutable.BitSet
import scalala.collection.sparse.DefaultArrayValue

/**
 * Represents a sparse int->v assoc sequence.
 * A companion to SparseArray based on hashing. Two parallel arrays, one of ints, one of keys.
 * @author dlwh
 */
@SerialVersionUID(1)
class OpenAddressHashArray[@specialized V:ClassManifest:DefaultArrayValue](val size: Int, initialSize:Int = 16) extends ArrayLike[V] with Serializable {
  private var index : Array[Int] = new Array[Int](initialSize)
  private var values: Array[V] = new Array[V](initialSize)
  private var occupied = new BitSet()
  private var load = 0;


  def keysIterator = occupied.iterator
  def length = size

  def activeSize = load

  def contains(i: Int) = occupied(locate(i))
  def apply(i: Int) = values(locate(i))

  def update(i: Int, v: V) {
    val pos = locate(i)
    if(!occupied(pos) && load >= index.size * .75) {
      rehash()
      update(i,v)
    } else {
      index(pos) = i
      values(pos) = v
    }
    if(!occupied(pos)) {
      occupied += pos
      load += 1
    }
  }

  def iterator = occupied.iterator.map(values)

  def pairsIterator = occupied.iterator.map(i => (index(i),values(i)))

  private def locate(i: Int) = {
    if(i >= size) throw new IndexOutOfBoundsException(i + " greater than size of " + size)
    if(i < 0) throw new IndexOutOfBoundsException(i + " less than 0")
    var hash = i.## % index.length
    while(occupied(hash) && index(hash) != i) {
      hash += 1
      hash %= index.length
    }
    hash
  }

  private def rehash() {
    val oldOccupied = occupied
    occupied = new BitSet()
    val oldIndex = index
    val oldValues = values
    index = new Array[Int](oldIndex.size * 2)
    values = new Array[V](oldIndex.size * 2)
    load = 0
    for(o <- oldOccupied) {
      update(oldIndex(o),oldValues(o))
    }
  }

  override def toString = {for( o <- occupied iterator) yield (index(o),values(o))}.mkString("OpenAddressHashArray(",",",")");


}