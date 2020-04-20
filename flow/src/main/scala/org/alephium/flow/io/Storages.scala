package org.alephium.flow.io

import java.nio.file.Path

import org.rocksdb.WriteOptions

import org.alephium.flow.io.RocksDBSource.ColumnFamily
import org.alephium.flow.trie.MerklePatriciaTrie
import org.alephium.flow.trie.MerklePatriciaTrie.Node
import org.alephium.protocol.ALF.Hash
import org.alephium.protocol.config.GroupConfig

object Storages {
  val isInitializedPostfix: Byte = 0
  val blockStatePostfix: Byte    = 1
  val trieHashPostfix: Byte      = 2
  val heightPostfix: Byte        = 3
  val chainStatePostfix: Byte    = 4

  trait Config {
    def blockCacheCapacity: Int
  }

  def createUnsafe(
      rootPath: Path,
      dbFolder: String,
      blocksFolder: String,
      writeOptions: WriteOptions)(implicit config: GroupConfig with Config): Storages = {
    val blockStorage      = BlockStorage.createUnsafe(rootPath, blocksFolder, config.blockCacheCapacity)
    val db                = createRocksDBUnsafe(rootPath, dbFolder)
    val headerStorage     = BlockHeaderRockDBStorage(db, ColumnFamily.Header, writeOptions)
    val blockStateStorage = BlockStateRockDBStorage(db, ColumnFamily.All, writeOptions)
    val nodeStateStorage  = NodeStateRockDBStorage(db, ColumnFamily.All, writeOptions)
    val trieStorage       = RocksDBKeyValueStorage[Hash, Node](db, ColumnFamily.Trie, writeOptions)
    val emptyTrie         = MerklePatriciaTrie.createStateTrie(trieStorage)
    val trieHashStorage   = TrieHashRockDBStorage(trieStorage, db, ColumnFamily.All, writeOptions)

    Storages(db,
             headerStorage,
             blockStorage,
             emptyTrie,
             trieHashStorage,
             blockStateStorage,
             nodeStateStorage)
  }

  private def createRocksDBUnsafe(rootPath: Path, dbFolder: String): RocksDBSource = {
    val dbPath = rootPath.resolve(dbFolder)
    RocksDBSource.openUnsafe(dbPath, RocksDBSource.Compaction.HDD)
  }
}

final case class Storages(
    rocksDBSource: RocksDBSource,
    headerStorage: BlockHeaderStorage,
    blockStorage: BlockStorage,
    trieStorage: MerklePatriciaTrie,
    trieHashStorage: TrieHashStorage,
    blockStateStorage: BlockStateStorage,
    nodeStateStorage: NodeStateStorage
) {
  def close(): IOResult[Unit] = rocksDBSource.close()

  def closeUnsafe(): Unit = rocksDBSource.closeUnsafe()
}
