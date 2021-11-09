package com.example.remote

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class Server(address: InetAddress, name: String, time: ULong){
    var Address: InetAddress = address
    var Name: String = name
    var Time: ULong = time

    fun toStringArray(): Array<String>{
        var array = ArrayList<String>(3)
        array[0] = Name
        array[1] = Address.toString().removePrefix("/")
        array[2] = (Time/1000u/60u).toString() + " minutes"
        return array.toTypedArray();
    }
}

class Broadcast(activity: MainActivity): AsyncTask<Void, Void, ArrayList<Server>>(){
    private var m_Issuer = activity
    private var m_Socket = DatagramSocket()
    private var m_Datagram = Request(RequestType.SystemInfo).toDatagram()

    init{
        m_Socket.soTimeout = 400
    }

    override fun doInBackground(vararg params: Void?): ArrayList<Server>? {
        var packet = m_Datagram.toPacket()
        packet.address = InetAddress.getByName("255.255.255.255");
        packet.port = Port

        m_Socket.send(packet)

        var servers = ArrayList<Server>();

        while(true){
            try{
                var packet = DatagramPacket(64)

                m_Socket.receive(packet)

                if(packet.length >= 18) {
                    var array = packet.data.toUByteArray()
                    if(getDatagramType(packet) == DatagramType.Reply.value){
                        var base = 10
                        if(getReplyType(packet) == ReplyType.SystemInfo.value || getReplyType(packet) == 0.toByte()){

                            if(getReplyType(packet) == 0.toByte())base-=1 // made for backward compatabillity

                            var time: ULong = ExtractULong(array, base)
                            var length: UInt = ExtractUInt(array, base + 8)

                            var name = String(packet.data, base + 12, length.toInt());

                            servers.add(Server(packet.address, name, time))
                        }
                    }
                }
            }catch (e: SocketTimeoutException){
                break;
            }
        }
        return servers
    }

    override fun onPostExecute(result: ArrayList<Server>?) {
        super.onPostExecute(result)
        if(result != null) {
            m_Issuer.setServers(result)
            Toast.makeText(m_Issuer.applicationContext, "Found " + result.size, Toast.LENGTH_SHORT).show()
            m_Issuer.m_RefreshLayout.isRefreshing = false
        }
    }
}

abstract class NetworkTask<Args, Progress, Result>(activity: AppCompatActivity, ip: InetAddress, port: Int): AsyncTask<Args, Progress, Result>(){
    protected var Issuer = activity;
    private var IP = ip
    private var Port = port
    protected var NoResponse = false

    abstract fun getDatagram(): ByteArray

    abstract fun extractResult(array: ByteArray, length: Int): Result

    abstract fun processReply(result: Result?)

    override fun doInBackground(vararg params: Args?): Result? {
        var datagram = getDatagram()
        var packet = DatagramPacket(datagram, datagram.size, IP, Port)
        var socket = DatagramSocket()
        socket.soTimeout = 1000;

        socket.send(packet)

        try{
            var array = ByteArray(64)
            var packet = DatagramPacket(array, array.size)
            socket.receive(packet)

            return extractResult(packet.data, packet.length)
        }catch (e: SocketTimeoutException){
            NoResponse = true
        }
        return null;
    }

    override fun onPostExecute(result: Result?) {
        super.onPostExecute(result)
        processReply(result)
        if(NoResponse)
            Toast.makeText(Issuer.applicationContext, "Нет ответа", Toast.LENGTH_SHORT).show()
    }
}

class Shutdown(mainActivity: AppCompatActivity, ip: InetAddress, port: Int): NetworkTask<Void, Void, Void?>(mainActivity, ip, port){
    override fun getDatagram(): ByteArray{
        return MagicWord + DatagramType.Signal.value + SignalType.Shutdown.value
    }

    override fun extractResult(array: ByteArray, length: Int): Void?{
        return null
    }

    override fun processReply(result: Void?){
        if(!NoResponse)
            Toast.makeText(Issuer.applicationContext, "Выключено!", Toast.LENGTH_SHORT).show()
    }
}
