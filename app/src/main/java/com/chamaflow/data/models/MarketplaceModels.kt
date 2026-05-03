package com.chamaflow.data.models

data class MarketplaceListing(
    val id: String = "",
    val chamaId: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val category: MarketplaceCategory = MarketplaceCategory.OTHER,
    val timestamp: Long = System.currentTimeMillis(),
    val isSold: Boolean = false
)

enum class MarketplaceCategory {
    GOODS, SERVICES, ELECTRONICS, FASHION, FOOD, HOME, OTHER
}
