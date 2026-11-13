# Gera app/src/main/java/com/pt2vhf/aprs/core/DeviceIdTable.kt: python3 scripts/gen-deviceid.py tocalls.yaml <saida.kt>
import sys, yaml
d = yaml.safe_load(open(sys.argv[1]))
def lab(e):
    v = (e.get('vendor') or '').strip(); m = (e.get('model') or '').strip()
    s = (v + ' ' + m).strip() if m and v and not m.lower().startswith(v.lower()) else (m or v)
    return s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$')
def q(s): return s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$')
out = ['package com.pt2vhf.aprs.core','',
'// GERADO por scripts/gen-deviceid.py a partir de tocalls.yaml do projeto aprs-deviceid',
'// (https://github.com/aprsorg/aprs-deviceid, Hessu OH7LZB, licença CC BY-SA 2.0). Não editar à mão.','',
'/** Tocall (com curingas ? n *) e nome do aparelho/aplicativo. */',
'internal val DEVICE_TOCALLS: Array<Pair<String, String>> = arrayOf(']
for e in d['tocalls']: out.append(f'    "{q(e["tocall"])}" to "{lab(e)}",')
out += [')','','/** Mic-E novo: sufixo de 2 caracteres no fim do comentário. */',
'internal val DEVICE_MICE: Array<Pair<String, String>> = arrayOf(']
for e in d['mice']: out.append(f'    "{q(e["suffix"])}" to "{lab(e)}",')
out += [')','','/** Mic-E antigo (Kenwood): prefixo, sufixo opcional ("" = nenhum) e nome. */',
'internal val DEVICE_MICE_LEGACY: Array<Triple<String, String, String>> = arrayOf(']
for e in d['micelegacy']: out.append(f'    Triple("{q(e["prefix"])}", "{q(e.get("suffix") or "")}", "{lab(e)}"),')
out += [')','']
open(sys.argv[2],'w').write('\n'.join(out))
