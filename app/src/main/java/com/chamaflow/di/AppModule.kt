package com.chamaflow.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = firestoreSettings { setLocalCacheSettings(memoryCacheSettings {}) }
        return db
    }

    @Provides @Singleton
    fun provideMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides @Singleton
    fun provideContext(@ApplicationContext ctx: Context): Context = ctx
}
