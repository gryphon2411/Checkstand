package com.checkstand

import org.junit.Test
import org.junit.Assert.*
import com.checkstand.domain.model.*
import java.math.BigDecimal
import java.util.Date

/**
 * Test basic receipt functionality
 */
class ReceiptTest {
    
    @Test
    fun testReceiptCreation() {
        val receipt = Receipt(
            merchantName = "Test Store",
            totalAmount = BigDecimal("25.99"),
            date = Date(),
            items = listOf(
                ReceiptItem(
                    name = "Coffee",
                    quantity = 2,
                    unitPrice = BigDecimal("4.50")
                ),
                ReceiptItem(
                    name = "Sandwich", 
                    quantity = 1,
                    unitPrice = BigDecimal("16.99")
                )
            ),
            category = ExpenseCategory.FOOD_DINING
        )
        
        assertEquals("Test Store", receipt.merchantName)
        assertEquals(BigDecimal("25.99"), receipt.totalAmount)
        assertEquals(ExpenseCategory.FOOD_DINING, receipt.category)
        assertEquals(2, receipt.items.size)
        assertEquals("Coffee", receipt.items[0].name)
        assertEquals(2, receipt.items[0].quantity)
    }
    
    @Test
    fun testExpenseCategories() {
        val categories = ExpenseCategory.values()
        assertTrue(categories.contains(ExpenseCategory.FOOD_DINING))
        assertTrue(categories.contains(ExpenseCategory.GROCERIES))
        assertTrue(categories.contains(ExpenseCategory.OFFICE_SUPPLIES))
        assertTrue(categories.contains(ExpenseCategory.UNCATEGORIZED))
    }
    
    @Test
    fun testReceiptItemCalculation() {
        val item = ReceiptItem(
            name = "Office Supplies",
            quantity = 3,
            unitPrice = BigDecimal("12.50")
        )
        
        val expectedTotal = BigDecimal("12.50").multiply(BigDecimal(3))
        assertEquals(expectedTotal, item.totalPrice)
    }
}
