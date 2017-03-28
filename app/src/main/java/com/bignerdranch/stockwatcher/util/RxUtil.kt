package com.bignerdranch.stockwatcher.util

import com.bignerdranch.stockwatcher.ui.RxFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private val LOADING_MESSAGE = "Loading"

fun <T> Observable<T>.applyUIDefaults(rxFragment: RxFragment): Observable<T> =
        this.addToCompositeDisposable(rxFragment)
                .applySchedulers()
                .applyRequestStatus(rxFragment)
                .showLoadingDialog(rxFragment)

private fun <T> Observable<T>.applySchedulers(): Observable<T> = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

private fun <T> Observable<T>.addToCompositeDisposable(rxFragment: RxFragment): Observable<T> =
        this.doOnSubscribe {
            rxFragment.compositeDisposable.add(it)
        }

private fun <T> Observable<T>.applyRequestStatus(rxFragment: RxFragment): Observable<T> =
        this.doOnSubscribe { rxFragment.requestInProgress = true }
                .doOnTerminate { rxFragment.requestInProgress = false }

private fun <T> Observable<T>.showLoadingDialog(rxFragment: RxFragment): Observable<T> =
        this.doOnSubscribe { DialogUtils.showProgressDialog(rxFragment.fragmentManager, LOADING_MESSAGE) }
                .doOnTerminate { DialogUtils.hideProgressDialog(rxFragment.fragmentManager) }