package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.BioKineticsEngine
import com.example.data.model.LiveHalfInput
import com.example.data.model.PreMatchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BioMatch xG", appName)
  }

  @Test
  fun `test pre match mathematical modeling`() {
    val input = PreMatchInput(
      homeBaseXg = 1.8,
      awayBaseXg = 1.2,
      tsszHome = 1.1,
      tsszAway = 1.0,
      dynamicBias = 0.0
    )
    val math = BioKineticsEngine.computePreMatchMath(input)
    // 1.8 * 1.1 + 1.2 * 1.0 = 1.98 + 1.2 = 3.18
    assertEquals(3.18, math.totalXg, 0.01)
    assertTrue(math.over25Prob > 0.5)
    assertEquals(6, math.topScores.size)
  }

  @Test
  fun `test live 1h sterility detection and formula`() {
    val input = LiveHalfInput(
      dangerousAttacksHome = 5,
      dangerousAttacksAway = 4, // total = 9, 9 / 15 = 0.60 < 0.70 -> sterile!
      xg0To15 = 0.05,
      xg16To45Base = 0.50,
      kszMultiplier = 0.60
    )
    val math = BioKineticsEngine.computeLiveHalfMath(input)
    assertEquals(0.60, math.dangerousAttackRatePerMin, 0.01)
    assertTrue(math.isSterileState)
    // 0.05 + 0.50 * 0.60 = 0.35 xG
    assertEquals(0.35, math.calculated1hXg, 0.01)
    assertEquals("ERŐSEN UNDER-PROFILÚ", math.dominantMarketDirection)
    assertTrue(math.under05Prob > 0.60)
  }

  @Test
  fun `test extracted match data model integrity`() {
    val extracted = com.example.data.model.ExtractedMatchData(
      homeTeam = "Arsenal",
      awayTeam = "Chelsea",
      score = "1-0",
      minute = 15,
      dangerousAttacksHome = 9,
      dangerousAttacksAway = 4
    )
    assertEquals("Arsenal", extracted.homeTeam)
    assertEquals("Chelsea", extracted.awayTeam)
    assertEquals(9, extracted.dangerousAttacksHome)
  }

  @Test
  fun `test local tip file export and conclusion storage`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val entity = com.example.data.db.SavedAnalysisEntity(
      id = 101L,
      type = "PRE_MATCH",
      homeTeam = "Barcelona",
      awayTeam = "Napoli",
      calculatedXg = 2.45,
      dominantDirectionOrInterval = "2-3 GÓL",
      summary = "Kötési affinitás 8.5/10",
      fullReport = "1. Fázis jelentés...",
      blackSwan = "Alacsony kockázat",
      actualScore = "2-1",
      tipStatus = "WON",
      conclusion = "A predikció helytállónak bizonyult.",
      learnedInsight = "TSSz szorzók pontosan kalibráltak.",
      isResolved = true
    )

    val file = com.example.data.storage.LocalTipStorageManager.saveTipToLocalFolder(context, entity)
    assertTrue(file.exists())
    val content = file.readText()
    assertTrue(content.contains("Barcelona vs. Napoli"))
    assertTrue(content.contains("Végeredmény: 2-1"))
    assertTrue(content.contains("WON"))
  }
}

