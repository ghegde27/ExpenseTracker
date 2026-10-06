package com.example.fitnesstracker

import com.example.fitnesstracker.voice.VoiceParser
import org.junit.Assert.*
import org.junit.Test

class VoiceParserTest {
    @Test fun parsesIncome() { val x=VoiceParser.parse("Add 500 rupees income from John"); assertEquals(VoiceParser.VoiceTransaction.Type.INCOME,x.type); assertEquals(500,x.amount); assertEquals("John",x.clientName) }
    @Test fun parsesSpokenAmount() { val x=VoiceParser.parse("Add five hundred rupees income from John"); assertEquals(500,x.amount) }
    @Test fun parsesExpense() { val x=VoiceParser.parse("Add 300 rupees petrol expense"); assertEquals(VoiceParser.VoiceTransaction.Type.EXPENSE,x.type); assertEquals(300,x.amount) }
}
