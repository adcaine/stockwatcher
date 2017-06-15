package com.bignerdranch.stockwatcher.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bignerdranch.stockwatcher.R
import com.bignerdranch.stockwatcher.StockWatcherApplication
import com.bignerdranch.stockwatcher.databinding.FragmentStockInfoBinding
import com.bignerdranch.stockwatcher.model.service.StockInfoForSymbol
import com.bignerdranch.stockwatcher.model.service.repository.StockDataRepository
import com.bignerdranch.stockwatcher.util.applyUIDefaults
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class StockInfoFragment : RxFragment() {

    @Inject
    internal lateinit var stockDataRepository: StockDataRepository
    private lateinit var binding: FragmentStockInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        StockWatcherApplication.getAppComponent(context).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate<FragmentStockInfoBinding>(inflater!!, R.layout.fragment_stock_info, container, false)
        binding.fetchDataButton.apply {
            setOnClickListener {
                binding.errorMessage.visibility = View.GONE
                loadRxData()
            }
        }

        binding.tickerSymbol.setOnEditorActionListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                loadRxData()
            }
            true
        }

        binding.clearCacheButton.setOnClickListener {
            stockDataRepository.clearCache()
            Toast.makeText(context, "observable cache cleared!", Toast.LENGTH_LONG).show()
        }
        return binding.root
    }

    override fun loadRxData() {
        Observable.just(binding.tickerSymbol.text.toString())
                .applyUIDefaults(this)
                .filter { it.isNotEmpty() }
                .singleOrError()
                .toObservable()
                .flatMap({ stockDataRepository.getStockInfoForSymbol(it) })
                .subscribe({ this.displayStockResults(it) }, { this.displayErrors(it) })
    }

    private fun displayErrors(throwable: Throwable) {
        var message = throwable.message
        if (throwable is NoSuchElementException) {
            message = "Enter a stock symbol first!!"
        }
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = message
    }

    private fun displayStockResults(stockInfoForSymbol: StockInfoForSymbol) {
        binding.stockValue.text = stockInfoForSymbol.toString()
    }
}