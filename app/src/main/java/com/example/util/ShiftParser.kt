package com.example.util

import com.example.data.model.RateRule

data class ParsedShiftItem(
    val tag: String,
    val amount: Double,
    val matchedRule: String
)

data class StaffParsedBlock(
    val detectedStaffName: String?,
    val items: List<ParsedShiftItem>
) {
    val totalAmount: Double get() = items.sumOf { it.amount }
    val count: Int get() = items.size
}

object ShiftParser {

    /**
     * Standard esports rate rules if none configured in database:
     * -25, -30 -> ₹30
     * -35, -45, -55 -> ₹40
     */
    val DEFAULT_RULES = listOf(
        RateRule(id = 1, tagPattern = "-25", rateAmount = 30.0, matchType = "SUFFIX", description = "Tier 3 / 25-slot scrim"),
        RateRule(id = 2, tagPattern = "-30", rateAmount = 30.0, matchType = "SUFFIX", description = "Tier 3 / 30-slot scrim"),
        RateRule(id = 3, tagPattern = "-35", rateAmount = 40.0, matchType = "SUFFIX", description = "Tier 2 / 35-slot scrim"),
        RateRule(id = 4, tagPattern = "-45", rateAmount = 40.0, matchType = "SUFFIX", description = "Tier 1 / 45-slot scrim"),
        RateRule(id = 5, tagPattern = "-55", rateAmount = 40.0, matchType = "SUFFIX", description = "Grand Final / 55-slot scrim")
    )

    fun resolveRate(token: String, rules: List<RateRule>): Pair<Double, String> {
        val cleanToken = token.trim().uppercase()

        for (rule in rules) {
            val pattern = rule.tagPattern.trim().uppercase()
            val matches = when (rule.matchType) {
                "SUFFIX" -> cleanToken.endsWith(pattern) || cleanToken.contains(pattern)
                "PREFIX" -> cleanToken.startsWith(pattern)
                "CONTAINS" -> cleanToken.contains(pattern)
                "EXACT" -> cleanToken == pattern
                else -> cleanToken.contains(pattern)
            }
            if (matches) {
                return Pair(rule.rateAmount, "Matched ${rule.tagPattern} (₹${rule.rateAmount.toInt()})")
            }
        }

        // Fallback default: if token ends with digit or slot number
        val numMatch = Regex("""-(\d+)""").find(cleanToken)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toIntOrNull()
            if (num != null) {
                return if (num <= 30) {
                    Pair(30.0, "Rule -${num} (₹30)")
                } else {
                    Pair(40.0, "Rule -${num} (₹40)")
                }
            }
        }

        return Pair(30.0, "Default Base (₹30)")
    }

    /**
     * Parses input text. Input can be:
     * 1) "Alex: -25, -30, -35"
     * 2) "-25, -30, -35, -45, -55"
     * 3) Multi-line lobby lists:
     *    Lobby 1 -25
     *    Lobby 2 -45
     */
    fun parseInput(rawText: String, activeRules: List<RateRule>): List<StaffParsedBlock> {
        val effectiveRules = if (activeRules.isNotEmpty()) activeRules else DEFAULT_RULES
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val staffNamePattern = Regex("""^([A-Za-z0-9_ ]{2,30})\s*[:\-]\s*(.+)$""")

        val result = mutableListOf<StaffParsedBlock>()

        var hasNamedLines = false
        for (line in lines) {
            val match = staffNamePattern.find(line)
            if (match != null && !line.startsWith("-")) {
                hasNamedLines = true
                val name = match.groupValues[1].trim()
                val rest = match.groupValues[2].trim()
                val tokens = extractTokens(rest)
                val items = tokens.map { tok ->
                    val (rate, matchedRule) = resolveRate(tok, effectiveRules)
                    ParsedShiftItem(tag = tok, amount = rate, matchedRule = matchedRule)
                }
                if (items.isNotEmpty()) {
                    result.add(StaffParsedBlock(detectedStaffName = name, items = items))
                }
            }
        }

        if (hasNamedLines) {
            return result
        }

        // If no named lines, treat entire text as shifts for the selected staff member
        val allTokens = extractTokens(rawText)
        val items = allTokens.map { tok ->
            val (rate, matchedRule) = resolveRate(tok, effectiveRules)
            ParsedShiftItem(tag = tok, amount = rate, matchedRule = matchedRule)
        }
        if (items.isNotEmpty()) {
            return listOf(StaffParsedBlock(detectedStaffName = null, items = items))
        }

        return emptyList()
    }

    private fun extractTokens(text: String): List<String> {
        // Split by commas, newlines, semicolons, or pipe
        val chunks = text.split(Regex("""[,;\n|]+""")).map { it.trim() }.filter { it.isNotBlank() }
        val tokens = mutableListOf<String>()

        for (chunk in chunks) {
            // If chunk has multiple space-separated tokens e.g. "-25 -30 -35"
            val subTokens = chunk.split(Regex("""\s+""")).filter { it.isNotBlank() }
            if (subTokens.size > 1 && subTokens.all { it.startsWith("-") || it.contains("-") }) {
                tokens.addAll(subTokens)
            } else {
                tokens.add(chunk)
            }
        }
        return tokens
    }
}
