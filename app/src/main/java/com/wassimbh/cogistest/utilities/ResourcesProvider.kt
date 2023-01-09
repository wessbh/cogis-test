package com.wassimbh.cogistest.utilities

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class ResourcesProvider(val context: Context) {

    /**
     * A method that returns a formatted string from a given resource id, concatinated
     * with the arguments passed as parameter
     * @param id resource id
     * @param argument the argument that will be concatenated in the string
     */
    fun getString(id: Int, vararg argument: String?): String = context.getString(id, argument)

    /**
     * a method that returns a string from a given resource name
     * @param resourceName resource name
     */
    fun getString(resourceName: String): String =
      getString(context.resources.getIdentifier(resourceName, "string", context.packageName), null)

    fun getDrawable(drawable: Int): Drawable? = ContextCompat.getDrawable(context, drawable)

    fun getDrawable(resourceName: String): Drawable? =
        getDrawable(context.resources.getIdentifier(resourceName, "drawable", context.packageName))

    fun getColor(color: Int): Int = ContextCompat.getColor(context, color)
    fun getColorStateList(id: Int): ColorStateList? {
        return ResourcesCompat.getColorStateList(context.resources, id, context.theme)
    }

    fun getCachePath():String{
        return  context.cacheDir.path
    }
}