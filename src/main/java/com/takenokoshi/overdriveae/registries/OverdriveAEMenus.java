package com.takenokoshi.overdriveae.registries;

import com.takenokoshi.overdriveae.OverdriveAE;
import com.takenokoshi.overdriveae.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.menu.OverdriveMachineMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OverdriveAEMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            OverdriveAE.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<OverdriveMachineMenu>> OVERDRIVE_MACHINE = MENU_TYPES
            .register("overdrive_machine", () -> OverdriveMachineMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<OverdriveInscriberMenu>> OVERDRIVE_INSCRIBER = MENU_TYPES
            .register("overdrive_inscriber", () -> OverdriveInscriberMenu.TYPE);
}
