package com.example.mixin;

import com.example.utils.ProjectProps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadLevel()V
        System.out.println("Hello from ExampleMixin! The server is starting up.");
        System.out.println("ModID: " + ProjectProps.getSafe("modId") + ", ModName: " + ProjectProps.getSafe("modName"));
    }
}