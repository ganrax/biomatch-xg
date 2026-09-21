package com.example.util

import com.example.data.model.LiveHalfAnalysis
import com.example.data.model.LiveHalfInput
import com.example.data.model.PreMatchAnalysis
import com.example.data.model.PreMatchInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AnalysisDossierBuilder {

    /**
     * Builds the complete end-to-end audit trail and analytical dossier for Pre-Match analysis,
     * from Step 0 (input data, screenshot extraction, context) through the mathematical and
     * biological reasoning to the final actionable conclusion.
     */
    fun buildFullPreMatchDossier(
        input: PreMatchInput,
        analysis: PreMatchAnalysis
    ): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        val isUnder = analysis.calculatedXg <= 2.45 || analysis.under25Prob >= 0.52
        val mainTip = if (analysis.concreteBetTip.isNotBlank()) analysis.concreteBetTip else if (isUnder) "Under 2.5 gól" else "Over 2.5 gól"

        return buildString {
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine("📋 BIOMATCH xG - TELJES ELEMZÉSI ÉS DÖNTÉSHOZATALI DOSSZIÉ")
            appendLine("Mérkőzés: ${input.homeTeam} vs ${input.awayTeam}")
            appendLine("Generálás ideje: ${dateFormat.format(Date(analysis.timestamp))}")
            appendLine("Modell: Bio-Kinetikai Transzlációs Motor & Komplex Dinamikai Rendszerek")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine()

            // 0. LÉPÉS: BEMENETI ADATOK ÉS FORRÁS
            appendLine("▶ 0. LÉPÉS: BEMENETI FORRÁSOK ÉS NYERS ADATKINYERÉS (INPUT & OCR)")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Érintett csapatok: ${input.homeTeam} (Hazai) vs ${input.awayTeam} (Vendég)")
            appendLine("• Rögzített meccskontextus / Képi információk:")
            appendLine("  ${if (input.context.isNotBlank()) input.context else "Általános bajnoki felállás, standard időjárási és taktikai körülmények"}")
            appendLine("• Forrás & feldolgozás típusa: Képernyőkép OCR / Statisztikai adatbeillesztés / Szenzoros bemenet")
            appendLine("• Nyers kiinduló paraméterek:")
            appendLine("  - Hazai bázis xG elvárás: ${String.format(Locale.US, "%.2f", input.homeBaseXg)}")
            appendLine("  - Vendég bázis xG elvárás: ${String.format(Locale.US, "%.2f", input.awayBaseXg)}")
            appendLine("  - Hazai Taktikai Súlyozási Szorzó (TSSZ): ${String.format(Locale.US, "%.2f", input.tsszHome)}")
            appendLine("  - Vendég Taktikai Súlyozási Szorzó (TSSZ): ${String.format(Locale.US, "%.2f", input.tsszAway)}")
            appendLine("  - Dinamikus kontextuális torzítás (Dynamic Bias): ${if (input.dynamicBias >= 0) "+${String.format(Locale.US, "%.2f", input.dynamicBias)}" else String.format(Locale.US, "%.2f", input.dynamicBias)}")
            appendLine()

            // 1. LÉPÉS: ADATFELDOLGOZÁS ÉS BIO-FIZIKAI MODELLEZÉS
            appendLine("▶ 1. LÉPÉS: BIO-FIZIKAI ADATFELDOLGOZÁS ÉS PARAMÉTER-KALIBRÁCIÓ")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Kötési Affinitási Index (Binding Affinity Index - BAI): ${analysis.bindingAffinityIndex}/10")
            appendLine("  - Taktikai geometria és receptor-illeszkedés:")
            if (analysis.bindingAffinityIndex <= 3) {
                appendLine("    [Alacsony affinitás (1-3)]: A két csapat játékrendszere kioltja egymást. A mély védelmi blokkok és a ritmus-fojtás miatt a térbeli illeszkedés minimális, erősen Under-irányú közeg.")
            } else if (analysis.bindingAffinityIndex in 4..6) {
                appendLine("    [Közepes affinitás (4-6)]: Kiegyensúlyozott taktikai kölcsönhatás, mérsékelt transzíciós zónák és kontrollált gólkockázat.")
            } else {
                appendLine("    [Magas affinitás (7-10)]: Magas kinetikai tempó, kölcsönös letámadási rések, reaktív és tranzíciós gólveszély.")
            }
            appendLine("• Fáradtsági & Időjárási súrlódási korrekció:")
            appendLine("  - Talajállapot és fizikai súrlódási tényezők integrálása a passzsebességbe.")
            appendLine("  - A 60. perc után fellépő metabolikus glikogén-degradáció és taktikai entrópiás szétesés súlyozása.")
            appendLine("• Integrált prediktált xG: ${String.format(Locale.US, "%.2f", analysis.calculatedXg)} gól")
            appendLine("• Legvalószínűbb végső góltartomány: ${analysis.mostLikelyInterval}")
            appendLine()

            // 2. LÉPÉS: A DÖNTÉSEK MIÉRTJE - A 4 FÁZISÚ TUDOMÁNYOS LEVEZETÉS
            appendLine("▶ 2. LÉPÉS: A DÖNTÉSEK MIÉRTJE - A 4 FÁZISÚ KINETIKAI LEVEZETÉS")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("1️⃣ 1. FÁZIS - MOLEKULÁRIS DOKKOLÁS (Strukturális illeszkedés & taktikai tér):")
            appendLine(analysis.phase1MolecularDocking.trim())
            appendLine()
            appendLine("2️⃣ 2. FÁZIS - METABOLIKUS KINETIKA (Állóképesség, ritmus & 60. perces entrópiás törés):")
            appendLine(analysis.phase2MetabolicKinetics.trim())
            appendLine()
            appendLine("3️⃣ 3. FÁZIS - NEMLINEÁRIS GÓLKÉPLET (Súlyozott regresszió és együtthatók):")
            appendLine(analysis.phase3FormulaExplanation.trim())
            appendLine()
            appendLine("4️⃣ 4. FÁZIS - MONTE-CARLO SZIMULÁCIÓ & VALÓSZÍNŰSÉGI MÁTRIX (10 000 futtatás):")
            appendLine(analysis.phase4MonteCarloText.trim())
            appendLine("  - Under 2.5 gól valószínűség: ${(analysis.under25Prob * 100).toInt()}%")
            appendLine("  - Over 2.5 gól valószínűség: ${(analysis.over25Prob * 100).toInt()}%")
            if (analysis.scoreProbabilities.isNotEmpty()) {
                val topScoreSummary = analysis.scoreProbabilities.take(4).joinToString(", ") { "${it.scoreText} (${it.formattedPercent})" }
                appendLine("  - Legvalószínűbb pontos végeredmények: $topScoreSummary")
            }
            appendLine()

            // 3. LÉPÉS: FEKETE HATTYÚ (BLACK SWAN) ANOMÁLIA-VIZSGÁLAT
            appendLine("▶ 3. LÉPÉS: FEKETE HATTYÚ (BLACK SWAN) ANOMÁLIA-VIZSGÁLAT")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Detektált gátlástörő kockázati faktor:")
            appendLine("  ${if (analysis.blackSwanFactor.isNotBlank()) analysis.blackSwanFactor.trim() else "Nincs kiugró aszimmetrikus anomália, standard variancia érvényesül."}")
            appendLine("• Kockázatkezelési hatás: A modell vizsgálta a korai kiállítás, kirívó kapushiba vagy taktikai fegyelmezetlenség lehetőségét, és ennek figyelembevételével határozta meg a biztonsági védőhálót.")
            appendLine()

            // 4. LÉPÉS: PIACI SZELEKCIÓ ÉS VALUE-LOGIKA
            appendLine("▶ 4. LÉPÉS: PIACI SZELEKCIÓ ÉS A FOGADÁSI DÖNTÉS LOGIKÁJA")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Miért pont a(z) \"$mainTip\" lett a kiválasztott fő fogadási opció?")
            if (isUnder) {
                appendLine("  - A bio-fizikai súrlódás és a fojtott bázis xG (${String.format(Locale.US, "%.2f", analysis.calculatedXg)}) miatt az Under opció rendelkezik matematikai értékkel (Value).")
                appendLine("  - Az Over 2.5 piac elutasításának oka: a receptoriális blokkolás és a játékvezetési/időjárási feltételek miatt az Over varianciája túlságosan kockázatos.")
            } else {
                appendLine("  - A magas reakciósebesség és a nyílt tranzíciós zónák miatt az Over opció rendelkezik kiemelkedő várható értékkel (EV).")
                appendLine("  - Az Under piac elutasításának oka: mindkét csapat védelmi entrópiája magas, a gólképződés gátlása nem fenntartható.")
            }
            if (analysis.secondaryBetTip.isNotBlank()) {
                appendLine("• Másodlagos / Biztonsági piac logikája:")
                appendLine("  - Ajánlott védőháló: ${analysis.secondaryBetTip}")
                appendLine("  - Célja a variancia tompítása és alacsonyabb kockázatú kombinációk biztosítása.")
            }
            appendLine()

            // 5. LÉPÉS: VÉGSŐ KONKRÉT KÖVETKEZTETÉS ÉS VÉGREHAJTÁS
            appendLine("▶ 5. LÉPÉS: VÉGSŐ KONKRÉT KÖVETKEZTETÉS ÉS VÉGREHAJTÁSI AJÁNLÁS")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine("🎯 FŐ FOGADÁSI TIPP: $mainTip")
            if (analysis.secondaryBetTip.isNotBlank()) {
                appendLine("🛡️ BIZTONSÁGI PIAC: ${analysis.secondaryBetTip}")
            }
            appendLine("📊 VÁRHATÓ xG: ${String.format(Locale.US, "%.2f", analysis.calculatedXg)} | GÓLTARTOMÁNY: ${analysis.mostLikelyInterval}")
            appendLine("🧬 KÖTÉSI AFFINITÁS (BAI): ${analysis.bindingAffinityIndex}/10")
            appendLine("📈 VALÓSZÍNŰSÉG: Under 2.5: ${(analysis.under25Prob * 100).toInt()}% | Over 2.5: ${(analysis.over25Prob * 100).toInt()}%")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
        }
    }

    /**
     * Builds the complete end-to-end audit trail and analytical dossier for Live 1st Half analysis,
     * from Step 0 (0-15 minute live match stats, screenshot extraction, match situation)
     * through the sterility testing, kinetic flux, and mathematical formula to the final actionable conclusion.
     */
    fun buildFullLiveHalfDossier(
        input: LiveHalfInput,
        analysis: LiveHalfAnalysis
    ): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        val mainTip = if (analysis.concreteBetTip.isNotBlank()) analysis.concreteBetTip else analysis.mostValuableMarket

        return buildString {
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine("⚡ BIOMATCH xG - TELJES ÉLŐ DÖNTÉSHOZATALI DOSSZIÉ (1. FÉLIDŐ)")
            appendLine("Mérkőzés: ${input.homeTeam} vs ${input.awayTeam} (15' Állás: ${input.currentScore})")
            appendLine("Generálás ideje: ${dateFormat.format(Date(analysis.timestamp))}")
            appendLine("Modell: Reakciósebesség (v0) & Kinetikai Fluxus Élő Prediktor")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine()

            // 0. LÉPÉS: BEMENETI FORRÁSOK ÉS VALÓS ÉLŐ STATISZTIKÁK
            appendLine("▶ 0. LÉPÉS: BEMENETI FORRÁSOK ÉS RÖGZÍTETT ÉLŐ STATISZTIKÁK (0–15. PERC)")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Mérkőzés: ${input.homeTeam} vs ${input.awayTeam}")
            appendLine("• 15. perces pillanatnyi állás: ${input.currentScore}")
            appendLine("• Forrás & feldolgozás: Élő közvetítés képernyőképe / Élő statisztikai adatok (OCR)")
            appendLine("• Rögzített 15. perces élő mérkőzésadatok:")
            appendLine("  - Kapura lövések: Hazai ${input.shotsHome} (${input.shotsHomeOnTarget} kaput talált) | Vendég ${input.shotsAway} (${input.shotsAwayOnTarget} kaput talált)")
            appendLine("  - Veszélyes támadások: Hazai ${input.dangerousAttacksHome} | Vendég ${input.dangerousAttacksAway} (Összesen: ${input.dangerousAttacksHome + input.dangerousAttacksAway})")
            appendLine("  - Szögletek: Hazai ${input.cornersHome} | Vendég ${input.cornersAway}")
            appendLine("  - Labdabirtoklás: ${input.possessionHome}% - ${input.possessionAway}%")
            appendLine("  - Szabálytalanságok & holtidő: ${input.foulsAndStoppages}")
            appendLine("  - Pályán tapasztalt taktikai benyomás:")
            appendLine("    ${input.tacticalImpression}")
            appendLine("  - 0–15. perc mért xG: ${String.format(Locale.US, "%.2f", input.xg0To15)} | 16–45. bázis xG: ${String.format(Locale.US, "%.2f", input.xg16To45Base)}")
            appendLine("  - Kinetikai Súlyozási Szorzó (KSz): ${String.format(Locale.US, "%.2f", input.kszMultiplier)}")
            appendLine()

            // 1. LÉPÉS: KINETIKAI ADATFELDOLGOZÁS ÉS STERILITÁSI TESZT
            appendLine("▶ 1. LÉPÉS: KINETIKAI ADATFELDOLGOZÁS ÉS A STERILITÁSI TESZT ELEMZÉSE")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Veszélyes támadási frekvencia: ${String.format(Locale.US, "%.2f", analysis.dangerousAttackRatePerMin)} akció / perc")
            appendLine("• 15. perces Sterilitási küszöb (<0.7 akció/perc szabály):")
            if (analysis.isSterileState) {
                appendLine("  🛑 ÁLLAPOT: STERIL / BLOKKOLT MEZŐNYJÁTÉK DETEKTÁLVA")
                appendLine("  - Elemzés: A percenkénti veszélyes akciók száma a 0.7-es kritikus szint alatt marad. A csapatok nem jutnak a büntetőterület mögé, a labdajáratás meddő és fojtott, ami drasztikusan lecsökkenti a szünet előtti gólvalószínűséget.")
            } else {
                appendLine("  ⚡ ÁLLAPOT: AKTÍV / KATALIZÁLT JÁTÉK DETEKTÁLVA")
                appendLine("  - Elemzés: A veszélyes akciók frekvenciája meghaladja a 0.7 akció/perc küszöböt, a tranzíciós zónák nyitottak, a gólképződés sebessége felgyorsult.")
            }
            appendLine("• Nemlineáris 1. félidős gólképlet számítása:")
            appendLine("  - 1H_xG = xG(0-15) + (xG(16-45)_bázis * KSz)")
            appendLine("  - Számítás: ${String.format(Locale.US, "%.2f", input.xg0To15)} + (${String.format(Locale.US, "%.2f", input.xg16To45Base)} * ${String.format(Locale.US, "%.2f", input.kszMultiplier)}) = ${String.format(Locale.US, "%.2f", analysis.calculated1hXg)} xG")
            appendLine("• Domináns piaci irány: ${analysis.dominantMarketDirection}")
            appendLine()

            // 2. LÉPÉS: A DÖNTÉSEK MIÉRTJE - A 4 FÁZISÚ ÉLŐ KINETIKAI LEVEZETÉS
            appendLine("▶ 2. LÉPÉS: A DÖNTÉSEK MIÉRTJE - A 4 FÁZISÚ ÉLŐ MODELL LEVEZETÉSE")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("1️⃣ 1. FÁZIS - REAKCIÓSEBESSÉG (v0) VS INHIBÍCIÓ (0–15. perc analízis):")
            appendLine(analysis.phase1ReactionAndInhibition.trim())
            appendLine()
            appendLine("2️⃣ 2. FÁZIS - KINETIKAI FLUXUS & RITMUS-DEGRADÁCIÓ (16–45. perc előrejelzés):")
            appendLine(analysis.phase2KineticFlux.trim())
            appendLine()
            appendLine("3️⃣ 3. FÁZIS - NEMLINEÁRIS ELSŐ FÉLIDŐS GÓLKÉPLET LEVEZETÉSE:")
            appendLine(analysis.phase3FormulaDetails.trim())
            appendLine()
            appendLine("4️⃣ 4. FÁZIS - MONTE-CARLO 1H & PIACI SZELEKCIÓ (Valószínűségi mátrix):")
            appendLine(analysis.phase4MarketText.trim())
            appendLine("  - 1H Under 0.5 valószínűség (0–0 marad a szünetig): ${(analysis.under05Prob * 100).toInt()}%")
            appendLine("  - 1H Over 0.5 valószínűség (legalább 1 gól esik a szünetig): ${(analysis.over05Prob * 100).toInt()}%")
            appendLine("  - 1H Under 1.5 valószínűség (maximum 1 gól lesz a szünetig): ${(analysis.under15Prob * 100).toInt()}%")
            appendLine("  - 1H Over 1.5 valószínűség (legalább 2 gól esik a szünetig): ${(analysis.over15Prob * 100).toInt()}%")
            appendLine("  - Legvalószínűbb szünetbeli eredmény (HT Score): ${analysis.mostLikelyHtScore}")
            appendLine()

            // 3. LÉPÉS: GÁTLÁSTÖRŐ ANOMÁLIA & FEKETE HATTYÚ ELEMZÉS
            appendLine("▶ 3. LÉPÉS: GÁTLÁSTÖRŐ ANOMÁLIA & FEKETE HATTYÚ ELEMZÉS")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Detektált gátlástörő faktor:")
            appendLine("  ${if (analysis.blackSwanFactor.isNotBlank()) analysis.blackSwanFactor.trim() else "Nem található gátlástörő anomália, stabil kinetikai mezőny érvényesül."}")
            appendLine("• Kockázati kihatás: Korai vezetés esetén a game-state gátlás tovább fojthatja a tempót, míg egy korai kiállítás felboríthatja a védelmi blokkolást.")
            appendLine()

            // 4. LÉPÉS: PIACI SZELEKCIÓ ÉS A FOGADÁSI DÖNTÉS LOGIKÁJA
            appendLine("▶ 4. LÉPÉS: PIACI SZELEKCIÓ ÉS A FOGADÁSI DÖNTÉS LOGIKÁJA")
            appendLine("───────────────────────────────────────────────────────────────────────────")
            appendLine("• Miért pont a(z) \"$mainTip\" a kiválasztott elsődleges fogadási ajánlás?")
            if (analysis.calculated1hXg < 0.65 || analysis.under05Prob >= 0.50) {
                appendLine("  - A 15. percig mért alacsony reakciósebesség és a sterilitási teszt negatív lefolyása miatt a piac lassan árazza be a gólképtelenséget, így az Under piac kiemelkedő matematikai értékkel (Value) bír.")
                appendLine("  - Az Over opció kizárásának oka: a befejezések minősége és a veszélyes akciók frekvenciája nem alapoz meg hirtelen gólzáport.")
            } else {
                appendLine("  - A katalizált játék és a magas akciófrekvencia gyors gólképződést jelez előre, a szünet előtti gól valószínűsége magas.")
            }
            if (analysis.secondaryBetTip.isNotBlank()) {
                appendLine("• Másodlagos / Biztonsági piac logikája:")
                appendLine("  - Védelmi opció: ${analysis.secondaryBetTip}")
                appendLine("  - Fedezetet nyújt egy esetleges késői 1H anomália (pl. vitatott tizenegyes vagy öngól) ellen.")
            }
            appendLine()

            // 5. LÉPÉS: VÉGSŐ KÖVETKEZTETÉS ÉS KONKRÉT ÉLŐ AJÁNLÁS
            appendLine("▶ 5. LÉPÉS: VÉGSŐ KÖVETKEZTETÉS ÉS KONKRÉT ÉLŐ AJÁNLÁS")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
            appendLine("🎯 ÉLŐ FŐ FOGADÁSI TIPP: $mainTip")
            if (analysis.secondaryBetTip.isNotBlank()) {
                appendLine("🛡️ BIZTONSÁGI PIAC: ${analysis.secondaryBetTip}")
            }
            appendLine("⏱️ 15' ÁLLÁS: ${input.currentScore} | VÁRHATÓ SZÜNETI ÁLLÁS: ${analysis.mostLikelyHtScore}")
            appendLine("📊 1H SZÁMÍTOTT xG: ${String.format(Locale.US, "%.2f", analysis.calculated1hXg)} | IRÁNY: ${analysis.dominantMarketDirection}")
            appendLine("🛑 STERILITÁS: ${if (analysis.isSterileState) "STERIL / BLOKKOLT MEZŐNYJÁTÉK (<0.7 akció/perc)" else "AKTÍV / KATALIZÁLT JÁTÉK"}")
            appendLine("📈 1H UNDER 0.5: ${(analysis.under05Prob * 100).toInt()}% | 1H UNDER 1.5: ${(analysis.under15Prob * 100).toInt()}%")
            appendLine("═══════════════════════════════════════════════════════════════════════════")
        }
    }
}
