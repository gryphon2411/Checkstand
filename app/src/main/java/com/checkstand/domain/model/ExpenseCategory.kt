package com.checkstand.domain.model

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD_DINING("Food & Dining", "🍽️"),
    GROCERIES("Groceries", "🛒"),
    TRANSPORTATION("Transportation", "🚗"),
    UTILITIES("Utilities", "⚡"),
    OFFICE_SUPPLIES("Office Supplies", "📄"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH_MEDICAL("Health & Medical", "🏥"),
    SHOPPING("Shopping", "🛍️"),
    SERVICES("Services", "🔧"),
    UNCATEGORIZED("Uncategorized", "🏷️"),
    OTHER("Other", "📦")
}
