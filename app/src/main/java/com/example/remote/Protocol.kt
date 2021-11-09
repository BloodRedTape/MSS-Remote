package com.example.remote

import java.net.DatagramPacket

var MagicWord = byteArrayOfInts(0xBB, 0xAA, 0xDD, 0x00, 0x00, 0xDD, 0xAA, 0xBB)
var Port = 5432

enum class DatagramType(val value: Byte){
    Request(1),
    Signal(2),
    Reply(3)
}

enum class SignalType(val value: Byte){
    Shutdown(1)
}

enum class RequestType(val value: Byte){
    SystemInfo(1)
}

enum class ReplyType(val value: Byte){
    SystemInfo(1),
    SignalSuccess(2)
}

class Datagram(type: DatagramType, data: ByteArray){
    var Type: DatagramType = type
    var Data: ByteArray = data;

    fun toPacket(): DatagramPacket{
        return DatagramPacket(MagicWord + Type.value+ Data, Data.size + 9);
    }
}

class Request(type: RequestType){
    var Type = type

    fun toDatagram(): Datagram{
       return Datagram(DatagramType.Request, byteArrayOf(Type.value))
    }
}

class Signal(type: SignalType){
    var Type = type

    fun toDatagram(): Datagram{
        return Datagram(DatagramType.Signal, byteArrayOf(Type.value))
    }
}

class Reply(type: ReplyType){
    var Type = type

    fun toDatagram(): Datagram{
        return Datagram(DatagramType.Reply, byteArrayOf(Type.value))
    }
}

fun DatagramPacket(size: Int): DatagramPacket{
    var array = ByteArray(size)
    return DatagramPacket(array, array.size)
}

fun getDatagramType(packet: DatagramPacket):Byte = packet.data[8]

fun getReplyType(packet: DatagramPacket): Byte = packet.data[9]
