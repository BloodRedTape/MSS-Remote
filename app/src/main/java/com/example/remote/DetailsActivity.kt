package com.example.remote

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.InetAddress

class DetailsAdapter(data: ArrayList<Pair<String, String>>) : RecyclerView.Adapter<DetailsAdapter.ViewHolder>() {
    private var m_Data = data

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var m_TextView_Key: TextView
        var m_TextView_Value: TextView
        init {
            m_TextView_Key = view.findViewById(R.id.TextView_Key) as TextView
            m_TextView_Value = view.findViewById(R.id.TextView_Value) as TextView
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.property_view, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.m_TextView_Key.text = m_Data[position].first
        viewHolder.m_TextView_Value.text = m_Data[position].second
    }

    override fun getItemCount() = m_Data.size

}

class DetailsActivity : AppCompatActivity() {
    lateinit var m_Button_Shutdown: Button
    lateinit var m_PropertiesView: RecyclerView
    lateinit var m_Server: Server
    private var m_Properties = ArrayList<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        m_Server = getServer(intent)
        m_Properties.add(Pair("Name", m_Server.Name))
        m_Properties.add(Pair("Ip Address", m_Server.Address.toString().removePrefix("/")))
        m_Properties.add(Pair("Bootup Time", (m_Server.Time/1000u/60u).toString() + " minutes"))

        m_PropertiesView = findViewById(R.id.PropertiesView) as RecyclerView
        m_Button_Shutdown = findViewById(R.id.Button_Shutdown) as Button

        m_PropertiesView.layoutManager = LinearLayoutManager(this)
        m_PropertiesView.adapter = DetailsAdapter(m_Properties)

        m_Button_Shutdown.setOnClickListener({v: View->
            var task = Shutdown(this, m_Server.Address, Port)
            task.execute()
            finish()
        })
    }

    companion object {
        var NameKey = "__Name__"
        var TimeKey = "__Time__"
        var AddressKey = "__Address__"

        fun createIntent(activity: AppCompatActivity, server: Server): Intent {
            var intent = Intent(activity, DetailsActivity::class.java)
            intent.putExtra(NameKey, server.Name)
            intent.putExtra(TimeKey, server.Time.toLong())
            intent.putExtra(AddressKey, server.Address.toString().removePrefix("/"))
            return intent
        }

        fun getServer(intent: Intent): Server{
            var server = Server(
                InetAddress.getByName(intent.getStringExtra(AddressKey)),
                intent.getStringExtra(NameKey),
                intent.getLongExtra(TimeKey, 0).toULong()
            );
            return server
        }
    }
}