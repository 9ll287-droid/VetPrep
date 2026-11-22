package com.vetprep.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.*

const val PRODUCT_ID = "flashcards_premium"

class Billing(private val act: Activity) : PurchasesUpdatedListener {
    private val sp = act.getSharedPreferences("vp", Context.MODE_PRIVATE)
    var premium by mutableStateOf(sp.getBoolean("premium", false))
    var price by mutableStateOf("₹49")
    private var details: ProductDetails? = null
    private val client = BillingClient.newBuilder(act).setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()

    private fun set(v: Boolean) { premium = v; sp.edit().putBoolean("premium", v).apply() }
    private fun owned(p: Purchase) = p.purchaseState == Purchase.PurchaseState.PURCHASED && p.products.contains(PRODUCT_ID)
    private fun ack(p: Purchase) {
        if (p.purchaseState == Purchase.PurchaseState.PURCHASED && !p.isAcknowledged)
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) {}
    }

    fun start() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(r: BillingResult) {
                if (r.responseCode != BillingClient.BillingResponseCode.OK) return
                val q = QueryProductDetailsParams.newBuilder().setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build())).build()
                client.queryProductDetailsAsync(q) { _, list ->
                    details = list.firstOrNull()
                    details?.oneTimePurchaseOfferDetails?.formattedPrice?.let { price = it }
                }
                client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { res, list ->
                    if (res.responseCode == BillingClient.BillingResponseCode.OK) { set(list.any { owned(it) }); list.forEach { ack(it) } }
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun buy() {
        val d = details
        if (d == null) { Toast.makeText(act, "Store not ready. Install the app from Google Play and try again.", Toast.LENGTH_LONG).show(); return }
        client.launchBillingFlow(act, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).build())).build())
    }

    override fun onPurchasesUpdated(r: BillingResult, list: MutableList<Purchase>?) {
        if (r.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            list.forEach { ack(it) }
            if (list.any { owned(it) }) { set(true); Toast.makeText(act, "Flashcards unlocked. Thank you!", Toast.LENGTH_LONG).show() }
        } else if (r.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) Toast.makeText(act, "Purchase did not complete.", Toast.LENGTH_LONG).show()
    }
}
