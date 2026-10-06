package com.example.fitnesstracker

import com.example.fitnesstracker.sms.SmsExpenseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsExpenseParserTest {

    @Test
    fun test1_accountDebitedByRs500() {
        val result = SmsExpenseParser.parse("Your account has been debited by Rs.500")
        assertNotNull(result)
        assertEquals(500L, result!!.amount)
    }

    @Test
    fun test2_spentAtSwiggy() {
        val result = SmsExpenseParser.parse("INR 1,250 spent at SWIGGY")
        assertNotNull(result)
        assertEquals(1250L, result!!.amount)
        assertTrue(result.source.contains("SWIGGY", ignoreCase = true))
    }

    @Test
    fun test3_upiPaymentToZomatoSuccessful() {
        val result = SmsExpenseParser.parse("UPI payment of Rs.299 to ZOMATO was successful")
        assertNotNull(result)
        assertEquals(299L, result!!.amount)
        assertTrue(result.source.contains("ZOMATO", ignoreCase = true))
    }

    @Test
    fun test4_hdfcAccountBalanceIgnored() {
        val result = SmsExpenseParser.parse("Your HDFC account balance is Rs.25,000")
        assertNull(result)
    }

    @Test
    fun test5_creditedIgnored() {
        val result = SmsExpenseParser.parse("Rs.500 credited to your account")
        assertNull(result)
    }

    @Test
    fun test6_cashbackIgnored() {
        val result = SmsExpenseParser.parse("Cashback of Rs.100 received")
        assertNull(result)
    }

    @Test
    fun test7_transactionFailedIgnored() {
        val result = SmsExpenseParser.parse("Transaction failed for Rs.500")
        assertNull(result)
    }

    @Test
    fun test8_otpIgnored() {
        val result = SmsExpenseParser.parse("OTP for transaction is 123456")
        assertNull(result)
    }

    @Test
    fun test9_paidToAmazon() {
        val result = SmsExpenseParser.parse("₹1,499 paid to Amazon")
        assertNotNull(result)
        assertEquals(1499L, result!!.amount)
    }

    @Test
    fun test10_iciciAccountDebitedByINR750() {
        val result = SmsExpenseParser.parse("Your ICICI Bank account has been debited by INR 750")
        assertNotNull(result)
        assertEquals(750L, result!!.amount)
    }

    @Test
    fun test11_availableBalanceIgnored() {
        val result = SmsExpenseParser.parse("Available balance is Rs.15,000")
        assertNull(result)
    }

    @Test
    fun test12_paidToSwiggyViaPhonePe() {
        val result = SmsExpenseParser.parse("₹500 paid to Swiggy via PhonePe")
        assertNotNull(result)
        assertEquals(500L, result!!.amount)
        assertTrue(result.source.contains("Swiggy", ignoreCase = true))
    }
}
