package com.pt2vhf.speedalert

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import java.io.File
import kotlin.math.*

class Roads(context:Context) {
  data class Result(val limit:Double?,val source:String)
  private val db:SQLiteDatabase?=try {
    val f=File(context.filesDir,"df_roads.sqlite")
    if(!f.exists()) context.assets.open("df_roads.sqlite").use { input ->
      f.outputStream().use { output -> input.copyTo(output) }
    }
    SQLiteDatabase.openDatabase(f.absolutePath,null,SQLiteDatabase.OPEN_READONLY)
  } catch(_:Exception) { null }
  fun resolve(loc:Location):Result {
    val d=db?:return Result(null,"BASE OFFLINE AUSENTE")
    val lat=loc.latitude;val lon=loc.longitude
    val latOffset=0.00045
    val lonOffset=latOffset/max(0.5,cos(Math.toRadians(lat)))
    val results=mutableListOf<Pair<Double,Double>>()
    try {
      d.rawQuery(
        "SELECT r.lat1,r.lon1,r.lat2,r.lon2,r.fwd,r.bwd FROM roads r "+
        "JOIN road_index idx ON r.id=idx.id "+
        "WHERE idx.maxLat>=? AND idx.minLat<=? AND idx.maxLon>=? AND idx.minLon<=? LIMIT 300",
        arrayOf((lat-latOffset).toString(),(lat+latOffset).toString(),
          (lon-lonOffset).toString(),(lon+lonOffset).toString())
      ).use { c ->
        while(c.moveToNext()) {
          val x1=(c.getDouble(1)-lon)*111320*cos(Math.toRadians(lat))
          val y1=(c.getDouble(0)-lat)*111320
          val x2=(c.getDouble(3)-lon)*111320*cos(Math.toRadians(lat))
          val y2=(c.getDouble(2)-lat)*111320
          val dx=x2-x1;val dy=y2-y1
          val norm=dx*dx+dy*dy
          if(norm<1.0) continue
          val t=(-(x1*dx+y1*dy)/norm).coerceIn(0.0,1.0)
          val dist=hypot(x1+t*dx,y1+t*dy)
          if(dist>27) continue
          val bearing=(Math.toDegrees(atan2(dx,dy))+360)%360
          val f=diff(loc.bearing.toDouble(),bearing)
          val b=diff(loc.bearing.toDouble(),(bearing+180)%360)
          val headingDiff=min(f,b)
          if(loc.hasBearing() && headingDiff>45) continue
          val index=if(f<=b) 4 else 5
          if(c.isNull(index)) continue
          results.add((dist+headingDiff*0.3) to c.getDouble(index))
        }
      }
    } catch(_:Exception) {return Result(null,"ERRO NA BASE OFFLINE")}
    results.sortBy { it.first }
    val top=results.firstOrNull()?:return Result(null,"LIMITE NÃO CADASTRADO")
    if(results.drop(1).any { it.first-top.first<7 && abs(it.second-top.second)>0.1 })
      return Result(null,"VIA AMBÍGUA")
    return Result(top.second,"OSM OFFLINE EXPERIMENTAL")
  }
  private fun diff(a:Double,b:Double)=abs((a-b+540)%360-180)
}
