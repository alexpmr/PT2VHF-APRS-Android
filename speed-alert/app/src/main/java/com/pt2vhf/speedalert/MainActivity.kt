package com.pt2vhf.speedalert

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity: Activity() {
  private lateinit var speed:TextView
  private lateinit var limit:TextView
  private lateinit var status:TextView
  private lateinit var source:TextView
  private lateinit var input:EditText
  private lateinit var manual:Switch
  private val pref by lazy { getSharedPreferences("config",MODE_PRIVATE) }
  private val receiver=object:BroadcastReceiver() {
    override fun onReceive(c:Context?,i:Intent?) {
      if(i?.action==MonitorService.UPDATE)
        render(i.getDoubleExtra("speed",-1.0),i.getDoubleExtra("limit",-1.0),
          i.getStringExtra("status")?:"GPS indisponível",
          i.getStringExtra("source")?:"--",i.getBooleanExtra("alarm",false))
    }
  }
  private fun text(s:String,size:Float):TextView=TextView(this).apply {
    text=s;textSize=size;setTextColor(Color.WHITE);gravity=Gravity.CENTER
    setPadding(12,18,12,18)
  }
  private fun add(root:LinearLayout,view:android.view.View) {
    val p=LinearLayout.LayoutParams(-1,-2);p.setMargins(0,8,0,8);root.addView(view,p)
  }
  override fun onCreate(savedInstanceState:Bundle?) {
    super.onCreate(savedInstanceState)
    window.statusBarColor=Color.rgb(12,20,29);window.navigationBarColor=Color.rgb(12,20,29)
    val scroll=ScrollView(this).apply { setBackgroundColor(Color.rgb(12,20,29)) }
    val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(24,18,24,24) }
    scroll.addView(box)
    add(box,text("PT2VHF SPEED ALERT",24f).apply { setTextColor(Color.rgb(94,214,156)) })
    add(box,text("VELOCIDADE ATUAL",14f))
    speed=text("--.-",82f).apply { setBackgroundColor(Color.rgb(28,45,62)) }
    add(box,speed)
    add(box,text("km/h",16f))
    add(box,text("LIMITE DA VIA",14f))
    limit=text("--",44f).apply { setBackgroundColor(Color.rgb(28,45,62)) }
    add(box,limit)
    status=text("MONITORAMENTO PARADO",18f)
    add(box,status)
    source=text("Sem acesso à Internet necessário durante o trajeto.",13f)
    add(box,source)
    manual=Switch(this).apply {
      text="Limite manual (substitui o limite da base offline)"
      setTextColor(Color.WHITE);isChecked=pref.getBoolean("manual",false)
      setOnCheckedChangeListener { _,v ->
        pref.edit().putBoolean("manual",v).apply()
        input.isEnabled=v;saveManual()
      }
    }
    add(box,manual)
    input=EditText(this).apply {
      inputType=InputType.TYPE_CLASS_NUMBER;gravity=Gravity.CENTER
      setText(pref.getInt("manualLimit",60).toString());setTextColor(Color.WHITE)
      hint="Limite km/h";isEnabled=manual.isChecked
    }
    add(box,input)
    val buttons=LinearLayout(this)
    val start=Button(this).apply { text="INICIAR";setOnClickListener { saveManual();startMonitor() } }
    val stop=Button(this).apply {
      text="PARAR"
      setOnClickListener {
        startService(Intent(this@MainActivity,MonitorService::class.java).setAction(MonitorService.STOP))
        render(-1.0,-1.0,"MONITORAMENTO PARADO","--",false)
      }
    }
    buttons.addView(start,LinearLayout.LayoutParams(0,-2,1f))
    buttons.addView(stop,LinearLayout.LayoutParams(0,-2,1f))
    add(box,buttons)
    add(box,text("Tolerância: 0 km/h | Bipes: 250 ms / 350 ms | Volume: 80% | Base OSM offline",12f))
    add(box,text("© OpenStreetMap contributors (ODbL). Base experimental: a sinalização oficial prevalece.",12f))
    setContentView(scroll)
  }
  private fun saveManual() {
    val n=input.text.toString().toIntOrNull()
    if(n!=null && n in 10..130) pref.edit().putInt("manualLimit",n).apply()
  }
  private fun startMonitor() {
    if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
      val perms=mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
      if(Build.VERSION.SDK_INT>=33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
      requestPermissions(perms.toTypedArray(),440)
      return
    }
    startForegroundService(Intent(this,MonitorService::class.java).setAction(MonitorService.START))
  }
  override fun onRequestPermissionsResult(code:Int,perms:Array<out String>,results:IntArray) {
    super.onRequestPermissionsResult(code,perms,results)
    if(code==440 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
      startMonitor()
    else if(code==440) Toast.makeText(this,"Autorize localização precisa.",Toast.LENGTH_LONG).show()
  }
  override fun onStart() {
    super.onStart()
    if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,IntentFilter(MonitorService.UPDATE),Context.RECEIVER_NOT_EXPORTED)
    else @Suppress("DEPRECATION") registerReceiver(receiver,IntentFilter(MonitorService.UPDATE))
    MonitorService.latest?.let { render(it.speed,it.limit,it.status,it.source,it.alarm) }
  }
  override fun onStop() { unregisterReceiver(receiver);super.onStop() }
  private fun render(v:Double,max:Double,message:String,origin:String,alarm:Boolean) {
    speed.text=if(v>=0) String.format(Locale.US,"%.1f",v) else "--.-"
    limit.text=if(max>=0) String.format(Locale.US,"%.0f km/h",max) else "--"
    status.text=message;source.text="Fonte: "+origin
    speed.setTextColor(if(alarm) Color.rgb(255,85,85) else Color.WHITE)
    status.setTextColor(if(alarm) Color.rgb(255,85,85) else Color.rgb(94,214,156))
  }
}
