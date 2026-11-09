package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageLogicTest {
    private val now = 1_800_000_000_000L
    private fun msg(id: Long, from: String, to: String, out: Boolean, ageDays: Int, status: String = "Recebida",
                    kind: AprsMessage.Kind = AprsMessage.Kind.MESSAGE, medium: Medium? = Medium.INTERNET) =
        ChatMessage(id, from, to, "t$id", now - ageDays * 86_400_000L, out, if (out) null else medium,
            status = status, kind = kind)

    @Test fun searchFindsPartialCallsignsWithExactSsid() {
        val stations = listOf(
            Station("PT2VHF-9", 0.0, 0.0, '/', '>', "", now, Medium.RF, "", null, null),
            Station("PT2ABC", 0.0, 0.0, '/', '-', "", now - 1000, Medium.INTERNET, "", null, null),
        )
        val messages = listOf(msg(1, "PY2XYZ-7", "ME", false, 0))
        val hits = searchCallsigns("pt2", stations, messages, "ME")
        assertEquals(listOf("PT2VHF-9", "PT2ABC"), hits.map { it.call })
        assertEquals(listOf("PT2VHF-9"), searchCallsigns("-9", stations, messages, "ME").map { it.call })
        val history = searchCallsigns("xyz", stations, messages, "ME").single()
        assertEquals("PY2XYZ-7", history.call)
        assertTrue(history.inHistory)
        assertTrue(searchCallsigns("", stations, messages, "ME").isEmpty())
        assertTrue(searchCallsigns("ME", stations, messages, "ME").isEmpty())
    }

    @Test fun filtersCombine() {
        val list = listOf(
            msg(1, "A", "ME", false, 0, medium = Medium.RF),
            msg(2, "ME", "A", true, 0, status = "Aguardando ACK · RF"),
            msg(3, "B", "ME", false, 10),
        )
        assertEquals(listOf(1L), list.filter { matchesMessage(it, MessageCriteria(direction = "in", medium = "rf"), now) }.map { it.id })
        assertEquals(listOf(2L), list.filter { matchesMessage(it, MessageCriteria(status = "pending"), now) }.map { it.id })
        assertEquals(listOf(1L, 2L), list.filter { matchesMessage(it, MessageCriteria(period = "7d"), now) }.map { it.id })
        val convs = conversations(list, "ME", MessageCriteria(), newestFirst = true, now = now)
        assertEquals(listOf("A", "B"), convs.map { it.first })
    }

    @Test fun deletionsUseStableIdsAndPreserveMessagesArrivingDuringDelete() {
        val existing = listOf(
            msg(101, "PT2ABC", "PT2VHF", false, 0),
            msg(102, "PT2ABC", "CQ", false, 0),
            msg(103, "PT2ABC", "BLN1", false, 0, kind = AprsMessage.Kind.BULLETIN),
            msg(104, "PT2VHF", "PT2ABC", true, 0),
        )
        assertEquals(setOf(101L, 102L, 103L, 104L), deletionVictimIds(existing, null))
        assertEquals(setOf(101L, 104L), deletionVictimIds(existing, MessageCategory.PRIVATE))
        assertEquals(setOf(102L, 103L), deletionVictimIds(existing, MessageCategory.BULLETIN))
        val victims = deletionVictimIds(existing, null)
        val incomingAfterConfirmation = msg(105, "PT2XYZ", "PT2VHF", false, 0)
        val afterCommit = (existing + incomingAfterConfirmation).filterNot { it.id in victims }
        assertEquals(listOf(105L), afterCommit.map { it.id })
        assertTrue(deletionVictimIds(emptyList(), null).isEmpty())
        assertEquals(4, existing.size) // failed I/O leaves the original state intact
    }

    @Test fun retentionRespectsAgeCountAndPending() {
        val list = listOf(
            msg(1, "A", "ME", false, 40),
            msg(2, "ME", "A", true, 40, status = "Aguardando ACK · APRS-IS"),
            msg(3, "B", "CQ", false, 40),
            msg(4, "A", "ME", false, 1),
            msg(5, "A", "ME", false, 0),
        )
        val policy = RetentionPolicy(private = RetentionRule(days = 30, max = 0), general = RetentionRule(0, 0))
        assertEquals(setOf(1L), retentionVictims(list, policy, now))
        val byCount = RetentionPolicy(private = RetentionRule(days = 0, max = 2))
        assertEquals(setOf(1L, 4L), retentionVictims(list, byCount, now))
        assertTrue(retentionVictims(list, RetentionPolicy(RetentionRule(0, 0), RetentionRule(0, 0), RetentionRule(0, 0)), now).isEmpty())
    }
}
