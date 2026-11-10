package com.pt2vhf.aprs.core

import java.util.Locale

/*
 * #59: validação de indicativo de radioamador pela tabela de blocos de prefixos da ITU
 * (Apêndice 42 do Regulamento de Radiocomunicações), embarcada e sem internet.
 *
 * Formato de cada bloco: "INÍCIO-FIM=PAÍS" (ou "PREFIXO=PAÍS"). PAÍS é o código ISO 3166 de duas letras;
 * entidades que não são países começam com "*" (ONU, ICAO, OMM, Ordem de Malta).
 * Blocos de uma letra (B, F, G, I, K, M, N, R, W) valem para qualquer segundo caractere.
 * Para atualizar: editar a lista abaixo e o teste CallsignPrefixesTest.
 *
 * Prefixo válido NÃO prova que o indicativo foi emitido; só que é plausível (backlog #59, estado "b").
 */
private const val ITU_BLOCKS = """
A2=BW A3=TO A4=OM A5=BT A6=AE A7=QA A8=LR A9=BH AA-AL=US AM-AO=ES AP-AS=PK AT-AW=IN AX=AU AY-AZ=AR
B=CN BM-BQ=TW BU-BX=TW
C2=NR C3=AD C4=CY C5=GM C6=BS C7=*WMO C8-C9=MZ CA-CE=CL CF-CK=CA CL-CM=CU CN=MA CO=CU CP=BO CQ-CU=PT
CV-CX=UY CY-CZ=CA
D2-D3=AO D4=CV D5=LR D6=KM D7-D9=KR DA-DR=DE DS-DT=KR DU-DZ=PH
E2=TH E3=ER E4=PS E5=CK E6=NU E7=BA EA-EH=ES EI-EJ=IE EK=AM EL=LR EM-EO=UA EP-EQ=IR ER=MD ES=EE ET=ET
EU-EW=BY EX=KG EY=TJ EZ=TM
F=FR G=GB
H2=CY H3=PA H4=SB H6-H7=NI H8-H9=PA HA=HU HB=CH HC-HD=EC HE=CH HF=PL HG=HU HH=HT HI=DO HJ-HK=CO HL=KR
HM=KP HN=IQ HO-HP=PA HQ-HR=HN HS=TH HT=NI HU=SV HV=VA HW-HY=FR HZ=SA
I=IT
J2=DJ J3=GD J4=GR J5=GW J6=LC J7=DM J8=VC JA-JS=JP JT-JV=MN JW-JX=NO JY=JO JZ=ID
K=US
L2-L9=AR LA-LN=NO LO-LW=AR LX=LU LY=LT LZ=BG
M=GB N=US
OA-OC=PE OD=LB OE=AT OF-OJ=FI OK-OL=CZ OM=SK ON-OT=BE OU-OZ=DK
P2=PG P3=CY P4=AW P5-P9=KP PA-PI=NL PJ=CW PK-PO=ID PP-PY=BR PZ=SR
R=RU
S2-S3=BD S5=SI S6=SG S7=SC S8=ZA S9=ST SA-SM=SE SN-SR=PL SSA-SSM=EG SSN-SSZ=SD SS=EG ST=SD SU=EG SV-SZ=GR
T2=TV T3=KI T4=CU T5=SO T6=AF T7=SM T8=PW T9=BA TA-TC=TR TD=GT TE=CR TF=IS TG=GT TH=FR TI=CR TJ=CM TK=FR
TL=CF TM=FR TN=CG TO-TQ=FR TR=GA TS=TN TT=TD TU=CI TV-TX=FR TY=BJ TZ=ML
UA-UI=RU UJ-UM=UZ UN-UQ=KZ UR-UZ=UA
V2=AG V3=BZ V4=KN V5=NA V6=FM V7=MH V8=BN VA-VG=CA VH-VN=AU VO=CA VP-VQ=GB VR=HK VS=GB VT-VW=IN VX-VY=CA
VZ=AU
W=US
XA-XI=MX XJ-XO=CA XP=GL XQ-XR=CL XS=CN XT=BF XU=KH XV=VN XW=LA XX=MO XY-XZ=MM
Y2-Y9=DE YA=AF YB-YH=ID YI=IQ YJ=VU YK=SY YL=LV YM=TR YN=NI YO-YR=RO YS=SV YT-YU=RS YV-YY=VE YZ=RS
Z2=ZW Z3=MK Z6=XK Z8=SS ZA=AL ZB-ZJ=GB ZK-ZM=NZ ZN-ZO=GB ZP=PY ZQ=GB ZR-ZU=ZA ZV-ZZ=BR
2A-2Z=GB
3A=MC 3B=MU 3C=GQ 3DA-3DM=SZ 3DN-3DZ=FJ 3D=FJ 3E-3F=PA 3G=CL 3H-3U=CN 3V=TN 3W=VN 3X=GN 3Y=NO 3Z=PL
4A-4C=MX 4D-4I=PH 4J-4K=AZ 4L=GE 4M=VE 4N=RS 4O=ME 4P-4S=LK 4T=PE 4U=*UN 4V=HT 4W=TL 4X=IL 4Y=*ICAO 4Z=IL
5A=LY 5B=CY 5C-5G=MA 5H-5I=TZ 5J-5K=CO 5L-5M=LR 5N-5O=NG 5P-5Q=DK 5R-5S=MG 5T=MR 5U=NE 5V=TG 5W=WS 5X=UG
5Y-5Z=KE
6A-6B=EG 6C=SY 6D-6J=MX 6K-6N=KR 6O=SO 6P-6S=PK 6T-6U=SD 6V-6W=SN 6X=MG 6Y=JM 6Z=LR
7A-7I=ID 7J-7N=JP 7O=YE 7P=LS 7Q=MW 7R=DZ 7S=SE 7T-7Y=DZ 7Z=SA
8A-8I=ID 8J-8N=JP 8O=BW 8P=BB 8Q=MV 8R=GY 8S=SE 8T-8Y=IN 8Z=SA
9A=HR 9B-9D=IR 9E-9F=ET 9G=GH 9H=MT 9I-9J=ZM 9K=KW 9L=SL 9M=MY 9N=NP 9O-9T=CD 9U=BI 9V=SG 9W=MY 9X=RW
9Y-9Z=TT
1A=*SMOM S0=EH
"""

