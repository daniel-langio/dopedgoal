package app.dopedgoal.core

import java.security.MessageDigest

/**
 * The deterministic generator behind every minted brick and banner.
 *
 * **This is a frozen contract.** A brick is minted once and must look the same
 * forever, and the wall renderer re-derives its texture — crack paths, speckle
 * positions, splatter — from a seed on every single draw rather than from
 * storage. Change the algorithm below, or the *order* in which a caller draws
 * from it, and every wall a user has already built quietly changes.
 *
 * The reference generators in `bricks.py` / `banners.py` use CPython's
 * Mersenne Twister, which nothing on the JVM reproduces. Those files are visual
 * reference for *how* a brick is drawn, not a source of *which* brick is drawn;
 * this implementation is authoritative. See
 * `docs/development/2026-09-15-porting-python-rng-determinism.md`.
 *
 * The algorithm is SplitMix64 (Steele, Lea & Flood 2014) — small enough to
 * verify by eye, well distributed, and stable for every seed including zero.
 */
class Rng(seed: Long) {

    private var state: Long = seed

    /** Raw 64 bits. Every other method is defined in terms of this one. */
    fun nextLong(): Long {
        state += GOLDEN_GAMMA
        var z = state
        z = (z xor (z ushr 30)) * MIX_A
        z = (z xor (z ushr 27)) * MIX_B
        return z xor (z ushr 31)
    }

    /** A double in `[0, 1)`, using the top 53 bits. */
    fun nextDouble(): Double = (nextLong() ushr 11) * (1.0 / (1L shl 53))

    fun nextFloat(): Float = nextDouble().toFloat()

    /**
     * A uniform `Int` in `[0, bound)`, rejection-sampled so the result is
     * unbiased even when [bound] does not divide the generator's range.
     */
    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound $bound must be positive" }
        while (true) {
            val bits = (nextLong() ushr 33).toInt()
            val value = bits % bound
            // Retry only on the short final stride, which would otherwise be
            // over-represented.
            if (bits - value + (bound - 1) >= 0) return value
        }
    }

    /** A uniform `Float` in `[min, max)`. */
    fun uniform(min: Float, max: Float): Float = min + (max - min) * nextFloat()

    /** Inclusive on both ends, matching the reference generator's `randint`. */
    fun intBetween(min: Int, max: Int): Int {
        require(max >= min) { "empty range $min..$max" }
        return min + nextInt(max - min + 1)
    }

    fun <T> choice(options: List<T>): T {
        require(options.isNotEmpty()) { "cannot choose from nothing" }
        return options[nextInt(options.size)]
    }

    fun chance(probability: Float): Boolean = nextFloat() < probability

    /**
     * A generator for one layer of a brick's appearance. Each visual layer draws
     * from its own salted stream, so a new layer can be added under a new salt
     * without shifting the values every existing layer sees.
     */
    fun salted(salt: String): Rng = Rng(state xor seedOf(salt))

    private companion object {
        val GOLDEN_GAMMA: Long = 0x9E3779B97F4A7C15uL.toLong()
        val MIX_A: Long = 0xBF58476D1CE4E5B9uL.toLong()
        val MIX_B: Long = 0x94D049BB133111EBuL.toLong()
    }
}

/**
 * Turns an identifier into a seed: the first eight bytes of its SHA-256, big
 * endian. Stable across processes and platforms, unlike [String.hashCode].
 */
fun seedOf(text: String): Long {
    val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
    var seed = 0L
    for (i in 0 until 8) {
        seed = (seed shl 8) or (digest[i].toLong() and 0xFF)
    }
    return seed
}

/** Interpolates from [a] to [b] by [t]. */
internal fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** Interpolates around the colour wheel, taking whichever way round is shorter. */
internal fun lerpHue(a: Float, b: Float, t: Float): Float {
    val delta = ((b - a + 180f).mod(360f)) - 180f
    return (a + delta * t).mod(360f)
}
