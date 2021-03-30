package com.dsceksu.myelper.Models

data class TransactionHistoryModel(
    val transactionID: String? = null,
    val title: Any? = null,
    val userID: Any? = null,
    val date: Any? = null,
    val paymentGateway: Any? = null,
    val amount: Any? = null,
    val totalAmountPaid: Any? = null,
    val status: Any? = null,
    val transactionMessage: Any? = null,
    val productsPurchased: Any? = null,
    val paymentTransactionRef: Any? = null,
    val paymentTransactionID: Any? = null,
    val paymentGatewayResponse: Any? = null,
    val amountSettled: Any? = null,
    val productPrice: Any? = null,
    val productQuantity: Any? = null,
    val productDeliveryCharge: Any? = null,
    val sponsoredWeeks: Any? = null,
    val content: Any? = null,
    val number: Any? = null,
    val email: Any? = null,
    val website: Any? = null,
    val image: Any? = null,
    val productImage: String? = null,
    val productTitle: String? = null,
    val postTitle: String? = null,
    val slot:String? = null
) {
}
