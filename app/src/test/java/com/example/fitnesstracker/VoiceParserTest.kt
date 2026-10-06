package com.example.fitnesstracker

import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.voice.VoiceParser
import com.example.fitnesstracker.voice.VoiceTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceParserTest {

    @Test
    fun test1_spent500OnFood() {
        val result = VoiceParser.parse("Spent 500 on food")
        assertEquals(VoiceTransaction.Type.EXPENSE, result.type)
        assertEquals(500L, result.amount)
        assertEquals(ExpenseCategory.FOOD, result.category)
    }

    @Test
    fun test2_paid1200ForFuel() {
        val result = VoiceParser.parse("Paid 1200 for fuel")
        assertEquals(VoiceTransaction.Type.EXPENSE, result.type)
        assertEquals(1200L, result.amount)
        assertEquals(ExpenseCategory.FUEL, result.category)
    }

    @Test
    fun test3_boughtEquipmentFor5000() {
        val result = VoiceParser.parse("Bought equipment for 5000")
        assertEquals(VoiceTransaction.Type.EXPENSE, result.type)
        assertEquals(5000L, result.amount)
        assertEquals(ExpenseCategory.EQUIPMENT, result.category)
    }

    @Test
    fun test4_spent350AtSwiggy() {
        val result = VoiceParser.parse("Spent 350 at Swiggy")
        assertEquals(VoiceTransaction.Type.EXPENSE, result.type)
        assertEquals(350L, result.amount)
        assertEquals(ExpenseCategory.FOOD, result.category)
        assertNotNull(result.description)
        assertTrue(result.description!!.contains("Swiggy", ignoreCase = true))
    }

    @Test
    fun test5_received25000Salary() {
        val result = VoiceParser.parse("Received 25000 salary")
        assertEquals(VoiceTransaction.Type.INCOME, result.type)
        assertEquals(25000L, result.amount)
    }

    @Test
    fun test6_got5000FromRavi() {
        val result = VoiceParser.parse("Got 5000 from Ravi")
        assertEquals(VoiceTransaction.Type.INCOME, result.type)
        assertEquals(5000L, result.amount)
        assertEquals("Ravi", result.clientName)
    }

    @Test
    fun test7_received10000FromFreelanceWork() {
        val result = VoiceParser.parse("Received 10000 from freelance work")
        assertEquals(VoiceTransaction.Type.INCOME, result.type)
        assertEquals(10000L, result.amount)
    }

    @Test
    fun test8_got2000Bonus() {
        val result = VoiceParser.parse("Got 2000 bonus")
        assertEquals(VoiceTransaction.Type.INCOME, result.type)
        assertEquals(2000L, result.amount)
    }

    @Test
    fun test9_spentOneThousandTwoHundredOnFuel() {
        val result = VoiceParser.parse("Spent one thousand two hundred on fuel")
        assertEquals(VoiceTransaction.Type.EXPENSE, result.type)
        assertEquals(1200L, result.amount)
        assertEquals(ExpenseCategory.FUEL, result.category)
    }

    @Test
    fun test10_receivedOneLakhSalary() {
        val result = VoiceParser.parse("Received one lakh salary")
        assertEquals(VoiceTransaction.Type.INCOME, result.type)
        assertEquals(100000L, result.amount)
    }

    @Test
    fun test11_ambiguous500() {
        val result = VoiceParser.parse("500")
        assertEquals(VoiceTransaction.Type.UNKNOWN, result.type)
        assertEquals(500L, result.amount)
    }

    @Test
    fun test12_foodNoAmount() {
        val result = VoiceParser.parse("Food")
        assertNull(result.amount)
    }
}
