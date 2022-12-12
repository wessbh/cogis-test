package com.wassimbh.cogistest.utilities

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesProvider(private val context: Context) {

        private val sharedPrefName = "my_pref"
        private val preference: SharedPreferences
            get() = getSharedPref()

        /** method that return an instance of either an encrypted SharedPreferences if android version >= Android 6.0 Marshmallow
         *  or regular SharedPreferences if android version below Android 6.0 or in debug
         */
        private fun getSharedPref(): SharedPreferences {
            val sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
            return sharedPref!!
        }


        /**
         * method that insert a string to SharedPreference
         * @param key unique key
         * @param value value of string
         */
        fun insertString(key: String, value: String) {
            preference.edit().putString(key, value).apply()
        }

        /**
         * method that insert an int to SharedPreference
         * @param key unique key
         * @param value value of int
         */
        fun insertInt(key: String, value: Int) {
            preference.edit().putInt(key, value).apply()
        }

        /**
         * method that insert a long to SharedPreference
         * @param key The name of the preference to retrieve.
         * @param value value of long
         */
        fun insertLong(key: String, value: Long) {
            preference.edit().putLong(key, value).apply()
        }


        /**
         * method that insert a boolean to SharedPreference
         * @param key The name of the preference to modify.
         * @param value value of boolean
         */
        fun insertBoolean(key: String, value: Boolean) {
            preference.edit().putBoolean(key, value).apply()
        }

        /**
         * method that returns a string to SharedPreference
         * @param key The name of the preference to retrieve.
         * @param defaultValue Value to return if this preference does not exist.
         */
        fun getString(key: String, defaultValue: String): String{
            return preference.getString(key, defaultValue)!!

        }

        /**
         * method that returns an int to SharedPreference
         * @param key The name of the preference to retrieve.
         * @param defaultValue Value to return if this preference does not exist.
         */
        fun getInt(key: String, defaultValue: Int): Int{
            return preference.getInt(key, defaultValue)
        }
        /**
         * method that returns a Long to SharedPreference
         * @param key The name of the preference to retrieve.
         * @param defaultValue Value to return if this preference does not exist.
         */
        fun getLong(key: String, defaultValue: Long): Long {
            return preference.getLong(key, defaultValue)
        }

        /**
         * method that returns a boolean to SharedPreference
         * @param key The name of the preference to retrieve.
         * @param defaultValue Value to return if this preference does not exist.
         */
        fun getBool(key: String, defaultValue: Boolean): Boolean {
            return preference.getBoolean(key, defaultValue)
        }
}