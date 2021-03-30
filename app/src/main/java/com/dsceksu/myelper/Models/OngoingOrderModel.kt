package com.dsceksu.myelper.Models

class OngoingOrderModel(
        val orderTotalAmount: String? = null,
        val productID: String? = null,
        val paymentTransactionRef: String? = null,
        val paymentTransactionID: String? = null,
        val paymentGatewayResponse: String? = null,
        val cancelledPrice: String? = null,
        val category: String? = null,
        val deliveryCharge: String? = null,
        val productPrice: String? = null,
        val status: String? = null,
        val buyer: String? = null,
        val seller: String? = null,
        val quantity: String? = null,
        val title: String? = null,
        val deliveryDays: String? = null,
        val date: Any? = null,
        val shipped: Boolean? = null,
        val received: Boolean? = null,
        val delivered: Boolean? = null,
        val confirmed: Boolean? = null,
        val productImage: String? = null,
        var orderID:String? = null
) {
}
