package de.xyndra.examplemod.options

import de.xyndra.examplemod.BooleanConfigOption
import de.xyndra.examplemod.Globals
import de.xyndra.examplemod.IntConfigOption
import de.xyndra.examplemod.utils.LoadKt

@LoadKt
object TestOptions {
    val shouldPrint = BooleanConfigOption(true)
    val testInt1 = object : IntConfigOption() {
        override var value = 2
        override fun predicate(new: Any): Boolean {
            return super.predicate(new) && (new as Int) % 2 == 0
        }
    }
    val testInt2 = IntConfigOption(1, 1, 9999)

    fun load() {
        Globals.configOptions["should_print"] = shouldPrint
        Globals.configOptions["test_int_1"] = testInt1
        Globals.configOptions["test_int_2"] = testInt2
    }
}