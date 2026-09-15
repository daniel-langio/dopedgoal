package app.dopedgoal.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RngTest {

    /**
     * SplitMix64's published output for seed 0. If this fails, the generator
     * itself has changed and every wall ever built has changed with it.
     */
    @Test
    fun `matches the reference SplitMix64 vectors`() {
        val rng = Rng(0L)
        assertEquals(0xE220A8397B1DCDAFuL.toLong(), rng.nextLong())
        assertEquals(0x6E789E6AA1B965F4uL.toLong(), rng.nextLong())
        assertEquals(0x06C45D188009454FuL.toLong(), rng.nextLong())
    }

    @Test
    fun `seedOf is stable and not String hashCode`() {
        assertEquals(-3005254734021552841L, seedOf("goal-learn-kotlin"))
        assertEquals(8861574740880364223L, seedOf("task-1"))
        assertEquals(seedOf("task-1"), seedOf("task-1"))
        assertTrue(seedOf("task-1") != seedOf("task-2"))
    }

    @Test
    fun `the same seed replays the same stream`() {
        val a = Rng(42L)
        val b = Rng(42L)
        repeat(100) { assertEquals(a.nextLong(), b.nextLong()) }
    }

    @Test
    fun `salted streams diverge from each other and from the root`() {
        val seed = seedOf("task-1")
        val grain = Rng(seed).salted("grain").nextLong()
        val paint = Rng(seed).salted("paint").nextLong()
        val root = Rng(seed).nextLong()
        assertTrue(grain != paint)
        assertTrue(grain != root)
        assertEquals(grain, Rng(seed).salted("grain").nextLong())
    }

    @Test
    fun `nextDouble stays in range`() {
        val rng = Rng(7L)
        repeat(10_000) {
            val d = rng.nextDouble()
            assertTrue(d >= 0.0 && d < 1.0, "$d is outside [0,1)")
        }
    }

    @Test
    fun `nextInt covers its range without escaping it`() {
        val rng = Rng(9L)
        val seen = IntArray(7)
        repeat(20_000) {
            val v = rng.nextInt(7)
            assertTrue(v in 0..6, "$v is outside [0,7)")
            seen[v]++
        }
        assertTrue(seen.all { it > 0 }, "some values never appeared: ${seen.toList()}")
    }

    @Test
    fun `intBetween is inclusive on both ends`() {
        val rng = Rng(11L)
        var low = false
        var high = false
        repeat(5_000) {
            when (rng.intBetween(1, 3)) {
                1 -> low = true
                3 -> high = true
                2 -> Unit
                else -> throw AssertionError("out of range")
            }
        }
        assertTrue(low && high)
    }

    @Test
    fun `lerpHue takes the short way round the wheel`() {
        assertEquals(355f, lerpHue(350f, 0f, 0.5f), 0.01f)
        assertEquals(5f, lerpHue(0f, 10f, 0.5f), 0.01f)
        assertEquals(350f, lerpHue(340f, 0f, 0.5f), 0.01f)
    }
}
