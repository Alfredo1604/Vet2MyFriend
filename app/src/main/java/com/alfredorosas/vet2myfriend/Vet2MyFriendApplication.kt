package com.alfredorosas.vet2myfriend

import android.app.Application
import com.alfredorosas.vet2myfriend.data.directory.LocalClinicDirectory
import com.alfredorosas.vet2myfriend.data.directory.RoomClinicRepository
import com.alfredorosas.vet2myfriend.domain.ClinicRepository

class Vet2MyFriendApplication : Application() {
    val clinicRepository: ClinicRepository by lazy {
        RoomClinicRepository(LocalClinicDirectory.open(this))
    }
}
