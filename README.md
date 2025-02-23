# BlockStore
A Bukkit plugin that stores arbitrary metadata on a per-block basis, originally forked from <https://github.com/Sothatsit/BlockStore>.

BlockStore is a dependency of several NerdNu plugins and the original author has stopped maintaining it, so we have stepped into the breach.


## Features
 * Stores arbitary metadata, per-block.
 * Automatically tracks whether blocks are player-placed or natural.
 * Hooks WorldEdit, if present, and marks blocks placed by WorldEdit as natural (not player-placed).


## API
The [Spigot resource page for BlockStore](https://www.spigotmc.org/resources/blockstore.19494/) has some discussion of the API.

The API is [class BlockStoreApi](https://github.com/NerdNu/BlockStore/blob/master/src/main/java/net/sothatsit/blockstore/BlockStoreApi.java). 
As originally written, there are no JavaDoc doc comments; we might add some, in time.
