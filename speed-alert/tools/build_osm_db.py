#!/usr/bin/env python3
"""Produce numeric maxspeed-only offline SQLite for Brasília/DF and surroundings.
All map data (c) OpenStreetMap contributors, licensed ODbL 1.0.
"""
import sys,sqlite3,re,pathlib,datetime
import osmium
input_file,output=sys.argv[1:]
out=pathlib.Path(output);out.parent.mkdir(parents=True,exist_ok=True)
if out.exists():out.unlink()
db=sqlite3.connect(out)
db.executescript("""
CREATE TABLE roads(id INTEGER PRIMARY KEY,lat1 REAL,lon1 REAL,lat2 REAL,lon2 REAL,fwd REAL,bwd REAL);
CREATE VIRTUAL TABLE road_index USING rtree(id,minLat,maxLat,minLon,maxLon);
CREATE TABLE metadata(k TEXT PRIMARY KEY,v TEXT);
""")
def limit(text):
    if not text:return None
    m=re.fullmatch(r"(\d{1,3})\s*(km/h|kmh|mph)?",text.lower().strip())
    if not m:return None
    n=int(m.group(1))
    if n<10 or n>160:return None
    return round(n*1.609344,2) if m.group(2)=="mph" else float(n)
class Collector(osmium.SimpleHandler):
    def __init__(self):
        super().__init__();self.rows=[];self.idx=[]
    def way(self,w):
        tag=w.tags.get("highway")
        if not tag or tag in ("footway","steps","path","cycleway","pedestrian","construction","proposed"):return
        if w.tags.get("maxspeed:conditional"):return
        standard=limit(w.tags.get("maxspeed"))
        fw=limit(w.tags.get("maxspeed:forward")) or standard
        bw=limit(w.tags.get("maxspeed:backward")) or standard
        if fw is None and bw is None:return
        try:pts=[(node.lat,node.lon) for node in w.nodes]
        except (osmium.InvalidLocationError,ValueError):return
        for a,b in zip(pts,pts[1:]):
            if not (any(-16.30<=p[0]<=-15.35 and -48.43<=p[1]<=-47.18 for p in (a,b))):continue
            if a==b:continue
            j=len(self.rows)+1
            self.rows.append((j,*a,*b,fw,bw))
            self.idx.append((j,min(a[0],b[0]),max(a[0],b[0]),min(a[1],b[1]),max(a[1],b[1])))
h=Collector()
h.apply_file(input_file,locations=True)
db.executemany("INSERT INTO roads VALUES(?,?,?,?,?,?,?)",h.rows)
db.executemany("INSERT INTO road_index VALUES(?,?,?,?,?)",h.idx)
db.executemany("INSERT INTO metadata VALUES(?,?)",[
  ("attribution","OpenStreetMap contributors (ODbL 1.0)"),
  ("extract","https://download.geofabrik.de/south-america/brazil/centro-oeste.html"),
  ("created_utc",datetime.datetime.now(datetime.timezone.utc).isoformat()),
  ("segments",str(len(h.rows)))
])
db.commit();print("OSM roads with explicit numeric speed:",len(h.rows))
if len(h.rows)<200:raise SystemExit("Coverage too low: refusing to publish")
db.execute("VACUUM");db.close()
