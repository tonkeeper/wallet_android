package com.tonkeeper.api.method

import com.tonkeeper.api.model.JettonItemModel
import org.json.JSONObject

class JettonsMethod(
    address: String
): BaseMethod<List<JettonItemModel>>("accounts/$address/jettons") {

    override fun parseJSON(response: JSONObject): List<JettonItemModel> {
        val array = response.getJSONArray("balances")
        val list = mutableListOf<JettonItemModel>()
        for (i in 0 until array.length()) {
            list.add(JettonItemModel(array.getJSONObject(i)))
        }
        return list
    }
}