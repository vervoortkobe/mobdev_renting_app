package edu.ap.mobiledevrentingapp

data class Device(
    val description: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val imageIds: List<String> = emptyList(),
    val ownerId: String = "",
    val price: String = "",
    val selectedCategory: String = ""
)