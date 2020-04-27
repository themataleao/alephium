package org.alephium.flow.io

import akka.util.ByteString
import org.rocksdb.{ReadOptions, WriteOptions}

import org.alephium.flow.io.HeightIndexStorage.hashesSerde
import org.alephium.flow.io.RocksDBSource.ColumnFamily
import org.alephium.protocol.ALF.Hash
import org.alephium.protocol.model.ChainIndex
import org.alephium.serde._
import org.alephium.util.{AVector, Bits}

object HeightIndexStorage {
  implicit val hashesSerde: Serde[AVector[Hash]] = avectorSerde[Hash]
}

class HeightIndexStorage(
    chainIndex: ChainIndex,
    storage: RocksDBSource,
    cf: ColumnFamily,
    writeOptions: WriteOptions,
    readOptions: ReadOptions
) extends RocksDBKeyValueStorage[Int, AVector[Hash]](storage, cf, writeOptions, readOptions) {
  private val postFix =
    ByteString(chainIndex.from.value.toByte, chainIndex.to.value.toByte, Storages.heightPostfix)

  override def storageKey(key: Int): ByteString = Bits.toBytes(key) ++ postFix
}