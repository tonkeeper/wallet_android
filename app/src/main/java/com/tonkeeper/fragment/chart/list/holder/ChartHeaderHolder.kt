package com.tonkeeper.fragment.chart.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonkeeper.R
import com.tonkeeper.fragment.chart.list.ChartItem

class ChartHeaderHolder(
    parent: ViewGroup
): ChartHolder<ChartItem.Header>(parent, R.layout.view_chart_header) {

    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)

    override fun onBind(item: ChartItem.Header) {
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance
    }
}