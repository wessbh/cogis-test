package com.wassimbh.cogistest.utilities

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A helper Object for handling coroutine operations
 */
object Coroutines {

    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }

    fun io(work: suspend (() -> Unit)) =


        CoroutineScope(Dispatchers.IO).launch(handler) {

            work()


        }


    fun default(work: suspend (() -> Unit)) =

        CoroutineScope(Dispatchers.Default).launch {
            work()
        }

}