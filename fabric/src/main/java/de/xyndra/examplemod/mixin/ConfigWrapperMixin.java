package de.xyndra.examplemod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.xyndra.examplemod.ConfigOption;
import de.xyndra.examplemod.ExampleModConfigWrapper;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ConfigWrapper.class)
public class ConfigWrapperMixin {
    @Inject(method = "collectFieldValues", at = @At("HEAD"), cancellable = true, remap = false)
    private void collectFieldValues(Option.Key parent, Object instance, Map<Option.Key, Option.BoundField<Object>> fields, CallbackInfo ci) {
        if ((Object) this instanceof ExampleModConfigWrapper) {
            ExampleModConfigWrapper.Companion.collectFieldValues(parent, fields);
            ci.cancel();
        }
    }

    @ModifyVariable(method = "initializeOptions", at = @At(value = "INVOKE_ASSIGN", target = "Lio/wispforest/owo/config/Option$BoundField;getValue()Ljava/lang/Object;"), remap = false)
    private ConfigWrapper.Constraint initializeOptions(ConfigWrapper.Constraint constraint, @Local Option.BoundField<Object> boundField) {
        if ((Object) this instanceof ExampleModConfigWrapper) {
            return new ConfigWrapper.Constraint("constraint", (newobj) -> {
                if (boundField.owner() instanceof ConfigOption<?> configOption) {
                    return configOption.predicate(newobj);
                } else {
                    String className = boundField.owner().getClass().getName();
                    System.err.println("ConfigWrapperMixin: Constraint check failed for " + className + " with value: " + newobj);
                    return false;
                }
            });
        } else {
            return constraint;
        }
    }
}
