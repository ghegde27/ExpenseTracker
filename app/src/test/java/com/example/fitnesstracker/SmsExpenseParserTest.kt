package com.example.fitnesstracker
import com.example.fitnesstracker.sms.SmsExpenseParser
import org.junit.Assert.*
import org.junit.Test
class SmsExpenseParserTest { @Test fun detectsDebit(){val x=SmsExpenseParser.parse("INR 450 debited at XYZ Fuel Station");assertNotNull(x);assertEquals(450,x!!.amount)} }
