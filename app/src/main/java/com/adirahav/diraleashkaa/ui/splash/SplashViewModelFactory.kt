package com.adirahav.diraleashkaa.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.AnnouncementService
import com.adirahav.diraleashkaa.data.network.services.UserService

class SplashViewModelFactory(private val activity: SplashActivity,
                             private val userService: UserService,
                             private val announcementService: AnnouncementService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(activity, userService, announcementService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}