package com.dsceksu.myelper.Models

class NotificationModel(val title: String? = null,
                        var id: String? = null,
                        val message: String? = null,
                        val date: Long? = null,
                        val key: String? = null,
                        val read: Boolean = false,
                        val productID: String? = null,
                        val sellerID: String? = null,
                        val productCategory: String? = null) {
}