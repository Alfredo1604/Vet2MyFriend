package com.alfredorosas.vet2myfriend.domain.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClinicServiceTest {
    @Test fun freeAndUnknownPricesRemainDifferent() {
        assertEquals(0, ClinicService(BigDecimal.ZERO, "Consulta").price?.signum())
        assertNull(ClinicService(null, "Consulta").price)
        assertNull(ClinicService(BigDecimal.ZERO, "Consulta").doctor)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativePrices() { ClinicService(BigDecimal("-0.01"), "Consulta") }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankServiceNames() { ClinicService(BigDecimal.ZERO, " ") }
}
