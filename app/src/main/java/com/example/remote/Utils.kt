package com.example.remote

fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }


fun ULong.toByteArray(): ByteArray{
    var array = ByteArray(8)
    array[0] = (this and 0xFF00000000000000u).shr(56).toByte();
    return array;
}

fun ExtractULong(array: UByteArray, base: Int): ULong{
    var value: ULong = 0u
    var i = 0
    var offset = 56
    while (i < 8) {
        var d = array[base + i].toULong();
        value = value or d.shl(offset).toULong()
        offset -= 8
        i++
    }
    return value
}
fun ExtractUInt(array: UByteArray, base: Int): UInt{
    return array[base + 0].toUInt() or array[base + 1].toUInt().shl(16) or array[base + 2].toUInt().shl(8) or array[base + 3].toUInt().shl(0)
}