package com.kolesova_violetta.ltc.exception

class DeviceException : RuntimeException() {
    override fun getLocalizedMessage(): String? {
        return "Ошибка датчика: Массы сохранены неверно\n" + super.getLocalizedMessage()
    }
}