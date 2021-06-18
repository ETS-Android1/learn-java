/**
 * The package of everything database related. In general, the progress of the user is stored inside
 * the database. Simpler things use the {@link android.content.SharedPreferences} instead.
 * <p>
 * Database is managed with the Room library. The core class is {@link com.gaspar.learnjava.database.LearnJavaDatabase}
 * which defines the database structure using the model classes annotated with {@link androidx.room.Entity}.
 * Operations can be performed with the {@link androidx.room.Dao} interfaces.
 */
package com.gaspar.learnjava.database;