private class PrefixBlock(val from: String, val to: String, val country: String)

private val BLOCKS: Map<Int, List<PrefixBlock>> = ITU_BLOCKS.trim().split(Regex("\\s+")).map { entry ->
    val (range, country) = entry.split('=')
    val from = range.substringBefore('-')
    val to = range.substringAfter('-', from)
    PrefixBlock(from, to, country)
}.groupBy { it.from.length }

private val SINGLE_LETTER = BLOCKS[1].orEmpty().associate { it.from[0] to it.country }

private fun lookup(length: Int, key: String): String? =
    BLOCKS[length].orEmpty().firstOrNull { key >= it.from && key <= it.to }?.country

/** País (ISO) ou entidade (*UN…) do prefixo; null se o prefixo não está alocado. */
fun prefixCountry(prefix: String): String? {
    val p = prefix.uppercase(Locale.ROOT)
    if (p.isEmpty()) return null
    if (p.length == 1) return SINGLE_LETTER[p[0]]
    if (p.length >= 3) lookup(3, p.take(3))?.let { return it }
    return lookup(2, p.take(2)) ?: SINGLE_LETTER[p[0]]
}

enum class CallsignProblem { EMPTY, FORMAT, SSID, PREFIX }

data class CallsignCheck(
    val ok: Boolean,
    val problem: CallsignProblem?,
    val base: String,
    val ssid: Int?,
    /** Prefixo identificado (antes do algarismo separador). */
    val prefix: String?,
    /** ISO 3166 do país, ou entidade especial começando com "*"; null se desconhecido. */
    val country: String?,
)

private val BASE_RE = Regex("^[A-Z0-9]{3,6}$")
private val SUFFIX_RE = Regex("^[A-Z0-9]{0,3}[A-Z]$")

/**
 * Indicativo de radioamador plausível no mundo (#59): prefixo alocado pela ITU + algarismo + sufixo de
 * 1 a 4 caracteres terminando em letra; SSID opcional -0 a -15, validado à parte.
 * Não se aplica a destinos de mensagem (CQ, BLN1, objetos), que continuam livres.
 */
fun checkAmateurCallsign(value: String): CallsignCheck {
    val call = value.trim().uppercase(Locale.ROOT)
    if (call.isEmpty()) return CallsignCheck(false, CallsignProblem.EMPTY, "", null, null, null)
    val base = call.substringBefore('-')
    val ssidText = call.substringAfter('-', "")
    val ssid = if (call.contains('-')) ssidText.toIntOrNull() else null
    if (call.contains('-') && (ssid == null || ssid !in 0..15 || ssidText.length > 2))
        return CallsignCheck(false, CallsignProblem.SSID, base, null, null, null)
    if (!BASE_RE.matches(base)) return CallsignCheck(false, CallsignProblem.FORMAT, base, ssid, null, null)
    var unknownPrefix: String? = null
    for (i in 1..minOf(3, base.length - 2)) {
        if (!base[i].isDigit()) continue
        val prefix = base.substring(0, i)
        val suffix = base.substring(i + 1)
        if (!SUFFIX_RE.matches(suffix) || prefix.none { it.isLetter() } && prefix.length < 2) continue
        val country = prefixCountry(prefix)
        if (country != null) return CallsignCheck(true, null, base, ssid, prefix, country)
        if (unknownPrefix == null) unknownPrefix = prefix
    }
    return if (unknownPrefix != null)
        CallsignCheck(false, CallsignProblem.PREFIX, base, ssid, unknownPrefix.take(2), null)
    else CallsignCheck(false, CallsignProblem.FORMAT, base, ssid, null, null)
}

/** País pelo prefixo de qualquer indicativo recebido (bandeira na ficha e filtro por região). */
fun callsignCountry(call: String): String? = checkAmateurCallsign(call).takeIf { it.ok }?.country

/** Bandeira em emoji (indicadores regionais) para um código ISO; vazio para entidades e desconhecidos. */
fun flagEmoji(iso: String?): String {
    if (iso == null || iso.length != 2 || !iso.all { it in 'A'..'Z' }) return ""
    return String(Character.toChars(0x1F1E6 + (iso[0] - 'A'))) + String(Character.toChars(0x1F1E6 + (iso[1] - 'A')))
}

/** Nome do país no idioma pedido; entidades especiais em inglês (nomes oficiais). */
fun countryName(iso: String?, locale: Locale): String? = when (iso) {
    null -> null
    "*UN" -> "United Nations"
    "*ICAO" -> "ICAO"
    "*WMO" -> "WMO"
    "*SMOM" -> "Sovereign Military Order of Malta"
    else -> Locale("", iso).getDisplayCountry(locale).ifBlank { iso }
}
