package ru.dargen.evoplus.event.world.block

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import ru.dargen.evoplus.event.CancellableEvent

class BlockChangeEvent(
    val chunk: Chunk, val blockPos: BlockPos,
    val oldState: BlockState?, val newState: BlockState,
    val moved: Boolean
) : CancellableEvent()