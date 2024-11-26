package edu.ap.mobiledevrentingapp.firebase

data class User(
    val addressNr: String = "",
    val city: String = "",
    val country: String = "",
    val fullName: String = "",
    val ibanNumber: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val streetName: String = "",
    val userId: String = "",
    val zipCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Device(
    val description: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val imageIds: List<String> = emptyList(),
    val ownerId: String = "",
    val price: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class DeviceImage(
    val imageId: String = "",
    val imageUrl: String = ""
)

data class Rental(
    val rentalId: String = "",
    val deviceId: String = "",
    val ownerId: String = "",
    val renterId: String = "",
    val startDate: String = "",
    val endDate: String = ""
)

enum class DeviceCategory {
    BATHROOM,
    BEDROOM,
    CLEANING,
    CONSTRUCTION,
    ELECTRIC,
    FURNITURE,
    GARDEN,
    HEATING_COOLING,
    HOUSEHOLD,
    KITCHEN,
    LAUNDRY,
    LIGHTING,
    MOVING,
    OTHER,
    PETS,
    PLUMBING,
    SAFETY,
    SECURITY,
    STORAGE,
    TECHNOLOGY,
    TOILET,
    TOOLS,
    VENTILATION,
    WASHING,
}