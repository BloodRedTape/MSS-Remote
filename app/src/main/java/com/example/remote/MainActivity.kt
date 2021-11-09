package com.example.remote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.InetAddress

class ServerAdapter(activity: MainActivity) : RecyclerView.Adapter<ServerAdapter.ViewHolder>() {
    private var dataSet: ArrayList<Server> = ArrayList<Server>();
    private var m_Activity = activity

    fun setServers(servers: ArrayList<Server>){
        dataSet = servers;
        notifyDataSetChanged()
    }

    class ViewHolder(adapter: ServerAdapter, view: View) : RecyclerView.ViewHolder(view) {
        var m_Owner = adapter
        var m_Layout_Root: ViewGroup
        var m_TextView_Name: TextView
        var m_TextView_Address: TextView
        var m_TextView_BootupTime: TextView

        init {
            m_Layout_Root = view.findViewById(R.id.Layout_Root) as ViewGroup
            m_TextView_Name = view.findViewById(R.id.TextView_Name) as TextView
            m_TextView_BootupTime = view.findViewById(R.id.TextView_BootupTime) as TextView
            m_TextView_Address = view.findViewById(R.id.TextView_Address) as TextView

            m_Layout_Root.setOnClickListener({v: View -> launchDetailed()})
        }

        fun launchDetailed(){
            m_Owner.m_Activity.startActivity(DetailsActivity.createIntent(m_Owner.m_Activity, m_Owner.dataSet[adapterPosition]))
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.server_view, viewGroup, false)

        return ViewHolder(this, view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.m_TextView_Name.text = dataSet[position].Name;
        viewHolder.m_TextView_Address.text = dataSet[position].Address.toString().removePrefix("/")
        viewHolder.m_TextView_BootupTime.text = (dataSet[position].Time/1000u/60u).toString() + " minutes"
    }

    override fun getItemCount() = dataSet.size

}


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener{
    private lateinit var m_RecyclerView_List: RecyclerView
    lateinit var m_RefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.INTERNET ), 0)

        m_RecyclerView_List = findViewById(R.id.RecyclerView_List) as RecyclerView
        m_RefreshLayout = findViewById(R.id.RefreshView)as SwipeRefreshLayout


        m_RefreshLayout.setOnRefreshListener(this)
        m_RecyclerView_List.adapter = ServerAdapter(this)
        m_RecyclerView_List.layoutManager = LinearLayoutManager(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        ScanNetwork()
    }

    override fun onRefresh() {
        ScanNetwork()
    }

    fun setServers(servers: ArrayList<Server>){
        (m_RecyclerView_List.adapter as ServerAdapter).setServers(servers)
    }

    private fun ScanNetwork(){
        var task = Broadcast(this)
        task.execute()
    }
}